package pe.chambitalabs.platform.acceptance;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pe.chambitalabs.platform.certification.application.internal.outboundservices.BlockchainCertificateRegistry;
import pe.chambitalabs.platform.iam.application.internal.outboundservices.MailService;
import pe.chambitalabs.platform.support.EmbeddedPostgresConfiguration;

/**
 * Starts the API on a random port against a real PostgreSQL. The Mail and Blockchain gateways
 * are replaced by mocks so the scenarios can read the verification codes and simulate Sepolia.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "chambita.certification.confirmation-timeout-seconds=5")
@Import(EmbeddedPostgresConfiguration.class)
public class CucumberSpringConfiguration {

    @MockitoBean
    MailService mailService;

    @MockitoBean
    BlockchainCertificateRegistry blockchainCertificateRegistry;
}
