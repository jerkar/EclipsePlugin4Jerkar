package org.jerkar.eclipseplugin.model;

import org.eclipse.core.resources.IProject;

public class MethodInfo {
    
    public final MethodDescription methodDescription;
    
    public final IProject iProject;

    public MethodInfo(MethodDescription methodDescription, IProject iProject) {
        super();
        this.methodDescription = methodDescription;
        this.iProject = iProject;
    }
    
    

}
