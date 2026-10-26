package org.oosd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationManagerTest {

    private int originalWidth;
    private ConfigurationManager.PlayerType originalPlayer1Type;

    @BeforeEach
    void saveOriginalState() {
        originalWidth = ConfigurationManager.getInstance().getWidth();
        originalPlayer1Type = ConfigurationManager.getInstance().getPlayer1Type();
    }

    @AfterEach
    void restoreOriginalState() {
        ConfigurationManager.getInstance().setWidth(originalWidth);
        ConfigurationManager.getInstance().setPlayer1Type(originalPlayer1Type);
    }

    @Test
    void setWidth_withinValidRange_isAccepted() {
        ConfigurationManager.getInstance().setWidth(12);
        assertEquals(12, ConfigurationManager.getInstance().getWidth());
    }

    @Test
    void setWidth_belowMinimum_isRejected() {
        ConfigurationManager.getInstance().setWidth(9);
        ConfigurationManager.getInstance().setWidth(3); // below allowed minimum of 5
        assertEquals(9, ConfigurationManager.getInstance().getWidth());
    }

    @Test
    void setPlayer1Type_setterGetterRoundTrip() {
        ConfigurationManager.getInstance().setPlayer1Type(ConfigurationManager.PlayerType.AI);
        assertEquals(ConfigurationManager.PlayerType.AI,
                ConfigurationManager.getInstance().getPlayer1Type());
    }
}