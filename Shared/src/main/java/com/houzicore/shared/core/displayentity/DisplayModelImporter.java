package com.houzicore.shared.core.displayentity;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Imports BDEngine/block-display model files into the local DisplayModel registry.
 */
public final class DisplayModelImporter {

    private static final int MAX_MODEL_BYTES = 8 * 1024 * 1024;
    private static final Pattern SAFE_ID = Pattern.compile("[a-z0-9_\\-]{1,48}");
    private static final Pattern BLOCK_DISPLAY_PAGE = Pattern.compile("https?://(?:www\\.)?block-display\\.com/bd/(\\d+)/?", Pattern.CASE_INSENSITIVE);
    private static final Pattern BDE_IMPORT_URL = Pattern.compile("https?://bdengine\\.app/\\?content=import&id=(\\d+)", Pattern.CASE_INSENSITIVE);

    private DisplayModelImporter() {
    }

    public static ImportResult importModel(DisplayEntityManager manager, String rawModelId, String source, boolean force) throws IOException {
        File modelsDir = getModelsDirectory(manager);
        SourceContent content = readSource(modelsDir, source);
        return importModelFromText(manager, rawModelId, content.text, source, content.sourceDescription, force);
    }

    public static SourceContent loadSource(DisplayEntityManager manager, String source) throws IOException {
        return readSource(getModelsDirectory(manager), source);
    }

    public static ImportResult importModelFromText(DisplayEntityManager manager, String rawModelId, String text, String extensionSource, String sourceDescription, boolean force) throws IOException {
        String modelId = sanitizeModelId(rawModelId);
        File modelsDir = getModelsDirectory(manager);
        if (!modelsDir.exists()) {
            modelsDir.mkdirs();
        }

        String extension = chooseExtension(extensionSource, text);
        File output = new File(modelsDir, modelId + extension);

        if (output.exists() && !force) {
            throw new IOException("Model file already exists. Add --force to overwrite: " + output.getName());
        }
        if (manager.getRegistry().getModel(modelId) != null && !force && !output.exists()) {
            throw new IOException("Model id is already registered. Pick another id or add --force: " + modelId);
        }

        DisplayModel model;
        try {
            model = ModelLoader.fromJson(modelId, text);
        } catch (RuntimeException e) {
            throw new IOException("Downloaded content is not a supported BDEngine/block-display export. " + hintFor(extensionSource, text), e);
        }

        Files.writeString(output.toPath(), text, StandardCharsets.UTF_8);
        manager.getRegistry().registerModel(model);
        return new ImportResult(modelId, output, model.getPartCount(), sourceDescription);
    }

    private static File getModelsDirectory(DisplayEntityManager manager) {
        File modelsDir = manager.getRegistry().getModelsDirectory();
        if (modelsDir == null) {
            modelsDir = new File(manager.getPlugin().getDataFolder(), "models");
        }
        return modelsDir;
    }

    public static String sanitizeModelId(String rawModelId) throws IOException {
        if (rawModelId == null) {
            throw new IOException("Missing model id.");
        }
        String modelId = rawModelId.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-]", "_");
        if (!SAFE_ID.matcher(modelId).matches()) {
            throw new IOException("Invalid model id. Use 1-48 chars: a-z, 0-9, _ or -.");
        }
        return modelId;
    }

    private static SourceContent readSource(File modelsDir, String source) throws IOException {
        if (source == null || source.trim().isEmpty()) {
            throw new IOException("Missing source URL or file name.");
        }

        String trimmed = source.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            String text = downloadText(trimmed);
            Matcher pageMatcher = BLOCK_DISPLAY_PAGE.matcher(trimmed);
            Matcher bdeMatcher = BDE_IMPORT_URL.matcher(trimmed);
            if (looksLikeHtml(text) && (pageMatcher.find() || bdeMatcher.find())) {
                String id = pageMatcher.matches() ? pageMatcher.group(1) : (bdeMatcher.matches() ? bdeMatcher.group(1) : "unknown");
                throw new IOException("That is a block-display page/editor URL, not the raw model export. Open it in BDEngine, export/download the .bdengine or .json file, then import that direct file URL or place it in the models folder. Model page id: " + id);
            }
            if (looksLikeHtml(text)) {
                throw new IOException("URL returned HTML, not a model export. Use a direct .bdengine/.json/.bdstudio export URL or a file in the models folder.");
            }
            return new SourceContent(text, trimmed);
        }

        File input = resolveSafeLocalFile(modelsDir, trimmed);
        String text = Files.readString(input.toPath(), StandardCharsets.UTF_8);
        return new SourceContent(text, input.getAbsolutePath());
    }

    private static File resolveSafeLocalFile(File modelsDir, String source) throws IOException {
        String fileName = source.startsWith("file:") ? source.substring("file:".length()) : source;
        File input = new File(fileName);
        if (!input.isAbsolute()) {
            input = new File(modelsDir, fileName);
        }

        File canonicalModels = modelsDir.getCanonicalFile();
        File canonicalInput = input.getCanonicalFile();
        if (!canonicalInput.toPath().startsWith(canonicalModels.toPath())) {
            throw new IOException("Local imports must be inside the models folder: " + canonicalModels.getAbsolutePath());
        }
        if (!canonicalInput.isFile()) {
            throw new IOException("Model source file not found: " + canonicalInput.getAbsolutePath());
        }
        if (canonicalInput.length() > MAX_MODEL_BYTES) {
            throw new IOException("Model file is too large. Limit: " + MAX_MODEL_BYTES + " bytes.");
        }
        return canonicalInput;
    }

    private static String downloadText(String source) throws IOException {
        URL url = URI.create(source).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(20000);
        connection.setRequestProperty("User-Agent", "HouziCore-DisplayModelImporter/1.0");

        int status = connection.getResponseCode();
        if (status < 200 || status >= 300) {
            throw new IOException("HTTP " + status + " while downloading model.");
        }
        int length = connection.getContentLength();
        if (length > MAX_MODEL_BYTES) {
            throw new IOException("Model download is too large. Limit: " + MAX_MODEL_BYTES + " bytes.");
        }

        byte[] bytes = connection.getInputStream().readNBytes(MAX_MODEL_BYTES + 1);
        if (bytes.length > MAX_MODEL_BYTES) {
            throw new IOException("Model download is too large. Limit: " + MAX_MODEL_BYTES + " bytes.");
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String chooseExtension(String source, String text) {
        String lower = source.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".json")) return ".json";
        if (lower.endsWith(".bdstudio")) return ".bdstudio";
        if (lower.endsWith(".bdengine")) return ".bdengine";
        String trimmed = text.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[") ? ".json" : ".bdengine";
    }

    private static boolean looksLikeHtml(String text) {
        String trimmed = text.trim().toLowerCase(Locale.ROOT);
        return trimmed.startsWith("<!doctype html") || trimmed.startsWith("<html") || trimmed.contains("<body");
    }

    private static String hintFor(String source, String text) {
        if (looksLikeHtml(text)) {
            return "The source returned HTML. Use a direct model export file instead of the website page URL.";
        }
        if (source.contains("block-display.com/bd/") || source.contains("bdengine.app/?content=import")) {
            return "Open the page in BDEngine and download/export the .bdengine file first.";
        }
        return "Use a .bdengine/.bdstudio/.json export from BDEngine/block-display.";
    }

    public record SourceContent(String text, String sourceDescription) {
    }

    public record ImportResult(String modelId, File file, int parts, String source) {
    }
}
