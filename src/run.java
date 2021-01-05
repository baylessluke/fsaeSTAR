/*
Runs the sim. First runs it for 4 steps, runs MeshRepair to see if anything needs to be nuked, then continues running.
 */

import star.common.MonitorIterationStoppingCriterionMaxLimitType;
import star.common.StarMacro;
import star.common.StarScript;

import java.io.File;

public class run extends StarMacro {

    private boolean CONVERGED = false;


    public void execute() {

        SimComponents activeSim = new SimComponents(getActiveSimulation());
        Regions obj = new Regions();
        obj.initFans(activeSim);
        activeSim.activeSim.println("--- RUNNING SIMULATION ---");
        Regions.setTurbulence(activeSim);
        initial(activeSim);
        continueRun(activeSim);
        activeSim.saveSim();
    }

    //This is only called once. This is what you run the first time you enter run.java. Sets up the maximum step stopping criteria based on the sysenv, and sets the freestream value from sysenv. Also sets an abort file. This abort file hasn't worked in a very long time, but the code is still here.
    private void initial(SimComponents activeSim)
    {
        int currentIteration = activeSim.activeSim.getSimulationIterator().getCurrentIteration();
        ((MonitorIterationStoppingCriterionMaxLimitType) activeSim.maxStepStop.getCriterionType()).getLimit().setValue(currentIteration + activeSim.maxSteps);
        activeSim.maxStepStop.setInnerIterationCriterion(true);
        activeSim.maxStepStop.setIsUsed(true);
        activeSim.setFreestreamParameterValue();
        activeSim.abortFile.setAbortFilePath(SimComponents.valEnvString("PBS_O_WORKDIR") + File.separator + SimComponents.valEnvString("PBS_JOBID"));
    }

    //There's some recursion in here.
    private void continueRun(SimComponents activeSim)
    {
        //Flip the convergence flag, and we climb out of the recursion ladder as fast as we possibly can.
        if (CONVERGED)
            return;

        //Disable maximum velocity criteria and run for 4 steps.
        activeSim.maxVel.setIsUsed(false);
        activeSim.activeSim.println("Disable maxVel attempted");
        activeSim.activeSim.getSimulationIterator().step(4);

        //Enable maximum velocity criteria
        activeSim.maxVel.setIsUsed(true);
        activeSim.activeSim.println("Enable maxVel attempted");

        //If we're not doing convergence checks, keep running indefinitely
        if (activeSim.convergenceCheck == false)
            activeSim.activeSim.getSimulationIterator().run();

        //If we're doing convergence checks, check for convergence every 100 iterations.
        else
        {
            ConvergenceChecker obj = new ConvergenceChecker(activeSim);
            if (obj.convergenceResults.getOrDefault(SimComponents.LIFT_COEFFICIENT_PLOT, false))
            {
                CONVERGED = true;
                return;
            }

            else if (!activeSim.maxStepStop.getIsSatisfied())
                activeSim.activeSim.getSimulationIterator().run(100);
        }

        //If maximum velocity is triggered, run mesh repair, then continue iterating again.
        if (activeSim.maxVel.getIsSatisfied())
        {
            new StarScript(getActiveRootObject(), new java.io.File(resolvePath("MeshRepair.java"))).play();
            continueRun(activeSim);
        }
    }

}
