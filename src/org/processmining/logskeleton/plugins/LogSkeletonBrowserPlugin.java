package org.processmining.logskeleton.plugins;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.logskeleton.algorithms.BrowserAlgorithm;
import org.processmining.logskeleton.configurations.BrowserConfiguration;
import org.processmining.logskeleton.inputs.BrowserInput;
import org.processmining.logskeleton.models.LogSkeleton;

/*
 * @deprecated Use BrowserPlugin instead.
 */
@Deprecated
public class LogSkeletonBrowserPlugin extends BrowserAlgorithm {

	public JComponent run(UIPluginContext context, final LogSkeleton logSkeleton) {
		BrowserInput input = new BrowserInput(logSkeleton);
		BrowserConfiguration configuration = new BrowserConfiguration(input);
		return apply(context, input, configuration).getComponent();
	}

}