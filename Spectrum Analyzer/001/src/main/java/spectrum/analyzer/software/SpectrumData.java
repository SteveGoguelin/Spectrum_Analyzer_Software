package spectrum.analyzer.software;

public class SpectrumData {
    private double[] amplitudes;
    private double[] phases;
    private double minFreq;
    private double maxFreq;
    private final int size;

    public SpectrumData(int size) {
        this.size = size;
        this.amplitudes = new double[size];
        this.phases = new double[size];
    }

    public void update(double[] amplitudes, double[] phases, double minFreq, double maxFreq) {
        this.amplitudes = amplitudes;
        this.phases = phases;
        this.minFreq = minFreq;
        this.maxFreq = maxFreq;
    }

    public double[] getAmplitudes() {
        return amplitudes;
    }

    public double[] getPhases() {
        return phases;
    }

    public double getMinFreq() {
        return minFreq;
    }

    public double getMaxFreq() {
        return maxFreq;
    }

    public int getSize() {
        return size;
    }
}