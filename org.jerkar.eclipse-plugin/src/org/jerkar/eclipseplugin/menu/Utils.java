package org.jerkar.eclipseplugin.menu;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

class Utils {
    
    static IProject currentProject() {
        ISelectionService iSelectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getSelectionService();
        IStructuredSelection selection = (IStructuredSelection) iSelectionService.getSelection();
        IAdaptable adaptable = (IAdaptable) selection.getFirstElement();
        if (adaptable instanceof IResource) {
            return ((IResource) adaptable).getProject();
        }
        if (adaptable == null) {
        	return null;
        }
        IProject iProject = adaptable.getAdapter(IProject.class);
        if (iProject == null) {
            IJavaElement iResource = adaptable.getAdapter(IJavaElement.class);
            iProject = iResource.getResource().getProject();
        }
        return iProject;
    }

}
