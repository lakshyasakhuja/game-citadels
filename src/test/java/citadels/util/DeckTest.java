package citadels.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the Deck utility class.
 * Tests all functionality of a generic deck including card operations,
 * deck management, and iteration.
 */
public class DeckTest {
    private Deck<String> deck;

    @BeforeEach
    public void setUp() {
        deck = new Deck<>();
    }

    /**
     * Tests that a newly created deck is empty.
     * Verifies both isEmpty() returns true and size() returns 0.
     */
    @Test
    public void testNewDeckIsEmpty() {
        assertTrue(deck.isEmpty());
        assertEquals(0, deck.size());
    }

    /**
     * Tests adding a single card to the deck.
     * Verifies that the deck is not empty and size increases correctly.
     */
    @Test
    public void testAddCard() {
        deck.addCard("Card1");
        assertFalse(deck.isEmpty());
        assertEquals(1, deck.size());
    }

    /**
     * Tests adding multiple cards to the deck at once.
     * Verifies that all cards are added and size is updated correctly.
     */
    @Test
    public void testAddCards() {
        List<String> cards = Arrays.asList("Card1", "Card2", "Card3");
        deck.addCards(cards);
        assertEquals(3, deck.size());
    }

    /**
     * Tests drawing cards from the deck.
     * Verifies:
     * 1. Cards are drawn in FIFO order
     * 2. Size decreases after each draw
     * 3. Drawing from empty deck returns null
     */
    @Test
    public void testDraw() {
        deck.addCard("Card1");
        deck.addCard("Card2");
        assertEquals("Card1", deck.draw());
        assertEquals(1, deck.size());
        assertEquals("Card2", deck.draw());
        assertTrue(deck.isEmpty());
        assertNull(deck.draw()); // Drawing from empty deck
    }

    /**
     * Tests deck shuffling functionality.
     * Verifies:
     * 1. All cards remain in deck after shuffle
     * 2. Order of at least one card changes
     * Note: There is a very small chance this test could fail even with
     * correct implementation due to random chance.
     */
    @Test
    public void testShuffle() {
        List<String> originalOrder = Arrays.asList("Card1", "Card2", "Card3", "Card4", "Card5");
        deck.addCards(originalOrder);
        
        // Store original order
        String[] before = new String[5];
        int i = 0;
        for (String card : deck) {
            before[i++] = card;
        }
        
        // Shuffle and store new order
        deck.shuffle();
        String[] after = new String[5];
        i = 0;
        for (String card : deck) {
            after[i++] = card;
        }
        
        // Verify size remains same
        assertEquals(5, deck.size());
        
        // Check if at least one card changed position
        boolean orderChanged = false;
        for (i = 0; i < 5; i++) {
            if (!before[i].equals(after[i])) {
                orderChanged = true;
                break;
            }
        }
        assertTrue(orderChanged);
    }

    /**
     * Tests clearing all cards from the deck.
     * Verifies that after clear():
     * 1. Deck is empty
     * 2. Size is zero
     */
    @Test
    public void testClear() {
        deck.addCards(Arrays.asList("Card1", "Card2", "Card3"));
        assertFalse(deck.isEmpty());
        deck.clear();
        assertTrue(deck.isEmpty());
        assertEquals(0, deck.size());
    }

    /**
     * Tests the deck's iterator functionality.
     * Verifies:
     * 1. Cards are iterated in correct order
     * 2. hasNext() returns correct values
     * 3. All cards can be accessed via iterator
     */
    @Test
    public void testIterator() {
        List<String> cards = Arrays.asList("Card1", "Card2", "Card3");
        deck.addCards(cards);
        
        Iterator<String> iterator = deck.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("Card1", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Card2", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("Card3", iterator.next());
        assertFalse(iterator.hasNext());
    }

    /**
     * Tests placing a card at the bottom of the deck.
     * Verifies:
     * 1. Size increases correctly
     * 2. Card order is maintained (FIFO)
     * 3. Cards can be drawn in correct order
     */
    @Test
    public void testPlaceOnBottom() {
        deck.addCard("Card1");
        deck.placeOnBottom("Card2");
        assertEquals(2, deck.size());
        assertEquals("Card1", deck.draw());
        assertEquals("Card2", deck.draw());
    }
} 