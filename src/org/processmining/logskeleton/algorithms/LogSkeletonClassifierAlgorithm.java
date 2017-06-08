package org.processmining.logskeleton.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonCount;
import org.processmining.logskeleton.plugins.LogSkeletonBuilderPlugin;
import org.processmining.logskeleton.plugins.LogSkeletonCheckerPlugin;
import org.processmining.logskeleton.plugins.PDC2017Log10FilterPlugin;
import org.processmining.logskeleton.plugins.PDC2017Log10SplitterPlugin;
import org.processmining.logskeleton.plugins.PDC2017Log1FilterPlugin;
import org.processmining.logskeleton.plugins.PDC2017Log2FilterPlugin;
import org.processmining.logskeleton.plugins.PDC2017Log2SplitterPlugin;
import org.processmining.logskeleton.plugins.PDC2017Log4SplitterPlugin;
import org.processmining.logskeleton.plugins.PDC2017Log5FilterPlugin;
import org.processmining.logskeleton.plugins.PDC2017Log5SplitterPlugin;
import org.processmining.logskeleton.plugins.PDC2017Log7SplitterPlugin;
import org.processmining.logskeleton.plugins.PDC2017Log9FilterPlugin;
import org.processmining.logskeleton.plugins.PDC2017Log9SplitterPlugin;

public class LogSkeletonClassifierAlgorithm {

	public XLog apply(PluginContext context, XLog trainingLog, XLog testLog) {
		String name = XConceptExtension.instance().extractName(trainingLog);

		XLog filteredTrainingLog = trainingLog;
		XLog filteredTestLog = testLog;

		/*
		 * Filter out the assumed noise.
		 */
		System.out.println("====== Filter " + name + " ======");
		if (name.equals("log1")) {
			filteredTrainingLog = (new PDC2017Log1FilterPlugin()).run(context, trainingLog);
		} else if (name.equals("log2")) {
			filteredTrainingLog = (new PDC2017Log2FilterPlugin()).run(context, trainingLog);
		} else if (name.equals("log5")) {
			filteredTrainingLog = (new PDC2017Log5FilterPlugin()).run(context, trainingLog);
		} else if (name.equals("log9")) {
			filteredTrainingLog = (new PDC2017Log9FilterPlugin()).run(context, trainingLog);
		} else if (name.equals("log10")) {
			filteredTrainingLog = (new PDC2017Log10FilterPlugin()).run(context, trainingLog);
		}

		/*
		 * Extend log with assumed false negatives from test log. Assumption is here that the test log is not that complete :-(.
		 */
		if (name.equals("log1")) {
			// June 9
//			addTrace(filteredMarchLog, new ArrayList<String>(Arrays.asList("c", "s", "m", "a", "p", "w", "v", "g", "e", "t", "u", "n", "d", "o")));
			// June 18
			addTrace(filteredTrainingLog, new ArrayList<String>(Arrays.asList("p", "v", "g", "e", "t", "c", "a", "w", "u", "m", "n", "s", "h", "o")));
		} else if (name.equals("log6")) {
			// June 11
			addTrace(filteredTrainingLog, new ArrayList<String>(Arrays.asList("d", "n", "a", "f", "k")));
			// June 4
			addTrace(filteredTrainingLog, new ArrayList<String>(Arrays.asList("c", "t", "q", "c", "a", "t", "r")));
		}

		/*
		 * Split the assumed reoccurring activities. 
		 */
		System.out.println("====== Split " + name + " ======");
		if (name.equals("log2")) {
			PDC2017Log2SplitterPlugin splitter = new PDC2017Log2SplitterPlugin();
			filteredTrainingLog = splitter.run(context, filteredTrainingLog);
			filteredTestLog = splitter.run(context, filteredTestLog);
		} else if (name.equals("log4")) {
			PDC2017Log4SplitterPlugin splitter = new PDC2017Log4SplitterPlugin();
			filteredTrainingLog = splitter.run(context, filteredTrainingLog);
			filteredTestLog = splitter.run(context, filteredTestLog);
		} else if (name.equals("log5")) {
			PDC2017Log5SplitterPlugin splitter = new PDC2017Log5SplitterPlugin();
			filteredTrainingLog = splitter.run(context, filteredTrainingLog);
			filteredTestLog = splitter.run(context, filteredTestLog);
		} else if (name.equals("log7")) {
			PDC2017Log7SplitterPlugin splitter = new PDC2017Log7SplitterPlugin();
			filteredTrainingLog = splitter.run(context, filteredTrainingLog);
			filteredTestLog = splitter.run(context, filteredTestLog);
		} else if (name.equals("log9")) {
			PDC2017Log9SplitterPlugin splitter = new PDC2017Log9SplitterPlugin();
			filteredTrainingLog = splitter.run(context, filteredTrainingLog);
			filteredTestLog = splitter.run(context, filteredTestLog);
		} else if (name.equals("log10")) {
			PDC2017Log10SplitterPlugin splitter = new PDC2017Log10SplitterPlugin();
			filteredTrainingLog = splitter.run(context, filteredTrainingLog);
			filteredTestLog = splitter.run(context, filteredTestLog);
		}
		
		/*
		 * Build the log skeleton.
		 */
		LogSkeletonBuilderPlugin createPlugin = new LogSkeletonBuilderPlugin();
		LogSkeleton model = createPlugin.run(context, filteredTrainingLog);
		context.getProvidedObjectManager()
				.createProvidedObject("Model for " + name, model, LogSkeleton.class, context);

		/*
		 * Use the log skeleton to classify the test traces.
		 */
		System.out.println("====== Classify " + name + " ======");
		XLog classifiedTestLog = classify(context, model, filteredTrainingLog, filteredTestLog, name);
		context.getProvidedObjectManager().createProvidedObject("Classified Log " + name, classifiedTestLog,
				XLog.class, context);

		/*
		 * Return the log containing all assumed positive test traces.
		 */
		return classifiedTestLog;
	}
	
	private static XLog classify(PluginContext context, LogSkeleton trainingModel, XLog trainingLog, XLog testLog, String name) {
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
								System.out.println("[PDC2017TestPlugin] Case "
										+ XConceptExtension.instance().extractName(subTrace)
										+ " excluded by positive filter " + positiveFilters + " and negative filter "
										+ negativeFilters + ", support = " + filteredTrainingLog.size());
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
						if (filteredTestLog.isEmpty() || filteredTrainingLog.isEmpty()
								|| filteredTrainingLog.size() < 16) {
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
											+ " and negative filter " + negativeFilters + ", support = "
											+ filteredTrainingLog.size());
									for (String message : messages) {
										System.out.println("[PDC2017TestPlugin]" + message);
									}
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
					for (String activity3 : trainingModel.getActivities()) {
						if (activity3 == LogSkeletonCount.STARTEVENT || activity3 == LogSkeletonCount.ENDEVENT) {
							continue;
						}
						if (activity3.compareTo(activity2) >= 0) {
							continue;
						}
						if (activity3.compareTo(activity) >= 0) {
							continue;
						}
						Set<String> positiveFilters = new HashSet<String>();
						Set<String> negativeFilters = new HashSet<String>();
						negativeFilters.add(activity);
						negativeFilters.add(activity2);
						negativeFilters.add(activity3);
						XLog filteredTrainingLog = filter(trainingLog, positiveFilters, negativeFilters);
						XLog filteredTestLog = filter(testLog, positiveFilters, negativeFilters);
						if (filteredTestLog.isEmpty() || filteredTrainingLog.isEmpty()
								|| filteredTrainingLog.size() < 16) {
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
											+ " and negative filter " + negativeFilters + ", support = "
											+ filteredTrainingLog.size());
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
		XLog newClassifiedTestLog = XFactoryRegistry.instance().currentDefault().createLog();
		XConceptExtension.instance().assignName(newClassifiedTestLog, name + " (classified)");
		for (XTrace trace : classifiedTestLog) {
			if (positiveTestTraces.contains(XConceptExtension.instance().extractName(trace))) {
				newClassifiedTestLog.add(trace);
			}
		}
		return newClassifiedTestLog;
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

	private static void addTrace(XLog log, List<String> activities) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XTrace trace = factory.createTrace();
		for (String activity : activities) {
			XEvent event = factory.createEvent();
			XConceptExtension.instance().assignName(event, activity);
			trace.add(event);
		}
		log.add(trace);
	}

}
