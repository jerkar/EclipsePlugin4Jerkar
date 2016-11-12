package org.jerkar.eclipseplugin.menu;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;

public class LastCommands {

    public static final LastCommands INSTANCE = new LastCommands();

    private Map<IProject, List<MethodDescription>> allCommands = new HashMap<>();

    public List<MethodDescription> commands(IProject project) {
        List<MethodDescription> result = allCommands.get(project);
        if (result == null) {
            return new LinkedList<>();
        }
        return Collections.unmodifiableList(result);
    }

    public void put(IProject project, MethodDescription methodDescription) {
        List<MethodDescription> commands = allCommands.get(project);
        if (commands == null) {
            commands = new LinkedList<>();
            allCommands.put(project, commands);
        }
        if (!commands.contains(methodDescription)) {
            commands.add(methodDescription);
        }
    }

}
