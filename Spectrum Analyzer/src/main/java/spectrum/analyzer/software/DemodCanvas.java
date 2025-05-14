package spectrum.analyzer.software;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

            public class DemodCanvas extends Canvas {
                private final SignalProcessor signalProcessor;

                public DemodCanvas(double width, double height, SignalProcessor processor) {
                    super(width, height);
                    this.signalProcessor = processor;
                }

                public void update() {
                    GraphicsContext gc = getGraphicsContext2D();
                    gc.setFill(Color.BLACK);
                    gc.fillRect(0, 0, getWidth(), getHeight());

                    SpectrumData data = signalProcessor.getSpectrumData();
                    double[] amplitudes = data.getAmplitudes();
                    gc.setStroke(Color.YELLOW);
                    gc.setLineWidth(1.0);
                    double width = getWidth();
                    double height = getHeight();
                    double xScale = width / amplitudes.length;
                    double yScale = height / 400.0;
                    gc.beginPath();
                    for (int i = 0; i < amplitudes.length; i++) {
                        double x = i * xScale;
                        double y = height / 2 - amplitudes[i] * yScale;
                        if (i == 0) {
                            gc.moveTo(x, y);
                        } else {
                            gc.lineTo(x, y);
                        }
                    }
                    gc.stroke();
                    gc.setStroke(Color.DARKGRAY);
                    gc.setLineWidth(0.5);
                    gc.strokeLine(0, height / 2, width, height / 2);
                }
            }