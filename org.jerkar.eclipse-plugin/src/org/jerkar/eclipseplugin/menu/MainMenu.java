package org.jerkar.eclipseplugin.menu;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.jerkar.eclipseplugin.commands.JerkarExecutor;
import org.jerkar.eclipseplugin.model.LastCommands;
import org.jerkar.eclipseplugin.model.MethodDescription;
import org.jerkar.eclipseplugin.model.MethodInfo;
import org.jerkar.eclipseplugin.window.EditDialog;
import org.jerkar.eclipseplugin.window.ScaffoldDialog;

public class MainMenu extends ContributionItem {

    public MainMenu() {
    }

    public MainMenu(String id) {
        super(id);
    }

    @Override
    public void fill(Menu menu, int index) {
        IProject project = Utils.currentProject();
        if (project == null) {
        	return;
        }
        int newIndex = lastCommands(menu, index, project);
        if (newIndex > index) {
            new MenuItem(menu, SWT.SEPARATOR);
        }
        launch(menu, newIndex + 1, project);
        updateClasspath(menu, newIndex + 2, project);
        scaffold(menu, newIndex + 3, project);
    }

    private static int lastCommands(final Menu menu, int index, final IProject project) {
        final List<MethodDescription> commands = LastCommands.INSTANCE.commands(project);
        for (final MethodDescription methodDescription : commands) {
            MenuItem menuItem = new MenuItem(menu, SWT.PUSH, index);
            menuItem.setText(methodDescription.getName());
            menuItem.setToolTipText(methodDescription.getDefinition());
            menuItem.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (e.stateMask == SWT.CTRL || e.stateMask == SWT.SHIFT) {
                        new EditDialog(menu.getShell(), new MethodInfo(methodDescription, project)).open();
                    } else {
                        LastCommands.INSTANCE.put(project, methodDescription);
                        JerkarExecutor.runCmdLine(project, methodDescription.getName(), null);
                    }
                }

            });
            index++;
        }
        return index;
    }

    private static void launch(Menu menu, int index, IProject project) {
        MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
        menuItem.setText("Launch");
        Menu submenu = new Menu(menu);
        menuItem.setMenu(submenu);
        LaunchMenu.fill(submenu, 0, project);
    }

    private static void updateClasspath(Menu menu, int index, final IProject project) {
        MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText("Update Classpath");
        menuItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                JerkarExecutor.runCmdLine(project, "eclipse#generateFiles", null);
            }

        });
    }

    private static void scaffold(final Menu menu, int index, final IProject project) {
        MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText("Scaffold");
        menuItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                new ScaffoldDialog(menu.getShell(), project).open();
            }

        });
    }

}
