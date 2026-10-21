package com.freelance;

import com.freelance.ui.MainFrame;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.awt.GraphicsEnvironment;

/**
 * Main entry point for Freelance . application.
 * Boots the Spring Boot 3 backend on http://localhost:8080
 * and launches the modern desktop interface.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        // Allow Swing desktop UI in Spring Boot
        ConfigurableApplicationContext context = new SpringApplicationBuilder(Application.class)
                .headless(false)
                .run(args);

        // Open Swing GUI on the Event Dispatch Thread
        if (!GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            });
        }
    }
}
