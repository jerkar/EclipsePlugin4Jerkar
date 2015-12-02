package org.jerkar.eclipseplugin.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class ClasspathVariableSetter {
	
	private static final String JERKAR_HOME = "JERKAR_HOME";
	
	private static final String JERKAR_REPO = "JERKAR_REPO";
	
	public static void run() throws JavaModelException {
		List<String> varNames = Arrays.asList(JavaCore.getClasspathVariableNames());
		if (!varNames.contains(JERKAR_HOME)) {
			File home = JerkarHelper.jerkarHome();
			if (home != null) {
				Path path = new Path(JerkarHelper.jerkarHome().getAbsolutePath());
				JavaCore.setClasspathVariable(JERKAR_HOME, path, null);
			}
		}
		if (!varNames.contains(JERKAR_REPO)) {
			Path repoPath = new Path(System.getProperty("user.home") + "/.jerkar/cache/repo");
			JavaCore.setClasspathVariable(JERKAR_REPO, repoPath, null);
		}
	}

}
