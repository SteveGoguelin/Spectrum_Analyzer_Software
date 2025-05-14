package spectrum.analyzer.software;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignalProcessor {
    private SpectrumData spectrumData;
    private double minFreq = 20.0;
    private double maxFreq = 1000.0;
    private final int fftSize = 16384;
    private final Random random = new Random();
    private String analysisMode = "FFT";
    private String windowFunction = "Hanning";
    private String demodulationType = "None";
    private double sweepSpeed = 1.0;
    private final SignalClassifier classifier;
    private final Demodulator demodulator;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SignalProcessor() {
        spectrumData = new SpectrumData(fftSize);
        classifier = new SignalClassifier();
        demodulator = new Demodulator();
        generateSimulatedData();
    }

    public void setFrequencyRange(double min, double max) {
        this.minFreq = min;
        this.maxFreq = max;
        generateSimulatedData();
    }

    public void setAnalysisMode(String mode) {
        this.analysisMode = mode;
        generateSimulatedData();
    }

    public void setWindowFunction(String window) {
        this.windowFunction = window;
        generateSimulatedData();
    }

    public void setDemodulationType(String type) {
        this.demodulationType = type;
        generateSimulatedData();
    }

    public void setSweepSpeed(double speed) {
        this.sweepSpeed = speed;
        generateSimulatedData();
    }

    public SpectrumData getSpectrumData() {
        generateSimulatedData();
        return spectrumData;
    }

    public String getSignalType() {
        return classifier.classify(spectrumData);
    }

    private void generateSimulatedData() {
        executor.submit(() -> {
            double[] amplitudes = new double[fftSize];
            double[] phases = new double[fftSize];
            double freqStep = (maxFreq - minFreq) / fftSize;
            double[] window = generateWindow();
            double noiseFloor = -160 - 20 * Math.log10(maxFreq / 1000);

            for (int i = 0; i < fftSize; i++) {
                double freq = minFreq + i * freqStep;
                amplitudes[i] = noiseFloor + random.nextGaussian() * 10;
                if (freq > 100 && freq < 200) { // Wi-Fi
                    amplitudes[i] += 100 * Math.exp(-Math.pow((freq - 150) / 10, 2));
                } else if (freq > 2400e6 && freq < 2480e6) { // Bluetooth
                    amplitudes[i] += 80 * Math.exp(-Math.pow((freq - 2440e6) / 5e6, 2));
                } else if (freq > 700e6 && freq < 2700e6) { // LTE
                    amplitudes[i] += 90 * Math.exp(-Math.pow((freq - 1800e6) / 50e6, 2));
                } else if (freq > 3500e6 && freq < 3700e6) { // 5G NR
                    amplitudes[i] += 85 * Math.exp(-Math.pow((freq - 3600e6) / 20e6, 2));
                } else if (freq > 2400e6 && freq < 2450e6) { // Zigbee
                    amplitudes[i] += 75 * Math.exp(-Math.pow((freq - 2425e6) / 5e6, 2));
                }
                amplitudes[i] *= window[i];
                phases[i] = random.nextDouble() * 2 * Math.PI;
            }

            if (!demodulationType.equals("None")) {
                double[] demodulated = demodulator.demodulate(amplitudes, phases, demodulationType);
                System.arraycopy(demodulated, 0, amplitudes, 0, fftSize);
            }

            if (analysisMode.equals("Swept-Tuned")) {
                double attenuation = 0.8 / sweepSpeed;
                for (int i = 0; i < fftSize; i++) {
                    amplitudes[i] *= attenuation;
                }
            }

            spectrumData.update(amplitudes, phases, minFreq, maxFreq);
        });
    }

    private double[] generateWindow() {
        double[] window = new double[fftSize];
        for (int i = 0; i < fftSize; i++) {
            if (windowFunction.equals("Hanning")) {
                window[i] = 0.5 * (1 - Math.cos(2 * Math.PI * i / (fftSize - 1)));
            } else if (windowFunction.equals("Blackman-Harris")) {
                double a0 = 0.35875, a1 = 0.48829, a2 = 0.14128, a3 = 0.01168;
                window[i] = a0 - a1 * Math.cos(2 * Math.PI * i / (fftSize - 1)) +
                        a2 * Math.cos(4 * Math.PI * i / (fftSize - 1)) -
                        a3 * Math.cos(6 * Math.PI * i / (fftSize - 1));
            } else if (windowFunction.equals("Kaiser")) {
                double beta = 8.6;
                window[i] = besselI0(beta * Math.sqrt(1 - Math.pow(2.0 * i / (fftSize - 1) - 1, 2))) / besselI0(beta);
            } else if (windowFunction.equals("Flat-Top")) {
                double a0 = 0.21557895, a1 = 0.41663158, a2 = 0.277263158, a3 = 0.083578947, a4 = 0.006947368;
                window[i] = a0 - a1 * Math.cos(2 * Math.PI * i / (fftSize - 1)) +
                        a2 * Math.cos(4 * Math.PI * i / (fftSize - 1)) -
                        a3 * Math.cos(6 * Math.PI * i / (fftSize - 1)) +
                        a4 * Math.cos(8 * Math.PI * i / (fftSize - 1));
            } else if (windowFunction.equals("Gaussian")) {
                double sigma = 0.4;
                window[i] = Math.exp(-0.5 * Math.pow((i - (fftSize - 1) / 2.0) / (sigma * (fftSize - 1) / 2.0), 2));
            }
        }
        return window;
    }

    private double besselI0(double x) {
        double sum = 1.0, term = 1.0;
        for (int k = 1; k < 20; k++) {
            term *= (x * x) / (4 * k * k);
            sum += term;
        }
        return sum;
    }
}