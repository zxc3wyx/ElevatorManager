import com.oocourse.elevator3.ElevatorRequest;
import com.oocourse.elevator3.PersonRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class Manager {                  //托盘
    private ArrayList<Vector<PersonRequest>> requestList = new ArrayList<>();
    private volatile boolean hasNext = true;
    private String pattern = "NOT READY";
    private HashMap<String, Integer> elevatorId2No = new HashMap<>();
    private ArrayList<Integer> typeList = new ArrayList<>();
    private int elevatorNum = 3;
    private int[] typeNums = {1, 1, 1};

    public Manager() {
        for (int i = 0; i < 3; i++) {
            requestList.add(new Vector<>());
        }
        elevatorId2No.put("1", 0);
        elevatorId2No.put("2", 1);
        elevatorId2No.put("3", 2);
        typeList.addAll(Arrays.asList(0, 1, 2));
    }

    public void addElevator(ElevatorRequest request) {
        requestList.add(new Vector<>());
        elevatorId2No.put(request.getElevatorId(), elevatorNum);
        typeList.add(request.getElevatorType().charAt(0) - 'A');
        typeNums[request.getElevatorType().charAt(0) - 'A']++;
        elevatorNum++;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean hasNextPerson() {
        return hasNext;
    }

    public boolean emptyRequest(String id) {
        return requestList.get(elevatorId2No.get(id)).isEmpty();
    }

    public void addPersonRequest(PersonRequest personRequest, int type) {
        ArrayList<Integer> correspondList = new ArrayList<>();
        for (int i = 0; i < requestList.size(); i++) {              //type型电梯
            if (typeList.get(i) == type) {
                correspondList.add(i);
            }
        }
        int min = Integer.MAX_VALUE;
        int book = 0;
        for (Integer i: correspondList) {
            if (requestList.get(i).size() < min) {
                min = requestList.get(i).size();
                book = i;
            }
        }
        requestList.get(book).add(personRequest);
    }

    public void addPersonRequest(PersonRequest personRequest) {
        int wholePeople = 0;
        int ctPeople = 0;
        int btPeople = 0;
        for (int i = 0; i < elevatorNum; i++) {
            for (int j = 0; j < requestList.get(i).size(); j++) {
                wholePeople++;
                if (typeList.get(i) == 2) {
                    ctPeople++;
                }
                if (typeList.get(i) == 1) {
                    btPeople++;
                }
            }
        }
        if (Algorithm.cCanArrive(personRequest.getFromFloor(), personRequest.getToFloor())
                && (3 + 2 * typeNums[2]) * ctPeople <= 2 * typeNums[2] * wholePeople) {
            addPersonRequest(personRequest, 2);
        } else if (Algorithm.bCanArrive(personRequest.getFromFloor(), personRequest.getToFloor())
                && (3 + 2 * typeNums[1] * btPeople <= 2 * typeNums[1] * wholePeople)) {
            addPersonRequest(personRequest, 1);
        } else {
            addPersonRequest(personRequest, 0);
        }
    }

    public int selectHighestFloor(String id) {
        synchronized (this) {
            int high = 1;
            Vector<PersonRequest> list = requestList.get(elevatorId2No.get(id));
            for (PersonRequest request: list) {
                if (request.getFromFloor() > high) {
                    high = request.getFromFloor();
                }
            }
            notifyAll();
            return high;
        }
    }

    public int selectLowestFloor(String id) {
        synchronized (this) {
            int low = 20;
            Vector<PersonRequest> list = requestList.get(elevatorId2No.get(id));
            for (PersonRequest request: list) {
                if (request.getFromFloor() < low) {
                    low = request.getFromFloor();
                }
            }
            notifyAll();
            return low;
        }
    }

    public Vector<PersonRequest> selectPersonToElevator(int floor, int limit, String id) {
        Vector<PersonRequest> ret = new Vector<>();
        Vector<PersonRequest> list = requestList.get(elevatorId2No.get(id));
        for (int i = list.size() - 1; i >= 0; i--) {
            if (ret.size() == limit) {
                break;
            }
            if (list.get(i).getFromFloor() == floor) {
                ret.add(list.get(i));
                list.remove(i);
            }
        }
        return ret;
    }

    public PersonRequest selectOnePersonToElevator(String id) {
        return requestList.get(elevatorId2No.get(id)).remove(0);
    }
}
