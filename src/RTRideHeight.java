/**
 * This class should be ran both before and after a RH change. Before the RH change, the class reads the centroid
 * location of CFD parts and tires individually, and the location and direction of radiator and fan coordinate
 * systems. After RH change, another set of data is read. The two sets of data are compared to get a delta. If the delta
 * matches the calculated delta, it passes the test.
 */
import star.base.report.ExpressionReport;
import star.base.report.SumReport;
import star.common.*;

import java.util.Collection;

public class RTRideHeight {

    private RTTestComponent rt;

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

    }

    /**
     * Set the input parts of centroid report to CFD_
     */
    private void setReportPartToCFD() {

        Collection<PartSurface> surfaces = rt.getAllSurfacesByPartGroup(rt.cfdParts);

        this.sumXPosArea.getParts().setObjects(surfaces);
        this.sumYPosArea.getParts().setObjects(surfaces);
        this.sumZPosArea.getParts().setObjects(surfaces);
        this.sumArea.getParts().setObjects(surfaces);

    }

    /**
     * Set the input parts of centroid report to tires
     */
    private void setReportPartToTire() {

        Collection<PartSurface> surfaces = rt.getAllSurfacesByPartGroup(rt.tireParts);

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

            double frontAngle = this.getRotationAngle(rt.frontWheelCylindrical, rrhOffset);
            double rearAngle = this.getRotationAngle(rt.rearWheelCylindrical, frhOffset);
            double[] frontDelta = this.getRotationDeltaAboutCS(rt.frontWheelCylindrical, originalLoc, frontAngle);
            double[] rearDelta = this.getRotationDeltaAboutCS(rt.rearWheelCylindrical, originalLoc, rearAngle);
            // superposition of front delta and rear delta to get the new delta
            for (int i = 0; i < 3; i++)
                newLoc[i] = originalLoc[i] - (frontDelta[i] + rearDelta[i]);

        }
        return newLoc;

    }

    /**
     * Get the angle the vehicle rotated through in radians
     * @param cs the coordinate system the car rotated about
     * @param rhOffset the ride height offset corresponding to this change
     * @return angle in radians
     */
    private double getRotationAngle(CylindricalCoordinateSystem cs, double rhOffset) {

        double angle = Math.atan(rhOffset / rt.WHEEL_BASE);
        return angle;

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

        // angle formed by the point's new location, coordinate system location, and the horizontal direction
        double pNewCSh = pOGCSh - rotation;

        // distance between the coordinate system location and the point parallel to vehicle center plane
        double r = Math.sqrt(Math.pow(csLoc[0] - originalLoc[0], 2) + Math.pow(csLoc[2] - originalLoc[2], 2));

        // the new location of the point
        double[] newLoc = new double[3];
        newLoc[0] = r * Math.cos(pNewCSh) + csLoc[0];
        newLoc[2] = r * Math.sin(pNewCSh) + csLoc[2];

        // get the delta
        double[] delta = new double[3];
        delta[0] = newLoc[0] - originalLoc[1];
        delta[1] = 0;
        delta[2] = newLoc[2] - originalLoc[2];

        return delta;

    }

    /**
     * Find the expected delta of coordinate system given an angle of rotation
     * @param originalCSDir the original direction of x-axis of cartesian coordinate system or the original direction
     *                      of r-axis of cylindrical coordinate system
     * @param rotation the angle the coordinate system rotated through in vehicle center plane in radians
     * @return the new x-axis or r-axis direction
     */
    private double[] newCSDirectionDelta(double[] originalCSDir, double rotation) {

        // find the original direction in terms of angle in the car center plane
        double originalDir = Math.atan(originalCSDir[1] / originalCSDir[0]);

        // new direction in terms of angle in the car's center plane
        double newDirAngle = originalDir + rotation;

        // find the delta
        double newX = Math.cos(newDirAngle);
        double newY = Math.sin(newDirAngle);
        double[] newDelta = new double[3];
        newDelta[0] = newX;
        newDelta[1] = newY;
        newDelta[2] = 0;

        return newDelta;


    }

}
