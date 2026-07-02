package com.example.lockly.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FcmConfig {

    @PostConstruct
    public void init() {
        try {
//            FileInputStream serviceAccount =
//                    new FileInputStream("src/main/resources/lockly-fcm-firebase-adminsdk-fbsvc-59f4582a60.json");
            InputStream serviceAccount =
                    getClass().getClassLoader()
                            .getResourceAsStream("lockly-fcm-firebase-adminsdk-fbsvc-59f4582a60.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            System.out.println("Firebase initialized");

        } catch (Exception e) {
            throw new RuntimeException("Firebase init failed", e);
        }
    }
}