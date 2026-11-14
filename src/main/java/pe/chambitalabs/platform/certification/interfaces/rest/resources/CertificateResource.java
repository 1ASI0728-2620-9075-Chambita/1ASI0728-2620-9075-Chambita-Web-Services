package pe.chambitalabs.platform.certification.interfaces.rest.resources;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

public record CertificateResource(UUID id, UUID engagementId, UUID studentId, String projectTitle, String category,
                                  int score, Instant issuedAt, String certificateHash, String status,
                                  BigInteger tokenId, String transactionHash, Instant revokedAt,
                                  String verificationUrl, String contractAddress) {
}
