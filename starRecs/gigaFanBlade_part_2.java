// Simcenter STAR-CCM+ macro: gigaFanBlade_part_2.java
// Written by Simcenter STAR-CCM+ 16.04.012
package macro;

import java.util.*;

import star.common.*;
import star.flow.*;

public class gigaFanBlade_part_2 extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    Region region_0 = 
      simulation_0.getRegionManager().getRegion("Fan");

    MomentumFanSource momentumFanSource_0 = 
      region_0.getValues().get(MomentumFanSource.class);

    LabCoordinateSystem labCoordinateSystem_0 = 
      simulation_0.getCoordinateSystemManager().getLabCoordinateSystem();

    CylindricalCoordinateSystem cylindricalCoordinateSystem_0 = 
      ((CylindricalCoordinateSystem) labCoordinateSystem_0.getLocalCoordinateSystemManager().getObject("Fan Cylindrical"));

    momentumFanSource_0.setCoordinateSystem(cylindricalCoordinateSystem_0);
  }
}
