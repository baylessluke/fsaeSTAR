/**
 */

import star.common.StarMacro;

public class RTTestController extends StarMacro {

    RTTestComponent rt;

    public void execute() {

        rt = new RTTestComponent(getActiveSimulation());

        // steering check
        // new RTSteering(rt);

        // Ride height check
        // RTRideHeight rtRideHeight = new RTRideHeight(rt);
        // rtRideHeight.preChange();
        // rtRideHeight.postChange(-1, 0);

        // Roll check
        // RTRideHeight rollTest = new RTRideHeight(rt);
        // rollTest.preChange();
        // rollTest.testRoll(3);
        // rollTest.postChange(3);
        // rollTest.debug(3);

        // Subtract check
        // new RTSubtract(rt);

        // Regions check
        new RTRegions(rt);

    }

}
