package com.houzicore.shared.core.displayentity.function;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Metadata for one BDEngine mcfunction export namespace.
 */
public final class BdeFunctionPack {

    private final String _namespace;
    private final File _rootDirectory;
    private final List<String> _functionIds;

    BdeFunctionPack(String namespace, File rootDirectory, List<String> functionIds) {
        _namespace = namespace;
        _rootDirectory = rootDirectory;
        _functionIds = new ArrayList<>(functionIds);
        Collections.sort(_functionIds);
    }

    public String getNamespace() {
        return _namespace;
    }

    public File getRootDirectory() {
        return _rootDirectory;
    }

    public List<String> getFunctionIds() {
        return Collections.unmodifiableList(_functionIds);
    }

    public String getCreateFunctionId() {
        String id = _namespace + ":_/create";
        return _functionIds.contains(id) ? id : null;
    }

    public String getDeleteFunctionId() {
        String id = _namespace + ":_/delete";
        return _functionIds.contains(id) ? id : null;
    }

    public List<String> getLoopAnimationFunctionIds() {
        List<String> result = new ArrayList<>();
        for (String id : _functionIds) {
            if (id.endsWith("/play_anim_loop")) {
                result.add(id);
            }
        }
        return result;
    }
}
