package org.oosd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HighScoreManager {

    private static final Path SCORE_FILE =
            Paths.get("scores.json");

    private static final HighScoreManager INSTANCE =
            new HighScoreManager();

    private final List<ScoreEntry> scores = new ArrayList<>();

    private HighScoreManager() {
        load();
    }

    public static HighScoreManager getInstance() {
        return INSTANCE;
    }

    public record ScoreEntry(String player, int score) {
    }

    public void addScore(String player, int score) {

        if (player == null || player.trim().isEmpty()) {
            player = "Player";
        }

        scores.add(
                new ScoreEntry(player.trim(), score)
        );

        scores.sort(
                Comparator.comparingInt(ScoreEntry::score)
                        .reversed()
        );

        if (scores.size() > 10) {
            scores.subList(10, scores.size()).clear();
        }

        save();
    }

    public List<ScoreEntry> getTopScores() {

        return new ArrayList<>(scores);
    }

    public void clearScores() {

        scores.clear();
        save();
    }

    private void save() {

        StringBuilder json =
                new StringBuilder("[\n");

        for (int i = 0; i < scores.size(); i++) {

            ScoreEntry entry = scores.get(i);

            json.append("  {\n");
            json.append("    \"player\": \"")
                    .append(escape(entry.player()))
                    .append("\",\n");
            json.append("    \"score\": ")
                    .append(entry.score())
                    .append("\n");
            json.append("  }");

            if (i < scores.size() - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("]");

        try {

            Files.writeString(
                    SCORE_FILE,
                    json.toString()
            );

        } catch (IOException e) {

            System.out.println(
                    "Could not save scores.json: "
                            + e.getMessage()
            );
        }
    }

    private void load() {

        scores.clear();

        if (!Files.exists(SCORE_FILE)) {

            save();
            return;
        }

        try {

            String json =
                    Files.readString(SCORE_FILE);

            Pattern pattern = Pattern.compile(
                    "\\{\\s*\"player\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*\"score\"\\s*:\\s*(\\d+)\\s*\\}"
            );

            Matcher matcher =
                    pattern.matcher(json);

            while (matcher.find()) {

                String player =
                        matcher.group(1);

                int score =
                        Integer.parseInt(
                                matcher.group(2)
                        );

                scores.add(
                        new ScoreEntry(player, score)
                );
            }

            scores.sort(
                    Comparator.comparingInt(
                                    ScoreEntry::score)
                            .reversed()
            );

            if (scores.size() > 10) {
                scores.subList(10, scores.size()).clear();
            }

        } catch (Exception e) {

            System.out.println(
                    "Could not load scores.json."
            );
        }
    }

    private String escape(String text) {

        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}