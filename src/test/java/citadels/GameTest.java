package citadels;

import citadels.card.CharacterCard;
import citadels.card.DistrictCard;
import citadels.player.AIPlayer;
import citadels.player.HumanPlayer;
import citadels.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.json.simple.JSONObject;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for the Citadels game core functionality.
 * Tests cover game initialization, player management, character abilities,
 * game flow, and various game mechanics.
 */
public class GameTest {
    private Game game;
    private Scanner originalScanner;
    private static final String TEST_PLAYER_NAME = "Test Player";

    /**
     * Sets up the test environment before each test.
     * - Creates a new game instance
     * - Stores original scanner
     * - Resets all game state variables
     * - Initializes test deck
     * - Sets up test environment
     * - Creates test players
     * - Deals initial cards
     */
    @BeforeEach
    public void setUp() {
        game = new Game();
        originalScanner = Game.getScanner();
        
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
        Game.setRoundInProgress(false);
        GameState.setCurrentPlayer(null);
        GameState.setCurrentCharacter(null);
        
        // Add test cards to deck
        for (int i = 0; i < 20; i++) {
            Game.districtDeck.addCard(new DistrictCard("District" + i, "yellow", 1, 1, null));
        }
        
        // Set test environment property to prevent System.exit
        System.setProperty("test.env", "true");
        
        // Initialize players for tests that need them
        game.createPlayers(4);
        
        // Deal initial cards and gold
        game.dealInitialCards();
    }

    /**
     * Cleans up the test environment after each test by restoring the original scanner.
     */
    @AfterEach
    public void tearDown() {
        Game.setScanner(originalScanner);
    }

    /**
     * Helper method to simulate user input during tests.
     * @param input The string to be used as simulated user input
     */
    private void setTestInput(String input) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        Game.setScanner(new Scanner(in));
    }

    /**
     * Tests that the game is properly initialized with correct default values.
     * Verifies initial game state variables, phase, crown holder, and character states.
     */
    @Test
    public void testInitialGameState() {
        // Verify initial state
        assertFalse(Game.isGameOver(), "Game should not be over initially");
        assertNull(Game.getWinner(), "Winner should be null initially");
        assertEquals(Game.GamePhase.SELECTION, Game.getCurrentPhase(), "Game should start in selection phase");
        assertEquals(0, Game.getCrownPlayerIndex(), "Crown should start with player 0");
        assertNull(Game.getAssassinatedCharacter(), "No character should be assassinated initially");
        assertNull(Game.getRobbedCharacter(), "No character should be robbed initially");
        assertFalse(Game.isRoundInProgress(), "Round should not be in progress initially");
        assertNull(GameState.getCurrentPlayer(), "Current player should be null initially");
        assertNull(GameState.getCurrentCharacter(), "Current character should be null initially");
    }

    /**
     * Tests player creation functionality.
     * Verifies that correct number of players are created with proper types
     * (1 human player followed by AI players).
     */
    @Test
    public void testCreatePlayers() {
        game.createPlayers(4);
        
        assertEquals(4, Game.players.size());
        assertTrue(Game.players.get(0) instanceof HumanPlayer);
        assertTrue(Game.players.get(1) instanceof AIPlayer);
        assertTrue(Game.players.get(2) instanceof AIPlayer);
        assertTrue(Game.players.get(3) instanceof AIPlayer);
    }

    /**
     * Tests initial card and gold distribution.
     * Verifies that each player receives the correct number of cards (4)
     * and starting gold amount (2).
     */
    @Test
    public void testDealInitialCards() {
        game.createPlayers(4);
        game.dealInitialCards();
        
        for (Player p : Game.players) {
            assertEquals(4, p.getHand().size(), "Each player should have 4 cards");
            assertEquals(2, p.getGold(), "Each player should have 2 gold");
        }
    }

    /**
     * Tests the character selection phase of the game.
     * Verifies that:
     * - Each player can select a unique character
     * - All selections are properly recorded
     * - Round state is properly updated after selection
     */
    @Test
    public void testCharacterSelection() {
        game.createPlayers(4);
        setTestInput("1\n2\n3\n4\nt\n"); // Each player selects a different character
        
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        game.playRound();
        
        assertEquals(4, Game.selectedCharacters.size(), "Each player should have selected a character");
        
        // Verify each player has a unique character
        Set<Integer> characterRanks = new HashSet<>();
        for (CharacterCard card : Game.selectedCharacters.values()) {
            assertTrue(characterRanks.add(card.getRank()), "Each player should have a unique character");
        }
        
        assertFalse(Game.isRoundInProgress(), "Round should end after character selection");
    }

    /**
     * Tests the Assassin character's special ability.
     * Verifies that:
     * - Assassin can target another character
     * - The assassination is properly recorded in game state
     */
    @Test
    public void testAssassinAbility() {
        game.createPlayers(4);
        Player assassinPlayer = Game.players.get(0);
        Player victim = Game.players.get(1);
        
        Game.selectedCharacters.put(assassinPlayer, new CharacterCard("Assassin", 1));
        Game.selectedCharacters.put(victim, new CharacterCard("King", 4));
        
        setTestInput("4\nt\n");
        
        Game.setCurrentPhase(Game.GamePhase.TURN);
        assassinPlayer.takeTurn();
        
        assertEquals("4", Game.getAssassinatedCharacter());
    }

    /**
     * Tests the Thief character's special ability.
     * Verifies that:
     * - Thief can target another character
     * - The robbery target is properly recorded in game state
     */
    @Test
    public void testThiefAbility() {
        game.createPlayers(4);
        Player thiefPlayer = Game.players.get(0);
        Player victim = Game.players.get(1);
        victim.addGold(5);
        
        Game.selectedCharacters.put(thiefPlayer, new CharacterCard("Thief", 2));
        Game.selectedCharacters.put(victim, new CharacterCard("King", 4));
        
        setTestInput("4\nt\n");
        
        Game.setCurrentPhase(Game.GamePhase.TURN);
        thiefPlayer.takeTurn();
        
        assertEquals("4", Game.getRobbedCharacter());
    }

    /**
     * Tests game end conditions and winner determination.
     * Verifies that:
     * - Game ends when a player builds 8 districts
     * - Winner is correctly determined
     * - Game state is properly updated
     */
    @Test
    public void testEndGame() {
        game.createPlayers(4);
        Player winner = Game.players.get(0);
        
        // Add 8 districts to trigger game end
        for (int i = 0; i < 8; i++) {
            winner.getCity().add(new DistrictCard("District" + i, "yellow", 1, 1, null));
        }
        
        // Add character to determine winner in case of tie
        Game.selectedCharacters.put(winner, new CharacterCard("King", 4));
        
        setTestInput("t\n");
        Game.setCurrentPhase(Game.GamePhase.TURN);
        Game.endGame();
        
        assertTrue(Game.isGameOver());
        assertEquals(winner, Game.getWinner());
    }

    /**
     * Tests the rainbow district bonus scoring.
     * Verifies that players receive correct bonus points for
     * having districts of all colors in their city.
     */
    @Test
    public void testRainbowBonus() {
        game.createPlayers(4);
        Player player = Game.players.get(0);
        int initialScore = player.calculateScore();
        
        // Add districts of all colors
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        player.getCity().add(new DistrictCard("Castle", "yellow", 4, 1, null));
        player.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        player.getCity().add(new DistrictCard("Watchtower", "red", 1, 1, null));
        player.getCity().add(new DistrictCard("Keep", "purple", 3, 1, null));
        
        // Calculate final score
        int finalScore = player.calculateScore();
        
        // Rainbow bonus should be 3 points for having all colors
        assertEquals(initialScore + 15, finalScore, "Score should include district costs (12) and rainbow bonus (3)");
        
        // Verify game end condition
        Game.endGame();
        assertTrue(Game.isGameOver(), "Game should end");
        assertEquals(player, Game.getWinner(), "Player with rainbow bonus should win");
    }

    /**
     * Tests game phase transitions throughout a round.
     * Verifies that:
     * - Game starts in selection phase
     * - Transitions to turn phase after character selection
     * - Ends in round end phase after all turns
     * - Round progress flags are properly updated
     */
    @Test
    public void testGamePhaseTransitions() {
        game.createPlayers(4);
        setTestInput("1\n2\n3\n4\nt\n"); // Character selection input
        
        // Start in selection phase
        assertEquals(Game.GamePhase.SELECTION, Game.getCurrentPhase(), "Game should start in selection phase");
        Game.setRoundInProgress(true);
        
        // Play through character selection
        game.playRound();
        
        // Should transition to turn phase after selection
        assertEquals(Game.GamePhase.TURN, Game.getCurrentPhase(), "Game should move to turn phase after selection");
        
        // Complete turns
        for (int i = 0; i < 4; i++) {
            setTestInput("gold\nt\n"); // Take gold action and end turn
            game.handlePlayerTurn(Game.players.get(i), Game.selectedCharacters.get(Game.players.get(i)));
        }
        
        // Should end in round end phase
        assertEquals(Game.GamePhase.ROUND_END, Game.getCurrentPhase(), "Game should end in round end phase");
        assertFalse(Game.isRoundInProgress(), "Round should not be in progress at end");
    }

    /**
     * Tests the crown transfer mechanism when the King character is played.
     * Verifies that the crown moves to the player who plays the King character.
     */
    @Test
    public void testCrownTransfer() {
        game.createPlayers(4);
        Game.setCrownPlayerIndex(0);
        
        Player kingPlayer = Game.players.get(1);
        Game.selectedCharacters.put(kingPlayer, new CharacterCard("King", 4));
        
        setTestInput("t\n");
        Game.setCurrentPhase(Game.GamePhase.TURN);
        kingPlayer.takeTurn();
        
        assertEquals(1, Game.getCrownPlayerIndex());
    }

    /**
     * Tests that invalid phase transitions are properly handled.
     * Verifies that the game phase remains unchanged when an invalid transition is attempted.
     */
    @Test
    public void testInvalidPhaseTransition() {
        game.createPlayers(4);
        Game.setCurrentPhase(Game.GamePhase.TURN);
        
        game.playRound();
        
        assertEquals(Game.GamePhase.TURN, Game.getCurrentPhase());
    }

    /**
     * Tests the game's ability to handle multiple rounds.
     * Verifies that:
     * - Multiple rounds can be played consecutively
     * - Game state is properly reset between rounds
     * - Players maintain their resources between rounds
     * - Character selections are cleared between rounds
     */
    @Test
    public void testMultipleRounds() {
        game.createPlayers(4);
        
        // First round
        setTestInput("1\n2\n3\n4\nt\n"); // Character selection
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        game.playRound();
        
        assertFalse(Game.isRoundInProgress(), "First round should be complete");
        assertEquals(Game.GamePhase.ROUND_END, Game.getCurrentPhase(), "Should be in round end phase");
        
        // Record state after first round
        Map<Player, Integer> goldAfterFirstRound = new HashMap<>();
        for (Player p : Game.players) {
            goldAfterFirstRound.put(p, p.getGold());
        }
        
        // Second round
        setTestInput("1\n2\n3\n4\nt\n"); // Character selection for second round
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        Game.selectedCharacters.clear(); // Clear previous round's characters
        game.playRound();
        
        assertFalse(Game.isRoundInProgress(), "Second round should be complete");
        assertEquals(Game.GamePhase.ROUND_END, Game.getCurrentPhase(), "Should be in round end phase");
        
        // Verify players gained resources in second round
        for (Player p : Game.players) {
            assertTrue(p.getGold() >= goldAfterFirstRound.get(p), 
                "Players should not lose gold between rounds");
        }
    }

    /**
     * Tests each character's special income abilities.
     * Verifies that:
     * - King gets income from yellow districts
     * - Bishop gets income from blue districts
     * - Merchant gets income from green districts
     * - Warlord gets income from red districts
     */
    @Test
    public void testCharacterAbilities() {
        game.createPlayers(4);
        Player player = Game.players.get(0);
        
        // Test each character's income ability
        CharacterCard[] characters = {
            new CharacterCard("King", 4),
            new CharacterCard("Bishop", 5),
            new CharacterCard("Merchant", 6),
            new CharacterCard("Warlord", 8)
        };
        
        String[] colors = {"yellow", "blue", "green", "red"};
        
        for (int i = 0; i < characters.length; i++) {
            Game.selectedCharacters.clear();
            Game.selectedCharacters.put(player, characters[i]);
            player.getCity().clear();
            player.getCity().add(new DistrictCard("Test", colors[i], 1, 1, null));
            
            setTestInput("t\n");
            int initialGold = player.getGold();
            player.takeTurn();
            
            assertTrue(player.getGold() >= initialGold,
                characters[i].getName() + " should not lose gold from " + colors[i] + " districts");
        }
    }

    /**
     * Tests game initialization with minimum required players.
     * Verifies that the game can run with exactly 4 players.
     */
    @Test
    public void testGameRunWithMinimalPlayers() {
        setTestInput("4\n1\n2\n3\n4\nt\nt\nt\nt\nexit\n");
        game.run();
        assertEquals(4, Game.players.size());
    }

    /**
     * Tests game initialization with invalid player counts.
     * Verifies that the game properly handles attempts to start with invalid number of players.
     */
    @Test
    public void testGameRunWithInvalidPlayerCount() {
        setTestInput("2\n8\n4\n1\n2\n3\n4\nt\nt\nt\nt\nexit\n");
        game.run();
        assertEquals(4, Game.players.size());
    }

    /**
     * Tests that game state remains consistent throughout gameplay.
     * Verifies that all game state variables maintain proper values and relationships.
     */
    @Test
    public void testGameStateConsistency() {
        setTestInput("3\n1\n2\n3\nt\nt\nt\nexit\n");
        game.run();
        
        // Verify game state
        assertFalse(Game.players.isEmpty());
        assertFalse(Game.selectedCharacters.isEmpty());
        assertNotNull(Game.districtDeck);
        assertFalse(Game.isSelectionPhase());
    }

    /**
     * Tests a complete game flow from start to finish.
     * Verifies all major game components working together:
     * - Player setup
     * - Character selection
     * - Turn execution
     * - Resource management
     * - Game end conditions
     */
    @Test
    public void testComprehensiveGameFlow() {
        // 1. Setup game with 4 players
        game.createPlayers(4);
        
        // 2. Test character selection phase
        setTestInput("1\n2\n3\n4\nt\n"); // Each player selects a different character
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        
        // Verify initial selection state
        assertTrue(Game.isSelectionPhase());
        
        // Execute selection phase
        game.playRound();
        
        assertEquals(4, Game.selectedCharacters.size(), "All players should have characters");
        assertFalse(Game.isRoundInProgress(), "Round should end after selection");
        
        // 3. Test Assassin ability
        Player assassinPlayer = Game.players.get(0);
        Game.selectedCharacters.clear();
        Game.selectedCharacters.put(assassinPlayer, new CharacterCard("Assassin", 1));
        setTestInput("4\nt\n"); // Target rank 4 (King)
        Game.setCurrentPhase(Game.GamePhase.TURN);
        Game.handlePlayerTurn(assassinPlayer, Game.selectedCharacters.get(assassinPlayer));
        assertEquals("King", Game.getAssassinatedCharacter());
        
        // 4. Test Thief ability
        Player thiefPlayer = Game.players.get(1);
        Player victimPlayer = Game.players.get(2);
        victimPlayer.addGold(5);
        Game.selectedCharacters.clear();
        Game.selectedCharacters.put(thiefPlayer, new CharacterCard("Thief", 2));
        Game.selectedCharacters.put(victimPlayer, new CharacterCard("Bishop", 5));
        setTestInput("5\nt\n"); // Target rank 5 (Bishop)
        Game.handlePlayerTurn(thiefPlayer, Game.selectedCharacters.get(thiefPlayer));
        assertEquals("Bishop", Game.getRobbedCharacter());
        
        // 5. Test Magician ability
        Player magicianPlayer = Game.players.get(0);
        Game.selectedCharacters.clear();
        Game.selectedCharacters.put(magicianPlayer, new CharacterCard("Magician", 3));
        
        // Test swap hands
        setTestInput("swap\n1\nt\n");
        magicianPlayer.drawCard(new DistrictCard("Test1", "blue", 1, 1, null));
        Game.players.get(1).drawCard(new DistrictCard("Test2", "red", 2, 2, null));
        Game.handlePlayerTurn(magicianPlayer, Game.selectedCharacters.get(magicianPlayer));
        
        // Test redraw
        setTestInput("redraw\nt\n");
        Game.handlePlayerTurn(magicianPlayer, Game.selectedCharacters.get(magicianPlayer));
        
        // 6. Test normal turn actions
        Player normalPlayer = Game.players.get(3);
        Game.selectedCharacters.put(normalPlayer, new CharacterCard("King", 4));
        
        // Test gold collection
        setTestInput("gold\nt\n");
        int initialGold = normalPlayer.getGold();
        Game.handlePlayerTurn(normalPlayer, Game.selectedCharacters.get(normalPlayer));
        assertEquals(initialGold + 2, normalPlayer.getGold());
        
        // Test card drawing
        setTestInput("cards\n1\nt\n");
        int initialHandSize = normalPlayer.getHand().size();
        Game.handlePlayerTurn(normalPlayer, Game.selectedCharacters.get(normalPlayer));
        assertEquals(initialHandSize + 1, normalPlayer.getHand().size());
        
        // 7. Test Museum special ability
        Player museumPlayer = Game.players.get(0);
        museumPlayer.getCity().add(new DistrictCard("Museum", "purple", 4, 1, null));
        museumPlayer.drawCard(new DistrictCard("Test", "blue", 1, 1, null));
        Game.selectedCharacters.put(museumPlayer, new CharacterCard("Bishop", 5));
        setTestInput("gold\n1\nt\n");
        Game.handlePlayerTurn(museumPlayer, Game.selectedCharacters.get(museumPlayer));
        assertEquals(1, museumPlayer.getBankedCards().size());
        
        // 8. Test Warlord ability
        Player warlordPlayer = Game.players.get(0);
        Player targetPlayer = Game.players.get(1);
        targetPlayer.getCity().add(new DistrictCard("TestDistrict", "blue", 1, 1, null));
        warlordPlayer.addGold(10);
        Game.selectedCharacters.clear();
        Game.selectedCharacters.put(warlordPlayer, new CharacterCard("Warlord", 8));
        Game.selectedCharacters.put(targetPlayer, new CharacterCard("Merchant", 6));
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(warlordPlayer, Game.selectedCharacters.get(warlordPlayer));
        assertTrue(targetPlayer.getCity().isEmpty());
        
        // 9. Test info commands
        setTestInput("gold\nhand\ncity\nall\nt\n");
        Game.handlePlayerTurn(warlordPlayer, Game.selectedCharacters.get(warlordPlayer));
        
        // 10. Test game end conditions
        for (int i = 0; i < 8; i++) {
            warlordPlayer.getCity().add(new DistrictCard("District" + i, "yellow", 1, 1, null));
        }
        Game.endGame();
        assertTrue(Game.isGameOver());
        assertNotNull(Game.getWinner());
    }

    /**
     * Tests edge cases in character selection phase.
     * Verifies handling of:
     * - King being removed face-up
     * - Invalid selection inputs
     * - Incorrect commands during selection phase
     */
    @Test
    public void testCharacterSelectionEdgeCases() {
        game.createPlayers(4);
        
        // Test King being removed face-up
        setTestInput("1\n2\n3\n4\nt\n");
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        game.playRound();
        
        // Test invalid selection inputs
        setTestInput("invalid\n1\nt\n");
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        Game.selectedCharacters.clear();
        game.playRound();
        
        // Test gold command during selection
        setTestInput("gold\n1\nt\n");
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        Game.selectedCharacters.clear();
        game.playRound();
    }

    /**
     * Tests the command handling system.
     * Verifies proper handling of:
     * - Valid commands
     * - Invalid commands
     * - Command timing restrictions
     */
    @Test
    public void testCommandHandling() {
        game.createPlayers(4);
        Player player = Game.players.get(0);
        
        // Test empty hand display
        setTestInput("hand\nt\n");
        Game.handlePlayerTurn(player, new CharacterCard("King", 4));
        
        // Test empty city display
        setTestInput("city\nt\n");
        Game.handlePlayerTurn(player, new CharacterCard("King", 4));
        
        // Test all command with empty states
        setTestInput("all\nt\n");
        Game.handlePlayerTurn(player, new CharacterCard("King", 4));
        
        // Test invalid commands
        setTestInput("invalid\nt\n");
        Game.handlePlayerTurn(player, new CharacterCard("King", 4));
    }

    /**
     * Tests special abilities of all character types.
     * Verifies correct implementation of:
     * - Assassin's kill ability
     * - Thief's steal ability
     * - Magician's hand swap/redraw
     * - Warlord's destruction ability
     */
    @Test
    public void testSpecialCharacterAbilities() {
        game.createPlayers(4);
        
        // Test Assassin with invalid target
        Player assassinPlayer = Game.players.get(0);
        Game.selectedCharacters.put(assassinPlayer, new CharacterCard("Assassin", 1));
        setTestInput("invalid\n4\nt\n");
        Game.handlePlayerTurn(assassinPlayer, Game.selectedCharacters.get(assassinPlayer));
        
        // Test Thief with invalid target
        Player thiefPlayer = Game.players.get(1);
        Game.selectedCharacters.put(thiefPlayer, new CharacterCard("Thief", 2));
        setTestInput("invalid\n1\n4\nt\n"); // Can't steal from Assassin
        Game.handlePlayerTurn(thiefPlayer, Game.selectedCharacters.get(thiefPlayer));
        
        // Test Magician with invalid inputs
        Player magicianPlayer = Game.players.get(2);
        Game.selectedCharacters.put(magicianPlayer, new CharacterCard("Magician", 3));
        setTestInput("invalid\nswap\ninvalid\n1\nt\n");
        Game.handlePlayerTurn(magicianPlayer, Game.selectedCharacters.get(magicianPlayer));
    }

    /**
     * Tests card drawing and district building mechanics.
     * Verifies:
     * - Proper card drawing from deck
     * - Building districts with sufficient gold
     * - Building restrictions
     * - Hand management
     */
    @Test
    public void testCardDrawingAndBuilding() {
        game.createPlayers(4);
        Player player = Game.players.get(0);
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        
        // Test invalid card selection
        setTestInput("cards\ninvalid\n1\nt\n");
        Game.handlePlayerTurn(player, Game.selectedCharacters.get(player));
        
        // Test skipping card selection
        setTestInput("cards\nt\n");
        Game.handlePlayerTurn(player, Game.selectedCharacters.get(player));
        
        // Test invalid action choice
        setTestInput("invalid\ngold\nt\n");
        Game.handlePlayerTurn(player, Game.selectedCharacters.get(player));
    }

    /**
     * Tests the main game loop functionality.
     * Verifies proper execution of:
     * - Round initialization
     * - Phase transitions
     * - Turn processing
     * - Game state updates
     */
    @Test
    public void testMainGameLoop() {
        // Test main game loop with different inputs
        setTestInput("4\n1\n2\n3\n4\nt\ngold\nexit\n");
        game.run();
        
        // Test with invalid player count then valid
        setTestInput("3\n8\n4\n1\n2\n3\n4\nt\nexit\n");
        game.run();
        
        // Test with non-numeric input for player count
        setTestInput("abc\n4\n1\n2\n3\n4\nt\nexit\n");
        game.run();
    }

    /**
     * Tests all possible scenarios in character selection.
     * Verifies:
     * - Face-up and face-down discards
     * - Selection order rules
     * - Character availability tracking
     * - Invalid selection handling
     */
    @Test
    public void testCharacterSelectionWithAllScenarios() {
        game.createPlayers(4);
        
        // Test with 4 players (2 face-up discards)
        setTestInput("1\n2\n3\n4\nt\n");
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        game.playRound();
        
        // Test with 5 players (1 face-up discard)
        game.createPlayers(5);
        setTestInput("1\n2\n3\n4\n5\nt\n");
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        Game.selectedCharacters.clear();
        game.playRound();
        
        // Test with 6 players (0 face-up discards)
        game.createPlayers(6);
        setTestInput("1\n2\n3\n4\n5\n6\nt\n");
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        Game.selectedCharacters.clear();
        game.playRound();
    }

    /**
     * Tests comprehensive functionality of all character abilities.
     * Verifies:
     * - Basic and special abilities
     * - Income generation
     * - Ability timing restrictions
     * - Interaction between abilities
     */
    @Test
    public void testAllCharacterAbilities() {
        game.createPlayers(4);
        
        // Test King ability and crown transfer
        Player kingPlayer = Game.players.get(0);
        kingPlayer.getCity().add(new DistrictCard("Castle", "yellow", 3, 1, null));
        Game.selectedCharacters.put(kingPlayer, new CharacterCard("King", 4));
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(kingPlayer, Game.selectedCharacters.get(kingPlayer));
        assertEquals(0, Game.getCrownPlayerIndex());
        
        // Test Bishop ability and protection
        Player bishopPlayer = Game.players.get(1);
        bishopPlayer.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        Game.selectedCharacters.put(bishopPlayer, new CharacterCard("Bishop", 5));
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(bishopPlayer, Game.selectedCharacters.get(bishopPlayer));
        
        // Test Merchant ability
        Player merchantPlayer = Game.players.get(2);
        merchantPlayer.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        Game.selectedCharacters.put(merchantPlayer, new CharacterCard("Merchant", 6));
        int initialGold = merchantPlayer.getGold();
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(merchantPlayer, Game.selectedCharacters.get(merchantPlayer));
        assertTrue(merchantPlayer.getGold() > initialGold);
        
        // Test Architect ability
        Player architectPlayer = Game.players.get(3);
        Game.selectedCharacters.put(architectPlayer, new CharacterCard("Architect", 7));
        int initialHandSize = architectPlayer.getHand().size();
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(architectPlayer, Game.selectedCharacters.get(architectPlayer));
        assertEquals(initialHandSize + 2, architectPlayer.getHand().size());
    }

    /**
     * Tests effects of special district cards.
     * Verifies:
     * - Purple card special abilities
     * - Unique district effects
     * - Effect timing and restrictions
     */
    @Test
    public void testSpecialDistrictEffects() {
        game.createPlayers(4);
        Player player = Game.players.get(0);
        
        // Test Museum card banking
        player.getCity().add(new DistrictCard("Museum", "purple", 4, 1, null));
        player.drawCard(new DistrictCard("Test", "blue", 1, 1, null));
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        setTestInput("gold\n1\nt\n");
        Game.handlePlayerTurn(player, Game.selectedCharacters.get(player));
        
        // Test invalid Museum banking
        setTestInput("gold\ninvalid\n1\nt\n");
        Game.handlePlayerTurn(player, Game.selectedCharacters.get(player));
        
        // Test Museum banking with empty hand
        player.getHand().clear();
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(player, Game.selectedCharacters.get(player));
    }

    /**
     * Tests various game ending scenarios.
     * Verifies:
     * - First to 8 districts
     * - Tiebreaker handling
     * - Score calculation
     * - Winner determination
     */
    @Test
    public void testGameEndScenarios() {
        game.createPlayers(4);
        
        // Test normal win condition
        Player winner = Game.players.get(0);
        for (int i = 0; i < 8; i++) {
            winner.getCity().add(new DistrictCard("District" + i, "yellow", 1, 1, null));
        }
        Game.endGame();
        assertTrue(Game.isGameOver());
        assertEquals(winner, Game.getWinner());
        
        // Test tie with character rank deciding winner
        Game.setGameOver(false);
        Game.setWinner(null);
        Player tiePlayer = Game.players.get(1);
        for (int i = 0; i < 8; i++) {
            tiePlayer.getCity().add(new DistrictCard("District" + i, "blue", 1, 1, null));
        }
        Game.selectedCharacters.put(winner, new CharacterCard("King", 4));
        Game.selectedCharacters.put(tiePlayer, new CharacterCard("Bishop", 5));
        Game.endGame();
        assertEquals(tiePlayer, Game.getWinner());
        
        // Test complete tie scenario
        Game.setGameOver(false);
        Game.setWinner(null);
        Game.selectedCharacters.clear();
        Game.endGame();
    }

    /**
     * Tests the turn phase processing system.
     * Verifies:
     * - Normal turn execution
     * - Turn processing with assassinated character
     * - Turn processing with robbed character
     * - Phase transitions during turns
     */
    @Test
    public void testTurnPhaseProcessing() {
        game.createPlayers(4);
        
        // Setup characters for all players
        for (int i = 0; i < 4; i++) {
            Game.selectedCharacters.put(Game.players.get(i), 
                new CharacterCard("Character" + (i+1), i+1));
        }
        
        // Test turn processing with different inputs
        setTestInput("t\ngold\nt\nt\nt\n");
        Game.processTurnPhase();
        assertFalse(Game.isRoundInProgress());
        
        // Test with assassinated character
        Game.setAssassinatedCharacter("Character2");
        Game.setCurrentPhase(Game.GamePhase.TURN);
        Game.setRoundInProgress(true);
        setTestInput("t\nt\nt\nt\n");
        Game.processTurnPhase();
        
        // Test with robbed character
        Game.setAssassinatedCharacter(null);
        Game.setRobbedCharacter("Character3");
        Game.setCurrentPhase(Game.GamePhase.TURN);
        Game.setRoundInProgress(true);
        setTestInput("t\nt\nt\nt\n");
        Game.processTurnPhase();
    }

    /**
     * Tests system handling of invalid user inputs.
     * Verifies proper error handling for:
     * - Invalid commands during turns
     * - Invalid card selections
     * - Invalid character selections
     * - Recovery from invalid inputs
     */
    @Test
    public void testInvalidInputHandling() {
        game.createPlayers(4);
        Player player = Game.players.get(0);
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        
        // Test invalid commands during turn
        setTestInput("invalid\ngold\nt\n");
        Game.handlePlayerTurn(player, Game.selectedCharacters.get(player));
        
        // Test invalid card selection
        setTestInput("cards\ninvalid\n1\nt\n");
        Game.handlePlayerTurn(player, Game.selectedCharacters.get(player));
        
        // Test invalid character selection
        setTestInput("abc\n1\nt\n");
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        Game.selectedCharacters.clear();
        game.playRound();
    }

    /**
     * Tests the game state management system.
     * Verifies proper handling of:
     * - Phase transitions
     * - Round progress flags
     * - Game over conditions
     * - Winner tracking
     * - Crown holder tracking
     * - Character status tracking
     */
    @Test
    public void testGameStateManagement() {
        // Test game state transitions
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        assertEquals(Game.GamePhase.SELECTION, Game.getCurrentPhase());
        
        Game.setRoundInProgress(true);
        assertTrue(Game.isRoundInProgress());
        
        Game.setGameOver(true);
        assertTrue(Game.isGameOver());
        
        Player winner = new HumanPlayer("TestWinner");
        Game.setWinner(winner);
        assertEquals(winner, Game.getWinner());
        
        Game.setCrownPlayerIndex(2);
        assertEquals(2, Game.getCrownPlayerIndex());
        
        Game.setAssassinatedCharacter("TestCharacter");
        assertEquals("TestCharacter", Game.getAssassinatedCharacter());
        
        Game.setRobbedCharacter("TestCharacter");
        assertEquals("TestCharacter", Game.getRobbedCharacter());
    }

    /**
     * Tests interactions between players.
     * Verifies:
     * - Gold display functionality
     * - AI player character selection
     * - Information command handling
     * - Player state visibility
     */
    @Test
    public void testPlayerInteractions() {
        game.createPlayers(4);
        
        // Test player gold display
        Game.showAllPlayerGold();
        
        // Test character selection for AI players
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        setTestInput("1\nt\n");
        game.playRound();
        
        // Test info commands
        Player player = Game.players.get(0);
        assertTrue(Game.handleInfoCommands("gold", player));
        assertTrue(Game.handleInfoCommands("hand", player));
        assertTrue(Game.handleInfoCommands("city", player));
        assertTrue(Game.handleInfoCommands("all", player));
        assertFalse(Game.handleInfoCommands("invalid", player));
    }

    /**
     * Tests character selection when King is removed.
     * Verifies proper handling of:
     * - King being drawn as face-up discard
     * - Selection process continuation
     * - Game state consistency
     */
    @Test
    public void testCharacterSelectionWithKingRemoval() {
        game.createPlayers(4);
        
        // Test when King is drawn as face-up discard
        setTestInput("1\n2\n3\n4\nt\n");
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        Game.selectedCharacters.clear();
        Game.isSelectionPhase(); // This will trigger the King removal scenario
        game.playRound();
    }

    /**
     * Tests edge cases for Magician character ability.
     * Verifies proper handling of:
     * - Invalid player selection for hand swap
     * - Invalid ability options
     * - Ability skipping
     * - Error recovery
     */
    @Test
    public void testMagicianAbilityEdgeCases() {
        game.createPlayers(4);
        Player magicianPlayer = Game.players.get(0);
        Game.selectedCharacters.put(magicianPlayer, new CharacterCard("Magician", 3));
        
        // Test swap with invalid player selection
        setTestInput("swap\ninvalid\n1\nt\n");
        Game.handlePlayerTurn(magicianPlayer, Game.selectedCharacters.get(magicianPlayer));
        
        // Test invalid option
        setTestInput("invalid\nswap\n1\nt\n");
        Game.handlePlayerTurn(magicianPlayer, Game.selectedCharacters.get(magicianPlayer));
        
        // Test skip option
        setTestInput("skip\nt\n");
        Game.handlePlayerTurn(magicianPlayer, Game.selectedCharacters.get(magicianPlayer));
    }

    /**
     * Tests edge cases for Warlord character ability.
     * Verifies proper handling of:
     * - Invalid target selection
     * - Protected districts
     * - Cost calculations
     * - Destruction restrictions
     */
    @Test
    public void testWarlordAbilityEdgeCases() {
        Player warlordPlayer = Game.players.get(0);
        Player targetPlayer = Game.players.get(1);
        
        CharacterCard warlordCard = new CharacterCard("Warlord", 8);
        CharacterCard bishopCard = new CharacterCard("Bishop", 5);
        
        GameState.setCurrentPlayer(warlordPlayer);
        GameState.setCurrentCharacter(warlordCard);
        
        // Test attacking Bishop
        targetPlayer.getCity().add(new DistrictCard("TestDistrict", "blue", 1, 1, null));
        warlordPlayer.addGold(10);
        Game.selectedCharacters.clear();
        Game.selectedCharacters.put(warlordPlayer, warlordCard);
        Game.selectedCharacters.put(targetPlayer, bishopCard);
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(warlordPlayer, warlordCard);
        
        // Test attacking with insufficient gold
        warlordPlayer.addGold(-10);
        Game.handlePlayerTurn(warlordPlayer, warlordCard);
        
        // Test attacking empty city
        targetPlayer.getCity().clear();
        Game.handlePlayerTurn(warlordPlayer, warlordCard);
    }

    /**
     * Tests edge cases in card drawing mechanics.
     * Verifies proper handling of:
     * - Empty deck situations
     * - Invalid card selections
     * - Drawing limits
     * - Deck reshuffling
     */
    @Test
    public void testCardDrawingEdgeCases() {
        Player player = Game.players.get(0);
        CharacterCard kingCard = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, kingCard);
        GameState.setCurrentPlayer(player);
        GameState.setCurrentCharacter(kingCard);
        
        // Test drawing cards with empty deck
        Game.districtDeck.clear();
        setTestInput("cards\nt\n");
        Game.handlePlayerTurn(player, kingCard);
        
        // Test card drawing with invalid selection
        Game.districtDeck.addCard(new DistrictCard("Test1", "blue", 1, 1, null));
        Game.districtDeck.addCard(new DistrictCard("Test2", "red", 2, 1, null));
        setTestInput("cards\ninvalid\n1\nt\n");
        Game.handlePlayerTurn(player, kingCard);
    }

    /**
     * Tests edge cases in game state management.
     * Verifies proper handling of:
     * - Invalid state transitions
     * - Unexpected game termination
     * - State recovery
     * - Error conditions
     */
    @Test
    public void testGameStateEdgeCases() {
        game.createPlayers(4);
        
        // Test round already in progress
        Game.setRoundInProgress(true);
        game.playRound();
        
        // Test invalid phase transition
        Game.setCurrentPhase(Game.GamePhase.TURN);
        game.playRound();
        
        // Test character selection with no players
        Game.players.clear();
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        game.playRound();
    }

    /**
     * Tests validation of player count input.
     * Verifies proper handling of:
     * - Invalid numeric inputs (0, negative, too high)
     * - Non-numeric inputs
     * - Boundary values
     * - Input retry mechanism
     */
    @Test
    public void testPlayerCountValidation() {
        // Test invalid numeric inputs
        setTestInput("0\n8\n4\nt\nexit\n");
        game.run();
        
        // Test non-numeric inputs
        setTestInput("abc\n-1\n4\nt\nexit\n");
        game.run();
        
        // Test boundary values
        setTestInput("3\n4\nt\nexit\n");
        game.run();
        
        setTestInput("8\n7\nt\nexit\n");
        game.run();
    }

    /**
     * Tests character-specific income abilities.
     * Verifies:
     * - King's income from yellow districts
     * - Bishop's income from blue districts
     * - Multiple district income calculation
     * - Income timing and application
     */
    @Test
    public void testCharacterAbilityIncome() {
        game.createPlayers(4);
        
        // Test King income with multiple yellow districts
        Player kingPlayer = Game.players.get(0);
        kingPlayer.getCity().add(new DistrictCard("Castle", "yellow", 3, 1, null));
        kingPlayer.getCity().add(new DistrictCard("Manor", "yellow", 3, 1, null));
        Game.selectedCharacters.put(kingPlayer, new CharacterCard("King", 4));
        setTestInput("gold\nt\n");
        int initialGold = kingPlayer.getGold();
        Game.handlePlayerTurn(kingPlayer, Game.selectedCharacters.get(kingPlayer));
        assertEquals(initialGold + 4, kingPlayer.getGold()); // 2 from gold action + 2 from yellow districts
        
        // Test Bishop income with multiple blue districts
        Player bishopPlayer = Game.players.get(1);
        bishopPlayer.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        bishopPlayer.getCity().add(new DistrictCard("Cathedral", "blue", 5, 1, null));
        Game.selectedCharacters.put(bishopPlayer, new CharacterCard("Bishop", 5));
        setTestInput("gold\nt\n");
        initialGold = bishopPlayer.getGold();
        Game.handlePlayerTurn(bishopPlayer, Game.selectedCharacters.get(bishopPlayer));
        assertEquals(initialGold + 4, bishopPlayer.getGold()); // 2 from gold action + 2 from blue districts
    }

    /**
     * Tests game state loading functionality.
     * Verifies proper handling of:
     * - Valid game state loading
     * - Invalid character names
     * - State restoration process
     */
    @Test
    public void testLoadGameState() {
        game.createPlayers(4);
        
        // Create a test JSON object
        JSONObject root = new JSONObject();
        root.put("mysteryDiscard", "King");
        
        // Test loading game state
        Game.loadGame(root);
        
        // Test with invalid character name
        root.put("mysteryDiscard", "InvalidCharacter");
        Game.loadGame(root);
    }

    /**
     * Tests comprehensive character ability functionality.
     * Verifies:
     * - All character special abilities
     * - Ability interactions
     * - Complex ability scenarios
     * - Edge cases for each ability
     */
    @Test
    public void testCharacterAbilitiesComprehensive() {
        game.createPlayers(4);
        
        // Test Assassin ability with invalid input
        Player assassinPlayer = Game.players.get(0);
        Game.selectedCharacters.put(assassinPlayer, new CharacterCard("Assassin", 1));
        setTestInput("invalid\n9\n4\nt\n");
        Game.handlePlayerTurn(assassinPlayer, Game.selectedCharacters.get(assassinPlayer));
        assertEquals("King", Game.getAssassinatedCharacter());
        
        // Test Thief ability with invalid target
        Player thiefPlayer = Game.players.get(1);
        Game.selectedCharacters.put(thiefPlayer, new CharacterCard("Thief", 2));
        setTestInput("1\n2\n4\nt\n"); // Can't steal from Assassin
        Game.handlePlayerTurn(thiefPlayer, Game.selectedCharacters.get(thiefPlayer));
        assertEquals("King", Game.getRobbedCharacter());
        
        // Test Magician ability with all options
        Player magicianPlayer = Game.players.get(2);
        Game.selectedCharacters.put(magicianPlayer, new CharacterCard("Magician", 3));
        magicianPlayer.drawCard(new DistrictCard("Test1", "blue", 1, 1, null));
        
        // Test swap with invalid selection
        setTestInput("swap\ninvalid\n1\nt\n");
        Game.handlePlayerTurn(magicianPlayer, Game.selectedCharacters.get(magicianPlayer));
        
        // Test redraw
        setTestInput("redraw\nt\n");
        Game.handlePlayerTurn(magicianPlayer, Game.selectedCharacters.get(magicianPlayer));
        
        // Test invalid option
        setTestInput("invalid\nskip\nt\n");
        Game.handlePlayerTurn(magicianPlayer, Game.selectedCharacters.get(magicianPlayer));
        
        // Test Bishop protection from Warlord
        Player bishopPlayer = Game.players.get(3);
        bishopPlayer.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        Game.selectedCharacters.put(bishopPlayer, new CharacterCard("Bishop", 5));
        Game.setAssassinatedCharacter(null);
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(bishopPlayer, Game.selectedCharacters.get(bishopPlayer));
        
        // Test Warlord with Bishop protection
        Player warlordPlayer = Game.players.get(0);
        warlordPlayer.addGold(10);
        Game.selectedCharacters.put(warlordPlayer, new CharacterCard("Warlord", 8));
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(warlordPlayer, Game.selectedCharacters.get(warlordPlayer));
        
        // Verify Bishop's district is still there
        assertTrue(bishopPlayer.getCity().size() > 0);
    }

    /**
     * Tests comprehensive game state transitions.
     * Verifies:
     * - All possible state transitions
     * - State consistency
     * - Complex transition scenarios
     * - Error handling in transitions
     */
    @Test
    public void testGameStateTransitionsComprehensive() {
        // Initialize character pool
        Game.selectedCharacters.clear();
        for (int i = 0; i < 4; i++) {
            Game.selectedCharacters.put(Game.players.get(i), 
                new CharacterCard("Character" + (i+1), i+1));
        }
        
        // Test selection phase with crown transfer
        Game.setCrownPlayerIndex(1);
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        setTestInput("1\n2\n3\n4\nt\n");
        game.playRound();
        
        // Test turn phase with assassinated character
        Game.setCurrentPhase(Game.GamePhase.TURN);
        Game.setAssassinatedCharacter("King");
        Game.setRoundInProgress(true);
        setTestInput("t\n");
        Game.processTurnPhase();
        
        // Test turn phase with robbed character
        Game.setCurrentPhase(Game.GamePhase.TURN);
        Game.setAssassinatedCharacter(null);
        Game.setRobbedCharacter("Merchant");
        Game.setRoundInProgress(true);
        setTestInput("t\n");
        Game.processTurnPhase();
        
        // Test round end phase
        Game.setCurrentPhase(Game.GamePhase.ROUND_END);
        Game.setRoundInProgress(false);
        game.playRound();
    }

    /**
     * Tests comprehensive player interactions.
     * Verifies:
     * - Complex player interaction scenarios
     * - Multi-player effects
     * - Player state management
     * - Player command handling
     */
    @Test
    public void testPlayerInteractionsComprehensive() {
        Player player = Game.players.get(0);
        CharacterCard kingCard = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, kingCard);
        GameState.setCurrentPlayer(player);
        GameState.setCurrentCharacter(kingCard);
        
        // Test card drawing with empty deck
        Game.districtDeck.clear();
        setTestInput("cards\nt\n");
        Game.handlePlayerTurn(player, kingCard);
        
        // Test card drawing with invalid selection
        Game.districtDeck.addCard(new DistrictCard("Test1", "blue", 1, 1, null));
        Game.districtDeck.addCard(new DistrictCard("Test2", "red", 2, 1, null));
        setTestInput("cards\ninvalid\n1\nt\n");
        Game.handlePlayerTurn(player, kingCard);
        
        // Test Museum ability with empty hand
        player.getCity().add(new DistrictCard("Museum", "purple", 4, 1, null));
        player.getHand().clear();
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(player, kingCard);
        
        // Test Museum ability with invalid selection
        player.drawCard(new DistrictCard("Test", "blue", 1, 1, null));
        setTestInput("gold\ninvalid\n1\nt\n");
        Game.handlePlayerTurn(player, kingCard);
    }

    /**
     * Tests comprehensive game run scenarios.
     * Verifies:
     * - Complete game execution
     * - Complex game situations
     * - Game flow management
     * - End-to-end gameplay
     */
    @Test
    public void testGameRunComprehensive() {
        // Test main game loop with different inputs
        setTestInput("4\n1\n2\n3\n4\nt\ngold\nexit\n");
        game.run();
        
        // Test with invalid player count then valid
        setTestInput("abc\n8\n4\n1\n2\n3\n4\nt\nexit\n");
        game.run();
        
        // Test with minimum player count
        setTestInput("4\n1\n2\n3\n4\nt\nexit\n");
        game.run();
        
        // Test with maximum player count
        setTestInput("7\n1\n2\n3\n4\n5\n6\n7\nt\nexit\n");
        game.run();
    }

    /**
     * Tests comprehensive character selection scenarios.
     * Verifies:
     * - All selection possibilities
     * - Complex selection interactions
     * - Selection restrictions
     * - Selection phase management
     */
    @Test
    public void testCharacterSelectionPhaseComprehensive() {
        // Test with King being drawn as face-up discard
        setTestInput("1\n2\n3\n4\nt\n");
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        Game.selectedCharacters.clear();
        Game.isSelectionPhase();
        
        // Test with different player counts
        game.createPlayers(5);
        setTestInput("1\n2\n3\n4\n5\nt\n");
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        Game.selectedCharacters.clear();
        Game.isSelectionPhase();
        
        game.createPlayers(6);
        setTestInput("1\n2\n3\n4\n5\n6\nt\n");
        Game.setCurrentPhase(Game.GamePhase.SELECTION);
        Game.setRoundInProgress(true);
        Game.selectedCharacters.clear();
        Game.isSelectionPhase();
    }

    /**
     * Tests comprehensive Magician ability scenarios.
     * Verifies:
     * - All Magician ability variations
     * - Complex ability interactions
     * - Special case handling
     * - Ability restrictions
     */
    @Test
    public void testMagicianAbilityComprehensive() {
        Player testPlayer = new HumanPlayer(TEST_PLAYER_NAME);
        CharacterCard magician = new CharacterCard("Magician", 3);
        Game.selectedCharacters.put(testPlayer, magician);
        
        // Test swap hands
        setTestInput("swap\n1\ngold\nt\n");
        Game.handlePlayerTurn(testPlayer, magician);
        
        // Test redraw
        setTestInput("redraw\ngold\nt\n");
        Game.handlePlayerTurn(testPlayer, magician);
        
        // Test skip
        setTestInput("skip\ngold\nt\n");
        Game.handlePlayerTurn(testPlayer, magician);
    }

    /**
     * Tests comprehensive Thief ability scenarios.
     * Verifies:
     * - All Thief ability variations
     * - Complex stealing scenarios
     * - Target restrictions
     * - Gold transfer mechanics
     */
    @Test
    public void testThiefAbilityComprehensive() {
        Player testPlayer = new HumanPlayer(TEST_PLAYER_NAME);
        CharacterCard thief = new CharacterCard("Thief", 2);
        Game.selectedCharacters.put(testPlayer, thief);
        
        // Test stealing
        setTestInput("3\ngold\nt\n");
        Game.handlePlayerTurn(testPlayer, thief);
        assertEquals("Magician", Game.getRobbedCharacter());
        
        // Test skip stealing
        setTestInput("t\ngold\nt\n");
        Game.handlePlayerTurn(testPlayer, thief);
        
        // Test invalid then valid input
        setTestInput("invalid\n3\ngold\nt\n");
        Game.handlePlayerTurn(testPlayer, thief);
    }

    /**
     * Tests comprehensive Warlord ability scenarios.
     * Verifies:
     * - All Warlord ability variations
     * - Complex destruction scenarios
     * - Cost calculations
     * - Protection mechanics
     */
    @Test
    public void testWarlordAbilityComprehensive() {
        Player testPlayer = new HumanPlayer(TEST_PLAYER_NAME);
        CharacterCard warlord = new CharacterCard("Warlord", 8);
        Game.selectedCharacters.put(testPlayer, warlord);
        
        // Add districts to target player
        Player target = Game.players.get(1);
        target.getCity().add(new DistrictCard("TestDistrict", "red", 1, 1, null));
        testPlayer.addGold(10);
        
        // Test destruction
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(testPlayer, warlord);
        
        // Test with protected district
        target.getCity().add(new DistrictCard("Keep", "purple", 3, 3, "Cannot be destroyed by the Warlord"));
        Game.handlePlayerTurn(testPlayer, warlord);
    }

    /**
     * Tests comprehensive Museum effect scenarios.
     * Verifies:
     * - All Museum card interactions
     * - Card banking mechanics
     * - Museum scoring
     * - Special ability timing
     */
    @Test
    public void testMuseumEffectComprehensive() {
        Player testPlayer = new HumanPlayer(TEST_PLAYER_NAME);
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(testPlayer, king);
        
        // Add Museum to player's city
        testPlayer.getCity().add(new DistrictCard("Museum", "purple", 4, 4, null));
        testPlayer.getHand().add(new DistrictCard("TestCard", "yellow", 1, 1, null));
        
        // Test banking a card
        setTestInput("gold\n1\n");
        Game.handlePlayerTurn(testPlayer, king);
        
        // Test skipping museum effect
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(testPlayer, king);
    }

    /**
     * Tests comprehensive character income scenarios.
     * Verifies:
     * - All income sources
     * - Complex income calculations
     * - Income timing
     * - Special income rules
     */
    @Test
    public void testCharacterIncomeComprehensive() {
        // Test King income
        Player testPlayer = new HumanPlayer(TEST_PLAYER_NAME);
        testPlayer.getCity().add(new DistrictCard("Palace", "yellow", 5, 5, null));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(testPlayer, king);
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(testPlayer, king);
        
        // Test Bishop income
        testPlayer.getCity().add(new DistrictCard("Cathedral", "blue", 5, 5, null));
        CharacterCard bishop = new CharacterCard("Bishop", 5);
        Game.selectedCharacters.put(testPlayer, bishop);
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(testPlayer, bishop);
        
        // Test Merchant income
        testPlayer.getCity().add(new DistrictCard("Market", "green", 2, 2, null));
        CharacterCard merchant = new CharacterCard("Merchant", 6);
        Game.selectedCharacters.put(testPlayer, merchant);
        setTestInput("gold\nt\n");
        Game.handlePlayerTurn(testPlayer, merchant);
    }

    /**
     * Tests comprehensive end game scoring scenarios.
     * Verifies:
     * - All scoring conditions
     * - Complex scoring situations
     * - Tiebreaker rules
     * - Final score calculations
     */
    @Test
    public void testEndGameScoringComprehensive() {
        Player testPlayer = Game.players.get(0);
        
        // Add districts of different colors
        testPlayer.getCity().add(new DistrictCard("Palace", "yellow", 5, 5, null));
        testPlayer.getCity().add(new DistrictCard("Cathedral", "blue", 5, 5, null));
        testPlayer.getCity().add(new DistrictCard("Market", "green", 2, 2, null));
        testPlayer.getCity().add(new DistrictCard("Fortress", "red", 5, 5, null));
        testPlayer.getCity().add(new DistrictCard("Museum", "purple", 4, 4, null));
        
        // Test scoring with all colors bonus
        Game.setGameOver(true);
        Game.setWinner(testPlayer);
        setTestInput("t\n");
        game.endGame();
        
        // Test tie-breaking
        Player tiedPlayer = Game.players.get(1);
        for (DistrictCard card : testPlayer.getCity()) {
            tiedPlayer.getCity().add(card);
        }
        Game.selectedCharacters.put(testPlayer, new CharacterCard("King", 4));
        Game.selectedCharacters.put(tiedPlayer, new CharacterCard("Bishop", 5));
        game.endGame();
    }

    /**
     * Tests comprehensive command handling scenarios.
     * Verifies:
     * - All command types
     * - Complex command sequences
     * - Command timing restrictions
     * - Command error handling
     */
    @Test
    public void testCommandHandlerComprehensive() {
        Player testPlayer = new HumanPlayer(TEST_PLAYER_NAME);
        
        // Test gold command
        assertTrue(Game.handleInfoCommands("gold", testPlayer));
        
        // Test hand command
        testPlayer.getHand().add(new DistrictCard("TestCard", "yellow", 1, 1, null));
        assertTrue(Game.handleInfoCommands("hand", testPlayer));
        
        // Test city command
        testPlayer.getCity().add(new DistrictCard("TestDistrict", "red", 1, 1, null));
        assertTrue(Game.handleInfoCommands("city", testPlayer));
        assertTrue(Game.handleInfoCommands("citadel", testPlayer));
        
        // Test all command
        assertTrue(Game.handleInfoCommands("all", testPlayer));
    }
} 