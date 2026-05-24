import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SMSAlert {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static void send(String message) {
        String sid = AppConfig.get("twilio.sid", "");
        String token = AppConfig.get("twilio.token", "");
        String from = AppConfig.get("twilio.from", "");
        String to = AppConfig.get("twilio.to", "");
        if (sid.isBlank() || token.isBlank() || from.isBlank() || to.isBlank()) {
            System.out.println("[SMS demo] " + message);
            return;
        }
        try {
            String body = "From=" + enc(from) + "&To=" + enc(to) + "&Body=" + enc(message);
            String auth = Base64.getEncoder().encodeToString((sid + ":" + token).getBytes(StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + sid + "/Messages.json"))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(resp -> System.out.println("[Twilio] HTTP " + resp.statusCode()));
        } catch (Exception ex) {
            System.out.println("[Twilio] SMS failed: " + ex.getMessage());
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
