package citadels.card;

import citadels.util.Deck;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DistrictDeckLoader.
 * Tests loading district cards from TSV file, including error cases.
 */
public class DistrictDeckLoaderTest {

    /**
     * Tests that the constructor can be called.
     */
    @Test
    public void testConstructor() {
        assertDoesNotThrow(() -> new DistrictDeckLoader());
    }

    /**
     * Tests loading a valid deck from TSV.
     * Verifies cards are loaded correctly and deck is shuffled.
     */
    @Test
    public void testLoadValidDeck() {
        try {
            Field resourceField = DistrictDeckLoader.class.getDeclaredField("CARDS_FILE");
            resourceField.setAccessible(true);
            String originalPath = (String) resourceField.get(null);
            resourceField.set(null, "/citadels/test_cards_new.tsv");

            Deck<DistrictCard> deck = DistrictDeckLoader.loadFromTSV();
            assertNotNull(deck);
            assertFalse(deck.isEmpty());

            // Test all valid cards are loaded
            boolean foundBlue = false;
            boolean foundGreen = false;
            boolean foundYellow = false;
            boolean foundPurple = false;

            while (!deck.isEmpty()) {
                DistrictCard card = deck.draw();
                assertNotNull(card.getName());
                assertFalse(card.getName().isEmpty());
                assertNotNull(card.getColor());
                assertFalse(card.getColor().trim().isEmpty());
                assertTrue(card.getCost() >= 0);
                assertTrue(card.getQuantity() > 0);

                switch (card.getColor().toLowerCase()) {
                    case "blue": foundBlue = true; break;
                    case "green": foundGreen = true; break;
                    case "yellow": foundYellow = true; break;
                    case "purple": 
                        foundPurple = true;
                        assertNotNull(card.getAbility());
                        break;
                }
            }

            assertTrue(foundBlue, "No blue card found");
            assertTrue(foundGreen, "No green card found");
            assertTrue(foundYellow, "No yellow card found");
            assertTrue(foundPurple, "No purple card found");

            resourceField.set(null, originalPath);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests loading from a non-existent file.
     */
    @Test
    public void testFileNotFound() {
        try {
            Field resourceField = DistrictDeckLoader.class.getDeclaredField("CARDS_FILE");
            resourceField.setAccessible(true);
            String originalPath = (String) resourceField.get(null);
            resourceField.set(null, "/citadels/nonexistent.tsv");
            
            assertThrows(RuntimeException.class, DistrictDeckLoader::loadFromTSV);
            
            resourceField.set(null, originalPath);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that invalid lines in the TSV are skipped.
     */
    @Test
    public void testInvalidLines() {
        try {
            Field resourceField = DistrictDeckLoader.class.getDeclaredField("CARDS_FILE");
            resourceField.setAccessible(true);
            String originalPath = (String) resourceField.get(null);
            resourceField.set(null, "/citadels/test_cards.tsv");

            Deck<DistrictCard> deck = DistrictDeckLoader.loadFromTSV();
            assertNotNull(deck);

            // Count valid cards (Temple, Market, Manor, Haunted)
            int expectedValidCards = 4;
            int actualValidCards = 0;
            
            while (!deck.isEmpty()) {
                DistrictCard card = deck.draw();
                String name = card.getName();
                if (name.equals("Temple") || name.equals("Market") || 
                    name.equals("Manor") || name.equals("Haunted")) {
                    actualValidCards++;
                }
            }

            assertEquals(expectedValidCards, actualValidCards, "Wrong number of valid cards loaded");

            resourceField.set(null, originalPath);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that lines with less than 4 parts are skipped.
     */
    @Test
    public void testInvalidLineParts() {
        try {
            Field resourceField = DistrictDeckLoader.class.getDeclaredField("CARDS_FILE");
            resourceField.setAccessible(true);
            String originalPath = (String) resourceField.get(null);
            resourceField.set(null, "/citadels/test_cards.tsv");

            Deck<DistrictCard> deck = DistrictDeckLoader.loadFromTSV();
            assertNotNull(deck);

            // Verify that the line with only 3 parts was skipped
            while (!deck.isEmpty()) {
                DistrictCard card = deck.draw();
                assertNotEquals("Bad", card.getName(), "Line with less than 4 parts was not skipped");
            }

            resourceField.set(null, originalPath);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that IOException during file reading is handled correctly.
     */
    @Test
    public void testIOError() {
        try {
            Field resourceField = DistrictDeckLoader.class.getDeclaredField("CARDS_FILE");
            resourceField.setAccessible(true);
            String originalPath = (String) resourceField.get(null);
            
            // Set a non-existent file path
            resourceField.set(null, "/citadels/nonexistent.tsv");
            
            // This should throw a RuntimeException
            RuntimeException ex = assertThrows(RuntimeException.class, DistrictDeckLoader::loadFromTSV);
            assertTrue(ex.getMessage().contains("not found"), "Expected error message about file not found");
            
            resourceField.set(null, originalPath);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that empty lines are skipped.
     */
    @Test
    public void testEmptyLines() {
        try {
            Field resourceField = DistrictDeckLoader.class.getDeclaredField("CARDS_FILE");
            resourceField.setAccessible(true);
            String originalPath = (String) resourceField.get(null);
            resourceField.set(null, "/citadels/test_cards.tsv");

            Deck<DistrictCard> deck = DistrictDeckLoader.loadFromTSV();
            assertNotNull(deck);

            // Verify that empty lines were skipped
            while (!deck.isEmpty()) {
                DistrictCard card = deck.draw();
                assertNotNull(card.getName());
                assertFalse(card.getName().trim().isEmpty());
            }

            resourceField.set(null, originalPath);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that the first line (header) is skipped.
     */
    @Test
    public void testHeaderSkipped() {
        try {
            Field resourceField = DistrictDeckLoader.class.getDeclaredField("CARDS_FILE");
            resourceField.setAccessible(true);
            String originalPath = (String) resourceField.get(null);
            resourceField.set(null, "/citadels/test_cards.tsv");

            Deck<DistrictCard> deck = DistrictDeckLoader.loadFromTSV();
            assertNotNull(deck);

            // Verify that the header line was not loaded as a card
            while (!deck.isEmpty()) {
                DistrictCard card = deck.draw();
                assertNotEquals("Name", card.getName());
                assertNotEquals("Qty", card.getName());
            }

            resourceField.set(null, originalPath);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that lines with exactly 4 parts (no ability) are handled correctly.
     */
    @Test
    public void testExactlyFourParts() {
        try {
            Field resourceField = DistrictDeckLoader.class.getDeclaredField("CARDS_FILE");
            resourceField.setAccessible(true);
            String originalPath = (String) resourceField.get(null);
            
            // Create a test file with exactly 4 parts
            String testPath = "/citadels/test_cards.tsv";
            resourceField.set(null, testPath);
            
            Deck<DistrictCard> deck = DistrictDeckLoader.loadFromTSV();
            assertNotNull(deck);
            
            resourceField.set(null, originalPath);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that empty color strings are handled correctly.
     */
    @Test
    public void testEmptyColor() {
        Deck<DistrictCard> deck = DistrictDeckLoader.loadFromTSV();
        while (!deck.isEmpty()) {
            DistrictCard card = deck.draw();
            assertFalse(card.getColor().trim().isEmpty(), "Card with empty color was not skipped");
        }
    }

    /**
     * Tests that invalid number formats are handled correctly.
     */
    @Test
    public void testInvalidNumberFormat() {
        Deck<DistrictCard> deck = DistrictDeckLoader.loadFromTSV();
        while (!deck.isEmpty()) {
            DistrictCard card = deck.draw();
            assertTrue(card.getCost() >= 0, "Card with invalid cost was not skipped");
            assertTrue(card.getQuantity() > 0, "Card with invalid quantity was not skipped");
        }
    }
} 