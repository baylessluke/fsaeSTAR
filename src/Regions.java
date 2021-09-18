import star.base.neo.DoubleVector;
import star.base.neo.NeoObjectVector;
import star.common.*;
import star.flow.*;
import star.motion.BoundaryReferenceFrameSpecification;
import star.motion.ReferenceFrameOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/*
Sets up regions and boundary conditions. Also holds a few functions used by other macros for boundary conditions.
 */

public class Regions extends StarMacro {

    public void execute() {
        execute0();
    }

    private void execute0() {
        SimComponents activeSim = new SimComponents(getActiveSimulation());

        activeSim.activeSim.println("--- Setting up regions ---");

        //This defines the initial condition. Apparently setting to zero is bad? But I also don't like setting to the full freestream value, since that messes with the monitors in early iterations. Overall convergence time doesn't really change much.
        double[] initialVector = SimComponents.vectorRotate(activeSim.valEnv("Yaw"), SimComponents.vectorScale(activeSim.freestreamVal * 0.1, activeSim.foreAftDirection));

        //Need to swap out the old region for the new regions. I like swapping rather than modifying, since it's more repeatable and consistent for the macros to deal with. This is destructive.,
        activeSim.regionSwap();

        // Recreate the SimComponents object after region swap.
        activeSim = new SimComponents(getActiveSimulation());


        // Assign boundary conditions to freestream/domain block. There is a naming convection to this.
        setDomainBoundaries(activeSim);

        // Set up wheel boundary conditions
        setTyreRotation(activeSim);

        // Set up initial conditions
        activeSim.steadyStatePhysics.getInitialConditions().get(VelocityProfile.class).
                getMethod(ConstantVectorProfileMethod.class).getQuantity().setConstant(initialVector);
        activeSim.desPhysics.getInitialConditions().get(VelocityProfile.class).
                getMethod(ConstantVectorProfileMethod.class).getQuantity().setConstant(initialVector);


        // This can be made much cleaner by using vars for some of these object returns.
        // Some of these calls will need to be modified if coordinate systems change in the future.

        //Set up boundaries for the radiator inlets and outlets.
        if (activeSim.radInlet == null || activeSim.radOutlet == null || activeSim.domainRadInlet == null || activeSim.domainRadOutlet == null)
        {
            throw new IllegalStateException("Could not assign radiator surfaces. Did you split radiator surfaces?");
        }
        if (activeSim.fanInlet == null || activeSim.domainFanInlet == null || activeSim.fanOutlet == null || activeSim.domainFanOutlet == null)
        {
            throw new IllegalStateException("Could not assign fan surfaces. Did you split fan surfaces?");
        }
        activeSim.massFlowInterfaceInlet = activeSim.activeSim.getInterfaceManager().
                createBoundaryInterface(activeSim.domainRadInlet, activeSim.radInlet,
                        SimComponents.RAD_INLET_STRING);

        activeSim.massFlowInterfaceOutlet = activeSim.activeSim.getInterfaceManager().
                createBoundaryInterface(activeSim.domainRadOutlet, activeSim.radOutlet,
                        SimComponents.RAD_OUTLET_STRING);

        if (activeSim.dualRadFlag)
        {
            activeSim.dualMassFlowInterfaceInlet = activeSim.activeSim.getInterfaceManager().
                    createBoundaryInterface(activeSim.domainDualRadInlet, activeSim.dualRadInlet,
                            SimComponents.RAD_INLET_STRING);

            activeSim.dualMassFlowInterfaceOutlet = activeSim.activeSim.getInterfaceManager().
                    createBoundaryInterface(activeSim.domainDualRadOutlet, activeSim.dualRadOutlet,
                            SimComponents.RAD_OUTLET_STRING);
        }

        activeSim.fanInterfaceInlet = activeSim.activeSim.getInterfaceManager().createBoundaryInterface(activeSim.domainFanInlet, activeSim.fanInlet, SimComponents.FAN_INLET_STRING);
        activeSim.fanInterfaceOutlet = activeSim.activeSim.getInterfaceManager().createBoundaryInterface(activeSim.domainFanOutlet, activeSim.fanOutlet, SimComponents.FAN_OUTLET_STRING);
        if (activeSim.dualFanFlag)
        {
            activeSim.dualFanInterfaceInlet = activeSim.activeSim.getInterfaceManager().createBoundaryInterface(activeSim.domainDualFanInlet, activeSim.dualFanInlet, SimComponents.FAN_INLET_STRING);
            activeSim.dualFanInterfaceOutlet = activeSim.activeSim.getInterfaceManager().createBoundaryInterface(activeSim.domainDualFanOutlet, activeSim.dualFanOutlet, SimComponents.FAN_OUTLET_STRING);
        }

        //Assign viscous properties to the radiator regions.
        setRadiatorParams(activeSim, activeSim.radiatorRegion);

        if (activeSim.dualRadFlag) {
            setRadiatorParams(activeSim, activeSim.dualRadiatorRegion);
        }
    }

    //This handles assigning a fan curve csv file to the fan curve table in STAR, and assigns that table to the fan boundary (passed as a parameter)
    public void setUpFan(SimComponents activeSim, Region fanRegion, CylindricalCoordinateSystem fanAxis) {
        Collection<Boundary> fanRegionBounds = fanRegion.getBoundaryManager().getBoundaries();
        Boundary inletBound = null;
        Boundary outletBound = null;

        for (Boundary bound : fanRegionBounds) {
            String presName = bound.getPresentationName();
            if (bound instanceof InterfaceBoundary) {
                if (presName.contains(SimComponents.FAN_INLET_STRING))
                    inletBound = bound;
                else if (presName.contains(SimComponents.FAN_OUTLET_STRING))
                    outletBound = bound;
            }
        }

        if (inletBound == null || outletBound == null) {
            throw new IllegalStateException("Could not detect inlet or outlet boundary for the fan region. check SetUpFan in Regions.java");
        }

        if (activeSim.fanFlag) {
            fanRegion.getConditions().get(MomentumUserSourceOption.class).setSelected(MomentumUserSourceOption.Type.FAN);
            MomentumFanSource fanModel = fanRegion.getValues().get(MomentumFanSource.class);
            fanModel.setTable(activeSim.fan_curve_table);
            fanModel.setTableVolDot(SimComponents.volDot);
            fanModel.setUpstreamBoundary(inletBound);
            fanModel.setDownstreamBoundary(outletBound);
            fanModel.setTableP(SimComponents.delP);
            fanModel.getSolver().setIStart(10);
            fanModel.setCoordinateSystem(fanAxis);
        } else
        {
            fanRegion.getConditions().get(MomentumUserSourceOption.class).setSelected(MomentumUserSourceOption.Type.NONE);
        }
    }

    private void setTyreRotation(SimComponents activeSim) {

        //Calculate angular rotation rate for wheels based on freestream (vehicle speed) and provided tyre radii. (omega = v/r, need to have m/s and meters)
        double frontRotationRate = activeSim.freestreamVal / activeSim.frontTyreRadius;
        double rearRotationRate = activeSim.freestreamVal / activeSim.rearTyreRadius;
        double diffVelocity;
        //Set rotation rates to zero if the wt flag is enabled.
        if (activeSim.wtFlag) {
            frontRotationRate = 0;
            rearRotationRate = 0;
        }

        // If the sim is a cornering sim, you need to calculate the difference between inner and outer tyre rotation rates, assuming no slip.
        if (activeSim.corneringFlag)
            diffVelocity = velocityDifference(activeSim);
        else
            diffVelocity = 0;

        //It's important to make sure the entire wheel assembly is combined into a single entity in STAR, otherwise your tyre will rotate, but nothing in the wheel will rotate, which doesn't make sense.
        //Right now technically the uprights and at least parts of the control arms are rotating as well.......which might be something you want to fix at some point.
        for (Boundary wheelBound : activeSim.wheelBounds) {

            //Set the correct boundary type to the wheels.
            String boundName = wheelBound.getPresentationName();
            wheelBound.getConditions().get(WallSlidingOption.class).
                    setSelected(WallSlidingOption.Type.LOCAL_ROTATION_RATE);

            try {
                //This is the chunk that actually assigns the rotation rate to the wheels.
                if (boundName.contains("Front")) {
                    double rotationRate = frontRotationRate;
                    if (boundName.contains("Front Right"))
                    {
                        rotationRate = frontRotationRate - 0.5 * diffVelocity / activeSim.frontTyreRadius;
                    }
                    else if (boundName.contains("Front Left"))
                    {
                        rotationRate = frontRotationRate + 0.5 * diffVelocity / activeSim.frontTyreRadius;
                    }
                    activeSim.activeSim.println("Setting front tyre rotation rate to : " + rotationRate + " for " + boundName);
                    wheelBound.getValues().
                            get(ReferenceFrame.class).setCoordinateSystem(activeSim.frontWheelCoord);
                    wheelBound.getValues().get(WallRelativeRotationProfile.class).
                            getMethod(ConstantScalarProfileMethod.class).getQuantity().
                            setValue(rotationRate);
                } else if (boundName.contains("Rear")) {
                    double rotationRate = rearRotationRate;
                    if (boundName.contains("Rear Right"))
                    {
                        rotationRate = rearRotationRate - 0.5 * diffVelocity / activeSim.rearTyreRadius;
                    }
                    else if (boundName.contains("Rear Left"))
                    {
                        rotationRate = rearRotationRate + 0.5 * diffVelocity / activeSim.rearTyreRadius;
                    }
                    activeSim.activeSim.println("Setting rear tyre rotation rate to : " + + rotationRate + " for " + boundName);
                    wheelBound.getValues().get(ReferenceFrame.class).
                            setCoordinateSystem(activeSim.rearWheelCoord);
                    wheelBound.getValues().get(WallRelativeRotationProfile.class).
                            getMethod(ConstantScalarProfileMethod.class).getQuantity().
                            setValue(rotationRate);
                }
            } catch (Exception e) {
                activeSim.activeSim.println("Regions.java - Wheel boundary set-up failed");
            }
        }
    }

    //Use track width and angular velocity to figure out what the linear velocity difference is between the two tyres.
    private double velocityDifference(SimComponents activeSim)
    {
        double omega = activeSim.angularVelocity.getQuantity().evaluate();
        return omega * activeSim.trackWidth * 0.0254;
    }

    //Sets up boundary conditions for the domain boundaries. Ground, inlet, outlet, symmetry, symmetry, symmetry.
    private void setDomainBoundaries(SimComponents activeSim) {
        if (activeSim.corneringFlag)
        {
            setDomainBoundaries_Cornering(activeSim);
            return;
        }
        String yVal = String.valueOf(activeSim.calculateSideslip());
        activeSim.leftPlane.setBoundaryType(SymmetryBoundary.class);
        activeSim.symPlane.setBoundaryType(SymmetryBoundary.class);
        activeSim.topPlane.setBoundaryType(SymmetryBoundary.class);
        activeSim.fsInlet.setBoundaryType(InletBoundary.class);
        activeSim.fsInlet.getValues().get(VelocityMagnitudeProfile.class).
                getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("${" + SimComponents.FREESTREAM_PARAMETER_NAME + "}");
        activeSim.groundPlane.getConditions().get(WallSlidingOption.class).
                setSelected(WallSlidingOption.Type.VECTOR);
        if (activeSim.wtFlag)
            activeSim.groundPlane.getValues().get(WallRelativeVelocityProfile.class).
                    getMethod(ConstantVectorProfileMethod.class).getQuantity().setConstant(new double[]{0, 0, 0});
        else
            activeSim.groundPlane.getValues().get(WallRelativeVelocityProfile.class).
                    getMethod(ConstantVectorProfileMethod.class).getQuantity().setDefinition("[${Freestream}," + yVal + ", 0]");
        activeSim.fsOutlet.setBoundaryType(PressureBoundary.class);
    }

    //Same thing as the method above, except for a cornering case.
    private void setDomainBoundaries_Cornering(SimComponents activeSim){
        activeSim.leftPlane.setBoundaryType(SymmetryBoundary.class);
        activeSim.symPlane.setBoundaryType(SymmetryBoundary.class);
        activeSim.topPlane.setBoundaryType(SymmetryBoundary.class);
        activeSim.fsOutlet.setBoundaryType(OutletBoundary.class);
        activeSim.groundPlane.getConditions().get(ReferenceFrameOption.class).setSelected(ReferenceFrameOption.Type.LOCAL_FRAME);
        activeSim.groundPlane.getValues().get(BoundaryReferenceFrameSpecification.class).setReferenceFrame(activeSim.rotatingFrame);
        activeSim.fsInlet.setBoundaryType(InletBoundary.class);
        activeSim.fsInlet.getConditions().get(FlowDirectionOption.class).setSelected(FlowDirectionOption.Type.BOUNDARY_NORMAL);
        activeSim.fsInlet.getConditions().get(ReferenceFrameOption.class).setSelected(ReferenceFrameOption.Type.LOCAL_FRAME);
        activeSim.fsInlet.getValues().get(BoundaryReferenceFrameSpecification.class).setReferenceFrame(activeSim.rotatingFrame);
        activeSim.fsInlet.getValues().get(VelocityMagnitudeProfile.class).getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(0);
    }

    //Set up interfaces for a full car domain. Need to interface the left and right boundaries together, otherwise the domain will naturally straighten any yaw condition you set at the inlet.
    //Honestly, I don't fully understand what's going on with this function, or the exact sequence of events that led me to write it the way it's written. It would've been a good idea for me to comment this function when I wrote it, but that ship has sailed.
    //I do know it was very hard to get this to work reliably. there are a lot of edge cases that unravel here, especially when resuing a sim, or changing a sim from full car to half car or vice versa.
    public void yawInterfaces(SimComponents activeSim) {
        setTyreRotation(activeSim);
        double yawVal = activeSim.valEnv("yaw");
        double slip = activeSim.freestreamVal * Math.tan(Math.toRadians(yawVal));

        if (activeSim.fullCarFlag) {
            activeSim.activeSim.println("Setting boundary conditions for yaw");

            // I don't know why this chunk is important, but it is.
            if (activeSim.activeSim.getInterfaceManager().has(SimComponents.YAW_INTERFACE_NAME) && activeSim.activeSim.getInterfaceManager().getInterface(SimComponents.YAW_INTERFACE_NAME) instanceof BoundaryInterface)
            {
                activeSim.activeSim.println("Found yaw interface");
                activeSim.yawInterface = (BoundaryInterface) activeSim.activeSim.getInterfaceManager().getInterface(SimComponents.YAW_INTERFACE_NAME);
                activeSim.activeSim.getInterfaceManager().deleteInterface(activeSim.yawInterface);
                setDomainBoundaries(activeSim);  //This is to make sure we're always in a consistent and well-defined state, rather than having to deal with deviations from the assumptions.
            }

            if (activeSim.corneringFlag)
            {
                activeSim.leftPlane.setBoundaryType(InletBoundary.class);
                activeSim.leftPlane.getConditions().get(FlowDirectionOption.class).setSelected(FlowDirectionOption.Type.BOUNDARY_NORMAL);
                activeSim.leftPlane.getConditions().get(ReferenceFrameOption.class).setSelected(ReferenceFrameOption.Type.LOCAL_FRAME);
                activeSim.leftPlane.getValues().get(BoundaryReferenceFrameSpecification.class).setReferenceFrame(activeSim.rotatingFrame);
                activeSim.leftPlane.getValues().get(VelocityMagnitudeProfile.class).getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(0);
                activeSim.symPlane.setBoundaryType(InletBoundary.class);
                activeSim.symPlane.getConditions().get(FlowDirectionOption.class).setSelected(FlowDirectionOption.Type.BOUNDARY_NORMAL);
                activeSim.symPlane.getConditions().get(ReferenceFrameOption.class).setSelected(ReferenceFrameOption.Type.LOCAL_FRAME);
                activeSim.symPlane.getValues().get(BoundaryReferenceFrameSpecification.class).setReferenceFrame(activeSim.rotatingFrame);
                activeSim.symPlane.getValues().get(VelocityMagnitudeProfile.class).getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(0);
                activeSim.rotatingFrame.getTranslationVelocity().setComponents(0, -slip, 0);
                activeSim.rotatingFrame.getTranslationVelocity().setUnits(activeSim.ms);

                return;
            }

            //Keeping with the philosophy of deleting the existing components if they exist, and recreating them from scratch for consistency.
            activeSim.activeSim.println("Creating yaw interface");
            activeSim.yawInterface = activeSim.activeSim.getInterfaceManager().createBoundaryInterface(activeSim.leftPlane, activeSim.symPlane, SimComponents.YAW_INTERFACE_NAME);

            activeSim.yawInterface.setPresentationName(SimComponents.YAW_INTERFACE_NAME);
            activeSim.yawInterface.getTopology().setSelected(InterfaceConfigurationOption.Type.PERIODIC);               //I don't have the foggiest idea what interface topology is supposed to be for. this is some blatant plagiarism.

            //Set up the yaw condition at the inlet.
            activeSim.fsInlet.getValues().get(VelocityMagnitudeProfile.class).
                    getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(activeSim.freestreamVal / Math.cos(Math.toRadians(yawVal)));
            activeSim.fsInlet.getConditions().get(FlowDirectionOption.class).setSelected(FlowDirectionOption.Type.ANGLES);
            FlowAnglesProfile yawAngleControl = activeSim.fsInlet.getValues().get(FlowAnglesProfile.class);
            yawAngleControl.getMethod(ConstantAnglesProfileMethod.class).getQuantity().setUnits(activeSim.degs);
            yawAngleControl.getMethod(ConstantAnglesProfileMethod.class).getQuantity().setRotationAngles(new DoubleVector(new double[]{yawVal, 0, 0}));
            activeSim.activeSim.println("Yaw set to " + yawVal + " attempted");
        } else {
            activeSim.activeSim.println("Can't set boundary conditions for yaw with fullCarFlag set to false");
        }
    }

    //Set up viscous and inertial resistances for the radiators. There's a good article on Siemens' Steve portal (or whatever they're calling it now, there's a very solid chance the Steve portal no longer exists if you're reading this in the future) explaining how you can get radiator properties out of wind tunnel data for a given radiator.
    private void setRadiatorParams(SimComponents activeSim, Region radiatorRegion) {
        radiatorRegion.setRegionType(PorousRegion.class);
        PrincipalTensorProfileMethod radiatorTensor = radiatorRegion.getValues().get(PorousInertialResistance.class).
                getMethod(PrincipalTensorProfileMethod.class);
        PrincipalTensorProfileMethod radiatorViscousTensor = radiatorRegion.getValues().get(PorousViscousResistance.class).getMethod(PrincipalTensorProfileMethod.class);

        radiatorTensor.getYAxis().setCoordinateSystem(activeSim.radiatorCoord);
        radiatorTensor.getXAxis().setCoordinateSystem(activeSim.radiatorCoord);
        radiatorTensor.getProfile(0).
                getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(activeSim.radResBig);
        radiatorTensor.getProfile(1).
                getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("${Inertial Resistance}");
        radiatorTensor.getProfile(2).
                getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(activeSim.radResBig);

        radiatorViscousTensor.getYAxis().setCoordinateSystem(activeSim.radiatorCoord);
        radiatorViscousTensor.getXAxis().setCoordinateSystem(activeSim.radiatorCoord);
        radiatorViscousTensor.getProfile(0).
                getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(activeSim.radResBig);
        radiatorViscousTensor.getProfile(1).
                getMethod(ConstantScalarProfileMethod.class).getQuantity().setDefinition("${Viscous Resistance}");
        radiatorViscousTensor.getProfile(2).
                getMethod(ConstantScalarProfileMethod.class).getQuantity().setValue(activeSim.radResBig);
    }

    //Make sure all regions are set to the correct physics model.
    public static void setTurbulence(SimComponents activeSim) {
        Collection<Region> always_enabled_regions = Arrays.asList(
                activeSim.domainRegion,
                activeSim.radiatorRegion,
                activeSim.fanRegion
        );

        PhysicsContinuum selectedPhysics = activeSim.DESFlag ? activeSim.desPhysics : activeSim.steadyStatePhysics;

        for (Region reg: always_enabled_regions)
            reg.setPhysicsContinuum(selectedPhysics);

        if (activeSim.dualRadFlag)
            activeSim.dualRadiatorRegion.setPhysicsContinuum(selectedPhysics);

        if (activeSim.dualFanFlag)
            activeSim.dualFanRegion.setPhysicsContinuum(selectedPhysics);
    }

    //this is really important for PostProc. 2D PostProc is very slow if you don't reduce the total number of boundaries. This merges boundaries. This could be done just before meshing, but I don't like doing that since you lose flexibility with reports. This is safe to do just before 2D PostProc, so long as you understand that this function will destory the mesh.
    public void mergeBoundaries (SimComponents activeSim)
    {
        MeshManager meshManager = activeSim.activeSim.getMeshManager();
        Collection<Boundary> mergeBounds = new ArrayList<>();
        for (Boundary x : activeSim.partBounds)
        {
            mergeBounds.add(x);

            for (Collection<Boundary> coll : activeSim.partSpecBounds.values())
            {
                if (coll.contains(x))
                    mergeBounds.remove(x);
            }
            if (x.getBoundaryType() instanceof InvalidCellBoundary || x instanceof InterfaceBoundary || x.getPresentationName().toLowerCase().contains("fan") || x.getPresentationName().toLowerCase().contains("radiator"))
                mergeBounds.remove(x);
        }
        activeSim.activeSim.println("merging: " + mergeBounds);
        meshManager.combineBoundaries(new NeoObjectVector(mergeBounds.toArray()));
    }

    //Quick and dirty method to set up fans, and only fans when needed by run.java, and fan boundaries aren't already known.
    public void initFans(SimComponents activeSim)
    {
        setUpFan(activeSim, activeSim.fanRegion, activeSim.fanAxis);
        if (activeSim.dualFanFlag)
            setUpFan(activeSim, activeSim.dualFanRegion, activeSim.dualFanAxis);
    }
}
