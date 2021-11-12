import star.base.neo.NeoObjectVector;
import star.base.report.StatisticsReport;
import star.cadmodeler.CadModel;
import star.cadmodeler.SolidModelCompositePart;
import star.cadmodeler.SolidModelManager;
import star.cadmodeler.UserDesignParameter;
import star.common.*;
import star.meshing.AutoMeshOperation2d;
import star.meshing.MeshOperationManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math.*;
import java.util.Arrays;
import java.util.Random;

public class optiSTAR extends StarMacro {

    public static final int STAR_ITERS = 1000;
    final int N_DESIGN_VARS = 7;
    final double convergence_crit = 0.1; //converged if we're not moving by more than 0.1%
    final int max_fevals = 1000;
    final int N_CYCLES = 5;
    final int CONV_TRIGS = 3;

    public void execute()
    {
        Simulation activeSim = getActiveSimulation();

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

        double [] initial_guess = {
                0,      //F1_F2_AOA
                0,      //F1_OVERLAP
                0,      //F1_SLOT
                0,      //F2_AOA
                0,      //F2_OVERLAP
                0,      //F2_SLOT
                0,      //GLOBAL_AOA
                0,      //GROUND_CLEARANCE
        };

        double temperature = 4;
        double[] best_ever_x_arr = new double[7];
        double best_lift = 0;
        double new_lift = 0;
        int convergence_counter = 0;
        int f_eval = 0;
        boolean converged = false;
        double cooling_rate = 0.8;

        while (f_eval < max_fevals && !converged) {
            activeSim.println("----STARTING OPTIMIZATION----");
            double acceptance_rate = 0;
            for (int i = 0; i < N_CYCLES; i++) {
                cycle results = run_cycle(step_vector, initial_guess, x_lower_bound, x_upper_bound, activeSim, temperature);
                initial_guess = results.latest_guess.clone();
                f_eval = f_eval + step_vector.length;
                activeSim.println("FEVAL: " + f_eval);
                if (results.best_lift < best_lift) {
                    best_lift = results.best_lift;
                    best_ever_x_arr = results.best_of_cycle.clone();
                    activeSim.println("NEW BEST LIFT: " + best_lift);
                }
                new_lift = results.latest_lift;
                acceptance_rate = acceptance_rate + results.acceptance_rate;
            }
            acceptance_rate = acceptance_rate / N_CYCLES;
            activeSim.println("ACCEPTANCE RATE: " + acceptance_rate);
            if (check_convergence(new_lift, best_lift))
                convergence_counter = convergence_counter + 1;
            if (convergence_counter >= CONV_TRIGS)
                converged = true;
            temperature = temperature * cooling_rate;
            activeSim.println("TEMPERATURE: " + temperature);
            activeSim.println("CONVERGENCE COUNT: " + convergence_counter);
            activeSim.println("STEP VECTOR: ");
            step_vector = update_step_vector(step_vector, acceptance_rate).clone();
            for (double v : step_vector) {
                activeSim.println(v);
            }
        }

        File fout = new File("results.txt");
        try {
            fout.createNewFile();
            FileWriter writer = new FileWriter(fout);
            writer.write("Number of fevals: " + f_eval + "\n");
            writer.write("Final lift coefficient: " + best_lift + "\n");
            writer.write("Final design variables: \n");
            for (double v : best_ever_x_arr) {
                writer.write(v + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveSim(activeSim);

    }

    private void saveSim(Simulation activeSim) {
        activeSim.saveState(activeSim.getSessionDir() + File.separator + activeSim.getPresentationName() + ".sim");
    }

    class cycle
    {
        public double [] best_of_cycle;
        public double acceptance_rate;
        public double best_lift;
        public double [] latest_guess;
        public double latest_lift;
    }

    public cycle run_cycle(double[] stepvector, double[] initial_guess, double[] lower_bounds, double[] upper_bounds, Simulation sim, double temperature)
    {
        cycle results = new cycle();
        Random rand = new Random();
        double best_lift = run_sim_and_get_results(initial_guess, sim);
        double old_lift = best_lift;
        double[] best_of_cycle = initial_guess.clone();
        int accepted = 0;
        int total = 0;
        results.latest_lift = best_lift;
        results.latest_guess = initial_guess.clone();

        if (stepvector.length != initial_guess.length)
            throw new IllegalStateException("There's something weird with these array sizes");

        for (int i = 0; i < stepvector.length; i++)
        {
            boolean bounds_valid = false;
            double[] cand_guess = initial_guess.clone();
            while (!bounds_valid)
            {
                double r = rand.nextDouble() * 2 - 1;
                cand_guess = initial_guess.clone();
                cand_guess[i] = cand_guess[i] + r * stepvector[i];
                bounds_valid = check_bounds(cand_guess, lower_bounds, upper_bounds);
            }
            for (double v: cand_guess)
            {
                sim.println("running var: " + v);
            }
            double new_lift = run_sim_and_get_results(cand_guess, sim);
            if (check_metropolis(new_lift, old_lift, temperature))
            {
                old_lift = new_lift;
                initial_guess = cand_guess.clone();
                results.latest_guess = initial_guess;
                results.latest_lift = new_lift;
                if (new_lift < best_lift)
                {
                    best_lift = new_lift;
                    best_of_cycle = cand_guess.clone();
                }
                accepted = accepted + 1;
            }
            total = total + 1;
        }

        results.acceptance_rate = (double) accepted / (double) total;
        results.best_lift = best_lift;
        results.best_of_cycle = best_of_cycle;
        saveSim(sim);
        return results;
    }

    public double run_sim_and_get_results(double[] design_vars, Simulation sim)
    {
        update_parameters(design_vars, sim);
        runSim(sim);
        return getLiftReport(sim);
    }

    public double[] update_step_vector(double[] step_vector, double acceptance_rate)
    {
        double[] new_step_vector = step_vector.clone();

        if (acceptance_rate > 0.6)
        {
            for (int i = 0; i < step_vector.length; i++)
            {
                new_step_vector[i] = step_vector[i] * (1 + 2 * (acceptance_rate - 0.6) / 0.4);
            }
        }

        if (acceptance_rate < 0.4)
        {
            for (int i = 0; i < step_vector.length; i++)
            {
                new_step_vector[i] = step_vector[i] / (1 + 2 * (0.4 - acceptance_rate) / 0.4);
            }
        }

        return new_step_vector;
    }

    public boolean check_bounds(double[] candidate, double[] lower_bounds, double[] upper_bounds)
    {
        if (!((candidate.length == lower_bounds.length) && (candidate.length == upper_bounds.length)))
        {
            throw new IllegalStateException("these array lengths are whack");
        }

        for (int i = 0; i < candidate.length; i++)
        {
            if (candidate[i] > upper_bounds[i] || candidate[i] < lower_bounds[i])
                return false;
        }

        return true;
    }

    public void update_parameters(double[] x_arr, Simulation sim)
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
        AutoMeshOperation2d autoMesh = (AutoMeshOperation2d) sim.get(MeshOperationManager.class).getObject("Automated Mesh (2D)");
        autoMesh.execute();
        SimulationIterator simIter = sim.getSimulationIterator();
        long current_iteration = simIter.getCurrentIteration();
        long new_max_iter = current_iteration + STAR_ITERS;
        StepStoppingCriterion maxStepCrit =
                ((StepStoppingCriterion) sim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Steps"));
        maxStepCrit.getMaximumNumberStepsObject().getQuantity().setValue(new_max_iter);
        sim.getSimulationIterator().run();
    }

    public double getLiftReport(Simulation sim)
    {
        StatisticsReport avgLift = (StatisticsReport) sim.getReportManager().getReport("Average Lift Coefficient");
        sim.println(avgLift.getReportMonitorValue());
        double lift_val = avgLift.getReportMonitorValue();
        if (lift_val < -6)
            lift_val = 0;
        return lift_val;
    }

    public boolean check_convergence(double cl_current, double cl_best_ever)
    {
        return (Math.abs(cl_current - cl_best_ever) / Math.abs(cl_best_ever)) * 100 < convergence_crit;
    }

    public boolean check_metropolis(double cl_new, double cl_old, double temperature)
    {
        double delF = cl_new - cl_old;
        if (delF < 0)
            return true;

        Random rand = new Random();
        double p_prime = rand.nextDouble();
        double P = Math.exp(-delF / temperature);
        return P < p_prime;
    }
}
