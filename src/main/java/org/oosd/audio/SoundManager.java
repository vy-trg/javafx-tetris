package org.oosd.audio;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

public final class SoundManager {

    public enum Sound {
        MOVE("move"),
        ROTATE("rotate"),
        LOCK("lock"),
        LINE_CLEAR("line_clear"),
        GAME_OVER("game_over"),
        PAUSE("pause");

        private final String baseName;

        Sound(String baseName) {
            this.baseName = baseName;
        }
    }

    private static final String AUDIO_DIR = "/audio/";
    private static final String MUSIC_BASE_NAME = "background_music";
    private static final String[] SUPPORTED_EXTENSIONS = {".wav", ".mp3", ".m4a", ".aiff"};

    private static final SoundManager INSTANCE = new SoundManager();

    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;

    private MediaPlayer musicPlayer;
    private boolean musicLoadAttempted = false;

    //true only between playMusic() (game start) and stopMusic() (game end)
    private boolean musicSessionActive = false;

    private final Map<Sound, AudioClip> clips = new EnumMap<>(Sound.class);

    private SoundManager() {
    }

    public static SoundManager getInstance() {
        return INSTANCE;
    }

    //settings (wired to config screen checkboxes)
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            pauseMusic();
        } else if (musicSessionActive && musicPlayer != null) {
            musicPlayer.play();
        }
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setSfxEnabled(boolean enabled) {
        this.sfxEnabled = enabled;
    }

    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    //background music
    public void playMusic() {
        musicSessionActive = true;
        if (!musicEnabled) {
            return;
        }
        ensureMusicLoaded();
        if (musicPlayer != null && musicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            musicPlayer.play();
        }
    }

    public void pauseMusic() {
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }

    public void resumeMusic() {
        if (musicEnabled && musicPlayer != null) {
            musicPlayer.play();
        }
    }

    public void stopMusic() {
        musicSessionActive = false;
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }

    private void ensureMusicLoaded() {
        if (musicLoadAttempted) {
            return;
        }
        musicLoadAttempted = true;

        URL url = findResource(MUSIC_BASE_NAME);
        if (url == null) {
            System.err.println("[SoundManager] No background music file found under src/main/resources/audio/"
                    + " (expected " + MUSIC_BASE_NAME + ".wav/.mp3/.m4a/.aiff) - music will stay silent until one is added.");
            return;
        }
        try {
            Media media = new Media(url.toExternalForm());
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.setVolume(0.5);
        } catch (Exception e) {
            System.err.println("[SoundManager] Could not load background music: " + e.getMessage());
            musicPlayer = null;
        }
    }

    //sound effects
    public void playEffect(Sound sound) {
        if (!sfxEnabled) {
            return;
        }
        AudioClip clip = getClip(sound);
        if (clip != null) {
            clip.play();
        }
    }

    private AudioClip getClip(Sound sound) {
        if (clips.containsKey(sound)) {
            return clips.get(sound); //may be a cached null if file is missing
        }
        AudioClip clip = loadClip(sound);
        clips.put(sound, clip);
        return clip;
    }

    private AudioClip loadClip(Sound sound) {
        URL url = findResource(sound.baseName);
        if (url == null) {
            System.err.println("[SoundManager] No sound file found under src/main/resources/audio/"
                    + " (expected " + sound.baseName + ".wav/.mp3/.m4a/.aiff) - skipping this effect until one is added.");
            return null;
        }
        try {
            return new AudioClip(url.toExternalForm());
        } catch (Exception e) {
            System.err.println("[SoundManager] Could not load sound '" + sound.baseName + "': " + e.getMessage());
            return null;
        }
    }

    private URL findResource(String baseName) {
        for (String extension : SUPPORTED_EXTENSIONS) {
            URL url = SoundManager.class.getResource(AUDIO_DIR + baseName + extension);
            if (url != null) {
                return url;
            }
        }
        return null;
    }
}
