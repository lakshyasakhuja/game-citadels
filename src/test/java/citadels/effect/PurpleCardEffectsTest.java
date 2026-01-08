package citadels.effect;

import citadels.card.DistrictCard;
import citadels.player.HumanPlayer;
import citadels.player.Player;
import citadels.player.AIPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for PurpleCardEffects class.
 * Tests all special abilities and effects of purple district cards,
 * including Laboratory, Keep, Great Wall, and other special districts.
 */
public class PurpleCardEffectsTest {

    private Player player;

    /**
     * Sets up test environment before each test.
     * Creates a test player and resets turn usage flags.
     */
    @BeforeEach
    public void setUp() {
        player = new HumanPlayer("Test Player");
        PurpleCardEffects.resetTurnUsage();
    }

    /**
     * Tests that the constructor can be instantiated.
     * Verifies no exceptions are thrown during construction.
     */
    @Test
    public void testConstructor() {
        assertDoesNotThrow(() -> new PurpleCardEffects());
    }

    /**
     * Tests the Keep's protection ability.
     * Verifies that districts are properly protected from destruction
     * when the Keep is present in a player's city.
     */
    @Test
    public void testIsProtected() {
        Player target = new HumanPlayer("Target");
        target.getCity().add(new DistrictCard("Keep", "purple", 3, 1, null));
        assertTrue(PurpleCardEffects.isProtected(target, "Keep"));
        assertFalse(PurpleCardEffects.isProtected(target, "Library"));

        // Test case-sensitive Keep protection
        target.getCity().add(new DistrictCard("keep", "purple", 3, 1, null));
        assertTrue(PurpleCardEffects.isProtected(target, "keep"));
    }

    /**
     * Tests district destruction cost calculations.
     * Verifies proper cost adjustments with Great Wall
     * and other modifying effects.
     */
    @Test
    public void testDestroyCost() {
        DistrictCard victim = new DistrictCard("Library", "blue", 5, 1, null);
        Player target = new HumanPlayer("Target");
        assertEquals(4, PurpleCardEffects.destroyCost(target, victim));

        // Test with Great Wall
        target.getCity().add(new DistrictCard("Great Wall", "purple", 6, 1, null));
        assertEquals(5, PurpleCardEffects.destroyCost(target, victim));

        // Test with multiple districts including Great Wall
        target.getCity().add(new DistrictCard("Keep", "purple", 3, 1, null));
        assertEquals(5, PurpleCardEffects.destroyCost(target, victim));
    }

    /**
     * Tests School of Magic color changing ability.
     * Verifies that School of Magic properly changes color
     * based on the current character.
     */
    @Test
    public void testEffectiveColor() {
        DistrictCard card = new DistrictCard("School of Magic", "purple", 6, 1, "Counts as any color");
        assertEquals("yellow", PurpleCardEffects.effectiveColor(card, player, "King"));
        assertEquals("blue", PurpleCardEffects.effectiveColor(card, player, "Bishop"));
        assertEquals("green", PurpleCardEffects.effectiveColor(card, player, "Merchant"));
        assertEquals("red", PurpleCardEffects.effectiveColor(card, player, "Warlord"));
        assertEquals(card.getColor(), PurpleCardEffects.effectiveColor(card, player, "Unknown")); // Test unknown character
        
        // Test regular card
        DistrictCard regular = new DistrictCard("Library", "blue", 5, 1, null);
        assertEquals("blue", PurpleCardEffects.effectiveColor(regular, player, "King"));
    }

    /**
     * Tests bonus score calculations from purple cards.
     * Verifies proper scoring for University, Dragon Gate,
     * Museum, and other scoring effects.
     */
    @Test
    public void testBonusScore() {
        // Test University bonus
        player.getCity().add(new DistrictCard("University", "purple", 6, 1, null));
        assertEquals(2, PurpleCardEffects.bonusScore(player));

        // Test Dragon Gate bonus
        player.getCity().add(new DistrictCard("Dragon Gate", "purple", 6, 1, null));
        assertEquals(4, PurpleCardEffects.bonusScore(player));

        // Test Museum bonus
        player.getCity().add(new DistrictCard("Museum", "purple", 4, 1, null));
        player.getBankedCards().add(new DistrictCard("Card1", "blue", 1, 1, null));
        player.getBankedCards().add(new DistrictCard("Card2", "green", 1, 1, null));
        assertEquals(6, PurpleCardEffects.bonusScore(player));

        // Test no color bonus case
        player.getCity().clear();
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        player.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        assertEquals(0, PurpleCardEffects.bonusScore(player));

        // Test Haunted City without 4 colors
        player.getCity().clear();
        player.getCity().add(new DistrictCard("Haunted City", "purple", 2, 1, null));
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        player.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        assertEquals(0, PurpleCardEffects.bonusScore(player));
    }

    /**
     * Tests bonus score calculations based on district colors.
     * Verifies proper scoring for having districts of different colors
     * and interactions with Haunted City.
     */
    @Test
    public void testBonusScoreColors() {
        // Test 5 colors bonus
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        player.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        player.getCity().add(new DistrictCard("Manor", "yellow", 3, 1, null));
        player.getCity().add(new DistrictCard("Watchtower", "red", 1, 1, null));
        player.getCity().add(new DistrictCard("University", "purple", 6, 1, null));
        assertEquals(5, PurpleCardEffects.bonusScore(player)); // 3 for colors + 2 for University

        // Test 4 colors + Haunted City
        player.getCity().clear();
        player.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        player.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        player.getCity().add(new DistrictCard("Manor", "yellow", 3, 1, null));
        player.getCity().add(new DistrictCard("Watchtower", "red", 1, 1, null));
        player.getCity().add(new DistrictCard("Haunted City", "purple", 2, 1, null));
        assertEquals(3, PurpleCardEffects.bonusScore(player)); // 3 for colors + Haunted
    }

    /**
     * Tests Laboratory card usage mechanics.
     * Verifies proper gold gain and card discarding,
     * as well as once-per-turn usage restriction.
     */
    @Test
    public void testLaboratoryUsage() {
        // Setup player with Laboratory and a card to discard
        player.getCity().add(new DistrictCard("Laboratory", "purple", 5, 1, null));
        player.getHand().add(new DistrictCard("Temple", "blue", 2, 1, null));
        int initialGold = player.getGold();
        int initialHandSize = player.getHand().size();

        // Test using Laboratory
        String input = "yes\n1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PurpleCardEffects.applyTurnEffects(player, scanner);

        // Verify Laboratory effect
        assertEquals(initialGold + 1, player.getGold());
        assertEquals(initialHandSize - 1, player.getHand().size());

        // Test Laboratory can't be used twice in same turn
        input = "yes\n1\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PurpleCardEffects.applyTurnEffects(player, scanner);

        // Verify no change on second use
        assertEquals(initialGold + 1, player.getGold());
        assertEquals(initialHandSize - 1, player.getHand().size());

        // Test Laboratory can be used again after reset
        PurpleCardEffects.resetTurnUsage();
        player.getHand().add(new DistrictCard("Market", "green", 2, 1, null));
        input = "yes\n1\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PurpleCardEffects.applyTurnEffects(player, scanner);

        // Verify Laboratory works after reset
        assertEquals(initialGold + 2, player.getGold());
        assertEquals(initialHandSize - 1, player.getHand().size());
    }

    /**
     * Tests edge cases for Laboratory card usage.
     * Verifies proper handling of invalid inputs,
     * empty hands, and AI player interactions.
     */
    @Test
    public void testLaboratoryEdgeCases() {
        // Test with no Laboratory in city
        String input = "yes\n1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PurpleCardEffects.applyTurnEffects(player, scanner);
        assertEquals(0, player.getGold());

        // Test with Laboratory but empty hand
        player.getCity().add(new DistrictCard("Laboratory", "purple", 5, 1, null));
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PurpleCardEffects.applyTurnEffects(player, scanner);
        assertEquals(0, player.getGold());

        // Test with Laboratory and invalid choice
        player.getHand().add(new DistrictCard("Temple", "blue", 2, 1, null));
        input = "yes\n99\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PurpleCardEffects.applyTurnEffects(player, scanner);
        assertEquals(0, player.getGold());
        assertEquals(1, player.getHand().size());

        // Test with Laboratory and negative choice
        input = "yes\n-1\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PurpleCardEffects.applyTurnEffects(player, scanner);
        assertEquals(0, player.getGold());
        assertEquals(1, player.getHand().size());

        // Test with Laboratory and "no" response
        input = "no\n";
        scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PurpleCardEffects.applyTurnEffects(player, scanner);
        assertEquals(0, player.getGold());
        assertEquals(1, player.getHand().size());

        // Test with AI player
        Player aiPlayer = new AIPlayer("AI Test");
        aiPlayer.getCity().add(new DistrictCard("Laboratory", "purple", 5, 1, null));
        aiPlayer.getHand().add(new DistrictCard("Temple", "blue", 2, 1, null));
        PurpleCardEffects.applyTurnEffects(aiPlayer, scanner);
        assertEquals(0, aiPlayer.getGold()); // AI should not use Laboratory
    }

    /**
     * Tests Warlord interaction with protection effects.
     * Verifies Keep protection and Great Wall cost modification
     * against Warlord's destruction ability.
     */
    @Test
    public void testWarlordProtectionAndCost() {
        // Test Keep protection
        DistrictCard keep = new DistrictCard("Keep", "purple", 3, 1, null);
        assertTrue(PurpleCardEffects.isProtectedFromWarlord(keep, player));

        // Test non-Keep card
        DistrictCard victim = new DistrictCard("Library", "blue", 5, 1, null);
        assertFalse(PurpleCardEffects.isProtectedFromWarlord(victim, player));

        // Test base destruction cost
        assertEquals(4, PurpleCardEffects.getWarlordDestructionCost(victim, player));

        // Test with Great Wall
        player.getCity().add(new DistrictCard("Great Wall", "purple", 6, 1, null));
        assertEquals(5, PurpleCardEffects.getWarlordDestructionCost(victim, player));
    }

    /**
     * Tests Laboratory state management.
     * Verifies proper tracking of Laboratory usage
     * within a turn and reset between turns.
     */
    @Test
    public void testLaboratoryStateManagement() {
        // Test initial state
        assertTrue(PurpleCardEffects.canUseLaboratory().getAsBoolean());

        // Test after marking as used
        PurpleCardEffects.markLaboratoryUsed();
        assertFalse(PurpleCardEffects.canUseLaboratory().getAsBoolean());

        // Test after reset
        PurpleCardEffects.resetTurnUsage();
        assertTrue(PurpleCardEffects.canUseLaboratory().getAsBoolean());
    }

    /**
     * Tests Keep protection mechanics in detail.
     * Verifies case-insensitive protection and
     * proper handling of similarly named cards.
     */
    @Test
    public void testKeepProtection() {
        Player target = new HumanPlayer("Target");
        
        // Test Keep protection with case sensitivity
        target.getCity().add(new DistrictCard("Keep", "purple", 3, 1, null));
        assertTrue(PurpleCardEffects.isProtected(target, "Keep"));
        assertTrue(PurpleCardEffects.isProtected(target, "keep"));
        assertFalse(PurpleCardEffects.isProtected(target, "Library"));

        // Test Keep protection with different case in city
        target.getCity().clear();
        target.getCity().add(new DistrictCard("keep", "purple", 3, 1, null));
        assertTrue(PurpleCardEffects.isProtected(target, "Keep"));
        assertTrue(PurpleCardEffects.isProtected(target, "keep"));

        // Test Keep protection with non-Keep card having same name
        target.getCity().clear();
        target.getCity().add(new DistrictCard("Keep", "blue", 3, 1, null));
        assertFalse(PurpleCardEffects.isProtected(target, "Keep"));

        // Test Keep protection with non-matching name
        target.getCity().clear();
        target.getCity().add(new DistrictCard("Keep", "purple", 3, 1, null));
        assertFalse(PurpleCardEffects.isProtected(target, "Temple"));

        // Test Keep protection with empty city
        target.getCity().clear();
        assertFalse(PurpleCardEffects.isProtected(target, "Keep"));
    }

    /**
     * Tests Great Wall effect on destruction costs.
     * Verifies proper cost increases for districts
     * when Great Wall is present.
     */
    @Test
    public void testGreatWallEffect() {
        Player target = new HumanPlayer("Target");
        DistrictCard victim = new DistrictCard("Library", "blue", 5, 1, null);
        
        // Test without Great Wall
        assertEquals(4, PurpleCardEffects.destroyCost(target, victim));
        
        // Test with Great Wall
        target.getCity().add(new DistrictCard("Great Wall", "purple", 6, 1, null));
        assertEquals(5, PurpleCardEffects.destroyCost(target, victim));
        
        // Test with multiple districts including Great Wall
        target.getCity().add(new DistrictCard("Keep", "purple", 3, 1, null));
        assertEquals(5, PurpleCardEffects.destroyCost(target, victim));
        
        // Test with Great Wall in different case
        target.getCity().clear();
        target.getCity().add(new DistrictCard("great wall", "purple", 6, 1, null));
        assertEquals(5, PurpleCardEffects.destroyCost(target, victim));

        // Test with non-Great Wall cards
        target.getCity().clear();
        target.getCity().add(new DistrictCard("Temple", "blue", 2, 1, null));
        target.getCity().add(new DistrictCard("Market", "green", 2, 1, null));
        assertEquals(4, PurpleCardEffects.destroyCost(target, victim));
    }
}
