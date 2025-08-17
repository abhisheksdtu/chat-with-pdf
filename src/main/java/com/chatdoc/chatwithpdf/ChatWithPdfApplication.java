package com.chatdoc.chatwithpdf;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
@Slf4j
public class ChatWithPdfApplication {

    private final Environment env;

    public ChatWithPdfApplication(Environment env) {
        this.env = env;
    }

    public static void main(String[] args) {
        SpringApplication.run(ChatWithPdfApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logSwaggerUrl() {
        String port = env.getProperty("local.server.port",
                env.getProperty("server.port", "8080"));
        String contextPath = env.getProperty("server.servlet.context-path", "");
        if ("/".equals(contextPath)) contextPath = "";

        String base = "http://localhost:" + port + contextPath;
        String swaggerUrl = base + "/swagger-ui/index.html";

        log.info("\n----------------------------------------------------------\n" +
                "  Application is ready!\n" +
                "  Swagger UI: {}\n" +
                "----------------------------------------------------------", swaggerUrl);
    }
}
