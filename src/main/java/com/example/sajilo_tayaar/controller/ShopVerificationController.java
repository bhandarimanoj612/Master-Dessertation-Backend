package com.example.sajilo_tayaar.controller;

import com.example.sajilo_tayaar.dto.response.ShopVerificationStatusResponse;
import com.example.sajilo_tayaar.dto.response.VerificationDocumentResponse;
import com.example.sajilo_tayaar.services.ShopVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/shop/verification")
@RequiredArgsConstructor
public class ShopVerificationController {

    private final ShopVerificationService verificationService;

    /**
     * Upload a verification document for a shop.
     * POST /api/shop/verification/{shopId}/documents
     */
    @PostMapping(value = "/{shopId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VerificationDocumentResponse> uploadDocument(
            @PathVariable Long shopId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", defaultValue = "OTHER") String documentType
    ) {
        VerificationDocumentResponse response = verificationService.uploadDocument(shopId, file, documentType);
        return ResponseEntity.ok(response);
    }

    /**
     * Request verification for a shop (status → REQ_FOR_VERIFICATION).
     * POST /api/shop/verification/{shopId}/request
     */
    @PostMapping("/{shopId}/request")
    public ResponseEntity<ShopVerificationStatusResponse> requestVerification(@PathVariable Long shopId) {
        return ResponseEntity.ok(verificationService.requestVerification(shopId));
    }

    /**
     * Get current verification status and uploaded documents for a shop.
     * GET /api/shop/verification/{shopId}/status
     */
    @GetMapping("/{shopId}/status")
    public ResponseEntity<ShopVerificationStatusResponse> getVerificationStatus(@PathVariable Long shopId) {
        return ResponseEntity.ok(verificationService.getVerificationStatus(shopId));
    }

    /**
     * Get all uploaded documents for a shop.
     * GET /api/shop/verification/{shopId}/documents
     */
    @GetMapping("/{shopId}/documents")
    public ResponseEntity<List<VerificationDocumentResponse>> getDocuments(@PathVariable Long shopId) {
        return ResponseEntity.ok(verificationService.getDocuments(shopId));
    }
}
