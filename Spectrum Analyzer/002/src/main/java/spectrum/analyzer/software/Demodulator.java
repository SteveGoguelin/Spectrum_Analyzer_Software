package spectrum.analyzer.software;

public class Demodulator {

    public double[] demodulate(double[] amplitudes, double[] phases, String type) {
        double[] result = new double[amplitudes.length];
        System.arraycopy(amplitudes, 0, result, 0, amplitudes.length);

        if (type.equals("AM")) {
            for (int i = 0; i < result.length; i++) {
                result[i] = Math.abs(result[i]) * Math.cos(phases[i]);
            }
        } else if (type.equals("FM")) {
            for (int i = 1; i < result.length; i++) {
                result[i] = (phases[i] - phases[i - 1]) / (2 * Math.PI);
            }
            result[0] = result[1];
        } else if (type.equals("PM")) {
            for (int i = 0; i < result.length; i++) {
                result[i] = phases[i] / (2 * Math.PI);
            }
        } else if (type.equals("QAM")) {
            for (int i = 0; i < result.length; i++) {
                double I = result[i] * Math.cos(phases[i]);
                double Q = result[i] * Math.sin(phases[i]);
                result[i] = Math.sqrt(I * I + Q * Q);
            }
        } else if (type.equals("PSK")) {
            for (int i = 0; i < result.length; i++) {
                result[i] = Math.round(phases[i] / (Math.PI / 4)) * (Math.PI / 4);
            }
        } else if (type.equals("OFDM")) {
            for (int i = 0; i < result.length; i++) {
                result[i] = Math.abs(result[i]) * Math.cos(phases[i] + randomPhaseOffset());
            }
        }

        return result;
    }

    private double randomPhaseOffset() {
        return Math.random() * Math.PI / 8;
    }
}