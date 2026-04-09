package com.example.sajilo_tayaar.controller;

import com.example.sajilo_tayaar.dto.request.CreateInvoiceRequest;
import com.example.sajilo_tayaar.dto.response.InvoiceResponse;
import com.example.sajilo_tayaar.entity.Invoice;
import com.example.sajilo_tayaar.services.PosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
public class PosController {

    private final PosService posService;

    /** POST /api/pos/invoices  — create a new invoice */
    @PostMapping("/invoices")
    public ResponseEntity<InvoiceResponse> create(@RequestBody CreateInvoiceRequest req) {
        Invoice invoice = posService.createInvoice(req);
        return ResponseEntity.ok(InvoiceResponse.from(invoice));
    }

    /** GET /api/pos/invoices/shop/{shopId}  — list invoices for a shop (newest first) */
    @GetMapping("/invoices/shop/{shopId}")
    public ResponseEntity<List<InvoiceResponse>> listByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(
                posService.listInvoices(shopId)
                        .stream()
                        .map(InvoiceResponse::from)
                        .collect(Collectors.toList())
        );
    }

    /** GET /api/pos/invoices/shop/{shopId}/{invoiceId}  — single invoice detail */
    @GetMapping("/invoices/shop/{shopId}/{invoiceId}")
    public ResponseEntity<InvoiceResponse> getOne(
            @PathVariable Long shopId,
            @PathVariable Long invoiceId) {
        return ResponseEntity.ok(
                InvoiceResponse.from(posService.getInvoice(shopId, invoiceId))
        );
    }

    /** GET /api/pos/invoices  — all invoices (admin use) */
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponse>> all() {
        return ResponseEntity.ok(
                posService.getAllInvoices()
                        .stream()
                        .map(InvoiceResponse::from)
                        .collect(Collectors.toList())
        );
    }
}