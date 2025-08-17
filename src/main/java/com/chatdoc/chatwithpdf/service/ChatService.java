package com.chatdoc.chatwithpdf.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final VectorStoreService vectorStoreService;
    private final ChatClient chatClient;

    public Answer answer(String query, Long documentId, Integer topK) {
        int k = (topK == null || topK <= 0) ? 5 : topK;

        String filter = (documentId != null && documentId > 0)
                ? "documentId == '" + documentId + "'"
                : null;
        List<Document> hits = vectorStoreService.search(query, k, filter);

        if (hits.isEmpty()) {
            return new Answer("I couldn’t find enough information in your documents to answer that.", List.of());
        }

        StringBuilder context = new StringBuilder();
        List<Citation> citations = new ArrayList<>();

        for (int i = 0; i < hits.size(); i++) {
            Document document = hits.get(i);

            // NOTE: use record accessors
            Map<String, Object> md = document.getMetadata();
            String fileName = String.valueOf(md.getOrDefault("fileName", "unknown"));
            String page = String.valueOf(md.getOrDefault("pageNumber", "n/a"));

            context.append("Source ").append(i + 1).append(" (")
                    .append(fileName).append(", page ").append(page).append("):\n")
                    .append(document.getText())
                    .append("\n\n");

            citations.add(new Citation(fileName, "n/a".equals(page) ? null : page));
        }

        String system = """
                You are a precise assistant. Use ONLY the provided context to answer.
                If the answer is not present, say you don't have enough information.
                Keep responses concise.
                """;

        String user = """
                Context:
                %s

                Question:
                %s
                """.formatted(context, query);

        String reply = chatClient
                .prompt()
                .system(system)
                .user(user)
                .call()
                .content();

        LinkedHashSet<Citation> uniqueCitations = new LinkedHashSet<>(citations);
        StringBuilder citationBlock = new StringBuilder();
        if (!uniqueCitations.isEmpty()) {
            citationBlock.append("\n\nSources:\n");
            for (Citation c : uniqueCitations) {
                if (c.pageNumber() != null) {
                    citationBlock.append("- Page ").append(c.pageNumber())
                            .append(", ").append(c.fileName()).append("\n");
                } else {
                    citationBlock.append("- ").append(c.fileName()).append("\n");
                }
            }
        }

        return new Answer(reply + citationBlock.toString(), new ArrayList<>(uniqueCitations));
    }

    public record Citation(String fileName, String pageNumber) {
    }

    public record Answer(String answer, List<Citation> sources) {
    }
}
