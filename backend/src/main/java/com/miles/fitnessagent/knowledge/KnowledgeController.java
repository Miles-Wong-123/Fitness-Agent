package com.miles.fitnessagent.knowledge;

import com.miles.fitnessagent.knowledge.dto.DocumentImportRequest;
import com.miles.fitnessagent.knowledge.dto.DocumentResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge/documents")
public class KnowledgeController {
    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @PostMapping
    public DocumentResponse importDocument(@Valid @RequestBody DocumentImportRequest request) {
        return knowledgeService.importDocument(request);
    }

    @GetMapping
    public List<DocumentResponse> listDocuments() {
        return knowledgeService.listDocuments();
    }
}
