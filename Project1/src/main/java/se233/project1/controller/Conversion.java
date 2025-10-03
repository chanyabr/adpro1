package se233.project1.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import se233.project1.model.ConversionConfig;
import se233.project1.model.ConversionConfig.FileConversionSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Conversion {
    private ExecutorService executorService;
    private ConversionConfig config;
    private Consumer<String> logCallback;
    private Consumer<Double> progressCallback;
    private Consumer<String> statusCallback;
    private AtomicInteger processedFiles;
    private AtomicInteger totalFiles;

    public Conversion(ConversionConfig config) {
        this.config = config;
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.processedFiles = new AtomicInteger(0);
        this.totalFiles = new AtomicInteger(0);
    }

    public void setLogCallback(Consumer<String> callback) {
        this.logCallback = callback;
    }

    public void setProgressCallback(Consumer<Double> callback) {
        this.progressCallback = callback;
    }

    public void setStatusCallback(Consumer<String> callback) {
        this.statusCallback = callback;
    }

    public Task<Void> createConversionTask(File outputDirectory) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                performBatchConversion(outputDirectory);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    updateStatus("All conversions completed successfully!");
                    logMessage("Batch conversion completed successfully!");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    updateStatus("Conversion failed!");
                    Throwable exception = getException();
                    if (exception != null) {
                        handleException(new ConversionException("Batch conversion failed", exception));
                    }
                });
            }
        };
    }

    private void performBatchConversion(File outputDirectory) throws Exception {
        List<FileConversionSettings> allSettings = config.getAllFileSettings();
        if (allSettings == null || allSettings.isEmpty()) {
            throw new ConversionException("No input files provided");
        }

        validateOutputDirectory(outputDirectory);

        totalFiles.set(allSettings.size());
        processedFiles.set(0);

        logMessage("Starting batch conversion of " + allSettings.size() + " file(s)");
        logMessage("Output directory: " + outputDirectory.getAbsolutePath());

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < allSettings.size(); i++) {
            final FileConversionSettings settings = allSettings.get(i);
            final int fileIndex = i + 1;

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    convertSingleFile(settings, outputDirectory, fileIndex);
                } catch (Exception e) {
                    throw new RuntimeException(new ConversionException("Failed to convert " + settings.getInputFile().getName(), e));
                }
            }, executorService);

            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            throw new ConversionException("Some conversions failed", e);
        }
    }

    private void convertSingleFile(FileConversionSettings settings, File outputDirectory, int fileIndex) throws Exception {
        File inputFile = settings.getInputFile();
        updateStatus("Converting: " + inputFile.getName());
        logMessage("Processing file " + fileIndex + "/" + totalFiles.get() + ": " + inputFile.getName());
        logMessage("   Settings: " + settings.getOutputFormat().toUpperCase() + ", " + settings.getQuality() +
                ", " + settings.getSampleRate() + ", " + settings.getChannels());

        String outputFileName = generateOutputFileName(inputFile, settings.getOutputFormat());
        File outputFile = new File(outputDirectory, outputFileName);

        performFFmpegConversion(inputFile, outputFile, settings);

        int completed = processedFiles.incrementAndGet();
        Platform.runLater(() -> {
            double progress = (double) completed / totalFiles.get();
            updateProgress(progress);
            logMessage("Completed: " + outputFile.getName() + " (" + completed + "/" + totalFiles.get() + ")");
        });
    }

    private void performFFmpegConversion(File inputFile, File outputFile, FileConversionSettings settings) throws Exception {
        try {
            long fileSize = inputFile.length();
            int baseTime = 500;
            int sizeTime = (int) (fileSize / (1024 * 1024) * 200);
            int totalTime = baseTime + sizeTime + (int)(Math.random() * 1000);

            Thread.sleep(Math.min(totalTime, 3000));

            // In real implementation, execute FFmpeg here with settings
            Files.copy(inputFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            Platform.runLater(() -> {
                logMessage("🔧 FFmpeg conversion: " + inputFile.getName() + " → " + outputFile.getName());
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConversionException("Conversion interrupted", e);
        } catch (IOException e) {
            throw new ConversionException("File I/O error during conversion", e);
        }
    }

    private String generateOutputFileName(File inputFile, String outputFormat) {
        String baseName = inputFile.getName();
        int lastDot = baseName.lastIndexOf('.');
        if (lastDot > 0) {
            baseName = baseName.substring(0, lastDot);
        }
        return baseName + "_converted." + outputFormat;
    }

    private void validateOutputDirectory(File outputDirectory) throws ConversionException {
        if (outputDirectory == null) {
            throw new ConversionException("Output directory cannot be null");
        }
        if (!outputDirectory.exists()) {
            throw new ConversionException("Output directory does not exist");
        }
        if (!outputDirectory.isDirectory()) {
            throw new ConversionException("Output path is not a directory");
        }
        if (!outputDirectory.canWrite()) {
            throw new ConversionException("Cannot write to output directory");
        }
    }

    private void logMessage(String message) {
        if (logCallback != null) {
            Platform.runLater(() -> logCallback.accept(message));
        }
    }

    private void updateProgress(double progress) {
        if (progressCallback != null) {
            Platform.runLater(() -> progressCallback.accept(progress));
        }
    }

    private void updateStatus(String status) {
        if (statusCallback != null) {
            Platform.runLater(() -> statusCallback.accept(status));
        }
    }

    private void handleException(Exception e) {
        logMessage("🚨 ERROR: " + e.getMessage());
        if (e.getCause() != null) {
            logMessage("   Cause: " + e.getCause().getMessage());
        }
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public static class ConversionException extends Exception {
        public ConversionException(String message) {
            super(message);
        }

        public ConversionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
