package org.jerkar.eclipseplugin.menu;

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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;
import org.jerkar.eclipseplugin.commands.JerkarExecutor;

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

    private static void fill(final Menu menu, int index, IJavaProject javaProject) throws JavaModelException {
        ITypeHierarchy typeHierarchy = getBuildClassType(javaProject);
        int i = index;
        int count =populateLastCommands(menu, index, javaProject.getProject());
        i += count;
        if (count > 0) {
            new MenuItem(menu, SWT.SEPARATOR, i);
            i ++;
        }
        MethodDescriptions methods = getPublicNoArgMethods(typeHierarchy);
        methods.sort();
        MenuItem title = new MenuItem(menu, SWT.CASCADE, i);
        title.setText(typeHierarchy.getType().getFullyQualifiedName());
        title.setToolTipText("Build class");
        title.setEnabled(false);
        i ++;
        new MenuItem(menu, SWT.SEPARATOR, i);
        i ++;
        final LocalRunHandler executor = new LocalRunHandler(javaProject.getProject());
        for (final MethodDescription methodDescription : methods) {
            MenuItem menuItem = new MenuItem(menu, SWT.CHECK, i);
            menuItem.setText(methodDescription.getName());
            final CommandInfo commandInfo = new CommandInfo();
            commandInfo.methodDescription = methodDescription;
            menuItem.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    new MethodShell(PlatformUI.getWorkbench().getDisplay()).open(commandInfo, executor);
                }
            });
            menuItem.setToolTipText(methodDescription.getDefinition());
            index++;
        }
    }
    
    private static int populateLastCommands(Menu menu, int index, final IProject project) {
        List<MethodDescription> commands = LastCommands.INSTANCE.commands(project);
        for (final MethodDescription methodDescription : commands) {
            MenuItem menuItem = new MenuItem(menu, SWT.PUSH, index);
            menuItem.setText(methodDescription.getName());
            menuItem.setToolTipText(methodDescription.getDefinition());
            final CommandInfo commandInfo = new CommandInfo();
            commandInfo.methodDescription = methodDescription;
            menuItem.addSelectionListener(new SelectionAdapter() {
                
                @Override
                public void widgetSelected(SelectionEvent e) {
                    JerkarExecutor.runCmdLine(project, methodDescription.getName());
                }
                


            });
            index++;
        }
        return index;
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
                        
                        // TODO fined the corresponding binary type in order to read the JkDoc annotation.
                        IType bynaryType = type;

                        // We want to get the first compiled class in this
                        // fragment
                        boolean lesser = selectedType == null
                                || name.compareTo(selectedType.getFullyQualifiedName()) < 0;
                        if (lesser) {
                            
                            ITypeHierarchy typeHierarchy = bynaryType.newSupertypeHierarchy(null);
                            for (IType type2 : typeHierarchy.getAllSuperclasses(bynaryType)) {
                                if (type2.equals(jkBuildType)) {
                                    selectedType = bynaryType;
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

    private static MethodDescriptions getPublicNoArgMethods(ITypeHierarchy typeHierarchy) throws JavaModelException {
        MethodDescriptions result = new MethodDescriptions();
        result.addAll(getPublicNoArgMethods(typeHierarchy.getType()));
        for (IType type : typeHierarchy.getAllSuperclasses(typeHierarchy.getType())) {
            if (!type.getFullyQualifiedName().equals(Object.class.getName())) {
                result.addAll(getPublicNoArgMethods(type));
            }
        }
        return result;

    }

    private static MethodDescriptions getPublicNoArgMethods(IType type) throws JavaModelException {
        MethodDescriptions result = new MethodDescriptions();
        for (IMethod method : type.getMethods()) {
            if (!Flags.isAbstract(method.getFlags()) 
                    && method.getNumberOfParameters() == 0
                    && Flags.isPublic(method.getFlags()) 
                    && method.getReturnType().endsWith(Signature.SIG_VOID)
                    && !method.isConstructor()
                    && !method.getElementName().equals("init")) {
                result.add(new MethodDescription(method));
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
    
    private static class LocalRunHandler implements RunHandler {
        
        private final IProject iProject;

        public LocalRunHandler(IProject iProject) {
            super();
            this.iProject = iProject;
        }

        @Override
        public void process(String commandLine, String definition) {
           LastCommands.INSTANCE.put(iProject, new MethodDescription(commandLine, definition));
           JerkarExecutor.runCmdLine(iProject, commandLine);
        }
       
    }
    
   

}