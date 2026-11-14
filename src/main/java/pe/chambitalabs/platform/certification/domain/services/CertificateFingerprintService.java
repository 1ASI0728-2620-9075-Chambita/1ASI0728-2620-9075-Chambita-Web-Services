package pe.chambitalabs.platform.certification.domain.services;

import org.web3j.crypto.Hash;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Calculates the keccak-256 fingerprint (bytes32) of the canonical content of a certificate. */
public final class CertificateFingerprintService {

    private CertificateFingerprintService() {
    }

    public static String fingerprint(UUID certificateId, UUID holderId, String projectTitle, String category,
                                     int score, Instant issuedAt) {
        var canonical = String.join("|", "chambita-certificate:v1", certificateId.toString(), holderId.toString(),
                projectTitle, category, Integer.toString(score), issuedAt.truncatedTo(ChronoUnit.SECONDS).toString());
        return Hash.sha3String(canonical);
    }
}
