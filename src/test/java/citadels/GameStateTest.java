package citadels;

import citadels.card.CharacterCard;
import citadels.player.HumanPlayer;
import citadels.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for GameState class.
 * Tests management of current player and character state
 * during game execution.
 */
public class GameStateTest {
    private Player player;
    private CharacterCard character;
    private Scanner originalScanner;

    /**
     * Sets up test environment before each test.
     * Initializes game state, creates test player and character,
     * and sets up test environment property.
     */
    @BeforeEach
    public void setUp() {
        // Reset game state
        Game.players.clear();
        Game.selectedCharacters.clear();
        Game.districtDeck.clear();
        Game.visibleDiscard.clear();
        Game.setAssassinatedCharacter(null);
        Game.setRobbedCharacter(null);
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setCrownPlayerIndex(0);
        Game.setGameOver(false);
        Game.setWinner(null);
        
        // Create test player and character
        player = new HumanPlayer("Test Player");
        character = new CharacterCard("King", 4);
        Game.players.add(player);
        
        originalScanner = Game.getScanner();
        System.setProperty("test.env", "true"); // Prevent System.exit in tests
    }

    /**
     * Cleans up test environment after each test.
     * Restores original scanner and clears game state.
     */
    @AfterEach
    public void tearDown() {
        Game.setScanner(originalScanner);
        GameState.setCurrentPlayer(null);
        GameState.setCurrentCharacter(null);
    }

    /**
     * Tests current player state management.
     * Verifies proper setting and clearing of current player,
     * including null state handling.
     */
    @Test
    public void testCurrentPlayerManagement() {
        assertNull(GameState.getCurrentPlayer(), "Current player should be null initially");
        
        GameState.setCurrentPlayer(player);
        assertEquals(player, GameState.getCurrentPlayer(), "Current player should be set correctly");
        
        GameState.setCurrentPlayer(null);
        assertNull(GameState.getCurrentPlayer(), "Current player should be null after clearing");
    }

    /**
     * Tests current character state management.
     * Verifies proper setting and clearing of current character,
     * including null state handling.
     */
    @Test
    public void testCurrentCharacterManagement() {
        assertNull(GameState.getCurrentCharacter(), "Current character should be null initially");
        
        GameState.setCurrentCharacter(character);
        assertEquals(character, GameState.getCurrentCharacter(), "Current character should be set correctly");
        
        GameState.setCurrentCharacter(null);
        assertNull(GameState.getCurrentCharacter(), "Current character should be null after clearing");
    }
}
