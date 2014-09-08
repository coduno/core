package uno.cod;

import java.util.List;
import java.io.OutputStreamWriter;

/**
 * Basic interface to implement game scenarios/simulations compatible with
 * {@link uno.cod.Coduno}.
 */
public interface Game<T extends Solution> {
	/**
	 * Read by {@link uno.cod.Coduno} to decide how many player IDs
	 * are needed to play this game.
	 *
	 * @return how many different implementations are needed to play this
	 *         game, thus how many real programmers compete in this game.
	 */
	int getPlayerCount();
	
	/**
	 * Read by {@link uno.cod.Coduno} to reward the winner of the game.
	 *
	 * @return the implemntation that won.
	 */
	Solution getWinner();
	
	/**
	 * Invoked after instantiating this game, to pass the implementations.
	 * These should be adequately stored by this game (for example by 
	 * cloning <tt>players</tt>.
	 *
	 * @param players the implementations comepting in this instance, it's
	 *                size <tt>players</tt> always correlates with
	 *                {@link #getPlayerCount()}.
	 */
	<S extends Solution> void load(final List<S> players);
	
	/**
	 * Running the actual simulation.
	 *
	 * @param args all commandline parameters that have not been consumed by
	 *             the framework. You should read your random seed and other
	 *             simulation parameters from there.
	 * @param writer points to the logfile that was created for the current
	 *               simulation. It is later processed by the Coduno website
	 *               to visualize the simulation.
	 */
	void run(List<String> args, OutputStreamWriter writer);
}
