module com.example.spectrumanalyzer {
    requires javafx.controls;
    requires javafx.fxml;


    opens spectrum.analyzer.software to javafx.fxml;
    exports spectrum.analyzer.software;
}