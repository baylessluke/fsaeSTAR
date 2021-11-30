/**
 */

import star.common.StarMacro;

public class RTTestController extends StarMacro {

    RTTestComponent rt;

    public void execute() {

        rt = new RTTestComponent(getActiveSimulation());

        RTRideHeight rtRideHeight = new RTRideHeight(rt);
        rtRideHeight.preChange();
        rtRideHeight.postChange(-1, 0);
        // rtRideHeight.debug();

    }

}
