import star.base.neo.DoubleVector;
import star.base.neo.NeoObjectVector;
import star.base.report.VolumeIntegralReport;
import star.common.*;
import star.meshing.AutoMeshOperation;
import star.meshing.LatestMeshProxyRepresentation;
import star.meshing.MeshOperationManager;
import star.meshing.MeshOperationPart;
import star.surfacewrapper.SurfaceWrapperAutoMeshOperation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class RTSteering {

    private RTTestComponent rt;
    private final String FRONT_TIRE_REGION_NAME = "Front Tires Region";

    public RTSteering(RTTestComponent rt) {

        this.rt = rt;
        Collection<GeometryPart> frontLeft = getFrontTireParts("Front Left");
        Collection<GeometryPart> frontRight = getFrontTireParts("Front Right");
        double steeringAngle = -30 * (Math.PI / 180);

        this.rotate(frontLeft, rt.frontWheelCylindrical, steeringAngle);
        this.rotate(frontRight, rt.frontWheelSteering, steeringAngle);
        this.wrap(frontLeft, frontRight);
        this.mesh();

    }

    /**
     * Get the parts under the specified tire (String tire)
     */
    private Collection<GeometryPart> getFrontTireParts(String tire) {

        Collection<GeometryPart> frontTire = new ArrayList<>();
        for (GeometryPart part:rt.tireParts) {
            rt.sim.println(part.getPresentationName());
            if (part.getParentPart().getPresentationName().equals(tire)) {
                frontTire.add(part);
            }
        }
        return frontTire;

    }

    /**
     * Rotate tires for steering angle
     */
    private void rotate(Collection<GeometryPart> tire, CylindricalCoordinateSystem cs, double steeringAngle) {
        rt.sim.get(SimulationPartManager.class).rotateParts(tire, new DoubleVector(new double[]{1, 0, 0}), Arrays.asList(rt.unitless, rt.unitless, rt.unitless), steeringAngle, cs);
        cs.getLocalCoordinateSystemManager().rotateLocalCoordinateSystems(Collections.singletonList(cs), new DoubleVector(new double[] {1, 0, 0}), new NeoObjectVector(new Units[]{rt.unitless, rt.unitless, rt.unitless}), steeringAngle, cs);
    }

    /**
     * Surface wrap the tires and assign them to region. Need to delete and remake the regions each time to make sure the names are correct
     */
    private void wrap(Collection<GeometryPart> frontLeft, Collection<GeometryPart> frontRight) {

        // merge two lists into one
        Collection<GeometryPart> frontTires = new ArrayList<>();
        frontTires.addAll(frontLeft);
        frontTires.addAll(frontRight);

        // set surface wrapper parts
        SurfaceWrapperAutoMeshOperation wrapper = ((SurfaceWrapperAutoMeshOperation) rt.sim.get(MeshOperationManager.class).getObject(rt.SURFACE_WRAPPER_NAME));
        wrapper.getInputGeometryObjects().setObjects(frontTires);

        // wrap it
        wrapper.execute();

        // delete the existing front wheels region if it exists
        Collection<Region> regions = rt.sim.getRegionManager().getRegions();
        for (Region region:regions) {
            if (region.getPresentationName().equals(FRONT_TIRE_REGION_NAME)) {
                rt.sim.getRegionManager().removeRegion(region);
                break;
            }
        }

        // make the region
        MeshOperationPart wrapperPart = (MeshOperationPart) rt.sim.get(SimulationPartManager.class).getPart(rt.SURFACE_WRAPPER_NAME);
        rt.sim.getRegionManager().newRegionsFromParts(new NeoObjectVector(new Object[] {wrapperPart}), "OneRegion", null, "OneBoundaryPerPart", null, "OneFeatureCurve", null, false);
    }

    /**
     * Mesh the tires contained in surface wrapper
     */
    private void mesh() {

        AutoMeshOperation autoMesh = (AutoMeshOperation) rt.sim.get(MeshOperationManager.class).getObject(rt.AUTO_MESH_NAME);
        MeshOperationPart part = (MeshOperationPart) rt.sim.get(SimulationPartManager.class).getPart(rt.SURFACE_WRAPPER_NAME);
        autoMesh.getInputGeometryObjects().setObjects(part);
        autoMesh.execute();

    }

    /**
     * Set up the volume integral of centroid x report with the correct parts and the value
     */
    private void getReportValue() {

        String steeringName = "Steering (Volume integral of Centroid X)";
        double steeringExpected = -3.867e-2; // m^4

        // create stuff needed later
        VolumeIntegralReport report = (VolumeIntegralReport) rt.sim.getReportManager().getReport("Volume Integral of Centroid X");
        VectorComponentFieldFunction centroidX = (VectorComponentFieldFunction) ((PrimitiveFieldFunction) rt.sim.getFieldFunctionManager().getFunction("Centroid")).getComponentFunction(0);

        // set up report
        report.setFieldFunction(centroidX);
        report.setUnits(rt.sim.getUnitsManager().createUnits("m^4"));
        report.getParts().setObjects(rt.sim.getRegionManager().getRegion(FRONT_TIRE_REGION_NAME));
        report.setRepresentation(rt.latestSrfVol);

        // get report value and print result
        double reportValue = report.getValue();
        if (reportValue == steeringExpected)
           rt.printTestResults(true, steeringName, Double.toString(reportValue), Double.toString(steeringExpected));
        else
            rt.printTestResults(false, steeringName, Double.toString(reportValue), Double.toString(steeringExpected));
    }

}
