package spectrum.analyzer.software;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SpectrumCanvas extends Canvas {
    private final SignalProcessor signalProcessor;
    private String displayMode = "Spectrum";
    private double[][] persistenceBuffer;
    private int persistenceIndex = 0;
    private double channelBandwidth = 1000;
    private int channelCount = 1;
    private double dynamicRange = 160;
    private boolean logScale = false;
    private double[] lastAmplitudes;

    public SpectrumCanvas(double width, double height, SignalProcessor processor) {
        super(width, height);
        this.signalProcessor = processor;
        this.persistenceBuffer = new double[400][16384];
        this.lastAmplitudes = new double[16384];
    }

    public void setDisplayMode(String mode) {
        this.displayMode = mode;
    }

    public void setChannelBandwidth(double bandwidth) {
        this.channelBandwidth = bandwidth;
    }

    public void setChannelCount(int count) {
        this.channelCount = Math.max(1, Math.min(count, 5));
    }

    public void setDynamicRange(double range) {
        this.dynamicRange = range;
    }

    public void setLogScale(boolean log) {
        this.logScale = log;
    }

    public double getChannelPower() {
        SpectrumData data = signalProcessor.getSpectrumData();
        double[] amplitudes = data.getAmplitudes();
        double centerFreq = (data.getMinFreq() + data.getMaxFreq()) / 2;
        double freqStep = (data.getMaxFreq() - data.getMinFreq()) / amplitudes.length;
        double power = 0;
        int count = 0;
        for (int i = 0; i < amplitudes.length; i++) {
            double freq = data.getMinFreq() + i * freqStep;
            if (Math.abs(freq - centerFreq) <= channelBandwidth / 2) {
                power += Math.pow(10, amplitudes[i] / 10);
                count++;
            }
        }
        return count > 0 ? 10 * Math.log10(power / count) : -dynamicRange;
    }

    public double getACPR() {
        SpectrumData data = signalProcessor.getSpectrumData();
        double[] amplitudes = data.getAmplitudes();
        double centerFreq = (data.getMinFreq() + data.getMaxFreq()) / 2;
        double freqStep = (data.getMaxFreq() - data.getMinFreq()) / amplitudes.length;
        double mainPower = 0, adjacentPower = 0;
        int mainCount = 0, adjCount = 0;

        for (int i = 0; i < amplitudes.length; i++) {
            double freq = data.getMinFreq() + i * freqStep;
            double distance = Math.abs(freq - centerFreq);
            if (distance <= channelBandwidth / 2) {
                mainPower += Math.pow(10, amplitudes[i] / 10);
                mainCount++;
            } else if (distance <= 3 * channelBandwidth / 2) {
                adjacentPower += Math.pow(10, amplitudes[i] / 10);
                adjCount++;
            }
        }
        double mainPowerDbm = mainCount > 0 ? 10 * Math.log10(mainPower / mainCount) : -dynamicRange;
        double adjPowerDbm = adjCount > 0 ? 10 * Math.log10(adjacentPower / adjCount) : -dynamicRange;
        return mainPowerDbm - adjPowerDbm;
    }

    public void update() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());

        SpectrumData data = signalProcessor.getSpectrumData();
        double[] amplitudes = data.getAmplitudes();

        if (displayMode.equals("Spectrum")) {
            drawSpectrum(gc, amplitudes, data);
        } else if (displayMode.equals("Persistence")) {
            drawPersistence(gc, amplitudes);
        }

        System.arraycopy(amplitudes, 0, lastAmplitudes, 0, amplitudes.length);
    }

    private void drawSpectrum(GraphicsContext gc, double[] amplitudes, SpectrumData data) {
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(1.0);
        double width = getWidth();
        double height = getHeight();
        double xScale = width / amplitudes.length;
        double yScale = height / dynamicRange;

        gc.beginPath();
        for (int i = 0; i < amplitudes.length; i++) {
            double x = logScale ? (Math.log1p(i / (double) amplitudes.length) / Math.log1p(1)) * width : i * xScale;
            double y = height - (amplitudes[i] + dynamicRange) * yScale;
            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                gc.lineTo(x, y);
            }
        }
        gc.stroke();

        // Draw channels
        double centerFreq = (data.getMinFreq() + data.getMaxFreq()) / 2;
        double freqStep = (data.getMaxFreq() - data.getMinFreq()) / amplitudes.length;
        gc.setStroke(Color.RED);
        for (int c = 0; c < channelCount; c++) {
            double offset = c - (channelCount - 1) / 2.0;
            double chanCenter = centerFreq + offset * channelBandwidth;
            double x1 = ((chanCenter - channelBandwidth / 2) - data.getMinFreq()) / freqStep * xScale;
            double x2 = ((chanCenter + channelBandwidth / 2) - data.getMinFreq()) / freqStep * xScale;
            if (logScale) {
                x1 = (Math.log1p(x1 / width) / Math.log1p(1)) * width;
                x2 = (Math.log1p(x2 / width) / Math.log1p(1)) * width;
            }
            gc.strokeLine(x1, 0, x1, height);
            gc.strokeLine(x2, 0, x2, height);
        }

        // Draw grid and labels
        gc.setStroke(Color.DARKGRAY);
        gc.setFill(Color.YELLOW);
        for (int i = 0; i <= 10; i++) {
            double y = i * height / 10;
            gc.strokeLine(0, y, width, y);
            gc.fillText(String.format("%d dBm", (int) (-dynamicRange + i * dynamicRange / 10)), 5, y - 5);
        }
        double freqStepDisplay = (data.getMaxFreq() - data.getMinFreq()) / 10;
        for (int i = 0; i <= 10; i++) {
            double x = i * width / 10;
            gc.strokeLine(x, 0, x, height);
            double freq = data.getMinFreq() + i * freqStepDisplay;
            gc.fillText(String.format("%.2f Hz", freq), x + 5, height - 5);
        }
    }

    private void drawPersistence(GraphicsContext gc, double[] amplitudes) {
        for (int x = 0; x < amplitudes.length; x++) {
            double value = (amplitudes[x] + dynamicRange) / dynamicRange;
            persistenceBuffer[persistenceIndex % 400][x] = Math.max(persistenceBuffer[persistenceIndex % 400][x] * 0.95, value);
            gc.setFill(Color.rgb(0, (int) (persistenceBuffer[persistenceIndex % 400][x] * 255), 0));
            gc.fillRect(x * getWidth() / amplitudes.length, 0, 1, getHeight());
        }
        persistenceIndex = (persistenceIndex + 1) % 400;
    }
}