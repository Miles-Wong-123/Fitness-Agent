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
