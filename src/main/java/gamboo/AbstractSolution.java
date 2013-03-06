package gamboo;

/**
 * Basic frame for real implementations of the Solution interface.
 */
public abstract class AbstractSolution implements Solution {
	@Override
	public abstract void run();
	
	/**
	 * Returns the time remaining to finish {@link #run()}.
	 */
	protected long getRemainingTime() {
		return ((GambooThread)Thread.currentThread()).getRemainingTime();
	}
}
