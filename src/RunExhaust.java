import star.common.Simulation;
import star.common.StarMacro;

import java.io.File;

public class RunExhaust extends StarMacro {
    public void execute()
    {
        Simulation activeSim = getActiveSimulation();
        activeSim.getSimulationIterator().run();
        activeSim.saveState(activeSim.getSessionDir() + File.separator + activeSim.getPresentationName() + ".sim");
    }
}
