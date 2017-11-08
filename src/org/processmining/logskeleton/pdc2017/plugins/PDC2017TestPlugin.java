package org.processmining.logskeleton.pdc2017.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.abstractplugins.ImportPlugin;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.LogPreprocessorAlgorithm;
import org.processmining.logskeleton.algorithms.LogSkeletonClassifierAlgorithm;
import org.processmining.logskeleton.pdc2017.dialogs.PDC2017TestDialog;
import org.processmining.logskeleton.pdc2017.models.PDC2017TestModel;
import org.processmining.logskeleton.pdc2017.parameters.PDC2017TestParameters;
import org.processmining.plugins.log.OpenLogFilePlugin;

@Plugin(name = "PDC 2017 Test", parameterLabels = {}, returnLabels = { "Results" }, returnTypes = { PDC2017TestModel.class })
public class PDC2017TestPlugin {

	private static ImportPlugin logImporter = new OpenLogFilePlugin();

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = {})
	public static PDC2017TestModel run(final UIPluginContext context) {
		PDC2017TestParameters testParameters = new PDC2017TestParameters();
		PDC2017TestDialog testDialog = new PDC2017TestDialog(testParameters);
		InteractionResult result = context.showWizard("Select test parameters", true, true, testDialog);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		PDC2017TestModel testModel = new PDC2017TestModel(testParameters);

		String Path = "D:\\Dropbox\\Projects\\";
		//		String Path = "C:\\Users\\hverbeek\\Dropbox\\Projects\\";
		//		String Path = "C:\\Users\\eric\\Dropbox\\Projects\\";
		try {
			for (int i = 1; i < 11; i++) {
				if (testParameters.getLogNames().contains("log" + i)) {
					XLog trainingLog = (XLog) logImporter.importFile(context, Path + "PDC 2017\\log" + i + ".xes");
					XLog testLogMay = testParameters.getCollectionNames().contains(PDC2017TestParameters.CAL1) ? (XLog) logImporter
							.importFile(context, Path + "PDC 2017\\test_log_may\\test_log_may_" + i + ".xes") : null;
					XLog testLogJune = testParameters.getCollectionNames().contains(PDC2017TestParameters.CAL2) ? (XLog) logImporter
							.importFile(context, Path + "PDC 2017\\test_log_june\\test_log_june_" + i + ".xes") : null;
					XLog testLogFinal = testParameters.getCollectionNames().contains(PDC2017TestParameters.TEST) ? (XLog) logImporter
							.importFile(context, Path + "PDC 2017\\test_log_contest_2017\\test_log_july_" + i + ".xes")
							: null;

					LogSkeletonClassifierAlgorithm classifierAlgorithm = new LogSkeletonClassifierAlgorithm();
					LogPreprocessorAlgorithm preprocessor = testParameters.getPreprocessor();

					XLog classifiedTestLogCal1 = null;
					XLog classifiedTestLogCal2 = null;
					XLog classifiedTestLogTest = null;
					
					// Classify the logs
					if (testParameters.getCollectionNames().contains(PDC2017TestParameters.CAL1)) {
						System.out.println("====== Classify " + PDC2017TestParameters.CAL1 + " " + i + " ======");
						classifiedTestLogCal1 = classifierAlgorithm.apply(context, trainingLog, testLogMay,
								preprocessor);
						context.getProvidedObjectManager().createProvidedObject("Test Log " + PDC2017TestParameters.CAL1 + " " + i,
								classifiedTestLogCal1, XLog.class, context);
					}
					if (testParameters.getCollectionNames().contains(PDC2017TestParameters.CAL2)) {
						System.out.println("====== Classify " + PDC2017TestParameters.CAL2 + " " + i + " ======");
						classifiedTestLogCal2 = classifierAlgorithm.apply(context, trainingLog, testLogJune,
								preprocessor);
						context.getProvidedObjectManager().createProvidedObject("Test Log " + PDC2017TestParameters.CAL2 + " " + i,
								classifiedTestLogCal2, XLog.class, context);
					}
					if (testParameters.getCollectionNames().contains(PDC2017TestParameters.TEST)) {
						System.out.println("====== Classify " + PDC2017TestParameters.TEST + " " + i + " ======");
						classifiedTestLogTest = classifierAlgorithm.apply(context, trainingLog, testLogFinal,
								preprocessor);
						context.getProvidedObjectManager().createProvidedObject("Test Log " + PDC2017TestParameters.TEST + " " + i,
								classifiedTestLogTest, XLog.class, context);
					}
					testModel.add(i, classifiedTestLogCal1, classifiedTestLogCal2, classifiedTestLogTest);
				}
			}
			return testModel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
