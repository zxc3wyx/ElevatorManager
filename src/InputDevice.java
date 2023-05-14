import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.ElevatorRequest;
import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;

import java.io.IOException;

public class InputDevice implements Runnable {
    private final Manager manager;
    private final AppRunner appRunner;

    public InputDevice(AppRunner appRunner, Manager manager) {
        this.appRunner = appRunner;
        this.manager = manager;
    }

    @Override
    public void run() {
        ElevatorInput elevatorInput = new ElevatorInput(System.in);
        String pattern = elevatorInput.getArrivingPattern();
        synchronized (manager) {
            manager.setPattern(pattern);
            manager.notifyAll();
        }
        while (true) {
            Request request = elevatorInput.nextRequest();
            if (request == null) {
                break;
            }
            synchronized (manager) {
                if (request instanceof PersonRequest) {
                    manager.addPersonRequest((PersonRequest)request);
                } else if (request instanceof ElevatorRequest) {
                    appRunner.addElevator(((ElevatorRequest)request).getElevatorId(),
                            ((ElevatorRequest)request).getElevatorType());
                    manager.addElevator((ElevatorRequest)request);
                }
                manager.notifyAll();
            }
        }
        synchronized (manager) {
            manager.setHasNext(false);
            manager.notifyAll();
        }
        try {
            elevatorInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
