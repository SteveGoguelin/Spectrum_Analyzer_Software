package spectrum.analyzer.software;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.FileWriter;
import java.io.IOException;

public class SpectrumAnalyzerController {

    @FXML private VBox canvasContainer;
    @FXML private VBox demodCanvasContainer;
    @FXML private Slider frequencySlider;
    @FXML private Slider dynamicRangeSlider;
    @FXML private Slider sweepSpeedSlider;
    @FXML private ComboBox<String> modeComboBox;
    @FXML private ComboBox<String> analysisModeComboBox;
    @FXML private ComboBox<String> windowComboBox;
    @FXML private ComboBox<String> demodulationComboBox;
    @FXML private Label frequencyLabel;
    @FXML private Label dynamicRangeLabel;
    @FXML private Label channelPowerLabel;
    @FXML private Label acprLabel;
    @FXML private Label signalTypeLabel;
    @FXML private Label windowInfoLabel;
    @FXML private TextField channelBandwidthField;
    @FXML private TextField channelCountField;
    @FXML private ToggleButton logScaleToggle;
    @FXML private Button exportButton;
    private SpectrumCanvas spectrumCanvas;
    private DemodCanvas demodCanvas;
    private SignalProcessor signalProcessor;
    private double maxFrequency = 50_000_000_000.0;

    @FXML
    public void initialize() {
        signalProcessor = new SignalProcessor();
        spectrumCanvas = new SpectrumCanvas(1160, 400, signalProcessor);
        demodCanvas = new DemodCanvas(1160, 150, signalProcessor);
        canvasContainer.getChildren().add(spectrumCanvas);
        demodCanvasContainer.getChildren().add(demodCanvas);
        frequencySlider.setMin(20);
        frequencySlider.setMax(maxFrequency);
        frequencySlider.setValue(1000);
        frequencySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            signalProcessor.setFrequencyRange(20, newVal.doubleValue());
            frequencyLabel.setText(String.format("Max Freq: %.2f Hz", newVal.doubleValue()));
        });
        dynamicRangeSlider.setMin(100);
        dynamicRangeSlider.setMax(200);
        dynamicRangeSlider.setValue(160);
        dynamicRangeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            spectrumCanvas.setDynamicRange(newVal.doubleValue());
            dynamicRangeLabel.setText(String.format("Dynamic Range: %.0f dB", newVal.doubleValue()));
        });
        sweepSpeedSlider.setMin(0.1);
        sweepSpeedSlider.setMax(10.0);
        sweepSpeedSlider.setValue(1.0);
        sweepSpeedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            signalProcessor.setSweepSpeed(newVal.doubleValue());
        });
        modeComboBox.getItems().addAll("Spectrum", "Persistence");
        modeComboBox.setValue("Spectrum");
        modeComboBox.setOnAction(e -> spectrumCanvas.setDisplayMode(modeComboBox.getValue()));
        analysisModeComboBox.getItems().addAll("FFT", "Swept-Tuned");
        analysisModeComboBox.setValue("FFT");
        analysisModeComboBox.setOnAction(e -> signalProcessor.setAnalysisMode(analysisModeComboBox.getValue()));
        windowComboBox.getItems().addAll("Hanning", "Blackman-Harris", "Kaiser", "Flat-Top", "Gaussian");
        windowComboBox.setValue("Hanning");
        windowComboBox.setOnAction(e -> {
            signalProcessor.setWindowFunction(windowComboBox.getValue());
            updateWindowInfo();
        });
        demodulationComboBox.getItems().addAll("None", "AM", "FM", "PM", "QAM", "PSK", "OFDM");
        demodulationComboBox.setValue("None");
        demodulationComboBox.setOnAction(e -> signalProcessor.setDemodulationType(demodulationComboBox.getValue()));
        channelBandwidthField.setText("1000");
        channelBandwidthField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double bandwidth = Double.parseDouble(newVal);
                spectrumCanvas.setChannelBandwidth(bandwidth);
            } catch (NumberFormatException ignored) {}
        });
        channelCountField.setText("1");
        channelCountField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int count = Integer.parseInt(newVal);
                spectrumCanvas.setChannelCount(count);
            } catch (NumberFormatException ignored) {}
        });
        logScaleToggle.setOnAction(e -> spectrumCanvas.setLogScale(logScaleToggle.isSelected()));
        exportButton.setOnAction(e -> exportPowerReport());
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                spectrumCanvas.update();
                demodCanvas.update();
                updateMetrics();
            }
        };
        timer.start();
        updateWindowInfo();
    }

    private void updateMetrics() {
        double channelPower = spectrumCanvas.getChannelPower();
        double acpr = spectrumCanvas.getACPR();
        String signalType = signalProcessor.getSignalType();
        channelPowerLabel.setText(String.format("Channel Power: %.2f dBm", channelPower));
        acprLabel.setText(String.format("ACPR: %.2f dB", acpr));
        signalTypeLabel.setText("Signal Type: " + signalType);
    }

    private void updateWindowInfo() {
        String window = windowComboBox.getValue();
        String info = switch (window) {
            case "Hanning" -> "Sidelobe: -31 dB, Resolution: Moderate";
            case "Blackman-Harris" -> "Sidelobe: -92 dB, Resolution: Low";
            case "Kaiser" -> "Sidelobe: -70 dB, Resolution: Adjustable";
            case "Flat-Top" -> "Sidelobe: -90 dB, Resolution: Low";
            case "Gaussian" -> "Sidelobe: -60 dB, Resolution: High";
            default -> "Unknown";
        };
        windowInfoLabel.setText("Window Info: " + info);
    }

    private void exportPowerReport() {
        try (FileWriter writer = new FileWriter("power_report.csv")) {
            writer.write("Channel,Power (dBm),ACPR (dB)\n");
            double power = spectrumCanvas.getChannelPower();
            double acpr = spectrumCanvas.getACPR();
            writer.write(String.format("Main,%.2f,%.2f\n", power, acpr));
        } catch (IOException e) {
            System.err.println("Error exporting report: " + e.getMessage());
        }
    }
}