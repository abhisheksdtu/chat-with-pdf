package com.chatdoc.chatwithpdf.messaging;

import com.chatdoc.chatwithpdf.events.DocumentChunkingAndEmbeddingEvent;
import com.chatdoc.chatwithpdf.model.DocumentChunkingAndEmbeddingStatus;
import com.chatdoc.chatwithpdf.model.DocumentMetadata;
import com.chatdoc.chatwithpdf.repository.DocumentMetadataRepository;
import com.chatdoc.chatwithpdf.service.ChunkingService;
import com.chatdoc.chatwithpdf.service.VectorStoreService;
import com.chatdoc.chatwithpdf.util.PdfUtilsNew;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentChunkingAndEmbeddingConsumer {

    private final S3Client s3Client;
    private final DocumentMetadataRepository metadataRepo;
    private final ChunkingService chunkingService;
    private final VectorStoreService vectorStoreService;

    @KafkaListener(
            topics = "${app.kafka.topics.document-uploads}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(DocumentChunkingAndEmbeddingEvent event) {
        Long docId = event.getDocumentId();
        log.error("Processing documentId={} from S3 s3://{}/{}", docId, event.getBucket(), event.getKey());

        DocumentMetadata meta = metadataRepo.findById(docId).orElse(null);
        if (meta == null) {
            log.warn("No metadata found for documentId={}, skipping", docId);
            return;
        }

        if (meta.getStatus() == DocumentChunkingAndEmbeddingStatus.READY) {
            log.error("Document {} already processed. Skipping.", docId);
            return;
        }

        try {
            meta.setStatus(DocumentChunkingAndEmbeddingStatus.PROCESSING);
            metadataRepo.save(meta);

            GetObjectRequest get = GetObjectRequest.builder()
                    .bucket(event.getBucket())
                    .key(event.getKey())
                    .build();

            try (InputStream in = s3Client.getObject(get)) {
                InputStream bounded = new org.apache.commons.io.input.BoundedInputStream(in, event.getSize() > 0 ? event.getSize() : Long.MAX_VALUE);

                var pages = PdfUtilsNew.extractText(bounded);
                int pageCount = pages.size();
                meta.setPageCount(pageCount);
                metadataRepo.save(meta);

                int nonEmptyPages = 0;
                for (var page : pages) {
                    String text = page.getText();
                    if (text == null || text.isBlank()) continue;

                    nonEmptyPages++;
                    List<String> chunks = chunkingService.chunk(text);
                    if (!chunks.isEmpty()) {
                        vectorStoreService.addChunks(
                                meta.getFileName(),
                                chunks,
                                meta.getId(),
                                page.getPageNumber()
                        );
                    }
                }

                log.error("Embedded documentId={} pages={} nonEmptyPages={}", docId, pageCount, nonEmptyPages);
            }

            meta.setStatus(DocumentChunkingAndEmbeddingStatus.READY);
            meta.setErrorMessage(null);
            metadataRepo.save(meta);

        } catch (Exception e) {
            log.error("Failed processing documentId={}: {}", docId, e.getMessage(), e);
            meta.setStatus(DocumentChunkingAndEmbeddingStatus.FAILED);
            meta.setErrorMessage(e.getMessage());
            metadataRepo.save(meta);
        }
    }
}