package org.processmining.logskeleton.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.abstractplugins.ImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.LogSkeletonClassifierAlgorithm;
import org.processmining.logskeleton.models.PDC2017Test;
import org.processmining.plugins.log.OpenLogFilePlugin;

@Plugin(name = "PDC 2017 Test", parameterLabels = {}, returnLabels = { "Results" }, returnTypes = { PDC2017Test.class })
public class PDC2017TestPlugin {

	private static ImportPlugin logImporter = new OpenLogFilePlugin();

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = {})
	public static PDC2017Test run(final PluginContext context) {
		PDC2017Test testModel = new PDC2017Test();
		String Path = "D:\\Dropbox\\Projects\\";
		//		String Path = "C:\\Users\\hverbeek\\Dropbox\\Projects\\";
		//		String Path = "C:\\Users\\eric\\Dropbox\\Projects\\";
		try {
			for (int i = 1; i < 11; i++) {
				XLog trainingLog = (XLog) logImporter.importFile(context, Path + "PDC 2017\\log" + i + ".xes");
				XLog testLogMay = (XLog) logImporter.importFile(context, Path + "PDC 2017\\test_log_may\\test_log_may_"
						+ i + ".xes");
				XLog testLogJune = (XLog) logImporter.importFile(context, Path + "PDC 2017\\test_log_june\\test_log_june_"
						+ i + ".xes");
				XLog testLogFinal = (XLog) logImporter.importFile(context, Path + "PDC 2017\\test_log_contest_2017\\test_log_july_"
						+ i + ".xes");

				LogSkeletonClassifierAlgorithm classifierAlgorithm = new LogSkeletonClassifierAlgorithm();

				// Classify the logs
				System.out.println("====== Classify May " + i + " ======");
				XLog classifiedTestLogMay = classifierAlgorithm.apply(context, trainingLog, testLogMay);
				context.getProvidedObjectManager().createProvidedObject("Test Log May " + i, classifiedTestLogMay,
						XLog.class, context);
				System.out.println("====== Classify June " + i + " ======");
				XLog classifiedTestLogJune = classifierAlgorithm.apply(context, trainingLog, testLogJune);
				context.getProvidedObjectManager().createProvidedObject("Test Log June " + i, classifiedTestLogJune, XLog.class,
						context);
				System.out.println("====== Classify Final " + i + " ======");
				XLog classifiedTestLogFinal = classifierAlgorithm.apply(context, trainingLog, testLogFinal);
				context.getProvidedObjectManager().createProvidedObject("Test Log Final " + i, classifiedTestLogFinal, XLog.class,
						context);

				testModel.add(i, classifiedTestLogMay, classifiedTestLogJune, classifiedTestLogFinal);
			}
			return testModel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
