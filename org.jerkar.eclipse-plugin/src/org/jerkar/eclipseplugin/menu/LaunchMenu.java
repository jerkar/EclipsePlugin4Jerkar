package org.jerkar.eclipseplugin.menu;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.jerkar.eclipseplugin.Activator;
import org.jerkar.eclipseplugin.model.MethodDescription;
import org.jerkar.eclipseplugin.model.MethodDescriptions;
import org.jerkar.eclipseplugin.model.MethodInfo;
import org.jerkar.eclipseplugin.model.UtilsXml;
import org.jerkar.eclipseplugin.utils.JerkarHelper;
import org.jerkar.eclipseplugin.window.RunDialog;
import org.w3c.dom.Document;

public class LaunchMenu extends ContributionItem {

    private static final IPath DEF_PATH = Path.forPosix("build/def");

    private static MethodDescriptions defaultMethods;

    public LaunchMenu() {
    }

    public LaunchMenu(String id) {
        super(id);
    }

    @Override
    public void fill(Menu menu, int index) {
        // Here you could get selection and decide what to do
        // You can also simply return if you do not want to show a menu

        IProject project = Utils.currentProject();
        fill(menu, index, project);
    }

    static void fill(final Menu menu, int index, IProject project) {
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
        IProject project = javaProject.getProject();
        final MethodDescriptions methods;
        MenuItem title = new MenuItem(menu, SWT.CASCADE, i);
        if (typeHierarchy != null) {
            methods = getPublicNoArgMethods(typeHierarchy);
            title.setText(typeHierarchy.getType().getFullyQualifiedName());
        } else {
            methods = getMethods();
            title.setText("JkJavaBuild");
        }
        methods.sort();
        title.setToolTipText("Build class");
        title.setEnabled(false);
        i++;
        new MenuItem(menu, SWT.SEPARATOR, i);
        i++;
        for (final MethodDescription methodDescription : methods) {
            MenuItem menuItem = new MenuItem(menu, SWT.CHECK, i);
            menuItem.setText(methodDescription.getName());
            final MethodInfo methodInfo = new MethodInfo(methodDescription, project);
            menuItem.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    new RunDialog(menu.getShell(), methodInfo).open();
                }
            });
            menuItem.setToolTipText(methodDescription.getDefinition());
            index++;
        }
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

                        // TODO fined the corresponding binary type in order to
                        // read the JkDoc annotation.
                        IType bynaryType = type;

                        // We want to get the first compiled class in this
                        // fragment
                        boolean lesser = selectedType == null
                                || name.compareTo(selectedType.getFullyQualifiedName()) < 0;
                        if (lesser && !Flags.isAbstract(selectedType.getFlags())) {

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
            if (!Flags.isAbstract(method.getFlags()) && method.getNumberOfParameters() == 0
                    && Flags.isPublic(method.getFlags()) && method.getReturnType().endsWith(Signature.SIG_VOID)
                    && !method.isConstructor() && !method.getElementName().equals("init")) {
                result.add(new MethodDescription(method));
            }
        }
        return result;
    }

    private static boolean isJavaProject(IProject project) {
        try {
            return project.hasNature("org.eclipse.jdt.core.javanature");
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodDescriptions getMethods() {
        if (defaultMethods != null) {
            return defaultMethods;
        }
        File file = null;
        try {
            file = File.createTempFile("jerkarpluginmethod", "xml");
            Process process = JerkarHelper.processBuilder("help", "-help.xmlFile=\"" + file.getAbsolutePath() + "\"").inheritIO()
                    .start();
            process.waitFor();
            Document document = UtilsXml.documentFrom(file);
            MethodDescriptions result = MethodDescriptions.fromXml(document);
            defaultMethods = result;
            file.delete();
            return result;
        } catch (Exception e) {
            ILog log = Activator.getDefault().getLog();
            log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error while parsing " + file.getAbsolutePath(), e));
            defaultMethods = new MethodDescriptions();
            return defaultMethods;
        }

    }

}