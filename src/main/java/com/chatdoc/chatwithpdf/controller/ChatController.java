package com.chatdoc.chatwithpdf.controller;

import com.chatdoc.chatwithpdf.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "Ask a question about your uploaded documents",
            description = "Optionally limit retrieval to a specific documentId")
    @ApiResponse(responseCode = "200", description = "Answer returned")
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest req) {
        var ans = chatService.answer(req.getQuery(), req.getDocumentId(), req.getTopK());
        ChatResponse resp = new ChatResponse();
        resp.setAnswer(ans.answer());
        resp.setSources(ans.sources().stream()
                .map(c -> new ChatResponse.Source(c.fileName(), c.pageNumber()))
                .toList());
        return ResponseEntity.ok(resp);
    }

    @Data
    public static class ChatRequest {
        private String query;
        private Long documentId; // optional
        private Integer topK;    // optional; default 5
    }

    @Data
    public static class ChatResponse {
        private String answer;
        private java.util.List<Source> sources;

        @Data
        public static class Source {
            private final String fileName;
            private final String pageNumber; // nullable
        }
    }
}
