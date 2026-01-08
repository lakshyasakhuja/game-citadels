package citadels.card;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for the CharacterCard class.
 * Tests cover card creation, field access, string representation,
 * and various usage scenarios in collections and game logic.
 */
public class CharacterCardTest {

    /**
     * Tests basic field access of CharacterCard.
     * Verifies that name and rank are correctly stored and retrieved.
     */
    @Test
    public void testCharacterCardFields() {
        CharacterCard card = new CharacterCard("King", 4);
        assertEquals("King", card.getName());
        assertEquals(4, card.getRank());
    }

    /**
     * Tests the string representation of CharacterCard.
     * Verifies that toString() returns the expected format "rank: name".
     */
    @Test
    public void testToStringFormat() {
        CharacterCard card = new CharacterCard("Assassin", 1);
        assertEquals("1: Assassin", card.toString());
    }

    /**
     * Tests that different character cards have different ranks.
     * Verifies rank uniqueness between different character types.
     */
    @Test
    public void testDifferentCardsHaveDifferentRanks() {
        CharacterCard a = new CharacterCard("Warlord", 8);
        CharacterCard b = new CharacterCard("Bishop", 5);
        assertNotEquals(a.getRank(), b.getRank());
    }

    /**
     * Tests that card names are non-null.
     * Verifies that getName() returns a non-null value for valid cards.
     */
    @Test
    public void testCardNameNonNull() {
        CharacterCard card = new CharacterCard("Magician", 3);
        assertNotNull(card.getName());
    }

    /**
     * Tests multiple instances of cards with same values.
     * Verifies that separate instances with same name and rank are equal.
     */
    @Test
    public void testMultipleInstancesWithSameValues() {
        CharacterCard a = new CharacterCard("Merchant", 6);
        CharacterCard b = new CharacterCard("Merchant", 6);
        assertEquals(a.getName(), b.getName());
        assertEquals(a.getRank(), b.getRank());
        assertEquals(a.toString(), b.toString());
    }

    /**
     * Tests using character cards in collections.
     * Verifies proper behavior when cards are stored in arrays
     * and accessed by index.
     */
    @Test
    public void testCharacterCardCollectionUsage() {
        CharacterCard king = new CharacterCard("King", 4);
        CharacterCard bishop = new CharacterCard("Bishop", 5);
        CharacterCard warlord = new CharacterCard("Warlord", 8);

        CharacterCard[] cards = {king, bishop, warlord};
        assertEquals("5: Bishop", cards[1].toString());
        assertTrue(cards[2].getRank() > cards[0].getRank());
    }

    /**
     * Tests character cards with negative ranks.
     * Verifies that cards can be created with negative ranks
     * and maintain proper string representation.
     */
    @Test
    public void testCharacterCardNegativeRank() {
        CharacterCard mysterious = new CharacterCard("Unknown", -1);
        assertEquals(-1, mysterious.getRank());
        assertEquals("Unknown", mysterious.getName());
        assertEquals("-1: Unknown", mysterious.toString());
    }

    /**
     * Tests character cards with zero rank.
     * Verifies that cards can be created with rank zero
     * and maintain proper string representation.
     */
    @Test
    public void testCharacterCardZeroRank() {
        CharacterCard neutral = new CharacterCard("Neutral", 0);
        assertEquals(0, neutral.getRank());
        assertEquals("0: Neutral", neutral.toString());
    }

    /**
     * Tests character card reference equality.
     * Verifies that same reference points to same card instance
     * and maintains consistent string representation.
     */
    @Test
    public void testCharacterCardWithSameReference() {
        CharacterCard original = new CharacterCard("King", 4);
        CharacterCard alias = original;
        assertSame(original, alias);
        assertEquals(original.toString(), alias.toString());
    }

    /**
     * Tests character cards with null names.
     * Verifies that cards can handle null names and
     * provide appropriate string representation.
     */
    @Test
    public void testCharacterCardWithNullName() {
        CharacterCard broken = new CharacterCard(null, 9);
        assertNull(broken.getName());
        assertEquals("9: null", broken.toString());
    }

    /**
     * Tests using character cards in List collections.
     * Verifies proper behavior when cards are stored in
     * and retrieved from ArrayList.
     */
    @Test
    public void testCharacterCardInList() {
        CharacterCard thief = new CharacterCard("Thief", 2);
        CharacterCard magician = new CharacterCard("Magician", 3);

        java.util.List<CharacterCard> list = new java.util.ArrayList<>();
        list.add(thief);
        list.add(magician);

        assertEquals(2, list.size());
        assertEquals("2: Thief", list.get(0).toString());
    }

}
