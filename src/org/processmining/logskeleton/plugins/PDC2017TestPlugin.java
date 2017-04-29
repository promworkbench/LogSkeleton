package org.processmining.logskeleton.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.abstractplugins.ImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.PDC2017LogFilterAlgorithm;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.PDC2017Test;
import org.processmining.plugins.log.OpenLogFilePlugin;

@Plugin(name = "PDC 2017 Test", parameterLabels = { }, returnLabels = { "Results" }, returnTypes = { PDC2017Test.class })
public class PDC2017TestPlugin {

	private static ImportPlugin logImporter = new OpenLogFilePlugin();

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { })
	public static PDC2017Test run(final PluginContext context) {
		LogSkeletonBuilderPlugin createPlugin = new LogSkeletonBuilderPlugin();
		LogSkeletonCheckerPlugin checkPlugin = new LogSkeletonCheckerPlugin();
		PDC2017LogFilterAlgorithm filterAlgorithm = new PDC2017LogFilterAlgorithm();
		PDC2017Test testModel = new PDC2017Test();
//		String Path = "D:\\Dropbox\\Projects\\";
		String Path = "C:\\Users\\eric\\Dropbox\\Projects\\";
		try {
			for (int i = 1; i < 11; i++) {
				XLog marchLog = (XLog) logImporter.importFile(context,
						Path + "PDC 2017\\log" + i + ".xes");
				XLog aprilLog = (XLog) logImporter.importFile(context,
						Path + "PDC 2016\\April\\test_log_april_" + i + ".xes");
				XLog mayLog = (XLog) logImporter.importFile(context,
						Path + "PDC 2016\\May\\test_log_may_" + i + ".xes");
				XLog juneLog = (XLog) logImporter.importFile(context,
						Path + "PDC 2017\\log" + i + ".xes");
				
				XLog filteredMarchLog = marchLog;
				if (i == 1 || i == 2 || i == 5 || i == 9 || i == 10) {
					filteredMarchLog = filterAlgorithm.apply(marchLog);
				}
				LogSkeleton model = createPlugin.run(context, filteredMarchLog);
				context.getProvidedObjectManager().createProvidedObject("Model " + i, model, LogSkeleton.class, context);
				XLog classifiedAprilLog = checkPlugin.run(context, model, aprilLog);
				XLog classifiedMayLog = checkPlugin.run(context, model, mayLog);
				XLog classifiedJuneLog = checkPlugin.run(context, model, juneLog);
				testModel.add(i, classifiedAprilLog, classifiedMayLog, classifiedJuneLog, model);
			}
			return testModel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}

