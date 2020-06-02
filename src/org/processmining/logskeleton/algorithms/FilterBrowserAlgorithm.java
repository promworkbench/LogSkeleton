package org.processmining.logskeleton.algorithms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.util.CompositePanel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.logskeleton.configurations.BrowserConfiguration;
import org.processmining.logskeleton.configurations.BuilderConfiguration;
import org.processmining.logskeleton.configurations.FilterBrowserConfiguration;
import org.processmining.logskeleton.inputs.BrowserInput;
import org.processmining.logskeleton.inputs.BuilderInput;
import org.processmining.logskeleton.inputs.FilterBrowserInput;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.outputs.FilterBrowserOutput;
import org.processmining.logskeleton.parameters.SplitterParameters;
import org.processmining.logskeleton.plugins.BrowserPlugin;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.components.SlickerButton;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class FilterBrowserAlgorithm {

	private PluginContext context;
	private XLog log;
	private JComponent rightPanel = null;
	private JPanel mainPanel = null;
//	private RoundedPanel splitterPanel = null;
//	private List<List<String>> splitters;
//	private Set<String> positiveFilters;
//	private Set<String> negativeFilters;
	private BrowserConfiguration browserConfiguration = null;
	
	public FilterBrowserOutput apply(PluginContext context, FilterBrowserInput input) {
		return apply(context, input, new FilterBrowserConfiguration(input));
	}
	
	public FilterBrowserOutput apply(PluginContext context, FilterBrowserInput input, FilterBrowserConfiguration configuration) {
		this.context = context;
		log = input.getLog();
		XEventClassifier classifier = /*new PrefixClassifier(*/configuration.getClassifier()/*)*/;
		
		mainPanel = new CompositePanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5348800905816927444L;

			public JComponent getMainComponent() {
				if (rightPanel instanceof CompositePanel) {
					return ((CompositePanel) rightPanel).getMainComponent();
				}
				return rightPanel;
			}
		};
		double size[][] = { { 250, TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		mainPanel.setLayout(new TableLayout(size));
		mainPanel.setOpaque(false);

//		splitters = new ArrayList<List<String>>();
//		positiveFilters = new HashSet<String>();
//		negativeFilters = new HashSet<String>();

		mainPanel.add(getControlPanel(configuration), "0, 0");

		update(context, configuration);

		provideInfo(log, configuration);
		
		return new FilterBrowserOutput(mainPanel);
	}

	private void provideInfo(XLog log, FilterBrowserConfiguration configuration) {
		XEventClassifier classifier = configuration.getClassifier();
		Map<Set<String>, Double> scores = new HashMap<Set<String>, Double>();
		double maxScore = 0;
		for (XTrace trace : log) {
			Set<String> score = new HashSet<String>();
			for (XEvent event : trace) {
				score.add(classifier.getClassIdentity(event));
			}
			if (!scores.containsKey(score)) {
				scores.put(score, 1.0 / (trace.size() + 1));
			} else {
				scores.put(score, scores.get(score) + (1.0 / (trace.size() + 1)));
			}
			if (scores.get(score) > maxScore) {
				maxScore = scores.get(score);
			}
		}
		for (Set<String> count : scores.keySet()) {
			if (scores.get(count) == maxScore) {
				System.out.println("[FilterBrowserAlgorithm] " + maxScore + ": " + count);
			}
		}
	}
	
	private void update(PluginContext context, FilterBrowserConfiguration configuration) {
		SplitterAlgorithm splitterAlgorithm = new SplitterAlgorithm();
		SplitterParameters splitterParameters = new SplitterParameters();
		XLog filteredLog = log;
		int horizon = configuration.getHorizon();
		XEventClassifier classifier = configuration.getClassifier();
		Set<String> positiveFilters = configuration.getPositiveFilters();
		Set<String> negativeFilters = configuration.getNegativeFilters();
		Set<String> boundaryActivities = configuration.getBoundaryActivities();
		List<List<String>> splitters = configuration.getSplitters();

		if (!positiveFilters.isEmpty() || !negativeFilters.isEmpty()) {
			filteredLog = filter(filteredLog, configuration);
		}
		for (List<String> splitter : splitters) {
			splitterParameters.setDuplicateActivity(splitter.get(0));
			splitterParameters.getMilestoneActivities().clear();
			for (int i = 1; i < splitter.size(); i++) {
				splitterParameters.getMilestoneActivities().add(splitter.get(i));
			}
			filteredLog = splitterAlgorithm.apply(filteredLog, classifier, splitterParameters);
		}
		BuilderAlgorithm builderAlgorithm = new BuilderAlgorithm();
		context.getProvidedObjectManager().createProvidedObject(XConceptExtension.instance().extractName(filteredLog) + " Split", filteredLog, XLog.class, context);
		BuilderInput builderInput = new BuilderInput(filteredLog);
		BuilderConfiguration builderConfiguration = new BuilderConfiguration(builderInput);
		builderConfiguration.setClassifier(classifier);
		builderConfiguration.setHorizon(horizon);
		builderConfiguration.setBoundaryActivities(boundaryActivities);
		LogSkeleton logSkeleton = builderAlgorithm.apply(context, builderInput, builderConfiguration).getLogSkeleton();
		logSkeleton.setRequired(positiveFilters);
		logSkeleton.setForbidden(negativeFilters);
		logSkeleton.setBoundary(boundaryActivities);
		logSkeleton.setSplitters(splitters);
		BrowserPlugin visualizerPlugin = new BrowserPlugin();
		if (rightPanel != null) {
			mainPanel.remove(rightPanel);
		}
		if (browserConfiguration == null) {
			browserConfiguration = new BrowserConfiguration(new BrowserInput(logSkeleton));
			browserConfiguration.getActivities().retainAll(logSkeleton.getActivities());
		}
		rightPanel = visualizerPlugin.run(context, logSkeleton, browserConfiguration);
		mainPanel.add(rightPanel, "1, 0");
		mainPanel.validate();
		mainPanel.repaint();
	}

	private XLog filter(XLog log, FilterBrowserConfiguration configuration) {
		XLog filteredLog = createLog(log);
		XLog discardedLog = createLog(log);
		XEventClassifier classifier = configuration.getClassifier();
		Set<String> positiveFilters = configuration.getPositiveFilters();
		Set<String> negativeFilters = configuration.getNegativeFilters();
		for (XTrace trace : log) {
			boolean ok = true;
			Set<String> toMatch = new HashSet<String>(positiveFilters);
			for (XEvent event : trace) {
				String activity = classifier.getClassIdentity(event);
				if (negativeFilters.contains(activity)) {
					ok = false;
					;
				}
				toMatch.remove(activity);
			}
			if (ok && toMatch.isEmpty()) {
				filteredLog.add(trace);
			} else {
				discardedLog.add(trace);
			}
		}
		context.getProvidedObjectManager().createProvidedObject(XConceptExtension.instance().extractName(filteredLog) + " In", filteredLog, XLog.class, context);
		context.getProvidedObjectManager().createProvidedObject(XConceptExtension.instance().extractName(discardedLog) + " Out", discardedLog, XLog.class, context);
		return filteredLog;
	}

	private XLog createLog(XLog log) {
		XLog createdLog = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
		createdLog.getExtensions().addAll(log.getExtensions());
		createdLog.getGlobalEventAttributes().addAll(log.getGlobalEventAttributes());
		createdLog.getGlobalTraceAttributes().addAll(log.getGlobalTraceAttributes());
		createdLog.getClassifiers().addAll(log.getClassifiers());
		return createdLog;
	}
	
	private List<String> getActivities(XLog log, FilterBrowserConfiguration configuration) {
		XEventClassifier classifier = configuration.getClassifier();
		Set<String> activities = new HashSet<String>();
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				String activity = classifier.getClassIdentity(event);
				activities.add(activity);
			}
		}
		List<String> activityList = new ArrayList<String>(activities);
		Collections.sort(activityList);
		return activityList;
	}

	private JComponent getControlPanel(final FilterBrowserConfiguration configuration) {
		final Set<String> positiveFilters = configuration.getPositiveFilters();
		final Set<String> negativeFilters = configuration.getNegativeFilters();
		final Set<String> boundaryActivities = configuration.getBoundaryActivities();
		final List<List<String>> splitters = configuration.getSplitters();

		final JPanel controlPanel = new JPanel();
		List<String> activities = getActivities(log, configuration);
		double controlSize[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL, 30, 30 } };
		controlPanel.setLayout(new TableLayout(controlSize));
		controlPanel.setOpaque(false);
		controlPanel.setBackground(WidgetColors.COLOR_LIST_BG);
		controlPanel.setForeground(WidgetColors.COLOR_LIST_FG);

		final JPanel filterPanel = new JPanel();
		double filterSize[][] = { { TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 30 } };
		filterPanel.setLayout(new TableLayout(filterSize));
		filterPanel.setOpaque(false);
		
		DefaultListModel<String> requiredActivityModel = new DefaultListModel<String>();
		for (String activity : activities) {
			requiredActivityModel.addElement(activity);
		}
		final ProMList<String> requiredActivityList = new ProMList<String>("Select required activities", requiredActivityModel);
		requiredActivityList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		requiredActivityList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selectedActivities = requiredActivityList.getSelectedValuesList();
				positiveFilters.clear();
				positiveFilters.addAll(selectedActivities);
			}
		});
		requiredActivityList.setPreferredSize(new Dimension(100, 100));
		filterPanel.add(requiredActivityList, "0, 0");

		DefaultListModel<String> forbiddenActivityModel = new DefaultListModel<String>();
		for (String activity : activities) {
			forbiddenActivityModel.addElement(activity);
		}
		final ProMList<String> forbiddenActivityList = new ProMList<String>("Select forbidden activities",
				forbiddenActivityModel);
		forbiddenActivityList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		forbiddenActivityList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selectedActivities = forbiddenActivityList.getSelectedValuesList();
				negativeFilters.clear();
				negativeFilters.addAll(selectedActivities);
			}
		});
		forbiddenActivityList.setPreferredSize(new Dimension(100, 100));
		filterPanel.add(forbiddenActivityList, "0, 1");

		DefaultListModel<String> boundaryActivityModel = new DefaultListModel<String>();
		for (String activity : activities) {
			boundaryActivityModel.addElement(activity);
		}
		final ProMList<String> boundaryActivityList = new ProMList<String>("Select boundary activities",
				boundaryActivityModel);
		boundaryActivityList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		boundaryActivityList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selectedActivities = boundaryActivityList.getSelectedValuesList();
				boundaryActivities.clear();
				boundaryActivities.addAll(selectedActivities);
			}
		});
		boundaryActivityList.setPreferredSize(new Dimension(100, 100));
		filterPanel.add(boundaryActivityList, "0, 2");

		final NiceSlider horizonSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Horizon (0 means no horizon)", 0, 20, configuration.getHorizon(), Orientation.HORIZONTAL);
		horizonSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = horizonSlider.getSlider().getValue();
				configuration.setHorizon(value);
			}
		});
		horizonSlider.setPreferredSize(new Dimension(100, 30));
		filterPanel.add(horizonSlider, "0, 3");

		final RoundedPanel splitterPanel = new RoundedPanel(10, 5, 0);
		splitterPanel.setPreferredSize(new Dimension(100, 100));
		double splitterSize[][] = {
				{ TableLayoutConstants.FILL, TableLayoutConstants.FILL },
				{ /*30,*/ TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL } };
		splitterPanel.setLayout(new TableLayout(splitterSize));
		splitterPanel.setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		splitterPanel.setForeground(WidgetColors.COLOR_LIST_FG);

		splitterPanel.setOpaque(false);
//		JLabel splitterLabel = new JLabel("Select activity splitters");
//		splitterLabel.setOpaque(false);
//		splitterLabel.setForeground(WidgetColors.COLOR_LIST_SELECTION_FG);
//		splitterLabel.setFont(splitterLabel.getFont().deriveFont(13f));
//		splitterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
//		splitterLabel.setHorizontalAlignment(SwingConstants.CENTER);
//		splitterLabel.setHorizontalTextPosition(SwingConstants.CENTER);

//		splitterPanel.add(splitterLabel, "0, 0, 1, 0");
		final ProMTextField inputs[][] = new ProMTextField[2][16];
		for (int row = 0; row < 16; row++) {
			for (int col = 0; col < 2; col++) {
				inputs[col][row] = new ProMTextField("", (col == 0 ? "Split activity " : "over activity ") + (1 + col + 2*row));
				splitterPanel.add(inputs[col][row], "" + col + ", " + (row/* + 1*/));
			}
		}
//		controlPanel.add(splitterPanel, "0, 2");

//		final SlickerButton splitterButton = new SlickerButton("Select activity splitters...");
//		splitterButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				JFrame frame = new JFrame();
//				frame.setIconImage(ImageLoader.load("rotule_30x35.png"));
//				frame.add(splitterPanel);
//				frame.setTitle("Select activity splitters for " + XConceptExtension.instance().extractName(log));
//				frame.setSize(460, 380);
//				frame.setVisible(true);
//			}
//
//		});
//		controlPanel.add(splitterButton, "0, 2");

		final SlickerButton basicButton = new SlickerButton("Filter options");
		final SlickerButton advancedButton = new SlickerButton("Splitter options");
		
		basicButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controlPanel.remove(splitterPanel);
				controlPanel.add(filterPanel, "0, 0, 1, 0");
				advancedButton.setEnabled(true);
				basicButton.setEnabled(false);
				controlPanel.validate();
				controlPanel.repaint();
			}

		});
		advancedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controlPanel.remove(filterPanel);
				controlPanel.add(splitterPanel, "0, 0, 1, 0");
				advancedButton.setEnabled(false);
				basicButton.setEnabled(true);
				controlPanel.validate();
				controlPanel.repaint();
			}

		});
		
		controlPanel.add(basicButton, "0, 1");
		controlPanel.add(advancedButton, "1, 1");
		
		controlPanel.add(filterPanel, "0, 0, 1, 0");
		advancedButton.setEnabled(true);
		basicButton.setEnabled(false);

		final SlickerButton button = new SlickerButton("Apply settings");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				splitters.clear();
				for (int row = 0; row < 16; row++) {
					List<String> filter = new ArrayList<String>();
					for (int col = 0; col < 2; col++) {
						filter.add(inputs[col][row].getText());
					}
					if (!filter.get(0).isEmpty() && !filter.get(1).isEmpty()) {
						System.out.println("[FilterBrowserAlgorithm] Filter added: " + filter);
						splitters.add(filter);
					}
				}
				update(context, configuration);
			}

		});
		controlPanel.add(button, "0, 2, 1, 2");

		return controlPanel;
	}
}
