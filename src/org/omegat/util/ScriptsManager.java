package org.omegat.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ScriptsManager
 *
 * - Descobre/gera uma pasta de scripts gravável pelo usuário (fora do .app no macOS).
 * - Se essa pasta estiver vazia, copia os scripts padrão do projeto.
 * - Isso resolve o problema do macOS notarizado descrito no feature request #1781.
 */
public class ScriptsManager {

    /**
     * Retorna o diretório de scripts do usuário para cada SO.
     * macOS:   ~/Library/Application Support/OmegaT/scripts
     * Windows: %APPDATA%/OmegaT/scripts
     * Linux:   ~/.config/omegat/scripts
     */
    public static Path getUserScriptsDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");

        if (os.contains("mac")) {
            // macOS
            return Paths.get(home, "Library", "Application Support", "OmegaT", "scripts");
        } else if (os.contains("win")) {
            // Windows
            String appdata = System.getenv("APPDATA");
            if (appdata == null || appdata.isEmpty()) {
                appdata = Paths.get(home, "AppData", "Roaming").toString();
            }
            return Paths.get(appdata, "OmegaT", "scripts");
        } else {
            // Linux / Unix
            return Paths.get(home, ".config", "omegat", "scripts");
        }
    }

    /**
     * Garante que a pasta externa de scripts existe.
     * Se estiver vazia, copia os scripts padrão do diretório "scripts" do projeto.
     */
    public static void ensureUserScriptsWithDefaults() throws IOException {
        Path userDir = getUserScriptsDir();

        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        // se já tem arquivo lá dentro, não sobrescreve
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(userDir)) {
            if (ds.iterator().hasNext()) {
                return;
            }
        }

        // está vazio → copiar da pasta "scripts" (raiz do projeto OmegaT)
        Path bundledScripts = Paths.get("scripts");
        if (Files.exists(bundledScripts) && Files.isDirectory(bundledScripts)) {
            copyRecursiveFromFilesystem(bundledScripts, userDir);
        }
    }

    /**
     * Diretórios onde o OmegaT deve procurar scripts.
     * (Por enquanto só devolve a pasta do usuário.)
     */
    public static List<Path> getScriptSearchPaths() {
        List<Path> paths = new ArrayList<>();
        paths.add(getUserScriptsDir());
        return paths;
    }

    private static void copyRecursiveFromFilesystem(Path source, Path target) throws IOException {
        Files.walk(source).forEach(src -> {
            try {
                Path rel = source.relativize(src);
                Path dest = target.resolve(rel.toString());

                if (Files.isDirectory(src)) {
                    if (!Files.exists(dest)) {
                        Files.createDirectories(dest);
                    }
                } else {
                    Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
