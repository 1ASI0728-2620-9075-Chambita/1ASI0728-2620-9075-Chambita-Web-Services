package pe.chambitalabs.platform.shared.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Locale;
import java.util.UUID;

/**
 * Storage Gateway: uploads files to a public Supabase Storage bucket with the REST API.
 */
@Service
public class SupabaseFileStorageService implements FileStorageService {

    private final String supabaseUrl;
    private final String serviceKey;
    private final String bucket;
    private final RestClient restClient;

    public SupabaseFileStorageService(@Value("${chambita.storage.supabase-url:}") String supabaseUrl,
                                      @Value("${chambita.storage.service-key:}") String serviceKey,
                                      @Value("${chambita.storage.bucket:chambita-files}") String bucket) {
        this.supabaseUrl = supabaseUrl.endsWith("/") ? supabaseUrl.substring(0, supabaseUrl.length() - 1) : supabaseUrl;
        this.serviceKey = serviceKey;
        this.bucket = bucket;
        this.restClient = RestClient.create();
    }

    @Override
    public boolean isConfigured() {
        return !supabaseUrl.isBlank() && !serviceKey.isBlank();
    }

    @Override
    public URI upload(String folder, String originalName, String contentType, byte[] content) {
        var path = folder + "/" + UUID.randomUUID() + "-" + sanitize(originalName);
        restClient.post()
                .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + path)
                .header("Authorization", "Bearer " + serviceKey)
                .header("apikey", serviceKey)
                .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType))
                .body(content)
                .retrieve()
                .toBodilessEntity();
        return URI.create(supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path);
    }

    private static String sanitize(String name) {
        var safe = name == null ? "file" : name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "-");
        return safe.length() > 80 ? safe.substring(safe.length() - 80) : safe;
    }
}
