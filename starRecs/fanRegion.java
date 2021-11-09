// Simcenter STAR-CCM+ macro: fanRegion.java
// Written by Simcenter STAR-CCM+ 16.02.008
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;
import star.flow.*;

public class fanRegion extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    Region region_1 = 
      simulation_0.getRegionManager().getRegion("Radiator");

    region_1.getConditions().get(MomentumUserSourceOption.class).setSelected(MomentumUserSourceOption.Type.NONE);

    region_1.getConditions().get(MomentumUserSourceOption.class).setSelected(MomentumUserSourceOption.Type.FAN);

    MomentumFanSource momentumFanSource_1 = 
      region_1.getValues().get(MomentumFanSource.class);

    FileTable fileTable_0 = 
      ((FileTable) simulation_0.getTableManager().getTable("fan_table_csv"));

    momentumFanSource_1.setTable(fileTable_0);

    momentumFanSource_1.setTableVolDot("m^3/s");

    momentumFanSource_1.setTableP("dP");

    InterfaceBoundary interfaceBoundary_0 = 
      ((InterfaceBoundary) region_1.getBoundaryManager().getBoundary("Inlet [Inlet interface 1]"));

    momentumFanSource_1.setUpstreamBoundary(interfaceBoundary_0);

    InterfaceBoundary interfaceBoundary_1 = 
      ((InterfaceBoundary) region_1.getBoundaryManager().getBoundary("Outlet [Outlet interface 1]"));

    momentumFanSource_1.setDownstreamBoundary(interfaceBoundary_1);
  }
}
