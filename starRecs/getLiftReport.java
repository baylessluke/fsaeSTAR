// Simcenter STAR-CCM+ macro: getLiftReport.java
// Written by Simcenter STAR-CCM+ 16.06.008
package macro;

import java.util.*;

import star.common.*;
import star.base.report.*;

public class getLiftReport extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    StatisticsReport statisticsReport_0 = 
      ((StatisticsReport) simulation_0.getReportManager().getReport("Average Lift Coefficient"));

    statisticsReport_0.printReport();
  }
}
