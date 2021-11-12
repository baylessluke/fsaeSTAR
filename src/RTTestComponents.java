import star.common.Simulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RTTestComponents {

    // simulation
    public Simulation sim;

    // test settings
    private static final String TEST_SETTING_FILE_NAME = "testSetting.test";
    private String[] macros;
    private boolean yawFlag;
    private boolean rollFlag;
    private boolean steerFlag;
    private boolean rhFlag;
    private boolean fanFlag;
    private boolean fullRunFlag;

    // -----------------------
    //     Initialization
    // -----------------------

    /**
     * Constructor
     * @param sim
     */
    public RTTestComponents(Simulation sim) {
        this.sim = sim;
        try {
            this.importSetting();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

}
