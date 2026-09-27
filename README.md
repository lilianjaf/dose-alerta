# DoseAlerta

Sistema de lembretes de medicação: lê a receita por foto, agenda os alarmes e avisa o paciente por WhatsApp e ligação até ele confirmar a dose.


## Módulos

Projeto Gradle multi-módulo, um Spring Boot app por módulo:

| Módulo | Porta | Descrição |
|---|---|---|
| `api-gateway` | 8080 | Ponto único de entrada: valida JWT, rate limit, CORS e correlation-id, e roteia só o que o paciente/profissional usa (ver abaixo) |
| `modulo-usuario` | 8081 | Cadastro de paciente, autenticação (JWT) |
| `modulo-ia` | 8082 | Extração da receita via IA |
| `modulo-scheduler` | 8083 | Decide quando cada etapa do alarme dispara |
| `modulo-notificacao` | 8084 | Decide o canal de cada etapa do escalonamento |
| `modulo-mensageria` | 8085 | Adapter Twilio (WhatsApp + Voice) |
| `modulo-relatorio-adesao` | 8086 | Taxa de adesão por paciente/medicamento |

Cada módulo segue a convenção de pacotes `core` (domínio, usecases, portas) / `infra` (implementações, Spring, adapters externos).

## Subindo o ambiente localmente

1. Copie `.env.example` para `.env` e preencha as variáveis (chave pública JWT, credenciais Twilio). As variáveis precisam estar exportadas no shell onde os módulos forem rodados (`export $(cat .env | xargs)` ou equivalente do seu terminal/IDE).

2. Suba o Postgres e o Jaeger:

   ```bash
   docker-compose up -d
   ```

   Banco disponível em `localhost:5433` (db `dose_alerta`, user/senha `dose_alerta`). Jaeger (tracing) em `localhost:16686`.

3. Rode um módulo específico:

   ```bash
   ./gradlew :modulo-usuario:bootRun
   ```

   Troque `:modulo-usuario` pelo módulo desejado (`:api-gateway`, `:modulo-ia`, `:modulo-scheduler`, `:modulo-notificacao`, `:modulo-mensageria`, `:modulo-relatorio-adesao`). Para o fluxo ponta-a-ponta, rode os módulos relevantes em terminais separados.

4. Build e testes de todos os módulos:

   ```bash
   ./gradlew build
   ```

## API Gateway (api-gateway)

Todo acesso de cliente (front, Postman) deve passar pelo gateway (`http://localhost:8080`). Rotas expostas em `api-gateway/src/main/resources/application.yaml`:

| Rota | Módulo | JWT |
|---|---|---|
| `POST /pacientes`, `POST /auth/login` | `modulo-usuario` (8081) | não (públicas) |
| `/receitas/**` | `modulo-ia` (8082) | sim |
| `GET /alarmes/{id}` | `modulo-scheduler` (8083) | sim |
| `GET /pacientes/{id}/adesao` | `modulo-relatorio-adesao` (8086) | sim |
| `POST /webhooks/twilio/**` | `modulo-mensageria` (8085) | não (a Twilio não manda JWT; a autenticidade é validada por assinatura, ver seção Twilio) |

**Não são expostos** (chamados só entre módulos, sem JWT): `POST /alarmes`, `/alarmes/confirmacoes`, `/alarmes/ligacoes/atendidas`, `/notificacoes/**`, `/mensagens/**`, `/ligacoes/**`, `/interacoes`. No gateway eles respondem 404 (ou 401 sem token, antes mesmo de checar a rota). **Isso é o que torna seguro expor o gateway na internet** (via ngrok, por exemplo) para receber os webhooks da Twilio: só `/webhooks/twilio/**` fica acessível sem token, e o `modulo-mensageria` continua exigindo a assinatura da Twilio nele. Nunca exponha o `modulo-mensageria` (8085) direto — ele não tem essa proteção nos demais endpoints. Ao criar um endpoint novo para o paciente, é preciso adicionar a rota (e um teste em `GatewayRoutingIntegrationTest`).

## Twilio (modulo-mensageria)

`TWILIO_WHATSAPP_NUMBER` e `TWILIO_VOICE_NUMBER` são números **da Twilio** (o remetente das mensagens/ligações), não o seu celular. O seu número entra como `telefone` nas requisições (o destinatário) — e em conta Trial ele precisa estar habilitado no Twilio antes de receber qualquer coisa (passo 3).

1. Crie uma conta Twilio.
   - **WhatsApp:** ative o **Sandbox** (Messaging > Try it out > Send a WhatsApp message). O Console mostra o número do sandbox (ex: `+14155238886`) e um código de join (ex: `join palavra-aleatoria`); use esse número em `TWILIO_WHATSAPP_NUMBER`.
   - **Voz:** o número do sandbox de WhatsApp **não faz ligação**. É preciso um número próprio com capacidade de voz (Phone Numbers > Manage > Buy a number; contas Trial ganham um grátis). Use esse número em `TWILIO_VOICE_NUMBER`. Se a conta não tiver nenhum número (`IncomingPhoneNumbers` vazio na API), as ligações falham mesmo com o resto certo.
   - Preencha `TWILIO_ACCOUNT_SID` e `TWILIO_AUTH_TOKEN` no `.env`.
2. **Habilite o seu número de teste** (ele é quem recebe, não quem envia):
   - **WhatsApp:** do seu celular, mande a mensagem de join (`join <código>`) para o número do sandbox. Só depois disso a Twilio entrega mensagens a esse número; sem isso o envio falha com o erro 572002 ("No Twilio trial phone number is assigned for messaging to this destination number").
   - **Ligação:** em conta Trial, o número de destino precisa estar em Verified Caller IDs (Console > Phone Numbers > Verified Caller IDs).
3. Para receber os webhooks (resposta do paciente, status de ligação), alguém precisa alcançar seu ambiente publicamente. **Exponha o api-gateway (porta 8080), nunca o `modulo-mensageria` direto** — o gateway só deixa passar sem token o que é assinado pela Twilio (ver seção API Gateway); o resto exigiria JWT ou nem existe nele.
   ```bash
   ngrok http 8080
   ```
   Preencha `TWILIO_WEBHOOK_BASE_URL` no `.env` com a URL gerada (a do gateway, ex: `https://algo.ngrok-free.app`) e reinicie o `modulo-mensageria` (é ele quem valida a assinatura contra essa URL).
4. Configure no console Twilio, apontando para a URL do gateway:
   - Sandbox do WhatsApp → "When a message comes in": `{TWILIO_WEBHOOK_BASE_URL}/webhooks/twilio/mensagens` (só ajustável pela UI do Console; não há endpoint de API público para isso).
   - As ligações de confirmação e o status callback são configurados automaticamente pelo próprio `modulo-mensageria` a cada chamada (`/webhooks/twilio/ligacoes/confirmacao` e `/webhooks/twilio/ligacoes/status`) — nada a fazer no Console.

## IA (modulo-ia)

### Configuração

A extração usa a API do Google Gemini (`generateContent`, com saída estruturada). Preencha `GEMINI_API_KEY` no `.env` (chave em https://aistudio.google.com/apikey). Propriedades em `modulo-ia/src/main/resources/application.yaml`:

| Propriedade | Padrão | Descrição |
|---|---|---|
| `ia.modelo` | `gemini-3.5-flash-lite` | Modelo usado. Só servem modelos com `generateContent` (os "Live" usam outro protocolo e não funcionam aqui) |
| `ia.gemini.temperature` | `0.1` | Baixa, para a extração ser o mais determinística possível |
| `ia.gemini.max-output-tokens` | `2048` | Inclui os tokens de raciocínio do modelo, além do JSON |
| `ia.gemini.read-timeout-ms` | `30000` | Timeout de leitura da chamada |
| `ia.gemini.retry-backoff-ms` | `1000` | Espera antes da 1ª nova tentativa (dobra a cada tentativa, até 4 tentativas) |

Erros da API do Google: 5xx ("high demand") e falhas de rede são tentados de novo; **429 (cota) não** (repetir só gasta mais cota) e devolve "Limite de uso do modelo de visão atingido". A chave gratuita tem limites baixos por modelo; se estourar, aguarde, troque `ia.modelo` ou ative o faturamento no projeto Google. O motivo real de cada falha aparece no log (`Chamada ao Gemini falhou`).

### Fluxo

1. `POST /receitas/extrair` (multipart: `imagem` JPEG/PNG/WEBP, `pacienteId`, `telefone`, `horarioInicial`) lê a foto e devolve `{ "receitas": [...], "naoProcessados": [...] }`: **uma receita `AGUARDANDO_CONFIRMACAO` por medicamento** da foto, e cada uma vira um alarme quando confirmada.
2. **Só receita formal é aceita**: a imagem precisa ser uma receita, com nome e registro profissional (CRM de médico ou CRO de dentista) do prescritor. Caso contrário a API responde 422 orientando a usar apenas medicamentos indicados por um profissional habilitado, mediante receita formal (o campo `motivo` diz o que faltou).
3. **Nada é inventado nem descartado por falta de dado.** Dose, frequência (`frequenciaHoras`, de 1h a 168h, ou seja, até semanal) e duração (`duracaoDias`) que a receita não traz, ou que não são legíveis, vêm `null` e ficam listadas em `camposPendentes` de cada receita. Só o item sem nome legível não vira receita (vai em `naoProcessados`). Para "uso contínuo" a duração assumida é 30 dias.
4. `POST /receitas/{id}/confirmar` confirma uma receita. O corpo é opcional: sem corpo, ou com campos omitidos, mantém o que foi extraído; só os campos enviados (`medicamento`, `dose`, `frequenciaHoras`, `duracaoDias`) corrigem a extração. Enquanto faltar dose, frequência ou duração, responde **422** com `camposPendentes`: pergunte ao paciente e envie os campos. **Sem todos os dados não há alarme.**
5. Ao confirmar, um `ReceitaConfirmadaEvent` vai para o outbox e o publisher cria o alarme da primeira dose no `modulo-scheduler`. Cada medicamento tem seu próprio alarme.

Testes de regressão do prompt ficam em `modulo-ia/src/test/resources/harness-receitas/` e não rodam no `./gradlew test`. Ver o `README.md` do diretório.

## Scheduler (modulo-scheduler)

`POST /alarmes` é **idempotente por paciente + medicamento**: se o paciente já tem um alarme `PENDENTE` do mesmo medicamento (nome comparado sem diferenciar maiúsculas), nenhum alarme novo é criado e o existente volta com **HTTP 200** (alarme novo: **201**). Isso evita duplicar alarmes quando o outbox reentrega o evento ou a mesma receita é confirmada duas vezes. Depois que o alarme deixa de ser `PENDENTE`, um novo do mesmo medicamento é aceito. Detalhes e limitações em `md/CONTRATOS_EVENTOS.md`.

## Relatório de adesão (modulo-relatorio-adesao)

1. Recebe `InteracaoRegistradaEvent` do `modulo-scheduler` em `POST /interacoes` (chamado pelo publisher do outbox) e mantém a adesão por paciente/medicamento.
2. `GET /pacientes/{pacienteId}/adesao?inicio=&fim=` (período opcional) retorna, por medicamento, `totalConfirmados`, `totalNaoConfirmados`, `totalLigacoesAtendidas` e `taxaConfirmacao` (`null` quando não há desfecho no período).

## Observabilidade

1. **Correlation-id**: toda requisição carrega um `X-Correlation-Id` (gerado se ausente), que vai para os logs (MDC) e para as chamadas entre módulos, inclusive as que passam pelo outbox.
2. **Tracing**: OpenTelemetry via `micrometer-tracing-bridge-otel`, exportando para o Jaeger do `docker-compose.yml` (`http://localhost:16686`).
3. **Métricas**: `/actuator/prometheus` em cada módulo. Além das métricas HTTP, há `ia.extracao.latencia` (tag `outcome=sucesso|falha`) e `alarme.desfecho` (tag `resultado=confirmado|nao_confirmado`).
4. **Logs**: JSON no formato Logstash (`logging.structured.format.console`), com o correlation-id.
5. **Health check**: `GET /actuator/health` em cada módulo, sem autenticação.

## Segurança

1. **JWT em todos os módulos**: cada módulo valida o token emitido pelo `modulo-usuario` com a `JWT_PUBLIC_KEY`. Endpoints chamados só entre módulos (ex.: `POST /notificacoes/solicitar-envio`, `POST /alarmes/confirmacoes`) não exigem token, por isso não são expostos no gateway; os usados pelo paciente/profissional (`/receitas/*`, `GET /alarmes/{id}`, `GET /pacientes/{id}/adesao`) exigem.
2. **Rate limit no api-gateway**: por IP, janela fixa em memória (`app.rate-limit.capacidade` / `app.rate-limit.janela-ms`, padrão 60 req/min), exceto `/actuator/health` e `/actuator/prometheus`. Funciona para uma réplica só do gateway.
3. **Segredos**: todas as credenciais (JWT, Twilio, Gemini, Postgres) vêm de variáveis de ambiente. Ver `.env.example`.
4. **Webhooks Twilio**: o `modulo-mensageria` valida o header `X-Twilio-Signature` em `/webhooks/twilio/**` usando `TWILIO_AUTH_TOKEN` e `TWILIO_WEBHOOK_BASE_URL`.

## Postman

Coleção em `postman/DoseAlerta.postman_collection.json` (File > Import). Rode **01 - Usuário > Cadastrar paciente** e **Login** primeiro, eles preenchem `pacienteId`, `telefone` e `token` para as outras requisições. As pastas de Notificação e Mensageria enviam mensagens de verdade pela Twilio (ajuste `telefoneTwilio` para um número da sandbox).

**Rodar a coleção inteira:** *Extrair receita* envia o arquivo `postman/receita-exemplo.png` (receita fictícia). O Postman só encontra arquivos do corpo `form-data` que estejam no *Working directory* (Settings > General), então aponte-o para a pasta `postman/` do repositório; sem isso o Runner reclama que o arquivo não existe. Reimportar a coleção também apaga a seleção manual de arquivo. Com Newman: `newman run postman/DoseAlerta.postman_collection.json --working-dir postman`. Para testar outra foto, troque o arquivo na aba Body ou substitua o `receita-exemplo.png`.

As requisições de cliente (pastas 01 e 02, `GET /alarmes/{id}` e `GET /pacientes/{id}/adesao`) usam `{{gatewayUrl}}`, o mesmo caminho do front. As internas (criar alarme, confirmações, notificações, mensageria, interações) e os health checks vão direto na porta de cada módulo.

Na pasta **02 - IA**, **Extrair receita** guarda a fila de receitas devolvidas (uma por medicamento) e **Confirmar receita** confirma uma por envio. No Collection Runner (ou Newman) ela se repete sozinha até confirmar todas. O que a receita não trouxe é preenchido com as variáveis da coleção `doseInformada`, `frequenciaInformada` e `duracaoInformada`, no papel das respostas do paciente.
