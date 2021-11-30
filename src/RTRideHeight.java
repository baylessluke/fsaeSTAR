/*
  This class should be ran both before and after a RH or roll change. Before the RH change, the class reads the centroid
  location of CFD parts and tires individually, and the location and direction of radiator and fan coordinate
  systems. After RH or roll change, another set of data is read. The two sets of data are compared to get a delta.
  If the delta matches the calculated delta, it passes the test.
 */
import star.base.report.ExpressionReport;
import star.base.report.SumReport;
import star.common.*;

import java.util.Collection;

public class RTRideHeight {

    private final RTTestComponent rt;

    // Names
    String CENTROID_X_REPORT_NAME = "Centroid X";
    String CENTROID_Y_REPORT_NAME = "Centroid Y";
    String CENTROID_Z_REPORT_NAME = "Centroid Z";
    String SUM_X_POS_AREA_NAME = "Sum of X_POS_AREA";
    String SUM_Y_POS_AREA_NAME = "Sum of Y_POS_AREA";
    String SUM_Z_POS_AREA_NAME = "Sum of Z_POS_AREA";
    String SUM_AREA_NAME = "Sum of Area";

    // reports
    ExpressionReport centroidXReport;
    ExpressionReport centroidYReport;
    ExpressionReport centroidZReport;
    SumReport sumXPosArea;
    SumReport sumYPosArea;
    SumReport sumZPosArea;
    SumReport sumArea;

    // numbers to compare, everything here is in inches
    double[] preChangeCFDCentroid = new double[3];
    double[] preChangeTireCentroid = new double[3];
    double[] preChangeRadCSLoc = new double[3];
    double[] preChangeDualRadCSLoc = new double[3];
    double[] preChangeRadCSDir = new double[3]; // x direction
    double[] preChangeDualRadCSDir = new double[3]; // x direction
    double[] preChangeFanCSLoc = new double[3];
    double[] preChangeDualFanCSLoc = new double[3];
    double[] preChangeFanCSDir = new double[3]; // r direction
    double[] preChangeDualFanCSDir = new double[3]; // r direction
    double[] postChangeCFDCentroid = new double[3];
    double[] postChangeTireCentroid = new double[3];
    double[] postChangeRadCSLoc = new double[3];
    double[] postChangeDualRadCSLoc = new double[3];
    double[] postChangeRadCSDir = new double[3]; // x direction
    double[] postChangeDualRadCSDir = new double[3]; // x direction
    double[] postChangeFanCSLoc = new double[3];
    double[] postChangeDualFanCSLoc = new double[3];
    double[] postChangeFanCSDir = new double[3]; // r direction
    double[] postChangeDualFanCSDir = new double[3]; // r direction

    // Initialization

    public RTRideHeight(RTTestComponent rt) {

        // initialization
        this.rt = rt;
        this.initReports();

    }

    /**
     * Initializing reports
     */
    private void initReports() {

        this.centroidXReport = (ExpressionReport) rt.sim.getReportManager().getReport(CENTROID_X_REPORT_NAME);
        this.centroidYReport = (ExpressionReport) rt.sim.getReportManager().getReport(CENTROID_Y_REPORT_NAME);
        this.centroidZReport = (ExpressionReport) rt.sim.getReportManager().getReport(CENTROID_Z_REPORT_NAME);
        this.sumXPosArea = (SumReport) rt.sim.getReportManager().getReport(SUM_X_POS_AREA_NAME);
        this.sumYPosArea = (SumReport) rt.sim.getReportManager().getReport(SUM_Y_POS_AREA_NAME);
        this.sumZPosArea = (SumReport) rt.sim.getReportManager().getReport(SUM_Z_POS_AREA_NAME);
        this.sumArea = (SumReport) rt.sim.getReportManager().getReport(SUM_AREA_NAME);

    }

    // Execution

    public void debug(double rollAngle) {

        for (int i = 0; i < 3; i++) {
            rt.sim.println("Pre Change CFD Centroid [" + i + "]: " + preChangeCFDCentroid[i]);
        }

        double[] newLoc = this.newLocation(Math.toRadians(rollAngle), preChangeCFDCentroid);

        for (int i = 0; i < 3; i++) {
            rt.sim.println("Post Change CFD Centroid [" + i + "]: " + preChangeCFDCentroid[i]);
        }

    }

    /**
     * Get the numbers before RH changes
     */
    public void preChange() {

        setReportPartToCFD();
        preChangeCFDCentroid = this.getCentroid();
        setReportPartToTire();
        preChangeTireCentroid = this.getCentroid();
        preChangeRadCSLoc = RTTestComponent.getCSLocation(rt.radCartesian);
        preChangeDualRadCSLoc = RTTestComponent.getCSLocation(rt.dualRadCartesian);
        preChangeFanCSLoc = RTTestComponent.getCSLocation(rt.fanCylindrical);
        preChangeDualFanCSLoc = RTTestComponent.getCSLocation(rt.dualFanCylindrical);
        preChangeRadCSDir = RTTestComponent.getCSDirection(rt.radCartesian, true);
        preChangeDualRadCSDir = RTTestComponent.getCSDirection(rt.dualRadCartesian, true);
        preChangeFanCSDir = RTTestComponent.getCSDirection(rt.fanCylindrical, false);
        preChangeDualFanCSDir = RTTestComponent.getCSDirection(rt.dualFanCylindrical, false);

    }

    /**
     * This method should be called after the RH has been changed
     * @param frhOffset Front RH offset from standard RH. Note: this is not a delta as it is in linuxConfig
     * @param rrhOffset Rear RH offset from standard RH. Note: this is not a delta as it is in linuxConfig
     */
    public void postChange(double frhOffset, double rrhOffset) {

        // find what the new locations are supposed to
        double[] expNewCFDCentroid = this.newLocation(frhOffset, rrhOffset, preChangeCFDCentroid);
        double[] expNewTireCentroid = this.preChangeTireCentroid;
        double[] expNewRadCSLoc = this.newLocation(frhOffset, rrhOffset, preChangeRadCSLoc);
        double[] expNewDualRadCSLoc = this.newLocation(frhOffset, rrhOffset, preChangeDualRadCSLoc);
        double[] expNewFanLoc = this.newLocation(frhOffset, rrhOffset, preChangeFanCSLoc);
        double[] expNewDualFanLoc = this.newLocation(frhOffset, rrhOffset, preChangeDualFanCSLoc);

        // find the new direction of coordinate system axes
        double[] expNewRadCSDir = this.newCSDirection(preChangeRadCSDir, frhOffset, rrhOffset);
        double[] expNewDualRadCSDir = this.newCSDirection(preChangeDualRadCSDir, frhOffset, rrhOffset);
        double[] expNewFanCSDir = this.newCSDirection(preChangeFanCSDir, frhOffset, rrhOffset);
        double[] expNewDualFanCSDir = this.newCSDirection(preChangeDualFanCSDir, frhOffset, rrhOffset);

        // get the actual new locations
        setReportPartToCFD();
        postChangeCFDCentroid = this.getCentroid();
        setReportPartToTire();
        postChangeTireCentroid = this.getCentroid();
        postChangeRadCSLoc = RTTestComponent.getCSLocation(rt.radCartesian);
        postChangeDualRadCSLoc = RTTestComponent.getCSLocation(rt.dualRadCartesian);
        postChangeFanCSLoc = RTTestComponent.getCSLocation(rt.fanCylindrical);
        postChangeDualFanCSLoc = RTTestComponent.getCSLocation(rt.dualFanCylindrical);
        postChangeRadCSDir = RTTestComponent.getCSDirection(rt.radCartesian, true);
        postChangeDualRadCSDir = RTTestComponent.getCSDirection(rt.dualRadCartesian, true);
        postChangeFanCSDir = RTTestComponent.getCSDirection(rt.fanCylindrical, false);
        postChangeDualFanCSDir = RTTestComponent.getCSDirection(rt.dualFanCylindrical, false);

        // print the results
        printResults(expNewCFDCentroid, expNewTireCentroid, expNewRadCSLoc, expNewDualRadCSLoc, expNewFanLoc, expNewDualFanLoc, expNewRadCSDir, expNewDualRadCSDir, expNewFanCSDir, expNewDualFanCSDir, true);

    }

    /**
     * Overloading this method to make it work for roll set
     * @param rollAngle roll angle in DEGREES
     */
    public void postChange(double rollAngle) {

        double roll = Math.toRadians(rollAngle); // roll angle in radians

        // find what the new locations are supposed to
        double[] expNewCFDCentroid = this.newLocation(roll, preChangeCFDCentroid);
        double[] expNewTireCentroid = this.preChangeTireCentroid;
        double[] expNewRadCSLoc = this.newLocation(roll, preChangeRadCSLoc);
        double[] expNewDualRadCSLoc = this.newLocation(roll, preChangeDualRadCSLoc);
        double[] expNewFanLoc = this.newLocation(roll, preChangeFanCSLoc);
        double[] expNewDualFanLoc = this.newLocation(roll, preChangeDualFanCSLoc);

        // find the new direction of coordinate system axes
        double[] expNewRadCSDir = this.newCSDirection(preChangeRadCSDir, roll);
        double[] expNewDualRadCSDir = this.newCSDirection(preChangeDualRadCSDir, roll);
        double[] expNewFanCSDir = this.newCSDirection(preChangeFanCSDir, roll);
        double[] expNewDualFanCSDir = this.newCSDirection(preChangeDualFanCSDir, roll);

        // get the actual new locations
        setReportPartToCFD();
        postChangeCFDCentroid = this.getCentroid();
        setReportPartToTire();
        postChangeTireCentroid = this.getCentroid();
        postChangeRadCSLoc = RTTestComponent.getCSLocation(rt.radCartesian);
        postChangeDualRadCSLoc = RTTestComponent.getCSLocation(rt.dualRadCartesian);
        postChangeFanCSLoc = RTTestComponent.getCSLocation(rt.fanCylindrical);
        postChangeDualFanCSLoc = RTTestComponent.getCSLocation(rt.dualFanCylindrical);
        postChangeRadCSDir = RTTestComponent.getCSDirection(rt.radCartesian, true);
        postChangeDualRadCSDir = RTTestComponent.getCSDirection(rt.dualRadCartesian, true);
        postChangeFanCSDir = RTTestComponent.getCSDirection(rt.fanCylindrical, false);
        postChangeDualFanCSDir = RTTestComponent.getCSDirection(rt.dualFanCylindrical, false);

        // print result
        printResults(expNewCFDCentroid, expNewTireCentroid, expNewRadCSLoc, expNewDualRadCSLoc, expNewFanLoc, expNewDualFanLoc, expNewRadCSDir, expNewDualRadCSDir, expNewFanCSDir, expNewDualFanCSDir, false);

    }

    /**
     * Set the input parts of centroid report to CFD_
     */
    private void setReportPartToCFD() {

        Collection<PartSurface> surfaces = RTTestComponent.getAllSurfacesByPartGroup(rt.cfdParts);

        this.sumXPosArea.getParts().setObjects(surfaces);
        this.sumYPosArea.getParts().setObjects(surfaces);
        this.sumZPosArea.getParts().setObjects(surfaces);
        this.sumArea.getParts().setObjects(surfaces);

    }

    /**
     * Set the input parts of centroid report to tires
     */
    private void setReportPartToTire() {

        Collection<PartSurface> surfaces = RTTestComponent.getAllSurfacesByPartGroup(rt.tireParts);

        this.sumXPosArea.getParts().setObjects(surfaces);
        this.sumYPosArea.getParts().setObjects(surfaces);
        this.sumZPosArea.getParts().setObjects(surfaces);
        this.sumArea.getParts().setObjects(surfaces);

    }

    /**
     * Get the centroid value in inches by simply reading the reports. This method does NOT do any setup. It's dumb, it
     * just reads the reports
     * @return x, y, z in inches
     */
    private double[] getCentroid() {

        double[] centroid = new double[3];
        centroid[0] = this.centroidXReport.getValue();
        centroid[1] = this.centroidYReport.getValue();
        centroid[2] = this.centroidZReport.getValue();
        return centroid;

    }

    /**
     * Calculate the new location of centroids or coordinate system origin after RH change
     */
    private double[] newLocation(double frhOffset, double rrhOffset, double[] originalLoc) {

        double[] newLoc = new double[3];
        if (frhOffset == rrhOffset) {
            newLoc[0] = originalLoc[0];
            newLoc[1] = originalLoc[1];
            newLoc[2] = originalLoc[2] - frhOffset;
        } else {

            double frontAngle = this.getRotationAngle(rrhOffset);
            double rearAngle = -this.getRotationAngle(frhOffset);
            double[] frontDelta = this.getRotationDeltaAboutCS(rt.frontWheelCylindrical, originalLoc, frontAngle);
            double[] rearDelta = this.getRotationDeltaAboutCS(rt.rearWheelCylindrical, originalLoc, rearAngle);
            // superposition of front delta and rear delta to get the new delta
            for (int i = 0; i < 3; i++)
                newLoc[i] = originalLoc[i] + (frontDelta[i] + rearDelta[i]);

        }
        return newLoc;

    }

    /**
     * Overloading the new location method for roll checker
     * @param roll roll angle in radians
     * @param originalLoc the location of the part before the roll change
     * @return new location in inches
     */
    private double[] newLocation(double roll, double[] originalLoc) {

        double[] newLoc = new double[3];
        if (roll == 0) {
            newLoc[0] = originalLoc[0];
            newLoc[1] = originalLoc[1];
            newLoc[2] = originalLoc[2];
        } else {
            newLoc = this.getRotationDeltaAboutCS(rt.rollCartesian, originalLoc, roll);
        }
        return newLoc;
    }

    /**
     * Get the angle the vehicle rotated through in radians
     * @param rhOffset the ride height offset corresponding to this change
     * @return angle in radians
     */
    private double getRotationAngle(double rhOffset) {

        return Math.atan(rhOffset / rt.WHEEL_BASE);

    }

    /**
     * Get the delta of x and z position of a point due to rotation about a specific axis
     * @param cs cylindrical coordinate system the axis is located at
     * @param originalLoc The original location of the point
     * @param rotation the angle that the point rotated through in radians
     * @return the delta in x and z direction
     */
    private double[] getRotationDeltaAboutCS(CylindricalCoordinateSystem cs, double[] originalLoc, double rotation) {

        // get the location of the coordinate system
        double[] csLoc = RTTestComponent.getCSLocation(cs);

        // angle formed by the point's original location, coordinate system location, and the horizontal direction
        double opposite = originalLoc[2] - csLoc[2];
        double adjacent = originalLoc[0] - csLoc[0];
        double pOGCSh = Math.atan(opposite / adjacent);
        if (pOGCSh < 0)
            pOGCSh += Math.PI;

        // angle formed by the point's new location, coordinate system location, and the horizontal direction
        double pNewCSh = pOGCSh + rotation;

        // distance between the coordinate system location and the point parallel to vehicle center plane
        double r = Math.sqrt(Math.pow(csLoc[0] - originalLoc[0], 2) + Math.pow(csLoc[2] - originalLoc[2], 2));

        // the new location of the point
        double[] newLoc = new double[3];
        newLoc[0] = csLoc[0] + r * Math.cos(pNewCSh);
        newLoc[2] = csLoc[2] + r * Math.sin(pNewCSh);

        // get the delta
        double[] delta = new double[3];
        delta[0] = newLoc[0] - originalLoc[0];
        delta[1] = 0;
        delta[2] = newLoc[2] - originalLoc[2];

        return delta;

    }

    /**
     * Overloading this method to work with roll test
     * @param cs roll axis coordinate system
     * @param originalLoc location before roll change
     * @param rotation the angle that the car rolled in radians
     * @return the delta in y and z directions
     */
    public double[] getRotationDeltaAboutCS(CartesianCoordinateSystem cs, double[] originalLoc, double rotation) {

        // get the location of the coordinate system
        double[] csLoc = RTTestComponent.getCSLocation(cs);

        // angle formed by the point's original location, coordinate system location, and the horizontal direction
        double opposite = originalLoc[2] - csLoc[2];
        double adjacent = originalLoc[1] - csLoc[1];
        double pOGCSh = Math.atan(opposite / adjacent);
        if (pOGCSh < 0)
            pOGCSh += Math.PI;
        rt.sim.println("pOGCSh: " + pOGCSh);

        // angle formed by the point's new location, coordinate system location, and the horizontal direction
        double pNewCSh = pOGCSh + rotation;
        rt.sim.println("pNewCSh: " + pNewCSh);

        // distance between the coordinate system location and the point parallel to vehicle center plane
        double r = Math.sqrt(Math.pow(csLoc[1] - originalLoc[1], 2) + Math.pow(csLoc[2] - originalLoc[2], 2));
        rt.sim.println("r: " + r);

        // the new location of the point
        double[] newLoc = new double[3];
        newLoc[1] = csLoc[1] + r * Math.cos(pNewCSh);
        newLoc[2] = csLoc[2] + r * Math.sin(pNewCSh);

        // get the delta
        double[] delta = new double[3];
        delta[0] = originalLoc[0];
        delta[1] = newLoc[1] - originalLoc[1];
        delta[2] = newLoc[2] - originalLoc[2];

        return delta;

    }

    /**
     * Find the new direction of the coordinate system after RH change
     * @param originalCSDir coordinate system
     * @param frh Front RH delta
     * @param rrh Rear RH delta
     * @return new direction of the coordinate system
     */
    private double[] newCSDirection(double[] originalCSDir, double frh, double rrh) {

        // rotations angles due to RH change
        double frontRHRotation = this.getRotationAngle(rrh);
        double rearRHRotation = -this.getRotationAngle(frh);

        // changes to the coordinate system direction
        double originalDirAngle = Math.atan(originalCSDir[2] / originalCSDir[0]);
        double newDirAngle = originalDirAngle + frontRHRotation + rearRHRotation;

        // find the vector in the car center plane
        double[] newDir = new double[3];
        newDir[0] = Math.cos(newDirAngle);
        newDir[1] = originalCSDir[1];
        newDir[2] = Math.sin(newDirAngle);

        return newDir;

    }

    /**
     * Overloading this method to work with roll test
     * @param originalCSDir the original direction of the coordiante systems
     * @param roll the roll angle in radians
     * @return new coordinate system direction
     */
    private double[] newCSDirection(double[] originalCSDir, double roll) {

        // changes to the coordinate system direction
        double originalDirAngle = Math.atan(originalCSDir[2] / originalCSDir[1]);
        double newDirAngle = originalDirAngle + roll;

        // find the vector in the car center plane
        double[] newDir = new double[3];
        newDir[0] = originalCSDir[0];
        newDir[1] = Math.cos(newDirAngle);
        newDir[2] = Math.sin(newDirAngle);

        return newDir;

    }

    private void printResults(double[] expNewCFDCentroid, double[] expNewTireCentroid, double[] expNewRadCSLoc,
                                  double[] expNewDualRadCSLoc, double[] expNewFanLoc, double[] expNewDualFanLoc,
                                  double[] expNewRadCSDir, double[] expNewDualRadCSDir, double[] expNewFanCSDir,
                                  double[] expNewDualFanCSDir, boolean isRH) {

        String testNamePrefix;
        if (isRH)
            testNamePrefix = "RH - ";
        else
            testNamePrefix = "Roll - ";

        try {
            rt.printTestResults(
                    RTTestComponent.numericalCompare(postChangeCFDCentroid, expNewCFDCentroid, 0.01),
                    testNamePrefix + "CFD Part Centroid",
                    RTTestComponent.buildResultStringFromArray(postChangeCFDCentroid, "in"),
                    RTTestComponent.buildResultStringFromArray(expNewCFDCentroid, " in")
            );
            rt.printTestResults(
                    RTTestComponent.numericalCompare(postChangeTireCentroid, expNewTireCentroid, 0.01),
                    testNamePrefix + "Tire Part Centroid",
                    RTTestComponent.buildResultStringFromArray(postChangeTireCentroid, "in"),
                    RTTestComponent.buildResultStringFromArray(expNewTireCentroid, " in")
            );
            rt.printTestResults(
                    RTTestComponent.numericalCompare(postChangeRadCSLoc, expNewRadCSLoc, 0.01),
                    testNamePrefix + "Radiator Cartesian Origin",
                    RTTestComponent.buildResultStringFromArray(postChangeRadCSLoc, "in"),
                    RTTestComponent.buildResultStringFromArray(expNewRadCSLoc, " in")
            );
            rt.printTestResults(
                    RTTestComponent.numericalCompare(postChangeDualRadCSLoc, expNewDualRadCSLoc, 0.01),
                    testNamePrefix + "Dual Radiator Cartesian Origin",
                    RTTestComponent.buildResultStringFromArray(postChangeDualRadCSLoc, "in"),
                    RTTestComponent.buildResultStringFromArray(expNewDualRadCSLoc, " in")
            );
            rt.printTestResults(
                    RTTestComponent.numericalCompare(postChangeFanCSLoc, expNewFanLoc, 0.01),
                    testNamePrefix + "Fan Cylindrical Origin",
                    RTTestComponent.buildResultStringFromArray(postChangeFanCSLoc, "in"),
                    RTTestComponent.buildResultStringFromArray(expNewFanLoc, " in")
            );
            rt.printTestResults(
                    RTTestComponent.numericalCompare(postChangeDualFanCSLoc, expNewDualFanLoc, 0.01),
                    testNamePrefix + "Dual Fan Cylindrical Origin",
                    RTTestComponent.buildResultStringFromArray(postChangeDualFanCSLoc, "in"),
                    RTTestComponent.buildResultStringFromArray(expNewDualFanLoc, " in")
            );
            rt.printTestResults(
                    RTTestComponent.numericalCompare(postChangeRadCSDir, expNewRadCSDir, 0.01),
                    testNamePrefix + "Radiator Cartesian Direction",
                    RTTestComponent.buildResultStringFromArray("Y-Axis:", postChangeRadCSDir),
                    RTTestComponent.buildResultStringFromArray("Y-Axis:", expNewRadCSDir)
            );
            rt.printTestResults(
                    RTTestComponent.numericalCompare(postChangeDualRadCSDir, expNewDualRadCSDir, 0.01),
                    testNamePrefix + "Dual Radiator Cartesian Direction",
                    RTTestComponent.buildResultStringFromArray("Y-Axis:", postChangeDualRadCSDir),
                    RTTestComponent.buildResultStringFromArray("Y-Axis:", expNewDualRadCSDir)
            );
            rt.printTestResults(
                    RTTestComponent.numericalCompare(postChangeFanCSDir, expNewFanCSDir, 0.01),
                    testNamePrefix + "Fan Cylindrical Direction",
                    RTTestComponent.buildResultStringFromArray("Z-Axis:", postChangeFanCSDir),
                    RTTestComponent.buildResultStringFromArray("Z-Axis:", expNewFanCSDir)
            );
            rt.printTestResults(
                    RTTestComponent.numericalCompare(postChangeDualFanCSDir, expNewDualFanCSDir, 0.01),
                    testNamePrefix + "Dual Fan Cylindrical Direction",
                    RTTestComponent.buildResultStringFromArray("Z-Axis:", postChangeDualFanCSDir),
                    RTTestComponent.buildResultStringFromArray("Z-Axis:", expNewDualFanCSDir)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* testing

    public void testRotation(double frh, double rrh) {

        double frontRot = Math.atan(rrh / rt.WHEEL_BASE);
        double rearRot = -Math.atan(frh / rt.WHEEL_BASE);

        if (frontRot != 0) {
            rotateParts(rt.cfdParts, rt.frontWheelCylindrical, frontRot);
            rotateCoord(rt.radCartesian, rt.frontWheelCylindrical, frontRot);
            rotateCoord(rt.fanCylindrical, rt.frontWheelCylindrical, frontRot);
            rotateCoord(rt.dualRadCartesian, rt.frontWheelCylindrical, frontRot);
            rotateCoord(rt.dualFanCylindrical, rt.frontWheelCylindrical, frontRot);
        }

        if (rearRot != 0) {
            rotateParts(rt.cfdParts, rt.rearWheelCylindrical, rearRot);
            rotateCoord(rt.radCartesian, rt.rearWheelCylindrical, rearRot);
            rotateCoord(rt.fanCylindrical, rt.rearWheelCylindrical, rearRot);
            rotateCoord(rt.dualRadCartesian, rt.rearWheelCylindrical, rearRot);
            rotateCoord(rt.dualFanCylindrical, rt.rearWheelCylindrical, rearRot);
        }
    }

    private void rotateParts(Collection<GeometryPart> parts, CylindricalCoordinateSystem rotationPoint, double rotationAngle)
    {
        rt.sim.get(SimulationPartManager.class).rotateParts(parts, new DoubleVector(new double[] {0, 0, 1}), Arrays.asList(rt.unitless, rt.unitless, rt.unitless), rotationAngle, rotationPoint);
    }

    private void rotateCoord(CoordinateSystem coord, CylindricalCoordinateSystem rotationPoint, double rotationAngle)
    {
        coord.getLocalCoordinateSystemManager().rotateLocalCoordinateSystems(Collections.singletonList(coord), new DoubleVector(new double[] {0, 0, 1}), new NeoObjectVector(new Units[]{rt.unitless, rt.unitless, rt.unitless}), rotationAngle, rotationPoint);
    }
    */
}
