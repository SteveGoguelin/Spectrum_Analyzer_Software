package spectrum.analyzer.software;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class SpectrumAnalyzerController {

    @FXML private VBox canvasContainer;
    @FXML private Slider frequencySlider;
    @FXML private ComboBox<String> modeComboBox;
    @FXML private Label frequencyLabel;

    private SpectrumCanvas spectrumCanvas;
    private SignalProcessor signalProcessor;
    private double maxFrequency = 50_000_000_000.0;
    @FXML
    public void initialize() {
        signalProcessor = new SignalProcessor();
        spectrumCanvas = new SpectrumCanvas(760, 400, signalProcessor);
        canvasContainer.getChildren().add(spectrumCanvas);

        frequencySlider.setMin(20);
        frequencySlider.setMax(maxFrequency);
        frequencySlider.setValue(1000);
        frequencySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            signalProcessor.setFrequencyRange(20, newVal.doubleValue());
            frequencyLabel.setText(String.format("Max Freq: %.2f Hz", newVal.doubleValue()));
        });

        modeComboBox.getItems().addAll("Spectrum", "Waterfall", "Persistence");
        modeComboBox.setValue("Spectrum");
        modeComboBox.setOnAction(e -> spectrumCanvas.setDisplayMode(modeComboBox.getValue()));

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                spectrumCanvas.update();
            }
        };
        timer.start();
    }
}