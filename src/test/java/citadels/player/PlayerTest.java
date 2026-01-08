package citadels.player;

import citadels.card.DistrictCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for the abstract Player class.
 * Tests core player functionality including resource management,
 * district building, and card operations.
 */
public class PlayerTest {
    private Player player;

    /**
     * Concrete implementation of Player for testing purposes.
     * Provides minimal implementation of abstract methods.
     */
    private static class TestPlayer extends Player {
        public TestPlayer(String name) {
            super(name);
        }

        @Override
        public boolean isHuman() {
            return false;
        }

        @Override
        public void takeTurn() {
            // Do nothing for testing
        }
    }

    /**
     * Sets up test environment before each test.
     * Creates a new test player instance.
     */
    @BeforeEach
    public void setUp() {
        player = new TestPlayer("Test Player");
    }

    /**
     * Tests player constructor.
     * Verifies initial state of all player properties.
     */
    @Test
    public void testConstructor() {
        assertEquals("Test Player", player.getName());
        assertEquals(0, player.getGold());
        assertTrue(player.getHand().isEmpty());
        assertTrue(player.getCity().isEmpty());
        assertTrue(player.getBankedCards().isEmpty());
    }

    /**
     * Tests gold management operations.
     * Verifies adding, spending, and handling negative gold amounts.
     */
    @Test
    public void testGoldOperations() {
        // Test adding gold
        player.addGold(5);
        assertEquals(5, player.getGold());

        // Test spending gold
        player.spendGold(3);
        assertEquals(2, player.getGold());

        // Test negative gold addition
        player.addGold(-1);
        assertEquals(1, player.getGold());
    }

    /**
     * Tests card drawing functionality.
     * Verifies handling of null cards and valid card draws.
     */
    @Test
    public void testDrawCard() {
        // Test drawing null card
        player.drawCard(null);
        assertTrue(player.getHand().isEmpty());

        // Test drawing valid card
        DistrictCard card = new DistrictCard("Temple", "blue", 2, 1, null);
        player.drawCard(card);
        assertEquals(1, player.getHand().size());
        assertEquals(card, player.getHand().get(0));
    }

    /**
     * Tests affordable district calculation.
     * Verifies proper filtering of districts based on gold.
     */
    @Test
    public void testGetAffordableDistricts() {
        // Add some cards to hand
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        player.drawCard(new DistrictCard("Castle", "yellow", 4, 1, null));
        player.drawCard(new DistrictCard("Palace", "yellow", 5, 1, null));

        // Test with no gold
        assertTrue(player.getAffordableDistricts().isEmpty());

        // Test with some gold
        player.addGold(3);
        assertEquals(1, player.getAffordableDistricts().size());
        assertEquals("Temple", player.getAffordableDistricts().get(0).getName());

        // Test with more gold
        player.addGold(2);
        assertEquals(2, player.getAffordableDistricts().size());
    }

    /**
     * Tests district building validation.
     * Verifies proper checks for gold, duplicates, and null cards.
     */
    @Test
    public void testCanBuildDistrict() {
        DistrictCard card = new DistrictCard("Temple", "blue", 2, 1, null);
        
        // Test with null card
        assertFalse(player.canBuildDistrict(null));

        // Test with insufficient gold
        assertFalse(player.canBuildDistrict(card));

        // Test with sufficient gold
        player.addGold(2);
        assertTrue(player.canBuildDistrict(card));

        // Test with duplicate district
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        assertFalse(player.canBuildDistrict(card));
    }

    /**
     * Tests district building execution.
     * Verifies proper handling of building costs, hand updates,
     * and city updates.
     */
    @Test
    public void testBuildDistrict() {
        // Add a card to hand
        DistrictCard card = new DistrictCard("Temple", "blue", 2, 1, null);
        player.drawCard(card);

        // Test with invalid index
        assertFalse(player.buildDistrict(-1));
        assertFalse(player.buildDistrict(1));

        // Test with insufficient gold
        assertFalse(player.buildDistrict(0));
        assertEquals(1, player.getHand().size());
        assertEquals(0, player.getCity().size());

        // Test with sufficient gold
        player.addGold(2);
        assertTrue(player.buildDistrict(0));
        assertEquals(0, player.getHand().size());
        assertEquals(1, player.getCity().size());
        assertEquals(0, player.getGold());

        // Test building duplicate district
        player.drawCard(new DistrictCard("Temple", "blue", 2, 1, null));
        player.addGold(2);
        assertFalse(player.buildDistrict(0));
    }

    /**
     * Tests district presence checking.
     * Verifies case-insensitive district lookup in city.
     */
    @Test
    public void testHasDistrict() {
        // Test with empty city
        assertFalse(player.hasDistrict("Temple"));

        // Test with district in city
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        assertTrue(player.hasDistrict("Temple"));
        assertTrue(player.hasDistrict("temple")); // Case insensitive

        // Test with non-existent district
        assertFalse(player.hasDistrict("Castle"));
    }

    /**
     * Tests card banking functionality.
     * Verifies proper storage of banked cards.
     */
    @Test
    public void testBankCard() {
        DistrictCard card = new DistrictCard("Temple", "blue", 2, 1, null);
        
        // Test initial state
        assertTrue(player.getBankedCards().isEmpty());

        // Test banking a card
        player.bankCard(card);
        assertEquals(1, player.getBankedCards().size());
        assertEquals(card, player.getBankedCards().get(0));

        // Test banking multiple cards
        DistrictCard card2 = new DistrictCard("Castle", "yellow", 4, 1, null);
        player.bankCard(card2);
        assertEquals(2, player.getBankedCards().size());
        assertTrue(player.getBankedCards().contains(card));
        assertTrue(player.getBankedCards().contains(card2));
    }

    /**
     * Tests building with exact gold amount.
     * Verifies proper resource management when cost equals gold.
     */
    @Test
    public void testBuildDistrictWithExactGold() {
        // Add a card to hand
        DistrictCard card = new DistrictCard("Temple", "blue", 2, 1, null);
        player.drawCard(card);
        player.addGold(2);

        // Test building with exact gold amount
        assertTrue(player.buildDistrict(0));
        assertEquals(0, player.getGold());
        assertEquals(1, player.getCity().size());
        assertEquals(0, player.getHand().size());
    }

    /**
     * Tests building with excess gold.
     * Verifies proper change calculation and resource updates.
     */
    @Test
    public void testBuildDistrictWithExcessGold() {
        // Add a card to hand
        DistrictCard card = new DistrictCard("Temple", "blue", 2, 1, null);
        player.drawCard(card);
        player.addGold(5);

        // Test building with more gold than needed
        assertTrue(player.buildDistrict(0));
        assertEquals(3, player.getGold());
        assertEquals(1, player.getCity().size());
        assertEquals(0, player.getHand().size());
    }

    /**
     * Tests affordable districts with empty hand.
     * Verifies proper handling when player has no cards.
     */
    @Test
    public void testGetAffordableDistrictsWithEmptyHand() {
        // Test with empty hand
        assertTrue(player.getAffordableDistricts().isEmpty());

        // Test with gold but empty hand
        player.addGold(10);
        assertTrue(player.getAffordableDistricts().isEmpty());
    }

    /**
     * Tests building validation with empty city.
     * Verifies proper handling of first district build.
     */
    @Test
    public void testCanBuildDistrictWithEmptyCity() {
        DistrictCard card = new DistrictCard("Temple", "blue", 2, 1, null);
        player.addGold(2);

        // Test with empty city
        assertTrue(player.canBuildDistrict(card));
    }

    /**
     * Tests building with full hand.
     * Verifies proper handling of builds from maximum hand size.
     */
    @Test
    public void testBuildDistrictWithFullHand() {
        // Fill hand with cards
        for (int i = 0; i < 7; i++) {
            player.drawCard(new DistrictCard("District" + i, "yellow", 2, 1, null));
        }
        player.addGold(10);

        // Test building from full hand
        assertTrue(player.buildDistrict(0));
        assertEquals(6, player.getHand().size());
        assertEquals(1, player.getCity().size());
    }
} 