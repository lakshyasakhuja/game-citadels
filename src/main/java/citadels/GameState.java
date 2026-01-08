package citadels;

import citadels.card.CharacterCard;
import citadels.card.DistrictCard;
import citadels.player.AIPlayer;
import citadels.player.HumanPlayer;
import citadels.player.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;

/**
 * Manages the game state for the Citadels game, including saving and loading functionality.
 * This class handles:
 * <ul>
 *   <li>Current player and character tracking</li>
 *   <li>Game state serialization to JSON</li>
 *   <li>Game state deserialization from JSON</li>
 *   <li>Player state management</li>
 * </ul>
 * The class is designed as a utility class with only static methods.
 *
 * @author Lakshya Sakhuja
 * @version 7.0
 */
public class GameState {

    /** The player whose turn it currently is */
    private static Player currentPlayer;
    /** The character card being played in the current turn */
    private static CharacterCard currentCharacter;

    /**
     * Private constructor to prevent instantiation of this utility class.
     * 
     * @throws UnsupportedOperationException always, as this class should not be instantiated
     */
    private GameState() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Sets the current active player.
     *
     * @param player the player whose turn it is
     */
    public static void setCurrentPlayer(Player player) {
        currentPlayer = player;
    }

    /**
     * Gets the current active player.
     *
     * @return the player whose turn it is
     */
    public static Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Sets the character card being played in the current turn.
     *
     * @param character the character card being played
     */
    public static void setCurrentCharacter(CharacterCard character) {
        currentCharacter = character;
    }

    /**
     * Gets the character card being played in the current turn.
     *
     * @return the current character card
     */
    public static CharacterCard getCurrentCharacter() {
        return currentCharacter;
    }

    /**
     * Saves the complete game state to a JSON object.
     * The saved state includes:
     * <ul>
     *   <li>Crown holder position</li>
     *   <li>Character assignments</li>
     *   <li>Player states (gold, hand, city)</li>
     *   <li>District deck contents</li>
     * </ul>
     *
     * @return a JSONObject containing the complete game state
     */
    @SuppressWarnings("unchecked")
    public static JSONObject saveGame() {
        JSONObject root = new JSONObject();

        root.put("crown", Game.getCrownPlayerIndex());

        JSONObject characterMap = new JSONObject();
        for (Map.Entry<Player, CharacterCard> entry : Game.selectedCharacters.entrySet()) {
            characterMap.put(entry.getKey().getName(), entry.getValue().getName());
        }
        root.put("characters", characterMap);

        JSONArray playersArray = new JSONArray();
        for (Player p : Game.players) {
            JSONObject obj = new JSONObject();
            obj.put("name", p.getName());
            obj.put("gold", p.getGold());

            JSONArray hand = new JSONArray();
            for (DistrictCard d : p.getHand()) {
                JSONObject dObj = new JSONObject();
                dObj.put("name", d.getName());
                dObj.put("color", d.getColor());
                dObj.put("cost", d.getCost());
                hand.add(dObj);
            }
            obj.put("hand", hand);

            JSONArray city = new JSONArray();
            for (DistrictCard d : p.getCity()) {
                JSONObject dObj = new JSONObject();
                dObj.put("name", d.getName());
                dObj.put("color", d.getColor());
                dObj.put("cost", d.getCost());
                city.add(dObj);
            }
            obj.put("city", city);

            playersArray.add(obj);
        }
        root.put("players", playersArray);

        JSONArray deckArray = new JSONArray();
        for (DistrictCard d : Game.districtDeck) {
            JSONObject dObj = new JSONObject();
            dObj.put("name", d.getName());
            dObj.put("color", d.getColor());
            dObj.put("cost", d.getCost());
            deckArray.add(dObj);
        }
        root.put("deck", deckArray);

        return root;
    }

    /**
     * Loads a complete game state from a JSON object.
     * This method:
     * <ul>
     *   <li>Clears the current game state</li>
     *   <li>Restores players and their states</li>
     *   <li>Restores character assignments</li>
     *   <li>Rebuilds the district deck</li>
     *   <li>Restores the crown holder</li>
     * </ul>
     *
     * @param root the JSONObject containing the game state to load
     */
    public static void loadGame(JSONObject root) {
        if (root == null) {
            return;
        }

        Game.players.clear();
        Game.selectedCharacters.clear();
        Game.districtDeck.clear();

        if (root.containsKey("crown")) {
            Object crownObj = root.get("crown");
            int crownIndex;
            if (crownObj instanceof Number) {
                crownIndex = ((Number) crownObj).intValue();
                Game.setCrownPlayerIndex(crownIndex);
            }
        }

        if (root.containsKey("players")) {
            JSONArray playersArray = (JSONArray) root.get("players");
            for (Object o : playersArray) {
                if (!(o instanceof JSONObject)) continue;
                
                JSONObject obj = (JSONObject) o;
                String name = (String) obj.get("name");
                if (name == null) continue;
                
                Player p = name.equals("Player 1") ? new HumanPlayer(name) : new AIPlayer(name);
                
                Object goldObj = obj.get("gold");
                if (goldObj instanceof Number) {
                    p.addGold(((Number) goldObj).intValue());
                }

                if (obj.containsKey("hand")) {
                    JSONArray hand = (JSONArray) obj.get("hand");
                    for (Object dObj : hand) {
                        if (!(dObj instanceof JSONObject)) continue;
                        JSONObject d = (JSONObject) dObj;
                        String cardName = (String) d.get("name");
                        String color = (String) d.get("color");
                        Object costObj = d.get("cost");
                        if (cardName != null && color != null && costObj instanceof Number) {
                            p.drawCard(new DistrictCard(
                                cardName,
                                color,
                                ((Number) costObj).intValue(),
                                1,
                                null
                            ));
                        }
                    }
                }

                if (obj.containsKey("city")) {
                    JSONArray city = (JSONArray) obj.get("city");
                    for (Object dObj : city) {
                        if (!(dObj instanceof JSONObject)) continue;
                        JSONObject d = (JSONObject) dObj;
                        String cardName = (String) d.get("name");
                        String color = (String) d.get("color");
                        Object costObj = d.get("cost");
                        if (cardName != null && color != null && costObj instanceof Number) {
                            p.getCity().add(new DistrictCard(
                                cardName,
                                color,
                                ((Number) costObj).intValue(),
                                1,
                                null
                            ));
                        }
                    }
                }

                Game.players.add(p);
            }
        }

        if (root.containsKey("characters")) {
            JSONObject characterMap = (JSONObject) root.get("characters");
            for (Object k : characterMap.keySet()) {
                String pname = (String) k;
                String cname = (String) characterMap.get(k);
                if (pname == null || cname == null) continue;

                Player p = Game.players.stream()
                        .filter(pl -> pl.getName().equals(pname))
                        .findFirst().orElse(null);

                if (p != null) {
                    Game.selectedCharacters.put(p, new CharacterCard(cname, 0));
                }
            }
        }

        if (root.containsKey("deck")) {
            JSONArray deckArray = (JSONArray) root.get("deck");
            for (Object dObj : deckArray) {
                if (!(dObj instanceof JSONObject)) continue;
                JSONObject d = (JSONObject) dObj;
                String cardName = (String) d.get("name");
                String color = (String) d.get("color");
                Object costObj = d.get("cost");
                if (cardName != null && color != null && costObj instanceof Number) {
                    Game.districtDeck.addCard(new DistrictCard(
                        cardName,
                        color,
                        ((Number) costObj).intValue(),
                        1,
                        null
                    ));
                }
            }
            Game.districtDeck.shuffle();
        }
    }

    /**
     * Saves the state of specific players to a JSON object.
     * This is primarily used for single-player game modes.
     * The saved state includes each player's:
     * <ul>
     *   <li>Name</li>
     *   <li>Gold amount</li>
     *   <li>Hand of cards</li>
     *   <li>Built city districts</li>
     * </ul>
     *
     * @param players the list of players whose state should be saved
     * @return a JSONObject containing the players' states
     */
    @SuppressWarnings("unchecked")
    public static JSONObject savePlayers(List<Player> players) {
        JSONArray arr = new JSONArray();
        for (Player p : players) {
            JSONObject obj = new JSONObject();
            obj.put("name", p.getName());
            obj.put("gold", p.getGold());

            JSONArray hand = new JSONArray();
            for (DistrictCard d : p.getHand()) {
                JSONObject dObj = new JSONObject();
                dObj.put("name", d.getName());
                dObj.put("color", d.getColor());
                dObj.put("cost", d.getCost());
                hand.add(dObj);
            }
            obj.put("hand", hand);

            JSONArray city = new JSONArray();
            for (DistrictCard d : p.getCity()) {
                JSONObject dObj = new JSONObject();
                dObj.put("name", d.getName());
                dObj.put("color", d.getColor());
                dObj.put("cost", d.getCost());
                city.add(dObj);
            }
            obj.put("city", city);

            arr.add(obj);
        }

        JSONObject root = new JSONObject();
        root.put("players", arr);
        return root;
    }

    /**
     * Loads player states from a JSON object.
     * This method creates new player instances based on the saved state,
     * restoring their:
     * <ul>
     *   <li>Gold amount</li>
     *   <li>Hand of cards</li>
     *   <li>Built city districts</li>
     * </ul>
     *
     * @param root the JSONObject containing the player states to load
     * @return a list of restored Player objects
     */
    public static List<Player> loadPlayers(JSONObject root) {
        List<Player> result = new ArrayList<>();
        if (root == null || !root.containsKey("players")) {
            return result;
        }

        JSONArray arr = (JSONArray) root.get("players");
        for (Object o : arr) {
            if (!(o instanceof JSONObject)) continue;
            
            JSONObject obj = (JSONObject) o;
            String name = (String) obj.get("name");
            if (name == null) continue;
            
            Player p = name.equals("Player 1") ? new HumanPlayer(name) : new AIPlayer(name);
            
            Object goldObj = obj.get("gold");
            if (goldObj instanceof Number) {
                p.addGold(((Number) goldObj).intValue());
            }

            if (obj.containsKey("hand")) {
                JSONArray hand = (JSONArray) obj.get("hand");
                for (Object dObj : hand) {
                    if (!(dObj instanceof JSONObject)) continue;
                    JSONObject d = (JSONObject) dObj;
                    String cardName = (String) d.get("name");
                    String color = (String) d.get("color");
                    Object costObj = d.get("cost");
                    if (cardName != null && color != null && costObj instanceof Number) {
                        p.drawCard(new DistrictCard(
                            cardName,
                            color,
                            ((Number) costObj).intValue(),
                            1,
                            null
                        ));
                    }
                }
            }

            if (obj.containsKey("city")) {
                JSONArray city = (JSONArray) obj.get("city");
                for (Object dObj : city) {
                    if (!(dObj instanceof JSONObject)) continue;
                    JSONObject d = (JSONObject) dObj;
                    String cardName = (String) d.get("name");
                    String color = (String) d.get("color");
                    Object costObj = d.get("cost");
                    if (cardName != null && color != null && costObj instanceof Number) {
                        p.getCity().add(new DistrictCard(
                            cardName,
                            color,
                            ((Number) costObj).intValue(),
                            1,
                            null
                        ));
                    }
                }
            }

            result.add(p);
        }

        return result;
    }
}
