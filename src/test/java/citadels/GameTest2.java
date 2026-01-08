package citadels;

import citadels.card.CharacterCard;
import citadels.card.DistrictCard;
import citadels.player.Player;
import citadels.player.HumanPlayer;
import citadels.player.AIPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayInputStream;
import java.util.Scanner;

/**
 * Additional test suite for Game class.
 * Tests complex game scenarios, edge cases, and special character
 * interactions not covered in the main GameTest.
 */
public class GameTest2 {
    private Game game;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private Scanner originalScanner;

    /**
     * Sets up test environment before each test.
     * Initializes game state, clears players and characters,
     * and sets up output capture.
     */
    @BeforeEach
    void setUp() {
        game = new Game();
        Game.players.clear();
        Game.selectedCharacters.clear();
        System.setOut(new PrintStream(outputStreamCaptor));
        System.setProperty("test.env", "true"); // Enable test mode
        originalScanner = Game.getScanner();
    }

    /**
     * Cleans up test environment after each test.
     * Restores standard output and scanner.
     */
    @AfterEach
    void tearDown() {
        System.setOut(standardOut);
        System.clearProperty("test.env"); // Clear test mode
        Game.setScanner(originalScanner);
    }

    /**
     * Helper method to set up test input.
     * Creates scanner with simulated user input.
     * @param input the input string to simulate
     */
    private void setTestInput(String input) {
        Game.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));
    }

    /**
     * Tests game end scenario with tied players.
     * Verifies proper handling of:
     * 1. Equal score detection
     * 2. Tie message display
     * 3. Player listing in tie
     */
    @Test
    void testEndGameWithTie() {
        // Create test players with equal scores
        Player player1 = new HumanPlayer("Player 1");
        Player player2 = new AIPlayer("Player 2");
        
        // Add test players to game
        Game.players.add(player1);
        Game.players.add(player2);
        
        // Give both players identical districts worth the same points
        DistrictCard district1 = new DistrictCard("Test District 1", "blue", 3, 1, null);
        DistrictCard district2 = new DistrictCard("Test District 2", "blue", 3, 1, null);
        
        player1.getCity().add(district1);
        player2.getCity().add(district2);
        
        // Set up a mystery discarded character
        CharacterCard mysteryCard = new CharacterCard("King", 4);
        Game.selectedCharacters.clear();
        
        // Call endGame
        Game.endGame();
        
        // Get the output
        String output = outputStreamCaptor.toString();
        
        // Verify the tie message and player names are shown
        assertTrue(output.contains("It's a tie between:"), "Should show tie message");
        assertTrue(output.contains("Player 1"), "Should show first player in tie");
        assertTrue(output.contains("Player 2"), "Should show second player in tie");
        assertTrue(output.contains("Mystery discarded character was:"), "Should show mystery card message");
        assertTrue(output.contains("Thanks for playing Citadels!"), "Should show end game message");
    }

    /**
     * Tests game end with tie and mystery card.
     * Verifies proper handling of:
     * 1. Equal score detection
     * 2. Mystery card revelation
     * 3. Final score display
     * @throws Exception if reflection access fails
     */
    @Test
    void testEndGameWithTieAndMysteryCard() throws Exception {
        // Create test players with equal scores
        Player player1 = new HumanPlayer("Player 1");
        Player player2 = new AIPlayer("Player 2");
        
        // Add test players to game
        Game.players.add(player1);
        Game.players.add(player2);
        
        // Give both players identical districts worth the same points
        DistrictCard district1 = new DistrictCard("Test District 1", "blue", 3, 1, null);
        DistrictCard district2 = new DistrictCard("Test District 2", "blue", 3, 1, null);
        
        player1.getCity().add(district1);
        player2.getCity().add(district2);
        
        // Set up a mystery discarded character using reflection
        CharacterCard mysteryCard = new CharacterCard("King", 4);
        Field mysteryField = Game.class.getDeclaredField("mysteryDiscard");
        mysteryField.setAccessible(true);
        mysteryField.set(null, mysteryCard);
        
        // Call endGame
        Game.endGame();
        
        // Get the output
        String output = outputStreamCaptor.toString();
        
        // Verify the mystery card message
        assertTrue(output.contains("Mystery discarded character was: King"), 
                  "Should show the King as mystery card");
    }

    /**
     * Tests round execution with Assassin character.
     * Verifies:
     * 1. Character selection process
     * 2. Assassin target selection
     * 3. Turn skipping for assassinated character
     * @throws Exception if reflection access fails
     */
    @Test
    void testPlayRoundWithAssassin() throws Exception {
        // Set up 4 players (minimum required)
        Game.players.add(new HumanPlayer("Player 1"));
        Game.players.add(new AIPlayer("Player 2"));
        Game.players.add(new AIPlayer("Player 3"));
        Game.players.add(new AIPlayer("Player 4"));
        
        // Get access to characterPool using reflection
        Field characterPoolField = Game.class.getDeclaredField("characterPool");
        characterPoolField.setAccessible(true);
        List<CharacterCard> characterPool = (List<CharacterCard>) characterPoolField.get(null);
        characterPool.clear();
        
        // Add character cards
        characterPool.add(new CharacterCard("Assassin", 1));
        characterPool.add(new CharacterCard("Thief", 2));
        characterPool.add(new CharacterCard("Magician", 3));
        characterPool.add(new CharacterCard("King", 4));
        characterPool.add(new CharacterCard("Bishop", 5));
        characterPool.add(new CharacterCard("Merchant", 6));
        characterPool.add(new CharacterCard("Architect", 7));
        characterPool.add(new CharacterCard("Warlord", 8));
        
        // Set up initial game state
        Game.setCrownPlayerIndex(0);
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(false);
        
        // Set the assassinated character
        Field assassinatedField = Game.class.getDeclaredField("assassinatedCharacter");
        assassinatedField.setAccessible(true);
        assassinatedField.set(null, "King");
        
        // Prepare input for character selection and turns
        StringBuilder input = new StringBuilder();
        // Character selection inputs (4 players, each needs to select)
        input.append("1\n"); // First player selects Assassin (rank 1)
        input.append("4\n"); // Second player selects King (rank 4) - will be assassinated
        input.append("5\n"); // Third player selects Bishop (rank 5)
        input.append("8\n"); // Fourth player selects Warlord (rank 8)
        
        // Turn phase inputs for each player
        // For each player's turn, simulate:
        // 1. Take gold (g)
        // 2. Draw cards (d)
        // 3. End turn (t)
        for (int i = 0; i < 4; i++) {
            input.append("g\n"); // Take gold
            input.append("d\n"); // Draw cards
            input.append("1\n"); // Select first card when drawing
            input.append("t\n"); // End turn
        }
        
        setTestInput(input.toString());
        
        // Give each player some districts and gold
        for (Player p : Game.players) {
            p.addGold(2); // Give some initial gold
            for (int i = 0; i < 8; i++) { // Give 8 districts to trigger game end
                p.getCity().add(new DistrictCard("District " + i, "blue", 1, 1, null));
            }
        }
        
        // Call playRound() directly
        try {
            Game.playRound();
        } catch (Exception e) {
            fail("Exception during playRound: " + e.getMessage());
        }
        
        // Verify phase transitions
        assertEquals(Game.GamePhase.ROUND_END, Game.getCurrentPhase());
        assertFalse(Game.isRoundInProgress());
        
        // Verify that each player took their turn
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("TURN PHASE"), "Should show turn phase message");
        assertTrue(output.contains("Character choosing is over"), "Should show character selection completion message");
        assertTrue(output.contains("was assassinated and loses their turn"), "Should show assassination message");
        assertTrue(output.contains("has built 7 or more districts"), "Should show game end message");
    }

    /**
     * Tests comprehensive round execution coverage.
     * Verifies:
     * 1. Complete round flow
     * 2. Character selection
     * 3. Turn execution
     * 4. Resource collection
     * @throws Exception if reflection access fails
     */
    @Test
    void testPlayRoundCoverage() throws Exception {
        // Set up 4 players (minimum required)
        Game.players.add(new HumanPlayer("Player 1"));
        Game.players.add(new AIPlayer("Player 2"));
        Game.players.add(new AIPlayer("Player 3"));
        Game.players.add(new AIPlayer("Player 4"));
        
        // Get access to characterPool using reflection
        Field characterPoolField = Game.class.getDeclaredField("characterPool");
        characterPoolField.setAccessible(true);
        List<CharacterCard> characterPool = (List<CharacterCard>) characterPoolField.get(null);
        characterPool.clear();
        
        // Add character cards
        characterPool.add(new CharacterCard("Assassin", 1));
        characterPool.add(new CharacterCard("Thief", 2));
        characterPool.add(new CharacterCard("Magician", 3));
        characterPool.add(new CharacterCard("King", 4));
        characterPool.add(new CharacterCard("Bishop", 5));
        characterPool.add(new CharacterCard("Merchant", 6));
        characterPool.add(new CharacterCard("Architect", 7));
        characterPool.add(new CharacterCard("Warlord", 8));
        
        // Set up initial game state
        Game.setCrownPlayerIndex(0);
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(false);
        
        // Prepare input for character selection and turns
        StringBuilder input = new StringBuilder();
        // Character selection inputs (4 players, each needs to select)
        input.append("4\n"); // First player selects King (rank 4)
        input.append("5\n"); // Second player selects Bishop (rank 5)
        input.append("6\n"); // Third player selects Merchant (rank 6)
        input.append("8\n"); // Fourth player selects Warlord (rank 8)
        
        // Turn phase inputs for each player
        // For each player's turn, simulate:
        // 1. Take gold (g)
        // 2. Draw cards (d)
        // 3. End turn (t)
        for (int i = 0; i < 4; i++) {
            input.append("g\n"); // Take gold
            input.append("d\n"); // Draw cards
            input.append("1\n"); // Select first card when drawing
            input.append("t\n"); // End turn
        }
        
        setTestInput(input.toString());
        
        // Give each player some districts and gold
        for (Player p : Game.players) {
            p.addGold(2); // Give some initial gold
            for (int i = 0; i < 6; i++) {
                p.getCity().add(new DistrictCard("District " + i, "blue", 1, 1, null));
            }
        }
        
        // Call playRound() directly
        try {
            Game.playRound();
        } catch (Exception e) {
            fail("Exception during playRound: " + e.getMessage());
        }
        
        // Verify phase transitions
        assertEquals(Game.GamePhase.ROUND_END, Game.getCurrentPhase());
        assertFalse(Game.isRoundInProgress());
        
        // Verify that each player took their turn
        String output = outputStreamCaptor.toString();
        assertTrue(output.contains("TURN PHASE"), "Should show turn phase message");
        assertTrue(output.contains("Character choosing is over"), "Should show character selection completion message");
    }

    /**
     * Tests round execution with Thief and robbed character.
     * Verifies:
     * 1. Thief target selection
     * 2. Gold transfer mechanics
     * 3. Turn order handling
     * @throws Exception if reflection access fails
     */
    @Test
    void testPlayRoundWithThiefAndRobbedCharacter() throws Exception {
        // Set up 4 players (minimum required)
        Game.players.add(new HumanPlayer("Player 1"));
        Game.players.add(new AIPlayer("Player 2"));
        Game.players.add(new AIPlayer("Player 3"));
        Game.players.add(new AIPlayer("Player 4"));
        
        // Get access to characterPool using reflection
        Field characterPoolField = Game.class.getDeclaredField("characterPool");
        characterPoolField.setAccessible(true);
        List<CharacterCard> characterPool = (List<CharacterCard>) characterPoolField.get(null);
        characterPool.clear();
        
        // Add character cards
        characterPool.add(new CharacterCard("Thief", 2));
        characterPool.add(new CharacterCard("Magician", 3));
        characterPool.add(new CharacterCard("King", 4));
        characterPool.add(new CharacterCard("Bishop", 5));
        characterPool.add(new CharacterCard("Merchant", 6));
        characterPool.add(new CharacterCard("Architect", 7));
        characterPool.add(new CharacterCard("Warlord", 8));
        
        // Set up initial game state
        Game.setCrownPlayerIndex(0);
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(false);
        
        // Set the robbed character
        Field robbedField = Game.class.getDeclaredField("robbedCharacter");
        robbedField.setAccessible(true);
        robbedField.set(null, "King");
        
        // Prepare input for character selection and turns
        StringBuilder input = new StringBuilder();
        // Character selection inputs (4 players, each needs to select)
        input.append("2\n"); // First player selects Thief (rank 2)
        input.append("4\n"); // Second player selects King (rank 4) - will be robbed
        input.append("5\n"); // Third player selects Bishop (rank 5)
        input.append("8\n"); // Fourth player selects Warlord (rank 8)
        
        // Add gold to the target player
        Game.players.get(1).addGold(5); // Add gold to the player who will be robbed
        
        // Turn phase inputs for each player
        // For each player's turn, simulate:
        // 1. Take gold (g)
        // 2. Draw cards (d)
        // 3. End turn (t)
        for (int i = 0; i < 4; i++) {
            input.append("g\n"); // Take gold
            input.append("d\n"); // Draw cards
            input.append("1\n"); // Select first card when drawing
            input.append("t\n"); // End turn
        }
        
        setTestInput(input.toString());
        
        // Give each player some districts
        for (Player p : Game.players) {
            p.addGold(2); // Give some initial gold
            for (int i = 0; i < 6; i++) { // Give 6 districts (not enough to trigger game end)
                p.getCity().add(new DistrictCard("District " + i, "blue", 1, 1, null));
            }
        }
        
        // Call playRound() directly
        try {
            Game.playRound();
        } catch (Exception e) {
            fail("Exception during playRound: " + e.getMessage());
        }
        
        // Verify that the thief got the gold from the robbed player
        Player thiefPlayer = Game.players.get(0);
        Player robbedPlayer = Game.players.get(1);
        assertTrue(thiefPlayer.getGold() > 2, "Thief should have gained gold");
        assertEquals(0, robbedPlayer.getGold(), "Robbed player should have no gold");
        
        // Verify phase transitions
        assertEquals(Game.GamePhase.ROUND_END, Game.getCurrentPhase());
        assertFalse(Game.isRoundInProgress());
    }

    /**
     * Tests round execution with invalid phase and progress states.
     * Verifies proper error handling and state recovery.
     */
    @Test
    void testPlayRoundWithInvalidPhaseAndInProgress() {
        // Set up 4 players (minimum required)
        Game.players.add(new HumanPlayer("Player 1"));
        Game.players.add(new AIPlayer("Player 2"));
        Game.players.add(new AIPlayer("Player 3"));
        Game.players.add(new AIPlayer("Player 4"));
        
        // Test starting round in invalid phase
        Game.setCurrentPhase(Game.GamePhase.TURN);
        Game.setRoundInProgress(false);
        Game.playRound();
        
        // Verify that round didn't start
        assertFalse(Game.isRoundInProgress());
        assertEquals(Game.GamePhase.TURN, Game.getCurrentPhase());
        
        // Test starting round when already in progress
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        Game.playRound();
        
        // Verify that new round didn't start
        assertTrue(Game.isRoundInProgress());
        assertEquals(Game.GamePhase.SELECTION, Game.getCurrentPhase());
    }

    /**
     * Tests Warlord district destruction mechanics.
     * Verifies:
     * 1. Target selection
     * 2. Cost calculation
     * 3. Protection effects
     * @throws Exception if reflection access fails
     */
    @Test
    void testWarlordDestructionWithInputs() throws Exception {
        // Set up players
        Game.players.add(new HumanPlayer("Player 1")); // Warlord
        Game.players.add(new AIPlayer("Player 2")); // Target
        
        // Set up character cards
        Game.selectedCharacters.clear();
        Game.selectedCharacters.put(Game.players.get(0), new CharacterCard("Warlord", 8));
        
        // Set up game state
        Game.setCurrentPhase(Game.GamePhase.TURN);
        GameState.setCurrentPlayer(Game.players.get(0));
        GameState.setCurrentCharacter(Game.selectedCharacters.get(Game.players.get(0)));
        
        // Test 1: Empty city case (hits line 889)
        StringBuilder input = new StringBuilder();
        input.append("destroy\n"); // Choose destroy action
        input.append("2\n"); // Target player 2
        input.append("t\n"); // End turn
        setTestInput(input.toString());
        Game.handlePlayerTurn(Game.players.get(0), Game.selectedCharacters.get(Game.players.get(0)));
        assertTrue(Game.players.get(1).getCity().isEmpty(), "Target's city should be empty");
        
        // Test 2: Protected district case (hits lines 889-891)
        Player target = Game.players.get(1);
        DistrictCard keep = new DistrictCard("Keep", "purple", 3, 3, "Cannot be destroyed by the Warlord.");
        target.getCity().add(keep);
        input = new StringBuilder();
        input.append("destroy\n"); // Choose destroy action
        input.append("2\n"); // Target player 2
        input.append("t\n"); // End turn
        setTestInput(input.toString());
        Game.handlePlayerTurn(Game.players.get(0), Game.selectedCharacters.get(Game.players.get(0)));
        assertEquals(1, target.getCity().size(), "Keep should remain in city");
        
        // Test 3: Successful destruction case (hits lines 889-897)
        target.getCity().clear();
        DistrictCard manor = new DistrictCard("Manor", "yellow", 3, 3, null);
        target.getCity().add(manor);
        Game.players.get(0).addGold(10); // Ensure Warlord has enough gold
        input = new StringBuilder();
        input.append("destroy\n"); // Choose destroy action
        input.append("2\n"); // Target player 2
        input.append("t\n"); // End turn
        setTestInput(input.toString());
        Game.handlePlayerTurn(Game.players.get(0), Game.selectedCharacters.get(Game.players.get(0)));
        assertTrue(target.getCity().isEmpty(), "Manor should be destroyed");
        assertEquals(7, Game.players.get(0).getGold(), "Warlord should have paid 3 gold");
        
        // Test 4: Not enough gold case (hits lines 889-893)
        target.getCity().add(new DistrictCard("Castle", "yellow", 4, 5, null));
        Game.players.get(0).addGold(-Game.players.get(0).getGold()); // Remove all gold
        input = new StringBuilder();
        input.append("destroy\n"); // Choose destroy action
        input.append("2\n"); // Target player 2
        input.append("t\n"); // End turn
        setTestInput(input.toString());
        Game.handlePlayerTurn(Game.players.get(0), Game.selectedCharacters.get(Game.players.get(0)));
        assertEquals(1, target.getCity().size(), "Castle should remain when Warlord has no gold");
    }

    /**
     * Tests specific edge cases in Warlord destruction.
     * Verifies handling of special cases in lines 889-901.
     * @throws Exception if reflection access fails
     */
    @Test
    void testWarlordDestructionLines889to901() throws Exception {
        // Set up players
        Game.players.add(new HumanPlayer("Player 1")); // Warlord
        Game.players.add(new AIPlayer("Player 2")); // Target
        
        // Set up character cards
        Game.selectedCharacters.clear();
        Game.selectedCharacters.put(Game.players.get(0), new CharacterCard("Warlord", 8));
        Game.selectedCharacters.put(Game.players.get(1), new CharacterCard("Merchant", 6));
        
        // Set up game state
        Game.setCurrentPhase(Game.GamePhase.TURN);
        GameState.setCurrentPlayer(Game.players.get(0));
        GameState.setCurrentCharacter(Game.selectedCharacters.get(Game.players.get(0)));
        
        // Give Warlord enough gold
        Game.players.get(0).addGold(10);
        
        // Give target a district
        Player target = Game.players.get(1);
        target.getCity().add(new DistrictCard("Tavern", "green", 1, 1, null));
        
        // The exact input sequence needed for the Warlord's destroy command
        StringBuilder input = new StringBuilder();
        input.append("destroy\n"); // Choose destroy command
        input.append("2\n"); // Select player 2
        input.append("t\n"); // End turn
        
        setTestInput(input.toString());
        
        // Execute Warlord's turn
        Game.handlePlayerTurn(Game.players.get(0), Game.selectedCharacters.get(Game.players.get(0)));
        
        // Verify the district was destroyed
        assertTrue(target.getCity().isEmpty());
        assertEquals(9, Game.players.get(0).getGold());
    }
} 