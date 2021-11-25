/**
 * Ride height testing. Generate the centroid x, y, z report values of all CFD parts and tires at (0,0) ride height.
 * Lower the ride height to (-1, -1), generate the centroid x, y, z report values of CFD parts and tire. x and y values
 * of CFD parts should not change, z value should be 1 in lower. No report values of tires should change
 * Raise the ride height to (-1, 0) and generate centroid x, y, and z report values of CFD parts tire. y values should
 * not change, x should move forward a little, and z should be increased accordingly. Tire centroid should not change.
 */
import star.base.report.ExpressionReport;
import star.base.report.SumReport;
import star.common.PartSurface;

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

    public RTRideHeight(RTTestComponent rt) {

        // initialization
        this.rt = rt;
        double[] stdTire = new double[3]; // centroid of tire parts in standard condition in inches
        double[] stdCFD = new double[3]; // centroid of CFD parts in standard condition in inches
        double[] rhTire = new double[3]; // centroid of tire parts in RH -1 condition in inches
        double[] rhCFD = new double[3]; // centroid of CFD parts in RH -1 condition in inches
        double[] pitchTire = new double[3]; // centroid of tire parts in (-1, 0) condition in inches
        double[] pitchCFD = new double[3]; // centroid of CFD parts in (-1, 0) condition in inches
        this.initReports();

        // standard conditions
        this.setReportPartToTire();
        stdTire = this.getCentroid();
        this.setReportPartToCFD();
        stdCFD = this.getCentroid();

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

}
