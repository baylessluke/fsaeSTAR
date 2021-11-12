// Simcenter STAR-CCM+ macro: run_auto_mesh.java
// Written by Simcenter STAR-CCM+ 16.06.008
package macro;

import java.util.*;

import star.common.*;
import star.meshing.*;

public class run_auto_mesh extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    AutoMeshOperation2d autoMeshOperation2d_0 = 
      ((AutoMeshOperation2d) simulation_0.get(MeshOperationManager.class).getObject("Automated Mesh (2D)"));

    autoMeshOperation2d_0.execute();
  }
}
