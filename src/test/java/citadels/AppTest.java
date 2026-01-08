package citadels;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the App class, which is the main entry point of the Citadels game.
 * Tests cover basic initialization, game setup, and complete game rounds.
 */
public class AppTest {
    private Scanner originalScanner;

    /**
     * Sets up the test environment before each test.
     * - Stores the original scanner
     * - Sets test environment property
     * - Resets all game state variables
     */
    @BeforeEach
    public void setUp() {
        originalScanner = Game.getScanner();
        // Set test environment property to prevent System.exit
        System.setProperty("test.env", "true");
        
        // Reset game state
        Game.players.clear();
        Game.selectedCharacters.clear();
        Game.districtDeck.clear();
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setGameOver(false);
        Game.setWinner(null);
    }

    /**
     * Cleans up after each test by restoring the original scanner.
     */
    @AfterEach
    public void tearDown() {
        Game.setScanner(originalScanner);
    }

    /**
     * Helper method to set up test input simulation.
     * Replaces the game's scanner with a new one that reads from the provided input string.
     * @param input The simulated input string to be fed to the game
     */
    private void setupTestInput(String input) {
        Game.setScanner(new Scanner(new ByteArrayInputStream(input.replace("\\n", "\n").getBytes())));
    }

    /**
     * Tests the basic constructor of the App class.
     * Verifies that a new App instance can be created successfully.
     */
    @Test
    public void testConstructor() {
        App app = new App();
        assertNotNull(app, "App instance should be created");
    }

    /**
     * Tests the main game loop with a quick single-round game.
     * Simulates:
     * - Setting up a 4-player game
     * - Character selection phase
     * - One complete round of turns
     * - Game exit
     * Verifies that players are created and the round completes properly.
     */
    @Test
    public void testMainMethod() {
        // Setup input for a quick game that ends after one round
        String input = "4\n" +  // 4 players
                      "1\n1\n1\n1\n" +  // Character selection
                      "t\n" +  // Start turn phase
                      "gold\n" +  // Take gold action
                      "t\n" +  // End turn
                      "t\n" +  // Next character (AI player 2)
                      "t\n" +  // Next character (AI player 3)
                      "t\n" +  // Next character (AI player 4)
                      "t\n" +  // End round
                      "exit\n";  // Exit game
        setupTestInput(input);
        
        // Run main method
        App.main(new String[]{});
        
        // Verify game was initialized and run
        assertTrue(Game.players.size() > 0, "Game should have players");
        assertFalse(Game.isRoundInProgress(), "Round should be complete");
    }

    /**
     * Tests a complete game round with more detailed interactions.
     * Simulates:
     * - 4-player game setup
     * - Character selection for player 1
     * - Complete turn execution including gold collection
     * - AI player turns
     * - Round completion
     * Verifies proper player count, game phase, and round completion status.
     */
    @Test
    public void testMainMethodWithFullRound() {
        // Setup input for a complete round
        String input = "4\n" +  // 4 players
                      // Character selection phase
                      "1\n" +   // Player 1 chooses character 1
                      "t\n" +   // Confirm selection
                      // Turn phase
                      "gold\n" + // Take gold action
                      "t\n" +    // End turn
                      // AI turns
                      "t\nt\nt\n" + // Skip through AI turns
                      // End round
                      "t\n" +    // Complete round
                      "exit\n";  // Exit game
        setupTestInput(input);
        
        // Run main method
        App.main(new String[]{});
        
        // Verify game state after a complete round
        assertEquals(4, Game.players.size(), "Should have 4 players");
        assertEquals(Game.GamePhase.ROUND_END, Game.getCurrentPhase(), "Should be in round end phase");
        assertFalse(Game.isRoundInProgress(), "Round should be complete");
    }
}
