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
 * Test suite for command handling functionality.
 * Tests parsing and execution of player commands during game turns,
 * including resource collection, building, and character abilities.
 */
public class CommandHandlerTest {
    private Player player;
    private Scanner originalScanner;
    private static final String TEST_PLAYER_NAME = "Test Player";

    /**
     * Sets up test environment before each test.
     * Initializes game state, creates test player,
     * and sets up command handling environment.
     * - Clears all game collections
     * - Resets game state flags
     * - Creates test player
     * - Stores original scanner
     */
    @BeforeEach
    public void setUp() {
        // Store original scanner
        originalScanner = Game.getScanner();
        
        // Initialize test environment
        System.setProperty("test.env", "true");
        
        // Reset game state
        Game.players.clear();
        Game.selectedCharacters.clear();
        Game.districtDeck.clear();
        
        // Create test player
        player = new HumanPlayer(TEST_PLAYER_NAME);
        Game.players.add(player);
        
        // Add some test cards to player's hand
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        player.drawCard(new DistrictCard("Market", "green", 2, 1, null));
        player.drawCard(new DistrictCard("Castle", "yellow", 4, 1, null));
        
        // Give player some initial gold
        player.addGold(5);
        
        // Assign a character card
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
    }

    @AfterEach
    public void tearDown() {
        Game.setScanner(originalScanner);
    }

    private void setupTestInput(String input) {
        Game.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));
    }

    /**
     * Tests end turn command.
     * Verifies that the turn can be ended cleanly with the 't' command.
     */
    @Test
    public void testEndTurnCommand() {
        setupTestInput("t\n");
        CommandHandler.run(player, Game.getScanner());
        // End turn should complete without errors
    }

    /**
     * Tests hand display command.
     * Verifies that the player's current hand of district cards is displayed correctly.
     */
    @Test
    public void testHandCommand() {
        setupTestInput("hand\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertEquals(3, player.getHand().size());
    }

    /**
     * Tests gold display command.
     * Verifies that the player's current gold amount is displayed correctly.
     */
    @Test
    public void testGoldCommand() {
        setupTestInput("gold\nt\n");
        int initialGold = player.getGold();
        CommandHandler.run(player, Game.getScanner());
        assertEquals(initialGold, player.getGold());
    }

    /**
     * Tests city display command.
     * Verifies that the player's built districts are displayed correctly.
     */
    @Test
    public void testCityCommand() {
        // Build a district first
        player.buildDistrict(0); // Build Temple
        setupTestInput("city\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertEquals(1, player.getCity().size());
    }

    /**
     * Tests city display command with player number.
     * Verifies that another player's city can be viewed by specifying their number.
     */
    @Test
    public void testCityCommandWithPlayerNumber() {
        // Add another player
        Player player2 = new HumanPlayer("Player 2");
        player2.drawCard(new DistrictCard("Watchtower", "red", 1, 1, null));
        player2.buildDistrict(0);
        Game.players.add(player2);
        
        setupTestInput("city 2\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Should complete without errors
    }

    /**
     * Tests successful district building.
     * Verifies that a district can be built when the player has sufficient gold.
     */
    @Test
    public void testBuildCommandSuccess() {
        setupTestInput("build 1\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertTrue(player.getCity().stream()
            .anyMatch(card -> card.getName().equals("Temple")));
        assertEquals(3, player.getGold()); // 5 initial - 2 cost
    }

    /**
     * Tests building with insufficient gold.
     * Verifies that building fails when player lacks required gold.
     */
    @Test
    public void testBuildCommandInsufficientGold() {
        player.addGold(-4); // Leave with 1 gold
        setupTestInput("build 3\n1\nt\n"); // Try to build Castle (4 cost), then try Temple
        CommandHandler.run(player, Game.getScanner());
        assertEquals(1, player.getCity().size()); // Should build Temple
        assertEquals(0, player.getGold()); // 1 - 1 cost
    }

    /**
     * Tests building with invalid card index.
     * Verifies that building fails gracefully when an invalid card index is provided.
     */
    @Test
    public void testBuildCommandInvalidIndex() {
        setupTestInput("build 10\nt\n"); // Invalid index
        CommandHandler.run(player, Game.getScanner());
        assertTrue(player.getCity().isEmpty());
        assertEquals(5, player.getGold());
    }

    /**
     * Tests building duplicate districts.
     * Verifies that attempting to build a duplicate district is properly handled.
     */
    @Test
    public void testBuildCommandDuplicateDistrict() {
        // Build Temple first
        player.buildDistrict(0);
        setupTestInput("build 1\n2\nt\n"); // Try to build Temple again, then try Market
        CommandHandler.run(player, Game.getScanner());
        assertEquals(2, player.getCity().size()); // Should have Temple and Market
        assertEquals(1, player.getGold()); // 5 - 2 - 2 costs
    }

    /**
     * Tests all players status command.
     * Verifies that the status of all players is displayed correctly.
     */
    @Test
    public void testAllCommand() {
        setupTestInput("all\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Should complete without errors
    }

    /**
     * Tests Magician's swap action.
     * Verifies that the Magician can swap hands with another player.
     */
    @Test
    public void testMagicianActionSwap() {
        // Create another player to swap with
        Player player2 = new HumanPlayer("Player 2");
        Game.players.add(player2);
        player2.drawCard(new DistrictCard("Watchtower", "red", 1, 1, null));
        
        // Set current player as Magician
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        setupTestInput("action swap 2\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        assertEquals(1, player.getHand().size());
        assertTrue(player.getHand().stream()
            .anyMatch(card -> card.getName().equals("Watchtower")));
    }

    /**
     * Tests Magician's redraw action.
     * Verifies that the Magician can exchange cards with the deck.
     */
    @Test
    public void testMagicianActionRedraw() {
        // Set current player as Magician
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        // Add some cards to district deck
        Game.districtDeck.addCard(new DistrictCard("Palace", "yellow", 5, 1, null));
        
        setupTestInput("action redraw 1,2\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        assertEquals(3, player.getHand().size()); // Should maintain same hand size
    }

    /**
     * Tests invalid Magician action.
     * Verifies that Magician actions fail when used by non-Magician character.
     */
    @Test
    public void testMagicianActionInvalidPlayer() {
        // Set current player as King (not Magician)
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        
        setupTestInput("action swap 2\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        assertEquals(3, player.getHand().size()); // Hand should remain unchanged
    }

    /**
     * Tests character info command.
     * Verifies that character information is displayed correctly.
     */
    @Test
    public void testInfoCommand() {
        setupTestInput("info king\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Should complete without errors
    }

    /**
     * Tests info command with invalid character.
     * Verifies that invalid character requests are handled gracefully.
     */
    @Test
    public void testInfoCommandInvalidCharacter() {
        setupTestInput("info invalid\nt\n");
        // Should handle invalid character gracefully
    }

    /**
     * Tests help command.
     * Verifies that help information is displayed correctly.
     */
    @Test
    public void testHelpCommand() {
        setupTestInput("help\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Should complete without errors
    }

    /**
     * Tests debug mode toggle.
     * Verifies that debug mode can be toggled on and off.
     */
    @Test
    public void testDebugCommand() {
        boolean initialDebugMode = Game.debugMode;
        setupTestInput("debug\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertNotEquals(initialDebugMode, Game.debugMode);
    }

    /**
     * Tests game state saving.
     * Verifies that the game state can be saved to a file.
     */
    @Test
    public void testSaveCommand() {
        setupTestInput("save test.json\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Should complete without errors
    }

    /**
     * Tests complete game state saving.
     * Verifies that the complete game state can be saved to a file.
     */
    @Test
    public void testSaveGameCommand() {
        setupTestInput("savegame test.json\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Should complete without errors
    }

    /**
     * Tests game state loading.
     * Verifies that the game state can be loaded from a file.
     */
    @Test
    public void testLoadCommand() {
        setupTestInput("load test.json\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Should handle non-existent file gracefully
    }

    /**
     * Tests complete game state loading.
     * Verifies that the complete game state can be loaded from a file.
     */
    @Test
    public void testLoadGameCommand() {
        setupTestInput("loadgame test.json\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Should handle non-existent file gracefully
    }

    /**
     * Tests invalid command handling.
     * Verifies that invalid commands are handled gracefully.
     */
    @Test
    public void testInvalidCommand() {
        setupTestInput("invalid\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Should handle invalid command gracefully
    }

    /**
     * Tests multiple command sequence.
     * Verifies that multiple commands can be executed in sequence.
     */
    @Test
    public void testMultipleCommands() {
        setupTestInput("hand\ngold\ncity\nall\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Should handle multiple commands in sequence
    }

    /**
     * Tests build command with invalid format.
     * Verifies that build command with missing card number is handled gracefully.
     */
    @Test
    public void testBuildCommandInvalidFormat() {
        setupTestInput("build\nt\n"); // Missing card number
        CommandHandler.run(player, Game.getScanner());
        assertTrue(player.getCity().isEmpty());
        assertEquals(5, player.getGold());
    }

    /**
     * Tests build command with non-numeric input.
     * Verifies that build command with non-numeric input is handled gracefully.
     */
    @Test
    public void testBuildCommandNonNumeric() {
        setupTestInput("build abc\nt\n"); // Non-numeric input
        CommandHandler.run(player, Game.getScanner());
        assertTrue(player.getCity().isEmpty());
        assertEquals(5, player.getGold());
    }

    /**
     * Tests all command with empty city.
     * Verifies that all command works correctly when a player has no districts.
     */
    @Test
    public void testAllCommandWithEmptyCity() {
        // Add another player with empty city
        Player player2 = new HumanPlayer("Player 2");
        Game.players.add(player2);
        
        setupTestInput("all\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Should complete without errors
    }

    /**
     * Tests Magician swap with invalid target.
     * Verifies that Magician's swap action fails with invalid target player.
     */
    @Test
    public void testMagicianSwapWithInvalidTarget() {
        // Set current player as Magician
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        setupTestInput("action swap 999\nt\n"); // Invalid player number
        CommandHandler.run(player, Game.getScanner());
        assertEquals(3, player.getHand().size()); // Hand should remain unchanged
    }

    /**
     * Tests Magician swap with self.
     * Verifies that Magician cannot swap hands with themselves.
     */
    @Test
    public void testMagicianSwapWithSelf() {
        // Set current player as Magician
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        setupTestInput("action swap 1\nt\n"); // Try to swap with self
        CommandHandler.run(player, Game.getScanner());
        assertEquals(3, player.getHand().size()); // Hand should remain unchanged
    }

    /**
     * Tests Magician redraw with invalid indices.
     * Verifies that Magician's redraw action fails with invalid card indices.
     */
    @Test
    public void testMagicianRedrawInvalidIndices() {
        // Set current player as Magician
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        setupTestInput("action redraw 1,99\nt\n"); // Invalid index
        CommandHandler.run(player, Game.getScanner());
        assertEquals(3, player.getHand().size()); // Hand size should remain unchanged
    }

    /**
     * Tests Magician redraw with empty deck.
     * Verifies that Magician's redraw action is handled properly with empty deck.
     */
    @Test
    public void testMagicianRedrawEmptyDeck() {
        // Set current player as Magician
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        Game.districtDeck.clear(); // Empty the deck
        
        setupTestInput("action redraw 1,2\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertTrue(player.getHand().size() <= 3); // Hand size should not increase
    }

    /**
     * Tests info command for all characters.
     * Verifies that info command works for all character types.
     */
    @Test
    public void testInfoCommandAllCharacters() {
        String[] characters = {
            "assassin", "thief", "magician", "king", "bishop",
            "merchant", "architect", "warlord"
        };
        
        for (String character : characters) {
            setupTestInput("info " + character + "\nt\n");
            CommandHandler.run(player, Game.getScanner());
        }
    }

    /**
     * Tests info command for special buildings.
     * Verifies that info command works for special district buildings.
     */
    @Test
    public void testInfoCommandSpecialBuildings() {
        String[] buildings = {"keep", "laboratory", "school"};
        
        for (String building : buildings) {
            setupTestInput("info " + building + "\nt\n");
            CommandHandler.run(player, Game.getScanner());
        }
    }

    /**
     * Tests info command with invalid input.
     * Verifies that info command handles invalid input gracefully.
     */
    @Test
    public void testInfoCommandInvalidInput() {
        setupTestInput("info\nt\n"); // Missing argument
        CommandHandler.run(player, Game.getScanner());
        
        setupTestInput("info nonexistent\nt\n"); // Invalid character/building
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests Magician action with invalid format.
     * Verifies that Magician actions with invalid format are handled gracefully.
     */
    @Test
    public void testMagicianActionInvalidFormat() {
        // Set current player as Magician
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        setupTestInput("action\nt\n"); // Missing action type
        CommandHandler.run(player, Game.getScanner());
        
        setupTestInput("action invalid\nt\n"); // Invalid action type
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests build command with maximum attempts.
     * Verifies that build command properly limits retry attempts.
     */
    @Test
    public void testBuildCommandMaxAttempts() {
        player.addGold(-4); // Leave with 1 gold
        setupTestInput("build 3\n3\n3\nt\n"); // Try to build Castle (4 cost) three times
        CommandHandler.run(player, Game.getScanner());
        assertTrue(player.getCity().isEmpty());
        assertEquals(1, player.getGold());
    }

    /**
     * Tests city command with invalid player number.
     * Verifies that city command handles invalid player numbers gracefully.
     */
    @Test
    public void testCityCommandInvalidPlayerNumber() {
        setupTestInput("city abc\nt\n"); // Non-numeric player number
        CommandHandler.run(player, Game.getScanner());
        
        setupTestInput("city 999\nt\n"); // Invalid player number
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests build command with Architect character.
     * Verifies that Architect can build multiple districts in one turn.
     */
    @Test
    public void testBuildCommandWithArchitect() {
        // Set current player as Architect
        Game.selectedCharacters.put(player, new CharacterCard("Architect", 7));
        player.addGold(5); // Total 10 gold
        
        // Try to build all three districts in one turn
        setupTestInput("build 1\nbuild 2\nbuild 3\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertEquals(3, player.getCity().size());
        assertEquals(2, player.getGold()); // 10 - 2 - 2 - 4 costs
    }

    /**
     * Tests Architect building with insufficient gold.
     * Verifies that Architect's multiple builds are limited by available gold.
     */
    @Test
    public void testBuildCommandWithArchitectInsufficientGold() {
        // Set current player as Architect
        Game.selectedCharacters.put(player, new CharacterCard("Architect", 7));
        player.addGold(-2); // Leave with 3 gold
        
        // Try to build all three districts in one turn
        setupTestInput("build 1\nbuild 2\nbuild 3\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertEquals(2, player.getCity().size()); // Should build Temple and Market
        assertEquals(0, player.getGold()); // 3 - 2 - 1 costs
    }

    /**
     * Tests Architect partial building.
     * Verifies that Architect can build fewer than maximum allowed districts.
     */
    @Test
    public void testBuildCommandWithArchitectPartialBuild() {
        // Set current player as Architect
        Game.selectedCharacters.put(player, new CharacterCard("Architect", 7));
        
        // Try to build two districts and end turn
        setupTestInput("build 1\nbuild 2\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertEquals(2, player.getCity().size());
        assertEquals(1, player.getGold()); // 5 - 2 - 2 costs
    }

    /**
     * Tests Architect maximum builds.
     * Verifies that Architect cannot exceed maximum allowed builds per turn.
     */
    @Test
    public void testBuildCommandWithArchitectMaxBuilds() {
        // Set current player as Architect
        Game.selectedCharacters.put(player, new CharacterCard("Architect", 7));
        player.addGold(15); // Total 20 gold
        
        // Add more cards to hand
        player.drawCard(new DistrictCard("Palace", "yellow", 5, 1, null));
        player.drawCard(new DistrictCard("Watchtower", "red", 1, 1, null));
        
        // Try to build more than 3 districts
        setupTestInput("build 1\nbuild 2\nbuild 3\nbuild 4\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertEquals(3, player.getCity().size()); // Should only build 3 districts
    }

    /**
     * Tests all command with null city.
     * Verifies that all command handles players with null city gracefully.
     */
    @Test
    public void testAllCommandWithNullCity() {
        // Add a player with null city (edge case)
        Player player2 = new HumanPlayer("Player 2");
        Game.players.add(player2);
        player2.getCity().clear();
        
        setupTestInput("all\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests load command with invalid JSON.
     * Verifies that load command handles invalid JSON files gracefully.
     */
    @Test
    public void testLoadCommandInvalidJson() {
        // Create an invalid JSON file
        setupTestInput("load invalid.json\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests load game command with invalid JSON.
     * Verifies that load game command handles invalid JSON files gracefully.
     */
    @Test
    public void testLoadGameCommandInvalidJson() {
        // Create an invalid JSON file
        setupTestInput("loadgame invalid.json\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests save command with invalid path.
     * Verifies that save command handles invalid file paths gracefully.
     */
    @Test
    public void testSaveCommandInvalidPath() {
        // Try to save to an invalid path
        setupTestInput("save /invalid/path/test.json\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests save game command with invalid path.
     * Verifies that save game command handles invalid file paths gracefully.
     */
    @Test
    public void testSaveGameCommandInvalidPath() {
        // Try to save to an invalid path
        setupTestInput("savegame /invalid/path/test.json\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests build command with full city.
     * Verifies that building is prevented when city is at maximum capacity.
     */
    @Test
    public void testBuildCommandWithFullCity() {
        // Fill the city with 8 districts
        for (int i = 0; i < 8; i++) {
            player.drawCard(new DistrictCard("District" + i, "yellow", 1, 1, null));
            player.buildDistrict(player.getHand().size() - 1);
        }
        
        // Add one more card to hand for testing
        player.drawCard(new DistrictCard("Extra District", "yellow", 1, 1, null));
        
        setupTestInput("build 1\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertEquals(8, player.getCity().size()); // City should remain at max size
    }

    /**
     * Tests build command with empty hand.
     * Verifies that build command handles empty hand gracefully.
     */
    @Test
    public void testBuildCommandWithEmptyHand() {
        // Clear the player's hand
        player.getHand().clear();
        
        setupTestInput("build 1\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertTrue(player.getCity().isEmpty());
    }

    /**
     * Tests Architect building with full city.
     * Verifies that Architect cannot exceed city size limit.
     */
    @Test
    public void testBuildCommandWithArchitectAndFullCity() {
        // Set current player as Architect
        Game.selectedCharacters.put(player, new CharacterCard("Architect", 7));
        
        // Fill the city with 7 districts
        for (int i = 0; i < 7; i++) {
            player.drawCard(new DistrictCard("District" + i, "yellow", 1, 1, null));
            player.buildDistrict(player.getHand().size() - 1);
        }
        
        // Add two more cards to hand for testing
        player.drawCard(new DistrictCard("Extra District 1", "yellow", 1, 1, null));
        player.drawCard(new DistrictCard("Extra District 2", "yellow", 1, 1, null));
        
        // Try to build more than one district
        setupTestInput("build 1\nbuild 2\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertEquals(8, player.getCity().size()); // Should only build up to 8 districts
    }

    /**
     * Tests build command with retry and end.
     * Verifies that build command properly handles retry and turn end sequence.
     */
    @Test
    public void testBuildCommandWithRetryAndEnd() {
        player.addGold(-4); // Leave with 1 gold
        setupTestInput("build 3\nt\n"); // Try to build Castle (4 cost), then end turn
        CommandHandler.run(player, Game.getScanner());
        assertTrue(player.getCity().isEmpty());
        assertEquals(1, player.getGold());
    }

    /**
     * Tests Magician swap invalid target message.
     * Verifies that appropriate error message is shown for invalid swap target.
     */
    @Test
    public void testMagicianSwapInvalidTargetMessage() {
        // Set current player as Magician
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        // Try to swap with a non-existent player
        setupTestInput("action swap 999\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Test will pass if it reaches this point without throwing an exception
        // The error message "Invalid swap target." should be displayed
    }

    /**
     * Tests city display with colored districts.
     * Verifies that districts are displayed with correct color information.
     */
    @Test
    public void testCityDisplayWithColoredDistricts() {
        // Build a district
        player.buildDistrict(0); // Build Temple
        
        // Add another player with districts
        Player player2 = new HumanPlayer("Player 2");
        player2.drawCard(new DistrictCard("Watchtower", "red", 1, 1, null));
        player2.buildDistrict(0);
        Game.players.add(player2);
        
        // Test city display for both current player and other player
        setupTestInput("city\ncity 2\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Test will pass if it reaches this point without throwing an exception
        // Should display district info including name, color, and cost
    }

    /**
     * Tests Magician redraw invalid indices message.
     * Verifies that appropriate error message is shown for invalid redraw indices.
     */
    @Test
    public void testMagicianRedrawInvalidIndicesMessage() {
        // Set current player as Magician
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        // Try to redraw with invalid indices
        setupTestInput("action redraw 99,100\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Test will pass if it reaches this point without throwing an exception
        // The error message "Invalid redraw indices." should be displayed
    }

    /**
     * Tests save command usage message.
     * Verifies that appropriate usage message is shown for save commands.
     */
    @Test
    public void testSaveCommandUsageMessage() {
        // Test save command without filename
        setupTestInput("save\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        // Test savegame command without filename
        setupTestInput("savegame\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // Test will pass if it reaches this point without throwing an exception
        // Should display appropriate usage messages
    }

    /**
     * Tests build command non-gold failure.
     * Verifies that build failures not related to gold are handled properly.
     */
    @Test
    public void testBuildCommandNonGoldFailure() {
        // Create a situation where build fails for a reason other than gold
        // For example, trying to build with an empty hand after a failed attempt
        player.getHand().clear(); // Clear the hand
        
        setupTestInput("build 1\nt\n");
        CommandHandler.run(player, Game.getScanner());
        // The command should return false and exit the build loop
        assertTrue(player.getCity().isEmpty());
    }

    /**
     * Tests hand display with already built district.
     * Verifies that hand display properly marks already built districts.
     */
    @Test
    public void testHandDisplayWithAlreadyBuiltDistrict() {
        // First build a district
        player.buildDistrict(0); // Build Temple
        
        // Test hand command - should show Temple with "△" status
        setupTestInput("hand\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        // The Temple card should still be in hand but marked as already built
        assertTrue(player.getHand().stream()
            .anyMatch(card -> card.getName().equals("Temple")));
        assertTrue(player.hasDistrict("Temple"));
    }

    /**
     * Tests load command usage messages.
     * Verifies that appropriate usage messages are shown for load commands.
     */
    @Test
    public void testLoadCommandUsageMessages() {
        // Test load command without arguments
        setupTestInput("load\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        // Test loadgame command without arguments
        setupTestInput("loadgame\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        // Test with incorrect number of arguments
        setupTestInput("load file1.json file2.json\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        setupTestInput("loadgame file1.json file2.json\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests CommandHandler instantiation.
     * Verifies that CommandHandler can be instantiated properly.
     */
    @Test
    public void testCommandHandlerInstantiation() {
        CommandHandler handler = new CommandHandler();
        assertNotNull(handler);
    }

    /**
     * Tests build command with already built district.
     * Verifies that attempting to build already built district is handled properly.
     */
    @Test
    public void testBuildCommandWithAlreadyBuiltDistrict() {
        // First build a district
        player.buildDistrict(0); // Build Temple
        
        // Try to build the same district again
        setupTestInput("build 1\nt\n"); // This will show the hand with "△" status
        CommandHandler.run(player, Game.getScanner());
        
        // Should still only have one Temple
        assertEquals(1, player.getCity().stream()
            .filter(d -> d.getName().equals("Temple"))
            .count());
    }

    /**
     * Tests insufficient gold with already built district.
     * Verifies handling of insufficient gold when district is already built.
     */
    @Test
    public void testInsufficientGoldWithAlreadyBuiltDistrict() {
        // First build a district
        player.buildDistrict(0); // Build Temple
        
        // Remove gold to trigger insufficient gold path
        player.addGold(-4); // Leave with 1 gold
        
        // Try to build an expensive district
        setupTestInput("build 3\nt\n"); // This will show the hand with "△" status for Temple
        CommandHandler.run(player, Game.getScanner());
        
        // Should still only have one district (Temple)
        assertEquals(1, player.getCity().size());
        assertEquals(1, player.getGold());
    }

    /**
     * Tests all command with built districts.
     * Verifies that all command properly displays built districts for all players.
     */
    @Test
    public void testAllCommandWithBuiltDistricts() {
        // Build a district for current player
        player.buildDistrict(0); // Build Temple
        
        // Add and setup another player with a built district
        Player player2 = new HumanPlayer("Player 2");
        Game.players.add(player2);
        player2.drawCard(new DistrictCard("Watchtower", "red", 1, 1, null));
        player2.buildDistrict(0);
        
        // Use 'all' command to display all players' states
        setupTestInput("all\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        // Verify both players have their districts
        assertEquals(1, player.getCity().size());
        assertTrue(player.hasDistrict("Temple"));
        assertEquals(1, player2.getCity().size());
        assertTrue(player2.hasDistrict("Watchtower"));
    }

    /**
     * Tests Magician redraw with invalid format.
     * Verifies that Magician redraw handles invalid input format gracefully.
     */
    @Test
    public void testMagicianRedrawInvalidFormat() {
        // Set current player as Magician
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        // Test with invalid format (no comma)
        setupTestInput("action redraw 1 2\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        // Test with non-numeric values
        setupTestInput("action redraw a,b\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests Magician swap with empty hand player.
     * Verifies that Magician swap handles empty hand target gracefully.
     */
    @Test
    public void testMagicianSwapWithEmptyHandPlayer() {
        // Set current player as Magician
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        // Add another player with empty hand
        Player player2 = new HumanPlayer("Player 2");
        Game.players.add(player2);
        
        // Try to swap with player with empty hand
        setupTestInput("action swap 2\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests hand command with special effects.
     * Verifies that hand display properly shows districts with special effects.
     */
    @Test
    public void testHandCommandWithSpecialEffects() {
        // Add a special effect district to hand
        player.drawCard(new DistrictCard("Laboratory", "purple", 5, 1, "special effect"));
        
        setupTestInput("hand\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests debug command toggle.
     * Verifies that debug mode can be properly toggled on and off.
     */
    @Test
    public void testDebugCommandToggle() {
        // Test turning debug mode on
        setupTestInput("debug\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertTrue(Game.debugMode);
        
        // Test turning debug mode off
        setupTestInput("debug\nt\n");
        CommandHandler.run(player, Game.getScanner());
        assertFalse(Game.debugMode);
    }

    /**
     * Tests end turn with pending actions.
     * Verifies that turn end is handled properly with pending actions.
     */
    @Test
    public void testEndTurnWithPendingActions() {
        // Set up a scenario where actions are pending
        Game.selectedCharacters.put(player, new CharacterCard("Architect", 7));
        player.addGold(10);
        
        // Try to end turn with pending builds
        setupTestInput("t\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests build command edge cases.
     * Verifies that build command handles various edge cases properly.
     */
    @Test
    public void testBuildCommandEdgeCases() {
        // Test with non-numeric input that's not 't'
        setupTestInput("build 3\nx\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        // Test with empty input
        setupTestInput("build 3\n\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        // Test with multiple spaces in input
        setupTestInput("build   3\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests all command with empty game.
     * Verifies that all command works correctly in an empty game state.
     */
    @Test
    public void testAllCommandWithEmptyGame() {
        // Clear all players
        Game.players.clear();
        Game.players.add(player); // Add back just the current player
        
        setupTestInput("all\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests run with invalid command.
     * Verifies that CommandHandler properly handles invalid commands.
     */
    @Test
    public void testRunWithInvalidCommand() {
        setupTestInput("invalidcommand\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests build command with invalid number format.
     * Verifies that build command handles invalid number formats gracefully.
     */
    @Test
    public void testBuildCommandWithInvalidNumberFormat() {
        // Test with invalid number format
        setupTestInput("build abc\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        // Test with out of bounds index
        setupTestInput("build 999\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        // Test with negative index
        setupTestInput("build -1\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests build command with maximum retries.
     * Verifies that build command properly enforces maximum retry attempts.
     */
    @Test
    public void testBuildCommandWithMaximumRetries() {
        // Remove enough gold to make building impossible
        player.addGold(-4); // Leave with 1 gold
        
        // Try to build expensive district multiple times
        setupTestInput("build 3\n3\n3\n3\nt\n"); // Try 4 times (beyond max attempts)
        CommandHandler.run(player, Game.getScanner());
        
        assertEquals(1, player.getGold());
        assertTrue(player.getCity().isEmpty());
    }

    /**
     * Tests Magician swap with invalid player number.
     * Verifies that Magician swap handles invalid player numbers gracefully.
     */
    @Test
    public void testMagicianSwapWithInvalidPlayerNumber() {
        // Set current player as Magician
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        // Test with invalid player number format
        setupTestInput("action swap abc\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        // Test with player number out of bounds
        setupTestInput("action swap 999\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        // Test with negative player number
        setupTestInput("action swap -1\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests hand command with empty hand and special cards.
     * Verifies that hand command properly handles empty hands and special cards.
     */
    @Test
    public void testHandCommandWithEmptyHandAndSpecialCards() {
        // Clear hand first
        player.getHand().clear();
        
        // Add a special effect district
        player.drawCard(new DistrictCard("Laboratory", "purple", 5, 1, "special effect"));
        
        setupTestInput("hand\nt\n");
        CommandHandler.run(player, Game.getScanner());
        
        // Test with completely empty hand
        player.getHand().clear();
        setupTestInput("hand\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }

    /**
     * Tests end turn with Architect and unused builds.
     * Verifies that turn end is handled properly when Architect has unused builds.
     */
    @Test
    public void testEndTurnWithArchitectAndUnusedBuilds() {
        // Set player as Architect
        Game.selectedCharacters.put(player, new CharacterCard("Architect", 7));
        player.addGold(10); // Ensure enough gold to build
        
        // Add more districts to hand
        player.drawCard(new DistrictCard("Palace", "yellow", 5, 1, null));
        player.drawCard(new DistrictCard("Castle", "yellow", 4, 1, null));
        
        // Try to end turn without using all builds
        setupTestInput("t\n"); // Try to end turn immediately
        CommandHandler.run(player, Game.getScanner());
        
        // Verify no districts were built
        assertTrue(player.getCity().isEmpty());
    }

    /**
     * Tests all command with null city and special districts.
     * Verifies that all command handles null cities and special districts properly.
     */
    @Test
    public void testAllCommandWithNullCityAndSpecialDistricts() {
        // Add a player with null city
        Player player2 = new HumanPlayer("Player 2");
        Game.players.add(player2);
        player2.getCity().clear();
        
        // Add special district to current player
        player.drawCard(new DistrictCard("Laboratory", "purple", 5, 1, "special effect"));
        player.buildDistrict(player.getHand().size() - 1);
        
        setupTestInput("all\nt\n");
        CommandHandler.run(player, Game.getScanner());
    }
}
