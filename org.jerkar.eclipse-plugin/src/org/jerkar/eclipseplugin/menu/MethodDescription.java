package org.jerkar.eclipseplugin.menu;

public class MethodDescription {
    
    private final String name;

    private final String definition;

    public MethodDescription(String name, String definition) {
        super();
        this.name = name;
        this.definition = definition;
    }
    
    public String getName() {
        return name;
    }

    public String getDefinition() {
        return definition;
    }
  
}
