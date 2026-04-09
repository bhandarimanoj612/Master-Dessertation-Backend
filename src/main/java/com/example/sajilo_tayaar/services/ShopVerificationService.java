package com.example.sajilo_tayaar.services;

import com.example.sajilo_tayaar.dto.response.PendingVerificationResponse;
import com.example.sajilo_tayaar.dto.response.ShopVerificationStatusResponse;
import com.example.sajilo_tayaar.dto.response.VerificationDocumentResponse;
import com.example.sajilo_tayaar.entity.Shop;
import com.example.sajilo_tayaar.entity.ShopVerificationDocument;
import com.example.sajilo_tayaar.entity.enums.DocumentType;
import com.example.sajilo_tayaar.entity.enums.VerificationStatus;
import com.example.sajilo_tayaar.exception.ResourceNotFoundException;
import com.example.sajilo_tayaar.repository.ShopRepository;
import com.example.sajilo_tayaar.repository.ShopVerificationDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopVerificationService {

    private final ShopRepository shopRepository;
    private final ShopVerificationDocumentRepository documentRepository;

    @Value("${app.upload.dir:uploads/verification}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:8080/uploads/verification}")
    private String uploadBaseUrl;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".pdf", ".jpg", ".jpeg", ".png");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    // ── SHOP OWNER OPERATIONS ─────────────────────────────────────────────────────

    @Transactional
    public VerificationDocumentResponse uploadDocument(Long shopId, MultipartFile file, String documentType) {
        Shop shop = findShopOrThrow(shopId);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file must not be empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the 10 MB limit.");
        }

        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.')).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: PDF, JPG, PNG.");
        }

        String docType = documentType != null ? documentType.toUpperCase() : "OTHER";
        DocumentType type;
        try {
            type = DocumentType.valueOf(docType);
        } catch (IllegalArgumentException e) {
            type = DocumentType.OTHER;
        }

        String storedName = UUID.randomUUID() + extension;

        Path dir = Paths.get(uploadDir);
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }

        String fileUrl = uploadBaseUrl + "/" + storedName;

        ShopVerificationDocument doc = new ShopVerificationDocument();
        doc.setShop(shop);
        doc.setFileUrl(fileUrl);
        doc.setFileName(originalName != null ? originalName : storedName);
        doc.setDocumentType(type);
        doc.setUploadedAt(Instant.now());
        doc.setStatus(VerificationStatus.REQ_FOR_VERIFICATION);
        doc.setCreatedAt(Instant.now());
        doc.setUpdatedAt(Instant.now());

        ShopVerificationDocument saved = documentRepository.save(doc);
        return toDocumentResponse(saved);
    }

    @Transactional
    public ShopVerificationStatusResponse requestVerification(Long shopId) {
        Shop shop = findShopOrThrow(shopId);

        List<ShopVerificationDocument> docs = documentRepository.findByShopId(shopId);
        if (docs.isEmpty()) {
            throw new IllegalStateException("Please upload at least one verification document before requesting verification.");
        }

        shop.setVerificationStatus(VerificationStatus.REQ_FOR_VERIFICATION);
        shop.setVerificationRequestedAt(Instant.now());
        shopRepository.save(shop);

        return toVerificationStatusResponse(shop, docs);
    }

    public ShopVerificationStatusResponse getVerificationStatus(Long shopId) {
        Shop shop = findShopOrThrow(shopId);
        List<ShopVerificationDocument> docs = documentRepository.findByShopId(shopId);
        return toVerificationStatusResponse(shop, docs);
    }

    public List<VerificationDocumentResponse> getDocuments(Long shopId) {
        findShopOrThrow(shopId);
        return documentRepository.findByShopId(shopId).stream()
                .map(this::toDocumentResponse)
                .toList();
    }

    // ── PLATFORM ADMIN OPERATIONS ────────────────────────────────────────────────

    public List<PendingVerificationResponse> getPendingVerifications() {
        return shopRepository.findByVerificationStatus(VerificationStatus.REQ_FOR_VERIFICATION)
                .stream()
                .map(shop -> {
                    List<ShopVerificationDocument> docs = documentRepository.findByShopId(shop.getId());
                    return toPendingVerificationResponse(shop, docs);
                })
                .toList();
    }

    @Transactional
    public ShopVerificationStatusResponse approveVerification(Long shopId, String notes) {
        Shop shop = findShopOrThrow(shopId);

        shop.setVerificationStatus(VerificationStatus.VERIFIED);
        shop.setVerified(true);

        shop.setVerifiedAt(Instant.now());
        if (notes != null) shop.setVerificationNotes(notes);

        List<ShopVerificationDocument> docs = documentRepository.findByShopId(shopId);
        docs.forEach(doc -> {
            doc.setStatus(VerificationStatus.VERIFIED);
            doc.setUpdatedAt(Instant.now());
            if (notes != null) doc.setAdminNotes(notes);
        });
        documentRepository.saveAll(docs);
        shopRepository.save(shop);

        return toVerificationStatusResponse(shop, docs);
    }

    @Transactional
    public ShopVerificationStatusResponse rejectVerification(Long shopId, String notes) {
        Shop shop = findShopOrThrow(shopId);

        shop.setVerificationStatus(VerificationStatus.LISTED);
        shop.setVerificationNotes(notes);

        List<ShopVerificationDocument> docs = documentRepository.findByShopId(shopId);
        docs.forEach(doc -> {
            doc.setStatus(VerificationStatus.LISTED);
            doc.setUpdatedAt(Instant.now());
            if (notes != null) doc.setAdminNotes(notes);
        });
        documentRepository.saveAll(docs);
        shopRepository.save(shop);

        return toVerificationStatusResponse(shop, docs);
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────────

    private Shop findShopOrThrow(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + shopId));
    }

    private VerificationDocumentResponse toDocumentResponse(ShopVerificationDocument doc) {
        return new VerificationDocumentResponse(
                doc.getId(),
                doc.getShop().getId(),
                doc.getFileUrl(),
                doc.getFileName(),
                doc.getDocumentType().name(),
                doc.getUploadedAt(),
                doc.getStatus().name(),
                doc.getAdminNotes()
        );
    }

    private ShopVerificationStatusResponse toVerificationStatusResponse(Shop shop, List<ShopVerificationDocument> docs) {
        List<VerificationDocumentResponse> docResponses = docs.stream()
                .map(this::toDocumentResponse)
                .toList();

        return new ShopVerificationStatusResponse(
                shop.getId(),
                shop.getName(),
                shop.getVerificationStatus().name(),
                shop.getVerificationRequestedAt(),
                shop.getVerifiedAt(),
                shop.getVerificationNotes(),
                docResponses
        );
    }

    private PendingVerificationResponse toPendingVerificationResponse(Shop shop, List<ShopVerificationDocument> docs) {
        List<VerificationDocumentResponse> docResponses = docs.stream()
                .map(this::toDocumentResponse)
                .toList();

        return new PendingVerificationResponse(
                shop.getId(),
                shop.getName(),
                shop.getCity(),
                shop.getPhone(),
                shop.getVerificationStatus().name(),
                shop.getVerificationRequestedAt(),
                docResponses
        );
    }
}
