package nl.andrewlalis.network_integration_demo.util;

/**
 * A type of thread with built-in shutdown capability via a 'running' flag.
 */
public abstract class StoppableThread extends Thread {
	private volatile boolean running;

	/**
	 * @return True if this thread is running, or false otherwise.
	 */
	public boolean isRunning() {
		return this.running;
	}

	/**
	 * Sets the thread to shutdown after it finishes the next loop iteration.
	 */
	public void shutdown() {
		this.running = false;
	}

	@Override
	public void run() {
		this.beforeStart();
		this.running = true;
		while (this.running) {
			this.step();
		}
		this.afterStop();
	}

	/**
	 * Can be overridden by child classes to add custom behavior for before the
	 * thread enters its main loop.
	 */
	public void beforeStart() {
		// Do nothing.
	}

	/**
	 * Executes one iteration of the thread's main loop.
	 */
	public abstract void step();

	/**
	 * Can be overridden by child classes to add custom behavior for after the
	 * thread has exited its main loop.
	 */
	public void afterStop() {
		// Do nothing.
	}
}
