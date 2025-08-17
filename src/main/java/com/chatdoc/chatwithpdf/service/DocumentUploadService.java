package com.chatdoc.chatwithpdf.service;

import com.chatdoc.chatwithpdf.events.DocumentChunkingAndEmbeddingEvent;
import com.chatdoc.chatwithpdf.messaging.DocumentChunkingAndEmbeddingProducer;
import com.chatdoc.chatwithpdf.model.DocumentChunkingAndEmbeddingStatus;
import com.chatdoc.chatwithpdf.model.DocumentMetadata;
import com.chatdoc.chatwithpdf.model.S3UploadResult;
import com.chatdoc.chatwithpdf.repository.DocumentMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class DocumentUploadService {
    private final DocumentMetadataRepository documentMetadataRepository;
    private final ChunkingService chunkingService;
    private final VectorStoreService vectorStoreService;
    private final S3StorageService s3StorageService;
    private final DocumentChunkingAndEmbeddingProducer documentChunkingAndEmbeddingProducer;

    @Value("${app.s3.bucket}") String bucket;

    public Long processAndStoreDocuments(MultipartFile file) throws IOException {
        if (!file.getOriginalFilename().endsWith(".pdf")) {
            throw new IOException("Invalid file type: " + file.getOriginalFilename());
        }

        S3UploadResult uploaded = s3StorageService.upload(file);
        DocumentMetadata documentMetadata = documentMetadataRepository.save(
                DocumentMetadata.builder()
                        .fileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
                        .uploadTime(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime())
                        .status(DocumentChunkingAndEmbeddingStatus.QUEUED)
                        .s3Key(uploaded.getKey())
                        .build()
        );

        DocumentChunkingAndEmbeddingEvent documentChunkingAndEmbeddingEvent = new DocumentChunkingAndEmbeddingEvent(
                documentMetadata.getId(),
                uploaded.getBucket(),
                uploaded.getKey(),
                uploaded.getContentType(),
                uploaded.getSize()
        );

        documentChunkingAndEmbeddingProducer.send(documentChunkingAndEmbeddingEvent);


//        List<PageText> pages = PdfUtils.extractText(file);
//
//        if (documentMetadata.getPageCount() == null) {
//            documentMetadata.setPageCount(pages.size());
//            documentMetadataRepository.save(documentMetadata);
//        }
//
//        for (PageText page : pages) {
//            List<String> chunks = chunkingService.chunk(page.getText());
//            if (!chunks.isEmpty()) {
//                vectorStoreService.addChunks(
//                        file.getOriginalFilename(),
//                        chunks,
//                        documentMetadata.getId(),
//                        page.getPageNumber()
//                );
//            }
//        }

//        var chunks = chunkingService.chunk(text);
//        vectorStoreService.addChunks(file.getOriginalFilename(), chunks, documentMetadata.getId());
        return documentMetadata.getId();

    }
}

