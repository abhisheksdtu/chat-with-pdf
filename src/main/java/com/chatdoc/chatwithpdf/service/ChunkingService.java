package com.chatdoc.chatwithpdf.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChunkingService {

    // Start simple: character-based chunks with overlap
    private static final int CHUNK_SIZE = 1200;   // ~800-1000 tokens depending on language
    private static final int CHUNK_OVERLAP = 200;

    public List<String> chunk(String text) {
        log.debug("Chunking text");
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            String piece = text.substring(start, end).trim();
            if (!piece.isBlank()) chunks.add(piece);
            if (end == text.length()) break;
            start = end - CHUNK_OVERLAP;
            if (start < 0) start = 0;
        }
        return chunks;
    }
}
