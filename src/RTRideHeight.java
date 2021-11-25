import star.base.report.ExpressionReport;

/**
 * Ride height testing. Generate the centroid x, y, z report values of all CFD parts and tires at (0,0) ride height.
 * Lower the ride height to (-1, -1), generate the centroid x, y, z report values of CFD parts and tire. x and y values
 * of CFD parts should not change, z value should be 1 in lower. No report values of tires should change
 * Raise the ride height to (-1, 0) and generate centroid x, y, and z report values of CFD parts tire. y values should
 * not change, x should move forward a little, and z should be increased accordingly. Tire centroid should not change.
 */
public class RTRideHeight {

    private RTTestComponent rt;

    // Names
    String CENTROID_X_REPORT_NAME = "Centroid X";
    String CENTROID_Y_REPORT_NAME = "Centroid Y";
    String CENTROID_Z_REPORT_NAME = "Centroid Z";

    // reports
    ExpressionReport centroidXReport;
    ExpressionReport centroidYReport;
    ExpressionReport centroidZReport;

    public RTRideHeight(RTTestComponent rt) {
        this.rt = rt;

        // initialization
        this.centroidXReport = (ExpressionReport) rt.sim.getReportManager().getReport(CENTROID_X_REPORT_NAME);
        this.centroidYReport = (ExpressionReport) rt.sim.getReportManager().getReport(CENTROID_Y_REPORT_NAME);
        this.centroidZReport = (ExpressionReport) rt.sim.getReportManager().getReport(CENTROID_Z_REPORT_NAME);

        // execution
        this.setReportPartToTire();
    }

    /**
     * Set the input parts of centroid report to CFD_
     */
    private void setReportPartToCFD() {

        this.centroidXReport.getParts().setObjects(rt.cfdParts);
        this.centroidYReport.getParts().setObjects(rt.cfdParts);
        this.centroidZReport.getParts().setObjects(rt.cfdParts);

    }

    private void setReportPartToTire() {

        this.centroidXReport.getParts().setObjects(rt.tireParts);
        this.centroidYReport.getParts().setObjects(rt.tireParts);
        this.centroidZReport.getParts().setObjects(rt.tireParts);

    }

}
