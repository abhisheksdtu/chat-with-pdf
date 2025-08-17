package com.chatdoc.chatwithpdf.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class S3UploadResult {
    private String bucket;
    private String key;
    private String eTag;
    private long size;
    private String contentType;
    private String presignedUrl;
}
