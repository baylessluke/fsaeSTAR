// Simcenter STAR-CCM+ macro: updateMaxSteps.java
// Written by Simcenter STAR-CCM+ 16.06.008
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;

public class updateMaxSteps extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    StepStoppingCriterion stepStoppingCriterion_0 = 
      ((StepStoppingCriterion) simulation_0.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Steps"));

    IntegerValue integerValue_0 = 
      stepStoppingCriterion_0.getMaximumNumberStepsObject();

    integerValue_0.getQuantity().setValue(3000.0);

    stepStoppingCriterion_0.setIsUsed(true);
  }
}
