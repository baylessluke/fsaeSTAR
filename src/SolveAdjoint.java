import star.common.*;
import star.motion.AdjointGridFluxSolver;

import java.util.Arrays;

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
        
		this.initial(sim, solver);
		this.solve(sim, solver);
		this.calcSrfSensitivity(sim, solver);
		
        clCrit.setIsUsed(true);
		sim.saveSim();
	}
	
	/**
	 * Sets the adjoint steps. I'm doing it with global max step stopping criterion because it's less jank. If I were to use an adjoint stop criterion, I have to increase the global stopping criterion anyways.
	 * @param sim
	 */
	private void initial(SimComponents sim, AdjointSolver solver) {
		
        int currentIteration = sim.activeSim.getSimulationIterator().getCurrentIteration();
        ((MonitorIterationStoppingCriterionMaxLimitType) sim.maxStepStop.getCriterionType()).getLimit().setValue(currentIteration + (int) sim.valEnv("adjoint_step"));
        sim.activeSim.println("Setting stopping criteria to: " + ((MonitorIterationStoppingCriterionMaxLimitType) sim.maxStepStop.getCriterionType()).getLimit().evaluate());
        sim.maxStepStop.setInnerIterationCriterion(true);
        sim.maxStepStop.setIsUsed(true);

		// solver set up
		// Increase krylov space to 60
		AdjointGmresAlgorithm gmres = solver.getGmresAlgorithm();
		gmres.setMaxKrylovBaseSize(60);

	}
	
	/**
	 * Run adjoint. Will only run the cost function selected.
	 * @param sim
	 * @param solver
	 */
	private void solve(SimComponents sim, AdjointSolver solver) {

		String costFunc = SimComponents.valEnvString("adjoint_cost_func");
		if (costFunc.equals(SimComponents.ADJOINT_COST_FUNC_CL))
			costFunc = "Lift Coefficient";
		else if (costFunc.equals(SimComponents.ADJOINT_COST_FUNC_CD))
			costFunc = "Drag Coefficient";
		else
			costFunc = "L/D";
		ReportCostFunction reportCostFunc = (ReportCostFunction) sim.activeSim.get(AdjointCostFunctionManager.class).getAdjointCostFunction(costFunc);

		sim.activeSim.println("Solving adjoint...");
		solver.runAdjoint(Arrays.<AdjointCostFunction>asList(reportCostFunc));
		sim.activeSim.println("Adjoint solved (I hope).");
		
	}
	
	private void calcSrfSensitivity(SimComponents sim, AdjointSolver solver) {
		
		sim.activeSim.println("Solving for surface sensitivity.");
		solver.computeSurfaceSensitivity();
	}

}
