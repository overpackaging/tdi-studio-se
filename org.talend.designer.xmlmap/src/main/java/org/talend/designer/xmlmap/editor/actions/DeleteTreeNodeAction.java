// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.xmlmap.editor.actions;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractInOutTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class DeleteTreeNodeAction extends SelectionAction {

    private boolean input;

    private MapperManager mapperManager;

    // private List<TreeNode> nodesNeedToChangeMainStatus = new ArrayList<TreeNode>();

    public static final String ID = "org.talend.designer.xmlmap.editor.actions.DeleteTreeNodeAction";

    public DeleteTreeNodeAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
        setText("Delete");
    }

    public void update(Object selection) {
        setSelection(new StructuredSelection(selection));
    }

    @Override
    protected boolean calculateEnabled() {
        // nodesNeedToChangeMainStatus.clear();
        if (getSelectedObjects().isEmpty()) {
            return false;
        } else {
            boolean enable = true;
            for (Object obj : getSelectedObjects()) {
                if (obj instanceof TreeNodeEditPart) {
                    TreeNodeEditPart nodePart = (TreeNodeEditPart) obj;
                    TreeNode treeNode = (TreeNode) nodePart.getModel();
                    int xPathLength = XmlMapUtil.getXPathLength(treeNode.getXpath());
                    if (xPathLength <= 2) {
                        enable = false;
                    }
                    // can't delete root
                    if (treeNode.eContainer() instanceof TreeNode
                            && XmlMapUtil.DOCUMENT.equals(((TreeNode) treeNode.eContainer()).getType())) {
                        enable = false;
                    }

                } else {
                    enable = false;
                }
            }
            return enable;
        }
    }

    @Override
    public void run() {
        try {
            TreeNode docRoot = null;
            for (Object obj : getSelectedObjects()) {
                TreeNodeEditPart nodePart = (TreeNodeEditPart) obj;
                TreeNode treeNode = (TreeNode) nodePart.getModel();
                if (treeNode.eContainer() instanceof TreeNode) {
                    TreeNode parent = (TreeNode) treeNode.eContainer();
                    if (input) {
                        // find root before remove
                        if (docRoot == null) {
                            docRoot = XmlMapUtil.getInputTreeNodeRoot(parent);
                        }
                    } else {
                        docRoot = XmlMapUtil.getOutputTreeNodeRoot((OutputTreeNode) parent);
                    }
                    XmlMapUtil.detachNodeConnections(treeNode, mapperManager.getCopyOfMapData(), true);

                    parent.getChildren().remove(treeNode);
                }

            }
            if (mapperManager != null) {
                if (input) {
                    if (docRoot != null && docRoot.eContainer() instanceof InputXmlTree) {
                        mapperManager.refreshInputTreeSchemaEditor((InputXmlTree) docRoot.eContainer());
                    }
                } else {
                    if (docRoot != null && docRoot.eContainer() instanceof OutputXmlTree) {
                        mapperManager.refreshOutputTreeSchemaEditor((OutputXmlTree) docRoot.eContainer());
                    }
                }

                if (docRoot != null && docRoot.eContainer() instanceof AbstractInOutTree) {
                    mapperManager.getProblemsAnalyser().checkLoopProblems((AbstractInOutTree) docRoot.eContainer());
                    mapperManager.getMapperUI().updateStatusBar();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    public void setMapperManager(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }
}
