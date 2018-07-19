package org.talend.sdk.component.studio.model.parameter.resolver;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;

/**
 * Common super class for ParameterResolvers. It contains common state and functionality
 */
abstract class AbstractParameterResolver implements ParameterResolver {
    
    protected final AbsolutePathResolver pathResolver = new AbsolutePathResolver();
    
    /**
     * PropertyNode, which represents Configuration class Option annotated with action annotation
     */
    protected final PropertyNode actionOwner;
    
    AbstractParameterResolver(final PropertyNode actionOwner) {
        this.actionOwner = actionOwner;

    }
    
    protected final TaCoKitElementParameter resolveParameter(final String relativePath, final Map<String, IElementParameter> settings) {
        String path = pathResolver.resolvePath(getOwnerPath(), relativePath);
        return (TaCoKitElementParameter) settings.get(path);
    }
    
    /**
     * Finds and returns all ElementParameters stored under {@code relativePath}. {@code relativePath} may point at "leaf" Configuration option and 
     * on Configuration type as well.
     * 
     * @param relativePath option path relative to action owner option
     * @param settings all "leaf" options stored by their path
     * @return
     */
    protected final List<TaCoKitElementParameter> resolveParameters(final String relativePath, final Map<String, IElementParameter> settings) {
        final String absolutePath = pathResolver.resolvePath(getOwnerPath(), relativePath);
        final TaCoKitElementParameter parameter = (TaCoKitElementParameter) settings.get(absolutePath);
        if (parameter != null) {
            // absolute path points at "leaf" Configuration option, which doesn't have children
            return Collections.singletonList(parameter);
        } else {
            // absolute path points at Configuration type, which has no corresponding ElementParameter, however there are ElementParameters
            // for its children
            return settings.entrySet().stream()
                    .filter(e -> isChildParameter(e.getKey(), absolutePath))
                    .map(Map.Entry::getValue)
                    .map(e -> (TaCoKitElementParameter) e)
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * Returns action owner option path in Configuration tree
     * 
     * @return option path
     */
    protected final String getOwnerPath() {
        return actionOwner.getProperty().getPath();
    }
    
    /**
     * Checks whether specified {@code path} is a child path of {@code parentPath}
     * 
     * @param path path to be checked
     * @param parentPath parent path
     * @return true, if path is child; false - otherwise
     */
    private boolean isChildParameter(final String path, final String parentPath) {
        return path.startsWith(parentPath) && path.substring(parentPath.length()).startsWith(".");
    }

}
