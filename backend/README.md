# Fitness Agent Backend

Spring Boot backend for the COMP7506B Fitness Agent Android app.

## Current Milestone

- PostgreSQL + pgvector with Flyway schema initialization
- Email-style registration with verification code
- Login with JWT
- Authenticated conversation and message APIs
- Placeholder chat answer ready for later RAG/Qwen integration

## Local Run

```bash
cd backend
cp .env.example .env
docker compose up -d postgres
mvn spring-boot:run
```

Health check:

```bash
curl http://localhost:8080/api/health
```

## Server Run

On Tencent Cloud:

```bash
cd /opt/fitness-agent
git clone https://github.com/Miles-Wong-123/Fitness-Agent.git .
cd backend
cp .env.example .env
# edit .env and set strong POSTGRES_PASSWORD/JWT_SECRET
docker compose up -d --build
```

Then test:

```bash
curl http://43.129.185.36:8080/api/health
```

## QQ Mail Verification Code

Use a QQ Mail SMTP authorization code, not your QQ password. Put it only in
`backend/.env` on the server:

```env
MAIL_ENABLED=true
MAIL_FROM=your_qq_number@qq.com
SMTP_HOST=smtp.qq.com
SMTP_PORT=465
SMTP_USERNAME=your_qq_number@qq.com
SMTP_SSL=true
SMTP_STARTTLS=false
```

Also set `SMTP_PASSWORD` in `.env` to your QQ Mail authorization code.

When `MAIL_ENABLED=true`, `/api/auth/send-code` sends the code by email and
does not return `devCode`.

## Plan APIs

Authenticated users can call:

```text
POST /api/plans/meal
POST /api/plans/workout
```

The Android app has pages for both features.
