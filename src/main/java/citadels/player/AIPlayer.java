package citadels.player;

import citadels.Game;
import citadels.card.CharacterCard;
import citadels.card.DistrictCard;
import citadels.effect.PurpleCardEffects;
import citadels.util.Deck;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents an AI-controlled player in the Citadels game.
 * This class implements the game logic for computer-controlled players,
 * making strategic decisions about character selection, resource management,
 * and district building.
 *
 * @author Lakshya Sakhuja
 * @version 7.0
 */
public class AIPlayer extends Player {
    /** Random number generator for making probabilistic decisions */
    @SuppressWarnings("unused")
	private static final Random RNG = new Random();

    /**
     * Creates a new AI player with the specified name.
     *
     * @param name the name of the player
     */
    public AIPlayer(String name) {
        super(name);
    }

    /**
     * Indicates that this is not a human player.
     *
     * @return false, as this is an AI player
     */
    @Override
    public boolean isHuman() {
        return false;
    }

    /**
     * Selects a character card for the AI player based on strategic considerations.
     * The AI uses the following strategy:
     * <ul>
     *   <li>If low on cards, prefers Architect or Magician</li>
     *   <li>If rich but has few districts, prefers Warlord or Merchant</li>
     *   <li>If close to endgame rainbow bonus, prefers King</li>
     *   <li>Otherwise picks highest rank character (breaking ties randomly)</li>
     * </ul>
     *
     * @param options the list of available character cards to choose from
     * @return the selected character card
     */
    public CharacterCard selectCharacter(List<CharacterCard> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }

        int cards = getHand().size();
        int gold  = getGold();
        int built = getCity().size();

        // a) emergency card need
        if (cards <= 1) {
            List<CharacterCard> want = options.stream()
                .filter(c -> c.getName().equalsIgnoreCase("Magician")
                          || c.getName().equalsIgnoreCase("Architect"))
                .collect(Collectors.toList());
            if (!want.isEmpty()) {
                return want.get(0);  // Always pick first for deterministic testing
            }
        }

        // b) spending priority
        if (gold >= 6 && built < 4) {
            List<CharacterCard> want = options.stream()
                .filter(c -> c.getName().equalsIgnoreCase("Warlord")
                          || c.getName().equalsIgnoreCase("Merchant"))
                .collect(Collectors.toList());
            if (!want.isEmpty()) {
                return want.get(0);  // Always pick first for deterministic testing
            }
        }

        // c) endgame rainbow bonus
        Set<String> colors = getCity().stream()
            .map(DistrictCard::getColor)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        if (colors.containsAll(new HashSet<>(
                Arrays.asList("red","blue","green","yellow","purple")))) {
            Optional<CharacterCard> king = options.stream()
                .filter(c -> c.getName().equalsIgnoreCase("King"))
                .findFirst();
            if (king.isPresent()) {
                return king.get();
            }
        }

        // d) fallback: highest rank
        return options.stream()
            .max(Comparator.comparingInt(CharacterCard::getRank))
            .orElse(null);
    }

    /**
     * Executes this AI player's turn using the current game state.
     * Delegates to the two-argument version of takeTurn.
     */
    @Override
    public void takeTurn() {
        CharacterCard role = Game.selectedCharacters.get(this);
        if (role == null) {
            return;  // No character assigned
        }

        // Check if assassinated
        if (String.valueOf(role.getRank()).equals(Game.getAssassinatedCharacter())) {
            System.out.println(getName() + " was assassinated and skips their turn.");
            return;
        }

        // Check if robbed
        if (String.valueOf(role.getRank()).equals(Game.getRobbedCharacter())) {
            int stolenGold = getGold();
            addGold(-stolenGold);  // Remove all gold
            System.out.println(getName() + " was robbed of " + stolenGold + " gold.");
        }

        takeTurn(role, Game.districtDeck);
    }

    /**
     * Executes a full AI turn with the given role and district deck.
     * The AI performs the following actions in order:
     * <ol>
     *   <li>Special character actions (Assassin/Thief)</li>
     *   <li>Resource collection (draw cards or take gold)</li>
     *   <li>Purple card effects and role income</li>
     *   <li>District destruction (as Warlord)</li>
     *   <li>Building districts</li>
     * </ol>
     *
     * @param role the character card the AI is playing as
     * @param districtDeck the deck of district cards
     */
    public void takeTurn(CharacterCard role, Deck<DistrictCard> districtDeck) {
        if (role == null || districtDeck == null) {
            return;
        }

        String name = role.getName();

        // 1) Assassin special action
        if ("Assassin".equalsIgnoreCase(name)) {
            for (int targetRank = 8; targetRank >= 2; targetRank--) {  // Start with highest rank
                final int rank = targetRank;
                Optional<CharacterCard> victim = Game.selectedCharacters.values().stream()
                    .filter(c -> c.getRank() == rank
                              && !"Assassin".equalsIgnoreCase(c.getName()))
                    .findFirst();
                if (victim.isPresent()) {
                    Game.setAssassinatedCharacter(String.valueOf(victim.get().getRank()));
                    System.out.println(getName() + " assassinated " + victim.get().getName());
                    break;
                }
            }
        }

        // 2) Thief special action
        if ("Thief".equalsIgnoreCase(name)) {
            Player richest = Game.players.stream()
                .filter(p -> 
                {CharacterCard c = Game.selectedCharacters.get(p);
                    if (c == null || c.getRank() == 1  // Can't rob Assassin (rank 1)
                        || String.valueOf(c.getRank()).equals(Game.getAssassinatedCharacter())) {
                        return false;
                    }
                    return true;
                })
                .max(Comparator.comparingInt(Player::getGold))
                .orElse(null);
            if (richest != null) {
                Game.setRobbedCharacter(
                    String.valueOf(Game.selectedCharacters.get(richest).getRank()));
                System.out.println(getName() + " robbed "
                    + Game.selectedCharacters.get(richest).getName());
            }
        }

        // 3) Draw vs Gold
        if (shouldDrawCards(districtDeck)) {
            if (!districtDeck.isEmpty()) {
                DistrictCard c1 = districtDeck.draw();
                if (!districtDeck.isEmpty()) {
                    DistrictCard c2 = districtDeck.draw();
                    DistrictCard pick = (c1.getCost() >= c2.getCost()) ? c1 : c2;
                    drawCard(pick);
                    districtDeck.placeOnBottom(c1 == pick ? c2 : c1);
                    System.out.println(getName() + " drew two and kept " + pick.getName());
                } else {
                    drawCard(c1);
                    System.out.println(getName() + " drew one card");
                }
            }
        } else {
            addGold(2);
            System.out.println(getName() + " took 2 gold (has " + getGold() + ")");
        }

        // 4) Purple‐card effects & role income
        PurpleCardEffects.applyTurnEffects(this, null);
        int income = 0;
        switch (name.toLowerCase()) {
            case "king":
                income = (int) getCity().stream()
                    .filter(d -> PurpleCardEffects.effectiveColor(d, this, "king").equals("yellow"))
                    .count();
                Game.setCrownPlayerIndex(Game.players.indexOf(this));
                break;
            case "bishop":
                income = (int) getCity().stream()
                    .filter(d -> PurpleCardEffects.effectiveColor(d, this, "bishop").equals("blue"))
                    .count();
                break;
            case "merchant":
                income = (int) getCity().stream()
                    .filter(d -> PurpleCardEffects.effectiveColor(d, this, "merchant").equals("green"))
                    .count() + 1;  // Merchant gets 1 bonus gold
                break;
            case "architect":
                if (!districtDeck.isEmpty()) {
                    drawCard(districtDeck.draw());
                    if (!districtDeck.isEmpty()) {
                        drawCard(districtDeck.draw());
                        System.out.println(getName() + " drew 2 extra cards.");
                    } else {
                        System.out.println(getName() + " drew 1 extra card.");
                    }
                }
                break;
            case "warlord":
                income = (int) getCity().stream()
                    .filter(d -> PurpleCardEffects.effectiveColor(d, this, "warlord").equals("red"))
                    .count();
                break;
            case "magician":
                // Try to swap with richest player if they have more cards
                Player swapTarget = Game.players.stream()
                    .filter(p -> p != this)
                    .filter(p -> p.getHand().size() > getHand().size())
                    .max(Comparator.comparingInt(p -> p.getHand().size()))
                    .orElse(null);
                
                if (swapTarget != null) {
                    List<DistrictCard> myHand = new ArrayList<>(getHand());
                    List<DistrictCard> theirHand = new ArrayList<>(swapTarget.getHand());
                    getHand().clear();
                    swapTarget.getHand().clear();
                    getHand().addAll(theirHand);
                    swapTarget.getHand().addAll(myHand);
                    System.out.println(getName() + " swapped hands with " + swapTarget.getName());
                } else if (!getHand().isEmpty()) {
                    // Redraw if can't swap
                    List<DistrictCard> oldHand = new ArrayList<>(getHand());
                    getHand().clear();
                    for (int i = 0; i < oldHand.size() && !districtDeck.isEmpty(); i++) {
                        drawCard(districtDeck.draw());
                    }
                    oldHand.forEach(districtDeck::placeOnBottom);
                    System.out.println(getName() + " redrew their hand");
                }
                break;
        }
        if (income > 0) {
            addGold(income);
            System.out.println(getName() + " gains " + income + " gold from role income.");
        }

        // 5) Warlord destruction
        if ("Warlord".equalsIgnoreCase(name)) {
            destroyWithWarlord();
        }

        // 6) Build districts
        int maxBuilds = "Architect".equalsIgnoreCase(name) ? 3 : 1;
        int builds = 0;
        
        while (builds < maxBuilds) {
            List<DistrictCard> buildable = getHand().stream()
                .filter(c -> c.getCost() <= getGold()
                         && !hasDistrict(c.getName()))
                .sorted(Comparator.comparingInt(DistrictCard::getCost).reversed())
                .collect(Collectors.toList());

            if (buildable.isEmpty()) {
                break;
            }

            DistrictCard toBuild = buildable.get(0);
            int idx = getHand().indexOf(toBuild);
            if (buildDistrict(idx)) {
                builds++;
                System.out.println(getName() + " built " + toBuild.getName() 
                    + " [" + toBuild.getColor() + toBuild.getCost() 
                    + "]. Remaining gold=" + getGold());
            } else {
                break;
            }
        }

        // 7) Museum ability
        if (hasDistrict("Museum") && !getHand().isEmpty()) {
            DistrictCard toBank = getHand().get(0);  // Bank first card
            bankCard(toBank);
            System.out.println(getName() + " banked " + toBank.getName() + " in Museum");
        }

        System.out.println(getName() + " ends turn as " + role.getRank() + ": " + role.getName());
    }

    /**
     * Determines whether the AI should draw cards or take gold.
     * The AI will draw cards if:
     * <ul>
     *   <li>Hand is empty</li>
     *   <li>Has no buildable districts and deck has enough cards</li>
     *   <li>Has less than 2 cards and deck has enough cards</li>
     * </ul>
     *
     * @param deck the district deck to draw from
     * @return true if the AI should draw cards, false if it should take gold
     */
    public boolean shouldDrawCards(Deck<DistrictCard> deck) {
        if (deck.isEmpty() || deck.size() == 1) {
            return false;  // Can't draw 2 cards
        }

        if (getHand().isEmpty()) {
            return true;  // Need cards
        }

        // Check if we have any buildable districts
        boolean hasBuildable = getHand().stream()
            .anyMatch(c -> c.getCost() <= getGold() && !hasDistrict(c.getName()));

        if (!hasBuildable && deck.size() >= 2) {
            return true;  // Can't build anything, try to get new cards
        }

        return getHand().size() < 2 && deck.size() >= 2;  // Keep minimum hand size
    }

    /**
     * Handles the Warlord's district destruction ability.
     * The AI targets:
     * <ul>
     *   <li>Players close to winning</li>
     *   <li>Expensive districts that can be destroyed</li>
     *   <li>Districts that would prevent bonuses</li>
     * </ul>
     */
    private void destroyWithWarlord() {
        // Find the most expensive destroyable district
        Optional<AbstractMap.SimpleEntry<Player, DistrictCard>> target = Game.players.stream()
            .filter(p -> p != this)  // Can't destroy own districts
            .filter(p -> {
                CharacterCard c = Game.selectedCharacters.get(p);
                return c != null && !c.getName().equalsIgnoreCase("Bishop");  // Can't destroy Bishop's districts
            })
            .flatMap(p -> p.getCity().stream()
                .filter(d -> !d.getName().equalsIgnoreCase("Great Wall"))  // Can't destroy if protected
                .filter(d -> d.getCost() - 1 <= getGold())  // Must be able to afford destruction
                .map(d -> new AbstractMap.SimpleEntry<>(p, d)))
            .max(Comparator.comparingInt(e -> e.getValue().getCost()));

        if (target.isPresent()) {
            Player victim = target.get().getKey();
            DistrictCard district = target.get().getValue();
            victim.getCity().remove(district);
            addGold(-(district.getCost() - 1));
            System.out.println(getName() + " destroyed " + victim.getName() + "'s "
                + district.getName() + " for " + (district.getCost() - 1) + " gold.");
        }
    }
}
