// Simcenter STAR-CCM+ macro: setLab.java
// Written by Simcenter STAR-CCM+ 16.04.012
package macro;

import java.util.*;

import star.common.*;
import star.motion.*;

public class setLab extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    Region region_0 = 
      simulation_0.getRegionManager().getRegion("Subtract");

    MotionSpecification motionSpecification_0 = 
      region_0.getValues().get(MotionSpecification.class);

    LabReferenceFrame labReferenceFrame_0 = 
      ((LabReferenceFrame) simulation_0.get(ReferenceFrameManager.class).getObject("Lab Reference Frame"));

    motionSpecification_0.setReferenceFrame(labReferenceFrame_0);

    ReferenceFrame referenceFrame_0 = 
      region_0.getValues().get(ReferenceFrame.class);

    LabCoordinateSystem labCoordinateSystem_0 = 
      simulation_0.getCoordinateSystemManager().getLabCoordinateSystem();

    referenceFrame_0.setCoordinateSystem(labCoordinateSystem_0);
  }
}
