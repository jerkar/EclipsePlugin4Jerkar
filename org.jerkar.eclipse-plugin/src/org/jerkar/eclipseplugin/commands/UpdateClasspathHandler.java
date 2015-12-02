package org.jerkar.eclipseplugin.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class UpdateClasspathHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return JerkarExecutor.run(event, "eclipse#generateFiles");
	}

}
