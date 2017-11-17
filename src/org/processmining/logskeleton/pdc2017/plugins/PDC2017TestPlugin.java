package org.processmining.logskeleton.pdc2017.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.LogPreprocessorAlgorithm;
import org.processmining.logskeleton.algorithms.LogSkeletonClassifierAlgorithm;
import org.processmining.logskeleton.pdc2017.dialogs.PDC2017TestDialog;
import org.processmining.logskeleton.pdc2017.models.PDC2017TestModel;
import org.processmining.logskeleton.pdc2017.parameters.PDC2017TestParameters;
import org.processmining.pdc2017.algorithms.PDC2017LogAlgorithm;
import org.processmining.pdc2017.algorithms.PDC2017Set;

@Plugin(name = "PDC 2017 Test", parameterLabels = {}, returnLabels = { "Results" }, returnTypes = { PDC2017TestModel.class })
public class PDC2017TestPlugin {

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

		try {
			for (int i : testParameters.getNrs()) {
					XLog trainingLog = (new PDC2017LogAlgorithm(PDC2017Set.TRAIN, i)).apply(context);
					XLog testLogMay = testParameters.getSets().contains(PDC2017Set.CAL1) ? (new PDC2017LogAlgorithm(
							PDC2017Set.CAL1, i)).apply(context) : null;
					XLog testLogJune = testParameters.getSets().contains(PDC2017Set.CAL2) ? (new PDC2017LogAlgorithm(
							PDC2017Set.CAL2, i)).apply(context) : null;
					XLog testLogFinal = testParameters.getSets().contains(PDC2017Set.TEST) ? (new PDC2017LogAlgorithm(
							PDC2017Set.TEST, i)).apply(context) : null;

					LogSkeletonClassifierAlgorithm classifierAlgorithm = new LogSkeletonClassifierAlgorithm();
					LogPreprocessorAlgorithm preprocessor = testParameters.getPreprocessor();

					XLog classifiedTestLogCal1 = null;
					XLog classifiedTestLogCal2 = null;
					XLog classifiedTestLogTest = null;

					// Classify the logs
					if (testParameters.getSets().contains(PDC2017Set.CAL1)) {
						System.out.println("====== Classify " + PDC2017Set.CAL1 + i + " ======");
						classifiedTestLogCal1 = classifierAlgorithm.apply(context, trainingLog, testLogMay,
								preprocessor);
						context.getProvidedObjectManager().createProvidedObject(
								"Test Log " + PDC2017Set.CAL1 + i, classifiedTestLogCal1, XLog.class,
								context);
					}
					if (testParameters.getSets().contains(PDC2017Set.CAL2)) {
						System.out.println("====== Classify " + PDC2017Set.CAL2 + i + " ======");
						classifiedTestLogCal2 = classifierAlgorithm.apply(context, trainingLog, testLogJune,
								preprocessor);
						context.getProvidedObjectManager().createProvidedObject(
								"Test Log " + PDC2017Set.CAL2 + i, classifiedTestLogCal2, XLog.class,
								context);
					}
					if (testParameters.getSets().contains(PDC2017Set.TEST)) {
						System.out.println("====== Classify " + PDC2017Set.TEST + i + " ======");
						classifiedTestLogTest = classifierAlgorithm.apply(context, trainingLog, testLogFinal,
								preprocessor);
						context.getProvidedObjectManager().createProvidedObject(
								"Test Log " + PDC2017Set.TEST + i, classifiedTestLogTest, XLog.class,
								context);
					}
					testModel.add(i, classifiedTestLogCal1, classifiedTestLogCal2, classifiedTestLogTest);
				}
			return testModel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
