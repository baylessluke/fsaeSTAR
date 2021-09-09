// Simcenter STAR-CCM+ macro: mesh_part_combine.java
// Written by Simcenter STAR-CCM+ 16.02.008
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;
import star.meshing.*;

public class mesh_part_combine extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    MeshPartFactory meshPartFactory_0 = 
      simulation_0.get(MeshPartFactory.class);

    CompositePart compositePart_0 = 
      ((CompositePart) simulation_0.get(SimulationPartManager.class).getPart("CFD_SURFACES"));

    MeshPart meshPart_0 = 
      ((MeshPart) compositePart_0.getChildParts().getPart("PF22_CHA_0004_DriverModels2.1_CLONE_387228"));

    MeshPart meshPart_1 = 
      ((MeshPart) compositePart_0.getChildParts().getPart("PF22_CHA_0004_DriverModels2.1_CLONE_408420"));

    meshPartFactory_0.combineMeshParts(meshPart_0, new NeoObjectVector(new Object[] {meshPart_1}));
  }
}
