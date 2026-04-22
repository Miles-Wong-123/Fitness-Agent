package com.miles.fitnessagent.knowledge;

import com.miles.fitnessagent.config.AppProperties;
import com.miles.fitnessagent.knowledge.dto.DocumentImportRequest;
import com.miles.fitnessagent.knowledge.dto.DocumentResponse;
import com.miles.fitnessagent.knowledge.dto.SourceChunk;
import com.miles.fitnessagent.qwen.QwenClient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeService {
    private final DocumentRepository documentRepository;
    private final EntityManager entityManager;
    private final QwenClient qwenClient;
    private final AppProperties appProperties;

    public KnowledgeService(
            DocumentRepository documentRepository,
            EntityManager entityManager,
            QwenClient qwenClient,
            AppProperties appProperties
    ) {
        this.documentRepository = documentRepository;
        this.entityManager = entityManager;
        this.qwenClient = qwenClient;
        this.appProperties = appProperties;
    }

    @Transactional
    public DocumentResponse importDocument(DocumentImportRequest request) {
        Document document = new Document();
        document.setTitle(request.title().trim());
        Document saved = documentRepository.save(document);

        List<String> chunks = splitText(request.content());
        for (String chunk : chunks) {
            List<Double> embedding = qwenClient.embed(chunk);
            entityManager.createNativeQuery("""
                            INSERT INTO document_chunks (document_id, content, embedding)
                            VALUES (:documentId, :content, CAST(:embedding AS vector))
                            """)
                    .setParameter("documentId", saved.getId())
                    .setParameter("content", chunk)
                    .setParameter("embedding", toVectorLiteral(embedding))
                    .executeUpdate();
        }
        entityManager.flush();
        return new DocumentResponse(saved.getId(), saved.getTitle(), saved.getCreatedAt(), chunks.size());
    }

    public List<DocumentResponse> listDocuments() {
        return documentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(document -> new DocumentResponse(
                        document.getId(),
                        document.getTitle(),
                        document.getCreatedAt(),
                        countChunks(document.getId())
                ))
                .toList();
    }

    public List<SourceChunk> retrieve(String question) {
        List<Double> embedding = qwenClient.embed(question);
        Query query = entityManager.createNativeQuery("""
                SELECT dc.document_id, d.title, dc.content, dc.embedding <=> CAST(:embedding AS vector) AS distance
                FROM document_chunks dc
                JOIN documents d ON d.id = dc.document_id
                ORDER BY dc.embedding <=> CAST(:embedding AS vector)
                LIMIT :limit
                """);
        query.setParameter("embedding", toVectorLiteral(embedding));
        query.setParameter("limit", appProperties.getRag().getTopK());
        List<?> rows = query.getResultList();
        List<SourceChunk> result = new ArrayList<>();
        for (Object row : rows) {
            Object[] values = (Object[]) row;
            result.add(new SourceChunk(
                    ((Number) values[0]).longValue(),
                    (String) values[1],
                    (String) values[2],
                    ((Number) values[3]).doubleValue()
            ));
        }
        return result;
    }

    private int countChunks(Long documentId) {
        Number count = (Number) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM document_chunks WHERE document_id = :documentId")
                .setParameter("documentId", documentId)
                .getSingleResult();
        return count.intValue();
    }

    private List<String> splitText(String content) {
        String clean = content.replaceAll("\\s+", " ").trim();
        int chunkSize = appProperties.getRag().getChunkSize();
        int overlap = Math.min(appProperties.getRag().getChunkOverlap(), chunkSize / 2);
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < clean.length()) {
            int end = Math.min(start + chunkSize, clean.length());
            String chunk = clean.substring(start, end).trim();
            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }
            if (end == clean.length()) {
                break;
            }
            start = end - overlap;
        }
        return chunks;
    }

    private String toVectorLiteral(List<Double> values) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(values.get(i));
        }
        return builder.append(']').toString();
    }
}
