package com.example.sajilo_tayaar.dto.response;

import java.time.Instant;
import java.util.List;

public record PendingVerificationResponse(
        Long shopId,
        String shopName,
        String city,
        String phone,
        String verificationStatus,
        Instant verificationRequestedAt,
        List<VerificationDocumentResponse> documents
) {}
