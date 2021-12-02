import star.common.*;
import star.flow.WallSlidingOption;

import java.util.Locale;

public class RTRegions {

    RTTestComponent rt;

    // names
    private final String FS_GROUND_NAME = "Freestream.Ground";
    private final String FS_LEFT_NAME = "Freestream.Left";
    private final String FS_SYM_NAME = "Freestream.Symmetry";
    private final String FS_INLET_NAME = "Freestream.Inlet";
    private final String FS_OUTLET_NAME = "Freestream.Outlet";
    private final String FS_TOP_NAME = "Freestream.Top";
    private final String SUBTRACT_REGION_NAME = "Subtract";
    private final String DOMAIN_DUAL_FAN_DEFAULT_NAME = "CFD_DUAL_FAN.Default";
    private final String DOMAIN_DUAL_FAN_INLET_NAME = "CFD_DUAL_FAN.Fan Inlet";
    private final String DOMAIN_DUAL_FAN_OUTLET_NAME = "CFD_DUAL_FAN.Fan Outlet";
    private final String DOMAIN_DUAL_RAD_DEFAULT_NAME = "CFD_DUAL_RADIATOR.Default";
    private final String DOMAIN_DUAL_RAD_INLET_NAME = "CFD_DUAL_RADIATOR.Inlet";
    private final String DOMAIN_DUAL_RAD_OUTLET_NAME = "CFD_DUAL_RADIATOR.Outlet";
    private final String DOMAIN_FAN_DEFAULT_NAME = "CFD_FAN.Default";
    private final String DOMAIN_FAN_INLET_NAME = "CFD_FAN.Fan Inlet";
    private final String DOMAIN_FAN_OUTLET_NAME = "CFD_FAN.Fan Outlet";
    private final String DOMAIN_RAD_DEFAULT_NAME = "CFD_RADIATOR.Default";
    private final String DOMAIN_RAD_INLET_NAME = "CFD_RADIATOR.Inlet";
    private final String DOMAIN_RAD_OUTLET_NAME = "CFD_RADIATOR.Outlet";

    // Regions
    private Region subtractRegion;

    // Boundaries
    private Boundary groundBdry;
    private Boundary leftBdry;
    private Boundary symBdry;
    private Boundary inletBdry;
    private Boundary outletBdry;
    private Boundary topBdry;
    private Boundary domainDualFanDefaultBdry;
    private Boundary domainDualFanInletBdry;
    private Boundary domainDualFanOutletBdry;
    private Boundary domainDualRadDefault;
    private Boundary domainDualRadInlet;
    private Boundary domainDualRadOutlet;
    private Boundary domainFanDefault;
    private Boundary domainFanInlet;
    private Boundary domainFanOutlet;
    private Boundary domainRadDefault;
    private Boundary domainRadInlet;
    private Boundary domainRadOutlet;

    // Boundary types
    SymmetryBoundary symBdryType;
    InletBoundary velInletBdryType;
    PressureBoundary presOutletBdryType;
    WallBoundary wallBdryType;

    public RTRegions(RTTestComponent rt) {
        this.rt = rt;
        this.initStarObjects();

        // perform tests
        // this.domainBoundaryTypeTest();
        this.checkGroundSliding();
    }

    /**
     * Initialize star objects
     */
    private void initStarObjects() {

        // regions
        this.subtractRegion = rt.sim.getRegionManager().getRegion(SUBTRACT_REGION_NAME);

        // Boundaries
        this.groundBdry = subtractRegion.getBoundaryManager().getBoundary(FS_GROUND_NAME);
        this.leftBdry = subtractRegion.getBoundaryManager().getBoundary(FS_LEFT_NAME);
        this.symBdry = subtractRegion.getBoundaryManager().getBoundary(FS_SYM_NAME);
        this.inletBdry = subtractRegion.getBoundaryManager().getBoundary(FS_INLET_NAME);
        this.outletBdry = subtractRegion.getBoundaryManager().getBoundary(FS_OUTLET_NAME);
        this.topBdry = subtractRegion.getBoundaryManager().getBoundary(FS_TOP_NAME);
        this.domainDualFanDefaultBdry = subtractRegion.getBoundaryManager().getBoundary(DOMAIN_DUAL_FAN_DEFAULT_NAME);
        this.domainDualFanInletBdry = subtractRegion.getBoundaryManager().getBoundary(DOMAIN_DUAL_FAN_INLET_NAME);
        this.domainDualFanOutletBdry = subtractRegion.getBoundaryManager().getBoundary(DOMAIN_DUAL_FAN_OUTLET_NAME);
        this.domainDualRadDefault = subtractRegion.getBoundaryManager().getBoundary(DOMAIN_DUAL_RAD_DEFAULT_NAME);
        this.domainDualRadInlet = subtractRegion.getBoundaryManager().getBoundary(DOMAIN_DUAL_RAD_INLET_NAME);
        this.domainDualRadOutlet = subtractRegion.getBoundaryManager().getBoundary(DOMAIN_DUAL_RAD_OUTLET_NAME);
        this.domainFanDefault = subtractRegion.getBoundaryManager().getBoundary(DOMAIN_FAN_DEFAULT_NAME);
        this.domainFanInlet = subtractRegion.getBoundaryManager().getBoundary(DOMAIN_FAN_INLET_NAME);
        this.domainFanOutlet = subtractRegion.getBoundaryManager().getBoundary(DOMAIN_FAN_OUTLET_NAME);
        this.domainRadDefault = subtractRegion.getBoundaryManager().getBoundary(DOMAIN_RAD_DEFAULT_NAME);
        this.domainRadInlet = subtractRegion.getBoundaryManager().getBoundary(DOMAIN_RAD_INLET_NAME);
        this.domainRadOutlet = subtractRegion.getBoundaryManager().getBoundary(DOMAIN_RAD_OUTLET_NAME);

        // Boundary types
        this.symBdryType = rt.sim.get(ConditionTypeManager.class).get(SymmetryBoundary.class);
        this.velInletBdryType = rt.sim.get(ConditionTypeManager.class).get(InletBoundary.class);
        this.presOutletBdryType = rt.sim.get(ConditionTypeManager.class).get(PressureBoundary.class);
        this.wallBdryType = rt.sim.get(ConditionTypeManager.class).get(WallBoundary.class);

    }

    // tests

    /**
     * Check for the boundary type in subtract.
     * Pass criteria:   Ground: wall
     *                  Inlet: velocity inlet
     *                  Outlet: pressure outlet
     *                  left: symmetry
     *                  symmetry: symmetry
     *                  top: symmetry
     *                  All rad and fan: wall
     */
    private void domainBoundaryTypeTest() {

        boolean ground = groundBdry.getBoundaryType().equals(wallBdryType);
        rt.printTestResults(ground, "FS.Ground Boundary Type", groundBdry.getBoundaryType().getPresentationName(), wallBdryType.getPresentationName());

        boolean inlet = inletBdry.getBoundaryType().equals(velInletBdryType);
        rt.printTestResults(inlet, "FS.Inlet Boundary Type", inletBdry.getBoundaryType().getPresentationName(), velInletBdryType.getPresentationName());

        boolean outlet = outletBdry.getBoundaryType().equals(presOutletBdryType);
        rt.printTestResults(outlet, "FS.Outlet Boundary Type", outletBdry.getBoundaryType().getPresentationName(), presOutletBdryType.getPresentationName());

        boolean left = leftBdry.getBoundaryType().equals(symBdryType);
        rt.printTestResults(left, "FS.Left Boundary Type", leftBdry.getBoundaryType().getPresentationName(), symBdryType.getPresentationName());

        boolean sym = symBdry.getBoundaryType().equals(symBdryType);
        rt.printTestResults(sym, "FS.Symmetry Boundary Type", symBdry.getBoundaryType().getPresentationName(), symBdryType.getPresentationName());

        boolean top = topBdry.getBoundaryType().equals(symBdryType);
        rt.printTestResults(top, "FS.Top Boundary Type", topBdry.getBoundaryType().getPresentationName(), symBdryType.getPresentationName());

        boolean radDefault = domainRadDefault.getBoundaryType().equals(wallBdryType);
        rt.printTestResults(radDefault, "FS.Dual Fan Default Boundary Type", domainRadDefault.getBoundaryType().getPresentationName(), wallBdryType.getPresentationName());

        boolean radInlet = domainRadInlet.getBoundaryType().equals(wallBdryType);
        rt.printTestResults(radInlet, "FS.Dual Fan Default Boundary Type", domainRadInlet.getBoundaryType().getPresentationName(), wallBdryType.getPresentationName());

        boolean radOutlet = domainRadOutlet.getBoundaryType().equals(wallBdryType);
        rt.printTestResults(radOutlet, "FS.Dual Fan Default Boundary Type", domainRadOutlet.getBoundaryType().getPresentationName(), wallBdryType.getPresentationName());

        boolean dualRadDefault = domainDualRadDefault.getBoundaryType().equals(wallBdryType);
        rt.printTestResults(dualRadDefault, "FS.Dual Fan Default Boundary Type", domainDualRadDefault.getBoundaryType().getPresentationName(), wallBdryType.getPresentationName());

        boolean dualRadInlet = domainDualRadInlet.getBoundaryType().equals(wallBdryType);
        rt.printTestResults(dualRadInlet, "FS.Dual Fan Default Boundary Type", domainDualRadInlet.getBoundaryType().getPresentationName(), wallBdryType.getPresentationName());

        boolean dualRadOutlet = domainDualRadOutlet.getBoundaryType().equals(wallBdryType);
        rt.printTestResults(dualRadOutlet, "FS.Dual Fan Default Boundary Type", domainDualRadOutlet.getBoundaryType().getPresentationName(), wallBdryType.getPresentationName());

        boolean fanDefault = domainFanDefault.getBoundaryType().equals(wallBdryType);
        rt.printTestResults(fanDefault, "FS.Dual Fan Default Boundary Type", domainFanDefault.getBoundaryType().getPresentationName(), wallBdryType.getPresentationName());

        boolean fanInlet = domainFanInlet.getBoundaryType().equals(wallBdryType);
        rt.printTestResults(fanInlet, "FS.Dual Fan Default Boundary Type", domainFanInlet.getBoundaryType().getPresentationName(), wallBdryType.getPresentationName());

        boolean fanOutlet = domainFanOutlet.getBoundaryType().equals(wallBdryType);
        rt.printTestResults(fanOutlet, "FS.Dual Fan Default Boundary Type", domainFanOutlet.getBoundaryType().getPresentationName(), wallBdryType.getPresentationName());

        boolean dualFanDefault = domainDualFanDefaultBdry.getBoundaryType().equals(wallBdryType);
        rt.printTestResults(dualFanDefault, "FS.Dual Fan Default Boundary Type", domainDualFanDefaultBdry.getBoundaryType().getPresentationName(), wallBdryType.getPresentationName());

        boolean dualFanInlet = domainDualFanInletBdry.getBoundaryType().equals(wallBdryType);
        rt.printTestResults(dualFanInlet, "FS.Dual Fan Default Boundary Type", domainDualFanInletBdry.getBoundaryType().getPresentationName(), wallBdryType.getPresentationName());

        boolean dualFanOutlet = domainDualFanOutletBdry.getBoundaryType().equals(wallBdryType);
        rt.printTestResults(dualFanOutlet, "FS.Dual Fan Default Boundary Type", domainDualFanOutletBdry.getBoundaryType().getPresentationName(), wallBdryType.getPresentationName());

    }

    /**
     * Check the velocity specification and ground speed
     */
    private void checkGroundSliding() {

        boolean tangentialVelSpec = groundBdry.getConditions().get(WallSlidingOption.class).getSelectedInput().getSelected().equals(WallSlidingOption.Type.VECTOR);
        rt.printTestResults(tangentialVelSpec, "Ground - Tangential Velocity Spec", groundBdry.getConditions().get(WallSlidingOption.class).getSelectedInput().getSelected().toString(), WallSlidingOption.Type.VECTOR.name());

    }

}
