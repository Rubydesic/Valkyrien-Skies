package valkyrienwarfare.addon.control.nodenetwork;

import gigaherz.graph.api.Graph;
import gigaherz.graph.api.GraphObject;

import java.util.List;

public class VWThreadSafeGraph extends Graph {

    private List<GraphObject> cachedObjects;

    @Override
    public void remove(GraphObject object) {
        super.remove(object);
    }
}
