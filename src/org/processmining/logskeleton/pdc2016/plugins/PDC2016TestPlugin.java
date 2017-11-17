package org.processmining.logskeleton.pdc2016.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.LogPreprocessorAlgorithm;
import org.processmining.logskeleton.algorithms.LogSkeletonClassifierAlgorithm;
import org.processmining.logskeleton.pdc2016.dialogs.PDC2016TestDialog;
import org.processmining.logskeleton.pdc2016.models.PDC2016TestModel;
import org.processmining.logskeleton.pdc2016.parameters.PDC2016TestParameters;
import org.processmining.pdc2016.algorithms.PDC2016LogAlgorithm;
import org.processmining.pdc2016.algorithms.PDC2016Set;

@Plugin(name = "PDC 2016 Test", parameterLabels = {}, returnLabels = { "Results" }, returnTypes = { PDC2016TestModel.class })
public class PDC2016TestPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = {})
	public static PDC2016TestModel run(final UIPluginContext context) {
		PDC2016TestParameters testParameters = new PDC2016TestParameters();
		PDC2016TestDialog testDialog = new PDC2016TestDialog(testParameters);
		InteractionResult result = context.showWizard("Select test parameters", true, true, testDialog);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		PDC2016TestModel testModel = new PDC2016TestModel(testParameters);

		try {
			for (int i : testParameters.getNrs()) {
				XLog trainingLog = (new PDC2016LogAlgorithm(PDC2016Set.TRAIN, i)).apply(context);
				XLog testLogMay = testParameters.getSets().contains(PDC2016Set.CAL1) ? (new PDC2016LogAlgorithm(
						PDC2016Set.CAL1, i)).apply(context) : null;
				XLog testLogJune = testParameters.getSets().contains(PDC2016Set.CAL2) ? (new PDC2016LogAlgorithm(
						PDC2016Set.CAL2, i)).apply(context) : null;
				XLog testLogFinal = testParameters.getSets().contains(PDC2016Set.TEST) ? (new PDC2016LogAlgorithm(
						PDC2016Set.TEST, i)).apply(context) : null;

				LogSkeletonClassifierAlgorithm classifierAlgorithm = new LogSkeletonClassifierAlgorithm();

				XLog classifiedTestLogCal1 = null;
				XLog classifiedTestLogCal2 = null;
				XLog classifiedTestLogTest = null;

				// Classify the logs
				if (testParameters.getSets().contains(PDC2016Set.CAL1)) {
					System.out.println("====== Classify " + PDC2016Set.CAL1 + i + " ======");
					classifiedTestLogCal1 = classifierAlgorithm.apply(context, trainingLog, testLogMay,
							new LogPreprocessorAlgorithm());
					context.getProvidedObjectManager().createProvidedObject("Test Log " + PDC2016Set.CAL1 + i,
							classifiedTestLogCal1, XLog.class, context);
				}
				if (testParameters.getSets().contains(PDC2016Set.CAL2)) {
					System.out.println("====== Classify " + PDC2016Set.CAL2 + i + " ======");
					classifiedTestLogCal2 = classifierAlgorithm.apply(context, trainingLog, testLogJune,
							new LogPreprocessorAlgorithm());
					context.getProvidedObjectManager().createProvidedObject("Test Log " + PDC2016Set.CAL2 + i,
							classifiedTestLogCal2, XLog.class, context);
				}
				if (testParameters.getSets().contains(PDC2016Set.TEST)) {
					System.out.println("====== Classify " + PDC2016Set.TEST + i + " ======");
					classifiedTestLogTest = classifierAlgorithm.apply(context, trainingLog, testLogFinal,
							new LogPreprocessorAlgorithm());
					context.getProvidedObjectManager().createProvidedObject("Test Log " + PDC2016Set.TEST + i,
							classifiedTestLogTest, XLog.class, context);
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
