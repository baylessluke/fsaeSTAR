// Simcenter STAR-CCM+ macro: gigaFanBlade.java
// Written by Simcenter STAR-CCM+ 16.04.012
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;
import star.flow.*;

public class gigaFanBlade extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    Region region_0 = 
      simulation_0.getRegionManager().getRegion("Fan");

    ReferenceFrame referenceFrame_0 = 
      region_0.getValues().get(ReferenceFrame.class);

    LabCoordinateSystem labCoordinateSystem_0 = 
      simulation_0.getCoordinateSystemManager().getLabCoordinateSystem();

    CylindricalCoordinateSystem cylindricalCoordinateSystem_0 = 
      ((CylindricalCoordinateSystem) labCoordinateSystem_0.getLocalCoordinateSystemManager().getObject("Fan Cylindrical"));

    referenceFrame_0.setCoordinateSystem(cylindricalCoordinateSystem_0);

    MomentumFanSource momentumFanSource_0 = 
      region_0.getValues().get(MomentumFanSource.class);

    MomentumFanSourceSolver momentumFanSourceSolver_0 = 
      momentumFanSource_0.getSolver();

    momentumFanSourceSolver_0.setIStart(10);
  }
}
