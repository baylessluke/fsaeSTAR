import star.common.GeometryObject;
import star.common.SimulationPartManager;
import star.meshing.MeshOperationManager;
import star.meshing.MeshOperationPart;
import star.meshing.SimpleBlockPart;
import star.meshing.SubtractPartsOperation;

import java.util.Collection;

public class RTSubtract {

    private final RTTestComponent rt;

    // Names
    private final String SUBTRACT_NAME = "Subtract";
    private final String FREESTREAM_NAME = "Freestream";
    private final String WRAPPER_NAME = "Surface wrapper";

    // Star objects
    private SubtractPartsOperation subtract;
    private SimpleBlockPart freestreamPart;
    private MeshOperationPart wrapperPart;

    public RTSubtract(RTTestComponent rt) {
        this.rt = rt;
        this.initStarObjects();
        this.test();
    }

    private void initStarObjects() {
        this.subtract = (SubtractPartsOperation) rt.sim.get(MeshOperationManager.class).getObject(SUBTRACT_NAME);
        this.freestreamPart = (SimpleBlockPart) rt.sim.get(SimulationPartManager.class).getObject(FREESTREAM_NAME);
        this.wrapperPart = (MeshOperationPart) rt.sim.get(SimulationPartManager.class).getObject(WRAPPER_NAME);
    }

    private void test() {

        // result
        Collection<GeometryObject> inputPartsCollection = subtract.getInputGeometryObjects().getObjects();
        GeometryObject[] inputPartsArray = new GeometryObject[inputPartsCollection.size()];
        Object[] inputPartObjects = inputPartsCollection.toArray();
        for (int i = 0; i < inputPartsCollection.size(); i++)
            inputPartsArray[i] = (GeometryObject) inputPartObjects[i];
        GeometryObject targetPart = subtract.getTargetPart();

        // expected results
        GeometryObject[] expInputParts = {freestreamPart, wrapperPart};

        // input part test
        boolean inputPartsPassed = inputPartsCollection.size() == 2 && inputPartsCollection.contains(freestreamPart) && inputPartsCollection.contains(wrapperPart);
        rt.printTestResults(
                inputPartsPassed,
                "Input Parts Selection",
                RTTestComponent.buildResultStringFromArray(inputPartsArray),
                RTTestComponent.buildResultStringFromArray(expInputParts)
        );

        // target part test
        boolean targetPartsPassed = targetPart.equals(freestreamPart);
        rt.printTestResults(
                targetPartsPassed,
                "Target Parts Selection",
                targetPart.getPresentationName(),
                freestreamPart.getPresentationName()
        );
    }

}
