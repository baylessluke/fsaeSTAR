// Simcenter STAR-CCM+ macro: getSolverElapsed.java
// Written by Simcenter STAR-CCM+ 16.06.008
package macro;

import java.util.*;

import star.common.*;
import star.base.report.*;

public class getSolverElapsed extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    CumulativeElapsedTimeReport cumulativeElapsedTimeReport_0 = 
      ((CumulativeElapsedTimeReport) simulation_0.getReportManager().getReport("Total Solver Elapsed Time"));

    cumulativeElapsedTimeReport_0.printReport();
  }
}
