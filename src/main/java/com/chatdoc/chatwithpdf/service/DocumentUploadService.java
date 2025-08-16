package com.chatdoc.chatwithpdf.service;

import com.chatdoc.chatwithpdf.model.DocumentMetadata;
import com.chatdoc.chatwithpdf.repository.DocumentMetadataRepository;
import com.chatdoc.chatwithpdf.util.PdfUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DocumentUploadService {
    private final DocumentMetadataRepository documentMetadataRepository;
    private final ChunkingService chunkingService;
    private final VectorStoreService vectorStoreService;

    public Long processAndStoreDocuments(MultipartFile file) throws IOException {
        if (!file.getOriginalFilename().endsWith(".pdf")) {
            throw new IOException("Invalid file type: " + file.getOriginalFilename());
        }

        // Extract text from PDF
        String text = PdfUtils.extractText(file);
        DocumentMetadata documentMetadata = documentMetadataRepository.save(
                DocumentMetadata.builder()
                        .fileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
//                        .pageCount(info.pageCount())
                        .uploadTime(LocalDateTime.now())
                        .build()
        );

        System.out.println("Extracted from " + file.getOriginalFilename() + ":");
        System.out.println(text);

        // TODO: Store in DB or S3

        var chunks = chunkingService.chunk(text);
        vectorStoreService.addChunks(file.getOriginalFilename(), chunks, documentMetadata.getId());
        return documentMetadata.getId();

    }
}

