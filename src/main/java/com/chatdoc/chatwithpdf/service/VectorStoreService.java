package com.chatdoc.chatwithpdf.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final PgVectorStore vectorStore;

    public void addChunks(String fileName, List<String> chunks, Long documentId, Integer pageNumber) {
        List<Document> docs = chunks.stream()
                .map(t -> new Document(
                        t,
                        Map.of(
                                "fileName", fileName,
                                "documentId", String.valueOf(documentId),
                                "pageNumber", String.valueOf(pageNumber)
                        )))
                .toList();
        vectorStore.add(docs);
    }

    // existing basic search
    public List<Document> search(String query, int topK) {
        SearchRequest req = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        return vectorStore.similaritySearch(req);
    }

    // NEW: search with optional metadata filter
    public List<Document> search(String query, int topK, String filterExpression) {
        SearchRequest.Builder b = SearchRequest.builder()
                .query(query)
                .topK(topK);
        if (filterExpression != null && !filterExpression.isBlank()) {
            b.filterExpression(filterExpression);
        }
        return vectorStore.similaritySearch(b.build());
    }
}
