package org.jerkar.eclipseplugin.menu;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MethodShell {

    private final Display display;
    
    private Button button;
    
    private Text text;

    public MethodShell(Display display) {
        super();
        this.display = display;
    }

    public void open(CommandInfo commandInfo, final RunHandler runHandler) {
        final Shell shell = new Shell(display, SWT.TOOL | SWT.CLOSE | SWT.RESIZE);
        shell.setText("Run a Jerkar method");
        GridLayout layout = new GridLayout();
        layout.makeColumnsEqualWidth = false;
        shell.setLayout(layout);
        
        Composite runForm = form(shell, commandInfo.methodDescription, runHandler);
        runForm.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        
        
        
        final ExtraInfoComponent component = new ExtraInfoComponent(shell, commandInfo);
        component.composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        
        button.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                runHandler.process(text.getText(), component.getDefinition());
                text.getShell().dispose();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }
        });
        
        Point cursorLocation = Display.getCurrent().getCursorLocation();
        shell.setLocation(cursorLocation.x, cursorLocation.y);
        
        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

    }

    private Composite form(Composite parent, MethodDescription methodDescription, final RunHandler runHandler) {
        Composite result = new Composite(parent, SWT.NULL);
        GridLayout gridLayout = new GridLayout(3, false);
        result.setLayout(gridLayout);
        Label label = new Label(result, SWT.NULL);
        label.setText("Command line:");
        text = new Text(result, SWT.BORDER);
        text.setText(methodDescription.getName() + " ");
        text.setLayoutData(GridDataFactory.defaultsFor(text).create());
        text.setSelection(methodDescription.getName().length() + 1);
        button = new Button(result, SWT.PUSH);
        button.setText("Run");
        button.setLayoutData(GridDataFactory.defaultsFor(button).create());
        
        return result;
    }

}
