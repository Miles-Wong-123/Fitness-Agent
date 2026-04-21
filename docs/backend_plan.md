# Spring Boot Backend Plan

Backend stack:

- Spring Boot
- PostgreSQL
- pgvector
- Docker Compose
- JWT authentication

First milestone:

1. Register by email and verification code.
2. Login and receive JWT.
3. Create/list conversations.
4. Send chat messages.
5. Persist chat history.

Second milestone:

1. Import fitness documents.
2. Generate embeddings.
3. Search pgvector chunks.
4. Call Qwen with retrieved context.
