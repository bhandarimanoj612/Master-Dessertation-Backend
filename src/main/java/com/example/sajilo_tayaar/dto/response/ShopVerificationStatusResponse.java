package com.example.sajilo_tayaar.dto.response;

import java.time.Instant;
import java.util.List;

public record ShopVerificationStatusResponse(
        Long shopId,
        String shopName,
        String verificationStatus,
        Instant verificationRequestedAt,
        Instant verifiedAt,
        String verificationNotes,
        List<VerificationDocumentResponse> documents
) {}
