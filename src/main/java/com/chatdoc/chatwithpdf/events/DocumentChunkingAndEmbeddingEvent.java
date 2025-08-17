package com.chatdoc.chatwithpdf.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunkingAndEmbeddingEvent {
    private Long documentId;
    private String bucket;
    private String key;
    private String contentType;
    private long size;
}