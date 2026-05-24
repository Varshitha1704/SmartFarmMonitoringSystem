import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.imageio.ImageIO;

public class DiseasePrediction extends JPanel {
    private final JLabel preview = new JLabel("Upload leaf image", JLabel.CENTER);
    private final JTextArea resultArea = new JTextArea();
    private File selectedImage;

    public DiseasePrediction() {
        setOpaque(false);
        setLayout(new BorderLayout(14, 14));
        preview.setPreferredSize(new Dimension(320, 260));
        preview.setOpaque(true);
        preview.setBackground(Color.WHITE);
        preview.setBorder(BorderFactory.createLineBorder(new Color(220, 228, 235)));
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setFont(Dashboard.font(14, false));
        resultArea.setText("Disease name, cause, pesticide, and prevention tips will appear here.");
        JButton upload = Dashboard.button("Upload Image");
        JButton predict = Dashboard.button("Predict Disease");
        upload.addActionListener(e -> chooseImage());
        predict.addActionListener(e -> predict());
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.add(upload);
        actions.add(predict);
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setOpaque(false);
        left.add(preview, BorderLayout.CENTER);
        left.add(actions, BorderLayout.SOUTH);
        add(left, BorderLayout.WEST);
        add(Dashboard.wrapCard("AI Disease Prediction", resultArea), BorderLayout.CENTER);
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Plant images", "jpg", "jpeg", "png", "bmp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImage = chooser.getSelectedFile();
            ImageIcon icon = new ImageIcon(selectedImage.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(300, 220, Image.SCALE_SMOOTH);
            preview.setText("");
            preview.setIcon(new ImageIcon(scaled));
        }
    }

    private void predict() {
        if (selectedImage == null) {
            resultArea.setText("Please upload a plant or leaf image first.");
            return;
        }
        resultArea.setText("Analyzing image with AI model...");
        Thread worker = new Thread(() -> {
            DiseaseResult result = runPythonPrediction(selectedImage);
            DatabaseConnection.savePrediction(result, selectedImage.getAbsolutePath());
            if (shouldAlert(result)) {
                SMSAlert.send("Disease detected: " + result.diseaseName + ". Suggested pesticide: " + result.pesticide);
                VoiceAlert.speak("Warning! " + result.diseaseName + " detected.");
            }
            SwingUtilities.invokeLater(() -> resultArea.setText(format(result)));
        });
        worker.setDaemon(true);
        worker.start();
    }

    private DiseaseResult runPythonPrediction(File image) {
        File script = new File("AIModel/predict.py");
        StringBuilder diagnostics = new StringBuilder();
        if (script.exists()) {
            for (List<String> command : pythonCommands(script, image)) {
                try {
                    ProcessBuilder pb = new ProcessBuilder(command);
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    String lastResultLine = null;
                    StringBuilder output = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (output.length() < 700) {
                                output.append(line).append(" ");
                            }
                            if (line.contains("|")) {
                                lastResultLine = line;
                            }
                        }
                    }
                    int exitCode = process.waitFor();
                    DiseaseResult parsed = parseResult(lastResultLine);
                    if (parsed != null) {
                        return parsed;
                    }
                    diagnostics.append("Command failed: ").append(String.join(" ", command))
                            .append(" | exit=").append(exitCode)
                            .append(" | output=").append(output).append("\n");
                } catch (Exception ignored) {
                    diagnostics.append("Command not available: ").append(String.join(" ", command))
                            .append(" | ").append(ignored.getClass().getSimpleName()).append(": ")
                            .append(ignored.getMessage()).append("\n");
                }
            }
        } else {
            diagnostics.append("AIModel/predict.py not found.\n");
        }
        DiseaseResult fallback = demoPrediction(image);
        return withEngineStatus(fallback, "TensorFlow model was NOT used. Java visual fallback was used.\n" + diagnostics);
    }

    private List<List<String>> pythonCommands(File script, File image) {
        String configured = AppConfig.get("python.command", "").trim();
        List<List<String>> commands = new ArrayList<>();
        if (!configured.isBlank()) {
            List<String> parts = new ArrayList<>(Arrays.asList(configured.split("\\s+")));
            parts.add(script.getPath());
            parts.add(image.getAbsolutePath());
            commands.add(parts);
        }
        commands.add(Arrays.asList("python", script.getPath(), image.getAbsolutePath()));
        commands.add(Arrays.asList("py", "-3", script.getPath(), image.getAbsolutePath()));
        commands.add(Arrays.asList("python3", script.getPath(), image.getAbsolutePath()));
        return commands;
    }

    private DiseaseResult parseResult(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split("\\|", -1);
            if (parts.length >= 4) {
            DiseaseResult aiResult = new DiseaseResult(parts[0], parts[1], parts[2], parts[3]);
            if (parts.length >= 6 && "tensorflow".equalsIgnoreCase(parts[4]) && isLowConfidence(parts[0], parts[5])) {
                return new DiseaseResult("Uncertain - Retake Leaf Photo",
                        "The trained model confidence was low. The image may be unclear, too dark, too far away, or outside the model's training examples.",
                        "Do not apply pesticide based only on this result",
                        "Retake the photo in bright light with one leaf centered on a plain background, then predict again.",
                        "TensorFlow produced a low-confidence result: " + parts[0] + " at " + parts[5] + "%." + topPredictionText(parts));
            }
            return new DiseaseResult(aiResult.diseaseName, aiResult.cause, aiResult.pesticide, aiResult.prevention,
                    engineStatusText(parts));
        }
        return null;
    }

    private boolean isLowConfidence(String diseaseName, String value) {
        try {
            double confidence = Double.parseDouble(value);
            boolean healthy = diseaseName.toLowerCase(Locale.ROOT).contains("healthy");
            return healthy ? confidence < 55.0 : confidence < 90.0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String engineStatusText(String[] parts) {
        if (parts.length < 5 || parts[4].isBlank()) {
            return "";
        }
        return parts[4] + topPredictionText(parts);
    }

    private String topPredictionText(String[] parts) {
        if (parts.length >= 7 && !parts[6].isBlank()) {
            return " | Top probabilities: " + parts[6];
        }
        return "";
    }

    private DiseaseResult demoPrediction(File image) {
        DiseaseResult visualResult = visualFallbackPrediction(image);
        if (visualResult != null) {
            return visualResult;
        }

        String name = image.getName().toLowerCase(Locale.ROOT);
        if (name.contains("rust")) {
            return new DiseaseResult("Leaf Rust", "Fungal spores spread in humid conditions.", "Propiconazole spray", "Improve airflow, remove infected leaves, avoid overhead irrigation.");
        }
        if (name.contains("blight")) {
            return new DiseaseResult("Early Blight", "Alternaria fungus affecting older leaves.", "Mancozeb or copper fungicide", "Rotate crops, mulch soil, keep leaves dry.");
        }
        if (name.contains("mildew")) {
            return new DiseaseResult("Powdery Mildew", "White fungal growth caused by poor ventilation.", "Sulfur-based fungicide", "Use resistant varieties and prune crowded foliage.");
        }
        return new DiseaseResult("Healthy / No Severe Disease", "No strong disease pattern was detected in demo mode.", "No pesticide required", "Continue monitoring moisture, temperature, and leaf color.");
    }

    private DiseaseResult visualFallbackPrediction(File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                return null;
            }

            int stepX = Math.max(1, image.getWidth() / 160);
            int stepY = Math.max(1, image.getHeight() / 160);
            LeafStats stats = collectLeafStats(image);
            if (stats.coloredLeafPixels < 40) {
                return null;
            }

            double damageRatio = stats.damagedPixels / (double) stats.coloredLeafPixels;
            double yellowRatio = stats.yellowPixels / (double) stats.coloredLeafPixels;
            double darkRatio = stats.darkPixels / (double) stats.coloredLeafPixels;
            double spotRatio = stats.spotPixels / (double) stats.coloredLeafPixels;
            double paleRatio = stats.palePixels / (double) stats.coloredLeafPixels;

            if (spotRatio > 0.025 && darkRatio < 0.18) {
                    return new DiseaseResult("Suspected Tomato Septoria Leaf Spot",
                        "Small circular brown spots with yellowing are commonly linked to Septoria leaf spot, especially when moisture splashes spores onto lower leaves.",
                        "Mancozeb or copper fungicide as locally recommended",
                        "Remove infected lower leaves, mulch soil to reduce splash, rotate crops, and keep foliage dry.",
                        "Java visual fallback result. Use TensorFlow for final disease classification.");
            }
            if (darkRatio > 0.18 || (darkRatio > 0.09 && yellowRatio > 0.10)) {
                return new DiseaseResult("Suspected Tomato Late Blight",
                        "Large dark necrotic patches with yellowing suggest a fast-spreading fungal infection favored by wet leaves, high humidity, and poor airflow.",
                        "Copper oxychloride or mancozeb as advised by local agriculture guidance",
                        "Remove infected parts, avoid wet leaves, improve airflow, and monitor nearby plants.",
                        "Java visual fallback result. Use TensorFlow for final disease classification.");
            }
            if (paleRatio > 0.20 && yellowRatio > 0.10 && darkRatio < 0.10) {
                return new DiseaseResult("Suspected Tomato Leaf Mold",
                        "Yellow-green blotches without heavy black tissue can indicate leaf mold, which is favored by high humidity and poor ventilation.",
                        "Copper-based fungicide or chlorothalonil as locally recommended",
                        "Increase plant spacing, prune dense foliage, ventilate protected crops, and avoid wet leaves.",
                        "Java visual fallback result. Use TensorFlow for final disease classification.");
            }
            if (damageRatio > 0.13) {
                return new DiseaseResult("Suspected Leaf Disease",
                        "Visible brown spots and yellowing indicate damaged leaf tissue that may be caused by fungal or bacterial infection in humid conditions.",
                        "Use a copper-based fungicide only after confirming the disease locally",
                        "Isolate the affected plant, remove damaged leaves, and keep foliage dry.",
                        "Java visual fallback result. Use TensorFlow for final disease classification.");
            }
        } catch (Exception ignored) {
            return null;
        }
        return null;
    }

    private LeafStats collectLeafStats(BufferedImage image) {
        LeafStats stats = new LeafStats();
        int stepX = Math.max(1, image.getWidth() / 180);
        int stepY = Math.max(1, image.getHeight() / 180);
            for (int y = 0; y < image.getHeight(); y += stepY) {
                for (int x = 0; x < image.getWidth(); x += stepX) {
                    int rgb = image.getRGB(x, y);
                    int r = (rgb >> 16) & 0xff;
                    int g = (rgb >> 8) & 0xff;
                    int b = rgb & 0xff;
                    int max = Math.max(r, Math.max(g, b));
                    int min = Math.min(r, Math.min(g, b));
                    int brightness = (r + g + b) / 3;
                    int saturation = max - min;

                    boolean background = saturation < 18 || brightness > 235;
                    boolean greenLeaf = !background && g > r * 0.72 && g > b * 0.72 && brightness > 35;
                    boolean necrotic = !background && brightness < 115 && r >= g * 0.75 && g >= b * 0.75;
                    boolean yellowing = !background && r > 90 && g > 80 && b < 115 && Math.abs(r - g) < 95;
                    boolean brownSpot = !background && r > 45 && g > 32 && b < 90 && r >= g * 0.85 && brightness < 155;
                    boolean smallSpot = brownSpot && brightness > 55 && brightness < 145 && saturation > 25;
                    boolean palePatch = !background && g >= r * 0.80 && r > b * 1.05 && brightness > 120 && brightness < 210;

                    if (greenLeaf || yellowing || necrotic || brownSpot) {
                        stats.coloredLeafPixels++;
                    }
                    if (necrotic || yellowing || brownSpot) {
                        stats.damagedPixels++;
                    }
                    if (yellowing) {
                        stats.yellowPixels++;
                    }
                    if (necrotic || brownSpot) {
                        stats.darkPixels++;
                    }
                    if (smallSpot) {
                        stats.spotPixels++;
                    }
                    if (palePatch) {
                        stats.palePixels++;
                    }
                }
            }
        return stats;
    }

    private static class LeafStats {
        int coloredLeafPixels;
        int damagedPixels;
        int yellowPixels;
        int darkPixels;
        int spotPixels;
        int palePixels;
    }

    private String format(DiseaseResult r) {
        String text = "Disease Name: " + r.diseaseName + "\n\n"
                + "Cause: " + r.cause + "\n\n"
                + "Suggested Pesticide: " + r.pesticide + "\n\n"
                + "Prevention Tips: " + r.prevention;
        if (r.engineStatus != null && !r.engineStatus.isBlank()) {
            text += "\n\nAI Engine Status: " + r.engineStatus;
        }
        return text;
    }

    private DiseaseResult withEngineStatus(DiseaseResult result, String status) {
        return new DiseaseResult(result.diseaseName, result.cause, result.pesticide, result.prevention, status);
    }

    private boolean shouldAlert(DiseaseResult result) {
        String name = result.diseaseName.toLowerCase(Locale.ROOT);
        return !name.contains("healthy") && !name.contains("uncertain");
    }
}
