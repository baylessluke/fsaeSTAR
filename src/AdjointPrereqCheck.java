/**
 * Just a sanity check to see if all requirements for adjoint solver has been met. Kills the sim if not.
 */
import star.common.StarMacro;
import star.common.StarPlot;

public class AdjointPrereqCheck extends StarMacro {


	public void execute() {
		
        SimComponents sim = new SimComponents(getActiveSimulation());
        sim.activeSim.getSceneManager().setVerbose(true);

        this.checkPrimal(sim);
        this.checkPhysics(sim);
	}
	
	/**
	 * Checks if primal solution exists. If no, kills the sim. If yes, then check continuity. If continuity is above 1e-4, spits out a warning about high residual. 
	 * @param sim
	 */
	private void checkPrimal(SimComponents sim) {
		
		if (sim.activeSim.getSimulationIterator().getCurrentIteration() <= 0) {
			sim.activeSim.println("Error! Adjoint solver needs primal solution to work");
			sim.killSim();
		} else {
			StarPlot plot = sim.activeSim.getPlotManager().getPlot("Residuals");
			double[] continuityValues = plot.getDataSetManager().getDataSet("Continuity").getYValues();
			double lastContinuity = continuityValues[continuityValues.length - 1];
			if (lastContinuity > 1e-4) {
				sim.activeSim.println("WARNING! High continuity residual detected. It is recommended to run adjoint solver with contiunity at or below 1e-4.");
			}
		}
	}
	
	/**
	 * Checks if S-a adjoint physics has been used for primal solution by comparing the name of physics continuum assigned to subtract, radiator, and dual radiator regions. Kills the sim if not.
	 * @param sim
	 */
	private void checkPhysics(SimComponents sim) {
		String domainRegionPhysics = sim.domainRegion.getPhysicsContinuum().getPresentationName();
		String radiatorRegionPhysics = sim.radiatorRegion.getPhysicsContinuum().getPresentationName();
		String dualRadRegionPhysics;
		if (sim.dualRadFlag) {
			dualRadRegionPhysics = sim.dualRadiatorRegion.getPhysicsContinuum().getPresentationName();
			if (!dualRadRegionPhysics.equals(SimComponents.ADJOINT_PHYSICS_NAME)) {
				sim.activeSim.println("Error in Radiator 2! Adjoint physics continuum has not been used to generate primal solution.");
				sim.killSim();
			}
		}

		if (!domainRegionPhysics.equals(SimComponents.ADJOINT_PHYSICS_NAME)) {
			sim.activeSim.println("Error in Subtract! Adjoint physics continuum has not been used to generate primal solution.");
			sim.killSim();
		}
			
		if (!radiatorRegionPhysics.equals(SimComponents.ADJOINT_PHYSICS_NAME)) {
			sim.activeSim.println("Error in Radiator! Adjoint physics continuum has not been used to generate primal solution.");
			sim.killSim();
		}
	}
}
