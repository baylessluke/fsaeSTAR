// Simcenter STAR-CCM+ macro: setset.java
// Written by Simcenter STAR-CCM+ 16.06.008
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;

public class setset extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    MonitorIterationStoppingCriterion monitorIterationStoppingCriterion_0 = 
      ((MonitorIterationStoppingCriterion) simulation_0.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Total Solver Time"));

    MonitorIterationStoppingCriterionMaxLimitType monitorIterationStoppingCriterionMaxLimitType_0 = 
      ((MonitorIterationStoppingCriterionMaxLimitType) monitorIterationStoppingCriterion_0.getCriterionType());

    monitorIterationStoppingCriterionMaxLimitType_0.getLimit().setValue(50.0);

    Units units_0 = 
      ((Units) simulation_0.getUnitsManager().getObject("s"));

    monitorIterationStoppingCriterionMaxLimitType_0.getLimit().setUnits(units_0);
  }
}
