import star.common.StarMacro;

/*
A nice little interface to test a macro chain without having to mess with MacroController.
 */

public class testBed extends StarMacro {
    private static boolean CONVERGED = false;

    public void execute()
    {
        SimComponents activeSim = new SimComponents(getActiveSimulation());
        ConvergenceChecker obj = new ConvergenceChecker(activeSim);
        for (String key : obj.convergenceResults.keySet())
        {
            if (key.contains(SimComponents.LIFT_COEFFICIENT_PLOT))
            {
                if (obj.convergenceResults.get(key) && activeSim.convergenceCheck) {
                    CONVERGED = true;
                    return;
                }
            }
        }
    }
}
