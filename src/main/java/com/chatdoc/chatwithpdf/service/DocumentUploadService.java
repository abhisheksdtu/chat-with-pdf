package com.chatdoc.chatwithpdf.service;

import com.chatdoc.chatwithpdf.model.DocumentMetadata;
import com.chatdoc.chatwithpdf.model.PageText;
import com.chatdoc.chatwithpdf.repository.DocumentMetadataRepository;
import com.chatdoc.chatwithpdf.util.PdfUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Pageable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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
        DocumentMetadata documentMetadata = documentMetadataRepository.save(
                DocumentMetadata.builder()
                        .fileName(file.getOriginalFilename())
                        .fileSize(file.getSize())
//                        .pageCount(info.pageCount())
                        .uploadTime(LocalDateTime.now())
                        .build()
        );
        List<PageText> pages = PdfUtils.extractText(file);

        if (documentMetadata.getPageCount() == null) {
            documentMetadata.setPageCount(pages.size());
            documentMetadataRepository.save(documentMetadata);
        }

//        System.out.println("Extracted from " + file.getOriginalFilename() + ":");
//        System.out.println(text);

        // TODO: Store in DB or S3

        for (PageText page : pages) {
            List<String> chunks = chunkingService.chunk(page.getText());
            if (!chunks.isEmpty()) {
                vectorStoreService.addChunks(
                        file.getOriginalFilename(),
                        chunks,
                        documentMetadata.getId(),
                        page.getPageNumber()
                );
            }
        }

//        var chunks = chunkingService.chunk(text);
//        vectorStoreService.addChunks(file.getOriginalFilename(), chunks, documentMetadata.getId());
        return documentMetadata.getId();

    }
}

