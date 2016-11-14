package org.jerkar.eclipseplugin.window;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.jerkar.eclipseplugin.commands.JerkarExecutor;

public class ScaffoldDialog extends TitleAreaDialog {

    private Button classicJavaProject;
    private Button springbootProject;
    
    private Button embeddedMode;
    
    private final IProject project;

    public ScaffoldDialog(Shell parentShell, IProject project) {
        super(parentShell);
        this.project = project;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Project Scaffolding");
        String projectName = project == null ? "your" : project.getName();
        setMessage("Let Jerkar create " + projectName + " project structure for you.", IMessageProvider.INFORMATION);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        container.setLayout(layout);

        createProjectType(container);
        createEmbeddedMode(container);

        return area;
    }

    private void createProjectType(Composite container) {
        
        Label projectTypeLabel = new Label(container, SWT.NONE);
        projectTypeLabel.setText("Project type");

        Composite composite = createProjectTypePanel(container);
        composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
    }
    
    private Composite createProjectTypePanel(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
        classicJavaProject = new Button(composite, SWT.RADIO);
        classicJavaProject.setText("Classic Java Project");
        classicJavaProject.setSelection(true);
        springbootProject = new Button(composite, SWT.RADIO);
        springbootProject.setText("Spring-Boot Project");
        return composite;
        
    }

    private void createEmbeddedMode(Composite container) {
        Label lbtLastName = new Label(container, SWT.NONE);
        lbtLastName.setText("Embed Jerkar ? ");
        this.embeddedMode = new Button(container, SWT.CHECK);   
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    

    @Override
    protected void okPressed() {
        StringBuilder cmdBuilder = new StringBuilder();
        if (this.springbootProject.getSelection()) {
            cmdBuilder.append("@org.jerkar:addin-spring-boot:+ -buildClass=JkSpringbootBuild scaffold eclipse#");
        } else {
            cmdBuilder.append("scaffold eclipse#");
        }
        if (this.embeddedMode.getSelection()) {
            cmdBuilder.append(" -scaffold.embedded");
        }
        JerkarExecutor.runCmdLine(project, cmdBuilder.toString(), null);
        super.okPressed();
    }

  
}