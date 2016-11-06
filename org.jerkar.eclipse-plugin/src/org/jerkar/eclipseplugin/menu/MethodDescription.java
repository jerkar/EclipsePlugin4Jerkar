package org.jerkar.eclipseplugin.menu;

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
                return null;
            }
            Object[] value = (Object[]) pairs[0].getValue(); 
            return (String) value[0];
        }
        return null;
    }

}
