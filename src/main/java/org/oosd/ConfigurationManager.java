package org.oosd;

import org.oosd.audio.SoundManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigurationManager {

    private static final Path CONFIG_FILE =
            Paths.get("config.json");

    private static final ConfigurationManager INSTANCE =
            new ConfigurationManager();

    public enum PlayerType {
        HUMAN,
        AI,
        EXTERNAL
    }

    private int width = 10;
    private int height = 20;
    private int level = 1;

    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    private boolean aiPlay = false;
    private boolean extendMode = false;

    private PlayerType player1Type = PlayerType.HUMAN;
    private PlayerType player2Type = PlayerType.HUMAN;

    private ConfigurationManager() {
        load();
    }

    public static ConfigurationManager getInstance() {
        return INSTANCE;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        if (width >= 5 && width <= 15) {
            this.width = width;
        }
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        if (height >= 15 && height <= 30) {
            this.height = height;
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level >= 1 && level <= 10) {
            this.level = level;
        }
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;

        SoundManager.getInstance()
                .setMusicEnabled(musicEnabled);
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;

        SoundManager.getInstance()
                .setSfxEnabled(soundEnabled);
    }

    public boolean isAiPlay() {
        return aiPlay;
    }

    public void setAiPlay(boolean aiPlay) {
        this.aiPlay = aiPlay;
    }

    public boolean isExtendMode() {
        return extendMode;
    }

    public void setExtendMode(boolean extendMode) {
        this.extendMode = extendMode;
    }

    public PlayerType getPlayer1Type() {
        return player1Type;
    }

    public void setPlayer1Type(PlayerType player1Type) {
        this.player1Type = player1Type;
    }

    public PlayerType getPlayer2Type() {
        return player2Type;
    }

    public void setPlayer2Type(PlayerType player2Type) {
        this.player2Type = player2Type;
    }

    public void save() {

        String json = "{\n"
                + "  \"width\": " + width + ",\n"
                + "  \"height\": " + height + ",\n"
                + "  \"level\": " + level + ",\n"
                + "  \"musicEnabled\": " + musicEnabled + ",\n"
                + "  \"soundEnabled\": " + soundEnabled + ",\n"
                + "  \"aiPlay\": " + aiPlay + ",\n"
                + "  \"extendMode\": " + extendMode + ",\n"
                + "  \"player1Type\": \"" + player1Type.name() + "\",\n"
                + "  \"player2Type\": \"" + player2Type.name() + "\"\n"
                + "}";

        try {
            Files.writeString(CONFIG_FILE, json);
        } catch (IOException e) {
            System.err.println("Could not save config.json: "
                    + e.getMessage());
        }
    }

    private void load() {

        if (!Files.exists(CONFIG_FILE)) {
            save();
            applySoundSettings();
            return;
        }

        try {

            String json = Files.readString(CONFIG_FILE);

            width = getInt(json, "width", 10);
            height = getInt(json, "height", 20);
            level = getInt(json, "level", 1);

            musicEnabled =
                    getBoolean(json, "musicEnabled", true);

            soundEnabled =
                    getBoolean(json, "soundEnabled", true);

            aiPlay =
                    getBoolean(json, "aiPlay", false);

            extendMode =
                    getBoolean(json, "extendMode", false);

            player1Type =
                    getPlayerType(json, "player1Type",
                            PlayerType.HUMAN);

            player2Type =
                    getPlayerType(json, "player2Type",
                            PlayerType.HUMAN);

        } catch (Exception e) {

            System.err.println(
                    "Could not load config.json. Using defaults."
            );
        }

        applySoundSettings();
    }

    private void applySoundSettings() {

        SoundManager.getInstance()
                .setMusicEnabled(musicEnabled);

        SoundManager.getInstance()
                .setSfxEnabled(soundEnabled);
    }

    private int getInt(
            String json,
            String key,
            int defaultValue) {

        Pattern pattern = Pattern.compile(
                "\"" + key + "\"\\s*:\\s*(\\d+)"
        );

        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return defaultValue;
    }

    private boolean getBoolean(
            String json,
            String key,
            boolean defaultValue) {

        Pattern pattern = Pattern.compile(
                "\"" + key + "\"\\s*:\\s*(true|false)"
        );

        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }

        return defaultValue;
    }

    private PlayerType getPlayerType(
            String json,
            String key,
            PlayerType defaultValue) {

        Pattern pattern = Pattern.compile(
                "\"" + key + "\"\\s*:\\s*\"([A-Z]+)\""
        );

        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {

            try {
                return PlayerType.valueOf(matcher.group(1));
            } catch (IllegalArgumentException ignored) {
                return defaultValue;
            }
        }

        return defaultValue;
    }
}