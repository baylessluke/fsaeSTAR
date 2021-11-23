import star.common.Simulation;
import star.common.StarMacro;

public class RTTestController extends StarMacro {

    public static Simulation sim;

    public void execute() {

        sim = getActiveSimulation();

    }

    /**
     * Runs all the macros provided according to the passed flag
     */
    private void runMacros() {
        // macro lists

        String[] GEOMETRY_PREP_MACROS = {
                "DomainSet.java",
                "RideHeight.java",
                "RollSet.java",
                "Steering.java"
        };

        String[] GEOMETRY_REPAIR_MACROS = {
                "DomainSet.java",
                "RideHeight.java",
                "RollSet.java",
                "Steering.java"
        };

        String[] MESH_PREP_MACROS = {
                "DomainSet.java",
                "RideHeight.java",
                "RollSet.java",
                "Steering.java"
        };

        String[] MESH_MACROS = {
                "DomainSet.java",
                "RideHeight.java",
                "RollSet.java",
                "Steering.java"
        };

        String[] MESH_REPAIR_MACROS = {
                "DomainSet.java",
                "RideHeight.java",
                "RollSet.java",
                "Steering.java"
        };

        String[] INITIAL_EXECUTION_MACROS = {
                "MeshRepair.java",
                "yawSet.java",
                "GenReports.java",
                "SoftRun.java",
                "MeshRepair.java",
                "SoftRun.java",
                "MeshRepair.java",
                "run.java",
        };

        String[] LATE_STAGE_EXECUTION_MACROS = {
                "DomainSet.java",
                "RideHeight.java",
                "RollSet.java",
                "Steering.java"
        };

        String[] POST_PROC_MACROS = {
                "DomainSet.java",
                "RideHeight.java",
                "RollSet.java",
                "Steering.java"
        };

        String [] FR_PRE_PROC_MACROS = {
                "DomainSet.java",
                "RideHeight.java",
                "RollSet.java",
                "Steering.java",
                "SurfaceWrap.java",
                "Subtract.java",
                "Regions.java",
                "AutoMesh.java",
                "MeshRepair.java",
                "MeshRepair.java",
                "MeshRepair.java",
                "save.java",
        };

        String [] FR_PROC_MACROS = {
                "MeshRepair.java",
                "yawSet.java",
                "GenReports.java",
                "SoftRun.java",
                "MeshRepair.java",
                "SoftRun.java",
                "MeshRepair.java",
                "run.java",
                "ExportReports.java"
        };


        String [] FR_POST_PROC_MACROS = {
                "ExportReports.java",
                "PostProc.java"
        };
    }

    /**
     * Print the test results. Created this method to standardize passed and failed messages
     * @param testPassed
     */
    public static void printTestResults(boolean testPassed, String test, String result, String expected) {

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
