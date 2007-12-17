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
package org.talend.componentdesigner.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.talend.componentdesigner.ImageLib;
import org.talend.componentdesigner.ui.wizard.copycomponent.CopyComponentWizard;

/**
 * @author rli
 *
 */
public class CopyComponentActionProvider  extends CommonActionProvider {
//	private static final String NEW_MENU_NAME = "common.new.menu"; //$NON-NLS-1$

	private IAction copyProjectAction;

	public void init(ICommonActionExtensionSite anExtensionSite) {

		if (anExtensionSite.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			copyProjectAction = new CopyComponentAction();
		}
	}

	/**
	 * Adds a submenu to the given menu with the name "New Component".
	 */
	public void fillContextMenu(IMenuManager menu) {
//		IMenuManager submenu = new MenuManager("New", NEW_MENU_NAME);
		menu.insertBefore("group.edit", copyProjectAction);
		for (IContributionItem item : menu.getItems()) {
			if (item == null || item.getId() == null) {
				continue;
			}
			if (item.getId().equals("export") || item.getId().equals("import")) {
				menu.remove(item);
			}
		}

		// append the submenu after the GROUP_NEW group.
//		menu.insertAfter(ICommonMenuConstants.GROUP_NEW, submenu);
	}
	
	/**
	 * @author rli
	 *
	 */
	class CopyComponentAction extends Action {

		public CopyComponentAction() {
			super("Copy Component");
			setImageDescriptor(ImageLib.getImageDescriptor(ImageLib.COPYCOMPONENT));
		}

		/*
		 * (non-Javadoc) Method declared on IAction.
		 */
		public void run() {
			CopyComponentWizard wizard = new CopyComponentWizard();
			WizardDialog dialog = new WizardDialog(null, wizard);
			dialog.open();
		}
	}
}
