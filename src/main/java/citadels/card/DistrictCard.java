package citadels.card;

/**
 * Represents a district card in the Citadels game.
 * District cards are used by players to build their city. Each district has
 * a name, color (representing its type), cost to build, and quantity in the deck.
 * Purple districts also have special abilities.
 *
 * @author Lakshya Sakhuja
 * @version 7.0
 */
public class DistrictCard {
    /** The name of the district */
    private final String name;
    /** The color/type of the district (yellow, blue, green, red, or purple) */
    private final String color;
    /** The cost in gold to build this district */
    private final int cost;
    /** The number of copies of this card in the deck */
    private final int quantity;
    /** The special ability text (only for purple districts) */
    private final String ability; // null unless color == "purple"

    /**
     * Creates a new district card.
     *
     * @param name the name of the district
     * @param color the color/type of the district
     * @param cost the cost in gold to build this district
     * @param quantity the number of copies of this card in the deck
     * @param ability the special ability text (null for non-purple districts)
     */
    public DistrictCard(String name, String color, int cost, int quantity, String ability) {
        this.name = name;
        this.color = color != null ? color.toLowerCase() : null;
        this.cost = cost;
        this.quantity = quantity;
        this.ability = ability;
    }

    /**
     * Gets the name of the district.
     * @return the district's name
     */
    public String getName() { return name; }

    /**
     * Gets the color/type of the district.
     * @return the district's color (yellow, blue, green, red, or purple)
     */
    public String getColor() { return color; }

    /**
     * Gets the cost to build this district.
     * @return the cost in gold
     */
    public int getCost() { return cost; }

    /**
     * Gets the number of copies of this card in the deck.
     * @return the quantity in the deck
     */
    public int getQuantity() { return quantity; }

    /**
     * Gets the special ability text for purple districts.
     * @return the ability text, or null for non-purple districts
     */
    public String getAbility() { return ability; }

    /**
     * Checks if this is a purple district card.
     * Purple districts have special abilities that can be used during the game.
     *
     * @return true if this is a purple district, false otherwise
     */
    public boolean isPurple() {
        return "purple".equalsIgnoreCase(color);
    }

    /**
     * Returns a string representation of the district card.
     * The format is "name (color, Cost: cost)" for cards without abilities,
     * and "name (color, Cost: cost) - ability" for cards with abilities.
     *
     * @return a string representation of the card
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name == null ? "null" : name);
        sb.append(" (");
        sb.append(color == null ? "null" : color);
        sb.append(", Cost: ");
        sb.append(cost);
        sb.append(")");
        
        if (ability != null && !ability.trim().isEmpty()) {
            sb.append(" - ");
            sb.append(ability);
        }
        
        return sb.toString();
    }
}

