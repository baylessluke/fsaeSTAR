import star.common.*;

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
    public final String AUTO_MESH_NAME = "Automated Mesh";

    // Coordinate systems
    public CylindricalCoordinateSystem frontWheelCylindrical; // Coordinate system used for both tire rotation and front left steering
    public CylindricalCoordinateSystem frontWheelSteering; // Used for front right steering, thanks for raunaq for the confusing naming convention

    // units
    public Units unitless;

    // Part collections
    public Collection<GeometryPart> cfdParts = new ArrayList<>();
    public Collection<GeometryPart> aeroParts = new ArrayList<>();
    public Collection<GeometryPart> chaParts = new ArrayList<>();
    public Collection<GeometryPart> susParts = new ArrayList<>();
    public Collection<GeometryPart> powParts = new ArrayList<>();
    public Collection<GeometryPart> tireParts = new ArrayList<>();
    public Collection<GeometryPart> fwParts = new ArrayList<>();
    public Collection<GeometryPart> rwParts = new ArrayList<>();
    public Collection<GeometryPart> swParts = new ArrayList<>();
    public Collection<GeometryPart> utParts = new ArrayList<>();
    public Collection<GeometryPart> nsParts = new ArrayList<>();

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

        // sort CFD, tire, aero, chassis, and suspension parts
        for (GeometryPart parent:geomParts) {
            if (parent.getPresentationName().contains("CFD_")) {
                cfdParts.addAll(parent.getLeafParts());

                if (parent.getPresentationName().equals("CFD_AERODYNAMICS_830250079")) {
                    aeroParts.addAll(parent.getLeafParts());
                }

                if (parent.getPresentationName().equals("CFD_CHASSIS")) {
                    chaParts.addAll(parent.getLeafParts());
                }

                if (parent.getPresentationName().equals("CFD_SUSPENSION")) {
                    susParts.addAll(parent.getLeafParts());
                }

                if (parent.getPresentationName().equals("CFD_POWERTRAIN")) {
                    powParts.addAll(parent.getLeafParts());
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

        // units
        this.unitless = sim.getUnitsManager().getObject("");

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
}
