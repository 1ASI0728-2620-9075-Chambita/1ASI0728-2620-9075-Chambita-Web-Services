package pe.chambitalabs.platform.certification.domain;

import org.junit.jupiter.api.Test;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;
import pe.chambitalabs.platform.certification.domain.model.aggregates.Certificate;
import pe.chambitalabs.platform.certification.domain.model.aggregates.CertificateHolder;
import pe.chambitalabs.platform.certification.domain.model.events.CertificateIssuanceFailed;
import pe.chambitalabs.platform.certification.domain.model.events.CertificateIssued;
import pe.chambitalabs.platform.certification.domain.model.valueobjects.CertificateStatus;
import pe.chambitalabs.platform.certification.domain.model.valueobjects.HolderId;
import pe.chambitalabs.platform.certification.domain.services.CertificateFingerprintService;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CertificateTest {

    private final Instant now = Instant.parse("2026-11-14T15:00:00Z");

    @Test
    void derivesTheHolderAddressFromTheLast20BytesOfKeccakOfThePseudonymousId() {
        var id = UUID.fromString("7f3a0c2e-1111-4222-8333-944455556666");
        var digest = Hash.sha3(id.toString().getBytes(StandardCharsets.UTF_8));
        var expected = Keys.toChecksumAddress(Numeric.toHexString(digest).substring(26));
        assertThat(new HolderId(id).toEthereumAddress()).isEqualTo(expected).startsWith("0x").hasSize(42);
    }

    @Test
    void fingerprintIsADeterministicBytes32OfTheCanonicalContent() {
        var certificateId = UUID.randomUUID();
        var holderId = UUID.randomUUID();
        var first = CertificateFingerprintService.fingerprint(certificateId, holderId, "Logo", "GRAPHIC_DESIGN", 5, now);
        var second = CertificateFingerprintService.fingerprint(certificateId, holderId, "Logo", "GRAPHIC_DESIGN", 5, now);
        var altered = CertificateFingerprintService.fingerprint(certificateId, holderId, "Logo", "GRAPHIC_DESIGN", 4, now);
        assertThat(first).isEqualTo(second).hasSize(66).startsWith("0x").isNotEqualTo(altered);
    }

    @Test
    void isIssuedWhenTheTransactionIsConfirmed() {
        var certificate = certificate();
        certificate.markSubmitted("0xabc");
        certificate.markIssued(BigInteger.ONE, now);
        assertThat(certificate.getStatus()).isEqualTo(CertificateStatus.ISSUED);
        assertThat(certificate.domainEventsForTest()).hasAtLeastOneElementOfType(CertificateIssued.class);
    }

    @Test
    void failsAfterThreeAttempts() {
        var certificate = certificate();
        for (var attempt = 1; attempt <= 3; attempt++) {
            certificate.markSubmitted("0x" + attempt);
            certificate.registerFailure("network error", 3, now);
        }
        assertThat(certificate.getStatus()).isEqualTo(CertificateStatus.FAILED);
        assertThat(certificate.domainEventsForTest()).hasAtLeastOneElementOfType(CertificateIssuanceFailed.class);
    }

    @Test
    void buildsTheVerificationLinkWithTheHash() {
        var certificate = certificate();
        assertThat(certificate.verificationUrl("https://verify.chambita.example"))
                .isEqualTo("https://verify.chambita.example/?hash=" + certificate.getCertificateHash());
    }

    private Certificate certificate() {
        return Certificate.forCompletedEngagement(UUID.randomUUID(), UUID.randomUUID(),
                CertificateHolder.forStudent(UUID.randomUUID()), "Identidad visual", "GRAPHIC_DESIGN", 5, now);
    }
}
