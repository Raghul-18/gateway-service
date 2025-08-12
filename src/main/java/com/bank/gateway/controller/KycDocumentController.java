package com.bank.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/kyc-documents")
@RequiredArgsConstructor
public class KycDocumentController {

    @Value("${services.kyc.base-url:http://localhost:8082}")
    private String kycServiceBaseUrl;

    private final RestTemplate restTemplate;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        try {
            log.info("📤 Gateway: Uploading {} document, size: {} bytes", documentType, file.getSize());

            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authorization token required"));
            }

            // Map frontend document types to backend expected values
            String mappedDocumentType = mapDocumentType(documentType);

            // Forward to KYC service
            String kycUrl = kycServiceBaseUrl + "/api/kyc/upload";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", authHeader);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("name", mappedDocumentType);
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(kycUrl, requestEntity, Map.class);

            log.info("✅ Gateway: Document uploaded successfully to KYC service");
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            log.error("❌ Gateway: Failed to upload document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload document: " + e.getMessage()));
        }
    }

    @PostMapping("/upload-base64")
    public ResponseEntity<?> uploadDocumentBase64(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        try {
            String documentType = request.get("documentType");
            String fileName = request.get("fileName");
            String base64Data = request.get("base64Data");
            String mimeType = request.get("mimeType");

            if (documentType == null || base64Data == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "documentType and base64Data are required"));
            }

            log.info("📤 Gateway: Uploading {} document via base64", documentType);

            // Extract base64 content (remove data:image/jpeg;base64, prefix if present)
            String actualBase64 = base64Data;
            if (base64Data.contains(",")) {
                actualBase64 = base64Data.split(",")[1];
            }

            // Decode base64 to bytes
            byte[] fileBytes = Base64.getDecoder().decode(actualBase64);

            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authorization token required"));
            }

            // Map frontend document types to backend expected values
            String mappedDocumentType = mapDocumentType(documentType);

            // Create ByteArrayResource for file
            ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return fileName != null ? fileName : mappedDocumentType + getFileExtension(mimeType);
                }
            };

            // Forward to KYC service
            String kycUrl = kycServiceBaseUrl + "/api/kyc/upload";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", authHeader);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("name", mappedDocumentType);
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(kycUrl, requestEntity, Map.class);

            log.info("✅ Gateway: Base64 document uploaded successfully to KYC service");
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            log.error("❌ Gateway: Failed to upload base64 document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload document: " + e.getMessage()));
        }
    }

    @GetMapping("/my-documents")
    public ResponseEntity<?> getMyDocuments(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authorization token required"));
            }

            String kycUrl = kycServiceBaseUrl + "/api/kyc/my-documents";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                    kycUrl, HttpMethod.GET, entity, Object.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            log.error("❌ Gateway: Failed to fetch documents: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch documents: " + e.getMessage()));
        }
    }

    @GetMapping("/document/{documentId}/download")
    public ResponseEntity<?> downloadDocument(
            @PathVariable Long documentId,
            HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authorization token required"));
            }

            String kycUrl = kycServiceBaseUrl + "/api/kyc/document/" + documentId + "/download";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    kycUrl, HttpMethod.GET, entity, byte[].class);

            return ResponseEntity.ok()
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (Exception e) {
            log.error("❌ Gateway: Failed to download document: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to download document: " + e.getMessage()));
        }
    }

    @DeleteMapping("/document/{documentId}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable Long documentId,
            HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authorization token required"));
            }

            String kycUrl = kycServiceBaseUrl + "/api/kyc/document/" + documentId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    kycUrl, HttpMethod.DELETE, entity, Void.class);

            return ResponseEntity.ok(Map.of("success", true, "message", "Document deleted successfully"));

        } catch (Exception e) {
            log.error("❌ Gateway: Failed to delete document: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete document: " + e.getMessage()));
        }
    }

    /**
     * Maps frontend document types to backend expected values
     */
    private String mapDocumentType(String frontendType) {
        switch (frontendType.toLowerCase()) {
            case "aadhaar":
            case "aadhar":
                return "AADHAR";
            case "pan":
                return "PAN";
            case "photo":
                return "PHOTO";
            default:
                return frontendType.toUpperCase();
        }
    }

    private String getFileExtension(String mimeType) {
        if (mimeType == null) return ".bin";

        switch (mimeType.toLowerCase()) {
            case "image/jpeg":
            case "image/jpg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "application/pdf":
                return ".pdf";
            default:
                return ".bin";
        }
    }
}