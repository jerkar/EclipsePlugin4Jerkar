package org.jerkar.eclipseplugin.window;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jerkar.eclipseplugin.commands.JerkarExecutor;
import org.jerkar.eclipseplugin.model.LastCommands;
import org.jerkar.eclipseplugin.model.MethodDescription;
import org.jerkar.eclipseplugin.model.MethodInfo;

public class RunDialog extends TitleAreaDialog {

    protected Text cmdtext;

    protected Text definitionText;

    protected final MethodInfo methodInfo;

    public RunDialog(Shell parentShell, MethodInfo methodInfo) {
        super(parentShell);
        this.methodInfo = methodInfo;
    }

    @Override
    public void create() {
        super.create();
        setTitle("Run Jerkar Method");
        setMessage("Run a build method.", IMessageProvider.INFORMATION);
        //setTitleImage(Images.RUN);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(2, false);
        layout.makeColumnsEqualWidth = false;
        container.setLayout(layout);

        createCommandLine(container);
        new Label(container, SWT.NONE);
        Label linetop = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        linetop.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());
        createDefinition(container);
        return area;
    }

    private void createCommandLine(Composite parent) {
        Label label = new Label(parent, SWT.NULL);
        label.setText("Command line : ");
        cmdtext = new Text(parent, SWT.BORDER);
        String cmd = methodInfo.methodDescription.getName() + " ";
        cmdtext.setText(cmd);
        cmdtext.setLayoutData(GridDataFactory.defaultsFor(cmdtext).create());
        cmdtext.setSelection(cmd.length());
    }

    private void createDefinition(Composite parent) {
        Label label = new Label(parent, SWT.NULL);
        label.setText("What does this method ?");
        label.setLayoutData(GridDataFactory.fillDefaults().span(2, 1).create());
        definitionText = new Text(parent, SWT.BORDER | SWT.MULTI);
        definitionText.setText(this.methodInfo.methodDescription.getDefinition());
        definitionText.setLayoutData(GridDataFactory.fillDefaults()
                .span(2, 1).hint(SWT.DEFAULT, 80).minSize(SWT.DEFAULT, 1).grab(true, false).create());
        
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Run", true);
    }

    @Override
    protected void okPressed() {
        LastCommands.INSTANCE.put(methodInfo.iProject,
                new MethodDescription(this.cmdtext.getText(), this.definitionText.getText()));
        JerkarExecutor.runCmdLine(methodInfo.iProject, cmdtext.getText(), null);
        super.okPressed();
    }

}
