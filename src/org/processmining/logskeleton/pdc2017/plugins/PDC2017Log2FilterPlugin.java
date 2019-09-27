package org.processmining.logskeleton.pdc2017.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.BuilderAlgorithm;
import org.processmining.logskeleton.classifiers.PrefixClassifier;
import org.processmining.logskeleton.configurations.BuilderConfiguration;
import org.processmining.logskeleton.inputs.BuilderInput;
import org.processmining.logskeleton.models.LogSkeletonCount;

@Plugin(name = "PDC 2017 Log 2 Filter", parameterLabels = { "Event Log 2" }, returnLabels = { "Filtered Log 2" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log2FilterPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		BuilderAlgorithm skeletonBuilder = new BuilderAlgorithm();
		XLog filteredLog = XFactoryRegistry.instance().currentDefault().createLog((XAttributeMap) log.getAttributes().clone());
		XConceptExtension.instance().assignName(filteredLog,
				XConceptExtension.instance().extractName(log) + " | filter: k=1, d=1, b=i+e");
		XLog traceLog = XFactoryRegistry.instance().currentDefault().createLog((XAttributeMap) log.getAttributes().clone());
		XEventClassifier classifier = new PrefixClassifier(new XEventNameClassifier());
		BuilderConfiguration builderConfiguration = new BuilderConfiguration(new BuilderInput(log));
		builderConfiguration.setClassifier(classifier);

		for (XTrace trace : log) {
			traceLog.clear();
			traceLog.add(trace);
			LogSkeletonCount count = skeletonBuilder.count(traceLog, builderConfiguration);
			if (count.get("k") != 1 || count.get("d") != 1) {
				continue;
			}
			if (count.get("b") != count.get("i") + count.get("e")) {
				continue;
			}
			filteredLog.add(trace);
		}
		return filteredLog;
	}
}
