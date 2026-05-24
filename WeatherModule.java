import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherModule extends JPanel {
    private final JLabel temp = Dashboard.valueLabel("-- C");
    private final JLabel rain = Dashboard.valueLabel("--");
    private final JLabel humidity = Dashboard.valueLabel("-- %");
    private final JLabel wind = Dashboard.valueLabel("-- m/s");
    private final JLabel status = new JLabel("Configure OpenWeather API key for live weather.");

    public WeatherModule() {
        setOpaque(false);
        setLayout(new BorderLayout(12, 12));
        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setOpaque(false);
        grid.add(Dashboard.card("Temperature", temp, "[T]"));
        grid.add(Dashboard.card("Rain Prediction", rain, "[R]"));
        grid.add(Dashboard.card("Humidity", humidity, "[H]"));
        grid.add(Dashboard.card("Wind Speed", wind, "[V]"));
        JButton refresh = Dashboard.button("Fetch Weather");
        refresh.addActionListener(e -> fetchWeather());
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(status, BorderLayout.CENTER);
        bottom.add(refresh, BorderLayout.EAST);
        add(grid, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public void fetchWeather() {
        String key = AppConfig.get("openweather.key", "");
        String city = AppConfig.get("weather.city", "Delhi");
        if (key.isBlank()) {
            status.setText("Demo mode: add openweather.key in config.properties.");
            temp.setText("29 C");
            humidity.setText("63 %");
            rain.setText("Light chance");
            wind.setText("3.4 m/s");
            return;
        }
        status.setText("Fetching weather for " + city + "...");
        Thread worker = new Thread(() -> {
            try {
                String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city.replace(" ", "%20") + "&appid=" + key + "&units=metric";
                HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
                String json = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).body();
                String t = extract(json, "\"temp\"\\s*:\\s*([-0-9.]+)");
                String h = extract(json, "\"humidity\"\\s*:\\s*([-0-9.]+)");
                String w = extract(json, "\"speed\"\\s*:\\s*([-0-9.]+)");
                String condition = extract(json, "\"main\"\\s*:\\s*\"([^\"]+)\"");
                SwingUtilities.invokeLater(() -> {
                    temp.setText(t + " C");
                    humidity.setText(h + " %");
                    wind.setText(w + " m/s");
                    rain.setText(condition.toLowerCase().contains("rain") ? "Likely" : "Low");
                    status.setText("Live weather updated for " + city + ".");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> status.setText("Weather fetch failed: " + ex.getMessage()));
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    private String extract(String json, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(json);
        return matcher.find() ? matcher.group(1) : "--";
    }
}
