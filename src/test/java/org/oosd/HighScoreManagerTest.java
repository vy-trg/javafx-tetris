package org.oosd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HighScoreManagerTest {

    private List<HighScoreManager.ScoreEntry> originalScores;

    @BeforeEach
    void saveOriginalScores() {
        originalScores = HighScoreManager.getInstance().getTopScores();
        HighScoreManager.getInstance().clearScores();
    }

    @AfterEach
    void restoreOriginalScores() {
        HighScoreManager.getInstance().clearScores();
        for (HighScoreManager.ScoreEntry entry : originalScores) {
            HighScoreManager.getInstance().addScore(entry.player(), entry.score());
        }
    }

    @Test
    void addScore_sortsEntriesDescendingByScore() {
        HighScoreManager.getInstance().addScore("A", 50);
        HighScoreManager.getInstance().addScore("B", 100);
        HighScoreManager.getInstance().addScore("C", 75);

        List<HighScoreManager.ScoreEntry> top = HighScoreManager.getInstance().getTopScores();

        assertEquals("B", top.get(0).player());
        assertEquals("C", top.get(1).player());
        assertEquals("A", top.get(2).player());
    }

    @Test
    void addScore_keepsOnlyTopTenEntries() {
        for (int i = 1; i <= 12; i++) {
            HighScoreManager.getInstance().addScore("Player" + i, i * 10);
        }

        assertEquals(10, HighScoreManager.getInstance().getTopScores().size());
        assertEquals(120, HighScoreManager.getInstance().getTopScores().get(0).score());
    }

    @Test
    void addScore_blankPlayerName_defaultsToPlayer() {
        HighScoreManager.getInstance().addScore("   ", 20);
        assertEquals("Player", HighScoreManager.getInstance().getTopScores().get(0).player());
    }
}