package org.processmining.logskeleton.plugins;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.abstractplugins.ImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.PDC2017LogFilterAlgorithm;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonCount;
import org.processmining.logskeleton.models.PDC2017Test;
import org.processmining.plugins.log.OpenLogFilePlugin;

@Plugin(name = "PDC 2017 Test", parameterLabels = {}, returnLabels = { "Results" }, returnTypes = { PDC2017Test.class })
public class PDC2017TestPlugin {

	private static ImportPlugin logImporter = new OpenLogFilePlugin();

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = {})
	public static PDC2017Test run(final PluginContext context) {
		LogSkeletonBuilderPlugin createPlugin = new LogSkeletonBuilderPlugin();
		LogSkeletonCheckerPlugin checkPlugin = new LogSkeletonCheckerPlugin();
		PDC2017LogFilterAlgorithm filterAlgorithm = new PDC2017LogFilterAlgorithm();
		PDC2017Test testModel = new PDC2017Test();
		String Path = "D:\\Dropbox\\Projects\\";
		//		String Path = "C:\\Users\\eric\\Dropbox\\Projects\\";
		try {
			for (int i = 1; i < 11; i++) {
				XLog marchLog = (XLog) logImporter.importFile(context, Path + "PDC 2017\\log" + i + ".xes");
				XLog aprilLog = (XLog) logImporter.importFile(context, Path + "PDC 2017\\test_log_may\\test_log_may_"
						+ i + ".xes");
				XLog mayLog = (XLog) logImporter
						.importFile(context, Path + "PDC 2016\\May\\test_log_may_" + i + ".xes");
				XLog juneLog = (XLog) logImporter.importFile(context, Path + "PDC 2017\\log" + i + ".xes");

				XLog filteredMarchLog = marchLog;
				XLog filteredAprilLog = aprilLog;
				XLog filteredMayLog = mayLog;
				XLog filteredJuneLog = juneLog;
				//				if (i == 1 || i == 2 || i == 5 || i == 9 || i == 10) {
				//					filteredMarchLog = filterAlgorithm.apply(marchLog);
				//				}
				if (i == 1) {
					filteredMarchLog = (new PDC2017Log1FilterPlugin()).run(context, marchLog);
				} else if (i == 2) {
					filteredMarchLog = (new PDC2017Log2FilterPlugin()).run(context, marchLog);
				} else if (i == 5) {
					filteredMarchLog = (new PDC2017Log5FilterPlugin()).run(context, marchLog);
				} else if (i == 9) {
					filteredMarchLog = (new PDC2017Log9FilterPlugin()).run(context, marchLog);
				} else if (i == 10) {
					filteredMarchLog = (new PDC2017Log10FilterPlugin()).run(context, marchLog);
				}
				if (i == 4) {
					PDC2017Log4SplitterPlugin splitter = new PDC2017Log4SplitterPlugin();
					filteredMarchLog = splitter.run(context, filteredMarchLog);
					filteredAprilLog = splitter.run(context, aprilLog);
					filteredMayLog = splitter.run(context, mayLog);
					filteredJuneLog = splitter.run(context, juneLog);
				} else if (i == 7) {
					PDC2017Log7SplitterPlugin splitter = new PDC2017Log7SplitterPlugin();
					filteredMarchLog = splitter.run(context, filteredMarchLog);
					filteredAprilLog = splitter.run(context, aprilLog);
					filteredMayLog = splitter.run(context, mayLog);
					filteredJuneLog = splitter.run(context, juneLog);
				} else if (i == 9) {
					PDC2017Log9SplitterPlugin splitter = new PDC2017Log9SplitterPlugin();
					filteredMarchLog = splitter.run(context, filteredMarchLog);
					filteredAprilLog = splitter.run(context, aprilLog);
					filteredMayLog = splitter.run(context, mayLog);
					filteredJuneLog = splitter.run(context, juneLog);
				} else if (i == 10) {
					PDC2017Log10SplitterPlugin splitter = new PDC2017Log10SplitterPlugin();
					filteredMarchLog = splitter.run(context, filteredMarchLog);
					filteredAprilLog = splitter.run(context, aprilLog);
					filteredMayLog = splitter.run(context, mayLog);
					filteredJuneLog = splitter.run(context, juneLog);
				}

				LogSkeleton model = createPlugin.run(context, filteredMarchLog);
				context.getProvidedObjectManager()
						.createProvidedObject("Model " + i, model, LogSkeleton.class, context);

				// Classify the logs
				System.out.println("====== April " + i + " ======");
				XLog classifiedAprilLog = classify(context, model, filteredMarchLog, filteredAprilLog);
				System.out.println("====== May " + i + " ======");
				XLog classifiedMayLog = classify(context, model, filteredMarchLog, filteredMayLog);
				System.out.println("====== June " + i + " ======");
				XLog classifiedJuneLog = classify(context, model, filteredMarchLog, filteredJuneLog);

				testModel.add(i, classifiedAprilLog, classifiedMayLog, classifiedJuneLog, model);
			}
			return testModel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static XLog classify(PluginContext context, LogSkeleton trainingModel, XLog trainingLog, XLog testLog) {
		LogSkeletonBuilderPlugin createPlugin = new LogSkeletonBuilderPlugin();
		LogSkeletonCheckerPlugin checkPlugin = new LogSkeletonCheckerPlugin();
		XLog classifiedTestLog = checkPlugin.run(context, trainingModel, testLog);
		Set<String> positiveTestTraces = new HashSet<String>();
		for (XTrace trace : classifiedTestLog) {
			positiveTestTraces.add(XConceptExtension.instance().extractName(trace));
		}
		for (String activity : trainingModel.getActivities()) {
			if (activity == LogSkeletonCount.STARTEVENT || activity == LogSkeletonCount.ENDEVENT) {
				continue;
			}
//			for (String activity2 : trainingModel.getActivities()) {
//				if (activity2 == LogSkeletonCount.STARTEVENT || activity2 == LogSkeletonCount.ENDEVENT) {
//					continue;
//				}
				Set<String> filters = new HashSet<String>();
				filters.add(activity);
//				filters.add(activity2);
				System.out.println("[PDC2017TestPlugin] Filters = " + filters);
				XLog filteredTrainingLog = filter(trainingLog, filters);
				XLog filteredTestLog = filter(testLog, filters);
				if (filteredTestLog.isEmpty() || filteredTrainingLog.isEmpty()) {
					continue;
				}
				LogSkeleton filteredTrainingModel = createPlugin.run(context, filteredTrainingLog);
				XLog classifiedFilteredTestLog = checkPlugin.run(context, filteredTrainingModel, filteredTestLog);
				Set<String> negativeTestTraces = new HashSet<String>();
				for (XTrace subTrace : filteredTestLog) {
					if (!classifiedFilteredTestLog.contains(subTrace)) {
						negativeTestTraces.add(XConceptExtension.instance().extractName(subTrace));
					}
				}
				positiveTestTraces.removeAll(negativeTestTraces);
//			}
		}
		XLog newClassifiedAprilLog = XFactoryRegistry.instance().currentDefault().createLog();
		for (XTrace trace : classifiedTestLog) {
			if (positiveTestTraces.contains(XConceptExtension.instance().extractName(trace))) {
				newClassifiedAprilLog.add(trace);
			}
		}
		return newClassifiedAprilLog;
	}

	private static XLog filter(XLog log, Set<String> filters) {
		XLog filteredLog = XFactoryRegistry.instance().currentDefault().createLog();
		for (XTrace trace : log) {
			boolean ok = false;
			Set<String> toMatch = new HashSet<String>(filters);
			for (XEvent event : trace) {
				toMatch.remove(XConceptExtension.instance().extractName(event));
			}
			if (toMatch.isEmpty()) {
				filteredLog.add(trace);
			}
		}
		return filteredLog;
	}

}
