import star.common.GeometryPart;
import star.common.Simulation;
import star.common.StarMacro;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

public class RTTestController extends StarMacro {

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

    // Initialization

    public RTTestController() {
        sim = getActiveSimulation();
    }

    public void execute() {

        sim = getActiveSimulation();

        sortParts(sim);

        new RTTestController().execute();
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
     * @param sim
     */
    private void sortParts(Simulation sim) {

        Collection<GeometryPart> geomParts = new ArrayList<>();
        geomParts = sim.getGeometryPartManager().getParts(); // get all parents

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
     * Print the test results. Created this method to standardize passed and failed messages
     * @param testPassed
     */
    public void printTestResults(boolean testPassed, String test, String result, String expected) {

        StringBuilder output = new StringBuilder();

        if (testPassed) {
            output.append(String.format("%40f | PASSED: ", test));
        } else {
            output.append(String.format("%40f | FAILED: ", test));
        }

        output.append("Result: " + result + " | Expected: " + expected);
        sim.println(output);
    }

}
