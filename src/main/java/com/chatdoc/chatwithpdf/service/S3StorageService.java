package com.chatdoc.chatwithpdf.service;

import com.chatdoc.chatwithpdf.model.S3UploadResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class S3StorageService {

    private static final long MULTIPART_THRESHOLD_BYTES = 5L * 1024 * 1024;

    private final S3Client s3Client;
    private final S3TransferManager transferManager;
    private final S3Presigner s3Presigner;

    private final String bucket;

    private final String keyPrefix;

    public S3StorageService(
            S3Client s3Client,
            S3TransferManager transferManager,
            S3Presigner s3Presigner,
            @Value("${app.s3.bucket}") String bucket,
            @Value("${app.s3.key-prefix:}") String keyPrefix) {
        this.s3Client = s3Client;
        this.transferManager = transferManager;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.keyPrefix = keyPrefix == null ? "" : keyPrefix;
    }

    public S3UploadResult upload(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String sanitized = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String key = keyPrefix
                + Instant.now().toString().replace(":", "-")
                + "_"
                + UUID.randomUUID()
                + "_"
                + sanitized;

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        long size = file.getSize();

        String eTag;

        if (size >= MULTIPART_THRESHOLD_BYTES) {
            Path temp = Files.createTempFile("s3-upload-", "-" + sanitized);
            try {
                file.transferTo(temp);

                UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                        .putObjectRequest(PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .contentType(contentType)
                                .serverSideEncryption("AES256")
                                .build())
                        .addTransferListener(LoggingTransferListener.create())
                        .source(temp)
                        .build();

                CompletedFileUpload completed = transferManager
                        .uploadFile(uploadFileRequest)
                        .completionFuture()
                        .join();

                HeadObjectResponse head = s3Client.headObject(HeadObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build());
                eTag = head.eTag();
            } finally {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignore) {
                }
            }
        } else {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .serverSideEncryption("AES256")
                    .build();

            eTag = s3Client.putObject(req, RequestBody.fromInputStream(file.getInputStream(), size)).eTag();
        }

        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presign = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(getReq)
                .build();

        URL url = s3Presigner.presignGetObject(presign).url();

        return new S3UploadResult(bucket, key, eTag, size, contentType, url.toString());
    }
}