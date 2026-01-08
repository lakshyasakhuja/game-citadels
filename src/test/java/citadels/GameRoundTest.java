package citadels;

import citadels.card.CharacterCard;
import citadels.player.Player;
import citadels.player.HumanPlayer;
import citadels.player.AIPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Test suite for game round functionality.
 * Tests round progression, phase transitions, and player interactions
 * during a complete game round.
 */
public class GameRoundTest {
    private Game game;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    /**
     * Sets up test environment before each test.
     * Initializes game state, adds test players, and sets up output capture.
     */
    @BeforeEach
    void setUp() {
        game = new Game();
        // Clear any existing state
        Game.players.clear();
        Game.selectedCharacters.clear();
        Game.visibleDiscard.clear();
        
        // Add test players
        Game.players.add(new HumanPlayer("Player 1"));
        Game.players.add(new AIPlayer("Player 2"));
        Game.players.add(new AIPlayer("Player 3"));
        Game.players.add(new AIPlayer("Player 4"));
        
        // Set crown player index to 0 (first player)
        Game.setCrownPlayerIndex(0);
        
        // Capture system output
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    /**
     * Cleans up test environment after each test.
     * Restores standard output stream.
     */
    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
    }

    /**
     * Helper method to generate test input for a complete round.
     * Creates input sequence for character selection, actions, and turn completion.
     * @return String containing simulated user input
     */
    private String generateTestInput() {
        // Generate input for a complete round:
        // 1. Character selection for human player
        // 2. Multiple 't' inputs for various prompts
        // 3. Gold/cards choice
        // 4. Build actions
        StringBuilder input = new StringBuilder();
        input.append("1\n");        // Character selection
        input.append("t\n");        // Continue after selection
        input.append("gold\n");     // Choose gold action
        input.append("t\n");        // End turn
        input.append("t\nt\nt\n");  // Continue through AI turns
        return input.toString();
    }

    /**
     * Tests phase transitions during round play.
     * Verifies proper progression from selection to turn phase
     * and round state management.
     */
    @Test
    void testPlayRoundPhaseTransition() {
        // Set initial conditions
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(false);
        
        // Use comprehensive input simulation
        Game.setScanner(new Scanner(new ByteArrayInputStream(generateTestInput().getBytes())));
        
        // Start the round
        Game.playRound();
        
        // Verify phase transition
        assertEquals(Game.GamePhase.TURN, Game.getCurrentPhase());
        assertTrue(Game.isRoundInProgress());
    }

    /**
     * Tests crown player announcement at round start.
     * Verifies proper identification and announcement of
     * the crowned player.
     */
    @Test
    void testCrownPlayerAnnouncement() {
        // Set up crown player
        Player crownedPlayer = Game.players.get(0);
        Game.setCrownPlayerIndex(0);
        
        // Use comprehensive input simulation
        Game.setScanner(new Scanner(new ByteArrayInputStream(generateTestInput().getBytes())));
        
        // Start round
        Game.playRound();
        
        // Verify crown player announcement
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains(crownedPlayer.getName() + " is the crowned player"),
                  "Output should contain crown player announcement");
    }

    /**
     * Tests character selection process.
     * Verifies proper assignment and tracking of
     * selected characters for all players.
     */
    @Test
    void testCharacterSelectionProcess() {
        // Set initial phase
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(false);
        
        // Build input for all players plus additional prompts
        StringBuilder input = new StringBuilder();
        input.append(generateTestInput());
        
        Game.setScanner(new Scanner(new ByteArrayInputStream(input.toString().getBytes())));
        
        // Start round
        Game.playRound();
        
        // Verify character selection results
        assertFalse(Game.selectedCharacters.isEmpty(), "Selected characters should not be empty");
        assertTrue(Game.selectedCharacters.size() > 0, "At least one character should be selected");
    }

    /**
     * Tests round progress state management.
     * Verifies proper tracking of round state and
     * phase transitions throughout round execution.
     */
    @Test
    void testRoundProgressStateManagement() {
        // Set initial conditions
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(false);
        
        // Use comprehensive input simulation
        Game.setScanner(new Scanner(new ByteArrayInputStream(generateTestInput().getBytes())));
        
        // Start round
        Game.playRound();
        
        // Verify round progress state
        assertTrue(Game.isRoundInProgress(), "Round should be in progress");
        assertEquals(Game.GamePhase.TURN, Game.getCurrentPhase(), "Should be in TURN phase");
    }
} 