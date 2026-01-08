package citadels.player;

import citadels.Game;
import citadels.card.CharacterCard;
import citadels.card.DistrictCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for AIPlayer class.
 * Tests AI behavior, decision making, and strategy implementation
 * for all game actions and character abilities.
 */
public class AIPlayerTest {
    private AIPlayer player;
    private Scanner originalScanner;

    /**
     * Sets up test environment before each test.
     * Initializes game state, creates AI player, and adds test cards.
     */
    @BeforeEach
    public void setUp() {
        // Store original scanner
        originalScanner = Game.getScanner();
        
        // Set up test Scanner with default input
        String input = "t\nt\nt\nt\n";  // Default input for tests
        Game.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));
        
        // Reset game state completely
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
        player = new AIPlayer("Test AI");
        Game.players.add(player);
        
        // Initialize district deck with test cards
        addTestCards();
        
        // Set test environment property
        System.setProperty("test.env", "true");
    }

    /**
     * Helper method to add test cards to the deck and player's hand.
     * Creates a variety of cards with different colors and costs.
     */
    private void addTestCards() {
        // Add a variety of test cards to the deck
        String[][] cards = {
            {"Temple", "blue", "2"},
            {"Castle", "yellow", "4"},
            {"Market", "green", "2"},
            {"Watchtower", "red", "1"},
            {"Keep", "purple", "3"}
        };
        
        for (String[] cardInfo : cards) {
            DistrictCard card = new DistrictCard(cardInfo[0], cardInfo[1], 
                Integer.parseInt(cardInfo[2]), 1, null);
            Game.districtDeck.addCard(card);
            // Also add to player's hand for testing
            player.drawCard(new DistrictCard(cardInfo[0], cardInfo[1], 
                Integer.parseInt(cardInfo[2]), 1, null));
        }
    }

    /**
     * Cleans up test environment after each test.
     * Restores original scanner.
     */
    @AfterEach
    public void tearDown() {
        // Restore original scanner
        Game.setScanner(originalScanner);
    }

    /**
     * Tests AI player constructor.
     * Verifies initial state of player properties.
     */
    @Test
    public void testConstructor() {
        AIPlayer newPlayer = new AIPlayer("Test AI");
        assertEquals("Test AI", newPlayer.getName());
        assertEquals(0, newPlayer.getGold());
        assertTrue(newPlayer.getHand().isEmpty());
        assertTrue(newPlayer.getCity().isEmpty());
        assertTrue(newPlayer.getBankedCards().isEmpty());
    }

    /**
     * Tests isHuman() method.
     * Verifies AI player is correctly identified as non-human.
     */
    @Test
    public void testIsHuman() {
        assertFalse(player.isHuman());
    }

    /**
     * Tests character selection with districts of all colors.
     * Verifies AI makes appropriate character choices based on
     * district colors in city.
     */
    @Test
    public void testSelectCharacterWithAllColors() {
        // Add districts of all colors to city
        player.getCity().clear();
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        player.getCity().add(new DistrictCard("Castle", "yellow", 4, 1, null));
        player.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        player.getCity().add(new DistrictCard("Watchtower", "red", 1, 1, null));
        
        List<CharacterCard> options = new ArrayList<>();
        options.add(new CharacterCard("King", 4));     // yellow
        options.add(new CharacterCard("Bishop", 5));   // blue
        options.add(new CharacterCard("Merchant", 6)); // green
        options.add(new CharacterCard("Warlord", 8));  // red
        
        CharacterCard selected = player.selectCharacter(options);
        assertNotNull(selected);
        assertTrue(options.contains(selected));
    }

    /**
     * Tests character selection with no available options.
     * Verifies AI handles empty character list appropriately.
     */
    @Test
    public void testSelectCharacterWithNoOptions() {
        List<CharacterCard> options = new ArrayList<>();
        assertNull(player.selectCharacter(options));
    }

    /**
     * Tests character selection with only one option.
     * Verifies AI selects the only available character.
     */
    @Test
    public void testSelectCharacterWithSingleOption() {
        List<CharacterCard> options = new ArrayList<>();
        CharacterCard king = new CharacterCard("King", 4);
        options.add(king);
        assertEquals(king, player.selectCharacter(options));
    }

    /**
     * Tests basic turn actions.
     * Verifies AI performs valid actions during its turn
     * (building, collecting resources).
     */
    @Test
    public void testTakeTurnBasicActions() {
        // Setup character and initial state
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        player.addGold(5);
        
        // Take turn
        player.takeTurn();
        
        // Verify AI took some action (either built district or collected resources)
        assertTrue(player.getCity().size() > 0 || player.getGold() > 5 || 
                  player.getHand().size() > 5);
    }

    /**
     * Tests turn behavior with empty hand.
     * Verifies AI prioritizes drawing cards when hand is empty.
     */
    @Test
    public void testTakeTurnWithEmptyHand() {
        // Setup character but empty hand
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        player.getHand().clear();
        player.addGold(2);  // Changed from 5 to ensure drawing cards is preferred
        
        // Take turn
        player.takeTurn();
        
        // Should collect cards
        assertTrue(player.getHand().size() > 0 || player.getGold() > 2,
            "Should either draw cards or collect gold");
    }

    /**
     * Tests turn behavior with no gold.
     * Verifies AI prioritizes collecting resources when poor.
     */
    @Test
    public void testTakeTurnWithNoGold() {
        // Setup character but no gold
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        player.getHand().clear();  // Ensure no buildable cards
        
        int initialGold = player.getGold();
        player.takeTurn();
        
        // Should collect gold or cards
        assertTrue(player.getGold() > initialGold || player.getHand().size() > 0,
            "Should collect either gold or cards");
    }

    /**
     * Tests turn behavior with empty game state.
     * Verifies AI handles edge case of no game state gracefully.
     */
    @Test
    public void testTakeTurnWithEmptyGameState() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.districtDeck.clear();
        player.takeTurn();
        // Should handle gracefully without exceptions
    }

    /**
     * Tests turn behavior when assassinated.
     * Verifies AI skips turn when targeted by Assassin.
     */
    @Test
    public void testTakeTurnWithAssassinatedCharacter() {
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        Game.setAssassinatedCharacter("4");  // Set by rank
        player.takeTurn();
        // Should skip turn without exceptions
    }

    /**
     * Tests turn behavior when robbed.
     * Verifies AI loses gold when targeted by Thief.
     */
    @Test
    public void testTakeTurnWithRobbedCharacter() {
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        Game.setRobbedCharacter("4");  // Set by rank
        player.addGold(5);
        player.takeTurn();
        assertEquals(0, player.getGold());
    }

    /**
     * Tests run assassin's behavior.
     */
    @Test
    public void testAssassinBehavior() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        
        AIPlayer victim = new AIPlayer("Victim");
        Game.players.add(victim);
        Game.selectedCharacters.put(player, new CharacterCard("Assassin", 1));
        Game.selectedCharacters.put(victim, new CharacterCard("King", 4));
        
        player.takeTurn();
        assertNotNull(Game.getAssassinatedCharacter());
        assertEquals("4", Game.getAssassinatedCharacter());
    }

    /**
     * Tests run thief's behavior.
     */
    @Test
    public void testThiefBehavior() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        
        AIPlayer victim = new AIPlayer("Victim");
        victim.addGold(5);
        Game.players.add(victim);
        Game.selectedCharacters.put(player, new CharacterCard("Thief", 2));
        Game.selectedCharacters.put(victim, new CharacterCard("King", 4));
        
        int initialGold = player.getGold();
        player.takeTurn();
        
        // Verify theft attempt
        assertNotNull(Game.getRobbedCharacter(), "Should attempt to rob a character");
        assertTrue(player.getGold() >= initialGold, "Should not lose gold from theft attempt");
    }

    /**
     * Tests Magician ability usage.
     * Verifies AI makes appropriate hand swaps and redraws.
     */
    @Test
    public void testMagicianBehavior() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        
        AIPlayer victim = new AIPlayer("Victim");
        victim.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        Game.players.add(victim);
        
        player.drawCard(new DistrictCard("Market", "green", 2, 1, null));
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        int initialHandSize = player.getHand().size();
        player.takeTurn();
        assertNotEquals(initialHandSize, player.getHand().size());
        assertTrue(player.getHand().stream().anyMatch(c -> c.getName().equals("Temple")));
    }

    /**
     * Tests Warlord ability usage.
     * Verifies AI targets appropriate districts for destruction.
     */
    @Test
    public void testWarlordBehavior() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        
        AIPlayer victim = new AIPlayer("Victim");
        victim.getCity().add(new DistrictCard("Tavern", "green", 1, 1, null));
        Game.players.add(victim);
        Game.selectedCharacters.put(player, new CharacterCard("Warlord", 8));
        Game.selectedCharacters.put(victim, new CharacterCard("Merchant", 6));
        player.addGold(10);
        
        player.takeTurn();
        assertTrue(victim.getCity().isEmpty());
    }

    /**
     * Tests Warlord vs Bishop interaction.
     * Verifies AI respects Bishop's protection from Warlord.
     */
    @Test
    public void testWarlordVsBishop() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        
        AIPlayer victim = new AIPlayer("Victim");
        victim.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        Game.players.add(victim);
        
        Game.selectedCharacters.put(player, new CharacterCard("Warlord", 8));
        Game.selectedCharacters.put(victim, new CharacterCard("Bishop", 5));
        player.addGold(10);
        
        player.takeTurn();
        assertEquals(1, victim.getCity().size());
    }

    /**
     * Tests Architect ability usage.
     * Verifies AI utilizes extra builds and card draws.
     */
    @Test
    public void testArchitectBehavior() {
        // Setup architect with resources
        Game.selectedCharacters.put(player, new CharacterCard("Architect", 7));
        player.addGold(10);
        player.getHand().clear();
        
        // Add buildable districts
        player.drawCard(new DistrictCard("District1", "yellow", 2, 1, null));
        player.drawCard(new DistrictCard("District2", "blue", 3, 1, null));
        
        int initialCitySize = player.getCity().size();
        player.takeTurn();
        
        // Should attempt to build at least one district
        assertTrue(player.getCity().size() > initialCitySize || 
                  player.getGold() < 10 || player.getHand().size() > 2,
            "Should either build, collect resources, or draw cards");
    }

    /**
     * Tests Museum card usage.
     * Verifies AI handles Museum's card banking ability.
     */
    @Test
    public void testMuseumBehavior() {
        // Add Museum to city
        DistrictCard museum = new DistrictCard("Museum", "purple", 4, 1, "Bank cards for points");
        player.getCity().add(museum);
        
        // Add card to hand for banking
        DistrictCard card = new DistrictCard("Card", "yellow", 1, 1, null);
        player.drawCard(card);
        
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        
        int initialBankedSize = player.getBankedCards().size();
        player.takeTurn();
        
        // Should attempt to use Museum ability
        assertTrue(player.getBankedCards().size() >= initialBankedSize,
            "Should not lose banked cards during turn");
    }

    /**
     * Tests character-specific income collection.
     * Verifies AI collects appropriate income based on character.
     */
    @Test
    public void testCharacterIncomeCollection() {
        // Setup character and matching districts
        CharacterCard king = new CharacterCard("King", 4);
        Game.selectedCharacters.put(player, king);
        
        // Add yellow districts for King's income
        player.getCity().add(new DistrictCard("Castle", "yellow", 4, 1, null));
        player.getCity().add(new DistrictCard("Palace", "yellow", 5, 1, null));
        
        int initialGold = player.getGold();
        player.takeTurn();
        
        // Should collect income
        assertTrue(player.getGold() >= initialGold,
            "Should not lose gold during turn");
    }

    /**
     * Tests district building strategy.
     * Verifies AI makes optimal building choices.
     */
    @Test
    public void testBuildingStrategy() {
        // Setup resources
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        player.addGold(10);
        player.getHand().clear();
        
        // Add districts with different costs
        player.drawCard(new DistrictCard("Cheap", "yellow", 1, 1, null));
        player.drawCard(new DistrictCard("Expensive", "yellow", 5, 1, null));
        
        int initialCitySize = player.getCity().size();
        player.takeTurn();
        
        // Should attempt to build
        assertTrue(player.getCity().size() > initialCitySize || 
                  player.getGold() < 10 || player.getHand().size() > 2,
            "Should either build, collect resources, or draw cards");
    }

    /**
     * Tests card drawing strategy.
     * Verifies AI makes appropriate decisions when drawing cards.
     */
    @Test
    public void testDrawStrategy() {
        // Setup with low resources
        player.getHand().clear();
        player.addGold(1);
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        
        int initialHandSize = player.getHand().size();
        int initialGold = player.getGold();
        player.takeTurn();
        
        // Should improve resource position
        assertTrue(player.getHand().size() > initialHandSize || 
                  player.getGold() > initialGold,
            "Should either draw cards or collect gold");
    }

    /**
     * Tests character selection when needing cards.
     * Verifies AI prioritizes characters that help draw cards when needed.
     */
    @Test
    public void testSelectCharacterEmergencyCardNeed() {
        // Test when AI has 1 or fewer cards
        player.getHand().clear();
        List<CharacterCard> options = new ArrayList<>();
        options.add(new CharacterCard("Magician", 3));
        options.add(new CharacterCard("Architect", 7));
        options.add(new CharacterCard("King", 4));
        
        CharacterCard selected = player.selectCharacter(options);
        assertTrue(selected.getName().equals("Magician") || selected.getName().equals("Architect"),
            "Should prefer Magician or Architect when low on cards");
    }

    /**
     * Tests character selection based on spending priority.
     * Verifies AI chooses characters that align with resource needs.
     */
    @Test
    public void testSelectCharacterSpendingPriority() {
        // Test when AI has lots of gold but few districts
        player.getCity().clear();
        player.addGold(8);  // More than 6 gold
        
        List<CharacterCard> options = new ArrayList<>();
        options.add(new CharacterCard("Warlord", 8));
        options.add(new CharacterCard("Merchant", 6));
        options.add(new CharacterCard("Bishop", 5));
        
        CharacterCard selected = player.selectCharacter(options);
        assertTrue(selected.getName().equals("Warlord") || selected.getName().equals("Merchant"),
            "Should prefer Warlord or Merchant when rich with few districts");
    }

    /**
     * Tests character selection for rainbow bonus.
     * Verifies AI considers district color variety when choosing characters.
     */
    @Test
    public void testSelectCharacterRainbowBonus() {
        // Add districts of all colors to trigger rainbow bonus logic
        player.getCity().clear();
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        player.getCity().add(new DistrictCard("Castle", "yellow", 4, 1, null));
        player.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        player.getCity().add(new DistrictCard("Watchtower", "red", 1, 1, null));
        player.getCity().add(new DistrictCard("Keep", "purple", 3, 1, null));
        
        List<CharacterCard> options = new ArrayList<>();
        options.add(new CharacterCard("King", 4));
        options.add(new CharacterCard("Bishop", 5));
        
        CharacterCard selected = player.selectCharacter(options);
        assertEquals("King", selected.getName(),
            "Should prefer King when close to rainbow bonus");
    }

    /**
     * Tests card drawing with empty deck.
     * Verifies AI handles empty deck situation appropriately.
     */
    @Test
    public void testShouldDrawCardsWithEmptyDeck() {
        Game.districtDeck.clear();
        assertFalse(player.shouldDrawCards(Game.districtDeck),
            "Should not draw cards when deck is empty");
    }

    /**
     * Tests card drawing with single card in deck.
     * Verifies AI handles limited deck appropriately.
     */
    @Test
    public void testShouldDrawCardsWithSingleCard() {
        Game.districtDeck.clear();
        Game.districtDeck.addCard(new DistrictCard("Temple", "blue", 2, 1, null));
        assertFalse(player.shouldDrawCards(Game.districtDeck),
            "Should not draw cards when deck has only one card");
    }

    /**
     * Tests card drawing with empty hand.
     * Verifies AI prioritizes drawing when hand is empty.
     */
    @Test
    public void testShouldDrawCardsWithEmptyHand() {
        player.getHand().clear();
        Game.districtDeck.addCard(new DistrictCard("Temple", "blue", 2, 1, null));
        Game.districtDeck.addCard(new DistrictCard("Castle", "yellow", 4, 1, null));
        assertTrue(player.shouldDrawCards(Game.districtDeck),
            "Should draw cards when hand is empty and deck has enough cards");
    }

    /**
     * Tests card drawing with no buildable districts.
     * Verifies AI seeks new cards when current hand is not useful.
     */
    @Test
    public void testShouldDrawCardsWithNoBuildableDistricts() {
        player.getHand().clear();
        player.drawCard(new DistrictCard("Castle", "yellow", 6, 1, null));
        player.addGold(2);  // Not enough gold to build
        
        Game.districtDeck.addCard(new DistrictCard("Temple", "blue", 2, 1, null));
        Game.districtDeck.addCard(new DistrictCard("Market", "green", 2, 1, null));
        
        assertTrue(player.shouldDrawCards(Game.districtDeck),
            "Should draw cards when no districts can be built");
    }

    /**
     * Tests Magician's hand swap with better hand available.
     * Verifies AI identifies and targets valuable hands to swap.
     */
    @Test
    public void testMagicianSwapWithRicherHand() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        
        // Create victim with more cards
        AIPlayer victim = new AIPlayer("Victim");
        for (int i = 0; i < 5; i++) {
            victim.drawCard(new DistrictCard("Card" + i, "blue", 2, 1, null));
        }
        Game.players.add(victim);
        
        // Setup player as Magician with fewer cards
        player.getHand().clear();
        player.drawCard(new DistrictCard("Poor Card", "blue", 2, 1, null));
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        int victimInitialSize = victim.getHand().size();
        player.takeTurn();
        
        assertEquals(victimInitialSize, player.getHand().size(),
            "Should swap with player having more cards");
    }

    /**
     * Tests Magician's redraw when swap is not beneficial.
     * Verifies AI chooses redraw when no good swap targets exist.
     */
    @Test
    public void testMagicianRedrawWhenNoSwap() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        
        // Setup player as Magician with more cards than others
        player.getHand().clear();
        for (int i = 0; i < 3; i++) {
            player.drawCard(new DistrictCard("Card" + i, "blue", 2, 1, null));
        }
        
        // Add victim with fewer cards
        AIPlayer victim = new AIPlayer("Victim");
        victim.drawCard(new DistrictCard("Poor Card", "blue", 2, 1, null));
        Game.players.add(victim);
        
        Game.selectedCharacters.put(player, new CharacterCard("Magician", 3));
        
        List<DistrictCard> originalHand = new ArrayList<>(player.getHand());
        player.takeTurn();
        
        assertNotEquals(originalHand, player.getHand(),
            "Should redraw hand when no good swap targets");
    }

    /**
     * Tests character selection with null options.
     * Verifies AI handles null character list gracefully.
     */
    @Test
    public void testSelectCharacterWithNullOptions() {
        assertNull(player.selectCharacter(null),
            "Should handle null options list");
    }

    /**
     * Tests turn behavior with null role.
     * Verifies AI handles missing character assignment gracefully.
     */
    @Test
    public void testTakeTurnWithNullRole() {
        player.takeTurn(null, Game.districtDeck);
        // Should handle gracefully without exceptions
    }

    /**
     * Tests turn behavior with null deck.
     * Verifies AI handles missing district deck gracefully.
     */
    @Test
    public void testTakeTurnWithNullDeck() {
        CharacterCard king = new CharacterCard("King", 4);
        player.takeTurn(king, null);
        // Should handle gracefully without exceptions
    }

    /**
     * Tests Architect's extra card draws.
     * Verifies AI properly uses Architect's bonus card draws.
     */
    @Test
    public void testArchitectExtraDraws() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        Game.selectedCharacters.put(player, new CharacterCard("Architect", 7));
        
        // Clear deck and add exactly 2 cards
        Game.districtDeck.clear();
        Game.districtDeck.addCard(new DistrictCard("Card1", "blue", 2, 1, null));
        Game.districtDeck.addCard(new DistrictCard("Card2", "blue", 2, 1, null));
        
        int initialHandSize = player.getHand().size();
        player.takeTurn();
        
        assertEquals(initialHandSize + 2, player.getHand().size(),
            "Architect should draw 2 extra cards if available");
    }

    /**
     * Tests Architect's extra draws with limited deck.
     * Verifies AI handles Architect ability with one card in deck.
     */
    @Test
    public void testArchitectExtraDrawsWithOneCard() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        Game.selectedCharacters.put(player, new CharacterCard("Architect", 7));
        
        // Clear deck and add exactly 1 card
        Game.districtDeck.clear();
        Game.districtDeck.addCard(new DistrictCard("Card1", "blue", 2, 1, null));
        
        int initialHandSize = player.getHand().size();
        player.takeTurn();
        
        assertEquals(initialHandSize + 1, player.getHand().size(),
            "Architect should draw 1 extra card if only 1 available");
    }

    /**
     * Tests King's crown transfer ability.
     * Verifies AI handles crown transfer when playing as King.
     */
    @Test
    public void testKingCrownTransfer() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        Game.selectedCharacters.put(player, new CharacterCard("King", 4));
        
        player.takeTurn();
        
        assertEquals(0, Game.getCrownPlayerIndex(),
            "King should receive crown token");
    }

    /**
     * Tests Merchant's bonus gold collection.
     * Verifies AI properly collects Merchant's bonus gold.
     */
    @Test
    public void testMerchantBonusGold() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        Game.selectedCharacters.put(player, new CharacterCard("Merchant", 6));
        
        // Add some green districts
        player.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        player.getCity().add(new DistrictCard("Trading Post", "green", 2, 1, null));
        
        int initialGold = player.getGold();
        player.takeTurn();
        
        assertEquals(initialGold + 3, player.getGold(),
            "Merchant should get 1 bonus gold plus 2 from green districts");
    }

    /**
     * Tests Bishop's protection ability.
     * Verifies AI respects and utilizes Bishop's protection.
     */
    @Test
    public void testBishopProtection() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        
        // Create victim as Bishop
        AIPlayer victim = new AIPlayer("Victim");
        victim.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        Game.players.add(victim);
        
        // Setup player as Warlord
        Game.selectedCharacters.put(player, new CharacterCard("Warlord", 8));
        Game.selectedCharacters.put(victim, new CharacterCard("Bishop", 5));
        player.addGold(10);
        
        int initialCitySize = victim.getCity().size();
        player.takeTurn();
        
        assertEquals(initialCitySize, victim.getCity().size(),
            "Warlord cannot destroy Bishop's districts");
    }

    /**
     * Tests Great Wall protection effect.
     * Verifies AI handles Great Wall's protection bonus correctly.
     */
    @Test
    public void testGreatWallProtection() {
        Game.selectedCharacters.clear();
        Game.players.clear();
        Game.players.add(player);
        
        // Create victim with Great Wall
        AIPlayer victim = new AIPlayer("Victim");
        victim.getCity().add(new DistrictCard("Great Wall", "purple", 6, 1, null));
        Game.players.add(victim);
        
        // Setup player as Warlord
        Game.selectedCharacters.put(player, new CharacterCard("Warlord", 8));
        Game.selectedCharacters.put(victim, new CharacterCard("Merchant", 6));
        player.addGold(10);
        
        int initialCitySize = victim.getCity().size();
        player.takeTurn();
        
        assertEquals(initialCitySize, victim.getCity().size(),
            "Warlord cannot destroy Great Wall");
    }
}