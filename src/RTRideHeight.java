/**
 * Ride height testing. Generate the centroid x, y, z report values of all CFD parts and tires at (0,0) ride height.
 * Lower the ride height to (-1, -1), generate the centroid x, y, z report values of CFD parts and tire. x and y values
 * of CFD parts should not change, z value should be 1 in lower. No report values of tires should change
 * Raise the ride height to (-1, 0) and generate centroid x, y, and z report values of CFD parts tire. y values should
 * not change, x should move forward a little, and z should be increased accordingly. Tire centroid should not change.
 */
public class RTRideHeight {

    private RTTestComponent rt;

    public RTRideHeight(RTTestComponent rt) {
        this.rt = rt;
    }

}
