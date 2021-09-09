// Simcenter STAR-CCM+ macro: combineParts.java
// Written by Simcenter STAR-CCM+ 16.02.008
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;
import star.cadmodeler.*;
import star.meshing.*;

public class combineParts extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    MeshPartFactory meshPartFactory_0 = 
      simulation_0.get(MeshPartFactory.class);

    CompositePart compositePart_0 = 
      ((CompositePart) simulation_0.get(SimulationPartManager.class).getPart("CFD_AERODYNAMICS_830250079"));

    SolidModelCompositePart solidModelCompositePart_2 = 
      ((SolidModelCompositePart) compositePart_0.getChildParts().getPart("FW_PF22"));

    SolidModelPart solidModelPart_0 = 
      ((SolidModelPart) solidModelCompositePart_2.getChildParts().getPart("PF22_AER_FW_INNER_FLAP_1_R_CLONE"));

    SolidModelPart solidModelPart_1 = 
      ((SolidModelPart) solidModelCompositePart_2.getChildParts().getPart("PF22_AER_FW_INNER_FLAP_1_L_CLONE"));

    meshPartFactory_0.combineMeshParts(solidModelPart_0, new NeoObjectVector(new Object[] {solidModelPart_1}));

    CompositePart compositePart_2 = 
      ((CompositePart) simulation_0.get(SimulationPartManager.class).getPart("CFD_SURFACES"));

    CompositePart compositePart_1 = 
      ((CompositePart) compositePart_2.getChildParts().getPart("PF22_CHA_0004_DriverModels2.1_CLONE"));

    CompositePart compositePart_3 = 
      ((CompositePart) compositePart_2.getChildParts().getPart("shock_spring2_CLONE"));

    simulation_0.get(SimulationPartManager.class).combineCompositeParts(new NeoObjectVector(new Object[] {compositePart_1, compositePart_3}));
  }
}
