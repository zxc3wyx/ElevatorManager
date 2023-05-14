import com.oocourse.TimableOutput;
import com.oocourse.elevator3.PersonRequest;

import java.util.ArrayList;
import java.util.Vector;

public class Elevator implements Runnable {
    private final Manager manager;
    private final String id;
    private final int type;         //A->0, B->1, C->2
    private final int moveSpeed;
    private final int volume;
    private ArrayList<PersonRequest> persons = new ArrayList<>();                       //在电梯里的人
    private int currentFloor = 1;
    private boolean isOpen = false;
    private int high = 1;
    private int low = 1;

    public Elevator(Manager manager, String id, int type) {
        this.manager = manager;
        this.id = id;
        this.type = type;
        moveSpeed = 600 - 200 * type;
        volume = 8 - 2 * type;
    }

    public int getType() {
        return type;
    }

    private void openDoor(int floor) {
        isOpen = true;
        TimableOutput.println("OPEN-" + floor + "-" + id);
    }

    private void closeDoor(int floor) {
        isOpen = false;
        TimableOutput.println("CLOSE-" + floor + "-" + id);
    }

    private void arriveAt(int floor) {
        TimableOutput.println("ARRIVE-" + floor + "-" + id);
    }

    private void personInElevator(PersonRequest request, int floor) {
        TimableOutput.println("IN-" + request.getPersonId() + "-" + floor + "-" + id);
    }

    private void personOutElevator(PersonRequest request, int floor) {
        TimableOutput.println("OUT-" + request.getPersonId() + "-" + floor + "-" + id);
    }

    ArrayList<PersonRequest> getOut(int floor) {
        ArrayList<PersonRequest> ret = new ArrayList<>();
        for (int i = persons.size() - 1; i >= 0; i--) {
            if (persons.get(i).getToFloor() == floor) {
                ret.add(persons.get(i));
                persons.remove(i);
            }
        }
        return ret;
    }

    private void reCalculateMinAndMax() {
        low = 20;
        high = 1;
        for (PersonRequest request: persons) {
            high = Math.max(high, request.getToFloor());
            low = Math.min(low, request.getToFloor());
        }
    }

    private void inAndOut() {
        ArrayList<PersonRequest> personOut = getOut(currentFloor);
        if (!personOut.isEmpty()) {
            openDoor(currentFloor);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (PersonRequest request: personOut) {
                personOutElevator(request, currentFloor);
            }
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Vector<PersonRequest> personAdd;
        synchronized (manager) {
            personAdd = manager.selectPersonToElevator(currentFloor, volume - persons.size(), id);
            manager.notifyAll();
        }
        if (!personAdd.isEmpty()) {
            if (!isOpen) {
                openDoor(currentFloor);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (PersonRequest request: personAdd) {
                personInElevator(request, currentFloor);
                high = Math.max(high, request.getToFloor());
                low = Math.min(low, request.getToFloor());
                persons.add(request);
            }
        }
        synchronized (manager) {
            if (persons.size() < volume) {
                high = Math.max(manager.selectHighestFloor(id), high);
                low = Math.min(manager.selectLowestFloor(id), low);
            } else {
                reCalculateMinAndMax();
            }
            manager.notifyAll();
        }
        if (isOpen) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            closeDoor(currentFloor);
        }
    }

    private void waitPeople() {
        while (persons.isEmpty() && manager.hasNextPerson() && manager.emptyRequest(id)) {
            synchronized (manager) {
                try {
                    manager.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                high = manager.selectHighestFloor(id);
                low = manager.selectLowestFloor(id);
                manager.notifyAll();
            }
        }
    }

    private void runModelRandom() {
        while (true) {
            waitPeople();
            if (persons.isEmpty() && !manager.hasNextPerson() && manager.emptyRequest(id)) {
                return;
            }
            while (high > currentFloor) {
                inAndOut();
                if (high <= currentFloor) {
                    break;
                }
                if (persons.isEmpty() && !manager.hasNextPerson() && manager.emptyRequest(id)) {
                    return;
                }
                try {
                    Thread.sleep(moveSpeed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentFloor++;
                arriveAt(currentFloor);
            }
            inAndOut();
            waitPeople();
            if (persons.isEmpty() && !manager.hasNextPerson() && manager.emptyRequest(id)) {
                return;
            }
            while (low < currentFloor) {
                inAndOut();
                if (low >= currentFloor) {
                    break;
                }
                if (persons.isEmpty() && !manager.hasNextPerson() && manager.emptyRequest(id)) {
                    return;
                }
                try {
                    Thread.sleep(moveSpeed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentFloor--;
                arriveAt(currentFloor);
            }
            inAndOut();
        }
    }

    private boolean loadMorning() {
        boolean hasNextPerson = true;
        while (persons.size() < volume) {
            synchronized (manager) {
                while (manager.hasNextPerson() && manager.emptyRequest(id)) {
                    try {
                        manager.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!manager.hasNextPerson() && manager.emptyRequest(id)) {
                    hasNextPerson = false;
                    break;
                }
                PersonRequest request = manager.selectOnePersonToElevator(id);
                personInElevator(request, 1);
                persons.add(request);
            }
        }
        return hasNextPerson;
    }

    private void runModelMorning() {
        boolean hasNextPerson = true;
        while (hasNextPerson) {
            openDoor(1);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hasNextPerson = loadMorning();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            closeDoor(1);
            for (PersonRequest request: persons) {
                if (high < request.getToFloor()) {
                    high = request.getToFloor();
                }
            }
            while (currentFloor < high) {
                try {
                    Thread.sleep(moveSpeed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                currentFloor++;
                arriveAt(currentFloor);
                for (int i = persons.size() - 1; i >= 0; i--) {
                    if (persons.get(i).getToFloor() == currentFloor) {
                        openDoor(currentFloor);
                        try {
                            Thread.sleep(400);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        personOutElevator(persons.get(i), currentFloor);
                        closeDoor(currentFloor);
                        persons.remove(i);
                    }
                }
            }
            if (hasNextPerson) {
                while (currentFloor > 1) {
                    try {
                        Thread.sleep(moveSpeed);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    currentFloor--;
                    arriveAt(currentFloor);
                }
            }
            high = 1;
        }
    }

    @Override
    public void run() {
        synchronized (manager) {
            while (manager.getPattern().equals("NOT READY")) {
                try {
                    manager.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            manager.notifyAll();
        }
        switch (manager.getPattern()) {
            case "Random":
            case "Night":
                runModelRandom();
                break;
            case "Morning":
                runModelMorning();
                break;
            default:
                break;
        }
    }
}
