package citadels;

import citadels.card.CharacterCard;
import citadels.card.DistrictCard;
import citadels.player.HumanPlayer;
import citadels.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for character abilities and income mechanics.
 * Tests each character's special abilities and district-based
 * income generation.
 */
public class CharacterAbilitiesTest {
    private Player player;
    private Scanner originalScanner;

    /**
     * Sets up test environment before each test.
     * Initializes game state, creates test player,
     * and sets test environment property.
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
        Game.setCurrentPhase(Game.GamePhase.TURN);
        Game.setCrownPlayerIndex(0);
        Game.setGameOver(false);
        Game.setWinner(null);
        
        // Create test player
        player = new HumanPlayer("Test Player");
        Game.players.add(player);
        
        originalScanner = Game.getScanner();
        System.setProperty("test.env", "true"); // Prevent System.exit in tests
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
     * Tests King's income generation.
     * Verifies:
     * 1. Income from yellow districts
     * 2. Base gold action
     * 3. Total gold calculation
     */
    @Test
    public void testKingIncome() {
        // Give player yellow districts
        player.getCity().add(new DistrictCard("Castle", "yellow", 4, 1, null));
        player.getCity().add(new DistrictCard("Palace", "yellow", 5, 1, null));
        
        // Set player as King
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        GameState.setCurrentPlayer(player);
        GameState.setCurrentCharacter(king);
        
        int initialGold = player.getGold();
        setupTestInput("gold\nt\n");  // Take gold action, end turn
        Game.handlePlayerTurn(player, king);
        
        assertEquals(initialGold + 4, player.getGold(), "King should get 2 gold from yellow districts plus 2 from gold action");
    }

    /**
     * Tests Bishop's income generation.
     * Verifies:
     * 1. Income from blue districts
     * 2. Base gold action
     * 3. Total gold calculation
     */
    @Test
    public void testBishopIncome() {
        // Give player blue districts
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        player.getCity().add(new DistrictCard("Church", "blue", 3, 1, null));
        
        // Set player as Bishop
        CharacterCard bishop = new CharacterCard("Bishop", 5);
        Game.selectedCharacters.put(player, bishop);
        GameState.setCurrentPlayer(player);
        GameState.setCurrentCharacter(bishop);
        
        int initialGold = player.getGold();
        setupTestInput("gold\nt\n");  // Take gold action, end turn
        Game.handlePlayerTurn(player, bishop);
        
        assertEquals(initialGold + 4, player.getGold(), "Bishop should get 2 gold from blue districts plus 2 from gold action");
    }

    /**
     * Tests Merchant's income generation.
     * Verifies:
     * 1. Income from green districts
     * 2. Merchant bonus gold
     * 3. Base gold action
     * 4. Total gold calculation
     */
    @Test
    public void testMerchantIncome() {
        // Give player green districts
        player.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        player.getCity().add(new DistrictCard("Shop", "green", 2, 1, null));
        
        // Set player as Merchant
        CharacterCard merchant = new CharacterCard("Merchant", 6);
        Game.selectedCharacters.put(player, merchant);
        GameState.setCurrentPlayer(player);
        GameState.setCurrentCharacter(merchant);
        
        int initialGold = player.getGold();
        setupTestInput("gold\nt\n");  // Take gold action, end turn
        Game.handlePlayerTurn(player, merchant);
        
        assertEquals(initialGold + 5, player.getGold(), "Merchant should get 2 gold from green districts plus 1 bonus plus 2 from gold action");
    }

    /**
     * Tests Warlord's income generation.
     * Verifies:
     * 1. Income from red districts
     * 2. Base gold action
     * 3. Total gold calculation
     */
    @Test
    public void testWarlordIncome() {
        // Give player red districts
        player.getCity().add(new DistrictCard("Fortress", "red", 5, 1, null));
        player.getCity().add(new DistrictCard("Prison", "red", 2, 1, null));
        
        // Set player as Warlord
        CharacterCard warlord = new CharacterCard("Warlord", 8);
        Game.selectedCharacters.put(player, warlord);
        GameState.setCurrentPlayer(player);
        GameState.setCurrentCharacter(warlord);
        
        int initialGold = player.getGold();
        setupTestInput("gold\nt\n");  // Take gold action, end turn
        Game.handlePlayerTurn(player, warlord);
        
        assertEquals(initialGold + 4, player.getGold(), "Warlord should get 2 gold from red districts plus 2 from gold action");
    }

    /**
     * Tests Thief's steal ability.
     * Verifies:
     * 1. Target selection
     * 2. Gold transfer
     * 3. Base gold action
     * 4. Victim gold reduction
     */
    @Test
    public void testThiefSteal() {
        // Create victim player
        Player victim = new HumanPlayer("Victim");
        victim.addGold(5);
        Game.players.add(victim);
        Game.selectedCharacters.put(victim, new CharacterCard("Bishop", 5));
        
        // Set player as Thief
        CharacterCard thief = new CharacterCard("Thief", 2);
        Game.selectedCharacters.put(player, thief);
        GameState.setCurrentPlayer(player);
        GameState.setCurrentCharacter(thief);
        
        int initialGold = player.getGold();
        setupTestInput("action Bishop\ngold\nt\n");  // Steal from Bishop, take gold action, end turn
        Game.handlePlayerTurn(player, thief);
        
        assertEquals(initialGold + 7, player.getGold(), "Thief should get 5 gold from victim plus 2 from gold action");
        assertEquals(0, victim.getGold(), "Victim should have no gold after theft");
    }
} 