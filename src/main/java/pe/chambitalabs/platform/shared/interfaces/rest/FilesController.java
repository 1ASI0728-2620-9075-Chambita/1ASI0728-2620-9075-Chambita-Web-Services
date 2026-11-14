package pe.chambitalabs.platform.shared.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.chambitalabs.platform.shared.domain.exceptions.BadRequestException;
import pe.chambitalabs.platform.shared.domain.exceptions.BusinessRuleException;
import pe.chambitalabs.platform.shared.infrastructure.storage.FileStorageService;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/** Uploads portfolio images and deliverable files and returns their public URL. */
@RestController
@Tag(name = "Files", description = "File uploads to Supabase Storage")
public class FilesController {

    private static final Set<String> FOLDERS = Set.of("portfolio", "deliverables");

    private final FileStorageService storage;

    public FilesController(FileStorageService storage) {
        this.storage = storage;
    }

    @PostMapping(value = "/api/v1/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Uploads a file (folder: portfolio or deliverables) and returns its URL")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                                      @RequestParam(defaultValue = "deliverables") String folder)
            throws IOException {
        if (!storage.isConfigured()) {
            throw new BusinessRuleException("storage.not-configured");
        }
        if (file.isEmpty() || !FOLDERS.contains(folder)) {
            throw new BadRequestException("storage.file-empty");
        }
        var url = storage.upload(folder, file.getOriginalFilename(), file.getContentType(), file.getBytes());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", url.toString()));
    }
}
