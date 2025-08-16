package com.chatdoc.chatwithpdf.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadResponse {
    private Long documentId;
    private String fileName;
    private String status;
    private String message;
}
