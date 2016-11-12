package org.jerkar.eclipseplugin.menu;

import org.eclipse.swt.widgets.Display;

public class CommandWindowRunner {

    public static void main(String[] args) {
        Display display = new Display();
        CommandInfo commandInfo = new CommandInfo();
        commandInfo.methodDescription = new MethodDescription("toto",  "blablablabla"); 
        new MethodShell(display).open(commandInfo, null);
        display.dispose();
    }
    
}
