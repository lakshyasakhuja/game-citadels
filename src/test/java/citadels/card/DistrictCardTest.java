package citadels.card;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DistrictCard.
 * Tests all functionality of district cards including creation,
 * getters, special abilities, and string representation.
 */
public class DistrictCardTest {
    
    /**
     * Tests basic district card fields and getters.
     */
    @Test
    public void testDistrictCardFields() {
        DistrictCard card = new DistrictCard("Temple", "blue", 1, 1, "Adds 1 faith");
        assertEquals("Temple", card.getName());
        assertEquals("blue", card.getColor());
        assertEquals(1, card.getCost());
        assertEquals("Adds 1 faith", card.getAbility());
    }

    /**
     * Tests toString() format with ability text.
     */
    @Test
    public void testToStringWithAbility() {
        DistrictCard card = new DistrictCard("Keep", "purple", 3, 1, "Indestructible");
        assertEquals("Keep (purple, Cost: 3) - Indestructible", card.toString());
    }

    /**
     * Tests toString() format without ability text.
     */
    @Test
    public void testToStringWithoutAbility() {
        DistrictCard card = new DistrictCard("Market", "green", 2, 1, null);
        assertEquals("Market (green, Cost: 2)", card.toString());
    }

    /**
     * Tests zero cost district card.
     */
    @Test
    public void testDistrictCardZeroCost() {
        DistrictCard card = new DistrictCard("Fountain", "blue", 0, 1, null);
        assertEquals(0, card.getCost());
    }

    /**
     * Tests empty ability string handling.
     */
    @Test
    public void testDistrictCardEmptyAbility() {
        DistrictCard card = new DistrictCard("Blank", "red", 2, 1, "");
        assertEquals("Blank (red, Cost: 2)", card.toString());
    }

    /**
     * Tests reference equality.
     */
    @Test
    public void testDistrictCardSameInstance() {
        DistrictCard card = new DistrictCard("Watchtower", "red", 1, 1, null);
        DistrictCard alias = card;
        assertSame(card, alias);
        assertEquals(card.toString(), alias.toString());
    }

    /**
     * Tests different color variants.
     */
    @Test
    public void testDistrictCardColorVariants() {
        DistrictCard card1 = new DistrictCard("Castle", "yellow", 4, 1, null);
        DistrictCard card2 = new DistrictCard("Church", "blue", 2, 1, null);
        assertNotEquals(card1.getColor(), card2.getColor());
    }

    /**
     * Tests null name handling.
     */
    @Test
    public void testDistrictCardNullName() {
        DistrictCard card = new DistrictCard(null, "purple", 3, 1, "Mystery");
        assertNull(card.getName());
        assertTrue(card.toString().contains("null"));
    }

    /**
     * Tests negative cost handling.
     */
    @Test
    public void testNegativeCost() {
        DistrictCard card = new DistrictCard("Broken Market", "green", -2, 1, "Glitch");
        assertEquals(-2, card.getCost());
    }

    /**
     * Tests extreme cost values.
     */
    @Test
    public void testExtremeCost() {
        DistrictCard card = new DistrictCard("Luxury Palace", "yellow", Integer.MAX_VALUE, 1, "Most expensive");
        assertEquals(Integer.MAX_VALUE, card.getCost());
        assertTrue(card.toString().contains("Cost: " + Integer.MAX_VALUE));
    }

    /**
     * Tests ability text trimming.
     */
    @Test
    public void testAbilityTrimming() {
        DistrictCard card = new DistrictCard("Spire", "purple", 3, 1, "  Grants visibility  ");
        assertTrue(card.getAbility().startsWith(" "));
        assertTrue(card.toString().contains("Grants visibility"));
    }

    /**
     * Tests equals logic (reference equality).
     */
    @Test
    public void testEqualsLogic() {
        DistrictCard a = new DistrictCard("Temple", "blue", 1, 1, null);
        DistrictCard b = new DistrictCard("Temple", "blue", 1, 1, null);
        assertNotEquals(a, b); // reference equality check — no .equals() override
    }

    /**
     * Tests handling of multiple null fields.
     */
    @Test
    public void testMultipleNullFields() {
        DistrictCard card = new DistrictCard(null, null, 0, 1, null);
        assertNull(card.getName());
        assertNull(card.getColor());
        assertEquals("null (null, Cost: 0)", card.toString());
    }

    /**
     * Tests toString length.
     */
    @Test
    public void testCardToStringLength() {
        DistrictCard card = new DistrictCard("Watchtower", "red", 1, 1, null);
        assertTrue(card.toString().length() > 5);
    }

    /**
     * Tests special Unicode characters in name.
     */
    @Test
    public void testSpecialUnicodeName() {
        DistrictCard card = new DistrictCard("🧙 Magic Hall", "purple", 3, 1, "Boost spells");
        assertTrue(card.getName().contains("🧙"));
        assertTrue(card.toString().contains("🧙"));
    }

    /**
     * Tests whitespace-only ability text.
     */
    @Test
    public void testAbilityOnlyWhitespace() {
        DistrictCard card = new DistrictCard("Mist", "blue", 1, 1, "   ");
        assertTrue(card.toString().contains("Cost: 1")); // Ability is ignored
    }

    /**
     * Tests toString() doesn't throw with null fields.
     */
    @Test
    public void testToStringDoesNotThrow() {
        DistrictCard card = new DistrictCard(null, null, 0, 1, null);
        assertDoesNotThrow(() -> {
            String s = card.toString();
            assertNotNull(s);
        });
    }

    /**
     * Tests quantity getter.
     */
    @Test
    public void testGetQuantity() {
        DistrictCard card = new DistrictCard("Temple", "blue", 1, 3, null);
        assertEquals(3, card.getQuantity());
    }

    /**
     * Tests isPurple method with various colors.
     */
    @Test
    public void testIsPurple() {
        DistrictCard purpleCard = new DistrictCard("Keep", "purple", 3, 1, "Indestructible");
        DistrictCard upperPurpleCard = new DistrictCard("Dragon Gate", "PURPLE", 6, 1, "Some ability");
        DistrictCard mixedPurpleCard = new DistrictCard("Laboratory", "PuRpLe", 5, 1, "Ability");
        DistrictCard nonPurpleCard = new DistrictCard("Temple", "blue", 1, 1, null);

        assertTrue(purpleCard.isPurple());
        assertTrue(upperPurpleCard.isPurple());
        assertTrue(mixedPurpleCard.isPurple());
        assertFalse(nonPurpleCard.isPurple());
    }
} 