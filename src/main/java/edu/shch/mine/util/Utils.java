package edu.shch.mine.util;

import edu.shch.mine.MineSweeperPlugin;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.*;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {
    public static final UUID TEXTURE_PACK_ID = UUID.fromString("8b14bced-a79b-3645-8349-ea23ad0781aa");

    public static void defer(Runnable action) {
        MineSweeperPlugin.instance.getServer().getScheduler().runTaskLater(
            MineSweeperPlugin.instance,
            action,
            1
        );
    }

    public static void serveResources(int port, Player player) throws ExecutionException, InterruptedException, TimeoutException {
        new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(port)) {
                // Pre-Flight Request for Hash + Client Request
                for (int i = 0; i < 2; i++) {
                    try (Socket client = ss.accept()) {
                        System.out.printf("Accepted Client on Port %s%n", port);
                        OutputStream out = client.getOutputStream();
                        out.write("HTTP/1.1 200 OK\r\n".getBytes());
                        out.write("Content-Type: application/zip\r\n".getBytes());
                        out.write("Content-Disposition: attachment; filename=\"minesweeper.zip\"\r\n".getBytes());
                        out.write("Connection: close\r\n\r\n".getBytes());
                        createResourceZipStream(out);
                        out.flush();
                        System.out.println("Finished Transfer.");
                    }
                }
            } catch (IOException e) {
                System.err.printf("Couldn't open Server Socket on Port %d%n%s%n", port, e.getMessage());
            }
        }, "ResourcePack Server").start();

        if (player != null) {
            player.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                .packs(ResourcePackInfo.resourcePackInfo()
                    .id(Utils.TEXTURE_PACK_ID)
                    .uri(URI.create("http://127.0.0.1:%d".formatted(port)))
                    .computeHashAndBuild()
                    .get(5, TimeUnit.SECONDS)
                ).required(true).asResourcePackRequest());
        }
    }

    static void createResourceZipStream(OutputStream stream) {
        try (ZipOutputStream zos = new ZipOutputStream(stream)) {
            URL resource = Utils.class.getClassLoader().getResource("resources");
            if (resource == null) {
                System.err.println("Couldn't find Resource.");
                return;
            }
            URI uri = resource.toURI();

            if ("file".equals(uri.getScheme())) {
                Path resourceRoot = Paths.get(uri);
                try (Stream<Path> paths = Files.walk(resourceRoot)) {
                    paths.filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                String entryName = resourceRoot.relativize(path).toString();
                                zos.putNextEntry(new ZipEntry(entryName));
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                System.err.println("Couldn't create a Zip Entry.");
                            }
                        });
                }
            } else if ("jar".equals(uri.getScheme())) {
                URI jarUri = new URI(uri.toString().split("!", 2)[0]);  // jar:file:/path/to/your.jar
                try (FileSystem zipfs = FileSystems.newFileSystem(jarUri, Map.of())) {
                    Path resourceRoot = zipfs.getPath("/resources");  // Root of resources/
                    try (Stream<Path> paths = Files.walk(resourceRoot)) {
                        paths.filter(Files::isRegularFile)
                            .forEach(path -> {
                                try {
                                    String entryName = resourceRoot.relativize(path).toString();
                                    zos.putNextEntry(new ZipEntry(entryName));
                                    try (InputStream pathIn = Files.newInputStream(path)) {
                                        pathIn.transferTo(zos);
                                    }
                                    zos.closeEntry();
                                } catch (IOException e) {
                                    System.err.println("JAR copy failed: " + e);
                                }
                            });
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Couldn't create ZipOutputStream.");
        } catch (URISyntaxException e) {
            System.err.printf("Invalid URI: %s%n", e.getMessage());
        }
    }
}
