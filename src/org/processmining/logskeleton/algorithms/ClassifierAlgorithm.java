package org.processmining.logskeleton.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.logskeleton.classifiers.PrefixClassifier;
import org.processmining.logskeleton.configurations.BuilderConfiguration;
import org.processmining.logskeleton.configurations.CheckerConfiguration;
import org.processmining.logskeleton.configurations.ClassifierConfiguration;
import org.processmining.logskeleton.inputs.BuilderInput;
import org.processmining.logskeleton.inputs.CheckerInput;
import org.processmining.logskeleton.inputs.ClassifierInput;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonCount;
import org.processmining.logskeleton.models.Violation;
import org.processmining.logskeleton.outputs.CheckerOutput;
import org.processmining.logskeleton.outputs.ClassifierOutput;

public class ClassifierAlgorithm {

	public ClassifierOutput apply(PluginContext context, ClassifierInput input, ClassifierConfiguration configuration) {
		XLog referenceLog = input.getReferenceLog();

		String name = XConceptExtension.instance().extractName(referenceLog);

		/*
		 * Preprocess the logs.
		 */
		PreprocessorAlgorithm preprocessorAlgorithm = new PreprocessorAlgorithm();
		input = preprocessorAlgorithm.preprocess(context, input);
		XLog preprocessedReferenceLog = input.getReferenceLog();
		XLog preprocessedLog = input.getLog();

		/*
		 * Build the log skeleton.
		 */
		BuilderAlgorithm builderAlgorithm = new BuilderAlgorithm();
		LogSkeleton logSkeleton = builderAlgorithm
				.apply(context, new BuilderInput(preprocessedReferenceLog), new BuilderConfiguration(configuration))
				.getLogSkeleton();
		if (configuration.isCreateProvidedObjects()) {
			context.getProvidedObjectManager().createProvidedObject("Reference log skeleton for " + name, logSkeleton,
					LogSkeleton.class, context);
		}

		/*
		 * Use the log skeleton to classify the test traces.
		 */
		System.out.println("[ClassifierAlgorithm] Classify " + name + " ======");
		XLog classifiedLog = classify(context, logSkeleton, preprocessedReferenceLog, preprocessedLog,
				new PrefixClassifier(configuration.getClassifier()), name, configuration.getMaxFilterDepth());
		if (configuration.isCreateProvidedObjects()) {
			context.getProvidedObjectManager().createProvidedObject("Classified Log " + name, classifiedLog, XLog.class,
					context);
		}

		/*
		 * Return the log containing all assumed positive test traces.
		 */
		return new ClassifierOutput(classifiedLog);
	}

	private static XLog classify(PluginContext context, LogSkeleton logSkeleton, XLog referenceLog, XLog log,
			XEventClassifier classifier, String name, int maxFilterDepth) {
		BuilderAlgorithm builderAlgorithm = new BuilderAlgorithm();
		CheckerAlgorithm checkerAlgorithm = new CheckerAlgorithm();
		CheckerInput checkerInput = new CheckerInput(logSkeleton, log);
		CheckerConfiguration checkerConfiguration = new CheckerConfiguration(checkerInput);
		checkerConfiguration.setClassifier(classifier);
		checkerConfiguration.setCheck(0, true);
		checkerConfiguration.setCheck(1, true);
		checkerConfiguration.setCheck(2, false);
		CheckerOutput checkerOutput = checkerAlgorithm.apply(context, checkerInput, checkerConfiguration);
		XLog classifiedTestLog = checkerOutput.getLog();
		Collection<Violation> violations = checkerOutput.getViolations();
		Set<String> positiveTestTraces = new HashSet<String>();
		int threshold = 0;
		for (XTrace trace : classifiedTestLog) {
			positiveTestTraces.add(XConceptExtension.instance().extractName(trace));
		}
		for (Violation violation : violations) {
			System.out.println(violation);
		}
		for (int i = 0; i < 3; i++) {
			checkerConfiguration.setCheck(0, i == 0);
			checkerConfiguration.setCheck(1, i == 1);
			checkerConfiguration.setCheck(2, i == 2);
			if (maxFilterDepth < 1) {
				continue;
			}
			for (String activity : logSkeleton.getActivities()) {
				if (positiveTestTraces.size() <= threshold) {
					continue;
				}
				if (activity == LogSkeletonCount.STARTEVENT || activity == LogSkeletonCount.ENDEVENT) {
					continue;
				}
				if (!logSkeleton.getSameCounts(activity).iterator().next().equals(activity)) {
					continue;
				}
				
				for (int f = 0; f < 2; f++) {
					if (positiveTestTraces.size() <= threshold) {
						continue;
					}
					Set<String> positiveFilters = new HashSet<String>();
					Set<String> negativeFilters = new HashSet<String>();
					if (f == 0) {
						positiveFilters.add(activity);
					} else {
						negativeFilters.add(activity);
					}
//					System.out.println(
//							"[ClassifierAlgorithm] Positive = " + positiveFilters + ", Negative = " + negativeFilters);
					XLog filteredReferenceLog = filter(referenceLog, classifier, positiveFilters, negativeFilters);
					XLog filteredLog = filter(log, classifier, positiveFilters, negativeFilters);
					if (filteredLog.isEmpty() || filteredReferenceLog.isEmpty() || filteredReferenceLog.size() < 16) {
						continue;
					}
					//					System.out.println("[PDC2017TestPlugin] Remaining traces 1: " + filteredTrainingLog.size());
					BuilderInput builderInput = new BuilderInput(filteredReferenceLog);
					BuilderConfiguration builderConfiguration = new BuilderConfiguration(builderInput);
					builderConfiguration.setClassifier(classifier);
					LogSkeleton filteredLogSkeleton = builderAlgorithm
							.apply(context, builderInput, builderConfiguration).getLogSkeleton();

					checkerOutput = checkerAlgorithm.apply(context, new CheckerInput(filteredLogSkeleton, filteredLog), checkerConfiguration);
					XLog classifiedLog = checkerOutput.getLog();
					Collection<Violation> classifiedViolations = checkerOutput.getViolations();
					for (XTrace subTrace : filteredLog) {
						//						if (positiveTestTraces.size() <= threshold) {
						//							continue;
						//						}
						if (!classifiedLog.contains(subTrace)) {
							String caseId = XConceptExtension.instance().extractName(subTrace);
							if (positiveTestTraces.remove(caseId)) {
								System.out.println("[ClassifierAlgoritmm] Case "
										+ XConceptExtension.instance().extractName(subTrace)
										+ " excluded by positive filter " + positiveFilters + " and negative filter "
										+ negativeFilters + ", support = " + filteredReferenceLog.size());
								for (Violation violation : classifiedViolations) {
									System.out.println(violation);
								}
							}
						}
					}
				}
			}
			if (maxFilterDepth < 2) {
				continue;
			}
			for (String activity : logSkeleton.getActivities()) {
				if (positiveTestTraces.size() <= threshold) {
					continue;
				}
				if (activity == LogSkeletonCount.STARTEVENT || activity == LogSkeletonCount.ENDEVENT) {
					continue;
				}
				if (!logSkeleton.getSameCounts(activity).iterator().next().equals(activity)) {
					continue;
				}
				for (String activity2 : logSkeleton.getActivities()) {
					if (positiveTestTraces.size() <= threshold) {
						continue;
					}
					if (activity2 == LogSkeletonCount.STARTEVENT || activity2 == LogSkeletonCount.ENDEVENT) {
						continue;
					}
					if (!logSkeleton.getSameCounts(activity2).iterator().next().equals(activity2)) {
						continue;
					}
					if (logSkeleton.getSameCounts(activity).contains(activity2)) {
						continue;
					}
					for (int f = 0; f < 4; f++) {
						if (positiveTestTraces.size() <= threshold) {
							continue;
						}
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
//						System.out.println("[ClassifierAlgorithm] Positive = " + positiveFilters + ", Negative = "
//								+ negativeFilters);
						XLog filteredReferenceLog = filter(referenceLog, classifier, positiveFilters, negativeFilters);
						XLog filteredLog = filter(log, classifier, positiveFilters, negativeFilters);
						if (filteredLog.isEmpty() || filteredReferenceLog.isEmpty()
								|| filteredReferenceLog.size() < 16) {
							continue;
						}
						BuilderInput builderInput = new BuilderInput(filteredReferenceLog);
						BuilderConfiguration builderConfiguration = new BuilderConfiguration(builderInput);
						builderConfiguration.setClassifier(classifier);
						LogSkeleton filteredLogSkeleton = builderAlgorithm
								.apply(context, builderInput, builderConfiguration).getLogSkeleton();

						checkerOutput = checkerAlgorithm.apply(context, new CheckerInput(filteredLogSkeleton, filteredLog), checkerConfiguration);
						XLog classifiedLog = checkerOutput.getLog();
						Collection<Violation> classifiedViolations = checkerOutput.getViolations();
						for (XTrace subTrace : filteredLog) {
							if (!classifiedLog.contains(subTrace)) {
								String caseId = XConceptExtension.instance().extractName(subTrace);
								if (positiveTestTraces.remove(caseId)) {
									System.out.println("[ClassifierAlgoritmm] Case "
											+ XConceptExtension.instance().extractName(subTrace)
											+ " excluded by positive filter " + positiveFilters
											+ " and negative filter " + negativeFilters + ", support = "
											+ filteredReferenceLog.size());
									for (Violation violation : classifiedViolations) {
										System.out.println(violation);
									}
								}
							}
						}
					}
				}
			}
			if (maxFilterDepth < 3) {
				continue;
			}
			for (String activity : logSkeleton.getActivities()) {
				if (positiveTestTraces.size() <= threshold) {
					continue;
				}
				if (activity == LogSkeletonCount.STARTEVENT || activity == LogSkeletonCount.ENDEVENT) {
					continue;
				}
				if (!logSkeleton.getSameCounts(activity).iterator().next().equals(activity)) {
					continue;
				}
				for (String activity2 : logSkeleton.getActivities()) {
					if (positiveTestTraces.size() <= threshold) {
						continue;
					}
					if (activity2 == LogSkeletonCount.STARTEVENT || activity2 == LogSkeletonCount.ENDEVENT) {
						continue;
					}
					if (!logSkeleton.getSameCounts(activity2).iterator().next().equals(activity2)) {
						continue;
					}
					if (logSkeleton.getSameCounts(activity).contains(activity2)) {
						continue;
					}
					for (String activity3 : logSkeleton.getActivities()) {
						if (positiveTestTraces.size() <= threshold) {
							continue;
						}
						if (activity3 == LogSkeletonCount.STARTEVENT || activity3 == LogSkeletonCount.ENDEVENT) {
							continue;
						}
						if (!logSkeleton.getSameCounts(activity3).iterator().next().equals(activity3)) {
							continue;
						}
						if (logSkeleton.getSameCounts(activity).contains(activity3)) {
							continue;
						}
						if (logSkeleton.getSameCounts(activity2).contains(activity3)) {
							continue;
						}
						for (int f = 0; f < 8; f++) {
							if (positiveTestTraces.size() <= threshold) {
								continue;
							}
							Set<String> positiveFilters = new HashSet<String>();
							Set<String> negativeFilters = new HashSet<String>();
							if (f == 0 || f == 1 || f == 2 || f == 3) {
								positiveFilters.add(activity);
							} else {
								negativeFilters.add(activity);
							}
							if (f == 0 || f == 1 || f == 4 || f == 5) {
								positiveFilters.add(activity2);
							} else {
								negativeFilters.add(activity2);
							}
							if (f == 0 || f == 2 || f == 4 || f == 6) {
								positiveFilters.add(activity3);
							} else {
								negativeFilters.add(activity3);
							}
//							System.out.println("[ClassifierAlgorithm] Positive = " + positiveFilters
//									+ ", Negative = " + negativeFilters);
							XLog filteredReferenceLog = filter(referenceLog, classifier, positiveFilters,
									negativeFilters);
							XLog filteredLog = filter(log, classifier, positiveFilters, negativeFilters);
							if (filteredLog.isEmpty() || filteredReferenceLog.isEmpty()
									|| filteredReferenceLog.size() < 16) {
								continue;
							}
							BuilderInput builderInput = new BuilderInput(filteredReferenceLog);
							BuilderConfiguration builderConfiguration = new BuilderConfiguration(builderInput);
							builderConfiguration.setClassifier(classifier);
							LogSkeleton filteredLogSkeleton = builderAlgorithm
									.apply(context, builderInput, builderConfiguration).getLogSkeleton();

							checkerOutput = checkerAlgorithm.apply(context, new CheckerInput(filteredLogSkeleton, filteredLog), checkerConfiguration);
							XLog classifiedLog = checkerOutput.getLog();
							Collection<Violation> classifiedViolations = checkerOutput.getViolations();
							for (XTrace subTrace : filteredLog) {
								//							if (positiveTestTraces.size() <= threshold) {
								//								continue;
								//							}
								if (!classifiedLog.contains(subTrace)) {
									String caseId = XConceptExtension.instance().extractName(subTrace);
									if (positiveTestTraces.remove(caseId)) {
										System.out.println("[ClassifierAlgoritmm] Case "
												+ XConceptExtension.instance().extractName(subTrace)
												+ " excluded by positive filter " + positiveFilters
												+ " and negative filter " + negativeFilters + ", support = "
												+ filteredReferenceLog.size());
										for (Violation violation : classifiedViolations) {
											System.out.println(violation);
										}
									}
								}
							}
						}

					}
				}
			}
		}
		XLog classifiedLog = XFactoryRegistry.instance().currentDefault().createLog();
		XConceptExtension.instance().assignName(classifiedLog, name + " (classified)");
		for (XTrace trace : classifiedTestLog) {
			if (positiveTestTraces.contains(XConceptExtension.instance().extractName(trace))) {
				classifiedLog.add(trace);
			}
		}
		return classifiedLog;
	}

	private static XLog filter(XLog log, XEventClassifier classifier, Set<String> positiveFilters,
			Set<String> negativeFilters) {
		XLog filteredLog = XFactoryRegistry.instance().currentDefault().createLog();
		for (XTrace trace : log) {
			boolean ok = true;
			Set<String> toMatch = new HashSet<String>(positiveFilters);
			for (XEvent event : trace) {
				String activity = classifier.getClassIdentity(event);
				if (negativeFilters.contains(activity)) {
					ok = false;
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
