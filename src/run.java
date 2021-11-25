/*
Runs the sim. First runs it for 4 steps, runs MeshRepair to see if anything needs to be nuked, then continues running.
 */

import star.common.MonitorIterationStoppingCriterionMaxLimitType;
import star.common.MonitorIterationStoppingCriterionType;
import star.common.StarMacro;
import star.common.StarScript;

import java.io.File;
import java.util.Objects;

public class run extends StarMacro {

    private boolean CONVERGED;


    public void execute() {

        SimComponents activeSim = new SimComponents(getActiveSimulation());
        CONVERGED = false;
        Regions obj = new Regions();
        obj.initFans(activeSim);
        activeSim.activeSim.println("--- RUNNING SIMULATION ---");
        Regions.setTurbulence(activeSim);
        initial(activeSim);
        continueRun(activeSim);
        activeSim.saveSim();   //REMEMBER TO ADD SAVE.JAVA TO MACROCONTROLLER IF YOU REMOVE THIS LINE
    }

    //This is only called once. This is what you run the first time you enter run.java. Sets up the maximum step stopping criteria based on the sysenv, and sets the freestream value from sysenv. Also sets an abort file. This abort file hasn't worked in a very long time, but the code is still here.
    private void initial(SimComponents activeSim)
    {
        int currentIteration = activeSim.activeSim.getSimulationIterator().getCurrentIteration();
        ((MonitorIterationStoppingCriterionMaxLimitType) activeSim.maxStepStop.getCriterionType()).getLimit().setValue(currentIteration + activeSim.maxSteps);
        activeSim.activeSim.println("Setting stopping criteria to: " + ((MonitorIterationStoppingCriterionMaxLimitType) activeSim.maxStepStop.getCriterionType()).getLimit().evaluate());
        activeSim.maxStepStop.setInnerIterationCriterion(true);
        activeSim.maxStepStop.setIsUsed(false); //using time only for now
        activeSim.setFreestreamParameterValue();
        activeSim.abortFile.setAbortFilePath(activeSim.dir + File.separator + SimComponents.valEnvString("SLURM_JOB_ID"));
        MonitorIterationStoppingCriterionMaxLimitType timeStopLimit = (MonitorIterationStoppingCriterionMaxLimitType) activeSim.maxTime.getCriterionType();
        String clusterName = Objects.requireNonNull(SimComponents.valEnvString("CLUSTER"), "Can't recognize cluster from linuxConfig!");
        if (clusterName.equals("scholar") || clusterName.equals("gpu"))
        {
            timeStopLimit.getLimit().setValue(activeSim.totalSolverTime.getValue() + 12600);
            timeStopLimit.getLimit().setUnits(activeSim.seconds);
        }
        else if (clusterName.equals("long"))
        {
            timeStopLimit.getLimit().setValue(activeSim.totalSolverTime.getValue() + 257400);
            timeStopLimit.getLimit().setUnits(activeSim.seconds);
        }
        else
        {
            throw new IllegalArgumentException("Can't figure out what cluster this is running on!");
        }
        activeSim.maxTime.setIsUsed(true);
        activeSim.activeSim.getSimulationIterator().step(1);
    }

    //There's some recursion in here.
    private void continueRun(SimComponents activeSim)
    {
        //If any of these conditions are true, we don't want to run any more iterations. Climb out of whatever recursion we've fallen down
        if (CONVERGED || activeSim.abortFile.getIsSatisfied() || activeSim.maxTime.getIsSatisfied())
            return;

        //Disable maximum velocity criteria and run for 4 steps.
        activeSim.maxVel.setIsUsed(false);
        activeSim.activeSim.println("Disable maxVel attempted");
        activeSim.activeSim.getSimulationIterator().step(4);

        //Enable maximum velocity criteria
        activeSim.maxVel.setIsUsed(true);
        activeSim.activeSim.println("Enable maxVel attempted");

        //If we're not doing convergence checks, keep running indefinitely
        if (!activeSim.convergenceCheck && !activeSim.abortFile.getIsSatisfied())
            activeSim.activeSim.getSimulationIterator().run();

        //If we're doing convergence checks, check for convergence every 50 iterations.
        do
        {
            ConvergenceChecker obj = new ConvergenceChecker(activeSim);
            for (String key : obj.convergenceResults.keySet())
            {
                if (key.contains(SimComponents.LIFT_COEFFICIENT_PLOT))
                {
                    if (obj.convergenceResults.get(key) && activeSim.convergenceCheck) {
                        CONVERGED = true;
                        return;
                    }
                }
            }
            if (!activeSim.maxStepStop.getIsSatisfied() && !activeSim.abortFile.getIsSatisfied())
                activeSim.activeSim.getSimulationIterator().run(50);
        } while(!CONVERGED && !activeSim.maxTime.getIsSatisfied() && !activeSim.abortFile.getIsSatisfied() && !activeSim.maxVel.getIsSatisfied());

        //If maximum velocity is triggered, run mesh repair, then continue iterating again.
        if (activeSim.maxVel.getIsSatisfied())
        {
            new StarScript(getActiveRootObject(), new java.io.File(resolvePath("MeshRepair.java"))).play();
            continueRun(activeSim);
        }
    }

}
