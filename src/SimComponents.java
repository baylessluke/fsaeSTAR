/*
    Class to instantiate frequently used simulation components. Largely to act as a support library / API for other
    macros to interact with objects in our simulation environment, and handle basic simulation operations too menial
    for a full macro.

    Raunaq Kumaran, January 2019
 */

import star.base.neo.NeoObjectVector;
import star.base.report.MaxReport;
import star.base.report.Report;
import star.cadmodeler.SolidModelPart;
import star.common.*;
import star.flow.AccumulatedForceTable;
import star.meshing.*;
import star.motion.UserRotatingAndTranslatingReferenceFrame;
import star.screenplay.Screenplay;
import star.surfacewrapper.SurfaceWrapperAutoMeshOperation;
import star.vis.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class SimComponents {

    //Some string constants. This is something I started doing later on, and haven't done for every string.
    //I keep flip-flopping between whether or not this is a good idea or not, which means the final result of string management is pretty poor. Sorry.

    public static final String YAW_INTERFACE_NAME = "Yaw interface";
    public static final String USER_FREESTREAM = "User Freestream";
    public static final String USER_YAW = "User Yaw";
    public static final String USER_FRONT_RIDE_HEIGHT = "User Front Ride Height";
    public static final String USER_REAR_RIDE_HEIGHT = "User Rear Ride Height";
    public static final String SIDESLIP = "User Sideslip";
    public static final String CONFIGSIDESLIP = "sideslip";
    public static final String FREESTREAM_PARAMETER_NAME = "Freestream";
    public static final String DOMAIN_REGION = "Subtract";
    public static final String RADIATOR_REGION = "Radiator";
    public static final String DUAL_RADIATOR_REGION = "Radiator 2";
    public static final String FRONT_LEFT = "Front Left";
    public static final String FRONT_RIGHT = "Front Right";
    public static final String REAR_LEFT = "Rear Left";
    public static final String REAR_RIGHT = "Rear Right";
    public static final String USER_STEERING = "User Steering";
    public static final String STEERING = "steering";
    public static final String CORNERING = "cornering";
    public static final String USER_CORNERING_RADIUS = "User Cornering Radius";
    public static final String ANGULAR_VELOCITY = "Angular Velocity";
    public static final String DOMAIN_AXIS = "Domain_Axis";
    public static final String ROTATING = "Rotating";
    public static final String FAN_CURVE_CSV_FN = "fan_curve.csv";
    public static final String SUBTRACT_NAME = "Subtract";
    public static final String[] AERO_PREFIXES = {"RW", "FW", "UT", "EC", "MOUNT", "SW", "FC"};                                       //These prefixes will be used to decide what an aero component is.
    public static final String[] LIFT_GENERATOR_PREFIXES = {"RW", "FW", "UT", "SW", "FC"};                                            //These prefixes generate lift. Aero surface wrap control needs to know this.
    public static final String[] NON_AERO_PREFIXES = {"CFD", "DONTGIVE", "NS"};                                                       //These are prefixes for non-aero parts. Everything other than aero and tyres must have one of these prefixes.
    public static final String[] WHEEL_NAMES = {FRONT_LEFT, FRONT_RIGHT, REAR_LEFT, REAR_RIGHT};                                      //Names for wheels. Must be exact.
    public static final String FREESTREAM_PREFIX = "Freestream";                                                                      //This is the domain. Good way to make sure the macros filter out domain surfaces later on. Just make sure no actual parts include the term "freestream"
    public static final String FREESTREAM_CORNERING = "Freestream_C";
    public static final String RADIATOR_NAME = "CFD_RADIATOR";
    public static final String DUAL_RADIATOR_NAME = "CFD_DUAL_RADIATOR";
    public static final String SURFACE_WRAPPER = "Surface wrapper";
    public static final String SURFACE_WRAPPER_PPM = "Surface wrapper PPM";
    public static final String AERO_CONTROL = "Aero Control";
    public static final String AERO_CONTROL_PPM = "Aero Control";
    public static final String USER_ROLL = "User Roll";
    public static final String CONFIG_ROLL = "roll";
    public static final String LIFT_COEFFICIENT_PLOT = "Lift Coefficient Monitor Plot";
    public static final String RAD_INLET_STRING = "Inlet interface";
    public static final String RAD_OUTLET_STRING = "Outlet interface";
    public static final String DUAL_RAD_INLET_STRING = "Dual inlet interface";
    public static final String DUAL_RAD_OUTLET_STRING = "Dual outlet interface";
    public static final String FAN_REGION = "Fan";
    public static final String FAN_INLET_STRING = "Fan Inlet";
    public static final String FAN_OUTLET_STRING = "Fan Outlet";
    public static final String DUAL_FAN_REGION = "Dual Fan";
    public static final String FAN_PART_STRING = "CFD_FAN";
    public static final String DUAL_FAN_PART_STRING = "CFD_DUAL_FAN";
    public static final String volDot = "m^3/s";
    public static final String delP = "dP";
    public static final String noFan = "no_fan";
    public static final String aeroParent = "CFD_AERODYNAMICS_830250079";

    //A bunch of declarations. Don't read too much into the access modifiers, they're not a big deal for a project like this.
    // I'm not going to comment all of these. there are way too many (future improvement suggestion: use fewer variables)

    //Version check. An easy way to make sure the sim and the macros are the same version. Throw an error at the beginning, rather than an uncaught NPE later.
    // This needs to match the version parameter in STAR. This is really just a way so people don't bug me with macro problems that can be solved with pulling the correct branch/tag
    private final double version = 5.0;

    // Simulation object
    public Simulation activeSim;

    public Collection<GeometryPart> aeroParts;
    public Collection<GeometryPart> nonAeroParts;
    public Collection<GeometryPart> wheels;
    private final Collection<GeometryPart> liftGenerators;
    public Collection<Boundary> domainBounds;
    public Collection<Boundary> radBounds;
    public Collection<Boundary> freestreamBounds;
    public Collection<Boundary> partBounds;
    public Collection<Boundary> wheelBounds;
    public Collection<Boundary> fanBounds;
    public Collection<Boundary> dualFanBounds;
    public Map<String, Collection<Boundary>> partSpecBounds;
    private final Collection<GeometryPart> allParts;
    public GeometryPart fanPart;
    public GeometryPart dualFanPart = null;

    //Double arrays to hold ranges for scenes and plane section sweeps. Limits are in inches, and control how far the cross sections will go. Pressures are Cps.
    public double[] profileLimits = {-29, 29};
    public double[] aftForeLimits = {-70, 55};
    public double[] utLimits = {0.35, 10};
    public double[] topBottomLimits = {10, 60};

    //These define the vector direction for cross section scenes.
    public double[] foreAftDirection = {1, 0, 0};
    public double[] profileDirection = {0, 1, 0};
    public double[] topBottomDirection = {0, 0, 1};


    //Some objects for reports and plots
    public Collection<Report> reports;
    public String massFlowRepName;
    public String pitchRepName;
    public PointPart point;
    public XyzInternalTable repTable;
    public Map<String, AccumulatedForceTable> forceTables;
    public Collection<StarPlot> plots;

    //Units
    public Units noUnit;
    public Units inches;
    public Units meters;
    public Units degs;
    public Units ms;

    //Vehicle dimensions radii
    public double frontTyreRadius = 0.228599;           //meters
    public double rearTyreRadius = 0.228599;            //meters
    public double wheelBase = 61;                       //inches (i know, sorry)
    public double trackWidth = 47;                      //inches again (sorry)
    public double radResBig = 10000;                    //Pretty sure this can be any big number.

    // Subtract object
    public SubtractPartsOperation subtract;

    //Parameters for user flow characteristics
    private ScalarGlobalParameter freestreamParameter;
    private ScalarGlobalParameter corneringRadiusParameter;
    private ScalarGlobalParameter userYaw;
    private ScalarGlobalParameter userFreestream;
    private ScalarGlobalParameter frontRide;
    private ScalarGlobalParameter rearRide;
    private ScalarGlobalParameter sideSlip;
    private ScalarGlobalParameter userSteering;
    private ScalarGlobalParameter rollParameter;
    public ScalarGlobalParameter angularVelocity;

    //Flags to track sim status
    public boolean fullCarFlag;             //True if full car domain detected
    public boolean wtFlag;                  //True if user wants WT (no ground velocity, no tyre rotation)
    public boolean DESFlag;

    //Stopping criteria
    public MonitorIterationStoppingCriterion maxVel;
    public int maxSteps;
    public MonitorIterationStoppingCriterion maxStepStop;
    public MaxReport maxVelocity;
    public AbortFileStoppingCriterion abortFile;    //Don't think we're using this for anything right now.
    public UpdateEvent monitorWaypoint;             //Only for transient.
    public boolean convergenceCheck;

    // Physics
    public PhysicsContinuum steadyStatePhysics;
    public PhysicsContinuum desPhysics;
    public double freestreamVal;
    public double corneringRadius;
    public boolean dualRadFlag;
    public boolean dualFanFlag;
    public boolean fanFlag;
    public boolean corneringFlag;
    public FileTable fan_curve_table;

    // Regions
    public Region radiatorRegion;
    public Region dualRadiatorRegion;
    public Region domainRegion;
    public Region fanRegion;
    public Region dualFanRegion;
    public BoundaryInterface massFlowInterfaceInlet;
    public BoundaryInterface massFlowInterfaceOutlet;
    public BoundaryInterface dualMassFlowInterfaceInlet;
    public BoundaryInterface dualMassFlowInterfaceOutlet;
    public BoundaryInterface fanInterfaceInlet;
    public BoundaryInterface dualFanInterfaceInlet;
    public BoundaryInterface fanInterfaceOutlet;
    public BoundaryInterface dualFanInterfaceOutlet;
    public CylindricalCoordinateSystem frontWheelCoord;
    public CylindricalCoordinateSystem rearWheelCoord;
    public CylindricalCoordinateSystem frontWheelSteering;
    public CylindricalCoordinateSystem domainAxis;
    public UserRotatingAndTranslatingReferenceFrame rotatingFrame;
    public Boundary fsInlet;                            //fs refers to freestream here
    public Boundary leftPlane;
    public Boundary groundPlane;
    public Boundary fsOutlet;
    public Boundary symPlane;
    public Boundary topPlane;
    public CartesianCoordinateSystem radiatorCoord;
    public CartesianCoordinateSystem rollAxis;          //I don't think I'm using this for anything right now. But there's some amount of code in here that allows for roll adjustments. This is necessary for that.
    public CartesianCoordinateSystem dualRadCoord;
    public BoundaryInterface yawInterface;              //This is necessary for doing yaw correctly.
    private Collection<Boundary> dualRadBounds;
    private final Collection<Boundary> domainRadBounds;
    public Boundary dualRadInlet;
    public Boundary dualRadOutlet;
    public Boundary radInlet;            // There are two sets of these corresponding to the two regions. Need these for interfacing
    public Boundary radOutlet;
    public Boundary domainRadInlet;
    public Boundary domainRadOutlet;
    public Boundary domainDualRadInlet;
    public Boundary domainDualRadOutlet;
    public Boundary radFanBound;
    public Boundary dualRadFanBound;
    public Boundary fanInlet;
    public Boundary fanOutlet;
    public Boundary dualFanInlet;
    public Boundary dualFanOutlet;
    public Boundary domainDualFanInlet;
    public Boundary domainDualFanOutlet;
    public Boundary domainFanInlet;
    public Boundary domainFanOutlet;

    //Scenes and displayers
    public PlaneSection crossSection;
    public Scene scene2D;
    public Scene scene3D;
    public String separator;
    public FvRepresentation finiteVol;
    public String dir;
    public String simName;
    public Scene meshScene;
    public Screenplay profile;

    //Mesh controls
    public AutoMeshOperation autoMesh;
    public SurfaceCustomMeshControl frontWingControl;
    public SurfaceCustomMeshControl rearWingControl;
    public SurfaceCustomMeshControl sideWingControl;
    public SurfaceCustomMeshControl undertrayControl;
    public SurfaceCustomMeshControl bodyworkControl;
    public SurfaceCustomMeshControl groundControl;
    public SurfaceCustomMeshControl freestreamControl;
    public VolumeCustomMeshControl radiatorControlVolume;
    public VolumeCustomMeshControl volControlCar;
    public VolumeCustomMeshControl volControlWake;
    public VolumeCustomMeshControl farWakeControl;
    public SurfaceCustomMeshControl radiatorControlSurface;
    public VolumeCustomMeshControl volControlWingWake;
    public VolumeCustomMeshControl volControlRearWing;
    public VolumeCustomMeshControl volControlFrontWing;
    public VolumeCustomMeshControl volControlUnderbody;
    public MeshOperationPart subtractPart;
    public SimpleBlockPart domain;
    public SolidModelPart domain_c;
    public SurfaceWrapperAutoMeshOperation surfaceWrapOperation;
    public SurfaceWrapperAutoMeshOperation surfaceWrapOperationPPM;
    public SurfaceCustomMeshControl aeroSurfaceWrapper;
    public SurfaceCustomMeshControl aeroSurfaceWrapperPPM;
    public GeometryPart dualRadPart = null;
    public GeometryPart radPart;
    public GeometryPart volumetricWake;
    public GeometryPart volumetricCar;
    public GeometryPart volumetricRearWing;
    public GeometryPart volumetricFrontWing;
    public GeometryPart volumetricUnderbody;
    public GeometryPart volumetricWingWake;
    public GeometryPart farWakePart;

    // Constructor
    public SimComponents(Simulation inputSim) {

        long startTime = System.currentTimeMillis();

        // Initialize simulation
        activeSim = inputSim;

        //Blow up if it's the wrong version
        checkVersion();

        //Define user parameters
        parameters();

        // Units
        noUnit = activeSim.getUnitsManager().getObject("");
        inches = activeSim.getUnitsManager().getObject("in");
        degs = activeSim.getUnitsManager().getObject("deg");
        meters = activeSim.getUnitsManager().getObject("m");
        ms = activeSim.getUnitsManager().getObject("m/s");

        // Initialize surface wrappers
        surfaceWrapOperation = ((SurfaceWrapperAutoMeshOperation) activeSim.get(MeshOperationManager.class).getObject(SURFACE_WRAPPER));
        surfaceWrapOperationPPM = ((SurfaceWrapperAutoMeshOperation) activeSim.get(MeshOperationManager.class).getObject(SURFACE_WRAPPER_PPM));
        aeroSurfaceWrapper = (SurfaceCustomMeshControl) surfaceWrapOperation.getCustomMeshControls().getObject(AERO_CONTROL);
        aeroSurfaceWrapperPPM = (SurfaceCustomMeshControl) surfaceWrapOperationPPM.getCustomMeshControls().getObject(AERO_CONTROL_PPM);


        // Part management. Get an object to hold all parts.
        allParts = activeSim.getGeometryPartManager().getParts();

        //Start filtering parts into categories
        aeroParts = new ArrayList<>();
        nonAeroParts = new ArrayList<>();
        wheels = new ArrayList<>();
        liftGenerators = new ArrayList<>();
        //This does all the filtering.
        for (GeometryPart prt : allParts) {
            String prtName = prt.getPresentationName();
            for (String prefix : AERO_PREFIXES) {
                if (prtName.startsWith(aeroParent)) {
                    aeroParts.add(prt);
                }
            }
            for (String prefix : NON_AERO_PREFIXES) {
                if (prtName.startsWith(prefix))
                    nonAeroParts.add(prt);
            }
            for (String prefix : WHEEL_NAMES) {
                if (prtName.startsWith(prefix))
                    wheels.add(prt);
            }
            for (String prefix : LIFT_GENERATOR_PREFIXES) {
                if (prtName.startsWith(prefix)) {
                    liftGenerators.add(prt);
                }
            }
            if (prtName.startsWith(RADIATOR_NAME))
                radPart = prt;
            if (prtName.startsWith(DUAL_RADIATOR_NAME))
                dualRadPart = prt;
            if (prtName.startsWith(FAN_PART_STRING))
                fanPart = prt;
            if (prtName.startsWith(DUAL_FAN_PART_STRING))
                dualFanPart = prt;

        }
        dualRadFlag = dualRadPart != null;     //Sets dual rad flag to true if dual rad part has a part assigned to it
        dualFanFlag = dualFanPart != null;     //Sets dual fan flag to true if dual fan part has a part assigned to it
        // Set up radiator regions
        try {
            domainRegion = assignRegion(DOMAIN_REGION);
            radiatorRegion = assignRegion(RADIATOR_REGION);
            fanRegion = assignRegion(FAN_REGION);
            if (dualRadFlag)
                dualRadiatorRegion = assignRegion(DUAL_RADIATOR_REGION);
            if (dualFanFlag)
                dualFanRegion = assignRegion(DUAL_FAN_REGION);
        } catch (Exception e) {
            activeSim.println("SimComponents.java - Couldn't find/create domain or radiator region");
        }


        // Extract radiator boundaries from radiator regions.
        domainBounds = Objects.requireNonNull(domainRegion).getBoundaryManager().getBoundaries();
        radBounds = new ArrayList<>();
        dualRadBounds = new ArrayList<>();
        fanBounds = new ArrayList<>();
        dualFanBounds = new ArrayList<>();
        radBounds = Objects.requireNonNull(radiatorRegion).getBoundaryManager().getBoundaries();
        fanBounds = Objects.requireNonNull(fanRegion).getBoundaryManager().getBoundaries();
        if (dualRadFlag)
            dualRadBounds = Objects.requireNonNull(dualRadiatorRegion).getBoundaryManager().getBoundaries();
        if (dualFanFlag)
            dualFanBounds = Objects.requireNonNull(dualFanRegion).getBoundaryManager().getBoundaries();
        // Takes all boundaries, filters them into freestream, parts, and wheels.
        freestreamBounds = new ArrayList<>();
        wheelBounds = new ArrayList<>();
        partBounds = new ArrayList<>();
        partSpecBounds = new HashMap<>();
        domainRadBounds = new ArrayList<>();
        boundarySet();

        // Set up coordinate systems
        activeSim.getCoordinateSystemManager().getLabCoordinateSystem();

        domainCatch();
        setupCoordinates();

        // Set up scenes, representations, and views.

        sceneSetup();


        // Set up subtract
        subtract = (SubtractPartsOperation) activeSim.get(MeshOperationManager.class).getObject(SUBTRACT_NAME);
        mesherSetup();


        // Set up reports
        reports = activeSim.getReportManager().getObjects();
        pitchRepName = "Pitch Moment Coefficient";
        massFlowRepName = "Radiator Mass Flow";
        String pointName = "Point";
        point = (PointPart) activeSim.getPartManager().getObject(pointName);
        repTable = (XyzInternalTable) activeSim.getTableManager().getTable("Reports table");
        // THESE KEYS MUST MATCH THE CONVENTION USED IN AERO PREFIXES
        forceTables = new HashMap<>();
        forceTables.put("FW", (AccumulatedForceTable) activeSim.getTableManager().getTable("FW Force Histogram"));
        forceTables.put("RW", (AccumulatedForceTable) activeSim.getTableManager().getTable("RW Force Histogram"));


        // Miscellaneous constructor things
        subtractPart = (MeshOperationPart) activeSim.get(SimulationPartManager.class).getPart(SUBTRACT_NAME);

        // Plots
        plots = activeSim.getPlotManager().getPlots();

        //Define domain sizes
        if (corneringFlag)
            fullCarFlag = true;
        else
            fullCarFlag = domainSizing();

        if (!fullCarFlag)
            profileLimits[1] = 0;

        //Set up fan flag and table
        fanFlag = boolEnv("FAN");
        fan_curve_table = (FileTable) activeSim.getTableManager().getTable("fan_table_csv");

        //Set physics objects
        physicsSet();

        //No autosave
        activeSim.getSimulationIterator().getAutoSave().getStarUpdate().setEnabled(false);
        activeSim.getSimulationIterator().getAutoSave().setAutoSaveMesh(false);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        activeSim.println("Time taken to generate SimComponents : " + totalTime + " ms");


    }


    //Assigns user parameters in the sim file to their associated java objects. Makes it easier to refer to them later

    private void parameters()
    {
        userFreestream = (ScalarGlobalParameter) activeSim.get(GlobalParameterManager.class).getObject(USER_FREESTREAM);
        freestreamParameter = (ScalarGlobalParameter) activeSim.get(GlobalParameterManager.class).getObject(FREESTREAM_PARAMETER_NAME);
        userYaw = (ScalarGlobalParameter) activeSim.get(GlobalParameterManager.class).getObject(USER_YAW);
        frontRide = (ScalarGlobalParameter) activeSim.get(GlobalParameterManager.class).getObject(USER_FRONT_RIDE_HEIGHT);
        rearRide = (ScalarGlobalParameter) activeSim.get(GlobalParameterManager.class).getObject(USER_REAR_RIDE_HEIGHT);
        sideSlip = (ScalarGlobalParameter) activeSim.get(GlobalParameterManager.class).getObject(SIDESLIP);
        userSteering = (ScalarGlobalParameter) activeSim.get(GlobalParameterManager.class).getObject(USER_STEERING);
        corneringRadiusParameter = (ScalarGlobalParameter) activeSim.get(GlobalParameterManager.class).getObject(USER_CORNERING_RADIUS);
        angularVelocity = (ScalarGlobalParameter) activeSim.get(GlobalParameterManager.class).getObject(ANGULAR_VELOCITY);
        rollParameter = (ScalarGlobalParameter) activeSim.get(GlobalParameterManager.class).getObject(USER_ROLL);
    }

    //Sets up boundary conditions. It's generally a good idea to avoid touching things (especially boundaries) when you can avoid it.
    private void boundarySet() {

        //Iterates through every boundary in domainBounds and checks for prexies.
        for (Boundary bound : domainBounds) {
            int partFlag = 0;                                      // Helps with if-logic to look for parts
            String boundName = bound.getPresentationName();

            //check for domain
            if (boundName.contains(FREESTREAM_PREFIX)) {
                freestreamBounds.add(bound);
                partFlag = 1;
            }
            //check for wheels
            else {
                for (String wheelName : WHEEL_NAMES) {
                    if (boundName.contains(wheelName)) {
                        wheelBounds.add(bound);
                        partFlag = 1;
                    }
                }
            }
            // if it isn't a domain or a wheel, it's a "part", and gets added to part bounds.
            if (partFlag == 0) {
                partBounds.add(bound);
            }
        }


        //Get inlet and outlet radiator boundaries in the domain region for reports. If it fails here, it's because the rad surfaces haven't been split
        for (Boundary bound : partBounds) {
            String boundName = bound.getPresentationName();
            if (boundName.contains("Inlet")) {
                if (boundName.contains(RADIATOR_NAME))
                    domainRadInlet = bound;
                if (boundName.contains(DUAL_RADIATOR_NAME))
                    domainDualRadInlet = bound;
            }
            if (boundName.contains("Outlet")) {
                if (boundName.contains(RADIATOR_NAME))
                    domainRadOutlet = bound;
                if (boundName.contains(DUAL_RADIATOR_NAME))
                    domainDualRadOutlet = bound;
            }
            if (boundName.contains(FAN_PART_STRING))
            {
                if (boundName.contains(FAN_INLET_STRING))
                    domainFanInlet = bound;
                if (boundName.contains(FAN_OUTLET_STRING))
                    domainFanOutlet = bound;
            }
            if (boundName.contains(DUAL_FAN_PART_STRING))
            {
                if (boundName.contains(FAN_INLET_STRING))
                    domainDualFanInlet = bound;
                if (boundName.contains(FAN_OUTLET_STRING))
                    domainDualFanOutlet = bound;
            }

            //Positively select aero parts, and throw them into partSpecBounds

            for (String prefix : AERO_PREFIXES) {
                if (boundName.contains(prefix) && boundName.contains(aeroParent))      // Janky code so CFD_SUSPENSION doesn't trigger the NS prefix.
                {
                    Collection<Boundary> temp = new ArrayList<>();
                    //Part spec bounds keys based on prefix, helps when setting up reports
                    if (partSpecBounds.containsKey(prefix))
                        temp = partSpecBounds.get(prefix);

                    temp.add(bound);
                    partSpecBounds.put(prefix, temp);
                }
            }
        }

        //This is for the radiator region now.
        for (Boundary bound : radBounds) {
            String boundName = bound.getPresentationName();
            if (boundName.contains("Inlet"))
                radInlet = bound;
            if (boundName.contains("Outlet"))
                radOutlet = bound;
        }

        //Now for the other radiator.
        for (Boundary bound : dualRadBounds) {
            String boundName = bound.getPresentationName();
            if (boundName.contains("Inlet"))
                dualRadInlet = bound;
            if (boundName.contains("Outlet"))
                dualRadOutlet = bound;
        }
        //now for fans
        for (Boundary bound : fanBounds){
            String boundName = bound.getPresentationName();
            if (boundName.contains(FAN_INLET_STRING))
                fanInlet = bound;
            else if (boundName.contains(FAN_OUTLET_STRING))
                fanOutlet = bound;
        }

        //do it again for the other fan
        for (Boundary bound: dualFanBounds)
        {
            String boundName = bound.getPresentationName();
            if (boundName.contains(FAN_INLET_STRING))
                dualFanInlet = bound;
            else if (boundName.contains(FAN_OUTLET_STRING))
                dualFanOutlet = bound;
        }

        //Filter out freestream boundaries to make it easier to set up boundary conditions later.
        for (Boundary fsBound : freestreamBounds)
        {
            //The check for YAW_INTERFACE is so i can keep using String.contains.
            String boundName = fsBound.getPresentationName();
            if (boundName.contains("Left") && !boundName.contains(SimComponents.YAW_INTERFACE_NAME))
                leftPlane = fsBound;
            else if (boundName.contains("Symmetry") && !boundName.contains(SimComponents.YAW_INTERFACE_NAME))
                symPlane = fsBound;
            else if (boundName.contains("Top"))
                topPlane = fsBound;
            else if (boundName.contains("Inlet"))
                fsInlet = fsBound;
            else if (boundName.contains("Ground"))
                groundPlane = fsBound;
            else if (boundName.contains("Outlet"))
                fsOutlet = fsBound;
        }
    }

    //Sets up continuua and populates the appropriate stopping criteria. The stopping criteria are a function of steady vs transient. It's been a while since I've used this for transient, I'm not sure if the transient code still works.
    private void physicsSet() {
        tagContinua();

        // Flags
        freestreamVal = valEnv("freestream");
        corneringRadius = valEnv(CORNERING);
        DESFlag = boolEnv("DES");
        wtFlag = boolEnv("windTunnel");
        setFreestreamParameterValue();

        //Stopping criteria
        maxSteps = (int) valEnv("maxSteps");
        maxStepStop = (MonitorIterationStoppingCriterion) activeSim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Iterations");
        maxVel = (MonitorIterationStoppingCriterion) activeSim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Maximum Velocity Monitor Criterion");
        maxVelocity = (MaxReport) activeSim.getReportManager().getReport("Maximum Velocity");
        abortFile = (AbortFileStoppingCriterion) activeSim.getSolverStoppingCriterionManager().getSolverStoppingCriterion("Stop File");
        monitorWaypoint = activeSim.getUpdateEventManager().getUpdateEvent("Monitor Waypoint");
        convergenceCheck = boolEnv("CONV_SC");

    }

    public void setFreestreamParameterValue() {
        freestreamParameter.getQuantity().setValue(freestreamVal);
        corneringRadiusParameter.getQuantity().setValue(corneringRadius);
    }

    //This is assigning the continuua objects in the sim to their java objects.
    private void tagContinua() {
        // Define physics block
        desPhysics = (PhysicsContinuum) activeSim.getContinuumManager().getContinuum("DES");

        //If there isn't a continuum named steady state, it will default to a continuum named S-a physics. This logic was largely for backwards compatibility, and could probably be removed.
        if (activeSim.getContinuumManager().has("Steady state"))
            steadyStatePhysics = (PhysicsContinuum) activeSim.getContinuumManager().getContinuum("Steady state");
        else
        {
            // I don't know if RuntimeException is the right exception class to throw. It probably isn't, but it gets the job done.
            throw new RuntimeException("No physics continuum found for steady state. Check physicsSet() in SimComponents.java for logic");
        }
    }

    //Assigns the freestream domain in the sim to its java object. Doesn't throw a killer exception. Could probably be modified to throw one. It's very unlikely the macro is going to get very far without a freestream anyway.
    private void domainCatch() {

        if (activeSim.get(SimulationPartManager.class).has(FREESTREAM_PREFIX))
        {
            domain = (SimpleBlockPart) activeSim.get(SimulationPartManager.class).getPart(FREESTREAM_PREFIX);
            corneringFlag = false;
            activeSim.println("Straight domain detected");
        }
        else if (activeSim.get(SimulationPartManager.class).has(FREESTREAM_CORNERING))
        {
            domain_c = (SolidModelPart) activeSim.get(SimulationPartManager.class).getPart(FREESTREAM_CORNERING);
            corneringFlag = true;
            fullCarFlag = true;
            activeSim.println("Cornering domain detected");
        }
        else
            throw new RuntimeException("Could not find a domain. Check the domainCatch() method in SimComponents.java");

    }

    //Returns true for full car. False for half car. Based purely on whether or not the y-coordinate of the domain block extends beyong positive +0.5 meters. PLEASE KEEP USING METERS.
    private boolean domainSizing() {

        if (domain == null)
            return true;
        double[] domainCorner = domain.getCorner1().evaluate().toDoubleArray();
        if (domainCorner[1] > 0.5) {
            activeSim.println("Full car domain detected");
            return true;
        } else {
            activeSim.println("Half car domain detected");
            return false;
        }
    }


    //Populate mesh objects with their STAR objects. This function doesn't do any processing, it's just object initialization.
    private void mesherSetup() {
        // Set up mesher
        try {

            // Get volumetric blocks
            SimulationPartManager partMan = activeSim.get(SimulationPartManager.class);
            volumetricCar = partMan.getPart("Volumetric Control Car");
            volumetricWake = partMan.getPart("Volumetric Control Wake");
            volumetricFrontWing = partMan.getPart("Front Wing Control");
            volumetricRearWing = partMan.getPart("Rear Wing Control");
            volumetricUnderbody = partMan.getPart("Underbody Control");
            volumetricWingWake = partMan.getPart("Rear Wing Wake");
            farWakePart = partMan.getPart("Far wake");


            // Get mesher
            autoMesh = (AutoMeshOperation) activeSim.get(MeshOperationManager.class).getObject("Automated Mesh");

            // Get custom controls

            CustomMeshControlManager getCustomControl = autoMesh.getCustomMeshControls();
            groundControl = (SurfaceCustomMeshControl) getCustomControl.getObject("Surface Control Ground");
            freestreamControl = (SurfaceCustomMeshControl) getCustomControl.getObject("Surface Control Freestream");
            rearWingControl = (SurfaceCustomMeshControl) getCustomControl.getObject("Surface Control Rear Wing");
            frontWingControl = (SurfaceCustomMeshControl) getCustomControl.getObject("Surface Control Front Wing");
            bodyworkControl = (SurfaceCustomMeshControl) getCustomControl.getObject("Surface Control Bodywork");
            sideWingControl = (SurfaceCustomMeshControl) getCustomControl.getObject("Surface Control Side Wing");
            undertrayControl = (SurfaceCustomMeshControl) getCustomControl.getObject("Surface Control Undertray");
            volControlWake = (VolumeCustomMeshControl) getCustomControl.getObject("Volumetric Control Wake");
            volControlCar = (VolumeCustomMeshControl) getCustomControl.getObject("Volumetric Control Car");
            volControlWingWake = (VolumeCustomMeshControl) getCustomControl.getObject("Volumetric Control Rear Wing Wake");
            volControlRearWing = (VolumeCustomMeshControl) getCustomControl.getObject("Volumetric Control Rear Wing");
            volControlFrontWing = (VolumeCustomMeshControl) getCustomControl.getObject("Volumetric Control Front Wing");
            volControlUnderbody = (VolumeCustomMeshControl) getCustomControl.getObject("Volumetric Control Underbody");
            farWakeControl = (VolumeCustomMeshControl) getCustomControl.getObject("Volumetric Control Far Wake");
            radiatorControlVolume = (VolumeCustomMeshControl) getCustomControl.getObject("Volumetric Control Radiator");
            radiatorControlSurface = (SurfaceCustomMeshControl) getCustomControl.getObject("Surface Control Radiator");
        } catch (Exception e) {
            activeSim.println(this.getClass().getName() + " - Mesh initializer error");
        }
    }

    public void clearMesh() {
        activeSim.getMeshPipelineController().clearGeneratedMeshes();
    }

    private void sceneSetup() {
        try {
            crossSection = (PlaneSection) activeSim.getPartManager().getObject("Plane Section");
            scene2D = activeSim.getSceneManager().getScene("2D scenes");
            scene2D.setMeshOverrideMode(SceneMeshOverride.USE_DISPLAYER_PROPERTY);
            scene3D = activeSim.getSceneManager().getScene("3D scenes");
            meshScene = activeSim.getSceneManager().getScene("Mesh");
            separator = File.separator;
            dir = activeSim.getSessionDir();
            simName = activeSim.getPresentationName();
            if (activeSim.getRepresentationManager().has("Volume Mesh"))
                finiteVol = (FvRepresentation) activeSim.getRepresentationManager().getObject("Volume Mesh");
        } catch (Exception e) {
            activeSim.println(this.getClass().getName() + " - Scene or displayer lookup failed, or volume mesh not found");
        }
    }

    //Scales a vector with a scale.
    //example: vectorScale(2, [1, 1, 1]) = [2, 2, 2]
    public static double[] vectorScale(double scalar, double[] vect) {
        vect = vect.clone();
        for (int i = 0; i < vect.length; i++)
            vect[i] *= scalar;

        return vect;
    }

    // I don't like this function, but it's a simple one. There's got to be a cleaner way to do this but, this works for now.
    public double valEnv(String env) {
        if (System.getenv(env) != null && !System.getenv(env).isEmpty())
            return Double.parseDouble(System.getenv(env));
        else if (env.equals("freestream"))
        {
            return userFreestream.getQuantity().getRawValue();
        }
        else if (env.equals("yaw"))
        {
            return userYaw.getQuantity().getRawValue();
        }
        else if (env.equals("frh"))
        {
            return frontRide.getQuantity().getRawValue();
        }
        else if (env.equals("rrh"))
        {
            return rearRide.getQuantity().getRawValue();
        }
        else if (env.equals(CONFIGSIDESLIP))
        {
            return sideSlip.getQuantity().getRawValue();
        }
        else if (env.equals(CORNERING))
        {
            return corneringRadiusParameter.getQuantity().getRawValue();
        }
        else if (env.equals(CONFIG_ROLL))
        {
            return rollParameter.getQuantity().getRawValue();
        }
        else if (env.equals(STEERING))
            return userSteering.getQuantity().getRawValue();
        else if (env.equals("maxSteps"))
            return 1100;
        else
            return 0;
    }

    //This is dumber than valEnv. Just pulls the sysenv and returns it as a string.
    public static String valEnvString(String env) {
        if (System.getenv(env) != null && !System.getenv(env).isEmpty())
            return System.getenv(env);
        else
            return null;
    }

    //Returns true if true, false if false. Needs to parse the string into a boolean. There's some legacy code for the domainSet flag, but this could be removed.
    public static boolean boolEnv(String env) {

        // Read the sys environment to figure out if you want a full car or a half car sim
        if (env.equals("domainSet") && System.getenv(env) != null && System.getenv(env).equalsIgnoreCase("half"))
            return true;
        return System.getenv(env) != null && System.getenv(env).equalsIgnoreCase("true");

    }


    //Initialize coordinate systems with their STAR objects.
    private void setupCoordinates() {
        try {
            radiatorCoord = (CartesianCoordinateSystem) activeSim.getCoordinateSystemManager().getCoordinateSystem("Radiator Cartesian");
            frontWheelSteering = (CylindricalCoordinateSystem) activeSim.getCoordinateSystemManager().getCoordinateSystem("Front Wheel Steering");
            frontWheelCoord = (CylindricalCoordinateSystem) activeSim.getCoordinateSystemManager().getCoordinateSystem("Front Wheel Cylindrical");
            rearWheelCoord = (CylindricalCoordinateSystem) activeSim.getCoordinateSystemManager().getCoordinateSystem("Rear Wheel Cylindrical");
            if (dualRadFlag)
                dualRadCoord = (CartesianCoordinateSystem) activeSim.getCoordinateSystemManager().
                        getCoordinateSystem("Dual Radiator Cartesian");
            if (corneringFlag)
            {
                rotatingFrame = (UserRotatingAndTranslatingReferenceFrame) activeSim.getReferenceFrameManager().getObject(ROTATING);
                domainAxis = (CylindricalCoordinateSystem) activeSim.getCoordinateSystemManager().getCoordinateSystem(DOMAIN_AXIS);
                domainAxis.getOrigin().setDefinition("[0, ${User Cornering Radius}, 0]");
            }

        } catch (Exception e) {
            activeSim.println("SimComponents.java - Coordinate system lookup failed");
        }
        try {
            rollAxis = (CartesianCoordinateSystem) activeSim.getCoordinateSystemManager().getCoordinateSystem("Roll axis");
        } catch (Exception e) {
            activeSim.println(this.getClass().getName() + " rollAxis not found");
            //createRollAxis();
        }
    }

    // Removes old regions. Creates new ones. Avoid using this function too if you can avoid it. The way this does things is very destructive.
    public void regionSwap() {

        removeAllRegions();
        try {
            if (dualRadFlag) {
                activeSim.getRegionManager().newRegionsFromParts(new NeoObjectVector(new Object[]{radPart, subtractPart, dualRadPart, fanPart}),
                        "OneRegionPerPart", null, "OneBoundaryPerPartSurface", null,
                        "OneFeatureCurve", null, false);
            } else {
                activeSim.getRegionManager().newRegionsFromParts(new NeoObjectVector(new Object[]{radPart, subtractPart, fanPart}),
                        "OneRegionPerPart", null, "OneBoundaryPerPartSurface", null,
                        "OneFeatureCurve", null, false);
            }
            if (dualFanFlag)
            {
                activeSim.getRegionManager().newRegionsFromParts(new NeoObjectVector(new Object[]{dualFanPart}),
                        "OneRegionPerPart", null, "OneBoundaryPerPartSurface", null,
                        "OneFeatureCurve", null, false);
            }
        } catch (NullPointerException e) {
            activeSim.println(this.getClass().getName() + " - Region swap failed");
        }

        domainRegion = assignRegion(DOMAIN_REGION);
        radiatorRegion = assignRegion(RADIATOR_NAME);
        fanRegion = assignRegion(FAN_PART_STRING);
        fanRegion.setPresentationName(FAN_REGION);
        radiatorRegion.setPresentationName(RADIATOR_REGION);
        if (dualRadFlag) {
            dualRadiatorRegion = assignRegion(DUAL_RADIATOR_NAME);
            dualRadiatorRegion.setPresentationName(DUAL_RADIATOR_REGION);
        }
        if (dualFanFlag)
        {
            dualFanRegion = assignRegion(DUAL_FAN_PART_STRING);
            dualFanRegion.setPresentationName(DUAL_FAN_REGION);
        }

    }

    public void removeAllRegions() {
        activeSim.println("Removing all existing regions");
        for (Region x : activeSim.getRegionManager().getRegions())
        {
            activeSim.getRegionManager().removeRegion(x);
        }
    }

    //I have no idea why this is here. But it reverses an array. eg [1, 2, 3] -> [3, 2, 1]
    public static Object[] reverseArr(Object [] arr)
    {
        for (int i = 0; i < arr.length / 2 ; i++)
        {
            Object temp = arr[i];
            arr[i] = arr[arr.length - 1 - i];
            arr[arr.length - 1 - i] = temp;
        }

        return arr;
    }

    public void saveSim() {
        String newName = System.getenv("newName");

        if (newName != null)
            activeSim.saveState(newName);
        else
            activeSim.saveState(activeSim.getSessionDir() + File.separator + activeSim.getPresentationName() + ".sim");
    }

    public void killSim() {
        activeSim.kill();
    }

    public void clearHistory() {
        activeSim.getSolution().clearSolution(Solution.Clear.History);
    }

    //If a region named regName exists, it'll return that region, otherwise it'll create a new empty region and name it regName
    private Region assignRegion(String regName) {
        Region output;
        if (activeSim.getRegionManager().has(regName))
            output = activeSim.getRegionManager().getRegion(regName);
        else {
            output = activeSim.getRegionManager().createEmptyRegion();
            output.setPresentationName(regName);
        }
        return output;
    }

    //Some 2D vector transformation.
    public static double[] vectorRotate(double angle, double[] arr) {
        double r = Math.hypot(arr[0], arr[1]);
        double x = r * Math.cos(angle);
        double y = -r * Math.sin(angle);

        arr[0] = x;
        arr[1] = y;

        return arr;

    }

    //Explode if the version in the macro doesn't match the version in the sim parameter.
    private void checkVersion()
    {
        ScalarGlobalParameter versionParam;
        if (activeSim.get(GlobalParameterManager.class).has("Version checker"))
        {
            versionParam = (ScalarGlobalParameter) activeSim.get(GlobalParameterManager.class).getObject("Version checker");
        }
        else
        {
            throw new IllegalStateException("You don't have a version checker parameter");
        }
        double val = versionParam.getQuantity().getRawValue();
        if (val != version)
        {
            activeSim.println("Version: " + version);
            throw new IllegalStateException("You're using the wrong macro + sim combination.");
        }
    }

    //Convert a sideslip angle into an actionable y-component of velocity for the ground. Obviously, since it's using a tangent function, don't use very high and unrealistic sideslip angles.
    public double calculateSideslip()
    {
        double sideslipAngle = valEnv(CONFIGSIDESLIP);
        return freestreamVal * Math.tan(Math.toRadians(sideslipAngle));
    }

    public boolean isUnix() throws IOException {
        if (System.getProperty("os.name").contains("Windows"))
        {
            activeSim.println("Windows platform detected");
            return false;
        }
        else
        {
            activeSim.println("I hope you're running this on a cluster with a \\tmp directory with plenty of space otherwise this could go poorly...");
            String location = separator + "tmp";
            if (!Files.isDirectory(Path.of(location)))
            {
                throw new RuntimeException("Can't find a \\tmp directory. we're not trying this");
            }

            String sizeFile = location + separator + "size." + valEnvString("SLURM_JOB_ID");
            if (!Files.exists(Path.of(sizeFile)))
                throw new RuntimeException("Can't find size file in " + sizeFile);

            BufferedReader br = new BufferedReader(new FileReader(sizeFile));
            String fileSize, currentline;
            fileSize = "";
            while ((currentline = br.readLine()) != null)
            {
                fileSize = currentline;
                activeSim.println("File size line: " + fileSize);
            }

            String splitString = fileSize.replaceAll("[^\\d.]", "");
            double folderSize = Double.valueOf(splitString);
            activeSim.println("File size: " + folderSize + " megabytes");
            if (folderSize > 5000)
                throw new RuntimeException("Not enough space on tmp to reliably run");
            return true;
        }
    }


}
