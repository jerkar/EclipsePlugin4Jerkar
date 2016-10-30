package org.jerkar.eclipseplugin.menu;

import java.util.Date;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

public class LaunchMenu extends ContributionItem {

    public LaunchMenu() {
    }

    public LaunchMenu(String id) {
        super(id);
    }

    @Override
    public void fill(Menu menu, int index) {
        //Here you could get selection and decide what to do
        //You can also simply return if you do not want to show a menu

        IProject project = project();
        if (project instanceof IJavaProject) {
            
        }
        
        //create the menu item
        MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
        menuItem.setText("My menu item (" + new Date() + ")");
        menuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                //what to do when menu is subsequently selected.
                System.err.println("Dynamic menu selected");
            }
        });
    }
    
    private static void fill(Menu menu, int index, IJavaProject javaProject) throws JavaModelException {
        IType jerkarMainType = javaProject.findType("org.jerkar.Main");
        if (jerkarMainType != null) {
            
        }
    }
    
    private static IProject project() {
        ISelectionService iSelectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
        IStructuredSelection selection = (IStructuredSelection) iSelectionService.getSelection();
        IAdaptable adaptable = (IAdaptable) selection.getFirstElement();
        System.out.println("adaptable class " + adaptable.getClass());
        System.out.println("adaptable " + adaptable);
        if (adaptable instanceof IResource) {
            return ((IResource) adaptable).getProject();
        }
        IProject iProject = adaptable.getAdapter(IProject.class);
        if (iProject == null) {
            IJavaElement iResource = adaptable.getAdapter(IJavaElement.class);
            iProject = iResource.getResource().getProject();
        }
        return iProject;
        
    }
}