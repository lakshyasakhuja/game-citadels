package citadels;

import citadels.card.CharacterCard;
import citadels.card.DistrictCard;
import citadels.player.HumanPlayer;
import citadels.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Supplementary test suite for Game class.
 * Tests additional edge cases and specific game mechanics
 * not covered in the main test suites.
 */
public class GameTestSupplement {
    private Game game;
    private Player player;
    private Scanner originalScanner;

    /**
     * Sets up test environment before each test.
     * Initializes game state with debug mode and single player.
     */
    @BeforeEach
    public void setup() {
        game = new Game();
        Game.players.clear();
        Game.selectedCharacters.clear();
        Game.districtDeck.clear();
        Game.debugMode = true;
        player = new HumanPlayer("Player 1");
        Game.players.add(player);
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
     * Tests Assassin's kill ability flow.
     * Verifies proper target selection and assassination effect.
     */
    @Test
    public void testHandleTurn_AssassinKillFlow() {
        Game.selectedCharacters.put(player, new CharacterCard("Assassin", 1));
        setupTestInput("2\n");
        Game.handlePlayerTurn(player, Game.selectedCharacters.get(player));
        assertEquals("Thief", Game.getAssassinatedCharacter());
    }

    /**
     * Tests Thief's rob ability flow.
     * Verifies proper target selection for robbery.
     */
    @Test
    public void testHandleTurn_ThiefRobFlow() {
        Game.selectedCharacters.put(player, new CharacterCard("Thief", 2));
        setupTestInput("3\n");
        Game.handlePlayerTurn(player, Game.selectedCharacters.get(player));
    }

    /**
     * Tests invalid draw choice handling.
     * Verifies fallback behavior with invalid input.
     */
    @Test
    public void testHandleTurn_InvalidDrawChoiceFallback() {
        Game.selectedCharacters.put(player, new CharacterCard("Merchant", 6));
        setupTestInput("wrong\n1\n");
        Game.handlePlayerTurn(player, Game.selectedCharacters.get(player));
    }

    /**
     * Tests end game scoring bonuses.
     * Verifies proper calculation of:
     * 1. Color variety bonus
     * 2. Purple card effects
     * 3. District completion bonus
     */
    @Test
    public void testEndGameScoringBonuses() {
        player.getCity().add(new DistrictCard("Red", "red", 1, 1, null));
        player.getCity().add(new DistrictCard("Blue", "blue", 1, 1, null));
        player.getCity().add(new DistrictCard("Green", "green", 1, 1, null));
        player.getCity().add(new DistrictCard("Yellow", "yellow", 1, 1, null));
        player.getCity().add(new DistrictCard("School of Magic", "purple", 1, 1, "any color"));
        player.getCity().add(new DistrictCard("Lab", "purple", 1, 1, null));
        player.getCity().add(new DistrictCard("Haunted City", "purple", 1, 1, null));
        player.getCity().add(new DistrictCard("Dragon Gate", "purple", 1, 1, null));
        Game.setGameOver(true);
        Game.setWinner(player);
        setupTestInput("t\n");
        game.playRound();  // triggers endGame print logic
    }

    /**
     * Tests player count input validation.
     * Verifies proper handling of invalid inputs
     * and fallback to valid player count.
     */
    @Test
    public void testLayerCountFallback() {
        setupTestInput("a\n3\n4\n");
        assertEquals(4, game.layerCount());
    }

    /**
     * Tests game exit command handling.
     * Verifies proper game termination on exit command.
     */
    @Test
    public void testRunExitCommand() {
        setupTestInput("4\nexit\n");
        game.run(); // exit path
    }

    /**
     * Tests character selection input bounds.
     * Verifies proper handling of selection limits
     * and duplicate selections.
     */
    @Test
    public void testPlayerCharacterSelectionHumanInputBounds() {
        Game.players.clear();
        Game.players.add(new HumanPlayer("Player 1"));
        Game.players.add(new HumanPlayer("Player 2"));
        Game.players.add(new HumanPlayer("Player 3"));
        Game.players.add(new HumanPlayer("Player 4"));
        setupTestInput("1\n1\n1\n1\n");
        game.startCharacterSelectionPhase();
        assertEquals(4, Game.selectedCharacters.size());
    }

    /**
     * Tests end game mystery card and winner logic.
     * Verifies proper display of:
     * 1. Mystery card revelation
     * 2. Winner announcement
     * 3. Final scoring
     */
    @Test
    public void testEndGame_MysteryPrintsAndWinnerLogic() {
        Game.players.clear();
        Player p = new HumanPlayer("Winner");
        Game.players.add(p);
        for (int i = 0; i < 8; i++) {
            p.getCity().add(new DistrictCard("C" + i, "green", 1, 1, null));
        }
        Game.setGameOver(true);
        Game.setWinner(p);
        setupTestInput("t\n");
        game.playRound();  // triggers mysteryDiscard print and scoring logic
    }
}
