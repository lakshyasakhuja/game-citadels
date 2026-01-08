package citadels.card;

/**
 * Represents a character card in the Citadels game.
 * Each character has a unique name and rank that determines their turn order
 * and special abilities during the game.
 *
 * @author Lakshya Sakhuja
 * @version 7.0
 */
public class CharacterCard {
    /** The name of the character */
    private final String name;
    /** The rank of the character (1-8) */
    private final int rank;

    /**
     * Creates a new character card.
     *
     * @param name the name of the character
     * @param rank the rank of the character (1-8, determines turn order)
     */
    public CharacterCard(String name, int rank) {
        this.name = name;
        this.rank = rank;
    }

    /**
     * Gets the name of the character.
     *
     * @return the character's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the rank of the character.
     *
     * @return the character's rank (1-8)
     */
    public int getRank() {
        return rank;
    }

    /**
     * Returns a string representation of the character card.
     * The format is "rank: name".
     *
     * @return a string in the format "rank: name"
     */
    @Override
    public String toString() {
        return rank + ": " + name;
    }
}
