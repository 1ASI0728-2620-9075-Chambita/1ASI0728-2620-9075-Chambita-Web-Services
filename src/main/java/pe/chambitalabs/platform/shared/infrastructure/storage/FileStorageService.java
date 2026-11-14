package pe.chambitalabs.platform.shared.infrastructure.storage;

import java.net.URI;

/** Port to store portfolio images and deliverable files. */
public interface FileStorageService {

    boolean isConfigured();

    URI upload(String folder, String originalName, String contentType, byte[] content);
}
