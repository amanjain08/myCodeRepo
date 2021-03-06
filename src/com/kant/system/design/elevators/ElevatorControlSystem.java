/**
 * 
 */
package com.kant.system.design.elevators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeSet;

/**
 * Elevator in this program implements following features - <br/>
 * 
 * If elevator is going up or down, it checks for nearest floor request to
 * process first in that direction. <br/>
 * 
 * If there is no request to process, it waits at last processed floor. <br/>
 *
 * If a new request comes while elevator is processing a request. It process the
 * new request first if it is nearest than the processing floor in same
 * direction.
 * 
 * 
 * For centralized System
 * https://hellosmallworld123.wordpress.com/2014/08/03/design
 * -an-object-oriented-elevator/
 * 
 * 
 * @author shaskant
 *
 */
public class ElevatorControlSystem {
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Welcome to MyLift");
		// RequestListenerThread to read requested floor and add to Set
		Thread requestListenerThread = new Thread(new RequestListener(),
				"RequestListenerThread");
		// RequestProcessorThread to read Set and process requested floor
		Thread requestProcessorThread = new Thread(
				new ElevatorRequestProcessor(), "RequestProcessorThread");

		Elevator.getInstance()
				.setRequestProcessorThread(requestProcessorThread);

		requestListenerThread.start();
		requestProcessorThread.start();
	}
}

/**
 * 
 * @author shaskant
 *
 */
class Elevator {
	private static Elevator elevator = null;
	// will be accessed from multiple threads ,synchronize it
	private TreeSet<Integer> requestSet = new TreeSet<Integer>();
	private int currentFloor = 0;
	private Direction direction = Direction.ELEVATOR_UP;

	private Elevator() {
	}

	private Thread requestProcessorElevator = null;

	/**
	 * make singleton
	 */
	public static Elevator getInstance() {
		if (elevator == null) {
			synchronized (Elevator.class) {
				if (elevator == null) {
					elevator = new Elevator();
				}
			}
		}
		return elevator;
	}

	public Direction getDirection() {
		return direction;
	}

	/**
	 * cannot directly set direction of elevator from outside of this class
	 * 
	 * @param direction
	 */
	private void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Thread getRequestProcessorThread() {
		return requestProcessorElevator;
	}

	public void setRequestProcessorThread(Thread requestProcessorThread) {
		this.requestProcessorElevator = requestProcessorThread;
	}

	public TreeSet<Integer> getRequestSet() {
		return requestSet;
	}

	public void setRequestSet(TreeSet<Integer> requestSet) {
		this.requestSet = requestSet;
	}

	public int getFloor() {
		return currentFloor;
	}

	/**
	 * Only {@link ElevatorRequestProcessor} can set the current floor and hence
	 * decides the direction to move to.
	 * 
	 * @param requestedfloor
	 * @throws InterruptedException
	 */
	public void setCurrentFloor(int requestedfloor) throws InterruptedException {
		if (this.currentFloor > requestedfloor) {
			setDirection(Direction.ELEVATOR_DOWN);
		} else {
			setDirection(Direction.ELEVATOR_UP);
		}
		this.currentFloor = requestedfloor;

		System.out.println("Floor : " + currentFloor);
		Thread.sleep(2000);
	}

	/**
	 * TODO Move this strategy out of this class. Delegate the responsibility to
	 * decide next floor to process next.
	 * 
	 * @return next request to process based on elevator current floor and
	 *         direction
	 */
	public synchronized int nextFloor() {
		Integer nxtFloor = null;

		if (!requestSet.isEmpty()) {
			if (direction == Direction.ELEVATOR_UP) {
				if (requestSet.ceiling(currentFloor) != null) {
					// set to next greater floor closest to current floor
					nxtFloor = requestSet.ceiling(currentFloor);
				} else {
					/**
					 * else based on request choose closest , can set lift to
					 * downward direction
					 */
					nxtFloor = requestSet.floor(currentFloor);
				}
			} else {
				if (requestSet.floor(currentFloor) != null) {
					nxtFloor = requestSet.floor(currentFloor);
				} else {
					/**
					 * else based on request choose closest , can set lift to
					 * upward direction
					 */
					nxtFloor = requestSet.ceiling(currentFloor);
				}
			}
		}

		if (nxtFloor == null) {
			try {
				/**
				 * only a new request can notify RequestProcessorThread.
				 */
				System.out.println("Waiting at Floor :" + getFloor());
				wait();
			} catch (InterruptedException e) {
				/**
				 * in case some issue with elevator system .. print stack trace
				 * and return -1.
				 */
				e.printStackTrace();
			}
		} else {
			/**
			 * Remove the request from Set as it is the request in Progress.
			 */
			requestSet.remove(nxtFloor);
		}
		return (nxtFloor == null) ? -1 : nxtFloor;
	}

	/**
	 * On events when a button is pressed.
	 * 
	 * @param f
	 */
	public synchronized void addFloor(int f) {
		requestSet.add(f);

		if (requestProcessorElevator.getState() == Thread.State.WAITING) {
			// Notify processor thread that a new request has come if it is
			// waiting
			notify();
		} else {
			// Interrupt Processor thread to check if new request should be
			// processed before current request or not.
			requestProcessorElevator.interrupt();
		}
	}

	/**
	 * 
	 * @return
	 */
	public ElevatorStatus status() {
		return (requestSet.size() > 0) ? ElevatorStatus.ELEVATOR_OCCUPIED
				: ElevatorStatus.ELEVATOR_EMPTY;
	}

}

enum Direction {
	ELEVATOR_UP, ELEVATOR_DOWN
}

enum ElevatorStatus {
	ELEVATOR_OCCUPIED, ELEVATOR_EMPTY;
}

/**
 * [Controller] Each elevator has a thread that runs it's request processing.
 * 
 * @author shaskant
 *
 */
class ElevatorRequestProcessor implements Runnable {

	/**
	 * running job
	 */
	@Override
	public void run() {
		Elevator elevator = Elevator.getInstance();
		// serve always
		while (true) {
			// get next floor to go to .. will make this thread wait if no
			// requests are there no process
			int nxtFloor = elevator.nextFloor();
			// get current floor
			int currentFloor = elevator.getFloor();
			try {
				// only process if next floor value > 0
				if (nxtFloor >= 0) {
					if (currentFloor > nxtFloor) {
						// reach down to that floor
						while (currentFloor > nxtFloor) {
							elevator.setCurrentFloor(--currentFloor);
						}
					} else {
						// reach up to that floor
						while (currentFloor < nxtFloor) {
							elevator.setCurrentFloor(++currentFloor);
						}
					}
					System.out
							.println("Reached Floor : " + elevator.getFloor());
				}
			} catch (InterruptedException e) {
				// If a new request has interrupted a current request processing
				// then check
				// >if the current request is already processed fine.
				// >otherwise add it back in request Set again
				if (elevator.getFloor() != nxtFloor) {
					elevator.getRequestSet().add(nxtFloor);
				}
			}
		}
	}
}

/**
 * 
 * @author shaskant
 *
 */
class RequestListener implements Runnable {

	@Override
	public void run() {

		while (true) {
			String floorNumberStr = null;
			try {
				// Read input from console
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(System.in));
				floorNumberStr = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Elevator elevator = Elevator.getInstance();
			if (isValidFloorNumber(floorNumberStr)) {
				System.out.println("User Pressed : " + floorNumberStr);
				elevator.addFloor(Integer.parseInt(floorNumberStr));
			} else {
				System.out.println("Floor Request Invalid : " + floorNumberStr);
			}
		}
	}

	/**
	 * This method is used to define maximum floors this elevator can process.
	 * 
	 * @param s
	 *            - requested floor
	 * @return true if requested floor is integer and upto two digits. (max
	 *         floor = 99)
	 */
	private boolean isValidFloorNumber(String s) {
		return (s != null) && s.matches("\\d{1,2}");
	}

}