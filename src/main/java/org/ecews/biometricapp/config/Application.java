package org.ecews.biometricapp.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Configuration("applicationProperties")
@Data
@Slf4j
public class Application {
    private String serverUrl;
    private String libraryPath;
    private int quality;
    public static String biometricDirectory;

    @PostConstruct
    public void setBiometricDirectory(){
        biometricDirectory = getLibraryPath();
        log.info("Path working directory ******* {}", biometricDirectory);
    }
}
