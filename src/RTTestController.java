import star.common.StarMacro;

public class RTTestController extends StarMacro {

    RTTestComponent rt;

    public void execute() {

        rt = new RTTestComponent(getActiveSimulation());

        new RTSurfaceWrap(rt);

    }

}
