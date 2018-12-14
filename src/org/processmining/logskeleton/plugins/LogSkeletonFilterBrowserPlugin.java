package org.processmining.logskeleton.plugins;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.logskeleton.algorithms.LogSkeletonBuilderAlgorithm;
import org.processmining.logskeleton.algorithms.SplitterAlgorithm;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.parameters.SplitterParameters;

import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.components.SlickerButton;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

@Plugin(name = "Log Skeleton Filter and Browser", parameterLabels = { "Event Log" }, returnLabels = { "Log Skeleton Filter and Browser" }, returnTypes = { JComponent.class }, userAccessible = true, help = "Log Skeleton Filter and Browser")
@Visualizer
public class LogSkeletonFilterBrowserPlugin {

	private UIPluginContext context;
	private XLog log;
	private JComponent rightPanel = null;
	private JPanel mainPanel = null;
	private List<List<String>> splitters;
	private Set<String> positiveFilters;
	private Set<String> negativeFilters;

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public JComponent run(UIPluginContext context, XLog log) {
		this.context = context;
		this.log = log;

		mainPanel = new JPanel();
		double size[][] = { { 250, TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		mainPanel.setLayout(new TableLayout(size));
		mainPanel.setOpaque(false);

		splitters = new ArrayList<List<String>>();
		positiveFilters = new HashSet<String>();
		negativeFilters = new HashSet<String>();

		mainPanel.add(getControlPanel(), "0, 0");

		update();

		return mainPanel;
	}

	private void update() {
		SplitterAlgorithm splitterAlgorithm = new SplitterAlgorithm();
		SplitterParameters splitterParameters = new SplitterParameters();
		XLog filteredLog = log;
		if (!positiveFilters.isEmpty() || !negativeFilters.isEmpty()) {
			filteredLog = filter(filteredLog, positiveFilters, negativeFilters);
		}
		for (List<String> splitter : splitters) {
			splitterParameters.setDuplicateActivity(splitter.get(0));
			splitterParameters.getMilestoneActivities().clear();
			for (int i = 1; i < splitter.size(); i++) {
				splitterParameters.getMilestoneActivities().add(splitter.get(i));
			}
			filteredLog = splitterAlgorithm.apply(filteredLog, splitterParameters);
		}
		LogSkeletonBuilderAlgorithm discoveryAlgorithm = new LogSkeletonBuilderAlgorithm();
		LogSkeleton model = discoveryAlgorithm.apply(filteredLog);
		model.setRequired(positiveFilters);
		model.setForbidden(negativeFilters);
		model.setSplitters(splitters);
		LogSkeletonBrowserPlugin visualizerPlugin = new LogSkeletonBrowserPlugin();
		if (rightPanel != null) {
			mainPanel.remove(rightPanel);
		}
		rightPanel = visualizerPlugin.run(context, model);
		mainPanel.add(rightPanel, "1, 0");
		mainPanel.validate();
		mainPanel.repaint();
	}

	private static XLog filter(XLog log, Set<String> positiveFilters, Set<String> negativeFilters) {
		XLog filteredLog = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
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

	private List<String> getActivities(XLog log) {
		Set<String> activities = new HashSet<String>();
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				String activity = XConceptExtension.instance().extractName(event);
				activities.add(activity);
			}
		}
		List<String> activityList = new ArrayList<String>(activities);
		Collections.sort(activityList);
		return activityList;
	}

	private JComponent getControlPanel() {
		JPanel controlPanel = new JPanel();
		List<String> activities = getActivities(log);
		double size[][] = { { TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 30 } };
		controlPanel.setLayout(new TableLayout(size));
		controlPanel.setOpaque(false);
		controlPanel.setBackground(WidgetColors.COLOR_LIST_BG);
		controlPanel.setForeground(WidgetColors.COLOR_LIST_FG);

		DefaultListModel<String> requiredActivityModel = new DefaultListModel<String>();
		for (String activity : activities) {
			requiredActivityModel.addElement(activity);
		}
		final ProMList<String> requiredActivityList = new ProMList<String>("Required Activities Filter", requiredActivityModel);
		requiredActivityList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		requiredActivityList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selectedActivities = requiredActivityList.getSelectedValuesList();
				positiveFilters.clear();
				positiveFilters.addAll(selectedActivities);
			}
		});
		requiredActivityList.setPreferredSize(new Dimension(100, 100));
		controlPanel.add(requiredActivityList, "0, 0");

		DefaultListModel<String> forbiddenActivityModel = new DefaultListModel<String>();
		for (String activity : activities) {
			forbiddenActivityModel.addElement(activity);
		}
		final ProMList<String> forbiddenActivityList = new ProMList<String>("Forbidden Activities Filter",
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
		controlPanel.add(forbiddenActivityList, "0, 1");

		RoundedPanel splitterPanel = new RoundedPanel(10, 5, 0);
		splitterPanel.setPreferredSize(new Dimension(100, 100));
		double splitterSize[][] = {
				{ TableLayoutConstants.FILL, TableLayoutConstants.FILL },
				{ 30, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
						TableLayoutConstants.FILL, TableLayoutConstants.FILL } };
		splitterPanel.setLayout(new TableLayout(splitterSize));
		splitterPanel.setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		splitterPanel.setForeground(WidgetColors.COLOR_LIST_FG);

		splitterPanel.setOpaque(false);
		JLabel splitterLabel = new JLabel("Activity Splitters");
		splitterLabel.setOpaque(false);
		splitterLabel.setForeground(WidgetColors.COLOR_LIST_SELECTION_FG);
		splitterLabel.setFont(splitterLabel.getFont().deriveFont(13f));
		splitterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		splitterLabel.setHorizontalAlignment(SwingConstants.CENTER);
		splitterLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		splitterPanel.add(splitterLabel, "0, 0, 1, 0");
		final ProMTextField inputs[][] = new ProMTextField[2][10];
		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 2; col++) {
				inputs[col][row] = new ProMTextField("", (col == 0 ? "Split Activity " : "Over Activity ") + (1 + col + 2*row));
				splitterPanel.add(inputs[col][row], "" + col + ", " + (row + 1));
			}
		}
		controlPanel.add(splitterPanel, "0, 2");

		final SlickerButton button = new SlickerButton("Apply Settings");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				splitters = new ArrayList<List<String>>();
				for (int row = 0; row < 10; row++) {
					List<String> filter = new ArrayList<String>();
					for (int col = 0; col < 2; col++) {
						filter.add(inputs[col][row].getText());
					}
					if (!filter.get(0).isEmpty() && !filter.get(1).isEmpty()) {
						System.out.println("[LogSkeletonFilterBrowserPlugin] Filter added: " + filter);
						splitters.add(filter);
					}
				}
				update();
			}

		});
		controlPanel.add(button, "0, 3");

		return controlPanel;
	}

}
