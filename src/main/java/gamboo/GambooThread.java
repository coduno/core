package gamboo;

import java.lang.management.ManagementFactory;
import static java.lang.Thread.State.*;

/** 
 * A simple Thread with some additional functionality to store a timeout and
 * check how much time is left during execution.
 */
public class GambooThread extends Thread {
	private final long timeout;
	private long time = 0;
	
	public long getCurrentTime() {
		if (super.getState() == NEW)
			return 0;
		else if (super.getState() == TERMINATED)
			return getTotalTime();
		else
			return ManagementFactory.getThreadMXBean().getThreadCpuTime(getId());
	}

	public long getTotalTime() {		
		return time;
	}
	
	public long getRemainingTime() {
		if (timeout <= 0)
			return Long.MAX_VALUE;
	
		return getTimeout() - getCurrentTime();
	}
	
	public long getTimeout() {
		return timeout;
	}
	
	public GambooThread() {
		super();
		this.timeout = 0;
	}
	
	public GambooThread(long timeout) {
		super();
		this.timeout = timeout;
	}
	
	public GambooThread(Runnable target, String name, long timeout) {
		super(target, name);
		this.timeout = timeout;
	}
	
	public GambooThread(String name, long timeout) {
		super(name);
		this.timeout = timeout;
	}
	
	public GambooThread(ThreadGroup group, Runnable target, long timeout) {
		super(group, target);
		this.timeout = timeout;
	}
	
	public GambooThread(ThreadGroup group, Runnable target, String name, long timeout) {
		super(group, target, name);
		this.timeout = timeout;
	}
	
	public GambooThread(ThreadGroup group, Runnable target, String name, long stackSize, long timeout) {
		super(group, target, name, stackSize);
		this.timeout = timeout;
	}
	
	public GambooThread(ThreadGroup group, String name, long timeout) {
		super(group, name);
		this.timeout = timeout;
	}

	@Override
	public void run() {
		super.run();
		time = ManagementFactory.getThreadMXBean().getThreadCpuTime(getId());
	}
}
