import star.common.*;
import star.meshing.LatestMeshProxyRepresentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

public class RTTestComponent {

    Simulation sim;

    // Test settings
    private final String TEST_SETTING_FILE_NAME = "testSetting.test";
    private String[] macros;
    private boolean yawFlag;
    private boolean rollFlag;
    private boolean steerFlag;
    private boolean rhFlag;
    private boolean fanFlag;
    private boolean fullRunFlag;

    // Names
    public final String SURFACE_WRAPPER_NAME = "Surface wrapper";
    public final String FRONT_WHEEL_CYLINDRICAL_NAME = "Front Wheel Cylindrical";
    public final String FRONT_WHEEL_STEERING_NAME = "Front Wheel Steering";
    public final String REAR_WHEEL_CYLINDRICAL_NAME = "Rear Wheel Cylindrical";
    public final String AUTO_MESH_NAME = "Automated Mesh";
    public final String LATEST_SRF_VOL_NAME = "Latest Surface/Volume";
    public final String RAD_CS_NAME = "Radiator Cartesian";
    public final String DUAL_RAD_CS_NAME = "Dual Radiator Cartesian";
    public final String FAN_CS_NAME = "Fan Cylindrical";
    public final String DUAL_FAN_CS_NAME = "Dual Fan Cylindrical";

    // Coordinate systems
    public CylindricalCoordinateSystem frontWheelCylindrical; // Coordinate system used for both tire rotation and front left steering
    public CylindricalCoordinateSystem frontWheelSteering; // Used for front right steering, thanks for raunaq for the confusing naming convention
    public CylindricalCoordinateSystem rearWheelCylindrical; // rear wheel cylindrical
    public CartesianCoordinateSystem radCartesian;
    public CartesianCoordinateSystem dualRadCartesian;
    public CylindricalCoordinateSystem fanCylindrical;
    public CylindricalCoordinateSystem dualFanCylindrical;

    // units
    public Units unitless;

    // misc
    public LatestMeshProxyRepresentation latestSrfVol;

    // Part collections
    public Collection<GeometryPart> cfdParts = new ArrayList<>();
    public Collection<GeometryPart> aeroParts = new ArrayList<>();
    public Collection<GeometryPart> tireParts = new ArrayList<>();
    public Collection<GeometryPart> fwParts = new ArrayList<>();
    public Collection<GeometryPart> rwParts = new ArrayList<>();
    public Collection<GeometryPart> swParts = new ArrayList<>();
    public Collection<GeometryPart> utParts = new ArrayList<>();
    public Collection<GeometryPart> nsParts = new ArrayList<>();

    // vehicle dimensions
    public final int WHEEL_BASE = 61; // inches

    // initialization

    public RTTestComponent(Simulation sim) {
        this.sim = sim;
        sortParts();
        initStarObjects();
    }

    /**
     * Imports test setting file and fill out all the setting parameters
     * @throws IOException
     */
    private void importSetting() throws IOException {

        // creating file from test settings
        String homeDir = System.getProperty("user.dir");
        InputStream testSettingFile = new FileInputStream(homeDir + File.separator + TEST_SETTING_FILE_NAME);

        // Properties object
        Properties testSettings = new Properties();
        testSettings.load(testSettingFile);

        // read properties
        String macrosStr = testSettings.getProperty("TEST_ENVS");
        this.macros = macrosStr.strip().split(",");
        this.yawFlag = Boolean.parseBoolean(testSettings.getProperty("yaw"));
        this.fanFlag = Boolean.parseBoolean(testSettings.getProperty("fan"));
        this.rollFlag = Boolean.parseBoolean(testSettings.getProperty("roll"));
        this.rhFlag = Boolean.parseBoolean(testSettings.getProperty("rh"));
        this.steerFlag = Boolean.parseBoolean(testSettings.getProperty("steering"));
        this.fullRunFlag = Boolean.parseBoolean(testSettings.getProperty("complete_run"));

    }

    /**
     * Sort leaf geometry parts into their respective category according to the names for parent geometry parts
     */
    private void sortParts() {

        Collection<GeometryPart> geomParts = sim.getGeometryPartManager().getParts(); // get all parents

        // sort CFD, tire, and aero parts
        for (GeometryPart parent:geomParts) {
            if (parent.getPresentationName().contains("CFD_")) {
                cfdParts.addAll(parent.getLeafParts());

                if (parent.getPresentationName().equals("CFD_AERODYNAMICS_830250079")) {
                    aeroParts.addAll(parent.getLeafParts());
                }
            } else if (parent.getPresentationName().equals("Front Left") || parent.getPresentationName().equals("Front Right") || parent.getPresentationName().equals("Rear Left") || parent.getPresentationName().equals("Rear Right"))
                tireParts.addAll(parent.getLeafParts());
        }

        // sort aero parts into the subcategories
        for (GeometryPart part:aeroParts) {
            GeometryPart parent = part.getParentPart();

            if (parent.getPresentationName().contains("FW_"))
                fwParts.add(part);
            else if (parent.getPresentationName().contains("RW_"))
                rwParts.add(part);
            else if (parent.getPresentationName().contains("SW_"))
                swParts.add(part);
            else if (parent.getPresentationName().contains("UT_"))
                utParts.add(part);
            else if (parent.getPresentationName().contains("NS_"))
                nsParts.add(part);
        }

    }

    /**
     * Initialize star objects like coordinate systems, reports, and other stuff
     */
    private void initStarObjects() {

        // coordinate system
        this.frontWheelCylindrical = (CylindricalCoordinateSystem) sim.getCoordinateSystemManager().getCoordinateSystem(FRONT_WHEEL_CYLINDRICAL_NAME);
        this.frontWheelSteering = (CylindricalCoordinateSystem) sim.getCoordinateSystemManager().getCoordinateSystem(FRONT_WHEEL_STEERING_NAME);
        this.rearWheelCylindrical = (CylindricalCoordinateSystem) sim.getCoordinateSystemManager().getCoordinateSystem(REAR_WHEEL_CYLINDRICAL_NAME);
        this.radCartesian = (CartesianCoordinateSystem) sim.getCoordinateSystemManager().getCoordinateSystem(RAD_CS_NAME);
        this.dualRadCartesian = (CartesianCoordinateSystem) sim.getCoordinateSystemManager().getCoordinateSystem(DUAL_RAD_CS_NAME);
        this.fanCylindrical = (CylindricalCoordinateSystem) sim.getCoordinateSystemManager().getCoordinateSystem(FAN_CS_NAME);
        this.dualFanCylindrical = (CylindricalCoordinateSystem) sim.getCoordinateSystemManager().getCoordinateSystem(DUAL_FAN_CS_NAME);

        // units
        this.unitless = sim.getUnitsManager().getObject("");

        // misc
        this.latestSrfVol = (LatestMeshProxyRepresentation) sim.getRepresentationManager().getObject(LATEST_SRF_VOL_NAME);

    }

    // public methods

    /**
     * Print the test results. Created this method to standardize passed and failed messages
     */
    public void printTestResults(boolean testPassed, String test, String result, String expected) {

        StringBuilder output = new StringBuilder();

        if (testPassed) {
            output.append(String.format("%40s | PASSED: ", test));
        } else {
            output.append(String.format("%40s | FAILED: ", test));
        }

        output.append("Result: ").append(result).append(" | Expected: ").append(expected);
        sim.println(output);
    }

    /**
     * Compare two numbers to see if they are equal given a safety factor. The safety factor is placed on the b value.
     * @param a actual value
     * @param b expected value
     * @param safetyFactor safety factor NOT in percentage
     */
    public static boolean numericalCompare(double a, double b, double safetyFactor) {

        double upper = b * (1 + safetyFactor);
        double lower = b * (1 - safetyFactor);

        if (b >= 0) {
            return upper > a && lower < a;
        } else {
            return upper < a && lower > a;
        }

    }

    /**
     * Compare two lists with a safety factor applied on b
     * @param a actual value
     * @param b expected value
     * @param safetyFactor
     */
    public static boolean numericalCompare(double[] a, double[] b, double safetyFactor) throws Exception {

        if (a.length != b.length)
            throw new Exception("Numerical compare failed! The size of two arrays are different");

        for (int i = 0; i < a.length; i++) {
            if (!numericalCompare(a[i], b[i], safetyFactor))
                return false;
        }

        return true;

    }

    /**
     * Get all the surfaces under all parts in a part group (e.g. cfdParts)
     */
    public static Collection<PartSurface> getAllSurfacesByPartGroup(Collection<GeometryPart> partGroup) {

        Collection<PartSurface> surfaces = new ArrayList<>();
        for (GeometryPart part:partGroup) {
            surfaces.addAll(part.getPartSurfaces());
        }
        return surfaces;

    }

    /**
     * Get the origin of the location of a coordinate system in inches
     */
    public static double[] getCSLocation(CoordinateSystem cs) {

        double[] origin = new double[3];
        for (int i = 0; i < 3; i++)
            origin[i] = meterToInch(cs.getOriginVector().getComponent(i));
        return origin;

    }

    /**
     * Get the x direction of cartesian coordinate or the r direction of cylindrical direction
     */
    public static double[] getCSDirection(CoordinateSystem cs, boolean isCartesian) {

        double[] direction = new double[3];
        if (isCartesian) {
            CartesianCoordinateSystem cartesian = (CartesianCoordinateSystem) cs;
            for (int i = 0; i < 3; i++)
                direction[i] = cartesian.getBasis0().getComponent(i);
        } else {
            CylindricalCoordinateSystem cylindrical = (CylindricalCoordinateSystem) cs;
            for (int i = 0; i < 3; i++)
                direction[i] = cylindrical.getBasis0().getComponent(i);
        }
        return direction;

    }

    /**
     * Build the result output string from an array
     * @param prefix String that comes before the array
     * @param array gee, I wonder what it means
     * @param postfix the String that comes after the array
     * @return result output string
     */
    public static String buildResultStringFromArray(String prefix, double[] array, String postfix) {

        StringBuilder output = new StringBuilder();
        output.append(prefix);
        output.append(" [");
        for (int i = 0; i < array.length; i++)
            output.append(String.format("%.5f", array[i])).append(" ,");
        output.delete(output.length() - 2, output.length());
        output.append("] ");
        output.append(postfix);

        return output.toString();

    }

    /**
     * Build the result output string from an array
     * @param array gee, I wonder what it means
     * @param postfix the String that comes after the array
     * @return result output string
     */
    public static String buildResultStringFromArray(double[] array, String postfix) {
        return buildResultStringFromArray("", array, postfix);
    }

    /**
     * Build the result output string from an array
     * @param prefix String that comes before the array
     * @param array gee, I wonder what it means
     * @return result output string
     */
    public static String buildResultStringFromArray(String prefix, double[] array) {
        return buildResultStringFromArray(prefix, array, "");
    }

    /**
     * Change meters to inches
     */
    public static double meterToInch(double meter) {
        return meter * 39.37;
    }

}
