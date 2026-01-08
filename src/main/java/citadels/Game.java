package citadels;

import citadels.card.CharacterCard;
import citadels.card.DistrictCard;
import citadels.card.DistrictDeckLoader;
import citadels.effect.PurpleCardEffects;
import citadels.player.AIPlayer;
import citadels.player.HumanPlayer;
import citadels.player.Player;
import citadels.util.Deck;
import org.json.simple.JSONObject;
import java.util.*;

/**
 * Core game engine class for the Citadels card game.
 * This class manages the game state, player turns, and game flow.
 * It implements the main game loop and handles all game phases including
 * character selection, player turns, and end-game conditions.
 *
 * @author Lakshya Sakhuja
 * @version 7.0
 */
public class Game {
    /**
     * Represents the different phases of the game.
     */
    public enum GamePhase {
        /** Character selection phase */
        SELECTION,
        /** Player turn phase */
        TURN,
        /** End of round phase */
        ROUND_END
    }

    /** Scanner for reading user input */
    protected static Scanner scanner = new Scanner(System.in);
    /** Maps players to their selected character cards */
    public static final Map<Player, CharacterCard> selectedCharacters = new HashMap<>();
    /** List of all players in the game */
    public static final List<Player> players = new ArrayList<>();
    /** Deck of district cards */
    public static final Deck<DistrictCard> districtDeck = DistrictDeckLoader.loadFromTSV();
    /** Debug mode flag */
    public static boolean debugMode = false;
    /** Current phase of the game */
    private static GamePhase currentPhase = GamePhase.SELECTION;
    /** Flag indicating if the game is over */
    private static boolean gameOver = false;
    /** Reference to the winning player */
    private static Player winner = null;
    /** Flag indicating if a round is in progress */
    private static boolean roundInProgress = false;

    /** Index of the player with the crown */
    private static int crownPlayerIndex;
    /** Pool of available character cards */
    private static final List<CharacterCard> characterPool = createDefaultCharacters();
    /** List of visible discarded character cards */
    public static final List<CharacterCard> visibleDiscard = new ArrayList<>();
    /** The face-down discarded character card */
    private static CharacterCard mysteryDiscard;
    /** Name of the assassinated character */
    private static String assassinatedCharacter = null;
    /** Name of the robbed character */
    private static String robbedCharacter = null;

    /**
     * Sets whether a round is currently in progress.
     * @param inProgress true if a round is in progress, false otherwise
     */
    public static void setRoundInProgress(boolean inProgress) {
        roundInProgress = inProgress;
    }

    /**
     * Checks if a round is currently in progress.
     * @return true if a round is in progress, false otherwise
     */
    public static boolean isRoundInProgress() { 
        return roundInProgress; 
    }

    /**
     * Gets the current phase of the game.
     * @return the current GamePhase
     */
    public static GamePhase getCurrentPhase() { return currentPhase; }

    /**
     * Sets the current phase of the game.
     * @param phase the new GamePhase to set
     */
    public static void setCurrentPhase(GamePhase phase) { currentPhase = phase; }

    /**
     * Gets the name of the assassinated character.
     * @return the name of the assassinated character, or null if no character was assassinated
     */
    public static String getAssassinatedCharacter() { return assassinatedCharacter; }

    /**
     * Sets the game over state.
     * @param over true to end the game, false to continue
     */
    public static void setGameOver(boolean over) { gameOver = over; }

    /**
     * Sets the winner of the game.
     * @param p the Player who won the game
     */
    public static void setWinner(Player p) { winner = p; }

    /**
     * Checks if the game is over.
     * @return true if the game is over, false otherwise
     */
    public static boolean isGameOver() { return gameOver; }

    /**
     * Gets the winner of the game.
     * @return the Player who won the game, or null if the game isn't over
     */
    public static Player getWinner() { return winner; }

    /**
     * Main game loop that runs the Citadels game.
     * This method:
     * <ul>
     *   <li>Initializes the game with the specified number of players</li>
     *   <li>Deals initial cards to all players</li>
     *   <li>Runs the main game loop until the game ends</li>
     *   <li>Handles player input between rounds</li>
     * </ul>
     */
    public void run() {
        System.out.println("Enter how many players [4-7]:");
        int count = layerCount();

        System.out.println("Shuffling deck...");
        System.out.println("Adding characters...");
        System.out.println("Dealing cards...");

        createPlayers(count);
        dealInitialCards();

        System.out.println("Starting Citadels with " + count + " players...");
        System.out.println("You are player 1");

        while (true) {
            playRound();
            System.out.println("Round complete. Type 't' to continue or 'exit' to quit.");
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim().toLowerCase();

                if (input.equals("gold")) {
                    showAllPlayerGold();
                    continue;
                }

                if (input.equals("exit")) return;
                if (input.equals("t")) break;

                System.out.println("Unknown command. Type 't', 'exit', or 'gold'.");
            }
        }

    }

    /**
     * Executes a single round of the game.
     * A round consists of:
     * <ul>
     *   <li>Character selection phase</li>
     *   <li>Turn phase where each character acts in order</li>
     *   <li>End of round cleanup and victory condition check</li>
     * </ul>
     */
    public static void playRound() {

        if (getCurrentPhase() != GamePhase.ROUND_END && getCurrentPhase() != GamePhase.SELECTION) {
        System.out.println("Cannot start selection phase now. Current phase: " + getCurrentPhase());
        return;
        }

        if (roundInProgress) {
            System.out.println("Round is already in progress.");
            return;
        }
        setCurrentPhase(GamePhase.SELECTION);
        roundInProgress = true;

        // Announce crowned player and wait for 't'
        Player crowned = players.get(crownPlayerIndex);
        System.out.println(crowned.getName() + " is the crowned player and goes first.");
        System.out.println("Press t to process turns");
        while (true) {
            System.out.print("> ");
            String cmd = scanner.nextLine().trim().toLowerCase();

            if (cmd.equals("gold")) {
                for (Player p : players) {
                    System.out.println(p.getName() + " has " + p.getGold() + " gold.");
                }
                continue;
            }

            if (cmd.equals("t")) break;

            System.out.println("It is not your turn. Press t to continue with other player turns.");
        }

        // SELECTION PHASE
        System.out.println("================================");
        System.out.println("SELECTION PHASE");
        System.out.println("================================");

        // Clear special effect state
        assassinatedCharacter = null;
        robbedCharacter = null;

        startCharacterSelectionPhase();

        System.out.println("Character choosing is over, action round will now begin.");

        // Now the Turn Phase keypress guard
        setCurrentPhase(GamePhase.TURN);
        System.out.println("================================");
        System.out.println("TURN PHASE");
        System.out.println("================================");

        // Process each rank in order
        for (int rank = 1; rank <= 8; rank++) {
            // 1) Find canonical card
            CharacterCard canon = null;
            for (CharacterCard c : characterPool) {
                if (c.getRank() == rank) {
                    canon = c;
                    break;
                }
            }

            // 2) Find who picked it
            CharacterCard picked = null;
            Player picker = null;
            for (Map.Entry<Player,CharacterCard> e : selectedCharacters.entrySet()) {
                if (e.getValue().getRank() == rank) {
                    picked = e.getValue();
                    picker = e.getKey();
                    break;
                }
            }


            // 3) Announce with special King message
            System.out.println(rank + ": " + canon.getName());
            if (picked == null) {
                System.out.println("No one is the " + canon.getName());
                continue;
            }
            if (rank == 4) {
                System.out.println(picker.getName() + " is the King");
            } else {
                System.out.println(picker.getName() + " is the " + picked.getName());
            }

            // 4) Assassin skip
            if (picked.getName().equalsIgnoreCase(assassinatedCharacter)) {
                System.out.println(picker.getName() + " was assassinated and loses their turn.");
                continue;
            }

            // 5) Execute turn
            GameState.setCurrentPlayer(picker);
            GameState.setCurrentCharacter(picked);
            if (picker instanceof HumanPlayer) {
                handlePlayerTurn(picker, picked);
            } else {
                picker.takeTurn();
            }
        }

        // End of round cleanup
        roundInProgress = false;
        setCurrentPhase(GamePhase.ROUND_END);
        // After all 8 ranks, check for game end
        for (Player p : players) {
            if (p.getCity().size() >= 7) {
                System.out.println(p.getName() + " has built 7 or more districts. The game ends!");
                endGame();
                return;
            }
        }

    }

    /**
     * Ends the current game and calculates final scores.
     * This method is called when a player has built 7 or more districts
     * or when the game needs to end prematurely.
     */
    public static void endGame() {
        boolean testMode = System.getProperty("test.env") != null;
        if (testMode) {
            System.out.println("[TEST MODE] Skipping System.exit");
        }

        System.out.println("\n--- Final Scores ---");
        Map<Player, Integer> scores = new HashMap<>();
        Map<Player, Integer> baseScores = new HashMap<>();
        Player firstToFinish = null;

        for (Player player : players) {
            int baseScore = player.getCity().stream().mapToInt(DistrictCard::getCost).sum();
            int bonus = 0;

            Set<String> colors = new HashSet<>();
            for (DistrictCard card : player.getCity()) {
                colors.add(PurpleCardEffects.effectiveColor(card, player, ""));
            }

            if (colors.containsAll(Arrays.asList("red", "blue", "green", "yellow", "purple"))) {
                bonus += 3;
                System.out.println(player.getName() + " has all district colors (+3 bonus)");
            }

            if (player.getCity().size() >= 8) {
                if (firstToFinish == null) {
                    firstToFinish = player;
                    bonus += 4;
                    System.out.println(player.getName() + " finished city first (+4 bonus)");
                } else {
                    bonus += 2;
                    System.out.println(player.getName() + " also completed city (+2 bonus)");
                }
            }

            bonus += PurpleCardEffects.bonusScore(player);
            int total = baseScore + bonus;
            scores.put(player, total);
            baseScores.put(player, baseScore);
            System.out.println(player.getName() + ": " + total + " points (base=" + baseScore + ", bonus=" + bonus + ")");
        }

        int maxScore = Collections.max(scores.values());
        List<Player> tied = new ArrayList<>();
        for (Map.Entry<Player, Integer> e : scores.entrySet()) {
            if (e.getValue() == maxScore) tied.add(e.getKey());
        }

        Player winner = null;

        if (tied.size() == 1) {
            winner = tied.get(0);
        } else {
            int highestRank = -1;
            Player best = null;
            for (Player p : tied) {
                CharacterCard c = selectedCharacters.get(p);
                if (c != null && c.getRank() > highestRank) {
                    highestRank = c.getRank();
                    best = p;
                }
            }
            if (best != null) {
                winner = best;
            } else {
                System.out.println("\nIt's a tie between:");
                for (Player p : tied) {
                    System.out.println("- " + p.getName());
                }
                System.out.println("Mystery discarded character was: " + (mysteryDiscard != null ? mysteryDiscard.getName() : "Unknown"));
                System.out.println("\nThanks for playing Citadels!");
                if (!testMode) System.exit(0);
                return;
            }
        }

        System.out.println("\nWinner: " + winner.getName());
        System.out.println("Mystery discarded character was: " + (mysteryDiscard != null ? mysteryDiscard.getName() : "Unknown"));
        System.out.println("\nCongratulations, " + winner.getName() + " wins the game!");
        System.out.println("Thanks for playing Citadels!");
        if (!testMode) System.exit(0);
    }

    /**
     * Gets the number of players from user input.
     * Validates that the input is between 4 and 7 players.
     *
     * @return the number of players
     */
    int layerCount() {
        while (true) {
            try {
                System.out.print("> ");
                int count = Integer.parseInt(scanner.nextLine().trim());
                if (count >= 4 && count <= 7) return count;
                System.out.println("Must be between 4 and 7 players.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    /**
     * Creates the specified number of players.
     * Player 1 is always human, others are AI.
     *
     * @param count number of players to create
     */
    void createPlayers(int count) {
        players.clear();
        players.add(new HumanPlayer("Player 1"));
        for (int i = 2; i <= count; i++) {
            players.add(new AIPlayer("Player " + i));
        }
    }

    /**
     * Deals initial cards and gold to all players.
     */
    void dealInitialCards() {
        for (Player p : players) {
            // Deal 4 cards to each player
            for (int i = 0; i < 4; i++) {
                p.drawCard(districtDeck.draw());
            }
            // Give 2 gold to each player
            p.addGold(2);
        }
    }

    /**
     * Initiates and manages the character selection phase.
     * During this phase:
     * <ul>
     *   <li>Characters are shuffled</li>
     *   <li>Some are discarded face-up and face-down</li>
     *   <li>Players choose characters in order</li>
     * </ul>
     */
    public static void startCharacterSelectionPhase() {
        // 1) Shuffle and clear out last round's selections
        List<CharacterCard> shuffled = new ArrayList<>(characterPool);
        Collections.shuffle(shuffled);
        visibleDiscard.clear();
        selectedCharacters.clear();

        // 2) Mystery (face-down) discard
        mysteryDiscard = shuffled.remove(0);
        System.out.println("A mystery character was removed.");

        // 3) Face-up discards based on player count
        int numFaceUp;
        switch (players.size()) {
            case 4: numFaceUp = 2; break;
            case 5: numFaceUp = 1; break;
            default: numFaceUp = 0; break;
        }

        for (int i = 0; i < numFaceUp; i++) {
            CharacterCard c = shuffled.remove(0);
            if (c.getName().equalsIgnoreCase("King")) {
                System.out.println("King was removed.");
                System.out.println("The King cannot be visibly removed, trying again..");
                System.out.println("A mystery character was removed.");
                shuffled.add(c);
                Collections.shuffle(shuffled);
                i--;
                continue;
            }
            visibleDiscard.add(c);
            System.out.println(c.getName() + " was removed.");
        }

        // 4) Character drafting phase
        List<CharacterCard> draft = new ArrayList<>(shuffled);
        int total = players.size();
        for (int turn = 0; turn < total; turn++) {
            Player p = players.get((crownPlayerIndex + turn) % total);
            System.out.println(p.getName() + " is choosing a character.");

            CharacterCard chosen;
            if (p instanceof HumanPlayer) {
                System.out.println("Choose your character. Available characters:");
                for (int j = 0; j < draft.size(); j++) {
                    System.out.printf("%d. %s%n", j + 1, draft.get(j).getName());
                }
                chosen = null;
                while (chosen == null) {
                    System.out.print("> ");
                    String in = scanner.nextLine().trim();

                    if (in.equalsIgnoreCase("gold")) {
                        showAllPlayerGold();
                        continue;
                    }

                    try {
                        int num = Integer.parseInt(in);
                        if (num >= 1 && num <= draft.size()) {
                            chosen = draft.get(num - 1);
                            break;
                        }
                    } catch (NumberFormatException ignored) {}

                    for (CharacterCard cc : draft) {
                        if (cc.getName().equalsIgnoreCase(in)) {
                            chosen = cc;
                            break;
                        }
                    }

                    if (chosen == null) {
                        System.out.println("Invalid choice. Enter a number 1–" + draft.size() + " or the character's name.");
                    }
                }
            } else {
                chosen = draft.remove(0);
                System.out.println(p.getName() + " chose a character.");
            }

            draft.remove(chosen);
            selectedCharacters.put(p, chosen);
        }
    }

    /**
     * Handles the character selection process for each player.
     * Players take turns selecting characters from the available pool.
     *
     * @param shuffled the shuffled list of character cards to choose from
     */
    public static void playerCharacterSelection(List<CharacterCard> shuffled) {
        int total = players.size();
        int idx = crownPlayerIndex;
        for (int i = 0; i < total; i++) {
            Player p = players.get(idx % total);
            System.out.println(p.getName() + " is choosing a character.");
            CharacterCard chosen;
            if (p instanceof HumanPlayer) {
                System.out.println("Choose your character. Available characters:");
                for (int j = 0; j < shuffled.size(); j++) {
                    System.out.println((j + 1) + ". " + shuffled.get(j).getName());
                }
                int sel = -1;
                while (sel < 1 || sel > shuffled.size()) {
                    System.out.print("> ");
                    try { sel = Integer.parseInt(scanner.nextLine().trim()); }
                    catch (Exception ignored) {}
                }
                chosen = shuffled.remove(sel - 1);
                System.out.println("You chose: " + chosen.getName());
            } else {
                chosen = shuffled.remove(0);
                System.out.println(p.getName() + " chose a character.");
            }
            selectedCharacters.put(p, chosen);
            idx++;
        }
    }

    private static boolean isSelection = false;
    public static void setSelectionPhase(boolean value) { isSelection = value; }
    public static boolean getSelectionPhase() { return isSelection; }

    public static boolean isSelectionPhase() {
        selectedCharacters.clear();
        List<CharacterCard> deck = new ArrayList<>(characterPool);
        Collections.shuffle(deck);

        mysteryDiscard = deck.remove(0);
        System.out.println("A mystery character was removed.");

        int faceUp = (players.size() == 4) ? 2
                     : (players.size() == 5) ? 1 : 0;
        visibleDiscard.clear();
        for (int i = 0; i < faceUp; i++) {
            CharacterCard c = deck.remove(0);
            if (c.getName().equalsIgnoreCase("King")) {
                deck.add(c);
                Collections.shuffle(deck);
                i--;
            } else {
                visibleDiscard.add(c);
                System.out.println(c.getName() + " was removed.");
            }
        }

        List<CharacterCard> draft = new ArrayList<>(deck);
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get((crownPlayerIndex + i) % players.size());
            System.out.println(p.getName() + " is choosing a character.");
            CharacterCard chosen;
            if (p instanceof HumanPlayer) {
                System.out.println("Choose your character. Available characters:");
                for (CharacterCard cc : draft) {
                    System.out.println("- " + cc.getName());
                }
                Optional<CharacterCard> opt;
                do {
                    System.out.print("> ");
                    opt = draft.stream()
                               .filter(c -> c.getName().equalsIgnoreCase(scanner.nextLine().trim()))
                               .findFirst();
                } while (!opt.isPresent());
                chosen = opt.get();
            } else {
                chosen = draft.remove(new Random().nextInt(draft.size()));
                System.out.println(p.getName() + " chose a character.");
            }
            draft.remove(chosen);
            selectedCharacters.put(p, chosen);
        }
        return true;
    }

    public static void processTurnPhase() {
        setCurrentPhase(GamePhase.TURN);
        roundInProgress = true;

        for (int rank = 1; rank <= 8; rank++) {
            // 1) Find the canonical character for this rank
            CharacterCard canon = null;
            for (CharacterCard c : characterPool) {
                if (c.getRank() == rank) {
                    canon = c;
                    break;
                }
            }

            // 2) Find who picked it (if anyone)
            CharacterCard picked = null;
            Player picker = null;
            for (Map.Entry<Player, CharacterCard> e : selectedCharacters.entrySet()) {
                if (e.getValue().getRank() == rank) {
                    picked = e.getValue();
                    picker = e.getKey();
                    break;
                }
            }

            // 3) Announce
            System.out.println(rank + ": " + (canon != null ? canon.getName() : "Unknown"));
            if (picked == null) {
                System.out.println("No one is the " + (canon != null ? canon.getName() : "Unknown"));
            } else {
                System.out.println(picker.getName() + " is the " + picked.getName());
            }

            // 4) *** Pause here and wait for 't' ***
            while (true) {
                System.out.print("> ");
                String cmd = scanner.nextLine().trim().toLowerCase();
                if (cmd.equals("t")) {
                    break;
                }
                System.out.println("It is not your turn. Press t to continue with other player turns.");
            }

            // 5) If nobody picked it OR they were assassinated, skip execution
            if (picked == null
                || picked.getName().equalsIgnoreCase(assassinatedCharacter)) {
                if (picked != null) {
                    System.out.println(picker.getName() + " was assassinated and loses their turn.");
                }
                continue;
            }

            // 6) Execute the turn
            GameState.setCurrentPlayer(picker);
            GameState.setCurrentCharacter(picked);
            if (picker instanceof HumanPlayer) {
                handlePlayerTurn(picker, picked);
            } else {
                picker.takeTurn();
            }
        }

        roundInProgress = false;
    }

    /**
     * Processes a player's turn during the turn phase.
     * Handles all possible actions a player can take during their turn.
     *
     * @param player the player whose turn it is
     * @param character the character card the player is using this turn
     */
    public static void handlePlayerTurn(Player player, CharacterCard character) {
        String name = character.getName();

        // — ASSASSIN —
        if (name.equals("Assassin")) {
            System.out.println("Your turn.");
            System.out.println("Who do you want to kill? Choose a character from 2–8:");
            while (true) {
                System.out.print("> ");
                String in = scanner.nextLine().trim().toLowerCase();
                if (handleInfoCommands(in, player)) continue;
                if (in.equals("t") || in.equals("end")) {
                    System.out.println("You skipped that step.");
                    return;
                }
                try {
                    int rank = Integer.parseInt(in);
                    for (CharacterCard c : characterPool) {
                        if (c.getRank() == rank) {
                            assassinatedCharacter = c.getName();
                            System.out.println("You have killed the " + assassinatedCharacter);
                            return;
                        }
                    }
                } catch (NumberFormatException ignored) {}
                    System.out.println("Invalid choice. Enter a number 2–8, or 't'/'end' to skip.");
            }
        }

        // — THIEF —
        if (name.equals("Thief")) {
            System.out.println("Your turn.");
            System.out.println("Who do you want to steal from? Choose a character from 2–8:");
            while (true) {
                System.out.print("> ");
                String in = scanner.nextLine().trim().toLowerCase();
                if (handleInfoCommands(in, player)) continue;
                if (in.equals("t") || in.equals("end")) return;
                try {
                    int rank = Integer.parseInt(in);
                    for (CharacterCard c : characterPool) {
                        if (c.getRank() == rank && !c.getName().equalsIgnoreCase("Assassin")) {
                            robbedCharacter = c.getName();
                            System.out.println("You chose to steal from the " + robbedCharacter);
                            // fall through into normal draw/build phase
                            in = "break";
                            break;
                        }
                    }
                    if ("break".equals(in)) break;
                } catch (NumberFormatException ignored) {}
                System.out.println("Invalid choice. Enter a number 2–8, or 't' to skip.");
            }
        }

        // — MAGICIAN —
        if (name.equals("Magician")) {
            System.out.println("Your turn.");
            System.out.println("Do you want to swap hands with another player, redraw your hand, or skip? [swap/redraw/skip]");
            while (true) {
                System.out.print("> ");
                String in = scanner.nextLine().trim().toLowerCase();
                if (handleInfoCommands(in, player)) continue;
                if (in.equals("skip") || in.equals("t") || in.equals("end")) break;
                if (in.equals("swap")) {
                    // list other players
                    List<Player> others = new ArrayList<>(players);
                    others.remove(player);
                    System.out.println("Choose a player to swap with:");
                    for (int i = 0; i < others.size(); i++) {
                        System.out.println((i+1) + ". " + others.get(i).getName());
                    }
                    while (true) {
                        System.out.print("> ");
                        String sel = scanner.nextLine().trim();
                        try {
                            int idx = Integer.parseInt(sel) - 1;
                            Player target = others.get(idx);
                            List<DistrictCard> tmp = new ArrayList<>(player.getHand());
                            player.getHand().clear();
                            player.getHand().addAll(target.getHand());
                            target.getHand().clear();
                            target.getHand().addAll(tmp);
                            System.out.println("Swapped hands with " + target.getName());
                            break;
                        } catch (Exception e) {
                            System.out.println("Invalid selection.");
                        }
                    }
                    break;
                } else if (in.equals("redraw")) {
                    int cnt = player.getHand().size();
                    player.getHand().clear();
                    for (int i = 0; i < cnt; i++) player.drawCard(districtDeck.draw());
                    System.out.println("You redrew " + cnt + " cards.");
                    break;
                } else {
                    System.out.println("Unknown option. Use swap, redraw, or skip.");
                }
            }
        }

        // — SKIP IF ASSASSINATED OR ROBBED —
        if (name.equalsIgnoreCase(assassinatedCharacter)) {
            System.out.println(player.getName() + " was assassinated and skips their turn.");
            return;
        }
        if (name.equalsIgnoreCase(robbedCharacter)) {
            for (Player p : players) {
                if (selectedCharacters.get(p).getName().equalsIgnoreCase("Thief")) {
                    System.out.println(player.getName() + " was robbed by " + p.getName() + "!");
                    p.addGold(player.getGold());
                    player.addGold(-player.getGold());
                    break;
                }
            }
            return;
        }

        // — NORMAL ACTION ROUND —
        System.out.println("Your turn.");

        // 1) Collect or draw
        System.out.println("Collect 2 gold or draw two cards and pick one [gold/cards]:");
        while (true) {
            System.out.print("> ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (choice.equals("t") || choice.equals("end")) return;
            if (choice.equals("gold")) {
                player.addGold(2);
                System.out.println(player.getName() + " received 2 gold.");
                break;
            }
            if (choice.equals("cards")) {
                DistrictCard c1 = districtDeck.draw();
                DistrictCard c2 = districtDeck.draw();
                while (true) {
                    System.out.println("Choose a card by typing '1', '2', 'card 1', or 'card 2', or 't' to skip:");
                    System.out.println("  1) " + c1.getName() + " [" + c1.getColor() + c1.getCost() + "]");
                    System.out.println("  2) " + c2.getName() + " [" + c2.getColor() + c2.getCost() + "]");
                    System.out.print("> ");
                    String pick = scanner.nextLine().trim().toLowerCase();
                    if (pick.equals("t") || pick.equals("end")) return;
                    if (pick.equals("1") || pick.equals("card 1")) { player.drawCard(c1); break; }
                    if (pick.equals("2") || pick.equals("card 2")) { player.drawCard(c2); break; }
                    System.out.println("Invalid choice. Enter '1','2','card 1','card 2', or 't'.");
                }
                break;
            }
            System.out.println("Invalid input. Enter 'gold', 'cards', 't', or 'end'.");
        }

        // 2) Apply purple card effects & calculate role income
        PurpleCardEffects.applyTurnEffects(player, scanner);
        int income = 0;
        switch (name.toLowerCase()) {
            case "king":
                income = (int) player.getCity().stream()
                    .filter(d -> PurpleCardEffects.effectiveColor(d, player, "king").equals("yellow"))
                    .count();
                crownPlayerIndex = players.indexOf(player);
                break;
            case "bishop":
                // Count actual blue districts in your city
                int blueCount = (int) player.getCity().stream()
                    .filter(d -> d.getColor().equalsIgnoreCase("blue"))
                    .count();
                income = blueCount;
                break;
            case "merchant":
                income = (int) player.getCity().stream()
                    .filter(d -> PurpleCardEffects.effectiveColor(d, player, "merchant").equals("green"))
                    .count() + 1;
                break;
            case "architect":
                player.drawCard(districtDeck.draw());
                player.drawCard(districtDeck.draw());
                break;
            case "warlord":
                income = (int) player.getCity().stream()
                    .filter(d -> PurpleCardEffects.effectiveColor(d, player, "warlord").equals("red"))
                    .count();
                break;
        }
        if (income > 0) {
            player.addGold(income);
            System.out.println(player.getName() + " gains " + income + " gold (now has " + player.getGold() + ").");
        }

        // 3) Warlord destruction
        if (name.equalsIgnoreCase("warlord")) {
            for (Player target : players) {
                if (target == player) continue;
                CharacterCard targetCard = selectedCharacters.get(target);
                if (targetCard.getName().equalsIgnoreCase("Bishop") &&
                    !targetCard.getName().equalsIgnoreCase(assassinatedCharacter)) {
                    continue;}

                if (target.getCity().isEmpty()) continue;
                DistrictCard victim = target.getCity().get(0);
                if (!PurpleCardEffects.isProtectedFromWarlord(victim, target)) {
                    int cost = PurpleCardEffects.getWarlordDestructionCost(victim, target);
                    if (player.getGold() >= cost && cost >= 0) {
                        player.addGold(-cost);
                        target.getCity().remove(0);
                        System.out.println(player.getName() + " destroyed "
                            + target.getName() + "'s " + victim.getName());
                    }
                    break;
                }
            }
        }

        if (player.getCity().stream().anyMatch(card -> card.getName().equalsIgnoreCase("Museum"))
            && !player.getHand().isEmpty()) {

            System.out.println("You may bank 1 card at the Museum for +1 point at game end.");
            System.out.println("Your hand:");
            for (int i = 0; i < player.getHand().size(); i++) {
                DistrictCard c = player.getHand().get(i);
                System.out.println((i + 1) + ". " + c.getName() + " [" + c.getColor() + c.getCost() + "]");
            }

            while (true) {
                System.out.print("Enter card number to bank or 't' to skip: ");
                String in = scanner.nextLine().trim().toLowerCase();
                if (in.equals("t") || in.equals("end")) break;
                try {
                    int idx = Integer.parseInt(in) - 1;
                    if (idx >= 0 && idx < player.getHand().size()) {
                        DistrictCard toBank = player.getHand().remove(idx);
                        player.bankCard(toBank);
                        System.out.println("Banked: " + toBank.getName());
                        break;
                    }
                } catch (NumberFormatException ignored) {}
                System.out.println("Invalid input.");
            }
        }

        // 4) Build phase & other commands
        CommandHandler.run(player, scanner);

        System.out.println(player.getName() + "'s turn ends.\n");
    }

    /**
     * Gets the current player taking their turn.
     *
     * @return the current Player object
     */
    public static Player currentPlayer() {
        return players.stream()
            .filter(p -> !selectedCharacters.containsKey(p))
            .findFirst()
            .orElse(null);
    }

    /**
     * Checks if a player has already chosen a character this round.
     *
     * @param player the player to check
     * @return true if the player has chosen a character, false otherwise
     */
    public static boolean hasChosenCharacter(Player player) {
        return selectedCharacters.containsKey(player);
    }

    public static void processTurn() {
        // placeholder
    }

    public int askPlayerCount() {
        while (true) {
            try {
                System.out.print("> ");
                int n = Integer.parseInt(scanner.nextLine().trim());
                if (n >= 4 && n <= 7) return n;
            } catch (Exception ignored) {}
        }
    }

    public static void loadGame(JSONObject root) {
        GameState.loadGame(root);
        if (root.containsKey("mysteryDiscard")) {
            String name = (String) root.get("mysteryDiscard");
            mysteryDiscard = characterPool.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
        }
    }


    public static void setCrownPlayerIndex(int idx) {
        crownPlayerIndex = idx;
    }

    public static int getCrownPlayerIndex() {
        return crownPlayerIndex;
    }

    public static void setAssassinatedCharacter(String name) {
        assassinatedCharacter = name;
    }

    /**
     * Creates the default set of character cards used in the game.
     *
     * @return a List of CharacterCard objects representing the standard character deck
     */
    private static List<CharacterCard> createDefaultCharacters() {
        List<CharacterCard> chars = new ArrayList<>();
        chars.add(new CharacterCard("Assassin", 1));
        chars.add(new CharacterCard("Thief", 2));
        chars.add(new CharacterCard("Magician", 3));
        chars.add(new CharacterCard("King", 4));
        chars.add(new CharacterCard("Bishop", 5));
        chars.add(new CharacterCard("Merchant", 6));
        chars.add(new CharacterCard("Architect", 7));
        chars.add(new CharacterCard("Warlord", 8));
        return chars;
    }

    /**
     * Sets the name of the character that has been robbed.
     *
     * @param name the name of the robbed character
     */
    public static void setRobbedCharacter(String name) {
        robbedCharacter = name;
    }

    /**
     * Gets the name of the character that has been robbed.
     *
     * @return the name of the robbed character, or null if no character was robbed
     */
    public static String getRobbedCharacter() {
        return robbedCharacter;
    }

    /**
     * Displays the current gold count for all players.
     */
    public static void showAllPlayerGold() {
    for (Player p : players) {
        System.out.println(p.getName() + " has " + p.getGold() + " gold.");
    }
    }

    /**
     * Handles information commands during a player's turn.
     *
     * @param input the command input from the player
     * @param player the current player
     * @return true if the command was handled, false otherwise
     */
    public static boolean handleInfoCommands(String input, Player player) {
        switch (input) {
            case "gold":
                showAllPlayerGold();
                return true;
            case "hand":
                List<DistrictCard> hand = player.getHand();
                if (hand.isEmpty()) {
                    System.out.println("Your hand is empty.");
                } else {
                    System.out.println("Your hand:");
                    for (int i = 0; i < hand.size(); i++) {
                        DistrictCard d = hand.get(i);
                        System.out.println((i + 1) + ". " + d.getName() + " (" + d.getColor() + "), cost: " + d.getCost());
                    }
                }
                return true;
            case "city":
            case "citadel":
                List<DistrictCard> city = player.getCity();
                System.out.println("Your city:");
                if (city.isEmpty()) {
                    System.out.println("(no districts built yet)");
                } else {
                    for (DistrictCard d : city) {
                        System.out.println("- " + d.getName() + " (" + d.getColor() + "), cost: " + d.getCost());
                    }
                }
                return true;
            case "all":
                for (Player p : players) {
                    System.out.print(p.getName() + ": gold=" + p.getGold() + ", city=");
                    for (DistrictCard d : p.getCity()) {
                        System.out.print(d.getName() + " [" + d.getColor() + d.getCost() + "] ");
                    }
                    System.out.println();
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * Sets the scanner for input. This is primarily used for testing.
     * @param newScanner the scanner to use for input
     */
    public static void setScanner(Scanner newScanner) {
        scanner = newScanner;
    }

    /**
     * Gets the current scanner being used for input.
     * @return the current scanner
     */
    public static Scanner getScanner() {
        return scanner;
    }

    public static void setCurrentPlayer(Player player) {
        // ... existing code ...
    }

}
