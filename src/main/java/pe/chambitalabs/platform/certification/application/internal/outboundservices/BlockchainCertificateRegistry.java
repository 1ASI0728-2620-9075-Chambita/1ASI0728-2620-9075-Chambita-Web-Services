package pe.chambitalabs.platform.certification.application.internal.outboundservices;

import java.math.BigInteger;
import java.util.Optional;

/** Port to the certificate smart contract on Ethereum Sepolia. */
public interface BlockchainCertificateRegistry {

    boolean isConfigured();

    /** Sends the issue transaction and returns its hash. */
    String issue(String holderAddress, String certificateHash);

    /** Returns the outcome of a mined transaction, or empty while it is still pending. */
    Optional<Issuance> findIssuance(String transactionHash);

    /** Sends the revoke transaction and returns its hash. */
    String revoke(BigInteger tokenId);

    String contractAddress();

    /** Outcome of a mined issuance transaction. */
    record Issuance(boolean successful, BigInteger tokenId, String error) {
    }
}
