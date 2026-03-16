package edu.shch.mine;

import edu.shch.mine.util.Utils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {
    @Test
    void serveResources() {
        Random random = new Random(42L);
        int port = random.nextInt(1 << 10, 1 << 16);

        File file = Paths.get("build/minesweeper.zip").toFile();
        if (file.exists()) {
            assertTrue(file.delete());
        }

        Utils.serveResources(port);

        try (HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build()
        ) {
            HttpRequest request = HttpRequest
                .newBuilder(URI.create("http://127.0.0.1:" + port)).build();

            AtomicReference<HttpResponse<Path>> response = new AtomicReference<>();
            assertDoesNotThrow(() -> response.set(client.sendAsync(request,
                    HttpResponse.BodyHandlers.ofFile(Paths.get("build/minesweeper.zip")))
                .get(30, TimeUnit.SECONDS)));

            assertEquals(200, response.get().statusCode());
            assertTrue(file.exists());
            assertTrue(file.length() > (1 << 10)); // > 1KiB
        }
    }
}