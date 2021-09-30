import java.io.File;

import star.common.AdjointSolver;
import star.common.AdjointSteadySolver;
import star.common.FixedStepsStoppingCriterion;
import star.common.MonitorIterationStoppingCriterion;
import star.common.MonitorIterationStoppingCriterionMaxLimitType;
import star.common.StarMacro;

/*
 * Solves adjoint and calculates surface sensitivity
 */

public class SolveAdjoint extends StarMacro {

	public void execute() {
		
        SimComponents sim = new SimComponents(getActiveSimulation());
        sim.activeSim.getSceneManager().setVerbose(true);
        MonitorIterationStoppingCriterion clCrit = (MonitorIterationStoppingCriterion) sim.activeSim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Lift Coefficient Criterion");
        clCrit.setIsUsed(false);
        AdjointSolver solver = ((AdjointSolver) sim.activeSim.getSolverManager().getSolver(AdjointSolver.class));
        clCrit.setIsUsed(true);
        
		this.initial(sim);
		this.solve(sim, solver);
		this.calcSrfSensitivity(sim, solver);
		sim.saveSim();
	}
	
	/**
	 * Sets the adjoint steps. I'm doing it with global max step stopping criterion because it's less jank. If I were to use an adjoint stop criterion, I have to increase the global stopping criterion anyways.
	 * @param sim
	 */
	private void initial(SimComponents sim) {
		
        int currentIteration = sim.activeSim.getSimulationIterator().getCurrentIteration();
        ((MonitorIterationStoppingCriterionMaxLimitType) sim.maxStepStop.getCriterionType()).getLimit().setValue(currentIteration + (int) sim.valEnv("adjoint_step"));
        sim.activeSim.println("Setting stopping criteria to: " + ((MonitorIterationStoppingCriterionMaxLimitType) sim.maxStepStop.getCriterionType()).getLimit().evaluate());
        sim.maxStepStop.setInnerIterationCriterion(true);
        sim.maxStepStop.setIsUsed(true);
		
	}
	
	/**
	 * Run adjoint
	 * @param sim
	 * @param solver
	 */
	private void solve(SimComponents sim, AdjointSolver solver) {
		
		sim.activeSim.println("Solving adjoint...");
		solver.runAdjoint();
		sim.activeSim.println("Adjoint solved (I hope).");
		
	}
	
	private void calcSrfSensitivity(SimComponents sim, AdjointSolver solver) {
		
		sim.activeSim.println("Solving for surface sensitivity.");
		solver.computeSurfaceSensitivity();
	}

}
