package citadels;

/**
 * Entry point for the Citadels game. This class serves as the main entry point for 
 * the Citadels card game application. It initializes the game environment and starts 
 * the game loop. The Citadels game is a medieval-themed strategy card game where 
 * players build districts and use character abilities to achieve victory.
 * 
 * @author Lakshya Sakhuja
 * @version 7.0
 */
public class App {

    /**
     * Creates a new App instance. The constructor initializes a new instance of 
     * the App class. Currently, it doesn't require any initialization parameters.
     */
    public App() {
    }

    /**
     * Main method to run the Citadels game. This method serves as the entry point 
     * of the application. It creates a new instance of the Game class and starts 
     * the game by calling the run method.
     *
     * @param args command-line arguments (not used in current implementation)
     */
    public static void main(String[] args) {
        Game game = new Game();
        game.run();
    }
}
