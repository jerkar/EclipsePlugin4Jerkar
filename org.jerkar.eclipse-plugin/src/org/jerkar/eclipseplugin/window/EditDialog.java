package org.jerkar.eclipseplugin.window;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.jerkar.eclipseplugin.model.LastCommands;
import org.jerkar.eclipseplugin.model.MethodDescription;
import org.jerkar.eclipseplugin.model.MethodInfo;

public class EditDialog extends RunDialog {

    public EditDialog(Shell parentShell, MethodInfo methodInfo) {
        super(parentShell, methodInfo);
    }
    
    @Override
    public void create() {
        super.create();
        setTitle("Edit Jerkar Method Menu Shortcut");
        setMessage("Edit or delete a method shortcut in Jerkar menu.", IMessageProvider.INFORMATION);
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Save", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "Delete", true);
    }

    @Override
    protected void okPressed() {
        LastCommands.INSTANCE.remove(methodInfo.iProject, methodInfo.methodDescription.getName());
        LastCommands.INSTANCE.put(methodInfo.iProject,
                new MethodDescription(this.cmdtext.getText(), this.definitionText.getText()));
        setReturnCode(OK);
        close();
    }
    
    @Override
    protected void cancelPressed() {
        LastCommands.INSTANCE.remove(methodInfo.iProject, methodInfo.methodDescription.getName());
        super.cancelPressed();
    }
    
}
