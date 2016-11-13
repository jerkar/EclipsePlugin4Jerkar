package org.jerkar.eclipseplugin.window;



import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

class ExtraInfoComponent {
    
    public final Composite composite;
    
    private Text text;
    
    public String getDefinition() {
        return this.text.getText();
    }

    public ExtraInfoComponent(Composite parent, String definition) {
        this.composite = new Composite(parent, SWT.NONE);
        this.composite.setLayout(new FillLayout());
        methodDescription(this.composite, definition);
    }
    
    private Group methodDescription(Composite parent, String description) {
        Group group = new Group(parent, SWT.NONE);
        FillLayout layout = new FillLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        group.setLayout(layout);
        group.setText("What does this method ?");
        this.text = new Text(group, SWT.BORDER | SWT.MULTI);
        text.setText(description);;
        return group;
    }  
    
    
    
}
