// Simcenter STAR-CCM+ macro: parameter_update.java
// Written by Simcenter STAR-CCM+ 16.06.008
package macro;

import java.util.*;

import star.common.*;
import star.base.neo.*;
import star.cadmodeler.*;

public class parameter_update extends StarMacro {

  public void execute() {
    execute0();
  }

  private void execute0() {

    Simulation simulation_0 = 
      getActiveSimulation();

    CadModel cadModel_0 = 
      ((CadModel) simulation_0.get(SolidModelManager.class).getObject("3D-CAD Model 1"));

    UserDesignParameter userDesignParameter_0 = 
      ((UserDesignParameter) cadModel_0.getDesignParameterManager().getObject("F2_SLOT"));

    userDesignParameter_0.getQuantity().setValue(0.1);

    Units units_0 = 
      ((Units) simulation_0.getUnitsManager().getObject("in"));

    userDesignParameter_0.getQuantity().setUnits(units_0);

    cadModel_0.update();

    SolidModelCompositePart solidModelCompositePart_0 = 
      ((SolidModelCompositePart) simulation_0.get(SimulationPartManager.class).getPart("FW_PARTS"));

    simulation_0.get(SimulationPartManager.class).updateParts(new NeoObjectVector(new Object[] {solidModelCompositePart_0}));
  }
}
