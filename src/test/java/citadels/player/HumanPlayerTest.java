package citadels.player;

import citadels.Game;
import citadels.card.CharacterCard;
import citadels.card.DistrictCard;
import citadels.util.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for HumanPlayer class.
 * Tests player interaction, command handling, and game mechanics
 * for human-controlled player actions.
 */
public class HumanPlayerTest {
    private HumanPlayer player;
    private Deck<DistrictCard> districtDeck;

    /**
     * Sets up test environment before each test.
     * Initializes player, deck, and game state with test data.
     */
    @BeforeEach
    public void setUp() {
        player = new HumanPlayer("Test Human");
        districtDeck = new Deck<>();
        districtDeck.addCard(new DistrictCard("Temple", "blue", 2, 1, null));
        districtDeck.addCard(new DistrictCard("Castle", "yellow", 4, 1, null));
        districtDeck.addCard(new DistrictCard("Market", "green", 2, 1, null));
        districtDeck.addCard(new DistrictCard("Watchtower", "red", 1, 1, null));
        districtDeck.addCard(new DistrictCard("Keep", "purple", 3, 1, null));

        // Clear and initialize Game state
        Game.players.clear();
        Game.players.add(player);
        Game.selectedCharacters.clear();
        Game.setAssassinatedCharacter(null);
        Game.setRobbedCharacter(null);
        Game.setCurrentPhase(Game.GamePhase.TURN);

        // Initialize Game.districtDeck
        Game.districtDeck.clear();
        for (DistrictCard card : districtDeck) {
            Game.districtDeck.addCard(new DistrictCard(card.getName(), card.getColor(), card.getCost(), 1, null));
        }

        // Add some cards to hand for testing
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        player.drawCard(new DistrictCard("Castle", "yellow", 4, 1, null));

        // Set up test scanner with default input
        Game.setScanner(new Scanner(new ByteArrayInputStream("t\n".getBytes())));
    }

    /**
     * Helper method to set up test input
     * @param input the input string to use for the test
     */
    @SuppressWarnings("unused")
    private void setupTestInput(String input) {
        Game.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));
    }

    /**
     * Tests player constructor.
     * Verifies initial state of player properties.
     */
    @Test
    public void testConstructor() {
        HumanPlayer newPlayer = new HumanPlayer("Test Human");
        assertEquals("Test Human", newPlayer.getName());
        assertEquals(0, newPlayer.getGold());
        assertTrue(newPlayer.getHand().isEmpty());
        assertTrue(newPlayer.getCity().isEmpty());
        assertTrue(newPlayer.getBankedCards().isEmpty());
    }

    /**
     * Tests isHuman() method.
     * Verifies human player is correctly identified.
     */
    @Test
    public void testIsHuman() {
        assertTrue(player.isHuman());
    }

    /**
     * Tests basic turn execution without arguments.
     * Verifies turn processing with default scanner.
     */
    @Test
    public void testTakeTurnNoArgs() {
        // Test the no-args takeTurn method
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        // Since we can't modify Game.scanner, we'll test the Scanner version directly
        String input = "gold\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
    }

    /**
     * Tests turn execution with valid input.
     * Verifies proper handling of valid commands.
     */
    @Test
    public void testTakeTurnValidInput() {
        // Test valid numeric input
        String input = "gold\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialGold = player.getGold();
        player.takeTurn(scanner);
        assertTrue(player.getGold() > initialGold);
    }

    /**
     * Tests turn execution with invalid input.
     * Verifies recovery and proper handling after invalid commands.
     */
    @Test
    public void testTakeTurnInvalidInput() {
        // Test invalid input followed by valid input
        String input = "invalid\ngold\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialGold = player.getGold();
        player.takeTurn(scanner);
        assertTrue(player.getGold() > initialGold);
    }

    /**
     * Tests turn execution with non-numeric input.
     * Verifies proper handling of non-numeric commands.
     */
    @Test
    public void testTakeTurnNonNumericInput() {
        // Test non-numeric input followed by valid input
        String input = "abc\ngold\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialGold = player.getGold();
        player.takeTurn(scanner);
        assertTrue(player.getGold() > initialGold);
    }

    /**
     * Tests card collection command.
     * Verifies proper handling of card drawing commands.
     */
    @Test
    public void testTakeTurnCollectCardCommand() {
        // Test collect card command
        String input = "cards\n1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialHandSize = player.getHand().size();
        player.takeTurn(scanner);
        assertTrue(player.getHand().size() > initialHandSize);
    }

    /**
     * Tests turn exit command.
     * Verifies proper handling of turn termination.
     */
    @Test
    public void testTakeTurnExitCommand() {
        // Test immediate exit command
        String input = "t\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        player.takeTurn(scanner);
    }

    /**
     * Tests invalid card index handling.
     * Verifies proper recovery from invalid card selection.
     */
    @Test
    public void testTakeTurnInvalidCardIndex() {
        // Test invalid card index
        String input = "cards\n3\n1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialHandSize = player.getHand().size();
        player.takeTurn(scanner);
        assertTrue(player.getHand().size() > initialHandSize);
    }

    /**
     * Tests multiple command execution.
     * Verifies proper handling of command sequences.
     */
    @Test
    public void testTakeTurnMultipleCommands() {
        // Test multiple commands in sequence
        String input = "cards\n1\ngold\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialHandSize = player.getHand().size();
        player.takeTurn(scanner);
        assertTrue(player.getHand().size() > initialHandSize);
    }

    /**
     * Tests turn execution with empty hand.
     * Verifies proper handling when player has no cards.
     */
    @Test
    public void testTakeTurnWithEmptyHand() {
        // Test turn with empty hand
        player.getHand().clear();
        String input = "gold\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialGold = player.getGold();
        player.takeTurn(scanner);
        assertTrue(player.getGold() > initialGold);
    }

    /**
     * Tests turn execution with full hand.
     * Verifies proper handling when player's hand is at capacity.
     */
    @Test
    public void testTakeTurnWithFullHand() {
        // Test turn with full hand
        for (int i = 0; i < 7; i++) {
            player.drawCard(new DistrictCard("District" + i, "yellow", 2, 1, null));
        }
        String input = "gold\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialGold = player.getGold();
        player.takeTurn(scanner);
        assertTrue(player.getGold() > initialGold);
    }

    /**
     * Tests district building command.
     * Verifies proper handling of district construction.
     */
    @Test
    public void testTakeTurnBuildCommand() {
        // Test build command
        player.addGold(5);
        String input = "build 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialCitySize = player.getCity().size();
        player.takeTurn(scanner);
        assertTrue(player.getCity().size() > initialCitySize, "Should build district");
    }

    /**
     * Tests building with invalid district index.
     * Verifies proper recovery from invalid build attempts.
     */
    @Test
    public void testTakeTurnBuildInvalidIndex() {
        // Test build command with invalid index
        player.addGold(5);
        String input = "build 10\nbuild 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialCitySize = player.getCity().size();
        player.takeTurn(scanner);
        assertTrue(player.getCity().size() > initialCitySize, "Should build district after invalid attempt");
    }

    /**
     * Tests building without sufficient gold.
     * Verifies proper handling of insufficient resources.
     */
    @Test
    public void testTakeTurnBuildNoGold() {
        // Test build command with insufficient gold
        player.addGold(0);
        String input = "build 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialCitySize = player.getCity().size();
        player.takeTurn(scanner);
        assertEquals(initialCitySize, player.getCity().size(), "Should not build without gold");
    }

    @Test
    public void testTakeTurnBuildDuplicate() {
        // Test build command with duplicate district
        player.addGold(10);
        DistrictCard temple = new DistrictCard("Temple", "blue", 2, 1, null);
        player.getCity().add(temple);
        String input = "build 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialCitySize = player.getCity().size();
        player.takeTurn(scanner);
        assertEquals(initialCitySize, player.getCity().size(), "Should not build duplicate district");
    }

    @Test
    public void testTakeTurnWithCharacterAbilities() {
        // Test character-specific abilities
        Game.players.clear();
        Game.players.add(player);
        AIPlayer victim = new AIPlayer("Victim");
        Game.players.add(victim);
        victim.addGold(5);

        // Test Assassin
        Game.selectedCharacters.clear();
        Game.selectedCharacters.put(player, new CharacterCard("Assassin", 1));
        Game.selectedCharacters.put(victim, new CharacterCard("King", 4));
        String input = "4\nt\n";  // Kill rank 4 (King)
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertEquals("King", Game.getAssassinatedCharacter(), "Should assassinate King");

        // Test Thief
        Game.selectedCharacters.clear();
        Game.setAssassinatedCharacter(null);
        Game.selectedCharacters.put(player, new CharacterCard("Thief", 2));
        Game.selectedCharacters.put(victim, new CharacterCard("King", 4));
        input = "4\nt\n";  // Rob rank 4 (King)
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertEquals("King", Game.getRobbedCharacter(), "Should rob King");

        // Test Magician
        Game.selectedCharacters.clear();
        Game.setRobbedCharacter(null);
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        Game.selectedCharacters.put(victim, new CharacterCard("King", 4));
        victim.drawCard(new DistrictCard("Market", "green", 2, 1, null));
        input = "swap\n1\nt\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertFalse(player.getHand().isEmpty(), "Should have swapped cards");
    }

    @Test
    public void testTakeTurnWithInvalidCommands() {
        // Test various invalid commands
        String input = "invalid command\nhelp\nstatus\ngold\nhand\ncity\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        player.takeTurn(scanner);
    }

    @Test
    public void testTakeTurnWithFullCity() {
        // Test when city is full
        for (int i = 0; i < 8; i++) {
            player.getCity().add(new DistrictCard("District" + i, "yellow", 2, 1, null));
        }
        player.addGold(10);
        String input = "build 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialCitySize = player.getCity().size();
        player.takeTurn(scanner);
        assertEquals(initialCitySize, player.getCity().size(), "Should not build when city is full");
    }

    @Test
    public void testTakeTurnCollectCardWithPrefix() {
        // Test collect card command with "collect card" prefix
        String input = "collect card 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialHandSize = player.getHand().size();
        player.takeTurn(scanner);
        assertTrue(player.getHand().size() > initialHandSize);
    }

    @Test
    public void testTakeTurnWithInvalidCardSelection() {
        // Test invalid card selection followed by valid input
        String input = "cards\n5\n1\nt\n";  // 5 is invalid index
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialHandSize = player.getHand().size();
        player.takeTurn(scanner);
        assertTrue(player.getHand().size() > initialHandSize);
    }

    @Test
    public void testTakeTurnWithAllInfoCommands() {
        // Test all info commands
        String input = "gold\nhand\ncity\nall\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        player.takeTurn(scanner);
    }

    @Test
    public void testTakeTurnWithMagicianRedraw() {
        // Test Magician's redraw ability
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));

        String input = "redraw\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
    }

    @Test
    public void testTakeTurnWithMagicianInvalidOption() {
        // Test Magician with invalid option
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));

        String input = "invalid\nskip\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
    }

    @Test
    public void testTakeTurnWithAssassinSkip() {
        // Test Assassin skipping target selection
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        Game.selectedCharacters.put(player, new CharacterCard("Assassin", 1));

        String input = "t\n";  // Skip assassination
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertNull(Game.getAssassinatedCharacter());
    }

    @Test
    public void testTakeTurnWithThiefSkip() {
        // Test Thief skipping target selection
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        Game.selectedCharacters.put(player, new CharacterCard("Thief", 2));

        String input = "t\n";  // Skip robbery
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertNull(Game.getRobbedCharacter());
    }

    @Test
    public void testTakeTurnWithInvalidCharacterRank() {
        // Test invalid character rank input for Assassin
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        Game.selectedCharacters.put(player, new CharacterCard("Assassin", 1));

        String input = "9\nt\n";  // Invalid rank
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertNull(Game.getAssassinatedCharacter());
    }

    @Test
    public void testTakeTurnWithMuseum() {
        // Test Museum special ability
        player.addGold(10);
        player.getCity().add(new DistrictCard("Museum", "purple", 4, 1, null));
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));

        String input = "1\nt\n";  // Bank first card
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialBankedSize = player.getBankedCards().size();
        player.takeTurn(scanner);
        assertTrue(player.getBankedCards().size() > initialBankedSize);
    }

    @Test
    public void testTakeTurnWithMuseumSkip() {
        // Test skipping Museum banking
        player.addGold(10);
        player.getCity().add(new DistrictCard("Museum", "purple", 4, 1, null));
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));

        String input = "t\n";  // Skip banking
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialBankedSize = player.getBankedCards().size();
        player.takeTurn(scanner);
        assertEquals(initialBankedSize, player.getBankedCards().size());
    }

    @Test
    public void testTakeTurnWithAssassinAbility() {
        Game.players.clear();
        Game.players.add(player);
        AIPlayer victim = new AIPlayer("Victim");
        Game.players.add(victim);

        // Test Assassin with valid target
        Game.selectedCharacters.clear();
        Game.selectedCharacters.put(player, new CharacterCard("Assassin", 1));
        Game.selectedCharacters.put(victim, new CharacterCard("King", 4));
        String input = "4\n";  // Kill rank 4 (King)
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertEquals("4", Game.getAssassinatedCharacter(), "Should assassinate rank 4");

        // Test Assassin with invalid target then valid
        Game.setAssassinatedCharacter(null);
        input = "9\n1\n4\n";  // Invalid rank, then rank 1 (invalid), then rank 4
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertEquals("4", Game.getAssassinatedCharacter(), "Should assassinate rank 4 after invalid attempts");

        // Test Assassin skip
        Game.setAssassinatedCharacter(null);
        input = "t\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertNull(Game.getAssassinatedCharacter(), "Should skip assassination");
    }

    @Test
    public void testTakeTurnWithThiefAbility() {
        Game.players.clear();
        Game.players.add(player);
        AIPlayer victim = new AIPlayer("Victim");
        Game.players.add(victim);
        victim.addGold(5);

        // Test Thief with valid target
        Game.selectedCharacters.clear();
        Game.selectedCharacters.put(player, new CharacterCard("Thief", 2));
        Game.selectedCharacters.put(victim, new CharacterCard("King", 4));
        String input = "4\n";  // Rob rank 4 (King)
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertEquals("4", Game.getRobbedCharacter(), "Should rob rank 4");

        // Test Thief with invalid target then valid
        Game.setRobbedCharacter(null);
        input = "9\n1\n4\n";  // Invalid rank, then rank 1 (Assassin - invalid), then rank 4
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertEquals("4", Game.getRobbedCharacter(), "Should rob rank 4 after invalid attempts");

        // Test Thief skip
        Game.setRobbedCharacter(null);
        input = "t\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertNull(Game.getRobbedCharacter(), "Should skip robbery");
    }

    @Test
    public void testTakeTurnWithMagicianAbility() {
        Game.players.clear();
        Game.players.add(player);
        AIPlayer victim = new AIPlayer("Victim");
        Game.players.add(victim);

        // Setup initial hands
        player.getHand().clear();
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        victim.drawCard(new DistrictCard("Market", "green", 2, 1, null));

        // Test Magician swap
        Game.selectedCharacters.clear();
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        Game.selectedCharacters.put(victim, new CharacterCard("King", 4));
        String input = "swap\n1\nt\n";  // Swap with player 1, then end turn
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertTrue(player.getHand().stream().anyMatch(c -> c.getName().equals("Market")), "Should have Market after swap");
        assertTrue(victim.getHand().stream().anyMatch(c -> c.getName().equals("Temple")), "Should have Temple after swap");

        // Test Magician redraw
        player.getHand().clear();
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        Game.districtDeck.addCard(new DistrictCard("Market", "green", 2, 1, null));
        input = "redraw\nt\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertFalse(player.getHand().isEmpty(), "Should have cards after redraw");
        assertFalse(player.getHand().stream().anyMatch(c -> c.getName().equals("Temple")), "Should not have Temple after redraw");

        // Test Magician skip
        input = "skip\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
    }

    @Test
    public void testTakeTurnWithMuseumAbility() {
        // Test Museum special ability
        player.getCity().add(new DistrictCard("Museum", "purple", 4, 1, null));
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        
        String input = "1\nt\n";  // Bank the first card
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialBankedSize = player.getBankedCards().size();
        player.takeTurn(scanner);
        assertEquals(initialBankedSize + 1, player.getBankedCards().size(), "Should bank a card with Museum");
    }

    @Test
    public void testTakeTurnWithMuseumAbilityWithEmptyHand() {
        // Test Museum ability when hand is empty
        player.getCity().add(new DistrictCard("Museum", "purple", 4, 1, null));
        player.getHand().clear();
        
        String input = "1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialBankedSize = player.getBankedCards().size();
        player.takeTurn(scanner);
        assertEquals(initialBankedSize, player.getBankedCards().size(), "Should not bank card when hand is empty");
    }

    @Test
    public void testTakeTurnWithNoCharacter() {
        // Test turn handling when no character is assigned
        Game.selectedCharacters.clear();
        
        String input = "gold\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));

        int initialGold = player.getGold();
        player.takeTurn(scanner);
        assertEquals(initialGold, player.getGold(), "Should not take actions without a character");
    }

    @Test
    public void testCollectCardCommand() {
        // Test the 'collect card' command
        String input = "collect card 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialHandSize = player.getHand().size();
        player.takeTurn(scanner);
        assertTrue(player.getHand().size() > initialHandSize, "Should collect a card");
    }

    @Test
    public void testCollectCardInvalidIndex() {
        // Test 'collect card' command with invalid index
        String input = "collect card 999\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialHandSize = player.getHand().size();
        player.takeTurn(scanner);
        assertEquals(initialHandSize, player.getHand().size(), "Should not collect card with invalid index");
    }

    @Test
    public void testBuildDuplicateDistrict() {
        // Test attempting to build a duplicate district
        player.addGold(100);  // Ensure enough gold
        DistrictCard temple = new DistrictCard("Temple", "blue", 2, 1, null);
        player.getCity().add(temple);
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        
        String input = "build 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialCitySize = player.getCity().size();
        player.takeTurn(scanner);
        assertEquals(initialCitySize, player.getCity().size(), "Should not build duplicate district");
    }

    @Test
    public void testHandCommand() {
        // Test the 'hand' command display
        String input = "hand\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        player.takeTurn(scanner);
        // Note: We can't directly test console output, but we're testing the command executes without error
    }

    @Test
    public void testCityCommand() {
        // Test the 'city' command display
        player.getCity().add(new DistrictCard("Castle", "yellow", 4, 1, null));
        
        String input = "city\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        player.takeTurn(scanner);
        // Note: We can't directly test console output, but we're testing the command executes without error
    }

    @Test
    public void testInvalidCommand() {
        // Test handling of invalid command
        String input = "invalidcommand\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        player.takeTurn(scanner);
        // Command should be ignored and turn should complete
    }

    @Test
    public void testBuildWithInsufficientGold() {
        // Test attempting to build with insufficient gold
        String input = "build 1\nt\n";  // Try to build first card in hand
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialCitySize = player.getCity().size();
        player.takeTurn(scanner);
        assertEquals(initialCitySize, player.getCity().size(), "Should not build district without sufficient gold");
    }

    @Test
    public void testMaximumHandSize() {
        // Test handling when hand is at maximum size (7 cards)
        while (player.getHand().size() < 7) {
            player.drawCard(new DistrictCard("District" + player.getHand().size(), "yellow", 2, 1, null));
        }
        
        String input = "cards\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialHandSize = player.getHand().size();
        player.takeTurn(scanner);
        assertEquals(initialHandSize, player.getHand().size(), "Should not draw card when hand is full");
    }

    @Test
    public void testMaximumCitySize() {
        // Test handling when city is at maximum size (8 districts)
        player.addGold(100);  // Ensure enough gold
        for (int i = 0; i < 8; i++) {
            player.getCity().add(new DistrictCard("District" + i, "yellow", 2, 1, null));
        }
        
        String input = "build 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);

        int initialCitySize = player.getCity().size();
        player.takeTurn(scanner);
        assertEquals(initialCitySize, player.getCity().size(), "Should not build district when city is full");
    }

    @Test
    public void testMagicianWithNoOtherPlayers() {
        // Test Magician when there are no other players to swap with
        Game.players.clear();
        Game.players.add(player);
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        String input = "swap\nskip\nt\n";  // Try swap, then skip when no players available
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        
        int initialHandSize = player.getHand().size();
        player.takeTurn(scanner);
        assertEquals(initialHandSize, player.getHand().size(), "Hand size should not change when swap fails");
    }

    @Test
    public void testMagicianRedrawWithEmptyDeck() {
        // Test Magician's redraw ability when deck is empty
        Game.players.clear();
        Game.players.add(player);
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        Game.districtDeck.clear();  // Empty the deck
        
        @SuppressWarnings("unused")
        List<DistrictCard> originalHand = new ArrayList<>(player.getHand());
        String input = "redraw\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        
        player.takeTurn(scanner);
        assertTrue(player.getHand().isEmpty(), "Hand should be empty after redraw with empty deck");
    }

    @Test
    public void testAssassinTargetingInvalidRanks() {
        // Test Assassin targeting invalid ranks
        Game.players.clear();
        Game.players.add(player);
        Game.selectedCharacters.put(player, new CharacterCard("Assassin", 1));
        
        String input = "9\n0\n-1\nt\n";  // Try various invalid ranks
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        
        player.takeTurn(scanner);
        assertNull(Game.getAssassinatedCharacter(), "No character should be assassinated with invalid ranks");
    }

    @Test
    public void testThiefTargetingAssassin() {
        // Test Thief trying to target Assassin (rank 1)
        Game.players.clear();
        Game.players.add(player);
        AIPlayer victim = new AIPlayer("Victim");
        Game.players.add(victim);
        Game.selectedCharacters.put(player, new CharacterCard("Thief", 2));
        Game.selectedCharacters.put(victim, new CharacterCard("Assassin", 1));
        
        String input = "1\nt\n";  // Try to rob Assassin (rank 1)
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        
        player.takeTurn(scanner);
        assertNull(Game.getRobbedCharacter(), "Should not be able to rob Assassin");
    }

    @Test
    public void testCommandsWithWhitespace() {
        // Test commands with various whitespace formatting
        String input = "  gold  \n\thand\t\n   city   \n  build  1  \nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        
        int initialGold = player.getGold();
        player.takeTurn(scanner);
        assertTrue(player.getGold() > initialGold, "Commands should work with extra whitespace");
    }

    @Test
    public void testCommandsWithMixedCase() {
        // Test commands with mixed case
        String input = "GoLd\nHaNd\nCiTy\nBuIlD 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        
        int initialGold = player.getGold();
        player.takeTurn(scanner);
        assertTrue(player.getGold() > initialGold, "Commands should work with mixed case");
    }

    @Test
    public void testPartialCommands() {
        // Test partial/incomplete commands
        String input = "buil\nbui\nb\ncollect\ncollec\ncol\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        
        player.takeTurn(scanner);
        // Should handle partial commands gracefully
    }

    @Test
    public void testCharacterIncomeWithMixedDistricts() {
        // Test income collection with mixed district colors
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        player.getCity().add(new DistrictCard("Castle", "yellow", 4, 1, null));
        player.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        player.getCity().add(new DistrictCard("Watchtower", "red", 1, 1, null));
        
        // Test King income (yellow)
        String input = "t\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        
        int initialGold = player.getGold();
        player.takeTurn(scanner);
        assertEquals(initialGold + 1, player.getGold(), "King should get gold for one yellow district");
    }

    @Test
    public void testCharacterIncomeWithNoMatchingDistricts() {
        // Test income collection with no matching districts
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        player.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        
        // Test King income with no yellow districts
        String input = "t\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        
        int initialGold = player.getGold();
        player.takeTurn(scanner);
        assertEquals(initialGold, player.getGold(), "King should get no gold without yellow districts");
    }

    @Test
    public void testMerchantBonusWithoutGreenDistricts() {
        // Test Merchant's +1 gold bonus without green districts
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        player.getCity().add(new DistrictCard("Castle", "yellow", 4, 1, null));
        
        String input = "t\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard merchant = new CharacterCard("Merchant", 6);
        Game.selectedCharacters.put(player, merchant);
        
        int initialGold = player.getGold();
        player.takeTurn(scanner);
        assertEquals(initialGold + 1, player.getGold(), "Merchant should get +1 gold bonus even without green districts");
    }

    @Test
    public void testDrawFromEmptyDeck() {
        // Test drawing when deck is empty
        Game.districtDeck.clear();
        
        String input = "cards\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        
        int initialHandSize = player.getHand().size();
        player.takeTurn(scanner);
        assertEquals(initialHandSize, player.getHand().size(), "Should not add cards from empty deck");
    }

    @Test
    public void testDrawWithOneCardInDeck() {
        // Test drawing when deck has exactly one card
        Game.districtDeck.clear();
        Game.districtDeck.addCard(new DistrictCard("Temple", "blue", 2, 1, null));
        
        String input = "cards\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        
        int initialHandSize = player.getHand().size();
        player.takeTurn(scanner);
        assertEquals(initialHandSize + 1, player.getHand().size(), "Should draw the last card from deck");
    }

    @Test
    public void testMagicianRedrawWithLimitedDeck() {
        // Test Magician's redraw when deck has fewer cards than hand size
        Game.districtDeck.clear();
        Game.districtDeck.addCard(new DistrictCard("Temple", "blue", 2, 1, null));
        player.getHand().clear();
        for (int i = 0; i < 3; i++) {
            player.drawCard(new DistrictCard("District" + i, "yellow", 2, 1, null));
        }
        
        String input = "redraw\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard magician = new CharacterCard("Magician", 3);
        Game.selectedCharacters.put(player, magician);
        
        player.takeTurn(scanner);
        assertEquals(1, player.getHand().size(), "Should only draw available cards from deck");
    }

    @Test
    public void testNumericInputValidation() {
        // Test various numeric input formats
        String input = " 1 \n\t2\t\n-1\n1.5\nt\n";  // Various numeric formats
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard assassin = new CharacterCard("Assassin", 1);
        Game.selectedCharacters.put(player, assassin);
        
        player.takeTurn(scanner);
        assertNull(Game.getAssassinatedCharacter(), "Should handle invalid numeric inputs gracefully");
    }

    @Test
    public void testBuildWithExactGold() {
        // Test building when player has exactly enough gold
        player.addGold(2);  // Temple costs 2
        String input = "build 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        
        int initialCitySize = player.getCity().size();
        player.takeTurn(scanner);
        assertEquals(initialCitySize + 1, player.getCity().size(), "Should build with exact gold amount");
        assertEquals(0, player.getGold(), "Should spend all gold");
    }

    @Test
    public void testMagicianSwapWithEmptyHand() {
        // Test Magician swap when one player has empty hand
        Game.players.clear();
        Game.players.add(player);
        AIPlayer other = new AIPlayer("Other");
        other.getHand().clear();  // Empty hand
        Game.players.add(other);
        
        String input = "swap\n1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard magician = new CharacterCard("Magician", 3);
        Game.selectedCharacters.put(player, magician);
        
        List<DistrictCard> initialHand = new ArrayList<>(player.getHand());
        player.takeTurn(scanner);
        assertTrue(player.getHand().isEmpty(), "Should have empty hand after swap");
        assertEquals(initialHand, other.getHand(), "Other player should have original hand");
    }

    @Test
    public void testBuildWithFullCityAndNoGold() {
        // Test build command with both full city and no gold
        for (int i = 0; i < 8; i++) {
            player.getCity().add(new DistrictCard("District" + i, "yellow", 2, 1, null));
        }
        player.addGold(0);
        
        String input = "build 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        
        int initialCitySize = player.getCity().size();
        player.takeTurn(scanner);
        assertEquals(initialCitySize, player.getCity().size(), "Should not build with full city and no gold");
    }

    @Test
    public void testCollectCardWithFullHandAndEmptyDeck() {
        // Test collect card command with both full hand and empty deck
        while (player.getHand().size() < 7) {
            player.drawCard(new DistrictCard("District" + player.getHand().size(), "yellow", 2, 1, null));
        }
        Game.districtDeck.clear();
        
        String input = "collect card 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        
        int initialHandSize = player.getHand().size();
        player.takeTurn(scanner);
        assertEquals(initialHandSize, player.getHand().size(), "Should not collect card with full hand and empty deck");
    }

    @Test
    public void testCharacterAbilityWithEmptyCityAndHand() {
        // Test character abilities when city and hand are empty
        player.getCity().clear();
        player.getHand().clear();
        
        // Test each character
        CharacterCard[] characters = {
            new CharacterCard("King", 4),
            new CharacterCard("Bishop", 5),
            new CharacterCard("Merchant", 6),
            new CharacterCard("Warlord", 8)
        };
        
        for (CharacterCard character : characters) {
            String input = "t\n";
            Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
            Game.selectedCharacters.put(player, character);
            
            int initialGold = player.getGold();
            player.takeTurn(scanner);
            
            if (character.getName().equals("Merchant")) {
                assertEquals(initialGold + 1, player.getGold(), "Merchant should still get bonus gold");
            } else {
                assertEquals(initialGold, player.getGold(), "Should not get district income with empty city");
            }
        }
    }

    @Test
    public void testTakeTurnWithWarlordAbility() {
        // Set up game state
        Game.selectedCharacters.put(player, new CharacterCard("Warlord", 8));
        player.addGold(10);
        
        // Create victim with a district
        AIPlayer victim = new AIPlayer("Victim");
        victim.getCity().add(new DistrictCard("Tavern", "green", 1, 1, null));
        Game.players.add(victim);
        Game.selectedCharacters.put(victim, new CharacterCard("Merchant", 6));
        
        // Test destroying a district
        String input = "destroy 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertTrue(victim.getCity().isEmpty());
    }

    @Test
    public void testTakeTurnWithWarlordVsBishop() {
        // Set up game state
        Game.selectedCharacters.put(player, new CharacterCard("Warlord", 8));
        player.addGold(10);
        
        // Create victim with a district and Bishop character
        AIPlayer victim = new AIPlayer("Victim");
        victim.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        Game.players.add(victim);
        Game.selectedCharacters.put(victim, new CharacterCard("Bishop", 5));
        
        // Test attempting to destroy Bishop's district
        String input = "destroy 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertFalse(victim.getCity().isEmpty());
    }

    @Test
    public void testTakeTurnWithKingAbility() {
        // Set up game state with yellow districts
        player.getCity().add(new DistrictCard("Castle", "yellow", 4, 1, null));
        player.getCity().add(new DistrictCard("Palace", "yellow", 5, 1, null));
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        
        int initialGold = player.getGold();
        String input = "t\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertEquals(initialGold + 2, player.getGold()); // Should get 2 gold from yellow districts
    }

    @Test
    public void testTakeTurnWithBishopAbility() {
        // Set up game state with blue districts
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        player.getCity().add(new DistrictCard("Church", "blue", 2, 1, null));
        Game.selectedCharacters.put(player, new CharacterCard("Bishop", 5));
        
        int initialGold = player.getGold();
        String input = "t\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertEquals(initialGold + 2, player.getGold()); // Should get 2 gold from blue districts
    }

    @Test
    public void testTakeTurnWithMerchantAbility() {
        // Set up game state with green districts
        player.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        player.getCity().add(new DistrictCard("Tavern", "green", 1, 1, null));
        Game.selectedCharacters.put(player, new CharacterCard("Merchant", 6));
        
        int initialGold = player.getGold();
        String input = "t\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertEquals(initialGold + 3, player.getGold()); // Should get 2 gold from green districts + 1 bonus
    }

    @Test
    public void testTakeTurnWithArchitectAbilityAndFullHand() {
        // Fill hand with cards
        for (int i = 0; i < 7; i++) {
            player.drawCard(new DistrictCard("District" + i, "yellow", 2, 1, null));
        }
        Game.selectedCharacters.put(player, new CharacterCard("Architect", 7));
        
        String input = "t\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertEquals(7, player.getHand().size()); // Hand should remain at max size
    }

    @Test
    public void testTakeTurnWithMuseumAndFullHand() {
        // Add Museum to city
        player.getCity().add(new DistrictCard("Museum", "purple", 4, 1, null));
        
        // Fill hand with cards
        for (int i = 0; i < 7; i++) {
            player.drawCard(new DistrictCard("District" + i, "yellow", 2, 1, null));
        }
        
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        String input = "bank 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertFalse(player.getBankedCards().isEmpty());
    }

    @Test
    public void testTakeTurnWithInvalidBuildIndex() {
        player.addGold(10);
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        
        String input = "build 99\nbuild 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertEquals(1, player.getCity().size());
    }

    @Test
    public void testTakeTurnWithInvalidBankIndex() {
        player.getCity().add(new DistrictCard("Museum", "purple", 4, 1, null));
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        
        String input = "bank 99\nbank 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertEquals(1, player.getBankedCards().size());
    }

    @Test
    public void testTakeTurnWithInvalidDestroyIndex() {
        Game.selectedCharacters.put(player, new CharacterCard("Warlord", 8));
        player.addGold(10);
        
        AIPlayer victim = new AIPlayer("Victim");
        victim.getCity().add(new DistrictCard("Tavern", "green", 1, 1, null));
        Game.players.add(victim);
        Game.selectedCharacters.put(victim, new CharacterCard("Merchant", 6));
        
        String input = "destroy 99\ndestroy 1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertTrue(victim.getCity().isEmpty());
    }

    @Test
    public void testTakeTurnWithInvalidMagicianTarget() {
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        
        AIPlayer victim = new AIPlayer("Victim");
        victim.drawCard(new DistrictCard("Market", "green", 2, 1, null));
        Game.players.add(victim);
        Game.selectedCharacters.put(victim, new CharacterCard("Merchant", 6));
        
        String input = "swap\n99\n1\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        player.takeTurn(scanner);
        assertTrue(player.getHand().stream().anyMatch(c -> c.getName().equals("Market")));
    }

    @Test
    public void testTakeTurnWithEmptyDeckAndNoGold() {
        // Test behavior when deck is empty and player has no gold
        Game.districtDeck.clear();
        player.getHand().clear();
        player.addGold(0);
        
        String input = "cards\ngold\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        
        player.takeTurn(scanner);
        assertEquals(2, player.getGold()); // Should still get base gold
    }

    @Test
    public void testTakeTurnWithMultipleBuildsAsArchitect() {
        // Test Architect's ability to build multiple districts
        player.addGold(10);
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        player.drawCard(new DistrictCard("Market", "green", 2, 1, null));
        player.drawCard(new DistrictCard("Watchtower", "red", 1, 1, null));
        
        String input = "build 1\nbuild 2\nbuild 3\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard architect = new CharacterCard("Architect", 7);
        Game.selectedCharacters.put(player, architect);
        
        player.takeTurn(scanner);
        assertEquals(3, player.getCity().size()); // Should build 3 districts
    }

    @Test
    public void testTakeTurnWithMuseumAndDuplicateCards() {
        // Test Museum ability with duplicate cards in hand
        player.getCity().add(new DistrictCard("Museum", "purple", 4, 1, null));
        player.getHand().clear();
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        
        String input = "bank 1\nbank 2\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        
        player.takeTurn(scanner);
        assertEquals(2, player.getBankedCards().size()); // Should bank both cards
    }

    @Test
    public void testTakeTurnWithWarlordAndProtectedDistricts() {
        // Test Warlord's inability to destroy protected districts
        AIPlayer victim = new AIPlayer("Victim");
        victim.getCity().add(new DistrictCard("Great Wall", "purple", 6, 1, null));
        victim.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        Game.players.add(victim);
        
        player.addGold(10);
        String input = "destroy 1\ndestroy 2\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard warlord = new CharacterCard("Warlord", 8);
        Game.selectedCharacters.put(player, warlord);
        Game.selectedCharacters.put(victim, new CharacterCard("Merchant", 6));
        
        player.takeTurn(scanner);
        assertEquals(2, victim.getCity().size()); // Great Wall should protect districts
    }

    @Test
    public void testTakeTurnWithMagicianAndEmptyTargetHand() {
        // Test Magician's swap ability when target has no cards
        AIPlayer victim = new AIPlayer("Victim");
        Game.players.add(victim);
        
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        String input = "magician swap 2\nmagician redraw\nt\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        CharacterCard magician = new CharacterCard("Magician", 3);
        Game.selectedCharacters.put(player, magician);
        Game.selectedCharacters.put(victim, new CharacterCard("King", 4));
        
        int initialHandSize = player.getHand().size();
        player.takeTurn(scanner);
        assertNotEquals(initialHandSize, player.getHand().size()); // Should redraw instead
    }
} 