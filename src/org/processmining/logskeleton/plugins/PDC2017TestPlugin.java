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
		PDC2017Test testModel = new PDC2017Test();
		String Path = "D:\\Dropbox\\Projects\\";
		//		String Path = "C:\\Users\\hverbeek\\Dropbox\\Projects\\";
		//		String Path = "C:\\Users\\eric\\Dropbox\\Projects\\";
		try {
			for (int i = 1; i < 2; i++) {
				XLog marchLog = (XLog) logImporter.importFile(context, Path + "PDC 2017\\log" + i + ".xes");
				XLog aprilLog = (XLog) logImporter.importFile(context,
						Path + "PDC 2017\\test_log_may\\test_log_may_" + i + ".xes");
				XLog mayLog = (XLog) logImporter.importFile(context, Path + "PDC 2017\\test_log_june\\test_log_june_" + i + ".xes");
				XLog juneLog = (XLog) logImporter.importFile(context, Path + "PDC 2017\\test_log_june\\test_log_june_" + i + ".xes");

				XLog filteredMarchLog = marchLog;
				XLog filteredAprilLog = aprilLog;
				XLog filteredMayLog = mayLog;
				XLog filteredJuneLog = juneLog;
				//				if (i == 1 || i == 2 || i == 5 || i == 9 || i == 10) {
				//					filteredMarchLog = filterAlgorithm.apply(marchLog);
				//				}
				System.out.println("====== Filter " + i + " ======");
				if (i == 1) {
					filteredMarchLog = (new PDC2017Log1FilterPlugin()).run(context, marchLog);
					//					filteredMarchLog = filterAlgorithm.apply(marchLog);
				} else if (i == 2) {
					filteredMarchLog = (new PDC2017Log2FilterPlugin()).run(context, marchLog);
					//					filteredMarchLog = filterAlgorithm.apply(marchLog);
				} else if (i == 5) {
					filteredMarchLog = (new PDC2017Log5FilterPlugin()).run(context, marchLog);
					//					filteredMarchLog = filterAlgorithm.apply(marchLog);
				} else if (i == 9) {
					filteredMarchLog = (new PDC2017Log9FilterPlugin()).run(context, marchLog);
					//					filteredMarchLog = filterAlgorithm.apply(marchLog);
				} else if (i == 10) {
					filteredMarchLog = (new PDC2017Log10FilterPlugin()).run(context, marchLog);
					//					filteredMarchLog = filterAlgorithm.apply(marchLog);
				}
				// Hack: add test traces which we consider to be good.
				if (i == 1) {
//					for (XTrace trace : filteredAprilLog) {
//						String caseId = XConceptExtension.instance().extractName(trace);
//						Set<String> positiveTraces = new HashSet<String>(Arrays.asList("2", "3"));
//						if (caseId.equals("9") || caseId.equals("18")) {
//							filteredMarchLog.add(trace);
//						}
//					}
					for (XTrace trace : filteredMayLog) {
						String caseId = XConceptExtension.instance().extractName(trace);
						if (caseId.equals("9") || caseId.equals("18")) {
							filteredMarchLog.add(trace);
						}
					}
				} else if (i == 6) {
					for (XTrace trace : filteredMayLog) {
						String caseId = XConceptExtension.instance().extractName(trace);
						if (caseId.equals("14") || caseId.equals("18")) {
							filteredMarchLog.add(trace);
						}
					}
				}
				System.out.println("====== Split " + i + " ======");
				if (i == 2) {
					PDC2017Log2SplitterPlugin splitter = new PDC2017Log2SplitterPlugin();
					filteredMarchLog = splitter.run(context, filteredMarchLog);
					filteredAprilLog = splitter.run(context, aprilLog);
					filteredMayLog = splitter.run(context, mayLog);
					filteredJuneLog = splitter.run(context, juneLog);
				} else if (i == 4) {
					PDC2017Log4SplitterPlugin splitter = new PDC2017Log4SplitterPlugin();
					filteredMarchLog = splitter.run(context, filteredMarchLog);
					filteredAprilLog = splitter.run(context, aprilLog);
					filteredMayLog = splitter.run(context, mayLog);
					filteredJuneLog = splitter.run(context, juneLog);
				} else if (i == 5) {
					PDC2017Log5SplitterPlugin splitter = new PDC2017Log5SplitterPlugin();
					filteredMarchLog = splitter.run(context, filteredMarchLog);
					filteredAprilLog = splitter.run(context, aprilLog);
					filteredMayLog = splitter.run(context, mayLog);
					filteredJuneLog = splitter.run(context, juneLog);
				} else if (i == 6) {
					PDC2017Log6SplitterPlugin splitter = new PDC2017Log6SplitterPlugin();
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
				context.getProvidedObjectManager().createProvidedObject("Model " + i, model, LogSkeleton.class,
						context);

				// Classify the logs
				System.out.println("====== Classify April " + i + " ======");
				XLog classifiedAprilLog = classify(context, model, filteredMarchLog, filteredAprilLog);
				context.getProvidedObjectManager().createProvidedObject("Log April " + i, classifiedAprilLog, XLog.class,
						context);
				System.out.println("====== Classify May " + i + " ======");
				XLog classifiedMayLog = classify(context, model, filteredMarchLog, filteredMayLog);
				context.getProvidedObjectManager().createProvidedObject("Log May " + i, classifiedMayLog, XLog.class,
						context);
				System.out.println("====== Classify June " + i + " ======");
				XLog classifiedJuneLog = classify(context, model, filteredMarchLog, filteredJuneLog);
				context.getProvidedObjectManager().createProvidedObject("Log June " + i, classifiedJuneLog, XLog.class,
						context);

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
		Set<String> messages = new HashSet<String>();
		boolean[] checks = new boolean[] { true, true, false };
		XLog classifiedTestLog = checkPlugin.run(context, trainingModel, testLog, messages, checks);
		Set<String> positiveTestTraces = new HashSet<String>();
		int threshold = 10;
		for (XTrace trace : classifiedTestLog) {
			positiveTestTraces.add(XConceptExtension.instance().extractName(trace));
		}
		for (String message : messages) {
			System.out.println("[PDC2017TestPlugin]" + message);
		}
		for (int i = 0; i < 3; i++) {
			checks[0] = (i == 0);
			checks[1] = (i == 1);
			checks[2] = (i == 2);
			for (String activity : trainingModel.getActivities()) {
				if (positiveTestTraces.size() <= threshold) {
					continue;
				}
				if (activity == LogSkeletonCount.STARTEVENT || activity == LogSkeletonCount.ENDEVENT) {
					continue;
				}

				for (int f = 0; f < 2; f++) {
					Set<String> positiveFilters = new HashSet<String>();
					Set<String> negativeFilters = new HashSet<String>();
					if (f == 0) {
						positiveFilters.add(activity);
					} else {
						negativeFilters.add(activity);
					}
					XLog filteredTrainingLog = filter(trainingLog, positiveFilters, negativeFilters);
					XLog filteredTestLog = filter(testLog, positiveFilters, negativeFilters);
					if (filteredTestLog.isEmpty() || filteredTrainingLog.isEmpty() || filteredTrainingLog.size() < 16) {
						continue;
					}
//					System.out.println("[PDC2017TestPlugin] Remaining traces 1: " + filteredTrainingLog.size());
					LogSkeleton filteredTrainingModel = createPlugin.run(context, filteredTrainingLog);
					messages = new HashSet<String>();
					XLog classifiedFilteredTestLog = checkPlugin.run(context, filteredTrainingModel, filteredTestLog,
							messages, checks);
					for (XTrace subTrace : filteredTestLog) {
						//						if (positiveTestTraces.size() <= threshold) {
						//							continue;
						//						}
						if (!classifiedFilteredTestLog.contains(subTrace)) {
							String caseId = XConceptExtension.instance().extractName(subTrace);
							if (positiveTestTraces.remove(caseId)) {
								System.out.println(
										"[PDC2017TestPlugin] Case " + XConceptExtension.instance().extractName(subTrace)
												+ " excluded by positive filter " + positiveFilters
												+ " and negative filter " + negativeFilters
												+ ", support = " + filteredTrainingLog.size());
								for (String message : messages) {
									System.out.println("[PDC2017TestPlugin]" + message);
								}
							}
						}
					}
				}
			}
			for (String activity : trainingModel.getActivities()) {
				if (positiveTestTraces.size() <= threshold) {
					continue;
				}
				if (activity == LogSkeletonCount.STARTEVENT || activity == LogSkeletonCount.ENDEVENT) {
					continue;
				}
				for (String activity2 : trainingModel.getActivities()) {
					if (activity2 == LogSkeletonCount.STARTEVENT || activity2 == LogSkeletonCount.ENDEVENT) {
						continue;
					}
					if (activity.compareTo(activity2) >= 0) {
						continue;
					}
					for (int f = 0; f < 4; f++) {
						Set<String> positiveFilters = new HashSet<String>();
						Set<String> negativeFilters = new HashSet<String>();
						if (f == 0 || f == 1) {
							positiveFilters.add(activity);
						} else {
							negativeFilters.add(activity);
						}
						if (f == 0 || f == 2) {
							positiveFilters.add(activity2);
						} else {
							negativeFilters.add(activity2);
						}
						XLog filteredTrainingLog = filter(trainingLog, positiveFilters, negativeFilters);
						XLog filteredTestLog = filter(testLog, positiveFilters, negativeFilters);
						if (filteredTestLog.isEmpty() || filteredTrainingLog.isEmpty() || filteredTrainingLog.size() < 16) {
							continue;
						}
//						System.out.println("[PDC2017TestPlugin] Remaining traces 2: " + filteredTrainingLog.size());
						LogSkeleton filteredTrainingModel = createPlugin.run(context, filteredTrainingLog);
						messages = new HashSet<String>();
						XLog classifiedFilteredTestLog = checkPlugin.run(context, filteredTrainingModel,
								filteredTestLog, messages, checks);
						for (XTrace subTrace : filteredTestLog) {
							//							if (positiveTestTraces.size() <= threshold) {
							//								continue;
							//							}
							if (!classifiedFilteredTestLog.contains(subTrace)) {
								String caseId = XConceptExtension.instance().extractName(subTrace);
								if (positiveTestTraces.remove(caseId)) {
									System.out.println("[PDC2017TestPlugin] Case "
											+ XConceptExtension.instance().extractName(subTrace)
											+ " excluded by positive filter " + positiveFilters
											+ " and negative filter " + negativeFilters
											+ ", support = " + filteredTrainingLog.size());
									for (String message : messages) {
										System.out.println("[PDC2017TestPlugin]" + message);
									}
								}
							}
						}
					}
				}
			}
		}
		XLog newClassifiedAprilLog = XFactoryRegistry.instance().currentDefault().createLog();
		for (XTrace trace : classifiedTestLog) {
			if (positiveTestTraces.contains(XConceptExtension.instance().extractName(trace))) {
				newClassifiedAprilLog.add(trace);
			}
		}
		return newClassifiedAprilLog;
	}

	private static XLog filter(XLog log, Set<String> positiveFilters, Set<String> negativeFilters) {
		XLog filteredLog = XFactoryRegistry.instance().currentDefault().createLog();
		for (XTrace trace : log) {
			boolean ok = true;
			Set<String> toMatch = new HashSet<String>(positiveFilters);
			for (XEvent event : trace) {
				String activity = XConceptExtension.instance().extractName(event);
				if (negativeFilters.contains(activity)) {
					ok = false;
					;
				}
				toMatch.remove(activity);
			}
			if (ok && toMatch.isEmpty()) {
				filteredLog.add(trace);
			}
		}
		return filteredLog;
	}

}
