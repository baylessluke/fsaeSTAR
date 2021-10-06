import star.common.*;

/*
 * Solves adjoint and calculates surface sensitivity
 */

public class SolveAdjoint extends StarMacro {

	public void execute() {
		
        SimComponents sim = new SimComponents(getActiveSimulation());
        sim.activeSim.getSceneManager().setVerbose(true);
        MonitorIterationStoppingCriterion clCrit = (MonitorIterationStoppingCriterion) sim.activeSim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Lift Coefficient Criterion");
        clCrit.setIsUsed(false);
        AdjointSolver solver = sim.activeSim.getSolverManager().getSolver(AdjointSolver.class);
        
		this.initial(sim, solver);
		this.solve(sim, solver);
		this.calcSrfSensitivity(sim, solver);
		
        clCrit.setIsUsed(true);
		sim.saveSim();
	}
	
	/**
	 * Sets the adjoint steps. I'm doing it with global max step stopping criterion because it's less jank. If I were to use an adjoint stop criterion, I have to increase the global stopping criterion anyways.
	 */
	private void initial(SimComponents sim, AdjointSolver solver) {
		
        int currentIteration = sim.activeSim.getSimulationIterator().getCurrentIteration();
        ((MonitorIterationStoppingCriterionMaxLimitType) sim.maxStepStop.getCriterionType()).getLimit().setValue(currentIteration + (int) sim.valEnv("adjoint_step"));
        sim.activeSim.println("Setting stopping criteria to: " + ((MonitorIterationStoppingCriterionMaxLimitType) sim.maxStepStop.getCriterionType()).getLimit().evaluate());
        sim.maxStepStop.setInnerIterationCriterion(true);
        sim.maxStepStop.setIsUsed(true);

		// solver set up
		// Switch to flexible GMRES
		solver.getAccelerationOption().setSelected(AdjointAccelerationOption.Type.FLEXIBLE_GMRES);

		// Increase krylov space to 50
		AdjointFlexibleGmresAlgorithm gmres = solver.getFlexibleGmresAlgorithm();
		gmres.setMaxKrylovBaseSize(50);

		// Increase recycle percentage to 50
		gmres.setRecycledVectorsPercentage(50);

	}
	
	/**
	 * Run adjoint. Will only run the cost function selected.
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
