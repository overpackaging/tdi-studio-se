// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.generic.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.commons.ui.swt.actions.ITreeContextualAction;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.runtime.services.IGenericDBService;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.core.utils.ReflectionUtils;
import org.talend.daikon.properties.presentation.Form;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.repository.generic.action.GenericAction;
import org.talend.repository.generic.internal.IGenericWizardInternalService;
import org.talend.repository.generic.internal.service.GenericWizardInternalService;
import org.talend.repository.generic.model.genericMetadata.SubContainer;
import org.talend.repository.generic.ui.DynamicComposite;
import org.talend.repository.generic.util.GenericConnectionUtil;
import org.talend.repository.generic.util.RepTypeMappingManager;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;

import orgomg.cwm.objectmodel.core.TaggedValue;

/**
 * created by ycbai on 2015年9月9日 Detailled comment
 *
 */
public class GenericWizardService implements IGenericWizardService {

    private IGenericWizardInternalService internalService = null;

    List<String> typeNames = new ArrayList<>();

    public GenericWizardService() {
        internalService = new GenericWizardInternalService();
    }

    @Override
    public List<RepositoryNode> createNodesFromComponentService(RepositoryNode curParentNode) {
        List<RepositoryNode> repNodes = new ArrayList<>();
        Set<ComponentWizardDefinition> wizardDefinitions = internalService.getComponentService().getTopLevelComponentWizards();
        for (ComponentWizardDefinition wizardDefinition : wizardDefinitions) {
            String name = wizardDefinition.getName();
            String displayName = wizardDefinition.getDisplayName();
            String folder = "metadata/" + name; //$NON-NLS-1$
            int ordinal = 100;
            ERepositoryObjectType repositoryType = internalService.createRepositoryType(name, displayName, name, folder, ordinal);
            if (curParentNode == null && "JDBC".equals(name)) { //$NON-NLS-1$
                Class<ComponentProperties> jdbcClass = ReflectionUtils.getClass(
                        "org.talend.components.jdbc.wizard.JDBCConnectionWizardProperties",
                        wizardDefinition.getClass().getClassLoader());
                if (jdbcClass != null && wizardDefinition.supportsProperties(jdbcClass)) {
                    IGenericDBService dbService = null;
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
                        dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(IGenericDBService.class);
                    }
                    if (dbService != null) {
                        dbService.getExtraTypes().add(repositoryType);
                    }
                }
            }
            if (curParentNode != null && !needHide(repositoryType)) {
                repNodes.add(internalService.createRepositoryNode(curParentNode, wizardDefinition.getDisplayName(),
                        repositoryType, ENodeType.SYSTEM_FOLDER));
            }
        }
        return repNodes;
    }

    private boolean needHide(ERepositoryObjectType type) {
        if (type == null) {
            return false;
        }
        List<ERepositoryObjectType> extraTypes = new ArrayList<ERepositoryObjectType>();
        IGenericDBService dbService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericDBService.class)) {
            dbService = (IGenericDBService) GlobalServiceRegister.getDefault().getService(IGenericDBService.class);
        }
        if (dbService != null) {
            extraTypes.addAll(dbService.getExtraTypes());
        }
        return extraTypes.contains(type);
    }

    @Override
    public List<String> getGenericTypeNames() {
        if (typeNames.isEmpty()) {
            Set<ComponentWizardDefinition> wizardDefinitions = internalService.getComponentService()
                    .getTopLevelComponentWizards();
            for (ComponentWizardDefinition wizardDefinition : wizardDefinitions) {
                typeNames.add(wizardDefinition.getName());
            }
        }
        return typeNames;
    }

    @Override
    public boolean isGenericType(ERepositoryObjectType repObjType) {
        if (repObjType == null) {
            return false;
        }
        List<String> genericTypeNames = getGenericTypeNames();
        if (genericTypeNames != null && genericTypeNames.contains(repObjType.getType())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isGenericItem(Item item) {
        if(item !=null && item instanceof ConnectionItem){
            return ((ConnectionItem)item).getConnection().getCompProperties() != null;
        }
        return false;
    }

    @Override
    public boolean isGenericConnection(Connection connection) {
        return connection != null && connection.getCompProperties() != null;
    }

    @Override
    public Image getNodeImage(String typeName) {
        InputStream imageStream = internalService.getComponentService().getWizardPngImage(typeName,
                WizardImageType.TREE_ICON_16X16);
        if (imageStream == null) {
            return null;
        }
        // node image   ImageProvider.getImageDesc(ECoreImage.METADATA_TABLE_ICON)
        ImageData id = new ImageData(imageStream);
        Image image = new Image(null, id);
        return image;
    }

    @Override
    public Image getWiardImage(String typeName) {
        InputStream imageStream = internalService.getComponentService().getWizardPngImage(typeName,
                WizardImageType.WIZARD_BANNER_75X66);
        ImageData id = new ImageData(imageStream);
        Image image = new Image(null, id);
        return image;
    }

    @Override
    public List<MetadataTable> getMetadataTables(Connection connection) {
        List<MetadataTable> metadataTables = new ArrayList<>();
        if (connection != null) {
            return SchemaUtils.getMetadataTables(connection, SubContainer.class);
        }
        return metadataTables;
    }

    @Override
    public Composite creatDynamicComposite(Composite composite, Element element, EComponentCategory sectionCategory,
            boolean isCompactView) {
        DynamicComposite dynamicComposite = null;
        if (element != null && element instanceof INode) {
            INode node = (INode) element;
            ComponentProperties props = null;
            if (node.getComponentProperties() == null) {
                props = ComponentsUtils.getComponentProperties(node.getComponent().getName());
            } else {
                props = node.getComponentProperties();
            }
            if (props != null) {
                Form form = props.getForm(EComponentCategory.ADVANCED.equals(sectionCategory) ? Form.ADVANCED : Form.MAIN);
                dynamicComposite = new DynamicComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS, sectionCategory,
                        element, isCompactView, composite.getBackground(), form);
                List<ElementParameter> elementParameters = (List<ElementParameter>) node.getElementParameters();
                for (ElementParameter parameter : elementParameters) {
                    if (parameter instanceof GenericElementParameter) {
                        GenericElementParameter genericElementParameter = (GenericElementParameter) parameter;
                        genericElementParameter.callBeforePresent();
                        genericElementParameter.removePropertyChangeListener(dynamicComposite);
                        genericElementParameter.addPropertyChangeListener(dynamicComposite);
                    }
                }
            }
        }
        return dynamicComposite;
    }

    @Override
    public void refreshDynamicComposite(Composite composite) {
        if (composite instanceof DynamicComposite) {
            ((DynamicComposite) composite).resetParameters();
        }
    }

    @Override
    public void updateComponentSchema(INode node, IMetadataTable metadataTable) {
        SchemaUtils.updateComponentSchema(node, metadataTable, Boolean.FALSE);
    }

    @Override
    public List<ComponentProperties> getAllComponentProperties(Connection connection, String tableLabel) {
        List<ComponentProperties> componentProperties = new ArrayList<>();
        Set<ComponentProperties> componentPropertiesSet = new HashSet<>();
        if (isGenericConnection(connection)) {
            String compProperties = connection.getCompProperties();
            ComponentProperties cp = ComponentsUtils.getComponentPropertiesFromSerialized(compProperties, connection, false);
            if (cp != null) {
                componentProperties.add(cp);
            }
            List<MetadataTable> metadataTables;
            if (tableLabel == null) {
                metadataTables = SchemaUtils.getMetadataTables(connection, SubContainer.class);
            } else {
                metadataTables = Arrays.asList(SchemaUtils.getMetadataTable(connection, tableLabel, SubContainer.class));
            }
            for (MetadataTable metadataTable : metadataTables) {
                if (metadataTable == null) {
                    continue;
                }
                for (TaggedValue taggedValue : metadataTable.getTaggedValue()) {
                    if (IComponentConstants.COMPONENT_PROPERTIES_TAG.equals(taggedValue.getTag())) {
                        ComponentProperties compPros = ComponentsUtils
                                .getComponentPropertiesFromSerialized(taggedValue.getValue(), connection, false);
                        if (compPros != null && !componentPropertiesSet.contains(compPros)) {
                            compPros.updateNestedProperties(cp);
                            componentProperties.add(compPros);
                            componentPropertiesSet.add(compPros);
                        }
                    }
                }
            }
        }
        return componentProperties;
    }

    @Override
    public ERepositoryObjectType getNewRepType(String oldRepTypeName) {
        return RepTypeMappingManager.getInstance().getNewRepType(oldRepTypeName);
    }

    @Override
    public String getConnectionProperties(Connection connection) {
        if (isGenericConnection(connection)) {
            String compProperties = connection.getCompProperties();
            return compProperties;
        }
        return null;
    }

    @Override
    public ITreeContextualAction getDefaultAction(RepositoryNode node) {
        if (node == null) {
            return null;
        }
        ITreeContextualAction defaultAction = null;
        ComponentWizard editWizard = GenericConnectionUtil.getEditWizard(node);
        if (editWizard != null) {
            defaultAction = new GenericAction(editWizard);
        }
        return defaultAction;
    }

}
