package com.chatdoc.chatwithpdf.repository;

import com.chatdoc.chatwithpdf.model.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {
}
