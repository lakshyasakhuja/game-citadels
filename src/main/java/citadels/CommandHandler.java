package citadels;

import citadels.card.DistrictCard;
import citadels.player.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Handles command processing and execution for player turns in the Citadels game.
 * This class processes player commands during their turn, including:
 * <ul>
 *   <li>Building districts</li>
 *   <li>Viewing game state (hand, gold, cities)</li>
 *   <li>Using character abilities (e.g., Magician's actions)</li>
 *   <li>Managing turn flow</li>
 * </ul>
 *
 * @author Lakshya Sakhuja
 * @version 7.0
 */
public class CommandHandler {

    /**
     * Runs the command processing loop for a player's turn.
     * Handles various commands including:
     * <ul>
     *   <li>'build X' - Build district card number X</li>
     *   <li>'hand' - Show cards in hand</li>
     *   <li>'gold' - Show current gold</li>
     *   <li>'city [X]' - Show built districts (optionally for player X)</li>
     *   <li>'all' - Show game state for all players</li>
     *   <li>'action' - Special character actions (e.g., Magician abilities)</li>
     *   <li>'t' or 'end' - End turn</li>
     * </ul>
     *
     * @param player the player whose turn it is
     * @param scanner the scanner for reading user input
     */
    public static void run(Player player, Scanner scanner) {
        boolean endTurn = false;

        // figure out how many builds this character gets
        String role = Game.selectedCharacters.get(player).getName();
        int maxBuilds = role.equalsIgnoreCase("Architect") ? 3 : 1;
        int buildsDone = 0;

        // 1) Show hand up front
        System.out.println("Your hand (you have " + player.getGold() + " gold):");
        System.out.println("✓ = Affordable | ✗ = Too expensive | ⚠ = Already built");
        System.out.println("----------------------------------------");
        List<DistrictCard> hand = player.getHand();
        if (hand.isEmpty()) {
            System.out.println("(empty)");
        } else {
            for (int i = 0; i < hand.size(); i++) {
                DistrictCard d = hand.get(i);
                String status;
                if (player.hasDistrict(d.getName())) {
                    status = "⚠";
                } else if (d.getCost() <= player.getGold()) {
                    status = "✓";
                } else {
                    status = "✗";
                }
                
                System.out.printf("%d. %s %s (%s), cost: %d gold%n",
                    i + 1,
                    status,
                    d.getName(),
                    d.getColor(),
                    d.getCost());
            }
        }

        // 2) Build prompt
        System.out.printf(
        "Now you may build up to %d district%s this turn.  Type 'build <card number>' or 't' to skip/build less.%n",
        maxBuilds,
        maxBuilds==1 ? "" : "s");

        while (!endTurn && buildsDone < maxBuilds) {
            System.out.print("> ");
            String raw = scanner.nextLine().trim();
            String input = raw.matches("\\d+") ? "build " + raw : raw.toLowerCase();

            // Process commands based on input
            if (handleEndTurn(input)) {
                System.out.println("You ended your turn.");
                endTurn = true;
                break;
            }

            if (handleHandCommand(input, player, hand)) continue;
            if (handleGoldCommand(input, player)) continue;
            if (handleCityCommand(input, player)) continue;
            if (handleBuildCommand(input, player, hand, maxBuilds, buildsDone)) {
                buildsDone++;
                if (buildsDone >= maxBuilds) {
                    endTurn = true;
                }
                continue;
            }
            if (handleAllCommand(input, player)) continue;
            if (handleMagicianAction(input, player)) continue;
            if (handleInfoCommand(input)) continue;
            if (handleSaveCommand(input, player)) continue;
            if (handleLoadCommand(input)) continue;
            if (handleDebugCommand(input)) continue;
            if (handleHelpCommand(input)) continue;

            // Unknown command
            System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    /**
     * Handles the end turn command.
     *
     * @param input the command input
     * @return true if this was an end turn command, false otherwise
     */
    private static boolean handleEndTurn(String input) {
        return input.equals("t") || input.equals("end");
    }

    /**
     * Handles the hand command, showing the player's current hand.
     *
     * @param input the command input
     * @param player the current player
     * @param hand the player's hand
     * @return true if the command was handled, false otherwise
     */
    private static boolean handleHandCommand(String input, Player player, List<DistrictCard> hand) {
        if (!input.equals("hand")) return false;
        
        System.out.println("Your hand (you have " + player.getGold() + " gold):");
        System.out.println("✓ = Affordable | ✗ = Too expensive | ⚠ = Already built");
        System.out.println("----------------------------------------");
        for (int i = 0; i < hand.size(); i++) {
            DistrictCard d = hand.get(i);
            String status;
            if (player.hasDistrict(d.getName())) {
                status = "⚠";
            } else if (d.getCost() <= player.getGold()) {
                status = "✓";
            } else {
                status = "✗";
            }
            
            System.out.printf("%d. %s %s (%s), cost: %d gold%n",
                i + 1,
                status,
                d.getName(),
                d.getColor(),
                d.getCost());
        }
        return true;
    }

    /**
     * Handles the gold command, showing the player's current gold.
     *
     * @param input the command input
     * @param player the current player
     * @return true if the command was handled, false otherwise
     */
    private static boolean handleGoldCommand(String input, Player player) {
        if (!input.equals("gold")) return false;
        
        System.out.println("You have " + player.getGold() + " gold.");
        return true;
    }

    /**
     * Handles the city command, showing built districts for a player.
     *
     * @param input the command input
     * @param player the current player
     * @return true if the command was handled, false otherwise
     */
    private static boolean handleCityCommand(String input, Player player) {
        if (!input.startsWith("city") && !input.startsWith("citadel") && !input.startsWith("list")) 
            return false;

        String[] parts = input.split("\\s+");
        Player target = player;
        if (parts.length == 2) {
            try {
                int idx = Integer.parseInt(parts[1]) - 1;
                target = Game.players.get(idx);
            } catch (Exception e) {
                System.out.println("Invalid player number.");
                return true;
            }
        }
        
        List<DistrictCard> city = target.getCity();
        if (city.isEmpty()) {
            System.out.println(target.getName() + " has built no districts.");
        } else {
            System.out.println("Player " + target.getName() + " has built:");
            for (DistrictCard d : city) {
                System.out.println("- " + d.getName()
                    + " (" + d.getColor() + "), points: " + d.getCost());
            }
        }
        return true;
    }

    /**
     * Handles the build command, attempting to build a district from hand.
     * Gives players up to 3 chances when they try to build a district they can't afford.
     *
     * @param input the command input
     * @param player the current player
     * @param hand the player's hand
     * @param maxBuilds maximum number of builds allowed this turn
     * @param buildsDone number of builds already done this turn
     * @return true if the command was handled, false otherwise
     */
    private static boolean handleBuildCommand(String input, Player player, List<DistrictCard> hand, 
                                            int maxBuilds, int buildsDone) {
        if (!input.startsWith("build ")) return false;

        String[] parts = input.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Usage: build <card number>");
            return false;
        }

        try {
            int cardNum = Integer.parseInt(parts[1]);
            if (cardNum < 1 || cardNum > hand.size()) {
                System.out.println("Invalid card number. Must be between 1 and " + hand.size());
                return false;
            }

            int attempts = 0;
            int maxAttempts = 3;
            boolean buildSuccess = false;

            while (attempts < maxAttempts && !buildSuccess) {
                buildSuccess = player.buildDistrict(cardNum - 1);
                
                if (!buildSuccess) {
                    attempts++;
                    if (attempts < maxAttempts) {
                        // Only show remaining attempts message if it was a gold-related failure
                        DistrictCard card = hand.get(cardNum - 1);
                        if (player.getGold() < card.getCost()) {
                            // Show all districts with affordability indicators
                            System.out.println("\nYour hand (you have " + player.getGold() + " gold):");
                            System.out.println("✓ = Affordable | ✗ = Too expensive | ⚠ = Already built");
                            System.out.println("----------------------------------------");
                            
                            for (int i = 0; i < hand.size(); i++) {
                                DistrictCard d = hand.get(i);
                                String status;
                                if (player.hasDistrict(d.getName())) {
                                    status = "⚠";
                                } else if (d.getCost() <= player.getGold()) {
                                    status = "✓";
                                } else {
                                    status = "✗";
                                }
                                
                                System.out.printf("%d. %s %s (%s), cost: %d gold%n",
                                    i + 1,
                                    status,
                                    d.getName(),
                                    d.getColor(),
                                    d.getCost());
                            }
                            
                            System.out.println("\nYou have " + (maxAttempts - attempts) + 
                                " more attempt" + (maxAttempts - attempts == 1 ? "" : "s") + 
                                " to build a district. Choose a different card or type 't' to end your turn.");
                            System.out.print("> ");
                            String newInput = Game.getScanner().nextLine().trim();
                            
                            if (newInput.equals("t") || newInput.equals("end")) {
                                return false;
                            }
                            
                            try {
                                cardNum = Integer.parseInt(newInput);
                                if (cardNum < 1 || cardNum > hand.size()) {
                                    System.out.println("Invalid card number. Must be between 1 and " + hand.size());
                                    continue;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Please enter a valid card number or 't' to end your turn.");
                                continue;
                            }
                        } else {
                            // If failure wasn't due to gold, break the loop
                            return false;
                        }
                    } else {
                        System.out.println("You've used all your attempts to build a district this turn.");
                        return false;
                    }
                }
            }

            if (buildSuccess && buildsDone < maxBuilds - 1) {
                System.out.printf(
                    "Built successfully! You have %d build%s remaining this turn.%n",
                    maxBuilds - buildsDone - 1,
                    (maxBuilds - buildsDone - 1)==1 ? "" : "s"
                );
            }

            return buildSuccess;

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid card number.");
            return false;
        }
    }

    /**
     * Handles the all command, showing game state for all players.
     *
     * @param input the command input
     * @param player the current player
     * @return true if the command was handled, false otherwise
     */
    private static boolean handleAllCommand(String input, Player player) {
        if (!input.equals("all")) return false;

        // Show current player's state
        System.out.printf("Player 1 (you): cards=%d gold=%d city=",
            player.getHand().size(), player.getGold());
        for (DistrictCard d : player.getCity()) {
            System.out.print(d.getName() + " [" + d.getColor() + d.getCost() + "] ");
        }
        System.out.println("\n");

        // Show other players' states
        for (int i = 2; i <= Game.players.size(); i++) {
            Player p = Game.players.get(i - 1);
            System.out.printf("Player %d: cards=%d gold=%d city=",
                i, p.getHand().size(), p.getGold());
            for (DistrictCard d : p.getCity()) {
                System.out.print(d.getName() + " [" + d.getColor() + d.getCost() + "] ");
            }
            System.out.println("\n");
        }
        return true;
    }

    /**
     * Handles Magician character actions (swap hands or redraw cards).
     *
     * @param input the command input
     * @param player the current player
     * @return true if the command was handled, false otherwise
     */
    private static boolean handleMagicianAction(String input, Player player) {
        if (!input.startsWith("action")) return false;

        String[] parts = input.split("\\s+");
        if (parts.length < 2) {
            System.out.println("Usage: action swap <n> OR action redraw <i1,i2,...>");
            return true;
        }

        if (!Game.selectedCharacters.get(player).getName().equals("Magician")) {
            System.out.println("You are not the Magician. You cannot use 'action'.");
            return true;
        }

        if (parts[1].equals("swap") && parts.length == 3) {
            handleMagicianSwap(parts[2], player);
        } else if (parts[1].equals("redraw") && parts.length == 3) {
            handleMagicianRedraw(parts[2], player);
        } else {
            System.out.println("Usage: action swap <n> OR action redraw <i1,i2,...>");
        }
        return true;
    }

    /**
     * Handles the Magician's hand swap ability.
     *
     * @param targetStr the target player number
     * @param player the current player
     */
    private static void handleMagicianSwap(String targetStr, Player player) {
        try {
            int targetIdx = Integer.parseInt(targetStr) - 1;
            List<Player> plist = Game.players;
            if (targetIdx < 0 || targetIdx >= plist.size() || plist.get(targetIdx) == player) {
                System.out.println("Invalid player number.");
            } else {
                Player tp = plist.get(targetIdx);
                List<DistrictCard> tmp = new ArrayList<>(player.getHand());
                player.getHand().clear();
                player.getHand().addAll(tp.getHand());
                tp.getHand().clear();
                tp.getHand().addAll(tmp);
                System.out.println("Swapped hands with " + tp.getName());
            }
        } catch (Exception e) {
            System.out.println("Invalid swap target.");
        }
    }

    /**
     * Handles the Magician's card redraw ability.
     *
     * @param indexStr comma-separated list of card indices to redraw
     * @param player the current player
     */
    private static void handleMagicianRedraw(String indexStr, Player player) {
        try {
            String[] indices = indexStr.split(",");
            List<DistrictCard> handList = player.getHand();
            List<DistrictCard> toRedraw = new ArrayList<>();
            Set<Integer> toRemove = new TreeSet<>(Collections.reverseOrder());
            
            for (String s : indices) {
                int i = Integer.parseInt(s.trim()) - 1;
                if (i >= 0 && i < handList.size()) {
                    toRedraw.add(handList.get(i));
                    toRemove.add(i);
                }
            }
            
            for (int i : toRemove) handList.remove(i);
            for (DistrictCard d : toRedraw) {
                Game.districtDeck.placeOnBottom(d);
            }
            
            for (int i = 0; i < toRedraw.size(); i++) {
                DistrictCard drawn = Game.districtDeck.draw();
                if (drawn != null) {
                    handList.add(drawn);
                }
            }
            
            System.out.println("Redrew " + toRedraw.size() + " cards.");
        } catch (Exception e) {
            System.out.println("Invalid redraw indices.");
        }
    }

    /**
     * Handles the info command, showing information about characters and buildings.
     *
     * @param input the command input
     * @return true if the command was handled, false otherwise
     */
    private static boolean handleInfoCommand(String input) {
        if (!input.startsWith("info")) return false;

        String[] parts = input.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Usage: info <character or building name>");
            return true;
        }

        switch (parts[1].toLowerCase()) {
            case "assassin":
                System.out.println("Assassin: Choose a character to assassinate. That player skips their turn.");
                break;
            case "thief":
                System.out.println("Thief: Choose a character to rob. You steal their gold.");
                break;
            case "magician":
                System.out.println("Magician: Swap hands or discard your hand and draw the same number.");
                break;
            case "king":
                System.out.println("King: Gains income from yellow districts and takes the crown.");
                break;
            case "bishop":
                System.out.println("Bishop: Gains income from blue districts. Warlord cannot destroy your city.");
                break;
            case "merchant":
                System.out.println("Merchant: Gains one extra gold and income from green districts.");
                break;
            case "architect":
                System.out.println("Architect: Draw 2 extra cards and may build up to 3 districts.");
                break;
            case "warlord":
                System.out.println("Warlord: May destroy one district (pay cost - 1). Gains income from red districts.");
                break;
            case "keep":
                System.out.println("Keep: This district cannot be destroyed by the Warlord.");
                break;
            case "laboratory":
                System.out.println("Laboratory: Once per turn, discard a card to gain 1 gold.");
                break;
            case "school":
                System.out.println("School of Magic: Counts as any color for income purposes.");
                break;
            default:
                System.out.println("No info available for '" + parts[1] + "'");
        }
        return true;
    }

    /**
     * Handles save commands for both single player and full game states.
     *
     * @param input the command input
     * @param player the current player
     * @return true if the command was handled, false otherwise
     */
    private static boolean handleSaveCommand(String input, Player player) {
        if (!input.startsWith("save") && !input.startsWith("savegame")) return false;

        boolean fullGame = input.startsWith("savegame");
        String[] parts = input.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Usage: " + (fullGame ? "savegame" : "save") + " <filename>.json");
            return true;
        }

        try (FileWriter writer = new FileWriter(parts[1])) {
            JSONObject state;
            if (fullGame) {
                state = GameState.saveGame();
                System.out.println("Full game saved to " + parts[1]);
            } else {
                state = GameState.savePlayers(Collections.singletonList(player));
                System.out.println("Game saved to " + parts[1]);
            }
            writer.write(state.toJSONString());
        } catch (Exception e) {
            System.out.println("Failed to save game: " + e.getMessage());
        }
        return true;
    }

    /**
     * Handles load commands for both single player and full game states.
     *
     * @param input the command input
     * @return true if the command was handled, false otherwise
     */
    private static boolean handleLoadCommand(String input) {
        if (!input.startsWith("load") && !input.startsWith("loadgame")) return false;

        boolean fullGame = input.startsWith("loadgame");
        String[] parts = input.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Usage: " + (fullGame ? "loadgame" : "load") + " <filename>.json");
            return true;
        }

        try (FileReader reader = new FileReader(parts[1])) {
            JSONObject state = (JSONObject) new JSONParser().parse(reader);
            if (fullGame) {
                GameState.loadGame(state);
                System.out.println("Game loaded from " + parts[1]);
            } else {
                List<Player> loaded = GameState.loadPlayers(state);
                System.out.println("Loaded " + loaded.size() + " player(s).");
            }
        } catch (Exception e) {
            System.out.println("Failed to load game: " + e.getMessage());
        }
        return true;
    }

    /**
     * Handles the debug command to toggle debug mode.
     *
     * @param input the command input
     * @return true if the command was handled, false otherwise
     */
    private static boolean handleDebugCommand(String input) {
        if (!input.equals("debug")) return false;

        Game.debugMode = !Game.debugMode;
        System.out.println("Debug mode is now " + (Game.debugMode ? "ON" : "OFF"));
        return true;
    }

    /**
     * Handles the help command, showing available commands.
     *
     * @param input the command input
     * @return true if the command was handled, false otherwise
     */
    private static boolean handleHelpCommand(String input) {
        if (!input.equals("help")) return false;

        System.out.println("Available commands:");
        System.out.println("'t' or 'end'      — end your turn");
        System.out.println("hand             — show your hand");
        System.out.println("gold             — show your gold");
        System.out.println("city [n]         — show built districts (optional player n)");
        System.out.println("build <n>        — build a district from your hand");
        System.out.println("all              — show status of all players");
        System.out.println("action swap/redraw (Magician only)");
        System.out.println("info <n>      — get info on a character or building");
        System.out.println("save <file>      — save your player");
        System.out.println("load <file>      — load your player");
        System.out.println("savegame <file>  — save full game");
        System.out.println("loadgame <file>  — load full game");
        System.out.println("debug, help");
        return true;
    }
}
