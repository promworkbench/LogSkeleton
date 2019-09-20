package org.processmining.logskeleton.plugins;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.logskeleton.algorithms.VisualizerAlgorithm;
import org.processmining.logskeleton.configurations.VisualizerConfiguration;
import org.processmining.logskeleton.inputs.VisualizerInput;

/*
 * @deprecated Use VisualizerPlugin instead.
 */
@Deprecated
public class LogSkeletonVisualizerPlugin extends VisualizerAlgorithm {

	public JComponent run(UIPluginContext context, XLog log) {
		VisualizerInput input = new VisualizerInput(log);
		VisualizerConfiguration configuration = new VisualizerConfiguration(input);
		return apply(context, input, configuration).getComponent();
	}		
}
