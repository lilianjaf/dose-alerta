# DoseAlerta

Sistema de lembretes de medicação: lê a receita por foto, agenda os alarmes e avisa o paciente por WhatsApp e ligação até ele confirmar a dose.

## Módulos

Projeto Gradle multi-módulo, um Spring Boot app por módulo:

| Módulo | Porta | Descrição |
|---|---|---|
| `api-gateway` | 8080 | Ponto único de entrada, roteia para os módulos internos |
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

## Twilio (modulo-mensageria)

1. Crie uma conta Twilio e ative o **WhatsApp Sandbox** (Messaging > Try it out > Send a WhatsApp message) e um número de voz. Preencha `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_WHATSAPP_NUMBER` e `TWILIO_VOICE_NUMBER` no `.env`.
2. Para receber os webhooks (resposta do paciente, status de ligação), o `modulo-mensageria` precisa ser alcançável publicamente. Em dev, exponha a porta 8085 com um túnel (ex: `ngrok http 8085`) e preencha `TWILIO_WEBHOOK_BASE_URL` com a URL gerada.
3. Configure no console Twilio:
   - Sandbox do WhatsApp → "When a message comes in": `{TWILIO_WEBHOOK_BASE_URL}/webhooks/twilio/mensagens`
   - As ligações de confirmação e o status callback são configurados automaticamente pelo próprio `modulo-mensageria` a cada chamada (`/webhooks/twilio/ligacoes/confirmacao` e `/webhooks/twilio/ligacoes/status`).

## IA (modulo-ia)

1. A extração da receita usa a API do Google Gemini, modelo `gemini-3.5-flash-lite` (configurável em `ia.modelo`, com `ia.gemini.temperature` e `ia.gemini.max-output-tokens`). Preencha `GEMINI_API_KEY` no `.env` (chave em https://aistudio.google.com/apikey).
2. Fluxo: `POST /receitas/extrair` (multipart: `imagem`, `pacienteId`, `telefone`, `horarioInicial`) extrai e salva a receita como `AGUARDANDO_CONFIRMACAO`. Depois, `POST /receitas/{id}/confirmar` (corpo opcional: sem corpo, ou com campos omitidos, mantém os dados como foram extraídos; só os campos enviados — `medicamento`, `dose`, `frequenciaHoras`, `duracaoDias` — corrigem a extração) grava um `ReceitaConfirmadaEvent` no outbox, e o publisher cria o alarme da primeira dose no `modulo-scheduler`.
3. Testes de regressão do prompt ficam em `modulo-ia/src/test/resources/harness-receitas/` e não rodam no `./gradlew test`. Ver o `README.md` do diretório.

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

1. **JWT em todos os módulos**: cada módulo valida o token emitido pelo `modulo-usuario` com a `JWT_PUBLIC_KEY`. Endpoints chamados só entre módulos (ex.: `POST /notificacoes/solicitar-envio`, `POST /alarmes/confirmacoes`) não exigem token; os usados pelo paciente/profissional (`/receitas/*`, `GET /alarmes/{id}`, `GET /pacientes/{id}/adesao`) exigem.
2. **Rate limit no api-gateway**: por IP, janela fixa em memória (`app.rate-limit.capacidade` / `app.rate-limit.janela-ms`, padrão 60 req/min), exceto `/actuator/health` e `/actuator/prometheus`. Funciona para uma réplica só do gateway.
3. **Segredos**: todas as credenciais (JWT, Twilio, Gemini, Postgres) vêm de variáveis de ambiente. Ver `.env.example`.
4. **Webhooks Twilio**: o `modulo-mensageria` valida o header `X-Twilio-Signature` em `/webhooks/twilio/**` usando `TWILIO_AUTH_TOKEN` e `TWILIO_WEBHOOK_BASE_URL`.

## Postman

Coleção em `postman/DoseAlerta.postman_collection.json` (File > Import). Rode **01 - Usuário > Cadastrar paciente** e **Login** primeiro, eles preenchem `pacienteId`, `telefone` e `token` para as outras requisições. As pastas de Notificação e Mensageria enviam mensagens de verdade pela Twilio (ajuste `telefoneTwilio` para um número da sandbox).
