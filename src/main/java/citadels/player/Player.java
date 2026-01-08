package citadels.player;

import citadels.card.DistrictCard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class representing a player in the Citadels game.
 * Each player has a name, gold, a hand of district cards, and a city of built districts.
 * Players can be either human or AI-controlled.
 *
 * @author Lakshya Sakhuja
 * @version 7.0
 */
public abstract class Player {
    /** The player's name */
    protected final String name;
    /** The amount of gold the player has */
    protected int gold;
    /** The district cards in the player's hand */
    protected final List<DistrictCard> hand = new ArrayList<>();
    /** The district cards built in the player's city */
    protected final List<DistrictCard> city = new ArrayList<>();
    /** Cards banked by special abilities (e.g., Museum) */
    private final List<DistrictCard> bankedCards = new ArrayList<>();

    /**
     * Creates a new player with the specified name.
     *
     * @param name the name of the player
     */
    public Player(String name) {
        this.name = name;
        this.gold = 0;
    }

    /**
     * Adds gold to the player's treasury.
     *
     * @param amount the amount of gold to add (can be negative to remove gold)
     */
    public void addGold(int amount) {
        gold += amount;
    }

    /**
     * Spends gold from the player's treasury.
     *
     * @param amount the amount of gold to spend
     */
    public void spendGold(int amount) {
        gold -= amount;
    }

    /**
     * Gets the current amount of gold the player has.
     *
     * @return the player's gold
     */
    public int getGold() {
        return gold;
    }

    /**
     * Adds a district card to the player's hand.
     *
     * @param card the card to add to the hand
     */
    public void drawCard(DistrictCard card) {
        if (card != null) {
            hand.add(card);
        }
    }

    /**
     * Gets the list of cards in the player's hand.
     *
     * @return the list of district cards in hand
     */
    public List<DistrictCard> getHand() {
        return hand;
    }

    /**
     * Gets the list of districts built in the player's city.
     *
     * @return the list of built district cards
     */
    public List<DistrictCard> getCity() {
        return city;
    }

    /**
     * Gets the player's name.
     *
     * @return the player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a list of district cards in the player's hand that they can afford to build.
     *
     * @return List of affordable district cards
     */
    public List<DistrictCard> getAffordableDistricts() {
        return hand.stream()
            .filter(card -> card.getCost() <= gold)
            .collect(Collectors.toList());
    }

    /**
     * Checks if a district card can be built by this player.
     * A district can be built if:
     * <ul>
     *   <li>The player has enough gold</li>
     *   <li>The player doesn't already have this district in their city</li>
     * </ul>
     *
     * @param card the district card to check
     * @return true if the district can be built, false otherwise
     */
    public boolean canBuildDistrict(DistrictCard card) {
        return card != null 
            && gold >= card.getCost() 
            && !hasDistrict(card.getName());
    }

    /**
     * Builds a district from the player's hand into their city.
     * The district is removed from hand and added to the city if:
     * <ul>
     *   <li>The player has enough gold to pay for it</li>
     *   <li>The player doesn't already have this district in their city</li>
     *   <li>The index is valid</li>
     * </ul>
     *
     * @param index the index of the card in the player's hand to build
     * @return true if the district was successfully built, false otherwise
     */
    public boolean buildDistrict(int index) {
        if (index < 0 || index >= hand.size()) {
            System.out.println("Invalid card index.");
            return false;
        }

        DistrictCard card = hand.get(index);

        if (hasDistrict(card.getName())) {
            System.out.println("You already have a " + card.getName() + " in your city.");
            return false;
        }

        if (gold < card.getCost()) {
            System.out.println("Not enough gold to build " + card.getName() 
                + ". (Cost: " + card.getCost() + ", you have: " + gold + ")");
            return false;
        }

        spendGold(card.getCost());
        hand.remove(index);
        city.add(card);
        System.out.println("Built: " + card.getName());
        return true;
    }

    /**
     * Checks if the player has a specific district in their city.
     *
     * @param name the name of the district to check for
     * @return true if the player has built this district, false otherwise
     */
    public boolean hasDistrict(String name) {
        for (DistrictCard c : city) {
            if (c.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    /**
     * Checks if this is a human player.
     *
     * @return true if this is a human player, false if AI
     */
    public abstract boolean isHuman();

    /**
     * Executes this player's turn.
     * Implementation differs between human and AI players.
     */
    public abstract void takeTurn();
    
    /**
     * Banks a card for special district abilities (e.g., Museum).
     *
     * @param card the card to bank
     */
    public void bankCard(DistrictCard card) {
        bankedCards.add(card);
    }

    /**
     * Gets the list of banked cards.
     *
     * @return the list of banked district cards
     */
    public List<DistrictCard> getBankedCards() {
        return bankedCards;
    }

    /**
     * Calculates the total score for this player.
     * Score includes:
     * - Cost of all districts in city
     * - Rainbow bonus (3 points) for having districts of all colors
     * - Any other bonuses from special district effects
     *
     * @return the total score for this player
     */
    public int calculateScore() {
        int score = 0;
        Set<String> colors = new HashSet<>();
        
        // Add up district costs and collect colors
        for (DistrictCard district : city) {
            score += district.getCost();
            colors.add(district.getColor());
        }
        
        // Add rainbow bonus if all colors are present
        if (colors.contains("blue") && colors.contains("yellow") && 
            colors.contains("green") && colors.contains("red") && 
            colors.contains("purple")) {
            score += 3; // Rainbow bonus
        }
        
        return score;
    }
}
