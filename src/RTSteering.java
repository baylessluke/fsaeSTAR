import star.common.GeometryPart;

import java.util.ArrayList;
import java.util.Collection;

public class RTSteering {

    private RTTestComponent rt;

    public RTSteering(RTTestComponent rt) {

        this.rt = rt;
        Collection<GeometryPart> frontLeft = getFrontTireParts("Front Left");
        Collection<GeometryPart> frontRight = getFrontTireParts("Front Right");

    }

    /**
     * Get the parts under the specified tire (String tire)
     */
    private Collection<GeometryPart> getFrontTireParts(String tire) {

        Collection<GeometryPart> frontTire = new ArrayList<>();
        for (GeometryPart part:rt.tireParts) {
            if (part.getParentPart().getPresentationName().equals(tire)) {
                frontTire.add(part);
            }
        }
        return frontTire;

    }

    /**
     * Surface wrap the tires and assign them to region. Need to delete and remake the regions each time to make sure the names are correct
     */
    private void wrap() {

    }

    /**
     * Mesh the tires
     */
    private void mesh() {

    }

    /**
     * Set up the volume integral of centroid x report with the correct parts and the value
     */
    private void getReportValue() {

    }



}
