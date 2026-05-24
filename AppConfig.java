import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static final Properties PROPS = new Properties();

    static {
        try (FileInputStream in = new FileInputStream("config.properties")) {
            PROPS.load(in);
        } catch (IOException ignored) {
            // Environment variables are enough for a first run.
        }
    }

    public static String get(String key, String defaultValue) {
        String envKey = "SMARTFARM_" + key.toUpperCase().replace('.', '_');
        String value = System.getenv(envKey);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return PROPS.getProperty(key, defaultValue);
    }

    public static boolean has(String key) {
        String value = get(key, "");
        return value != null && !value.isBlank();
    }
}
