package com.example.sajilo_tayaar.dto.response;

import java.time.Instant;

public record VerificationDocumentResponse(
        Long id,
        Long shopId,
        String fileUrl,
        String fileName,
        String documentType,
        Instant uploadedAt,
        String status,
        String adminNotes
) {}
