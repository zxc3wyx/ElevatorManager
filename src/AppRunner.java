import com.oocourse.TimableOutput;

class AppRunner implements Runnable {
    private int elevatorNum = 3;
    private Manager manager;
    private InputDevice inputDevice;
    private Elevator[] elevator = new Elevator [5];
    private Thread[] elevatorThread = new Thread[5];

    public void addElevator(String elevatorId, String elevatorType) {
        elevator[elevatorNum] = new Elevator(manager, elevatorId, elevatorType.charAt(0) - 'A');
        elevatorThread[elevatorNum] = new Thread(elevator[elevatorNum]);
        elevatorThread[elevatorNum].start();
        elevatorNum++;
    }

    @Override
    public void run() {
        manager = new Manager();
        inputDevice = new InputDevice(this, manager);
        Thread inputThread = new Thread(inputDevice);
        for (int i = 0; i < 3; i++) {
            elevator[i] = new Elevator(manager, "" + (i + 1), i);
            elevatorThread[i] = new Thread(elevator[i]);
        }
        TimableOutput.initStartTimestamp();
        inputThread.start();
        for (int i = 0; i < 3; i++) {
            elevatorThread[i].start();
        }
    }
}
