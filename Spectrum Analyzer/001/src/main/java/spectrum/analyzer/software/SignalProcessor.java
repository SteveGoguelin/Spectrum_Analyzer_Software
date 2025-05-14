package spectrum.analyzer.software;

import java.util.Random;

public class SignalProcessor {
    private SpectrumData spectrumData;
    private double minFreq = 20.0;
    private double maxFreq = 1000.0;
    private final int fftSize = 8192;
    private final Random random = new Random();

    public SignalProcessor() {
        spectrumData = new SpectrumData(fftSize);
        generateSimulatedData();
    }

    public void setFrequencyRange(double min, double max) {
        this.minFreq = min;
        this.maxFreq = max;
        generateSimulatedData();
    }

    public SpectrumData getSpectrumData() {
        generateSimulatedData();
        return spectrumData;
    }

    private void generateSimulatedData() {
        double[] amplitudes = new double[fftSize];
        double[] phases = new double[fftSize];
        double freqStep = (maxFreq - minFreq) / fftSize;
        for (int i = 0; i < fftSize; i++) {
            double freq = minFreq + i * freqStep;
            amplitudes[i] = -160 + random.nextGaussian() * 10;
            if (freq > 100 && freq < 200) {
                amplitudes[i] += 100 * Math.exp(-Math.pow((freq - 150) / 10, 2));
            }
            phases[i] = random.nextDouble() * 2 * Math.PI;
        }
        spectrumData.update(amplitudes, phases, minFreq, maxFreq);
    }
}