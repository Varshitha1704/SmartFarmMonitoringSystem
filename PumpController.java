public class PumpController {
    private static final double LOW_MOISTURE_LIMIT = 35.0;
    private static final double RECOVERY_LIMIT = 55.0;
    private boolean pumpOn;

    public boolean updatePump(double soilMoisture) {
        if (soilMoisture < LOW_MOISTURE_LIMIT) {
            pumpOn = true;
        } else if (soilMoisture > RECOVERY_LIMIT) {
            pumpOn = false;
        }
        return pumpOn;
    }

    public boolean isPumpOn() {
        return pumpOn;
    }

    public static boolean isLowMoisture(double value) {
        return value < LOW_MOISTURE_LIMIT;
    }
}
