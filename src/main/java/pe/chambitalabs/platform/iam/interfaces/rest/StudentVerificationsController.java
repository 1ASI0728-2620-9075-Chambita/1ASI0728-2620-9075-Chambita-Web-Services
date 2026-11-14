package pe.chambitalabs.platform.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.chambitalabs.platform.iam.domain.model.commands.ConfirmStudentVerificationCommand;
import pe.chambitalabs.platform.iam.domain.model.commands.RequestStudentVerificationCommand;
import pe.chambitalabs.platform.iam.domain.services.StudentVerificationCommandService;
import pe.chambitalabs.platform.iam.interfaces.rest.resources.StudentVerificationConfirmationResource;
import pe.chambitalabs.platform.iam.interfaces.rest.resources.StudentVerificationRequestResource;

import java.util.Map;

/** Institutional email verification (TS02). */
@RestController
@RequestMapping("/api/v1/student-verifications")
@Tag(name = "Student verifications", description = "Institutional email verification")
@SecurityRequirements
public class StudentVerificationsController {

    private final StudentVerificationCommandService verificationService;

    public StudentVerificationsController(StudentVerificationCommandService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping
    @Operation(summary = "Sends a new verification code to the institutional email")
    public ResponseEntity<Map<String, String>> request(@Valid @RequestBody StudentVerificationRequestResource resource) {
        verificationService.handle(new RequestStudentVerificationCommand(resource.email()));
        return ResponseEntity.accepted().body(Map.of("status", "CODE_SENT"));
    }

    @PostMapping("/confirmations")
    @Operation(summary = "Confirms the verification code and marks the student as verified")
    public ResponseEntity<Map<String, String>> confirm(@Valid @RequestBody StudentVerificationConfirmationResource resource) {
        verificationService.handle(new ConfirmStudentVerificationCommand(resource.email(), resource.code()));
        return ResponseEntity.ok(Map.of("status", "VERIFIED"));
    }
}
