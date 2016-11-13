package org.jerkar.eclipseplugin.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jerkar.eclipseplugin.Activator;

public class LastCommands {
    
    private static final String STORE_PATH = ".settings/org.jerkar.plugin.lastcommands.properties";

    public static final LastCommands INSTANCE = new LastCommands();

    private Map<IProject, List<MethodDescription>> allCommands = new HashMap<>();

    public List<MethodDescription> commands(IProject project) {
        List<MethodDescription> result = allCommands.get(project);
        if (result == null) {
            load(project);
            result = allCommands.get(project);
            if (result == null) {
                return new LinkedList<>();
            }
        }
        return Collections.unmodifiableList(result);
    }
    
    public void remove(IProject project, String name) {
        List<MethodDescription> commands = allCommands.get(project);
        if (commands == null) {
            return;
        }
        commands.remove(new MethodDescription(name, ""));
    }

    public void put(IProject project, MethodDescription methodDescription) {
        List<MethodDescription> commands = allCommands.get(project);
        if (commands == null) {
            commands = new LinkedList<>();
            allCommands.put(project, commands);
        }
        if (commands.contains(methodDescription)) {
            commands.remove(methodDescription);    
        }
        commands.add(0, methodDescription);
        if (commands.size() > 6) {
            commands.remove(commands.size() -1);
        }
        save(project);
    }
    
    private void save(IProject project) {
        Properties properties = new Properties();
        List<MethodDescription> descriptions = this.commands(project);
        for (int i = 0; i < descriptions.size(); i++) {
            String keyName = "" + i + ".name";
            String valueName = descriptions.get(i).getName();
            properties.put(keyName, valueName);
            String keyDef = "" + i + ".def";
            String valueDef = descriptions.get(i).getDefinition();
            properties.put(keyDef, valueDef);
        }
        properties.put("count", ""+ descriptions.size());
        File root = project.getLocation().toFile();
        File storeFile = new File(root, STORE_PATH);
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(storeFile);
            properties.store(fileWriter, null);
        } catch (IOException e) {
            ILog log = Activator.getDefault().getLog();
            log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error while loading " + STORE_PATH, e));
        }
        
    }
    
    private void load(IProject project) {
        Properties properties = new Properties();
        File root = project.getLocation().toFile();
        File storeFile = new File(root, STORE_PATH);
        if (!storeFile.exists()) {
            return;
        }
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(storeFile);
            properties.load(fileInputStream);
        } catch (Exception e) {
            ILog log = Activator.getDefault().getLog();
            log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error while loading " + STORE_PATH, e));
            return;
        }
        String countString = properties.getProperty("count");
        if (countString == null) {
            return;
        }
        int count;
        try {
            count = Integer.parseInt(countString);
        } catch (RuntimeException e) {
            ILog log = Activator.getDefault().getLog();
            log.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to parse count property to integer : " + countString, e));
            return;
        }
        for (int i=count-1; i >= 0; i--) {
            String keyName = "" + i + ".name";
            String keyDef = "" + i + ".def";
            String valueName = properties.getProperty(keyName);
            String valueDef = properties.getProperty(keyDef);
            MethodDescription description = new MethodDescription(valueName, valueDef);
            this.put(project, description);
        }
    }
    
    

}
