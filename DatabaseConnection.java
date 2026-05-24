import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DatabaseConnection {
    private static boolean initialized;
    private static boolean unavailableNoticeShown;
    private static final Map<String, DemoUser> demoFarmers = new HashMap<>();

    public static Connection getConnection() throws SQLException {
        String url = AppConfig.get("db.url", "jdbc:mysql://localhost:3306/smartfarm");
        String user = AppConfig.get("db.user", "root");
        String password = AppConfig.get("db.password", "");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL JDBC driver not found. Add mysql-connector-j to the classpath.", ex);
        }
        return DriverManager.getConnection(url, user, password);
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (username VARCHAR(60) PRIMARY KEY, password VARCHAR(120) NOT NULL, role VARCHAR(20) NOT NULL DEFAULT 'farmer')");
            try {
                st.executeUpdate("ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'farmer'");
            } catch (SQLException ignored) {
                // Existing databases may already have the role column.
            }
            st.executeUpdate("CREATE TABLE IF NOT EXISTS sensor_readings (id INT AUTO_INCREMENT PRIMARY KEY, temperature DOUBLE, humidity DOUBLE, soil_moisture DOUBLE, water_level DOUBLE, pump_status VARCHAR(10), water_usage DOUBLE, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS disease_predictions (id INT AUTO_INCREMENT PRIMARY KEY, image_path VARCHAR(500), disease_name VARCHAR(120), cause_text TEXT, pesticide TEXT, prevention TEXT, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS farmer_profiles (username VARCHAR(60) PRIMARY KEY, full_name VARCHAR(120), phone VARCHAR(40), crop VARCHAR(80), farm_location VARCHAR(160), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            st.executeUpdate("INSERT IGNORE INTO users(username, password, role) VALUES ('admin', 'admin123', 'admin')");
            st.executeUpdate("INSERT IGNORE INTO users(username, password, role) VALUES ('farmer', 'farmer123', 'farmer')");
            st.executeUpdate("INSERT IGNORE INTO farmer_profiles(username, full_name, phone, crop, farm_location) VALUES ('farmer', 'Demo Farmer', '9999999999', 'Tomato', 'Demo Farm')");
        } catch (SQLException ex) {
            showUnavailableNotice(ex);
        }
    }

    public static boolean validateLogin(String username, String password) {
        return validateLogin(username, password, "farmer");
    }

    public static boolean validateLogin(String username, String password, String role) {
        initialize();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM users WHERE username=? AND password=? AND role=?")) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            if ("admin".equals(role)) {
                return "admin".equals(username) && "admin123".equals(password);
            }
            if ("farmer".equals(role) && "farmer".equals(username) && "farmer123".equals(password)) {
                return true;
            }
            DemoUser demoUser = demoFarmers.get(username);
            return "farmer".equals(role) && demoUser != null && demoUser.password.equals(password);
        }
    }

    public static String registerFarmer(String username, String password, String fullName, String phone, String crop, String location) {
        initialize();
        if ("admin".equalsIgnoreCase(username)) {
            return "This username is reserved for admin.";
        }
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement check = con.prepareStatement("SELECT 1 FROM users WHERE username=?")) {
                check.setString(1, username);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        con.rollback();
                        return "Username already exists. Please choose another username.";
                    }
                }
            }
            try (PreparedStatement user = con.prepareStatement("INSERT INTO users(username, password, role) VALUES (?, ?, 'farmer')");
                 PreparedStatement profile = con.prepareStatement("INSERT INTO farmer_profiles(username, full_name, phone, crop, farm_location) VALUES (?, ?, ?, ?, ?)")) {
                user.setString(1, username);
                user.setString(2, password);
                user.executeUpdate();
                profile.setString(1, username);
                profile.setString(2, fullName);
                profile.setString(3, phone);
                profile.setString(4, crop);
                profile.setString(5, location);
                profile.executeUpdate();
                con.commit();
                return null;
            } catch (SQLException ex) {
                con.rollback();
                return "Could not register farmer: " + ex.getMessage();
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            if ("farmer".equals(username) || demoFarmers.containsKey(username)) {
                return "Username already exists. Please choose another username.";
            }
            demoFarmers.put(username, new DemoUser(password, fullName, phone, crop, location));
            showUnavailableNotice(ex);
            return null;
        }
    }

    public static int countRows(String tableName) {
        initialize();
        if (!"sensor_readings".equals(tableName) && !"disease_predictions".equals(tableName)) {
            return 0;
        }
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException ex) {
            return 0;
        }
    }

    public static int countUsersByRole(String role) {
        initialize();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM users WHERE role=?")) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            return "farmer".equals(role) ? 1 + demoFarmers.size() : 0;
        }
    }

    public static void saveSensorReading(SensorReading r) {
        initialize();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO sensor_readings(temperature, humidity, soil_moisture, water_level, pump_status, water_usage) VALUES (?,?,?,?,?,?)")) {
            ps.setDouble(1, r.temperature);
            ps.setDouble(2, r.humidity);
            ps.setDouble(3, r.soilMoisture);
            ps.setDouble(4, r.waterLevel);
            ps.setString(5, r.pumpOn ? "ON" : "OFF");
            ps.setDouble(6, r.waterUsage);
            ps.executeUpdate();
        } catch (SQLException ex) {
            showUnavailableNotice(ex);
        }
    }

    public static void savePrediction(DiseaseResult result, String imagePath) {
        initialize();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO disease_predictions(image_path, disease_name, cause_text, pesticide, prevention) VALUES (?,?,?,?,?)")) {
            ps.setString(1, imagePath);
            ps.setString(2, result.diseaseName);
            ps.setString(3, result.cause);
            ps.setString(4, result.pesticide);
            ps.setString(5, result.prevention);
            ps.executeUpdate();
        } catch (SQLException ex) {
            showUnavailableNotice(ex);
        }
    }

    private static void showUnavailableNotice(SQLException ex) {
        if (!unavailableNoticeShown) {
            unavailableNoticeShown = true;
            System.out.println("[SmartFarm] Database offline: " + ex.getMessage());
        }
    }

    private static class DemoUser {
        final String password;
        final String fullName;
        final String phone;
        final String crop;
        final String location;

        DemoUser(String password, String fullName, String phone, String crop, String location) {
            this.password = password;
            this.fullName = fullName;
            this.phone = phone;
            this.crop = crop;
            this.location = location;
        }
    }
}
