package citadels.card;

import citadels.util.Deck;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility class for loading the district cards deck from a TSV (Tab-Separated Values) file.
 * The TSV file should have the following columns:
 * <ol>
 *   <li>Name - The name of the district</li>
 *   <li>Qty - The quantity of this card in the deck</li>
 *   <li>color - The color/type of the district</li>
 *   <li>cost - The cost in gold to build the district</li>
 *   <li>text (optional) - The special ability text for purple districts</li>
 * </ol>
 *
 * @author Lakshya Sakhuja
 * @version 7.0
 */
public class DistrictDeckLoader {

    /** Path to the cards TSV file */
    private static String CARDS_FILE = "/citadels/cards.tsv";

    /**
     * Loads the district cards from the cards.tsv resource file and creates a shuffled deck.
     * The file is expected to be in the resources/citadels directory.
     * The first line of the file is assumed to be a header and is skipped.
     *
     * @return a shuffled Deck containing all district cards
     * @throws RuntimeException if the file cannot be found or read
     */
    public static Deck<DistrictCard> loadFromTSV() {
        Deck<DistrictCard> deck = new Deck<>();

        InputStream stream = DistrictDeckLoader.class.getResourceAsStream(CARDS_FILE);
        if (stream == null) {
            throw new RuntimeException(CARDS_FILE + " not found");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            boolean isFirstLine = true;

            while (true) {
                try {
                    line = reader.readLine();
                    if (line == null) break;
                } catch (IOException e) {
                    break;  // Stop reading on error
                }

                if (line.trim().isEmpty()) continue;
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] parts = line.split("\t");
                if (parts.length < 4) {
                    System.err.println("Skipping invalid line: " + line);
                    continue;
                }

                try {
                    String name = parts[0].trim();
                    int quantity = Integer.parseInt(parts[1].trim());
                    String color = parts[2].trim();
                    int cost = Integer.parseInt(parts[3].trim());
                    String ability = parts.length > 4 ? parts[4].trim() : null;
                    
                    // Skip empty names or invalid quantities
                    if (name.isEmpty() || quantity < 1) {
                        System.err.println("Skipping line with empty name or invalid quantity: " + line);
                        continue;
                    }

                    for (int i = 0; i < quantity; i++) {
                        deck.addCard(new DistrictCard(name, color, cost, 1, ability));
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line with invalid numbers: " + line);
                }
            }
        } catch (IOException e) {
            // Ignore close errors
        }

        deck.shuffle();
        return deck;
    }
}
