import star.common.Simulation;

import java.io.File;
import java.io.IOException;

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
        File testSettingFile = new File(homeDir + File.separator + TEST_SETTING_FILE_NAME);
        

    }

}
