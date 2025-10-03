package se233.project1.controller;

import se233.project1.model.ConversionConfig;

import java.util.HashMap;
import java.util.Map;

public class Configr {
    private static final String[] SUPPORTED_FORMATS = {"mp3", "wav", "m4a", "flac"};
    private static final Map<String, QualityPreset[]> FORMAT_QUALITY_PRESETS = new HashMap<>();
    private static final String[] SAMPLE_RATES = {"44100 Hz", "48000 Hz", "22050 Hz", "16000 Hz", "8000 Hz"};
    private static final String[] CHANNEL_OPTIONS = {"Mono", "Stereo"};

    private ConversionConfig config;

    static {
        FORMAT_QUALITY_PRESETS.put("mp3", new QualityPreset[]{
                new QualityPreset("Economy", "64 kbps", 0),
                new QualityPreset("Standard", "128 kbps", 1),
                new QualityPreset("Good", "192 kbps", 2),
                new QualityPreset("Best", "320 kbps", 3)
        });

        FORMAT_QUALITY_PRESETS.put("wav", new QualityPreset[]{
                new QualityPreset("Economy", "16-bit", 0),
                new QualityPreset("Standard", "16-bit", 1),
                new QualityPreset("Good", "24-bit", 2),
                new QualityPreset("Best", "24-bit", 3)
        });

        FORMAT_QUALITY_PRESETS.put("m4a", new QualityPreset[]{
                new QualityPreset("Economy", "64 kbps", 0),
                new QualityPreset("Standard", "128 kbps", 1),
                new QualityPreset("Good", "192 kbps", 2),
                new QualityPreset("Best", "320 kbps", 3)
        });

        FORMAT_QUALITY_PRESETS.put("flac", new QualityPreset[]{
                new QualityPreset("Economy", "Level 0", 0),
                new QualityPreset("Standard", "Level 5", 1),
                new QualityPreset("Good", "Level 5", 2),
                new QualityPreset("Best", "Level 8", 3)
        });
    }

    public Configr(ConversionConfig config) {
        this.config = config;
    }

    public String[] getSupportedFormats() {
        return SUPPORTED_FORMATS.clone();
    }

    public QualityPreset[] getQualityPresetsForFormat(String format) {
        return FORMAT_QUALITY_PRESETS.getOrDefault(format, new QualityPreset[]{});
    }

    public String[] getSampleRates() {
        return SAMPLE_RATES.clone();
    }

    public String[] getChannelOptions() {
        return CHANNEL_OPTIONS.clone();
    }

    public void updateOutputFormat(String format) {
        try {
            if (format == null || format.trim().isEmpty()) {
                throw new IllegalArgumentException("Output format cannot be null or empty");
            }
            // This would be used for batch settings if needed
        } catch (Exception e) {
            handleException(new ConfigurationException("Failed to update output format", e));
        }
    }

    public void updateQuality(String quality) {
        try {
            if (quality == null || quality.trim().isEmpty()) {
                throw new IllegalArgumentException("Quality cannot be null or empty");
            }
            // This would be used for batch settings if needed
        } catch (Exception e) {
            handleException(new ConfigurationException("Failed to update quality", e));
        }
    }

    public void updateSampleRate(String sampleRate) {
        try {
            if (sampleRate == null || sampleRate.trim().isEmpty()) {
                throw new IllegalArgumentException("Sample rate cannot be null or empty");
            }
            // This would be used for batch settings if needed
        } catch (Exception e) {
            handleException(new ConfigurationException("Failed to update sample rate", e));
        }
    }

    public void updateChannels(String channels) {
        try {
            if (channels == null || channels.trim().isEmpty()) {
                throw new IllegalArgumentException("Channels cannot be null or empty");
            }
            // This would be used for batch settings if needed
        } catch (Exception e) {
            handleException(new ConfigurationException("Failed to update channels", e));
        }
    }

    public void updateBitrate(String bitrate) {
        // This would be used for batch settings if needed
    }

    private void handleException(Exception e) {
        System.err.println("Configuration Error: " + e.getMessage());
        if (e.getCause() != null) {
            System.err.println("Cause: " + e.getCause().getMessage());
        }
    }

    public static class QualityPreset {
        private final String label;
        private final String value;
        private final int sliderPosition;

        public QualityPreset(String label, String value, int sliderPosition) {
            this.label = label;
            this.value = value;
            this.sliderPosition = sliderPosition;
        }

        public String getLabel() { return label; }
        public String getValue() { return value; }
        public int getSliderPosition() { return sliderPosition; }
    }

    public static class ConfigurationException extends Exception {
        public ConfigurationException(String message) {
            super(message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
