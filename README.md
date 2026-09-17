# DoseAlerta

Ver `RESUMO_TECNICO.md` (visão geral e decisões técnicas), `PLANO_DESENVOLVIMENTO.md` (roteiro de implementação) e `CONTRATOS_EVENTOS.md` (contrato de cada evento/comando trocado entre módulos).

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

Cada módulo segue a convenção de pacotes `core` (domínio, usecases, portas) / `infra` (implementações, Spring, adapters externos) descrita na seção 5 do `RESUMO_TECNICO.md`.

## Subindo o ambiente localmente

1. Copie `.env.example` para `.env` e preencha as variáveis (chave pública JWT, credenciais Twilio). As variáveis precisam estar exportadas no shell onde os módulos forem rodados (`export $(cat .env | xargs)` ou equivalente do seu terminal/IDE).

2. Suba o Postgres:

   ```bash
   docker-compose up -d
   ```

   Banco disponível em `localhost:5433` (db `dose_alerta`, user/senha `dose_alerta`).

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
2. Para receber os webhooks (resposta do paciente, status de ligação), o `modulo-mensageria` precisa ser alcançável publicamente — em dev, exponha a porta 8085 com um túnel (ex: `ngrok http 8085`) e preencha `TWILIO_WEBHOOK_BASE_URL` com a URL gerada.
3. Configure no console Twilio:
   - Sandbox do WhatsApp → "When a message comes in": `{TWILIO_WEBHOOK_BASE_URL}/webhooks/twilio/mensagens`
   - As ligações de confirmação e o status callback são configurados automaticamente pelo próprio `modulo-mensageria` a cada chamada (`/webhooks/twilio/ligacoes/confirmacao` e `/webhooks/twilio/ligacoes/status`).
