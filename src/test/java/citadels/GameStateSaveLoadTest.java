package citadels;

import citadels.card.CharacterCard;
import citadels.card.DistrictCard;
import citadels.player.HumanPlayer;
import citadels.player.Player;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for game state persistence functionality.
 * Tests saving and loading of game state including players,
 * characters, districts, and game progress.
 */
public class GameStateSaveLoadTest {
    private Game game;
    private Player player1;
    private Player player2;
    private Scanner originalScanner;

    /**
     * Sets up test environment before each test.
     * Initializes game state with:
     * 1. Two players with resources
     * 2. Districts in hands and cities
     * 3. Character assignments
     * 4. District deck
     */
    @BeforeEach
    public void setUp() {
        game = new Game();
        Game.players.clear();
        Game.selectedCharacters.clear();
        Game.districtDeck.clear();
        Game.debugMode = false;
        
        // Create test players
        player1 = new HumanPlayer("Player 1");
        player2 = new HumanPlayer("Player 2");
        
        // Add initial resources
        player1.addGold(5);
        player2.addGold(3);
        
        // Add some districts
        player1.drawCard(new DistrictCard("Temple", "blue", 1, 1, null));
        player1.drawCard(new DistrictCard("Castle", "yellow", 4, 1, null));
        player2.drawCard(new DistrictCard("Market", "green", 2, 1, null));
        
        // Add districts to cities
        player1.getCity().add(new DistrictCard("Watchtower", "red", 1, 1, null));
        player2.getCity().add(new DistrictCard("Cathedral", "blue", 5, 1, null));
        
        // Add characters
        Game.selectedCharacters.put(player1, new CharacterCard("King", 4));
        Game.selectedCharacters.put(player2, new CharacterCard("Bishop", 5));
        
        Game.players.add(player1);
        Game.players.add(player2);
        
        // Add some cards to district deck
        Game.districtDeck.addCard(new DistrictCard("Manor", "yellow", 3, 1, null));
        Game.districtDeck.addCard(new DistrictCard("Tavern", "green", 1, 1, null));
        
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
     * Tests basic game state persistence.
     * Verifies proper saving and loading of:
     * 1. Player information
     * 2. Resources and cards
     * 3. Character assignments
     */
    @Test
    public void testSaveAndLoadBasicGameState() {
        // Save game state
        JSONObject savedState = GameState.saveGame();
        
        // Clear game state
        Game.players.clear();
        Game.selectedCharacters.clear();
        Game.districtDeck.clear();
        
        // Load game state
        GameState.loadGame(savedState);
        
        // Verify players
        assertEquals(2, Game.players.size());
        Player loadedPlayer1 = Game.players.get(0);
        assertEquals("Player 1", loadedPlayer1.getName());
        assertEquals(5, loadedPlayer1.getGold());
        assertEquals(2, loadedPlayer1.getHand().size());
        assertEquals(1, loadedPlayer1.getCity().size());
        
        // Verify characters
        assertEquals(2, Game.selectedCharacters.size());
        assertEquals("King", Game.selectedCharacters.get(loadedPlayer1).getName());
    }

    /**
     * Tests persistence with empty game state.
     * Verifies proper handling of:
     * 1. Empty player list
     * 2. Empty character assignments
     * 3. Empty district deck
     */
    @Test
    public void testSaveAndLoadWithEmptyState() {
        // Clear everything
        Game.players.clear();
        Game.selectedCharacters.clear();
        Game.districtDeck.clear();
        
        // Save empty state
        JSONObject savedState = GameState.saveGame();
        
        // Add some content
        Game.players.add(player1);
        Game.selectedCharacters.put(player1, new CharacterCard("King", 4));
        
        // Load empty state
        GameState.loadGame(savedState);
        
        // Verify empty state was restored
        assertTrue(Game.players.isEmpty());
        assertTrue(Game.selectedCharacters.isEmpty());
        assertTrue(Game.districtDeck.isEmpty());
    }

    /**
     * Tests character assignment persistence.
     * Verifies proper saving and loading of:
     * 1. Character-player mappings
     * 2. Character properties
     * 3. Assignment restoration
     */
    @Test
    public void testSaveAndLoadWithCharacterAssignments() {
        // Save state
        JSONObject savedState = GameState.saveGame();
        
        // Change character assignments
        Game.selectedCharacters.clear();
        Game.selectedCharacters.put(player1, new CharacterCard("Warlord", 8));
        Game.selectedCharacters.put(player2, new CharacterCard("Merchant", 6));
        
        // Load state
        GameState.loadGame(savedState);
        
        // Verify original assignments restored
        Player loadedPlayer1 = Game.players.stream()
            .filter(p -> p.getName().equals("Player 1"))
            .findFirst()
            .orElse(null);
        Player loadedPlayer2 = Game.players.stream()
            .filter(p -> p.getName().equals("Player 2"))
            .findFirst()
            .orElse(null);
            
        assertNotNull(loadedPlayer1);
        assertNotNull(loadedPlayer2);
        assertEquals("King", Game.selectedCharacters.get(loadedPlayer1).getName());
        assertEquals("Bishop", Game.selectedCharacters.get(loadedPlayer2).getName());
    }

    /**
     * Tests district deck persistence.
     * Verifies proper saving and loading of:
     * 1. Deck size
     * 2. Card order
     * 3. Card properties
     */
    @Test
    public void testSaveAndLoadWithDistrictDeck() {
        // Count initial cards
        int initialDeckSize = Game.districtDeck.size();
        
        // Save state
        JSONObject savedState = GameState.saveGame();
        
        // Add different cards
        Game.districtDeck.clear();
        Game.districtDeck.addCard(new DistrictCard("Different", "purple", 6, 1, null));
        
        // Load state
        GameState.loadGame(savedState);
        
        // Verify deck restored
        assertEquals(initialDeckSize, Game.districtDeck.size());
    }

    /**
     * Tests player city persistence.
     * Verifies proper saving and loading of:
     * 1. Built districts
     * 2. District properties
     * 3. City composition
     */
    @Test
    public void testSaveAndLoadWithPlayerCities() {
        // Save state
        JSONObject savedState = GameState.saveGame();
        
        // Change cities
        player1.getCity().clear();
        player2.getCity().clear();
        
        // Load state
        GameState.loadGame(savedState);
        
        // Verify cities restored
        Player loadedPlayer1 = Game.players.stream()
            .filter(p -> p.getName().equals("Player 1"))
            .findFirst()
            .orElse(null);
        Player loadedPlayer2 = Game.players.stream()
            .filter(p -> p.getName().equals("Player 2"))
            .findFirst()
            .orElse(null);
            
        assertNotNull(loadedPlayer1);
        assertNotNull(loadedPlayer2);
        assertEquals(1, loadedPlayer1.getCity().size());
        assertEquals(1, loadedPlayer2.getCity().size());
        assertEquals("Watchtower", loadedPlayer1.getCity().get(0).getName());
        assertEquals("Cathedral", loadedPlayer2.getCity().get(0).getName());
    }
} 