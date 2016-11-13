package org.jerkar.eclipseplugin.window;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jerkar.eclipseplugin.model.MethodInfo;
import org.jerkar.eclipseplugin.model.MethodDescription;
import org.jerkar.eclipseplugin.window.RunDialog;

public class RunDialogRunner {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        MethodInfo methodInfo = new MethodInfo( new MethodDescription("toto",  "blablablabla"), null);
        new RunDialog(shell, methodInfo).open();
        display.dispose();
    }
    
}
