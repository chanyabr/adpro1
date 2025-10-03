package se233.project1.view;

import javafx.scene.control.Alert;
import se233.project1.model.ConversionConfig;

import java.io.File;
import java.util.List;

public class BatchConverter {

    public void validateBatchConversion(ConversionConfig config) throws BatchConversionException {
        if (config == null) {
            throw new BatchConversionException("Configuration cannot be null");
        }

        List<File> files = config.getInputFiles();
        if (files == null || files.isEmpty()) {
            throw new BatchConversionException("No files selected for batch conversion");
        }

        for (File file : files) {
            if (!file.exists()) {
                throw new BatchConversionException("File not found: " + file.getName());
            }

            if (!file.canRead()) {
                throw new BatchConversionException("Cannot read file: " + file.getName());
            }
        }
    }

    public void showBatchProgress(int current, int total) {
        System.out.println("Processing file " + current + " of " + total);
    }

    public void showBatchComplete(int totalFiles, File outputDirectory) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Batch Conversion Complete");
        alert.setHeaderText("Success!");
        alert.setContentText("Successfully converted " + totalFiles + " file(s).\n" +
                "Output directory: " + outputDirectory.getAbsolutePath());
        alert.showAndWait();
    }

    public String generateBatchReport(ConversionConfig config) {
        StringBuilder report = new StringBuilder();
        report.append("=== Batch Conversion Report ===\n");
        report.append("Total Files: ").append(config.getInputFileCount()).append("\n");
        report.append("\nFile Details:\n");

        int index = 1;
        for (ConversionConfig.FileConversionSettings settings : config.getAllFileSettings()) {
            report.append(index++).append(". ")
                    .append(settings.getInputFile().getName())
                    .append(" -> ").append(settings.getOutputFormat().toUpperCase())
                    .append(" (").append(settings.getQuality()).append(")\n");
        }

        return report.toString();
    }

    public void logBatchSettings(ConversionConfig config) {
        System.out.println("=== Batch Conversion Settings ===");
        for (ConversionConfig.FileConversionSettings settings : config.getAllFileSettings()) {
            System.out.println("File: " + settings.getInputFile().getName());
            System.out.println("  Output Format: " + settings.getOutputFormat());
            System.out.println("  Quality: " + settings.getQuality());
            System.out.println("  Sample Rate: " + settings.getSampleRate());
            System.out.println("  Channels: " + settings.getChannels());
            System.out.println("---");
        }
    }

    public static class BatchConversionException extends Exception {
        public BatchConversionException(String message) {
            super(message);
        }

        public BatchConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
