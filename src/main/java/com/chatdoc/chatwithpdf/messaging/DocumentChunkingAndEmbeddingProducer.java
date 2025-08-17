package com.chatdoc.chatwithpdf.messaging;

import com.chatdoc.chatwithpdf.events.DocumentChunkingAndEmbeddingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DocumentChunkingAndEmbeddingProducer {

    private final KafkaTemplate<String, DocumentChunkingAndEmbeddingEvent> kafkaTemplate;
    private final String topic;

    public DocumentChunkingAndEmbeddingProducer(
            KafkaTemplate<String, DocumentChunkingAndEmbeddingEvent> kafkaTemplate,
            @Value("${app.kafka.topics.document-uploads}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void send(DocumentChunkingAndEmbeddingEvent event) {
        log.debug("Sending message");
        kafkaTemplate.send(
                MessageBuilder
                        .withPayload(event)
                        .setHeader("kafka_topic", topic)
                        .setHeader("kafka_messageKey", String.valueOf(event.getDocumentId()))
                        .build()
        );
    }
}
