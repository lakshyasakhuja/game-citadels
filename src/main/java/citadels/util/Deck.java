package citadels.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A generic deck of cards implementation.
 * This class provides basic deck operations like drawing, shuffling,
 * and adding cards. It can be used with any card type.
 *
 * @param <T> the type of cards in the deck
 * 
 * @author Lakshya Sakhuja
 * @version 7.0
 */
public class Deck<T> implements Iterable<T> {
    /** The list containing all cards in the deck */
    private final List<T> cards;

    /**
     * Creates a new empty deck.
     */
    public Deck() {
        this.cards = new ArrayList<>();
    }

    /**
     * Adds a single card to the deck.
     *
     * @param card the card to add
     */
    public void addCard(T card) {
        cards.add(card);
    }

    /**
     * Adds multiple cards to the deck.
     *
     * @param newCards the list of cards to add
     */
    public void addCards(List<T> newCards) {
        cards.addAll(newCards);
    }

    /**
     * Randomly shuffles all cards in the deck.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Draws a card from the top of the deck.
     *
     * @return the top card, or null if the deck is empty
     */
    public T draw() {
        return cards.isEmpty() ? null : cards.remove(0);
    }

    /**
     * Checks if the deck is empty.
     *
     * @return true if the deck contains no cards, false otherwise
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Gets the number of cards currently in the deck.
     *
     * @return the number of cards
     */
    public int size() {
        return cards.size();
    }

    /**
     * Removes all cards from the deck.
     */
    public void clear() {
        cards.clear();
    }

    /**
     * Returns an iterator over the cards in the deck.
     *
     * @return an Iterator instance
     */
    @Override
    public Iterator<T> iterator() {
        return cards.iterator();
    }

    /**
     * Places a card at the bottom of the deck.
     *
     * @param card the card to place at the bottom
     */
    public void placeOnBottom(T card) {
        cards.add(card);
    }
}
