package org.talend.sdk.component.studio.model.parameter.resolver;

import java.util.Map;

import org.talend.core.model.process.IElementParameter;
import org.talend.sdk.component.studio.model.parameter.PropertyNode;
import org.talend.sdk.component.studio.model.parameter.TaCoKitElementParameter;

/**
 * Common super class for ParameterResolvers. It contains common state and functionality
 */
abstract class AbstractParameterResolver implements ParameterResolver {
    
    protected final AbsolutePathResolver pathResolver = new AbsolutePathResolver();
    
    protected final PropertyNode actionOwner;
    
    AbstractParameterResolver(final PropertyNode actionOwner) {
        this.actionOwner = actionOwner;

    }
    
    protected final TaCoKitElementParameter resolveParameter(final String relativePath, final Map<String, IElementParameter> settings) {
        String path = pathResolver.resolvePath(actionOwner.getProperty().getPath(), relativePath);
        return (TaCoKitElementParameter) settings.get(path);
    }

}
