package org.jerkar.eclipseplugin.menu;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class MethodDescriptions implements Iterable<MethodDescription> {
    
    private List<MethodDescription> list = new LinkedList<>();
    
    void add(MethodDescription description) {
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
    
    void sort() {
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
