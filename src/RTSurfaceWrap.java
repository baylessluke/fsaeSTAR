import star.common.*;
import star.meshing.MeshOperationManager;
import star.surfacewrapper.GapClosureOption;
import star.surfacewrapper.SurfaceWrapperAutoMeshOperation;
import star.surfacewrapper.SurfaceWrapperAutoMesher;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;

public class RTSurfaceWrap extends StarMacro {

    private static final String SURFACE_WRAPPER_NAME = "Surface wrapper";

    // Surface wrapper PPM test
    private static final String SURFACE_WRAPPER_PPM_NAME = "Surface wrapper PPM";
    private static final String SURFACE_WRAPPER_PPM_TEST = "Surface Wrapper PPM";
    private static final String SURFACE_WRAPPER_PPM_EXPECTED = "No part selected";

    // Gap closure test
    private static final String GAP_CLOSURE_TEST = "Gap Closure";
    private static final String GAP_CLOSURE_EXPECTED = "true";

    // Parts selected test
    private static final String PARTS_SELECTED_TEST = "Parts Selected";
    private static final String PARTS_SELECTED_EXPECTED = "All CFD_ and tires selected";

    @Override
    public void execute() {

        Simulation sim = getActiveSimulation();

        // Surface wrapper PPM
        wrapperPPM(sim);
        gapClosure(sim);


    }

    /**
     * Surface wrapper PPM check.
     * pass: no parts selected
     * @param sim
     */
    private void wrapperPPM(Simulation sim) {

        SurfaceWrapperAutoMeshOperation wrapper = ((SurfaceWrapperAutoMeshOperation) sim.get(MeshOperationManager.class).getObject(SURFACE_WRAPPER_PPM_NAME));
        GeometryObjectGroup partsSelected = wrapper.getInputGeometryObjects();
        int numParts = partsSelected.getChildrenCount();
        if (numParts == 0)
            RTTestController.printTestResults(true, SURFACE_WRAPPER_PPM_TEST, SURFACE_WRAPPER_PPM_EXPECTED, SURFACE_WRAPPER_PPM_EXPECTED);
        else
            RTTestController.printTestResults(false, SURFACE_WRAPPER_PPM_TEST, Integer.toString(numParts) + " parts selected", SURFACE_WRAPPER_PPM_EXPECTED);

    }

    /**
     * Check gap closure setting
     * pass: true
     * @param sim
     */
    private void gapClosure(Simulation sim) {

        SurfaceWrapperAutoMeshOperation wrapper = ((SurfaceWrapperAutoMeshOperation) sim.get(MeshOperationManager.class).getObject(SURFACE_WRAPPER_NAME));
        boolean gapClosureSetting = wrapper.getDoGapClosure();
        if (gapClosureSetting)
            RTTestController.printTestResults(true, GAP_CLOSURE_TEST, Boolean.toString(true), GAP_CLOSURE_EXPECTED);
        else
            RTTestController.printTestResults(false, GAP_CLOSURE_TEST, Boolean.toString(false), GAP_CLOSURE_EXPECTED);

    }

    /**
     * Check parts selected in surface wrapper
     * Pass: all CFD_ parts and tires are selected
     * @param sim
     */
    private void partsSelected(Simulation sim) {

        // get selected parts
        SurfaceWrapperAutoMeshOperation wrapper = ((SurfaceWrapperAutoMeshOperation) sim.get(MeshOperationManager.class).getObject(SURFACE_WRAPPER_NAME));
        GeometryObjectGroup group = wrapper.getInputGeometryObjects();
        Collection<GeometryPart> partsSelected = group.getLeafParts();

        // get expected parts
        Collection<GeometryPart> geomParts = sim.getGeometryPartManager().getParts(); // get all parents
        Collection<GeometryPart> expectedWrapperLeafParts = new ArrayList<>();
        for (GeometryPart parents:geomParts) {
            if (parents.getPresentationName().contains("CFD_") || parents.getPresentationName().equals("Front Left") || parents.getPresentationName().equals("Front Right") || parents.getPresentationName().equals("Rear Left") || parents.getPresentationName().equals("Rear Right")) {
                expectedWrapperLeafParts.addAll(parents.getLeafParts());
            }
        }

        // compare the two
        boolean testPassed = true;
        Collection<GeometryPart> unselectedParts = new ArrayList<>(); // parts that should be selected but are not selected
        for (GeometryPart parts:expectedWrapperLeafParts) {
            if (!partsSelected.contains(parts)) {
                testPassed = false;
                unselectedParts.add(parts);
            }
        }

        // print results
        if (testPassed)
            RTTestController.printTestResults(true, PARTS_SELECTED_TEST, PARTS_SELECTED_EXPECTED, PARTS_SELECTED_EXPECTED)
        else {
            StringBuilder unselectedPartsString = new StringBuilder("Parts: ");
            for (GeometryPart part:unselectedParts) {
                unselectedPartsString.append(part.getPresentationName() + " ,");
            }
            unselectedPartsString.delete(unselectedPartsString.length() - 2, unselectedPartsString.length());
            unselectedPartsString.append(" NOT selected");

            RTTestController.printTestResults(false, PARTS_SELECTED_TEST, unselectedPartsString.toString(), PARTS_SELECTED_TEST);
        }
    }

}
