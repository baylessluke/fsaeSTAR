/*
 * TO-DO: surface wrapper 3D scene export
 */

import star.common.*;
import star.meshing.MeshOperationManager;
import star.meshing.SurfaceCustomMeshControl;
import star.surfacewrapper.SurfaceWrapperAutoMeshOperation;

import java.util.ArrayList;
import java.util.Collection;

public class RTSurfaceWrap {

    RTTestController test;

    private static final String SURFACE_WRAPPER_NAME = "Surface wrapper";

    // Surface wrapper PPM test
    private final String SURFACE_WRAPPER_PPM_NAME = "Surface wrapper PPM";
    private final String SURFACE_WRAPPER_PPM_TEST = "Surface Wrapper PPM";
    private final String SURFACE_WRAPPER_PPM_EXPECTED = "No part selected";

    // Gap closure test
    private final String GAP_CLOSURE_TEST = "Gap Closure";
    private final String GAP_CLOSURE_EXPECTED = "true";

    // Parts selected test
    private final String PARTS_SELECTED_TEST = "Parts Selected";
    private final String PARTS_SELECTED_EXPECTED = "All CFD_ and tires selected";

    // Aero control test
    private final String AERO_CONTROL_NAME = "Aero Control";
    private final String AERO_CONTROL_PARTS_SELECTED_TEST = "Aero Control Parts Selected";
    private final String AERO_CONTROL_PARTS_SELECTED_EXPECTED = "All CFD_Aerodynamics_830250079 parts selected";
    private final String AERO_CONTROL_AVAILABILITY_TEST = "Aero Control Availability";
    private final String AERO_CONTROL_AVAILABILITY_EXPECTED = "Aero control enabled";

    /**
     * Execute the tests
     */
    public void execute(Simulation sim) {

        test = new RTTestController();
        SurfaceWrapperAutoMeshOperation wrapper = ((SurfaceWrapperAutoMeshOperation) sim.get(MeshOperationManager.class).getObject(SURFACE_WRAPPER_NAME));

        // Surface wrapper PPM
        wrapperPPM(sim);
        gapClosure(wrapper);
        partsSelected(wrapper);
        aeroControl(wrapper);

    }

    /**
     * Surface wrapper PPM check.
     * pass: no parts selected
     */
    private void wrapperPPM(Simulation sim) {

        SurfaceWrapperAutoMeshOperation wrapper = ((SurfaceWrapperAutoMeshOperation) sim.get(MeshOperationManager.class).getObject(SURFACE_WRAPPER_PPM_NAME));
        GeometryObjectGroup partsSelected = wrapper.getInputGeometryObjects();
        int numParts = partsSelected.getChildrenCount();
        if (numParts == 0)
            test.printTestResults(true, SURFACE_WRAPPER_PPM_TEST, SURFACE_WRAPPER_PPM_EXPECTED, SURFACE_WRAPPER_PPM_EXPECTED);
        else
            test.printTestResults(false, SURFACE_WRAPPER_PPM_TEST, numParts + " parts selected", SURFACE_WRAPPER_PPM_EXPECTED);

    }

    /**
     * Check gap closure setting
     * pass: true
     */
    private void gapClosure(SurfaceWrapperAutoMeshOperation wrapper) {

        boolean gapClosureSetting = wrapper.getDoGapClosure();
        if (gapClosureSetting)
            test.printTestResults(true, GAP_CLOSURE_TEST, Boolean.toString(true), GAP_CLOSURE_EXPECTED);
        else
            test.printTestResults(false, GAP_CLOSURE_TEST, Boolean.toString(false), GAP_CLOSURE_EXPECTED);

    }

    /**
     * Check parts selected in surface wrapper
     * Pass: all CFD_ parts and tires are selected
     */
    private void partsSelected(SurfaceWrapperAutoMeshOperation wrapper) {

        // get selected parts
        Collection<GeometryPart> partsSelected = wrapper.getInputGeometryObjects().getLeafParts();

        // compare selected parts against CFD parts
        boolean testPassed = true;
        Collection<GeometryPart> unselectedParts = new ArrayList<>(); // parts that should be selected but are not selected
        for (GeometryPart parts:test.cfdParts) {
            if (!partsSelected.contains(parts)) {
                testPassed = false;
                unselectedParts.add(parts);
            }
        }

        // print results
        if (testPassed)
            test.printTestResults(true, PARTS_SELECTED_TEST, PARTS_SELECTED_EXPECTED, PARTS_SELECTED_EXPECTED);
        else {
            StringBuilder unselectedPartsString = new StringBuilder("Parts: ");
            for (GeometryPart part:unselectedParts) {
                unselectedPartsString.append(part.getPresentationName()).append(" ,");
            }
            unselectedPartsString.delete(unselectedPartsString.length() - 2, unselectedPartsString.length());
            unselectedPartsString.append(" NOT selected");

            test.printTestResults(false, PARTS_SELECTED_TEST, unselectedPartsString.toString(), PARTS_SELECTED_TEST);
        }
    }

    /**
     * Check parts selected in aero control
     * Pass: all parts in CFD_Aerodynamics_830250079 selected
     */
    private void aeroControl(SurfaceWrapperAutoMeshOperation wrapper) {

        SurfaceCustomMeshControl aeroControl = ((SurfaceCustomMeshControl) wrapper.getCustomMeshControls().getObject(AERO_CONTROL_NAME));

        // get parts selected
        Collection<GeometryPart> partsSelected = aeroControl.getGeometryObjects().getLeafParts();

        // compare selected parts against aero parts
        boolean testPassed = true;
        Collection<GeometryPart> unselectedParts = new ArrayList<>(); // parts that should be selected but are not selected
        for (GeometryPart parts:test.aeroParts) {
            if (!partsSelected.contains(parts)) {
                testPassed = false;
                unselectedParts.add(parts);
            }
        }

        // print results of aero control parts selected test
        if (testPassed)
            test.printTestResults(true, AERO_CONTROL_PARTS_SELECTED_TEST, AERO_CONTROL_PARTS_SELECTED_EXPECTED, AERO_CONTROL_PARTS_SELECTED_EXPECTED);
        else {
            StringBuilder unselectedPartsString = new StringBuilder("Parts: ");
            for (GeometryPart part:unselectedParts) {
                unselectedPartsString.append(part.getPresentationName()).append(" ,");
            }
            unselectedPartsString.delete(unselectedPartsString.length() - 2, unselectedPartsString.length());
            unselectedPartsString.append(" NOT selected");

            test.printTestResults(false, AERO_CONTROL_PARTS_SELECTED_TEST, unselectedPartsString.toString(), AERO_CONTROL_PARTS_SELECTED_EXPECTED);
        }

        // print result of aero control availability test
        if (aeroControl.getEnableControl())
            test.printTestResults(true, AERO_CONTROL_AVAILABILITY_TEST, AERO_CONTROL_AVAILABILITY_EXPECTED, AERO_CONTROL_AVAILABILITY_EXPECTED);
        else
            test.printTestResults(false, AERO_CONTROL_AVAILABILITY_TEST, "Aero control disabled", AERO_CONTROL_AVAILABILITY_EXPECTED);
    }

}
