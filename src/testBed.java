import star.common.StarMacro;

public class testBed extends StarMacro {
    public void execute()
    {
        simComponents activeSim = new simComponents(getActiveSimulation());
        exportScenes.exportMesh(activeSim);
    }
}