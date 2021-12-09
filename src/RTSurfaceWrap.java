/*
 * TO-DO: surface wrapper 3D scene export
 */

import star.base.report.AreaAverageReport;
import star.base.report.SurfaceAreaAverageReport;
import star.common.*;
import star.meshing.MeshOperationManager;
import star.meshing.SurfaceCustomMeshControl;
import star.surfacewrapper.SurfaceWrapperAutoMeshOperation;

import java.util.ArrayList;
import java.util.Collection;

public class RTSurfaceWrap {

    RTTestComponent rt;

    public RTSurfaceWrap(RTTestComponent rt) {
        this.rt = rt;
        SurfaceWrapperAutoMeshOperation wrapper = ((SurfaceWrapperAutoMeshOperation) rt.sim.get(MeshOperationManager.class).getObject(rt.SURFACE_WRAPPER_NAME));

        // Surface wrapper PPM
        wrapperPPM(rt.sim);
        gapClosure(wrapper);
        partsSelected(wrapper);
        aeroControl(wrapper);
        aeroSurfaceWrap();
    }

    /**
     * Surface wrapper PPM check.
     * pass: no parts selected
     */
    private void wrapperPPM(Simulation sim) {

        String surfaceWrapperPPMName = "Surface wrapper PPM";
        String surfaceWrapperPPMTest = "Surface Wrapper PPM";
        String surfaceWrapperPPMExpected = "No part selected";

        SurfaceWrapperAutoMeshOperation wrapper = ((SurfaceWrapperAutoMeshOperation) sim.get(MeshOperationManager.class).getObject(surfaceWrapperPPMName));
        GeometryObjectGroup partsSelected = wrapper.getInputGeometryObjects();
        int numParts = partsSelected.getChildrenCount();
        if (numParts == 0)
            rt.printTestResults(true, surfaceWrapperPPMTest, surfaceWrapperPPMExpected, surfaceWrapperPPMExpected);
        else
            rt.printTestResults(false, surfaceWrapperPPMTest, numParts + " parts selected", surfaceWrapperPPMExpected);

    }

    /**
     * Check gap closure setting
     * pass: true
     */
    private void gapClosure(SurfaceWrapperAutoMeshOperation wrapper) {

        String gapClosureTest = "Gap Closure";
        String gapClosureExpected = "true";

        boolean gapClosureSetting = wrapper.getDoGapClosure();
        if (gapClosureSetting)
            rt.printTestResults(true, gapClosureTest, Boolean.toString(true), gapClosureExpected);
        else
            rt.printTestResults(false, gapClosureTest, Boolean.toString(false), gapClosureExpected);

    }

    /**
     * Check parts selected in surface wrapper
     * Pass: all CFD_ parts and tires are selected
     */
    private void partsSelected(SurfaceWrapperAutoMeshOperation wrapper) {

        String partsSelectedTest = "Parts Selected";
        String partsSelectedExpected = "All CFD_ and tires selected";

        // get selected parts
        Collection<GeometryPart> partsSelected = wrapper.getInputGeometryObjects().getLeafParts();

        // compare selected parts against CFD parts
        boolean testPassed = true;
        Collection<GeometryPart> unselectedParts = new ArrayList<>(); // parts that should be selected but are not selected
        for (GeometryPart parts:rt.cfdParts) {
            if (!partsSelected.contains(parts)) {
                testPassed = false;
                unselectedParts.add(parts);
            }
        }

        // print results
        if (testPassed)
            rt.printTestResults(true, partsSelectedTest, partsSelectedExpected, partsSelectedExpected);
        else {
            StringBuilder unselectedPartsString = new StringBuilder("Parts: ");
            for (GeometryPart part:unselectedParts) {
                unselectedPartsString.append(part.getPresentationName()).append(" ,");
            }
            unselectedPartsString.delete(unselectedPartsString.length() - 2, unselectedPartsString.length());
            unselectedPartsString.append(" NOT selected");

            rt.printTestResults(false, partsSelectedTest, unselectedPartsString.toString(), partsSelectedTest);
        }
    }

    /**
     * Check parts selected in aero control
     * Pass: all parts in CFD_Aerodynamics_830250079 selected
     */
    private void aeroControl(SurfaceWrapperAutoMeshOperation wrapper) {

        String aeroControlName = "Aero Control";
        String aeroControlPartsSelectedTest = "Aero Control Parts Selected";
        String aeroControlPartsSelectedExpected = "All CFD_Aerodynamics_830250079 parts selected";
        String aeroControlAvailabilityTest = "Aero Control Availability";
        String aeroControlAvailabilityExpected = "Aero control enabled";

        SurfaceCustomMeshControl aeroControl = ((SurfaceCustomMeshControl) wrapper.getCustomMeshControls().getObject(aeroControlName));

        // get parts selected
        Collection<GeometryPart> partsSelected = aeroControl.getGeometryObjects().getLeafParts();

        // compare selected parts against aero parts
        boolean testPassed = true;
        Collection<GeometryPart> unselectedParts = new ArrayList<>(); // parts that should be selected but are not selected
        for (GeometryPart parts:rt.aeroParts) {
            if (!partsSelected.contains(parts)) {
                testPassed = false;
                unselectedParts.add(parts);
            }
        }

        // print results of aero control parts selected test
        if (testPassed)
            rt.printTestResults(true, aeroControlPartsSelectedTest, aeroControlPartsSelectedExpected, aeroControlPartsSelectedExpected);
        else {
            StringBuilder unselectedPartsString = new StringBuilder("Parts: ");
            for (GeometryPart part:unselectedParts) {
                unselectedPartsString.append(part.getPresentationName()).append(" ,");
            }
            unselectedPartsString.delete(unselectedPartsString.length() - 2, unselectedPartsString.length());
            unselectedPartsString.append(" NOT selected");

            rt.printTestResults(false, aeroControlPartsSelectedTest, unselectedPartsString.toString(), aeroControlPartsSelectedExpected);
        }

        // print result of aero control availability test
        if (aeroControl.getEnableControl())
            rt.printTestResults(true, aeroControlAvailabilityTest, aeroControlAvailabilityExpected, aeroControlAvailabilityExpected);
        else
            rt.printTestResults(false, aeroControlAvailabilityTest, "Aero control disabled", aeroControlAvailabilityExpected);
    }

    /**
     * Check for the average surface cell area for surface wrapped aero parts
     */
    private void aeroSurfaceWrap() {

        // Get the report
        AreaAverageReport averageCellSizeReport = (AreaAverageReport) rt.sim.getReportManager().getReport("Average Surface Cell Area");

        // set representation
        averageCellSizeReport.setRepresentation(rt.latestSurface);


    }

}
