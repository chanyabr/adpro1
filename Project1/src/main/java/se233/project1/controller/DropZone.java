package se233.project1.controller;

import javafx.scene.control.Alert;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import se233.project1.model.ConversionConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DropZone {
    private static final String[] SUPPORTED_FORMATS = {"mp3", "wav", "m4a", "flac"};
    private ConversionConfig config;
    private Runnable onFilesDropped;
    private Runnable onDragEntered;
    private Runnable onDragExited;

    public DropZone(ConversionConfig config) {
        this.config = config;
    }

    public void setOnFilesDropped(Runnable callback) {
        this.onFilesDropped = callback;
    }

    public void setOnDragEntered(Runnable callback) {
        this.onDragEntered = callback;
    }

    public void setOnDragExited(Runnable callback) {
        this.onDragExited = callback;
    }

    public void handleDragOver(DragEvent event) {
        try {
            if (event.getGestureSource() != event.getSource() && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                if (onDragEntered != null) {
                    onDragEntered.run();
                }
            }
        } catch (Exception e) {
            handleException(new AudioProcessingException("Error handling drag over", e));
        }
        event.consume();
    }

    public void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        try {
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                List<File> validFiles = validateAndFilterFiles(files);

                if (!validFiles.isEmpty()) {
                    for (File file : validFiles) {
                        config.addInputFile(file);
                    }
                    success = true;
                    if (onFilesDropped != null) {
                        onFilesDropped.run();
                    }
                } else {
                    showAlert("No Valid Files",
                            "No valid audio files found. Supported formats: " +
                                    String.join(", ", SUPPORTED_FORMATS).toUpperCase());
                }
            }
        } catch (IllegalArgumentException e) {
            handleException(new AudioProcessingException("Invalid file input", e));
        } catch (Exception e) {
            handleException(new AudioProcessingException("Error processing dropped files", e));
        }

        if (onDragExited != null) {
            onDragExited.run();
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private List<File> validateAndFilterFiles(List<File> files) throws IllegalArgumentException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided");
        }

        List<File> validFiles = new ArrayList<>();
        for (File file : files) {
            if (isValidAudioFile(file)) {
                validFiles.add(file);
            }
        }
        return validFiles;
    }

    private boolean isValidAudioFile(File file) {
        if (!file.exists() || !file.isFile()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        return Arrays.stream(SUPPORTED_FORMATS)
                .anyMatch(format -> fileName.endsWith("." + format));
    }

    private void handleException(Exception e) {
        System.err.println("DropZone Error: " + e.getMessage());
        if (e.getCause() != null) {
            System.err.println("Cause: " + e.getCause().getMessage());
        }
        showAlert("Drop Zone Error", e.getMessage());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class AudioProcessingException extends Exception {
        public AudioProcessingException(String message) {
            super(message);
        }

        public AudioProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}