public class Algorithm {
    static boolean cCanArrive(int fromFloor, int toFloor) {         //电梯C可达
        if (fromFloor > 3 && fromFloor < 18) {
            return false;
        }
        return toFloor <= 3 || toFloor >= 18;
    }

    static boolean bCanArrive(int fromFloor, int toFloor) {         //电梯B可达
        return ((fromFloor & 1) == 1) && ((toFloor & 1) == 1);
    }
}
