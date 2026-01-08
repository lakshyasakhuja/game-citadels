package citadels.effect;

import citadels.card.DistrictCard;
import citadels.player.Player;

import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * Handles special effects and abilities of purple district cards in the Citadels game.
 * This utility class manages:
 * <ul>
 *   <li>Turn-based effects (e.g., Laboratory)</li>
 *   <li>Scoring bonuses (e.g., University, Dragon Gate)</li>
 *   <li>Protection effects (e.g., Keep)</li>
 *   <li>Color modification effects (e.g., School of Magic)</li>
 *   <li>Cost modification effects (e.g., Great Wall)</li>
 * </ul>
 *
 * @author Lakshya Sakhuja
 * @version 7.0
 */
public class PurpleCardEffects {

    /** Tracks which players have used their Laboratory this turn */
    private static final Set<String> labUsed = new HashSet<>();

    /**
     * Resets the tracking of Laboratory usage for a new turn.
     * Should be called at the start of each game turn.
     */
    public static void resetTurnUsage() {
        labUsed.clear();
    }

    /**
     * Applies special effects that can be used during a player's turn.
     * Currently handles:
     * <ul>
     *   <li>Laboratory: Allows discarding a card for 1 gold once per turn</li>
     * </ul>
     *
     * @param player the player whose turn it is
     * @param scanner scanner for reading user input (for human players)
     */
    public static void applyTurnEffects(Player player, Scanner scanner) {
        for (DistrictCard card : player.getCity()) {
            String name = card.getName().toLowerCase();
            if (name.equals("laboratory") && !labUsed.contains(player.getName())) {
                if (player.isHuman()) {
                    System.out.println("Use Laboratory to discard a card for 1 gold? (yes/no)");
                    String response = scanner.nextLine().trim().toLowerCase();
                    if (response.equals("yes") && !player.getHand().isEmpty()) {
                        System.out.println("Choose a card to discard:");
                        for (int i = 0; i < player.getHand().size(); i++) {
                            DistrictCard d = player.getHand().get(i);
                            System.out.println((i + 1) + ". " + d.getName() + " (Cost: " + d.getCost() + ")");
                        }
                        try {
                            int choice = Integer.parseInt(scanner.nextLine().trim()) - 1;
                            if (choice >= 0 && choice < player.getHand().size()) {
                                player.getHand().remove(choice);
                                player.addGold(1);
                                System.out.println("Discarded card. +1 gold.");
                                labUsed.add(player.getName());
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    /**
     * Calculates bonus points from special district effects.
     * Bonuses include:
     * <ul>
     *   <li>+3 points for having all 5 colors</li>
     *   <li>+3 points for having 4 colors plus Haunted City</li>
     *   <li>+2 points for University</li>
     *   <li>+2 points for Dragon Gate</li>
     *   <li>+1 point per card in Museum</li>
     * </ul>
     *
     * @param player the player to calculate bonuses for
     * @return total bonus points
     */
    public static int bonusScore(Player player) {
        int bonus = 0;
        Set<String> colors = new HashSet<>();
        boolean hauntedCity = false;

        for (DistrictCard d : player.getCity()) {
            String color = d.getColor();
            if (d.getName().equalsIgnoreCase("Haunted City")) {
                hauntedCity = true;
                continue;
            }
            colors.add(color);
        }

        if (hauntedCity && colors.size() == 4) {
            bonus += 3;
            System.out.println("Bonus +3 for 4 colors + Haunted City");
        } else if (!hauntedCity && colors.size() >= 5) {
            bonus += 3;
            System.out.println("Bonus +3 for 5 distinct colors");
        }

        for (DistrictCard d : player.getCity()) {
            String name = d.getName().toLowerCase();
            if (name.equals("university")) {
                bonus += 2;
                System.out.println("Bonus +2 for University");
            } else if (name.equals("dragon gate")) {
                bonus += 2;
                System.out.println("Bonus +2 for Dragon Gate");
            } else if (name.equals("museum")) {
                int banked = player.getBankedCards().size();
                bonus += banked;
                System.out.println("Bonus +" + banked + " for Museum (banked cards)");
            }
        }

        return bonus;
    }

    /**
     * Checks if a specific district is protected from effects.
     * Currently checks for:
     * <ul>
     *   <li>Keep: Protects itself from destruction</li>
     * </ul>
     *
     * @param target the player who owns the district
     * @param districtName the name of the district to check
     * @return true if the district is protected, false otherwise
     */
    public static boolean isProtected(Player target, String districtName) {
        for (DistrictCard d : target.getCity()) {
            String name = d.getName().toLowerCase();
            if (name.equals("keep") && d.getColor().equals("purple") && d.getName().equalsIgnoreCase(districtName)) return true;
        }
        return false;
    }

    /**
     * Calculates the cost to destroy a district.
     * Base cost is (district cost - 1), modified by:
     * <ul>
     *   <li>Great Wall: +1 to destruction cost</li>
     * </ul>
     *
     * @param target the player who owns the district
     * @param card the district card to be destroyed
     * @return the modified destruction cost
     */
    public static int destroyCost(Player target, DistrictCard card) {
        int base = card.getCost() - 1;
        for (DistrictCard d : target.getCity()) {
            if (d.getName().equalsIgnoreCase("Great Wall")) {
                return base + 1;
            }
        }
        return base;
    }

    /**
     * Determines the effective color of a district for character income purposes.
     * Special cases:
     * <ul>
     *   <li>School of Magic: Counts as any color for income</li>
     * </ul>
     *
     * @param card the district card to check
     * @param player the owner of the card
     * @param character the character collecting income
     * @return the effective color of the district
     */
    public static String effectiveColor(DistrictCard card, Player player, String character) {
        if (card.getName().equalsIgnoreCase("School Of Magic")) {
            switch (character.toLowerCase()) {
                case "king": return "yellow";
                case "bishop": return "blue";
                case "merchant": return "green";
                case "warlord": return "red";
            }
        }
        return card.getColor();
    }

    /**
     * Checks if a district is protected from the Warlord's destruction ability.
     * Protected districts include:
     * <ul>
     *   <li>Keep: Cannot be destroyed</li>
     * </ul>
     *
     * @param card the district card to check
     * @param owner the player who owns the district
     * @return true if the district is protected from the Warlord
     */
    public static boolean isProtectedFromWarlord(DistrictCard card, Player owner) {
        return card.getName().equalsIgnoreCase("Keep");
    }

    /**
     * Calculates the cost for the Warlord to destroy a district.
     * Base cost is (district cost - 1), modified by:
     * <ul>
     *   <li>Great Wall: +1 to destruction cost</li>
     * </ul>
     *
     * @param card the district card to be destroyed
     * @param owner the player who owns the district
     * @return the modified destruction cost
     */
    public static int getWarlordDestructionCost(DistrictCard card, Player owner) {
        int baseCost = card.getCost() - 1;
        for (DistrictCard d : owner.getCity()) {
            if (d.getName().equalsIgnoreCase("Great Wall")) {
                return baseCost + 1;
            }
        }
        return baseCost;
    }

    /**
     * Checks if a player can use their Laboratory this turn.
     *
     * @return a BooleanSupplier that determines if Laboratory can be used
     */
    public static BooleanSupplier canUseLaboratory() {
        return () -> labUsed.isEmpty();
    }

    /**
     * Marks a player's Laboratory as used for this turn.
     */
    public static void markLaboratoryUsed() {
        labUsed.add("test");
    }
}
