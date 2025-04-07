package nl.tue.astar.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import lpsolve.MsgListener;

public class LPProblemProvider {

	private final LpSolve[] problems;
	private final AtomicBoolean[] problemsInUse;
	private final boolean[] deleted;
	private final AtomicInteger available;
	private final int numProblems;

	public LPProblemProvider(LpSolve problem, int num) throws LpSolveException {
		this.numProblems = num;
		problems = new LpSolve[num];
		problemsInUse = new AtomicBoolean[num];
		deleted = new boolean[num];
		available = new AtomicInteger(num);
		problems[0] = problem;
		problemsInUse[0] = new AtomicBoolean(false);
		for (int i = 1; i < num; i++) {
			problems[i] = problem.copyLp();
			problemsInUse[i] = new AtomicBoolean(false);
		}
	}

	public LPProblemProvider(LpSolve problem, int num, MsgListener listener, int msgHandle) throws LpSolveException {
		this(problem, num);
		for (int i = 0; i < problems.length; i++) {
			problem.putMsgfunc(listener, i, msgHandle);
		}
	}

	/**
	 * Returns the first available solver. If no solver is available, the
	 * current thread is put in a busy-wait state until one becomes available.
	 * 
	 * The returned solver can be used without the need for synchronizing on it.
	 * Furthermore, once finished with a solver, it should be returned in the
	 * finished method.
	 * 
	 * It is good practice to call the finished() method from a finally block
	 * after catching any exception coming from the solver, to make sure no
	 * solvers ever get lost.
	 * 
	 * @return
	 */
	public LpSolve firstAvailable() {
		//		while (available.compareAndSet(0, 0)) {
		//			synchronized (this) {
		//				try {
		//					this.wait();
		//				} catch (InterruptedException e) {
		//				}
		//			}
		//		}
		int i = 0;
		do {
			if (i == numProblems) {
				synchronized (this) {
					try {
						this.wait(25);
					} catch (InterruptedException e) {
					}
				}
			}
			i = 0;
			while (i < numProblems && !problemsInUse[i].compareAndSet(false, true)) {
				i = i + 1;
			}
			// if i == numProblems then no avaiable problem was found. Wait 25 ms and try again.
		} while (i == numProblems);
		// problem [i] was true and is now set to false.
		//		available.decrementAndGet();
		return problems[i];

		//		assert false;
		//		return null;
		//}
	}

	/**
	 * 
	 * Signals that this solver is done and can be used by another thread.
	 * 
	 * It is good practice to call the finished() method from a finally block
	 * after catching any exception coming from the solver, to make sure no
	 * solvers ever get lost.
	 * 
	 * @param solver
	 */
	public void finished(LpSolve solver) {
		for (int i = problems.length; i-- > 0;) {
			if (problems[i] == solver) {
				problemsInUse[i].compareAndSet(true, false);
				//				if (available.incrementAndGet() == 1) {
				//					// was 0, notify if a thread was listening.
				//					synchronized (this) {
				//						this.notify();
				//					}
				//				}
				return;
			}
		}
		// finished called on a solver not belonging to this provider
		assert false;
		//		}
	}

	public synchronized void deleteLps() {
		for (int i = problems.length; i-- > 0;) {
			if (!deleted[i]) {
				problemsInUse[i].set(true);
				try {
					problems[i].deleteAndRemoveLp();
				} catch (Exception e) {

				}
				deleted[i] = true;
			}
		}
	}

	// /**
	// * Cleanup all solvers, regardless whether they are currently in use. If
	// * this class is used correctly, then problemsInUse should be empty when
	// * finalize is called by the garbage collector.
	// */
	// protected void finalize() throws Throwable {
	// try {
	// for (int i = problems.length; i-- > 0;) {
	// try {
	// if (!deleted[i]) {
	// problemsInUse[i] = true;
	// problems[i].deleteLp();
	// deleted[i] = true;
	// }
	// } catch (Exception e) {
	// // Ignore
	// }
	// }
	// } finally {
	// super.finalize();
	// }
	// }
}
