import star.cadmodeler.SolidModelPart;
import star.common.CompositePart;
import star.common.GeometryPart;
import star.common.Simulation;
import star.common.StarMacro;
import star.material.SolidModel;
import star.meshing.MeshPartFactory;

import java.util.ArrayList;
import java.util.Collection;

public class explodeEverything extends StarMacro {

    public void execute()
    {
        explode();
    }

    public void explode()
    {
        SimComponents activeSim = new SimComponents(getActiveSimulation());
        Simulation simFile = activeSim.activeSim;
        Collection<GeometryPart> allParts = simFile.getGeometryPartManager().getParts();
        for (GeometryPart prt : allParts)
        {
            if (prt instanceof CompositePart)
            {
                //if aero then run explode children on all the children of the composite
                //if not aero, then run explode children directly

                if (prt.getPresentationName().contains(SimComponents.aeroParent))
                {
                    for (GeometryPart aeroKids : ((CompositePart) prt).getChildParts().getParts())
                    {
                        if (aeroKids instanceof CompositePart)
                            explode_children((CompositePart) aeroKids, false);
                    }
                }
                else
                    explode_children((CompositePart) prt, false);
            }
        }
        activeSim.saveSim();
    }

    public void explode_children(CompositePart prt, boolean explode)
    {
        //go through all children of the child then invoke an explosion function on all the children of the explosion. explode_children should not maintain the top level of the explosion
        for (GeometryPart children: prt.getChildParts().getParts())
        {
            if (children instanceof CompositePart)
                explode_children((CompositePart) children, true);
        }

        if (explode)
        {
            getActiveSimulation().println("Exploding " + prt.getPresentationName());
            prt.explode();
        }
    }

}
