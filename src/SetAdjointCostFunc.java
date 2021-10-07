import java.util.ArrayList;
import java.util.Collection;

import star.common.*;
import star.flow.ForceCoefficientReport;

public class SetAdjointCostFunc extends StarMacro {

	private final static String costFuncReportName = "Adjoint Cost Function Report";
	private final static String costFuncName = "Cost Function";

	public void execute() {
		
        SimComponents sim = new SimComponents(getActiveSimulation());
        sim.activeSim.getSceneManager().setVerbose(true);

		this.setCostFuncReport(sim);
        this.removeExisting(sim);
		this.setCostFunc(sim);
	}

	/**
	 * Set up report for cost function
	 */
	private void setCostFuncReport(SimComponents sim) {

		ForceCoefficientReport adjointReport = (ForceCoefficientReport) sim.activeSim.getReportManager().getReport(costFuncReportName);
		System.out.println("Here");
		String reportType = SimComponents.valEnvString("adjoint_cost_func");

		// set force direction
		if (reportType.equals(SimComponents.ADJOINT_COST_FUNC_CD))
			adjointReport.getDirection().setComponents(1, 0, 0);
		else if (reportType.equals(SimComponents.ADJOINT_COST_FUNC_CL))
			adjointReport.getDirection().setComponents(0, 0, 1);
		else {
			sim.activeSim.println("Selected adjoint cost function is invalid! Killing sim.");
			sim.killSim();
		}

		// set full / half car
		if (sim.fullCarFlag)
			adjointReport.getReferenceArea().setValue(1.0);
		else
			adjointReport.getReferenceArea().setValue(0.5);

		// set freestream
		adjointReport.getReferenceVelocity().setValue(sim.freestreamVal);

		// set all aero parts as components for the report
		Collection<Boundary> aeroParts = new ArrayList<>();
		for (String prefix : SimComponents.AERO_PREFIXES){
			aeroParts.addAll(sim.partSpecBounds.get(prefix));
		}
		adjointReport.getParts().setObjects(aeroParts);

	}

	/**
	 * Remove existing cost functions not named costFuncName if there are any
	 */
	private void removeExisting(SimComponents sim) {
		
		Collection<AdjointCostFunction> costFuncs = sim.activeSim.get(AdjointCostFunctionManager.class).getObjects();

		for (AdjointCostFunction f:costFuncs) {
			if (!f.getPresentationName().equals(costFuncName)){
				sim.activeSim.get(AdjointCostFunctionManager.class).removeObjects(f);
			}
		}
		
	}
	
	/**
	 * Sets the cost function. If there's no cost function selected, kill the sim
	 */
	private void setCostFunc(SimComponents sim) {

        // create cost functions if only there are no cost function set
        Collection<AdjointCostFunction> costFuncs = sim.activeSim.get(AdjointCostFunctionManager.class).getObjects();
		if (costFuncs.size() == 0) {
			ReportCostFunction reportCostFunc = sim.activeSim.get(AdjointCostFunctionManager.class).createAdjointCostFunction(ReportCostFunction.class);
			reportCostFunc.setReport(sim.activeSim.getReportManager().getReport("Adjoint Cost Function"));
			reportCostFunc.setPresentationName(costFuncName);
		}
	}
}
