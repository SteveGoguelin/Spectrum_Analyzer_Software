## Spektrumanalysator-Software (Spectrum Analyzer Software)



### Application Initialization
- **Purpose**: Starting the application.
- **Logic**:
  - Loads the `spectrum_analyzer.fxml` file to define the UI layout.
  - Applies the `style.css` stylesheet for visual styling (black background, yellow text, etc.).
  - Creates a window (`Stage`) titled "Spectrum Analyzer" with a resolution of 1200x800 pixels.
  - Starts the application using `launch(args)` in the `main` method.
### User Interface and Control
- **Purpose**: Manages the UI components and user interactions, coordinating updates to the visualization and signal processing.
- **Key UI Elements**:
  - **Sliders**: Adjust max frequency (20 Hz to 50 GHz), dynamic range (100–200 dB), and sweep speed (0.1–10).
  - **ComboBoxes**: Select display mode (Spectrum/Persistence), analysis mode (FFT/Swept-Tuned), window function (Hanning, Blackman-Harris, etc.), and demodulation type (None, AM, FM, etc.).
  - **TextFields**: Set channel bandwidth and number of channels (1–5).
  - **ToggleButton**: Enable/disable logarithmic frequency scale.
  - **Button**: Export power report to a CSV file.
  - **Labels**: Display metrics like channel power, ACPR (Adjacent Channel Power Ratio), signal type, and window function info.
  - **Canvas Containers**: Host the spectrum and demodulation visualizations.
- **Initialization Logic**:
  - Creates a `SignalProcessor` to handle signal data generation.
  - Initializes `SpectrumCanvas` (1160x400) and `DemodCanvas` (1160x150) for visualizations.
  - Sets up listeners for UI controls to update `SignalProcessor` and `SpectrumCanvas` parameters (e.g., frequency range, dynamic range, sweep speed).
  - Configures an `AnimationTimer` to continuously update the canvases and metrics.
- **Key Functions**:
  - **`updateMetrics()`**: Updates labels for channel power, ACPR, and signal type based on `SpectrumCanvas` calculations and `SignalProcessor` classification.
  - **`updateWindowInfo()`**: Displays window function characteristics (e.g., sidelobe levels, resolution) based on the selected window.
  - **`exportPowerReport()`**: Writes channel power and ACPR to a CSV file (`power_report.csv`).
### 3. Signal Processing
- **Purpose**: Generates simulated signal data, applies window functions, demodulation, and analysis modes.
- **Key Parameters**:
  - FFT size: 16,384 points.
  - Frequency range: Min (default 20 Hz) to max (default 1 kHz, adjustable).
  - Analysis mode: FFT or Swept-Tuned.
  - Window function: Hanning, Blackman-Harris, Kaiser, Flat-Top, or Gaussian.
  - Demodulation type: None, AM, FM, PM, QAM, PSK, or OFDM.
  - Sweep speed: Affects amplitude attenuation in Swept-Tuned mode.
- **Logic**:
  - **Data Generation**:
    - Uses an `ExecutorService` to run data generation asynchronously.
    - Generates a noise floor based on the max frequency (`-160 - 20 * log10(maxFreq / 1000)` dB).
    - Simulates signals for specific frequency bands:
      - Wi-Fi (100–200 Hz, +100 dB Gaussian peak at 150 Hz).
      - Bluetooth (2400–2480 MHz, +80 dB peak at 2440 MHz).
      - LTE (700–2700 MHz, +90 dB peak at 1800 MHz).
      - 5G NR (3500–3700 MHz, +85 dB peak at 3600 MHz).
      - Zigbee (2400–2450 MHz, +75 dB peak at 2425 MHz).
    - Adds Gaussian noise (±10 dB) to the noise floor.
    - Generates random phases (0 to 2π) for each frequency bin.
  - **Window Application**:
    - Applies the selected window function to the amplitude data to reduce spectral leakage.
    - Supports Hanning, Blackman-Harris, Kaiser, Flat-Top, and Gaussian windows, each with specific coefficients or parameters (e.g., Kaiser uses a Bessel function with β=8.6).
  - **Demodulation**:
    - If a demodulation type is selected, applies the `Demodulator` to modify amplitudes based on phases (see Demodulator section).
  - **Analysis Mode**:
    - In Swept-Tuned mode, attenuates amplitudes based on sweep speed (`0.8 / sweepSpeed`).
  - **Data Storage**:
    - Updates a `SpectrumData` object with amplitudes, phases, and frequency range.
### Spectrum Visualizatio
- **Purpose**: Renders the signal spectrum or persistence display on a canvas.
- **Key Features**:
  - Display modes: Spectrum (real-time plot) or Persistence (intensity-based history).
  - Adjustable dynamic range (default 160 dB).
  - Logarithmic or linear frequency scale.
  - Channel markers for bandwidth and count.
  - Grid and labels for frequency and amplitude.
- **Logic**:
  - **Spectrum Mode**:
    - Clears the canvas with a black background.
    - Plots amplitudes as a yellow line, scaled to canvas dimensions.
    - Supports logarithmic frequency scaling if enabled.
    - Draws red markers for channel boundaries based on center frequency, bandwidth, and channel count.
    - Adds a grid (10x10) with frequency (Hz) and amplitude (dBm) labels.
  - **Persistence Mode**:
    - Maintains a 400-frame buffer of amplitude histories.
    - Fades older data (0.95 decay) and updates with new amplitudes.
    - Colors pixels green based on intensity (0–255).
  - **Metrics**:
    - **`getChannelPower()`**: Calculates average power (dBm) within the channel bandwidth around the center frequency.
    - **`getACPR()`**: Computes the power ratio (dB) between the main channel and adjacent channels (up to 3x bandwidth).
### Demodulation Graphics
- **Purpose**: Displays the demodulated signal waveform.
- **Logic**:
  - Clears the canvas with a black background.
  - Retrieves amplitude data from `SignalProcessor` (post-demodulation).
  - Plots the waveform as a yellow line, centered vertically, with amplitude scaled to ±400 units.
  - Draws a dark gray centerline for reference.
  - Updates in sync with the `AnimationTimer`.
### Demodulation 
- **Purpose**: Applies demodulation to the signal based on the selected type.
- **Supported Types**:
  - **AM**: Multiplies amplitude by `cos(phase)`.
  - **FM**: Computes phase differences (`(phase[i] - phase[i-1]) / 2π`).
  - **PM**: Normalizes phases (`phase / 2π`).
  - **QAM**: Combines in-phase (`I = amplitude * cos(phase)`) and quadrature (`Q = amplitude * sin(phase)`) components as `sqrt(I² + Q²)`.
  - **PSK**: Quantizes phases to nearest multiples of `π/4`.
  - **OFDM**: Applies AM with a random phase offset (±π/8).
- **Logic**:
  - Copies input amplitudes to a result array.
  - Modifies the result based on the demodulation type using amplitude and phase data.
  - Returns the demodulated amplitudes.
### Signal Classification
- **Purpose**: Identifies the signal type based on spectral characteristics.
- **Logic**:
  - Analyzes the `SpectrumData` amplitudes to find the peak amplitude and corresponding frequency.
  - Counts peaks with the same amplitude to adjust confidence (fewer peaks → 90%, more → 70%).
  - Classifies based on frequency and amplitude thresholds:
    - **Bluetooth**: 2400–2480 MHz, >-100 dBm.
    - **Wi-Fi**: 100–200 Hz, >-80 dBm.
    - **LTE**: 700–2700 MHz, >-90 dBm.
    - **5G NR**: 3500–3700 MHz, >-95 dBm.
    - **Zigbee**: 2400–2450 MHz, >-105 dBm.
    - **Unknown**: Default, 50% confidence.
  - Returns a string with the signal type and confidence percentage.
### Data Storage 
- **Purpose**: Stores and provides access to spectral data.
- **Logic**:
  - Holds arrays for amplitudes and phases (size 16,384).
  - Stores min and max frequencies.
  - Provides getters for data access and an `update` method to refresh data
### UI Layout 
- **Purpose**: Defines the UI structure using JavaFX components.
- **Structure**:
  - A `BorderPane` with a top `Label` ("Spectrum Analyzer").
  - A center `SplitPane` containing:
    - **Left Panel**: Control panel with sliders, ComboBoxes, TextFields, ToggleButton, and export Button, organized in `TitledPane` sections (Frequency, Analysis Mode, etc.).
    - **Right Panel**: Canvas containers for spectrum and demodulation visualizations, plus metric labels (channel power, ACPR, signal type, window info).
  - Styled via `style.css` for a dark theme with yellow accents.
### Styling 
- **Purpose**: Defines the visual appearance of the UI.
- **Key Styles**:
  - Black background for most components.
  - Yellow text and accents for labels, buttons, and sliders.
  - Dark gray for tracks, borders, and backgrounds of interactive elements.
  - Courier New font, 12px size.
  - Custom styling for sliders, ComboBoxes, ToggleButtons, and scrollbars.
