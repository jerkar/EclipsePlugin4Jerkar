package org.jerkar.eclipseplugin.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class MethodDescriptions implements Iterable<MethodDescription> {
	
	public static MethodDescriptions fromXml(Document document) {
		Element buildEl = document.getDocumentElement();
		Element methodsEl = UtilsXml.directChild(buildEl, "methods");
		MethodDescriptions result = new MethodDescriptions();
		for (Element methodEl : UtilsXml.directChildren(methodsEl, "method")) {
			String name = UtilsXml.directChildText(methodEl, "name");
			String description = UtilsXml.directChildText(methodEl, "description");
			result.add(new MethodDescription(name, description));
		}
		return result;
	}
    
    private List<MethodDescription> list = new LinkedList<>();
    
    public void add(MethodDescription description) {
        int indexOfExisting = list.indexOf(description); 
        if (indexOfExisting >= 0) {
            MethodDescription existing = list.get(indexOfExisting);
            if (!existing.hasDefinition()) {
               list.remove(indexOfExisting);
               list.add(description);
            } 
            return;
        }
        this.list.add(description);
    }
    
    public void addAll(MethodDescriptions other) {
        for (MethodDescription description : other) {
            this.add(description);
        }
    }
    
    public void sort() {
        Collections.sort(list, new MethodComparator());
    }

    @Override
    public Iterator<MethodDescription> iterator() {
        return list.iterator();
    }
    
    private static class MethodComparator implements Comparator<MethodDescription> {

        @Override
        public int compare(MethodDescription o1, MethodDescription o2) {
            return - o1.getName().compareTo(o2.getName());
        }
        
    }
    
    @Override
    public String toString() {
        return list.toString();
    }

}
