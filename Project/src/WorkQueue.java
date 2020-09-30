import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple work queue implementation based on the IBM Developer article by Brian Goetz. It is up to
 * the user of this class to keep track of whether there is any pending work remaining.
 *
 * @see <a href="https://www.ibm.com/developerworks/library/j-jtp0730/index.html"> Java Theory and
 *      Practice: Thread Pools and Work Queues</a>
 */
public class WorkQueue {

	/**
	 * logger to log errors and debug
	 */
	private final static Logger log = LogManager.getLogger(WorkQueue.class);

	/** Pool of worker threads that will wait in the background until work is available. */
	private final PoolWorker[] workers;

	/** Queue of pending work requests. */
	private final LinkedList<Runnable> queue;

	/** Used to signal the queue should be shutdown. */
	private volatile boolean shutdown;

	/** The default number of threads to use when not specified. */
	public static final int DEFAULT = 5;

	/**
	 * variable used to count the amount of working threads
	 */
	private int count;

	/**
	 * Starts a work queue with the default number of threads.
	 *
	 * @see #WorkQueue(int)
	 */
	public WorkQueue() {
		this(DEFAULT);
	}

	/**
	 * Starts a work queue with the specified number of threads.
	 *
	 * @param threads number of worker threads; should be greater than 1
	 */
	public WorkQueue(int threads) {
		this.queue = new LinkedList<Runnable>();
		this.workers = new PoolWorker[threads];

		this.shutdown = false;
		this.count = 0;

		// start the threads so they are waiting in the background
		for (int i = 0; i < threads; i++) {
			workers[i] = new PoolWorker();
			workers[i].start();
		}
	}

	/**
	 * Adds a work request to the queue. A thread will process this request when available.
	 *
	 * @param r work request (in the form of a {@link Runnable} object)
	 */
	public void execute(Runnable r) {
		// a lock built into incrementing it
		increment();
		synchronized (queue) {
			queue.addLast(r);
			queue.notifyAll();
		}
	}

	/**
	 * synchronized on this one thread can access it at a time
	 */
	private synchronized void increment() {
		count++;
	}

	/**
	 * synchronized on this one thread can access it at a time
	 */
	private synchronized void decrement() {
		count--;
		if (count == 0) {
			this.notifyAll();
		}
	}

	/**
	 * Waits for all pending work to be finished.
	 */
	public void finish() {
		// this is class based - only one thing is waiting on this class at a time
		synchronized (this) {
			while (count > 0) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					log.error("Did not finish");
				}
			}
			assert count == 0;
		}

	}

	/**
	 * Asks the queue to shutdown. Any unprocessed work will not be finished, but threads in-progress
	 * will not be interrupted.
	 */
	public void shutdown() {
		// safe to do unsynchronized due to volatile keyword
		shutdown = true;

		synchronized (queue) {
			queue.notifyAll();
		}
	}

	/**
	 * Returns the number of worker threads being used by the work queue.
	 *
	 * @return number of worker threads
	 */
	public int size() {
		return workers.length;
	}

	/**
	 * Waits until work is available in the work queue. When work is found, will remove the work from
	 * the queue and run it. If a shutdown is detected, will exit instead of grabbing new work from the
	 * queue. These threads will continue running in the background until a shutdown is requested.
	 */
	private class PoolWorker extends Thread {

		@Override
		public void run() {
			Runnable r = null;

			while (true) {
				synchronized (queue) {
					while (queue.isEmpty() && !shutdown) {
						try {
							queue.wait();
						} catch (InterruptedException ex) {
							log.error("Warning: Work queue interrupted.");
							Thread.currentThread().interrupt();
						}
					}

					// exit while for one of two reasons:
					// (a) queue has work, or (b) shutdown has been called

					if (shutdown) {
						break;
					} else {
						r = queue.removeFirst();
					}
				}

				try {
					r.run();
				} catch (RuntimeException ex) {
					// catch runtime exceptions to avoid leaking threads
					log.error("Warning: Work queue encountered a runtime exception while running.");
					log.catching(ex);

				} finally {
					decrement();
				}
			}
		}
	}
}
