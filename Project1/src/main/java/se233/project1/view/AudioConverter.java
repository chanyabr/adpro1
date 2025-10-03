package se233.project1.view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import se233.project1.controller.Configr;
import se233.project1.controller.Conversion;
import se233.project1.controller.DropZone;
import se233.project1.model.ConversionConfig;

import java.io.File;

public class AudioConverter extends Application {

    private Stage primaryStage;
    private ConversionConfig config;
    private DropZone dropZoneController;
    private Configr configController;
    private Conversion conversionController;
    private BatchConverter batchConverter;

    private VBox mainContainer;
    private VBox dropZoneView;
    private VBox configurationPanel;
    private ListView<ConversionConfig.FileConversionSettings> fileList;
    private Button convertButton;
    private Button clearButton;
    private ProgressBar progressBar;
    private Label statusLabel;
    private TextArea logArea;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        initializeControllers();
        initializeUI();
        setupEventHandlers();

        Scene scene = new Scene(mainContainer, 950, 850);

        primaryStage.setTitle("Audio Converter - SE 233 Advanced Programming");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(800);
        primaryStage.setOnCloseRequest(e -> {
            conversionController.shutdown();
            Platform.exit();
        });
        primaryStage.show();
    }

    private void initializeControllers() {
        config = new ConversionConfig();
        dropZoneController = new DropZone(config);
        configController = new Configr(config);
        conversionController = new Conversion(config);
        batchConverter = new BatchConverter();

        setupControllerCallbacks();
    }

    private void setupControllerCallbacks() {
        dropZoneController.setOnFilesDropped(this::onFilesDropped);
        dropZoneController.setOnDragEntered(this::onDragEntered);
        dropZoneController.setOnDragExited(this::onDragExited);

        conversionController.setLogCallback(this::logMessage);
        conversionController.setProgressCallback(this::updateProgress);
        conversionController.setStatusCallback(this::updateStatus);
    }

    private void initializeUI() {
        mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setStyle("-fx-background-color: #f0f2f5;");

        createDropZone();
        createConfigurationPanel();

        configurationPanel.setVisible(false);
        mainContainer.getChildren().addAll(createHeader(), dropZoneView, configurationPanel);
    }

    private VBox createHeader() {
        Label titleLabel = new Label("🎵 Audio Converter");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#2c3e50"));

        Label subtitleLabel = new Label("Batch Convert • Individual Settings • Multiple Formats");
        subtitleLabel.setFont(Font.font("Arial", 14));
        subtitleLabel.setTextFill(Color.web("#7f8c8d"));

        VBox headerBox = new VBox(5, titleLabel, subtitleLabel);
        headerBox.setAlignment(Pos.CENTER);
        return headerBox;
    }

    private void createDropZone() {
        dropZoneView = new VBox(20);
        dropZoneView.setAlignment(Pos.CENTER);
        dropZoneView.setPrefHeight(180);
        dropZoneView.setMaxWidth(700);
        dropZoneView.setStyle(
                "-fx-border-color: #bdc3c7; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-style: dashed; " +
                        "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-radius: 15;"
        );

        Label dropIcon = new Label("📁");
        dropIcon.setFont(Font.font(48));

        Label dropLabel = new Label("Drag and Drop Audio Files Here");
        dropLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        dropLabel.setTextFill(Color.web("#34495e"));

        Label supportedLabel = new Label("Supported: MP3, WAV, M4A, FLAC");
        supportedLabel.setFont(Font.font("Arial", 13));
        supportedLabel.setTextFill(Color.web("#7f8c8d"));

        Label instructionLabel = new Label("Each file can have individual conversion settings");
        instructionLabel.setFont(Font.font("Arial", 12));
        instructionLabel.setTextFill(Color.web("#95a5a6"));

        dropZoneView.getChildren().addAll(dropIcon, dropLabel, supportedLabel, instructionLabel);
    }

    private void createConfigurationPanel() {
        configurationPanel = new VBox(20);
        configurationPanel.setPadding(new Insets(25));
        configurationPanel.setMaxWidth(850);
        configurationPanel.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );

        VBox fileSection = createFileSection();
        VBox convertSection = createConvertSection();
        VBox progressSection = createProgressSection();
        VBox logSection = createLogSection();

        configurationPanel.getChildren().addAll(
                fileSection,
                new Separator(),
                convertSection,
                progressSection,
                logSection
        );
    }

    private VBox createFileSection() {
        Label filesLabel = new Label("Selected Files - Individual Settings");
        filesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        fileList = new ListView<>();
        fileList.setPrefHeight(300);
        fileList.setStyle("-fx-background-radius: 8; -fx-border-color: #ecf0f1; -fx-border-radius: 8;");
        fileList.setCellFactory(param -> new FileConversionCell());

        clearButton = new Button("Clear All");
        clearButton.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 6; " +
                        "-fx-padding: 8 16;"
        );

        HBox buttonBox = new HBox(clearButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        return new VBox(10, filesLabel, fileList, buttonBox);
    }

    // Custom ListCell for individual file configuration
    private class FileConversionCell extends ListCell<ConversionConfig.FileConversionSettings> {
        private HBox content;
        private VBox infoBox;
        private Label fileNameLabel;
        private Label fileSizeLabel;
        private ComboBox<String> formatCombo;
        private ComboBox<String> qualityCombo;
        private Button settingsButton;
        private Button removeButton;

        public FileConversionCell() {
            super();

            // File info
            fileNameLabel = new Label();
            fileNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            fileNameLabel.setTextFill(Color.web("#2c3e50"));

            fileSizeLabel = new Label();
            fileSizeLabel.setFont(Font.font("Arial", 11));
            fileSizeLabel.setTextFill(Color.web("#7f8c8d"));

            infoBox = new VBox(3, fileNameLabel, fileSizeLabel);
            infoBox.setPrefWidth(250);

            // Format combo
            formatCombo = new ComboBox<>();
            formatCombo.getItems().addAll(configController.getSupportedFormats());
            formatCombo.setPrefWidth(100);
            formatCombo.setStyle("-fx-font-size: 11px;");

            // Quality combo
            qualityCombo = new ComboBox<>();
            qualityCombo.setPrefWidth(120);
            qualityCombo.setStyle("-fx-font-size: 11px;");

            // Settings button
            settingsButton = new Button("⚙️");
            settingsButton.setPrefSize(35, 35);
            settingsButton.setStyle(
                    "-fx-background-color: #3498db; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 5; " +
                            "-fx-font-size: 14px;"
            );
            settingsButton.setTooltip(new Tooltip("Advanced Settings"));

            // Remove button
            removeButton = new Button("-");
            removeButton.setPrefSize(35, 35);
            removeButton.setStyle(
                    "-fx-background-color: #e74c3c; " +
                            "-fx-text-fill: white; " +
                            "-fx-background-radius: 5; " +
                            "-fx-font-size: 14px;"
            );
            removeButton.setTooltip(new Tooltip("Remove File"));

            // Labels for combos
            VBox formatBox = new VBox(3);
            Label formatLabel = new Label("Format:");
            formatLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            formatLabel.setTextFill(Color.web("#666"));
            formatBox.getChildren().addAll(formatLabel, formatCombo);

            VBox qualityBox = new VBox(3);
            Label qualityLabel = new Label("Quality:");
            qualityLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            qualityLabel.setTextFill(Color.web("#666"));
            qualityBox.getChildren().addAll(qualityLabel, qualityCombo);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            content = new HBox(15);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(10));
            content.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
            content.getChildren().addAll(infoBox, spacer, formatBox, qualityBox, settingsButton, removeButton);
        }

        @Override
        protected void updateItem(ConversionConfig.FileConversionSettings item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
            } else {
                File file = item.getInputFile();
                fileNameLabel.setText("📄 " + file.getName());
                fileSizeLabel.setText(getFileSize(file) + " • " + getFileExtension(file.getName()).toUpperCase());

                // Set format
                formatCombo.setValue(item.getOutputFormat());
                formatCombo.setOnAction(e -> {
                    String selectedFormat = formatCombo.getValue();
                    item.setOutputFormat(selectedFormat);
                    updateQualityComboForItem(item, qualityCombo, selectedFormat);
                    logMessage("Format changed to " + selectedFormat.toUpperCase() + " for: " + file.getName());
                });

                // Update quality combo based on format
                updateQualityComboForItem(item, qualityCombo, item.getOutputFormat());

                // Set quality
                qualityCombo.setValue(item.getQuality());
                qualityCombo.setOnAction(e -> {
                    String selectedQuality = qualityCombo.getValue();
                    item.setQuality(selectedQuality);
                    logMessage("Quality changed to " + selectedQuality + " for: " + file.getName());
                });

                // Settings button action
                settingsButton.setOnAction(e -> showAdvancedSettingsForFile(item));

                // Remove button action
                removeButton.setOnAction(e -> {
                    config.removeInputFile(file);
                    updateFileList();
                    logMessage("Removed file: " + file.getName());
                    if (!config.hasInputFiles()) {
                        configurationPanel.setVisible(false);
                    }
                });

                setGraphic(content);
            }
        }
    }

    private void updateQualityComboForItem(ConversionConfig.FileConversionSettings item,
                                           ComboBox<String> combo, String format) {
        combo.getItems().clear();
        Configr.QualityPreset[] presets = configController.getQualityPresetsForFormat(format);
        for (Configr.QualityPreset preset : presets) {
            combo.getItems().add(preset.getValue());
        }
        if (combo.getItems().size() > 0) {
            combo.setValue(combo.getItems().get(2)); // Default to "Good"
            item.setQuality(combo.getValue());
        }
    }

    private void showAdvancedSettingsForFile(ConversionConfig.FileConversionSettings fileSettings) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Advanced Settings");
        dialog.setHeaderText("Configure settings for: " + fileSettings.getInputFile().getName());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        Label sampleRateLabel = new Label("Sample Rate:");
        ComboBox<String> sampleRateCombo = new ComboBox<>();
        sampleRateCombo.getItems().addAll(configController.getSampleRates());
        sampleRateCombo.setValue(fileSettings.getSampleRate());
        sampleRateCombo.setPrefWidth(150);

        Label channelsLabel = new Label("Channels:");
        ComboBox<String> channelsCombo = new ComboBox<>();
        channelsCombo.getItems().addAll(configController.getChannelOptions());
        channelsCombo.setValue(fileSettings.getChannels());
        channelsCombo.setPrefWidth(150);

        grid.addRow(0, sampleRateLabel, sampleRateCombo);
        grid.addRow(1, channelsLabel, channelsCombo);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                fileSettings.setSampleRate(sampleRateCombo.getValue());
                fileSettings.setChannels(channelsCombo.getValue());
                logMessage("Advanced settings updated for " + fileSettings.getInputFile().getName() +
                        ": " + sampleRateCombo.getValue() + ", " + channelsCombo.getValue());
            }
        });
    }

    private VBox createConvertSection() {
        convertButton = new Button("Convert All Files");
        convertButton.setPrefWidth(250);
        convertButton.setPrefHeight(50);
        convertButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #27ae60, #229954); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 16px; " +
                        "-fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);"
        );

        HBox convertBox = new HBox(convertButton);
        convertBox.setAlignment(Pos.CENTER);

        return new VBox(convertBox);
    }

    private VBox createProgressSection() {
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(500);
        progressBar.setPrefHeight(25);
        progressBar.setVisible(false);
        progressBar.setStyle("-fx-accent: #27ae60;");

        statusLabel = new Label("");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        statusLabel.setTextFill(Color.web("#2980b9"));

        VBox progressSection = new VBox(10, progressBar, statusLabel);
        progressSection.setAlignment(Pos.CENTER);

        return progressSection;
    }

    private VBox createLogSection() {
        Label logLabel = new Label("Processing Log");
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        logArea = new TextArea();
        logArea.setPrefRowCount(8);
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setStyle(
                "-fx-background-color: #fafafa; " +
                        "-fx-border-color: #ddd; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 11px;"
        );

        return new VBox(10, logLabel, logArea);
    }

    private void setupEventHandlers() {
        dropZoneView.setOnDragOver(e -> dropZoneController.handleDragOver(e));
        dropZoneView.setOnDragDropped(e -> dropZoneController.handleDragDropped(e));

        clearButton.setOnAction(e -> clearFiles());
        convertButton.setOnAction(e -> startConversion());
    }

    private void onFilesDropped() {
        updateFileList();
        configurationPanel.setVisible(true);
        logMessage("✅ Added " + config.getInputFileCount() + " file(s)");
    }

    private void onDragEntered() {
        dropZoneView.setStyle(
                "-fx-border-color: #27ae60; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-style: dashed; " +
                        "-fx-background-color: rgba(39,174,96,0.1); " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-radius: 15;"
        );
    }

    private void onDragExited() {
        dropZoneView.setStyle(
                "-fx-border-color: #bdc3c7; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-style: dashed; " +
                        "-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-border-radius: 15;"
        );
    }

    private void updateFileList() {
        fileList.getItems().clear();
        fileList.getItems().addAll(config.getAllFileSettings());
    }

    private String getFileSize(File file) {
        long bytes = file.length();
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    private void clearFiles() {
        config.clearInputFiles();
        updateFileList();
        configurationPanel.setVisible(false);
        logArea.clear();
        progressBar.setProgress(0);
        progressBar.setVisible(false);
        statusLabel.setText("");
        logMessage("🧹 All files cleared");
    }

    private void startConversion() {
        if (!config.hasInputFiles()) {
            showAlert("No Files", "Please drop audio files to convert.");
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Directory");
        File outputDir = directoryChooser.showDialog(primaryStage);

        if (outputDir == null) return;

        convertButton.setDisable(true);
        clearButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(0);
        statusLabel.setText("🔄 Starting conversion...");

        Task<Void> conversionTask = conversionController.createConversionTask(outputDir);

        progressBar.setVisible(true);
        statusLabel.setText("🔄 Starting conversion...");

        conversionTask.setOnSucceeded(e -> {
            convertButton.setDisable(false);
            clearButton.setDisable(false);
            showAlert("Success", "All files converted successfully!\nOutput: " + outputDir.getAbsolutePath());
        });

        conversionTask.setOnFailed(e -> {
            convertButton.setDisable(false);
            clearButton.setDisable(false);
            showAlert("Error", "Conversion failed: " + conversionTask.getException().getMessage());
        });

// เริ่ม task ใน thread แยก
        Thread conversionThread = new Thread(conversionTask);
        conversionThread.setDaemon(true);
        conversionThread.start();

    }

    private void logMessage(String message) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
            logArea.appendText("[" + timestamp + "] " + message + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private void updateProgress(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    }

    private void updateStatus(String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
