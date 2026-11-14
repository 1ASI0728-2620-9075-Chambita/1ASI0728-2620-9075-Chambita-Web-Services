package pe.chambitalabs.platform.certification.infrastructure.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Numeric;
import pe.chambitalabs.platform.certification.application.internal.outboundservices.BlockchainCertificateRegistry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

/**
 * Blockchain Gateway (anti-corruption layer): signs transactions with the issuer account, whose
 * private key comes from an environment variable, and reaches Sepolia through Alchemy (Web3j).
 */
@Component
public class Web3jCertificateRegistry implements BlockchainCertificateRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(Web3jCertificateRegistry.class);
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(350_000);
    private static final Event CERTIFICATE_ISSUED = new Event("CertificateIssued", List.of(
            new TypeReference<Uint256>(true) { }, new TypeReference<Address>(true) { },
            new TypeReference<Bytes32>(true) { }));
    private static final String CERTIFICATE_ISSUED_TOPIC = EventEncoder.encode(CERTIFICATE_ISSUED);

    private final String contractAddress;
    private final Web3j web3j;
    private final RawTransactionManager transactionManager;

    public Web3jCertificateRegistry(@Value("${chambita.blockchain.rpc-url:}") String rpcUrl,
                                    @Value("${chambita.blockchain.issuer-private-key:}") String issuerPrivateKey,
                                    @Value("${chambita.blockchain.contract-address:}") String contractAddress,
                                    @Value("${chambita.blockchain.chain-id:11155111}") long chainId) {
        this.contractAddress = contractAddress;
        if (rpcUrl.isBlank() || issuerPrivateKey.isBlank() || contractAddress.isBlank()) {
            LOGGER.warn("Blockchain gateway not configured: certificates stay PENDING until it is.");
            this.web3j = null;
            this.transactionManager = null;
        } else {
            this.web3j = Web3j.build(new HttpService(rpcUrl));
            this.transactionManager = new RawTransactionManager(web3j, Credentials.create(issuerPrivateKey), chainId);
        }
    }

    @Override
    public boolean isConfigured() {
        return web3j != null;
    }

    @Override
    public String issue(String holderAddress, String certificateHash) {
        var function = new Function("issue", List.of(new Address(holderAddress),
                new Bytes32(Numeric.hexStringToByteArray(certificateHash))), List.of(new TypeReference<Uint256>() { }));
        return send(FunctionEncoder.encode(function));
    }

    @Override
    public String revoke(BigInteger tokenId) {
        var function = new Function("revoke", List.of(new Uint256(tokenId)), List.of());
        return send(FunctionEncoder.encode(function));
    }

    @Override
    public Optional<Issuance> findIssuance(String transactionHash) {
        requireConfigured();
        try {
            var receipt = web3j.ethGetTransactionReceipt(transactionHash).send().getTransactionReceipt();
            if (receipt.isEmpty()) {
                return Optional.empty();
            }
            if (!receipt.get().isStatusOK()) {
                return Optional.of(new Issuance(false, null, "transaction reverted"));
            }
            return receipt.get().getLogs().stream()
                    .filter(log -> !log.getTopics().isEmpty() && CERTIFICATE_ISSUED_TOPIC.equals(log.getTopics().get(0)))
                    .findFirst()
                    .map(log -> new Issuance(true, Numeric.toBigInt(log.getTopics().get(1)), null))
                    .or(() -> Optional.of(new Issuance(false, null, "CertificateIssued event not found")));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @Override
    public String contractAddress() {
        return contractAddress;
    }

    private String send(String data) {
        requireConfigured();
        try {
            var gasPrice = web3j.ethGasPrice().send().getGasPrice().multiply(BigInteger.valueOf(12))
                    .divide(BigInteger.TEN);
            var response = transactionManager.sendTransaction(gasPrice, GAS_LIMIT, contractAddress, data,
                    BigInteger.ZERO);
            if (response.hasError()) {
                throw new IllegalStateException("RPC error: " + response.getError().getMessage());
            }
            return response.getTransactionHash();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException("blockchain gateway not configured");
        }
    }
}
