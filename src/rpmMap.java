import star.base.report.StatisticsReport;
import star.common.*;

import java.io.File;

public class rpmMap extends StarMacro {

    public void execute()
    {
        double minRPM = Double.parseDouble(System.getenv("minRPM"));
        double stepRPM = Double.parseDouble(System.getenv("stepRPM"));
        double currentRPM = minRPM;
        String clusterName = System.getenv("CLUSTER");
        Double delta_time = null;

        if (clusterName.equals("scholar") || clusterName.equals("gpu"))
        {
            delta_time = 220 * 60 * 1000.0;
        }
        else if (clusterName.equals("long"))
        {
            delta_time = 4300 * 60 * 1000.0;
        }

        if (delta_time == null)
        {
            throw new IllegalStateException("Can't figure out what cluster you're running this on!");
        }

        double current_time = System.currentTimeMillis();
        double final_time = current_time + delta_time;

        Simulation sim = getActiveSimulation();

        MonitorIterationStoppingCriterion maxTime = (MonitorIterationStoppingCriterion) sim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Total Solver Time");
        MonitorIterationStoppingCriterionMaxLimitType timeStopLimit = (MonitorIterationStoppingCriterionMaxLimitType) maxTime.getCriterionType();
        PhysicalTimeStoppingCriterion timeLimit = (PhysicalTimeStoppingCriterion) sim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Physical Time");
        StatisticsReport averageExhaust = (StatisticsReport) sim.getReportManager().getReport("Average Exhaust");
        MonitorPlot iteration = (MonitorPlot) sim.getPlotManager().getPlot("Reports Plot - Iteration");
        MonitorPlot time = (MonitorPlot) sim.getPlotManager().getPlot("Reports Plot - Time");

        boolean keep_going = true;
        String filePath = sim.getSessionPath() + File.separator;
        ScalarGlobalParameter rpm = (ScalarGlobalParameter) sim.get(GlobalParameterManager.class).getObject("RPM");

        while (keep_going)
        {
            sim.clearSolution();
            rpm.getQuantity().setValue(currentRPM);
            sim.getSimulationIterator().step(1);
            timeStopLimit.getLimit().setValue((final_time - System.currentTimeMillis()) / 1000);
            sim.getSimulationIterator().step(1);
            sim.getSimulationIterator().run();
            if (maxTime.getIsSatisfied())
            {
                keep_going = false;
            }
            if (timeLimit.getIsSatisfied())
            {
                averageExhaust.printReport(filePath + currentRPM + " avg exhaust.text", false);
                iteration.export(filePath + currentRPM + " iteration.text");
                time.export(filePath + currentRPM + " time.text");
                currentRPM = currentRPM + stepRPM;
            }
        }
    }

}
