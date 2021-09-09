// Simcenter STAR-CCM+ macro: explode_part.java
// Written by Simcenter STAR-CCM+ 16.02.008
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;
import star.cadmodeler.*;

public class explode_part extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    CompositePart compositePart_0 = 
      ((CompositePart) simulation_0.get(SimulationPartManager.class).getPart("CFD_AERODYNAMICS_830250079"));

    SolidModelCompositePart solidModelCompositePart_1 = 
      ((SolidModelCompositePart) compositePart_0.getChildParts().getPart("FW_PF22"));

    SolidModelCompositePart solidModelCompositePart_2 = 
      ((SolidModelCompositePart) solidModelCompositePart_1.getChildParts().getPart("PF22_AER_60645K111_0MASS_CLONE"));

    solidModelCompositePart_2.explode(new NeoObjectVector(new Object[] {solidModelCompositePart_2}));
  }
}
