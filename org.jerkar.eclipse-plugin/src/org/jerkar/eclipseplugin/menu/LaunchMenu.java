package org.jerkar.eclipseplugin.menu;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
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
        if (isJavaProject(project)) {
            IJavaProject javaProject = JavaCore.create(project);
            try {
                fill(menu, index, javaProject);
            } catch (JavaModelException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static void fill(Menu menu, int index, IJavaProject javaProject) throws JavaModelException {
        ITypeHierarchy typeHierarchy = getBuildClassType(javaProject);
        int i = index;
        List<IMethod> methods = getPublicNoArgMethods(typeHierarchy);
        Collections.sort(methods, new MethodComparator());
        for (IMethod method : methods) {
            MethodDescription methodDescription = new MethodDescription(method);
            MenuItem menuItem = new MenuItem(menu, SWT.CHECK, i);
            menuItem.setText(methodDescription.getName());
            menuItem.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    System.err.println("Dynamic menu selected");
                }
            });
            menuItem.setToolTipText(methodDescription.getDefinition());
            index++;
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
            IPath path = fragmentRoot.getPath().makeRelativeTo(javaProject.getPath());
            if (fragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE && path.equals(DEF_PATH)) {
                IType selectedType = null;
                ITypeHierarchy selectedHierarchy = null;
                for (IJavaElement javaElement : fragmentRoot.getChildren()) {
                    IPackageFragment packageFragment = (IPackageFragment) javaElement;
                    for (ICompilationUnit compilationUnit : packageFragment.getCompilationUnits()) {
                        IType type = compilationUnit.getTypes()[0];
                        String name = type.getFullyQualifiedName();

                        // We want to get the first compiled class in this
                        // fragment
                        boolean lesser = selectedType == null
                                || name.compareTo(selectedType.getFullyQualifiedName()) < 0;
                        if (lesser) {
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

    private static List<IMethod> getPublicNoArgMethods(ITypeHierarchy typeHierarchy) throws JavaModelException {
        List<IMethod> result = new LinkedList<>();
        result.addAll(getPublicNoArgMethods(typeHierarchy.getType()));
        for (IType type : typeHierarchy.getAllSuperclasses(typeHierarchy.getType())) {
            if (!type.getFullyQualifiedName().equals(Object.class.getName())) {
                result.addAll(getPublicNoArgMethods(type));
            }
        }
        return result;

    }

    private static List<IMethod> getPublicNoArgMethods(IType type) throws JavaModelException {
        List<IMethod> result = new LinkedList<>();
        for (IMethod method : type.getMethods()) {
            if (!Flags.isAbstract(method.getFlags()) 
                    && method.getNumberOfParameters() == 0
                    && Flags.isPublic(method.getFlags()) 
                    && method.getReturnType().endsWith(Signature.SIG_VOID)
                    && !method.isConstructor()
                    && !method.getElementName().equals("init")) {
                result.add(method);
            }
        }
        return result;
    }

    private boolean isJavaProject(IProject project) {
        try {
            return project.hasNature("org.eclipse.jdt.core.javanature");
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static class MethodComparator implements Comparator<IMethod> {

        @Override
        public int compare(IMethod o1, IMethod o2) {
            return - o1.getElementName().compareTo(o2.getElementName());
        }
        
    }

}