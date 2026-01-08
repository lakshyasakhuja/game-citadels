package citadels.player;

import citadels.Game;
import citadels.card.CharacterCard;
import citadels.card.DistrictCard;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a human player in the Citadels game.
 * This class handles user input and interaction for a human-controlled player,
 * allowing them to make decisions during their turn through the console.
 *
 * @author Lakshya Sakhuja
 * @version 7.0
 */
public class HumanPlayer extends Player {

    /**
     * Creates a new human player with the specified name.
     *
     * @param name the name of the player
     */
    public HumanPlayer(String name) {
        super(name);
    }

    /**
     * Indicates that this is a human player.
     *
     * @return true, as this is a human player
     */
    @Override
    public boolean isHuman() {
        return true;
    }

    /**
     * Executes this player's turn.
     * Delegates to the two-argument version using Game.getScanner().
     */
    @Override
    public void takeTurn() {
        takeTurn(Game.getScanner());
    }

    /**
     * Executes this player's turn with a specific Scanner.
     * Handles user input for card selection and actions during the turn.
     * The player can:
     * <ul>
     *   <li>Select cards when drawing</li>
     *   <li>Execute commands through the CommandHandler</li>
     * </ul>
     *
     * @param scanner the Scanner to use for input
     */
    public void takeTurn(Scanner scanner) {
        CharacterCard character = Game.selectedCharacters.get(this);
        if (character == null) return;

        // Handle character-specific abilities first
        String name = character.getName();
        if (name.equals("Assassin")) {
            System.out.println("Who do you want to kill? Choose a character from 2-8 or 't' to skip:");
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("t") || input.equals("end")) {
                    Game.setAssassinatedCharacter(null);
                    break;
                }
                try {
                    int rank = Integer.parseInt(input);
                    if (rank >= 2 && rank <= 8) {
                        Game.setAssassinatedCharacter(String.valueOf(rank));
                        break;
                    }
                } catch (NumberFormatException ignored) {}
                System.out.println("Invalid choice. Enter a number 2-8 or 't' to skip.");
            }
            return;  // Return after handling Assassin ability
        }

        if (name.equals("Thief")) {
            System.out.println("Who do you want to rob? Choose a character from 2-8 or 't' to skip:");
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("t") || input.equals("end")) {
                    Game.setRobbedCharacter(null);
                    break;
                }
                try {
                    int rank = Integer.parseInt(input);
                    if (rank >= 2 && rank <= 8 && rank != 1) {  // Can't rob Assassin
                        Game.setRobbedCharacter(String.valueOf(rank));
                        break;
                    }
                } catch (NumberFormatException ignored) {}
                System.out.println("Invalid choice. Enter a number 2-8 or 't' to skip.");
            }
            return;  // Return after handling Thief ability
        }

        if (name.equals("Magician")) {
            System.out.println("Choose action: swap (with another player), redraw (your hand), or skip:");
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("skip") || input.equals("t")) {
                    break;
                }
                if (input.equals("redraw")) {
                    // Store old hand
                    List<DistrictCard> oldHand = new ArrayList<>(getHand());
                    // Clear hand and draw new cards
                    getHand().clear();
                    for (int i = 0; i < oldHand.size(); i++) {
                        DistrictCard newCard = Game.districtDeck.draw();
                        if (newCard != null) {
                            drawCard(newCard);
                        }
                    }
                    // Return old cards to deck
                    for (DistrictCard card : oldHand) {
                        Game.districtDeck.addCard(card);
                    }
                    Game.districtDeck.shuffle();
                    break;
                }
                if (input.equals("swap")) {
                    // List other players
                    List<Player> others = new ArrayList<>(Game.players);
                    others.remove(this);
                    if (others.isEmpty()) {
                        System.out.println("No other players to swap with.");
                        break;
                    }
                    System.out.println("Choose player to swap with (1-" + others.size() + "):");
                    for (int i = 0; i < others.size(); i++) {
                        System.out.println((i+1) + ". " + others.get(i).getName());
                    }
                    try {
                        int choice = Integer.parseInt(scanner.nextLine().trim());
                        if (choice >= 1 && choice <= others.size()) {
                            Player other = others.get(choice - 1);
                            // Store both hands
                            List<DistrictCard> myOldHand = new ArrayList<>(getHand());
                            List<DistrictCard> theirOldHand = new ArrayList<>(other.getHand());
                            // Clear both hands
                            getHand().clear();
                            other.getHand().clear();
                            // Swap hands
                            getHand().addAll(theirOldHand);
                            other.getHand().addAll(myOldHand);
                            System.out.println("Swapped hands with " + other.getName());
                            break;
                        }
                    } catch (NumberFormatException ignored) {}
                    System.out.println("Invalid choice.");
                    continue;
                }
                System.out.println("Invalid choice. Enter 'swap', 'redraw', or 'skip'.");
            }
            return;  // Return after handling Magician ability
        }

        // Normal turn actions
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.startsWith("collect card")) {
                if (getHand().size() >= 7) {
                    System.out.println("Your hand is full.");
                    continue;
                }
                input = input.replace("collect card", "").trim();
                try {
                    int index = Integer.parseInt(input) - 1;
                    if (index >= 0 && index < getHand().size()) {
                        drawCard(Game.districtDeck.draw());
                        System.out.println("Drew a card.");
                    }
                } catch (NumberFormatException ignored) {
                    System.out.println("Invalid card index.");
                }
                continue;
            }

            // Handle info commands
            if (input.equals("gold")) {
                addGold(2);  // Collect 2 gold
                System.out.println("Collected 2 gold. You now have " + getGold() + " gold.");
                continue;
            }
            if (input.equals("hand")) {
                if (getHand().isEmpty()) {
                    System.out.println("Your hand is empty.");
                } else {
                    System.out.println("Your hand:");
                    for (int i = 0; i < getHand().size(); i++) {
                        DistrictCard card = getHand().get(i);
                        System.out.println((i+1) + ". " + card.getName() + " [" + card.getColor() + card.getCost() + "]");
                    }
                }
                continue;
            }
            if (input.equals("city")) {
                if (getCity().isEmpty()) {
                    System.out.println("Your city is empty.");
                } else {
                    System.out.println("Your city:");
                    for (DistrictCard card : getCity()) {
                        System.out.println("- " + card.getName() + " [" + card.getColor() + card.getCost() + "]");
                    }
                }
                continue;
            }

            // Handle action commands
            if (input.equals("t") || input.equals("end")) {
                // Collect character-specific income
                if (name.equals("King")) {
                    int yellowCount = (int) getCity().stream().filter(c -> c.getColor().equals("yellow")).count();
                    addGold(yellowCount);
                } else if (name.equals("Bishop")) {
                    int blueCount = (int) getCity().stream().filter(c -> c.getColor().equals("blue")).count();
                    addGold(blueCount);
                } else if (name.equals("Merchant")) {
                    int greenCount = (int) getCity().stream().filter(c -> c.getColor().equals("green")).count();
                    addGold(greenCount + 1);  // +1 for being Merchant
                } else if (name.equals("Warlord")) {
                    int redCount = (int) getCity().stream().filter(c -> c.getColor().equals("red")).count();
                    addGold(redCount);
                }
                break;
            }

            if (input.equals("cards")) {
                if (getHand().size() >= 7) {
                    System.out.println("Your hand is full.");
                    continue;
                }
                drawCard(Game.districtDeck.draw());
                System.out.println("Drew a card.");
                continue;
            }

            if (input.startsWith("build ")) {
                if (getCity().size() >= 8) {
                    System.out.println("Your city is full.");
                    continue;
                }
                try {
                    int index = Integer.parseInt(input.substring(6).trim()) - 1;
                    if (index >= 0 && index < getHand().size()) {
                        DistrictCard card = getHand().get(index);
                        if (getGold() >= card.getCost() && !hasDistrict(card.getName())) {
                            buildDistrict(index);
                            System.out.println("Built " + card.getName());
                        } else {
                            System.out.println("Cannot build that district.");
                        }
                    }
                } catch (NumberFormatException ignored) {
                    System.out.println("Invalid build command.");
                }
                continue;
            }

            // Handle Museum special ability
            if (getCity().stream().anyMatch(c -> c.getName().equals("Museum")) && !getHand().isEmpty()) {
                try {
                    int index = Integer.parseInt(input) - 1;
                    if (index >= 0 && index < getHand().size()) {
                        DistrictCard card = getHand().remove(index);
                        bankCard(card);
                        System.out.println("Banked " + card.getName() + " in Museum.");
                        continue;
                    }
                } catch (NumberFormatException ignored) {}
            }

            System.out.println("Invalid command.");
        }
    }
}
