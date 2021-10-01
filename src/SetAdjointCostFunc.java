import java.util.Collection;

import star.common.AdjointCostFunction;
import star.common.AdjointCostFunctionManager;
import star.common.ReportCostFunction;
import star.common.StarMacro;

public class SetAdjointCostFunc extends StarMacro {
	
	public void execute() {
		
        SimComponents sim = new SimComponents(getActiveSimulation());
        sim.activeSim.getSceneManager().setVerbose(true);
        
        this.removeExisting(sim);
		this.setCostFunc(sim);
	}
	
	/**
	 * Remove existing cost functions if there are any
	 * @param sim
	 */
	private void removeExisting(SimComponents sim) {
		
		Collection<AdjointCostFunction> costFuncs = sim.activeSim.get(AdjointCostFunctionManager.class).getObjects();
		
		for (AdjointCostFunction f:costFuncs) {
			sim.activeSim.get(AdjointCostFunctionManager.class).removeObjects((ReportCostFunction) f);
		}
		
	}
	
	/**
	 * Sets the cost function. If there's no cost function selected, kill the sim
	 * @param sim
	 */
	private void setCostFunc(SimComponents sim) {
		String costFunc = SimComponents.valEnvString("adjoint_cost_func");
		ReportCostFunction reportCostFunc;
		reportCostFunc = sim.activeSim.get(AdjointCostFunctionManager.class).createAdjointCostFunction(ReportCostFunction.class);
		
		if (costFunc.equals(SimComponents.ADJOINT_COST_FUNC_CL)) {
			reportCostFunc.setPresentationName("Lift Coefficient");
			reportCostFunc.setReport(sim.activeSim.getReportManager().getReport("Lift Coefficient"));
		} else if (costFunc.equals(SimComponents.ADJOINT_COST_FUNC_CD)){
			reportCostFunc.setPresentationName("Drag Coefficient");
			reportCostFunc.setReport(sim.activeSim.getReportManager().getReport("Drag Coefficient"));
		} else if (costFunc.equals(SimComponents.ADJOINT_COST_FUNC_LD)) {
			reportCostFunc.setPresentationName("L/D");
			reportCostFunc.setReport(sim.activeSim.getReportManager().getReport("L/D"));
		} else {
			sim.activeSim.println("Error! No cost function selected. Killing sim.");
			sim.killSim();
		}
	}
	
}
