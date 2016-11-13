package org.jerkar.eclipseplugin.window;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ScaffoldDialogRunner {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        ScaffoldDialog scaffoldDialog = new ScaffoldDialog(shell, null);
        scaffoldDialog.open();
        display.dispose();
    }
    
}
