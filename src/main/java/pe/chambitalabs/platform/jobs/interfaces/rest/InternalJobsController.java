package pe.chambitalabs.platform.jobs.interfaces.rest;

import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import pe.chambitalabs.platform.certification.application.internal.commandservices.CertificateCommandService;
import pe.chambitalabs.platform.certification.domain.model.commands.CertificateCommands.RetryPendingCertificatesCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.AutoApproveOverdueDeliverablesCommand;
import pe.chambitalabs.platform.engagements.domain.services.EngagementCommandService;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Daily tasks that Vercel Cron invokes with "Authorization: Bearer CRON_SECRET": approves overdue
 * deliverables (7 days), confirms or retries pending certificates and resubmits incomplete events.
 */
@RestController
@Hidden
public class InternalJobsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalJobsController.class);

    private final EngagementCommandService engagementCommandService;
    private final CertificateCommandService certificateCommandService;
    private final IncompleteEventPublications incompleteEventPublications;
    private final String secret;

    public InternalJobsController(EngagementCommandService engagementCommandService,
                                  CertificateCommandService certificateCommandService,
                                  IncompleteEventPublications incompleteEventPublications,
                                  @Value("${chambita.jobs.secret}") String secret) {
        this.engagementCommandService = engagementCommandService;
        this.certificateCommandService = certificateCommandService;
        this.incompleteEventPublications = incompleteEventPublications;
        this.secret = secret;
    }

    @GetMapping("/internal/jobs/daily")
    public ResponseEntity<Map<String, Object>> daily(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (authorization == null || !MessageDigest.isEqual(authorization.getBytes(StandardCharsets.UTF_8),
                ("Bearer " + secret).getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var result = new LinkedHashMap<String, Object>();
        incompleteEventPublications.resubmitIncompletePublicationsOlderThan(Duration.ofMinutes(5));
        result.put("eventsResubmitted", true);
        result.put("deliverablesAutoApproved",
                engagementCommandService.handle(new AutoApproveOverdueDeliverablesCommand()));
        result.put("certificatesProcessed", certificateCommandService.handle(new RetryPendingCertificatesCommand()));
        LOGGER.info("Daily jobs executed: {}", result);
        return ResponseEntity.ok(result);
    }
}
