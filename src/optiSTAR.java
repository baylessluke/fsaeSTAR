import star.base.neo.NeoObjectVector;
import star.base.report.StatisticsReport;
import star.cadmodeler.CadModel;
import star.cadmodeler.SolidModelCompositePart;
import star.cadmodeler.SolidModelManager;
import star.cadmodeler.UserDesignParameter;
import star.common.*;

public class optiSTAR extends StarMacro {

    public void execute()
    {
        Simulation activeSim = getActiveSimulation();

        /*
        TODO:
        Build a function for metropolis
        Track best ever
        Track cycles
        Pick number of cycles for temperature reduction
        Pick temperature reduction rate
        Pick maximum number of fevals
        Write a function to write results to a text file to resume from
        Write a function to read results from a text file to resume from
        Pick bounds
        Track number of accepted moves vs number of potential moves.
        Write function to update step vector.
        
         */

        final double[] x_lower_bound = {
                -20,    //F1_F2_AOA
                -0.5,   //F1_OVERLAP
                -0.1,   //F1_SLOT
                -20,    //F2_AOA
                -0.5,   //F2_OVERLAP
                -0.1,   //F2_SLOT
                -3,     //GLOBAL_AOA
                -1.5,   //GROUND_CLEARANCE
        };

        final double[] x_upper_bound = {
                20,     //F1_F2_AOA
                1,      //F1_OVERLAP
                0.4,    //F1_SLOT
                20,     //F2_AOA
                1,      //F2_OVERLAP
                0.4,    //F2_SLOT
                3,      //GLOBAL_AOA
                1,      //GROUND_CLEARANCE
        };

        double[] step_vector = {
                4,      //F1_F2_AOA
                0.25,   //F1_OVERLAP
                0.15,   //F1_SLOT
                4,      //F2_AOA
                0.125,  //F2_OVERLAP
                0.125,  //F2_SLOT
                1,      //GLOBAL_AOA
                0.25,   //GROUND_CLEARANCE
        };

        int cycle_count = 0;
        double temperature = 4;
        double[] best_ever_x_arr = new double[7];
        double best_lift;
        


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

    public void runSim(Simulation sim)
    {
        SimulationIterator simIter = sim.getSimulationIterator();
        long current_iteration = simIter.getCurrentIteration();
        long new_max_iter = current_iteration + 1500;
        StepStoppingCriterion maxStepCrit =
                ((StepStoppingCriterion) sim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Steps"));
        maxStepCrit.getMaximumNumberStepsObject().getQuantity().setValue(new_max_iter);
        sim.getSimulationIterator().run();
    }

    public double getLiftReport(Simulation sim)
    {
        StatisticsReport avgLift = (StatisticsReport) sim.getReportManager().getReport("Average Lift Coefficient");
        sim.println(avgLift.getReportMonitorValue());
        return avgLift.getReportMonitorValue();
    }
}
