// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.model.RepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode.EProperties;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.ui.views.RepositoryView;

/**
 * Utility class to manage RepositoryNode.<br/>
 * 
 * $Id: RepositoryNodeUtilities.java 1368 2007-01-10 09:50:53Z smallet $
 * 
 */
public class RepositoryNodeUtilities {

    private final static String[] METADATA_LABELS = new String[] {};

    public static IPath getPath(RepositoryNode node) {
        if (node == null) {
            return null;
        }
        if (node.isBin()) {
            return new Path("bin"); //$NON-NLS-1$
        }
        if ((node.getType() == ENodeType.STABLE_SYSTEM_FOLDER && node.getContentType() != ERepositoryObjectType.JOBS && node
                .getContentType() != ERepositoryObjectType.JOBLETS)
                || node.getType() == ENodeType.SYSTEM_FOLDER) {
            return new Path(""); //$NON-NLS-1$
        }
        if (node.getType() == ENodeType.SIMPLE_FOLDER) {
            String label = node.getObject().getProperty().getLabel();
            return getPath(node.getParent()).append(label);
        }

        String label = node.getLabel();
        // checks if node is under Documentations/Generatated/Jobs
        if (node.getType() == ENodeType.STABLE_SYSTEM_FOLDER
                && (node.getContentType() == ERepositoryObjectType.JOBS || node.getContentType() == ERepositoryObjectType.JOBLETS)) {
            String nodeLabel = (String) node.getProperties(EProperties.LABEL);
            if (nodeLabel.equalsIgnoreCase(ERepositoryObjectType.JOBS.toString())) {
                return new Path("");
            } else {
                return getPath(node.getParent()).append(label);
            }
        } else {
            if (!isMetadataLabel(label) && !label.equals(ERepositoryObjectType.PROCESS.toString())
                    && !label.equals(ERepositoryObjectType.JOBLET.toString())
                    && !label.equals(ERepositoryObjectType.CONTEXT.toString())
                    && !label.equals(ERepositoryObjectType.ROUTINES.toString())
                    && !label.equals(ERepositoryObjectType.SQLPATTERNS.toString())
                    && !label.equals(ERepositoryObjectType.DOCUMENTATION.toString())
                    && !label.equals(ERepositoryObjectType.BUSINESS_PROCESS.toString())) {
                return getPath(node.getParent()).append(label);
            } else {
                return getPath(node.getParent());
            }
        }

    }

    /**
     * Gather all view's metadata children nodes dynamic and get their label.
     * <p>
     * DOC YeXiaowei Comment method "isMetadataLabel".
     * 
     * @param label
     * @return
     */
    private static boolean isMetadataLabel(final String label) {

        RepositoryView view = (RepositoryView) RepositoryView.show();
        if (view == null) {
            return false;
        }

        String[] metadataLabels = view.gatherMetadataChildenLabels();
        if (metadataLabels == null || metadataLabels.length <= 0) {
            return false;
        }

        for (String mlabel : metadataLabels) {
            if (mlabel.equals(label)) {
                return true;
            }
        }

        return false;

    }

    /**
     * 
     * ggu Comment method "getPath".
     * 
     * get path by repository item id. can't get the folders.
     */
    public static IPath getPath(final String id) {
        if (id == null || "".equals(id) || RepositoryNode.NO_ID.equals(id)) {
            return null;
        }
        IProxyRepositoryFactory factory = RepositoryPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        try {
            final IRepositoryObject lastVersion = factory.getLastVersion(id);
            return getPath(lastVersion);
        } catch (PersistenceException e) {
            //
        }
        return null;
    }

    public static IPath getPath(IRepositoryObject curNode) {
        if (curNode == null) {
            return null;
        }
        final RepositoryNode repositoryNode = getRepositoryNode(curNode);
        if (repositoryNode != null) {
            return getPath(repositoryNode);
        }
        return null;
    }

    /**
     * 
     * ggu Comment method "getRepositoryNode".
     * 
     * @param id
     * @return the repository node by id
     */
    public static RepositoryNode getRepositoryNode(final String id) {
        return getRepositoryNode(id, true);
    }

    public static RepositoryNode getRepositoryNode(final String id, boolean expanded) {
        if (id == null || "".equals(id) || RepositoryNode.NO_ID.equals(id)) {
            return null;
        }
        IProxyRepositoryFactory factory = RepositoryPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        try {
            final IRepositoryObject lastVersion = factory.getLastVersion(id);
            if (lastVersion != null) {
                return getRepositoryNode(lastVersion, expanded);
            }
        } catch (PersistenceException e) {
            //
        }
        return null;
    }

    /**
     * 
     * ggu Comment method "getRepositoryNode".
     * 
     * get the repository node by a IRepositoryObject.
     */
    public static RepositoryNode getRepositoryNode(IRepositoryObject curNode) {
        return getRepositoryNode(curNode, true);
    }

    public static RepositoryNode getRepositoryNode(IRepositoryObject curNode, boolean expanded) {
        if (curNode == null) {
            return null;
        }
        IRepositoryView view = RepositoryView.show();
        if (view == null) {
            return null;
        }
        return getRepositoryNode(view.getRoot(), curNode, view, expanded);
    }

    private static RepositoryNode getRepositoryNode(RepositoryNode rootNode, IRepositoryObject curNode, IRepositoryView view,
            boolean expanded) {
        if (rootNode == null || curNode == null || view == null) {
            return null;
        }
        if (expanded) {
            // expande the unvisible node
            expandNode(rootNode, curNode, view);
        }

        final List<RepositoryNode> children = rootNode.getChildren();

        if (children != null) {
            // in the first, search the current folder
            List<RepositoryNode> folderChild = new ArrayList<RepositoryNode>();

            for (RepositoryNode childNode : children) {
                if (isRepositoryFolder(childNode) || childNode.getType() == ENodeType.REFERENCED_PROJECT) {
                    folderChild.add(childNode);
                } else if (childNode.getId().equals(curNode.getId()) && childNode.getObjectType() == curNode.getType()) {
                    return childNode;
                }

            }
            for (RepositoryNode folderNode : folderChild) {
                final RepositoryNode repositoryNode = getRepositoryNode(folderNode, curNode, view, expanded);
                if (repositoryNode != null) {
                    return repositoryNode;
                }
            }
        }

        return null;
    }

    private static void expandNode(RepositoryNode rootNode, IRepositoryObject curNode, IRepositoryView view) {
        if (rootNode == null || curNode == null || view == null) {
            return;
        }
        final ERepositoryObjectType rootContextType = rootNode.getContentType();
        // for referenced project
        if (rootContextType == ERepositoryObjectType.REFERENCED_PROJECTS || rootNode.getType() == ENodeType.REFERENCED_PROJECT) {
            expandParentNode(view, rootNode);
        }
        if (rootContextType != null) {
            final ERepositoryObjectType curType = curNode.getType();
            ERepositoryObjectType tmpType = null;
            switch (curType) {
            // case METADATA_CON_TABLE:
            // case METADATA_CON_VIEW:
            // case METADATA_CON_SYNONYM:
            // case METADATA_CON_QUERY:
            case METADATA_CONNECTIONS:
            case METADATA_FILE_DELIMITED:
            case METADATA_FILE_POSITIONAL:
            case METADATA_FILE_REGEXP:
            case METADATA_FILE_XML:
            case METADATA_FILE_LDIF:
            case METADATA_FILE_EXCEL:
            case METADATA_GENERIC_SCHEMA:
            case METADATA_LDAP_SCHEMA:
            case METADATA_SALESFORCE_SCHEMA:
            case METADATA_WSDL_SCHEMA:
                tmpType = ERepositoryObjectType.METADATA;
                break;
            // case ROUTINES:
            // case SNIPPETS:
            // tmpType = ERepositoryObjectType.ROUTINES;
            // break;
            // case DOCUMENTATION:
            // case JOB_DOC:
            // case JOBLET_DOC:
            // tmpType = ERepositoryObjectType.DOCUMENTATION;
            // //
            // break;
            default:
            }

            if (tmpType != null && tmpType == rootContextType) {
                expandParentNode(view, rootNode);
            }
            // expand the parent node

            if (curType == rootContextType && isRepositoryFolder(rootNode)) {
                if (rootContextType == ERepositoryObjectType.SQLPATTERNS
                        && !(rootNode.getParent() instanceof ProjectRepositoryNode)) {
                    // sql pattern
                } else {
                    expandParentNode(view, rootNode);
                    view.getViewer().refresh();
                }
            }

        }
    }

    private static void expandParentNode(IRepositoryView view, RepositoryNode node) {
        if (view == null || node == null) {
            return;
        }
        expandParentNode(view, node.getParent());
        view.expand(node, true);
    }

    private static boolean isRepositoryFolder(RepositoryNode node) {
        if (node == null) {
            return false;
        }
        final ENodeType type = node.getType();
        if (type == ENodeType.SIMPLE_FOLDER || type == ENodeType.STABLE_SYSTEM_FOLDER || type == ENodeType.SYSTEM_FOLDER) {
            return true;
        }
        return false;
    }

    public static RepositoryNode getSchemeFromConnection(String schemaValue) {
        String[] values = schemaValue.split(" - ");
        String repositoryID = values[0];
        String tableName = values[1];

        try {
            final RepositoryNode realNode = getRepositoryNode(repositoryID);
            return getSchemeFromConnection(realNode, tableName);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return null;
    }

    private static RepositoryNode getSchemeFromConnection(RepositoryNode connection, String tableName) {
        ERepositoryObjectType type = connection.getObject().getType();
        if (type == ERepositoryObjectType.METADATA_CONNECTIONS) {
            connection = connection.getChildren().get(0);
        }
        // if(connection.getObject().getProperty().getItem() instanceof ConnectionItem){
        //            
        // }
        for (RepositoryNode node : connection.getChildren()) {
            if (node.getObject().getLabel().equals(tableName)) {
                return node;
            }
        }
        return null;
    }

    /**
     * 
     * ggu Comment method "getParentRepositoryNodeFromSelection".
     * 
     */
    public static RepositoryNode getParentRepositoryNodeFromSelection(IRepositoryObject object) {
        IRepositoryView viewPart = RepositoryView.show();
        ISelection repositoryViewSelection = viewPart.getViewer().getSelection();

        if (repositoryViewSelection instanceof IStructuredSelection) {
            RepositoryNode selectedRepositoryNode = (RepositoryNode) ((IStructuredSelection) repositoryViewSelection)
                    .getFirstElement();
            // fixed for the opened job and lost the selected node.
            if (object != null && selectedRepositoryNode == null) {
                selectedRepositoryNode = getRepositoryNode(object);
            }
            if (selectedRepositoryNode != null) {
                return selectedRepositoryNode.getParent();
            }
        }
        return null;

    }
}
