// Simcenter STAR-CCM+ macro: coordSys.java
// Written by Simcenter STAR-CCM+ 16.04.012
package macro;

import java.util.*;

import star.common.*;
import star.motion.*;

public class coordSys extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    Region region_0 = 
      simulation_0.getRegionManager().getRegion("Subtract");

    ReferenceFrame referenceFrame_0 = 
      region_0.getValues().get(ReferenceFrame.class);

    LabCoordinateSystem labCoordinateSystem_0 = 
      simulation_0.getCoordinateSystemManager().getLabCoordinateSystem();

    CylindricalCoordinateSystem cylindricalCoordinateSystem_0 = 
      ((CylindricalCoordinateSystem) labCoordinateSystem_0.getLocalCoordinateSystemManager().getObject("Domain_Axis"));

    referenceFrame_0.setCoordinateSystem(cylindricalCoordinateSystem_0);

    MotionSpecification motionSpecification_0 = 
      region_0.getValues().get(MotionSpecification.class);

    UserRotatingReferenceFrame userRotatingReferenceFrame_0 = 
      ((UserRotatingReferenceFrame) simulation_0.get(ReferenceFrameManager.class).getObject("Rotating"));

    motionSpecification_0.setReferenceFrame(userRotatingReferenceFrame_0);
  }
}
