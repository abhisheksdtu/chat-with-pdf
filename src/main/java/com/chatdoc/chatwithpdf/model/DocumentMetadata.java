package com.chatdoc.chatwithpdf.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private long fileSize;

    private LocalDateTime uploadTime;

    private Integer pageCount;

    private String s3Key;

    @Enumerated(EnumType.STRING)
    private DocumentChunkingAndEmbeddingStatus status;

    private String errorMessage;
}
