package pe.chambitalabs.platform.certification.interfaces.rest.resources;

import java.math.BigInteger;
import java.time.Instant;

/** Public data of a certificate for the verification portal: no personal data. */
public record PublicCertificateResource(String certificateHash, String projectTitle, String category, int score,
                                        Instant issuedAt, String status, BigInteger tokenId, String transactionHash,
                                        Instant revokedAt, String contractAddress) {
}
