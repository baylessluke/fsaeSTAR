// This is a fun one. THIS NEEDS TO BE RUN FROM A BATCH SCRIPT. There's probably a way to modify this so it doesn't
// need to run off of a batch script, but that's too much work.
// -frh = front ride height
// -rrh = rear ride height


import star.base.neo.DoubleVector;
import star.base.neo.NeoObjectVector;
import star.common.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/*
Sets ride height for the car. Pretty self-explanatory code. Works by rotating the car about the front wheel axis, then the rear wheel axis. Gives good control for front and rear ride height independently, but since it's all trig it probably isn't a good idea to use this for insane ride height changes (multiple inches)
 */

public class RideHeight extends StarMacro {

    public void execute()
    {
        execute0();
    }

    private void execute0()
    {
        SimComponents sim = new SimComponents(getActiveSimulation());
        double frontRot;
        double rearRot;
        double frh;
        double rrh;

        frh = sim.valEnv("frh");
        rrh = sim.valEnv("rrh");

        if (frh == 0 && rrh == 0)
            return;

        //DEBUG
        frontRot = Math.atan(rrh / sim.wheelBase);
        rearRot = -Math.atan(frh / sim.wheelBase);

        sim.activeSim.println("Front, rear pitch angle change attempted " + frontRot + " " + rearRot);

        if (frontRot != 0)
            rotateAboutAxis(sim, frontRot, sim.frontWheelCoord);

        if (rearRot != 0)
            rotateAboutAxis(sim, rearRot, sim.rearWheelCoord);
    }

    private void rotateAboutAxis(SimComponents sim, double rotationAngle, CylindricalCoordinateSystem coordSys)
    {
        rotateParts(sim, sim.aeroParts, coordSys, rotationAngle);
        rotateParts(sim, sim.nonAeroParts, coordSys, rotationAngle);
        rotateCoord(sim, sim.radiatorCoord, coordSys, rotationAngle);
        rotateCoord(sim, sim.fanAxis, coordSys, rotationAngle);
        if (sim.dualRadFlag)
            rotateCoord(sim, sim.dualRadCoord, coordSys, rotationAngle);
        if (sim.dualFanFlag)
            rotateCoord(sim, sim.dualFanAxis, coordSys, rotationAngle);
    }

    private void rotateParts(SimComponents activeSim, Collection<GeometryPart> parts, CylindricalCoordinateSystem rotationPoint, double rotationAngle)
    {
        activeSim.activeSim.get(SimulationPartManager.class).rotateParts(parts,
                new DoubleVector(new double[] {0, 0, 1}), Arrays.asList(activeSim.noUnit, activeSim.noUnit, activeSim.noUnit), rotationAngle, rotationPoint);
    }

    private void rotateCoord(SimComponents activeSim, CoordinateSystem coord, CylindricalCoordinateSystem rotationPoint, double rotationAngle)
    {
        coord.getLocalCoordinateSystemManager().
                rotateLocalCoordinateSystems(Collections.singletonList(coord),
                        new DoubleVector(new double[] {0, 0, 1}),
                        new NeoObjectVector(new Units[]{activeSim.noUnit,
                                activeSim.noUnit, activeSim.noUnit}), rotationAngle, rotationPoint);
    }
}
