import java.time.LocalDateTime;

public class SensorReading {
    public final double temperature;
    public final double humidity;
    public final double soilMoisture;
    public final double waterLevel;
    public final boolean pumpOn;
    public final double waterUsage;
    public final LocalDateTime timestamp;

    public SensorReading(double temperature, double humidity, double soilMoisture,
                         double waterLevel, boolean pumpOn, double waterUsage) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.soilMoisture = soilMoisture;
        this.waterLevel = waterLevel;
        this.pumpOn = pumpOn;
        this.waterUsage = waterUsage;
        this.timestamp = LocalDateTime.now();
    }
}
