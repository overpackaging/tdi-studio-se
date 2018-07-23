/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.model.action;

import java.util.regex.Pattern;
import org.talend.core.model.utils.ContextParameterUtils;

public class ActionParameter {

    private static final Pattern QUOTES_PATTERN = Pattern.compile("^\"|\"$");

    /**
     * ElementParameter name (which also denotes its path)
     */
    private final String name;

    /**
     * Action parameter alias, which used to make callback
     */
    private final String parameter;

    /**
     * Denotes whether associated ElementParameter is set and ready to be used. 
     * Once set it can't be unset
     */
    private boolean isSet = false;

    /**
     * Parameter value
     */
    private String value;
    
    /**
     * Creates ActionParameter
     * 
     * @param name ElementParameter name
     * @param parameter Action parameter name
     * @param initialValue initial value, can be null. If it's not null, then it switches ActionParameter to set state
     */
    public ActionParameter(final String name, final String parameter, final String initialValue) {
        this.name = name;
        this.parameter = parameter;
        setValue(initialValue);
    }

    void setValue(final String newValue) {
        if (newValue != null) {
            this.value = removeQuotes(newValue);
            // todo: if context -> evaluate
            this.isSet = !this.value.equals(newValue) || !ContextParameterUtils.containContextVariables(newValue);
        } else {
            this.value = null;
            this.isSet = false;
        }
    }

    protected String removeQuotes(final String quotedString) {
        return QUOTES_PATTERN.matcher(quotedString).replaceAll("");
    }

    /**
     * ElementParameter name (which also denotes its path)
     */
    String getName() {
        return this.name;
    }

    /**
     * Action parameter alias, which used to make callback
     */
    String getParameter() {
        return this.parameter;
    }

    /**
     * Denotes whether associated ElementParameter is set and can be used in action call. 
     * Once set it can't be unset
     */
    boolean isSet() {
        return this.isSet;
    }

    /**
     * Parameter value
     */
    String getValue() {
        return this.value;
    }
}
