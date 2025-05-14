package spectrum.analyzer.software;

public class SignalClassifier {

    public String classify(SpectrumData data) {
        double[] amplitudes = data.getAmplitudes();
        double minFreq = data.getMinFreq();
        double maxFreq = data.getMaxFreq();
        double freqStep = (maxFreq - minFreq) / amplitudes.length;
        double maxAmplitude = -160;
        double peakFreq = minFreq;
        int peakCount = 0;
        for (int i = 0; i < amplitudes.length; i++) {
            if (amplitudes[i] > maxAmplitude) {
                maxAmplitude = amplitudes[i];
                peakFreq = minFreq + i * freqStep;
                peakCount = 1;
            } else if (amplitudes[i] == maxAmplitude) {
                peakCount++;
            }
        }
        double confidence = peakCount < 5 ? 0.9 : 0.7;

        if (peakFreq >= 2400e6 && peakFreq <= 2480e6 && maxAmplitude > -100) {
            return String.format("Bluetooth (%.0f%%)", confidence * 100);
        } else if (peakFreq >= 100 && peakFreq <= 200 && maxAmplitude > -80) {
            return String.format("Wi-Fi (%.0f%%)", confidence * 100);
        } else if (peakFreq >= 700e6 && peakFreq <= 2700e6 && maxAmplitude > -90) {
            return String.format("LTE (%.0f%%)", confidence * 100);
        } else if (peakFreq >= 3500e6 && peakFreq <= 3700e6 && maxAmplitude > -95) {
            return String.format("5G NR (%.0f%%)", confidence * 100);
        } else if (peakFreq >= 2400e6 && peakFreq <= 2450e6 && maxAmplitude > -105) {
            return String.format("Zigbee (%.0f%%)", confidence * 100);
        }
        return String.format("Unknown (%.0f%%)", 50.0);
    }
}