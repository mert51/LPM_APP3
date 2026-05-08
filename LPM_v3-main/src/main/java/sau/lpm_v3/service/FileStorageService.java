package sau.lpm_v3.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${upload.static.path}")
    private String uploadFolder;

    public String saveFile(MultipartFile file) {
        if (file.isEmpty()) return null;

        // Dosya adını benzersiz yapalım
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        try {
            // Proje dizinine göre yolu belirle
            // Ensure upload folder exists (create directories if necessary)
            Path uploadDir = Paths.get(uploadFolder);
            if (!uploadDir.isAbsolute()) {
                uploadDir = uploadDir.toAbsolutePath().normalize();
            }
            Files.createDirectories(uploadDir);

            // Target path for the new file
            Path path = uploadDir.resolve(fileName);

            // Dosyayı fiziksel olarak kopyala
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            log.info("IMAGE UPLOAD: File saved to static folder: [{}] (abs: {})", fileName, path.toString());
            return fileName;
        } catch (IOException e) {
            log.error("Could not store file inside static folder!", e);
            return null;
        }
    }
}