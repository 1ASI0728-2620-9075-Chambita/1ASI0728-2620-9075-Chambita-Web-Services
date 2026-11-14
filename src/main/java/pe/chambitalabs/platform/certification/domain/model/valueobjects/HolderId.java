package pe.chambitalabs.platform.certification.domain.model.valueobjects;

import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Pseudonymous id of a certificate holder. The certificate is minted to the address formed by
 * the last 20 bytes of keccak256(id): nobody holds its private key and no personal data is used.
 */
public record HolderId(UUID value) {

    public String toEthereumAddress() {
        var digest = Hash.sha3(value.toString().getBytes(StandardCharsets.UTF_8));
        var address = new byte[20];
        System.arraycopy(digest, digest.length - 20, address, 0, 20);
        return Keys.toChecksumAddress(org.web3j.utils.Numeric.toHexString(address));
    }
}
