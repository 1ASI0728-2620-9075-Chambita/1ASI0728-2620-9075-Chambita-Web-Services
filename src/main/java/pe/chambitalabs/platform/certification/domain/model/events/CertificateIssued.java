package pe.chambitalabs.platform.certification.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

/** The certificate was registered on Ethereum Sepolia; Profiles increases the certificate count. */
public record CertificateIssued(UUID certificateId, UUID studentId, UUID engagementId, String certificateHash, BigInteger tokenId, String transactionHash, Instant occurredOn) implements DomainEvent {
}
