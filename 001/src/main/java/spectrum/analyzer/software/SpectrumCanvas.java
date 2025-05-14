package spectrum.analyzer.software;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SpectrumCanvas extends Canvas {
    private final SignalProcessor signalProcessor;
    private String displayMode = "Spectrum";
    private double[][] waterfallBuffer;
    private int waterfallIndex = 0;

    public SpectrumCanvas(double width, double height, SignalProcessor processor) {
        super(width, height);
        this.signalProcessor = processor;
        this.waterfallBuffer = new double[400][8192];
    }

    public void setDisplayMode(String mode) {
        this.displayMode = mode;
    }
    public void update() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());
        SpectrumData data = signalProcessor.getSpectrumData();
        double[] amplitudes = data.getAmplitudes();
        if (displayMode.equals("Spectrum")) {
            drawSpectrum(gc, amplitudes, data);
        } else if (displayMode.equals("Waterfall")) {
            drawWaterfall(gc, amplitudes);
        } else if (displayMode.equals("Persistence")) {
            drawPersistence(gc, amplitudes);
        }
    }

    private void drawSpectrum(GraphicsContext gc, double[] amplitudes, SpectrumData data) {
        gc.setStroke(Color.LIME);
        gc.setLineWidth(1.0);
        double width = getWidth();
        double height = getHeight();
        double xScale = width / amplitudes.length;
        double yScale = height / 160.0;
        gc.beginPath();
        for (int i = 0; i < amplitudes.length; i++) {
            double x = i * xScale;
            double y = height - (amplitudes[i] + 160) * yScale;
            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                gc.lineTo(x, y);
            }
        }
        gc.stroke();
        gc.setStroke(Color.DARKGRAY);
        for (int i = 0; i <= 10; i++) {
            double y = i * height / 10;
            gc.strokeLine(0, y, width, y);
            gc.fillText(String.format("%d dBm", -160 + i * 16), 5, y - 5);
        }
        double freqStep = (data.getMaxFreq() - data.getMinFreq()) / 10;
        for (int i = 0; i <= 10; i++) {
            double x = i * width / 10;
            gc.strokeLine(x, 0, x, height);
            gc.fillText(String.format("%.2f Hz", data.getMinFreq() + i * freqStep), x + 5, height - 5);
        }
    }

    private void drawWaterfall(GraphicsContext gc, double[] amplitudes) {
        System.arraycopy(amplitudes, 0, waterfallBuffer[waterfallIndex], 0, amplitudes.length);
        waterfallIndex = (waterfallIndex + 1) % (int) getHeight();
        for (int y = 0; y < getHeight(); y++) {
            int bufferY = (waterfallIndex - y + (int) getHeight()) % (int) getHeight();
            for (int x = 0; x < amplitudes.length; x++) {
                double value = (waterfallBuffer[bufferY][x] + 160) / 160.0;
                gc.setFill(Color.rgb(0, (int) (value * 255), 0));
                gc.fillRect(x * getWidth() / amplitudes.length, y, 1, 1);
            }
        }
    }

    private void drawPersistence(GraphicsContext gc, double[] amplitudes) {
        for (int i = 0; i < amplitudes.length; i++) {
            double value = (amplitudes[i] + 160) / 160.0;
            gc.setFill(Color.rgb(0, (int) (value * 255), 0));
            gc.fillRect(i * getWidth() / amplitudes.length, 0, 1, getHeight());
        }
    }
}