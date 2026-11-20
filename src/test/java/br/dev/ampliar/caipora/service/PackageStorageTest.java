package br.dev.ampliar.caipora.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PackageStorageTest {

    @TempDir
    Path root;

    private PackageStorage storage() {
        return new PackageStorage(root.toString(), List.of(" ZIP", ".msi", "tar.gz", "jar"));
    }

    @Test
    void listsOnlyAcceptedFilesDirectlyInsideTheApplicationFolder() throws IOException {
        Files.createDirectories(root.resolve("APP/sub"));
        Files.writeString(root.resolve("APP/app-1.zip"), "1");
        Files.writeString(root.resolve("APP/app-2.TAR.GZ"), "2");
        Files.writeString(root.resolve("APP/readme.txt"), "x");
        Files.writeString(root.resolve("APP/archive.gz"), "x");
        Files.writeString(root.resolve("APP/sub/nested.zip"), "x");

        final var names = storage().listPackages("APP").stream().map(PackageStorage.PackageFile::name).sorted().toList();

        assertEquals(List.of("app-1.zip", "app-2.TAR.GZ"), names);
    }

    @Test
    void missingFolderHasNoPackages() {
        assertTrue(storage().listPackages("NONE").isEmpty());
    }

    @Test
    void refusesNamesEscapingTheFolders() throws IOException {
        Files.createDirectories(root.resolve("APP"));
        Files.writeString(root.resolve("outside.zip"), "x");
        final PackageStorage storage = storage();

        assertThrows(IllegalArgumentException.class, () -> storage.resolvePackage("APP", "../outside.zip"));
        assertThrows(IllegalArgumentException.class, () -> storage.resolvePackage("..", "outside.zip"));
        assertThrows(IllegalArgumentException.class, () -> storage.applicationFolder("../etc"));
        assertThrows(IllegalArgumentException.class, () -> storage.applicationFolder("a/b"));
        assertThrows(IllegalArgumentException.class, () -> storage.resolvePackage("APP", "app.exe"));
    }

    @Test
    void displaysSizes() {
        assertEquals("512 B", PackageStorage.displaySize(512));
        assertEquals("1.5 KB", PackageStorage.displaySize(1536));
        assertEquals("2.0 GB", PackageStorage.displaySize(2L * 1024 * 1024 * 1024));
    }
}
