package engine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class Utils {

    private Utils() {
        // Utility class
    }

    public static String readFile(String filePath) {
        String str;
        try {
            str = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException excp) {
            throw new RuntimeException("Error reading file [" + filePath + "]", excp);
        }
        return str;
    }

    public static String readFileFromResources(String resourcePath) {
        try {
            InputStream inputStream = Utils.class.getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new RuntimeException("Resource not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error reading resource: " + resourcePath, e);
        }
    }

}