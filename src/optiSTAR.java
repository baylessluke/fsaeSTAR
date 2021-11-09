import star.base.neo.NeoObjectVector;
import star.cadmodeler.CadModel;
import star.cadmodeler.SolidModelCompositePart;
import star.cadmodeler.SolidModelManager;
import star.cadmodeler.UserDesignParameter;
import star.common.Simulation;
import star.common.SimulationPartManager;
import star.common.StarMacro;

public class optiSTAR extends StarMacro {

    public void execute()
    {
        Simulation activeSim = getActiveSimulation();

    }

    public void update_parameters(double x_arr[], Simulation sim)
    {
        CadModel starCAD = ((CadModel) sim.get(SolidModelManager.class).getObject("3D-CAD Model 1"));

        UserDesignParameter F1F2AOA =  getDesignParameter(starCAD, "F1_F2_AOA_OFFSET");
        F1F2AOA.getQuantity().setValue(x_arr[0]);

        UserDesignParameter F1_OVERLAP = getDesignParameter(starCAD, "F1_OVERLAP");
        F1_OVERLAP.getQuantity().setValue(x_arr[1]);

        UserDesignParameter F1_SLOT = getDesignParameter(starCAD, "F1_SLOT");
        F1_SLOT.getQuantity().setValue(x_arr[2]);

        UserDesignParameter F2_AOA = getDesignParameter(starCAD, "F2_AOA");
        F2_AOA.getQuantity().setValue(x_arr[3]);

        UserDesignParameter F2_OVERLAP = getDesignParameter(starCAD, "F2_OVERLAP");
        F2_OVERLAP.getQuantity().setValue(x_arr[4]);

        UserDesignParameter F2_SLOT = getDesignParameter(starCAD, "F2_SLOT");
        F2_SLOT.getQuantity().setValue(x_arr[5]);

        UserDesignParameter GLOBAL_AOA = getDesignParameter(starCAD, "GLOBAL_AOA_OFFSET");
        GLOBAL_AOA.getQuantity().setValue(x_arr[6]);

        UserDesignParameter GROUND_CLEARANCE = getDesignParameter(starCAD, "GROUND_CLEARANCE_OFFSET");
        GROUND_CLEARANCE.getQuantity().setValue(x_arr[7]);

        SolidModelCompositePart FW_BODY = ((SolidModelCompositePart) sim.get(SimulationPartManager.class).getPart("FW_PARTS"));

        if (FW_BODY.needsUpdate())
        {
            sim.get(SimulationPartManager.class).updateParts(new NeoObjectVector(new Object[] {FW_BODY}));
        }
    }

    

    public UserDesignParameter getDesignParameter(CadModel cad, String name)
    {
        return ((UserDesignParameter) cad.getDesignParameterManager().getObject(name));
    }

}
