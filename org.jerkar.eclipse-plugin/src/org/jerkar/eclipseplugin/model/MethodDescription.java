package org.jerkar.eclipseplugin.model;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

public class MethodDescription {

    private final String name;

    private final String definition;

    public MethodDescription(String name, String definition) {
        super();
        this.name = name;
        this.definition = definition;
    }

    public MethodDescription(IMethod method) throws JavaModelException {
        this(method.getElementName(), description(method));
    }

    public String getName() {
        return name;
    }

    public String getDefinition() {
        return definition;
    }

    private static String description(IMethod method) throws JavaModelException {
        IAnnotation annotation = method.getAnnotation("org.jerkar.tool.JkDoc");
        if (annotation.exists()) {
            IMemberValuePair[] pairs = annotation.getMemberValuePairs();
            if (pairs.length == 0) {
                return "";
            }
            Object[] value = (Object[]) pairs[0].getValue(); 
            return singleString(value);
        }
        return "";
    }
    
    public boolean hasDefinition() {
        return definition != null && !definition.trim().equals("");
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MethodDescription other = (MethodDescription) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    private static String singleString(Object[] lines) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i< lines.length; i++) {
            result.append(lines[i].toString());
            if ( i+1 < lines.length) {
                result.append("\n");
            }
        }
        return result.toString();
    }

}
