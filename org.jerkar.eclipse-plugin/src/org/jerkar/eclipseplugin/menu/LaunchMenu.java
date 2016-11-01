package org.jerkar.eclipseplugin.menu;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

public class LaunchMenu extends ContributionItem {

    private static final IPath DEF_PATH = Path.forPosix("build/def");

    public LaunchMenu() {
    }

    public LaunchMenu(String id) {
        super(id);
    }

    @Override
    public void fill(Menu menu, int index) {
        // Here you could get selection and decide what to do
        // You can also simply return if you do not want to show a menu

        IProject project = project();
        if (project instanceof IJavaProject) {

        }

        // create the menu item
        MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
        menuItem.setText("My menu item (" + new Date() + ")");
        menuItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                // what to do when menu is subsequently selected.
                System.err.println("Dynamic menu selected");
            }
        });
    }

    private static void fill(Menu menu, int index, IJavaProject javaProject) throws JavaModelException {
        IType mainBuildType = getBuildClassType(javaProject).getType();
        List<IMethod> methods = new LinkedList<>();
        for (IMethod method : mainBuildType.getMethods()) {
            
        }
    }

    private static IProject project() {
        ISelectionService iSelectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getSelectionService();
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

    private static ITypeHierarchy getBuildClassType(IJavaProject javaProject) throws JavaModelException {
        IType jkBuildType = javaProject.findType("org.jerkar.tool.JkBuild");
        for (IPackageFragmentRoot fragmentRoot : javaProject.getPackageFragmentRoots()) {
            IPath path = fragmentRoot.getPath();
            if (fragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE && path.equals(DEF_PATH)) {
                IType selectedType = null;
                ITypeHierarchy selectedHierarchy = null;
                for (IJavaElement javaElement : fragmentRoot.getChildren()) {
                    IPackageFragment packageFragment = (IPackageFragment) javaElement;
                    for (ICompilationUnit compilationUnit : packageFragment.getCompilationUnits()) {
                        IType type = compilationUnit.getTypes()[0];
                        String name = type.getFullyQualifiedName();
                        boolean lesser = name.compareTo(selectedType.getFullyQualifiedName()) < 0;
                        if (selectedType == null || lesser) {
                            ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(null);
                            for (IType type2 : typeHierarchy.getAllSuperclasses(type)) {
                                if (type2.equals(jkBuildType)) {
                                    selectedType = type;
                                    selectedHierarchy = typeHierarchy;
                                    break;
                                }
                            }
                        }
                    }

                }
                return selectedHierarchy;
            }
        }
        return null;
    }

   
}