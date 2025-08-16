package com.chatdoc.chatwithpdf.controller;

import com.chatdoc.chatwithpdf.model.DocumentUploadResponse;
import com.chatdoc.chatwithpdf.service.DocumentUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/documents")
public class DocumentUploadController {

    private final DocumentUploadService documentUploadService;

    public DocumentUploadController(DocumentUploadService documentUploadService) {
        this.documentUploadService = documentUploadService;
    }

    @Operation(summary = "Upload PDF document", description = "Uploads a single PDF file for processing.")
    @ApiResponse(responseCode = "200", description = "Document uploaded successfully")
    @PostMapping(value = "/upload", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            Long documentId = documentUploadService.processAndStoreDocuments(file);
            return ResponseEntity.ok(
                    new DocumentUploadResponse(
                            documentId,
                            file.getOriginalFilename(),
                            "SUCCESS",
                            "Uploaded successfully"
                    )
            );
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(
                    new DocumentUploadResponse(
                            null,
                            file != null ? file.getOriginalFilename() : null,
                            "FAILED",
                            "Failed to process document: " + e.getMessage()
                    )
            );
        }
    }
}
