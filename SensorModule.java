import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SensorModule {
    private final Random random = new Random();
    private final PumpController pumpController;
    private final List<SensorReading> history = new ArrayList<>();
    private double waterUsage = 0;

    public SensorModule(PumpController pumpController) {
        this.pumpController = pumpController;
    }

    public SensorReading nextReading() {
        double temperature = round(22 + random.nextDouble() * 18);
        double humidity = round(45 + random.nextDouble() * 45);
        double soilMoisture = round(18 + random.nextDouble() * 65);
        double waterLevel = round(35 + random.nextDouble() * 65);
        boolean pumpOn = pumpController.updatePump(soilMoisture);
        if (pumpOn) {
            waterUsage += 1.2 + random.nextDouble() * 2.2;
        }
        SensorReading reading = new SensorReading(temperature, humidity, soilMoisture, waterLevel, pumpOn, round(waterUsage));
        history.add(reading);
        if (history.size() > 40) {
            history.remove(0);
        }
        DatabaseConnection.saveSensorReading(reading);
        return reading;
    }

    public List<SensorReading> getHistory() {
        return Collections.unmodifiableList(history);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
