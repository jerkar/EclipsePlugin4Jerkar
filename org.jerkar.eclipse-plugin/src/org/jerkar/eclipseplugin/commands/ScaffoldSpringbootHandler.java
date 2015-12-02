package org.jerkar.eclipseplugin.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ScaffoldSpringbootHandler extends AbstractHandler {

	@Override 
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return JerkarExecutor.run(event, "@org.jerkar:addin-spring-boot:1.2.7.+", "-buildClass=JkSpringbootBuild", "scaffold", "eclipse#");
	}

}
