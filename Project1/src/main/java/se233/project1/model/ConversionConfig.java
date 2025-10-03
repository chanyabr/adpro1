package se233.project1.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversionConfig {
    private List<FileConversionSettings> fileSettings;
    private Map<File, FileConversionSettings> settingsMap;

    public ConversionConfig() {
        this.fileSettings = new ArrayList<>();
        this.settingsMap = new HashMap<>();
    }

    public List<File> getInputFiles() {
        List<File> files = new ArrayList<>();
        for (FileConversionSettings settings : fileSettings) {
            files.add(settings.getInputFile());
        }
        return files;
    }

    public void setInputFiles(List<File> inputFiles) {
        if (inputFiles == null) {
            throw new IllegalArgumentException("Input files list cannot be null");
        }

        fileSettings.clear();
        settingsMap.clear();

        for (File file : inputFiles) {
            addInputFile(file);
        }
    }

    public void addInputFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Input file cannot be null");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("Input file does not exist");
        }

        // Check if file already exists
        if (settingsMap.containsKey(file)) {
            return; // Don't add duplicates
        }

        FileConversionSettings settings = new FileConversionSettings(file);
        fileSettings.add(settings);
        settingsMap.put(file, settings);
    }

    public void removeInputFile(File file) {
        FileConversionSettings settings = settingsMap.remove(file);
        if (settings != null) {
            fileSettings.remove(settings);
        }
    }

    public void clearInputFiles() {
        fileSettings.clear();
        settingsMap.clear();
    }

    public int getInputFileCount() {
        return fileSettings.size();
    }

    public boolean hasInputFiles() {
        return !fileSettings.isEmpty();
    }

    public List<FileConversionSettings> getAllFileSettings() {
        return new ArrayList<>(fileSettings);
    }

    public FileConversionSettings getSettingsForFile(File file) {
        return settingsMap.get(file);
    }

    public void updateFileSettings(File file, String outputFormat, String quality,
                                   String sampleRate, String channels) {
        FileConversionSettings settings = settingsMap.get(file);
        if (settings != null) {
            settings.setOutputFormat(outputFormat);
            settings.setQuality(quality);
            settings.setSampleRate(sampleRate);
            settings.setChannels(channels);
        }
    }

    // Inner class for individual file settings
    public static class FileConversionSettings {
        private File inputFile;
        private String outputFormat;
        private String quality;
        private String sampleRate;
        private String channels;
        private String bitrate;

        public FileConversionSettings(File inputFile) {
            this.inputFile = inputFile;
            this.outputFormat = "mp3";
            this.quality = "192 kbps";
            this.sampleRate = "44100 Hz";
            this.channels = "Stereo";
            this.bitrate = "192 kbps";
        }

        public File getInputFile() { return inputFile; }

        public String getOutputFormat() { return outputFormat; }
        public void setOutputFormat(String outputFormat) {
            if (outputFormat == null || outputFormat.trim().isEmpty()) {
                throw new IllegalArgumentException("Output format cannot be null or empty");
            }
            this.outputFormat = outputFormat.toLowerCase().trim();
        }

        public String getQuality() { return quality; }
        public void setQuality(String quality) {
            if (quality == null || quality.trim().isEmpty()) {
                throw new IllegalArgumentException("Quality cannot be null or empty");
            }
            this.quality = quality.trim();
        }

        public String getSampleRate() { return sampleRate; }
        public void setSampleRate(String sampleRate) {
            if (sampleRate == null || sampleRate.trim().isEmpty()) {
                throw new IllegalArgumentException("Sample rate cannot be null or empty");
            }
            this.sampleRate = sampleRate.trim();
        }

        public String getChannels() { return channels; }
        public void setChannels(String channels) {
            if (channels == null || channels.trim().isEmpty()) {
                throw new IllegalArgumentException("Channels cannot be null or empty");
            }
            this.channels = channels.trim();
        }

        public String getBitrate() { return bitrate; }
        public void setBitrate(String bitrate) { this.bitrate = bitrate; }

        public String getChannelsAsNumber() {
            return "Mono".equals(channels) ? "1" : "2";
        }

        public String getSampleRateAsNumber() {
            return sampleRate.split(" ")[0];
        }

        @Override
        public String toString() {
            return String.format("FileSettings{file='%s', format='%s', quality='%s', sampleRate='%s', channels='%s'}",
                    inputFile.getName(), outputFormat, quality, sampleRate, channels);
        }
    }
}
