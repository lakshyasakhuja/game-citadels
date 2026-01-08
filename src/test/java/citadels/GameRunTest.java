package citadels;

import citadels.card.DistrictCard;
import citadels.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for Game class run functionality.
 * Tests complete game execution flow including setup,
 * round progression, and game termination.
 */
public class GameRunTest {
    private Scanner originalScanner;

    /**
     * Sets up test environment before each test.
     * Initializes game state, adds test cards, and stores original scanner.
     */
    @BeforeEach
    public void setup() {
        // Clear game state
        Game.players.clear();
        Game.selectedCharacters.clear();
        Game.districtDeck.clear();
        Game.debugMode = false;
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setGameOver(false);
        Game.setWinner(null);
        Game.setCrownPlayerIndex(0);
        Game.setAssassinatedCharacter(null);

        // Add some cards to avoid empty deck exceptions
        for (int i = 0; i < 10; i++) {
            Game.districtDeck.addCard(new DistrictCard("TestCard" + i, "blue", 1, 1, null));
        }
        
        originalScanner = Game.getScanner();
    }

    /**
     * Cleans up test environment after each test.
     * Restores original scanner.
     */
    @AfterEach
    public void tearDown() {
        Game.setScanner(originalScanner);
    }

    /**
     * Helper method to set up test input.
     * Creates scanner with simulated user input.
     * @param input the input string to simulate
     */
    private void setupTestInput(String input) {
        Game.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));
    }

    /**
     * Tests basic game execution flow.
     * Verifies:
     * 1. Game setup with 4 players
     * 2. Multiple round execution
     * 3. Resource collection
     * 4. Proper game termination
     * @throws Exception if game execution fails
     */
    @Test
    public void testRunExecutesBasicFlow() throws Exception {
        String input = String.join("\n",
                "4",         // player count
                "1",         // human picks character
                "t",         // proceed after selection
                "gold",      // collect gold
                "t",        // end turn
                "t",         // second round
                "1",         // character selection
                "t",         // proceed after selection
                "gold",      // collect gold
                "t",        // end turn
                "exit"       // exit after two rounds
        );
        setupTestInput(input);

        Game game = new Game();

        // Patch endGame() to avoid System.exit
        System.setProperty("test.env", "true");

        try {
            game.run(); // should execute without throwing
        } catch (Exception e) {
            fail("Game execution failed: " + e.getMessage());
        }

        List<Player> players = Game.players;
        assertEquals(4, players.size());
        assertTrue(players.get(0).getGold() >= 2); // At least 2 gold from first round
    }
}
