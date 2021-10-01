import java.util.Collection;

import star.common.AdjointCostFunction;
import star.common.AdjointCostFunctionManager;
import star.common.ReportCostFunction;
import star.common.StarMacro;
import star.common.StarPlot;

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
		StarPlot plot = sim.activeSim.getPlotManager().getPlot("Residuals");
		
		// Remove existing cost function only if it doesn't have existing solutions
		for (AdjointCostFunction f:costFuncs) {
			double[] res = plot.getDataSetManager().getDataSet(f.getPresentationName() + "::Residual").getYValues();
			if (res.length == 0)
				sim.activeSim.get(AdjointCostFunctionManager.class).removeObjects((ReportCostFunction) f);
		}
		
	}
	
	/**
	 * Sets the cost function. If there's no cost function selected, kill the sim
	 * @param sim
	 */
	private void setCostFunc(SimComponents sim) {
		
        String costFunc = SimComponents.valEnvString("adjoint_cost_func");
        ReportCostFunction reportCostFunc = sim.activeSim.get(AdjointCostFunctionManager.class).createAdjointCostFunction(ReportCostFunction.class);
        
        // don't create new cost function if there's a existing cost fucntion for the same report
        Collection<AdjointCostFunction> costFuncs = sim.activeSim.get(AdjointCostFunctionManager.class).getObjects();
        boolean hasCl = false, hasCd = false, hasLD = false;
        for (AdjointCostFunction f:costFuncs) {
        	
        	String name = f.getPresentationName();
        	if (name.equals(SimComponents.ADJOINT_COST_FUNC_CL))
        		hasCl = true;
        	if (name.equals(SimComponents.ADJOINT_COST_FUNC_CD))
        		hasCd = true;
        	if (name.equals(SimComponents.ADJOINT_COST_FUNC_LD))
        		hasLD = true;
        }
        
        
		if (costFunc.equals(SimComponents.ADJOINT_COST_FUNC_CL) && !hasCl) {
			reportCostFunc.setPresentationName("Lift Coefficient");
			reportCostFunc.setReport(sim.activeSim.getReportManager().getReport("Lift Coefficient"));
		} else if (costFunc.equals(SimComponents.ADJOINT_COST_FUNC_CD) && !hasCd){
			reportCostFunc.setPresentationName("Drag Coefficient");
			reportCostFunc.setReport(sim.activeSim.getReportManager().getReport("Drag Coefficient"));
		} else if (costFunc.equals(SimComponents.ADJOINT_COST_FUNC_LD) && !hasLD) {
			reportCostFunc.setPresentationName("L/D");
			reportCostFunc.setReport(sim.activeSim.getReportManager().getReport("L/D"));
		} else if (hasCl || hasCd || hasLD) {
			sim.activeSim.println("Existing cost function of the same type detected. No new cost function created.");
		} else {
			sim.activeSim.println("Error! No cost function selected. Killing sim.");
			sim.killSim();
		}
	}
	
}
