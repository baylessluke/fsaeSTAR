import star.common.GeometryObjectGroup;
import star.common.GeometryPart;
import star.common.Simulation;
import star.common.StarMacro;
import star.meshing.MeshOperationManager;
import star.surfacewrapper.SurfaceWrapperAutoMeshOperation;

public class RTSurfaceWrap extends StarMacro {

    private static String SURFACE_WRAPPER_PPM_NAME = "Surface wrapper PPM";

    @Override
    public void execute() {

        Simulation sim = getActiveSimulation();

        // Surface wrapper PPM
        checkWrapperPPM(sim);


    }

    private void checkWrapperPPM(Simulation sim) {

        SurfaceWrapperAutoMeshOperation wrapper = ((SurfaceWrapperAutoMeshOperation) sim.get(MeshOperationManager.class).getObject(SURFACE_WRAPPER_PPM_NAME));
        GeometryObjectGroup partsSelected = wrapper.getInputGeometryObjects();
        int numParts = partsSelected.getChildrenCount();
        if (numParts == 0) {
            
        }

    }

}
