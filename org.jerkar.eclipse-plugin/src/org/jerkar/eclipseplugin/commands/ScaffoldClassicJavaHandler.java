package org.jerkar.eclipseplugin.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ScaffoldClassicJavaHandler extends AbstractHandler {

	@Override 
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return JerkarExecutor.run(event, "scaffold", "eclipse#");
	}

}
