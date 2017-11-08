package org.processmining.logskeleton.pdc2016.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.abstractplugins.ImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.pdc2017.models.PDC2017TestModel;
import org.processmining.logskeleton.pdc2017.parameters.PDC2017TestParameters;
import org.processmining.logskeleton.plugins.LogSkeletonBuilderPlugin;
import org.processmining.logskeleton.plugins.LogSkeletonCheckerPlugin;
import org.processmining.plugins.log.OpenLogFilePlugin;

@Plugin(name = "PDC 2017 Test 2016", parameterLabels = { }, returnLabels = { "Results" }, returnTypes = { PDC2017TestModel.class })
public class PDC2017Test2016Plugin {


	private static ImportPlugin logImporter = new OpenLogFilePlugin();

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { })
	public static PDC2017TestModel run(final PluginContext context) {
		LogSkeletonBuilderPlugin createPlugin = new LogSkeletonBuilderPlugin();
		LogSkeletonCheckerPlugin checkPlugin = new LogSkeletonCheckerPlugin();
		
		PDC2017TestModel testModel = new PDC2017TestModel(new PDC2017TestParameters());
		String Path = "D:\\Dropbox\\Projects\\";
//		String Path = "C:\\Users\\eric\\Dropbox\\Projects\\";
		
		try {
			for (int i = 1; i < 11; i++) {
				XLog marchLog = (XLog) logImporter.importFile(context,
						Path + "PDC 2016\\training_log_" + i + ".xes");
				XLog aprilLog = (XLog) logImporter.importFile(context,
						Path + "PDC 2016\\April\\test_log_april_" + i + ".xes");
				XLog mayLog = (XLog) logImporter.importFile(context,
						Path + "PDC 2016\\May\\test_log_may_" + i + ".xes");
				XLog juneLog = (XLog) logImporter.importFile(context,
						Path + "PDC 2016\\June\\test_log_june_" + i + ".xes");
				
//				PDC2017FilterPlugin filter = new PDC2017FilterPlugin();
//				marchLog = filter.apply(marchLog);
				LogSkeleton model = createPlugin.run(context, marchLog);
				XLog classifiedAprilLog = checkPlugin.run(context, model, aprilLog);
				XLog classifiedMayLog = checkPlugin.run(context, model, mayLog);
				XLog classifiedJuneLog = checkPlugin.run(context, model, juneLog);
				
//				XLog filteredMarchLog = filterAlgorithm.applyNeverTogether(marchLog, model);
//				PDC2017ConstraintModel filteredModel = createPlugin.run(context, filteredMarchLog);
//				XLog filteredAprilLog = checkPlugin.run(context, filteredModel, filterAlgorithm.applyNeverTogether(classifiedAprilLog, model));
//				XLog filteredMayLog = checkPlugin.run(context, filteredModel, filterAlgorithm.applyNeverTogether(classifiedMayLog, model));
//				XLog filteredJuneLog = checkPlugin.run(context, filteredModel, filterAlgorithm.applyNeverTogether(classifiedJuneLog, model));
				
				testModel.add(i, classifiedAprilLog, classifiedMayLog, classifiedJuneLog);
//				testModel.add(i, filteredAprilLog, filteredMayLog, filteredJuneLog, model);
			}
			return testModel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
