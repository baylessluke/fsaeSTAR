import star.cadmodeler.SolidModelPart;
import star.common.CompositePart;
import star.common.GeometryPart;
import star.common.StarMacro;
import star.material.SolidModel;
import star.meshing.MeshPart;
import star.meshing.MeshPartFactory;
import star.meshing.SimpleBlockPart;

import java.util.ArrayList;
import java.util.Collection;

public class combineEverything extends StarMacro {

    public void execute()
    {
        combineThethings();
    }

    public void combineThethings()
    {
        SimComponents activeSim = new SimComponents(getActiveSimulation());
        for (GeometryPart part : activeSim.activeSim.getGeometryPartManager().getObjects())
        {
            if (part instanceof CompositePart)
            {
                if (part.getPresentationName().contains(SimComponents.aeroParent))
                {
                    for (GeometryPart child : ((CompositePart) part).getChildParts().getParts())
                    {
                        if (child instanceof CompositePart)
                            combine((CompositePart) child);
                    }
                }
                else
                    combine((CompositePart) part);
            }
        }
        activeSim.saveSim();
    }

    public void combine(CompositePart prt)
    {
        MeshPartFactory meshFactory = getActiveSimulation().get(MeshPartFactory.class);
        Collection<GeometryPart> solids = new ArrayList<>();
        for (GeometryPart kids : prt.getChildParts().getParts())
        {
            if (!(kids instanceof CompositePart))
                solids.add(kids);
        }
        if (solids.size() > 1)
        {
            GeometryPart target = solids.iterator().next();
            solids.remove(target);
            getActiveRootObject().println("Combining " + target.getPresentationName());
            meshFactory.combineMeshParts(target, solids);
        }
    }

}
