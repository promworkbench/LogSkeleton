package org.processmining.logskeleton.plugins;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

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
import org.processmining.logskeleton.algorithms.LogSkeletonBuilderAlgorithm;
import org.processmining.logskeleton.algorithms.RecurrentActivitySplitterAlgorithm;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.parameters.RecurrentActivitySplitterParameters;

import com.fluxicon.slickerbox.components.SlickerButton;

@Plugin(name = "Configurable Log Skeleton Browser", parameterLabels = { "Event Log" }, returnLabels = { "Configurable Log SKeleton Browser" }, returnTypes = { JComponent.class }, userAccessible = true, help = "Configurable Log Skeleton Browser")
@Visualizer
public class ConfigurableLogSkeletonBrowserPlugin {

	private UIPluginContext context;
	private XLog log;
	private JComponent leftPanel = null;
	private JComponent rightPanel = null;
	private JPanel mainPanel = null;
	private List<List<String>> splitters;
	private Set<String> filters;

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public JComponent run(UIPluginContext context, XLog log) {
		this.context = context;
		this.log = log;

		mainPanel = new JPanel();
		double size[][] = { { 200, TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		mainPanel.setLayout(new TableLayout(size));
		mainPanel.setOpaque(false);

		splitters = new ArrayList<List<String>>();
		filters = new HashSet<String>();

		mainPanel.add(getControlPanel(), "0, 0");

		update();

		return mainPanel;
	}

	private void update() {
		RecurrentActivitySplitterAlgorithm splitterAlgorithm = new RecurrentActivitySplitterAlgorithm();
		RecurrentActivitySplitterParameters splitterParameters = new RecurrentActivitySplitterParameters();
		XLog filteredLog = log;
		if (!filters.isEmpty()) {
			filteredLog = filter(filteredLog, filters);
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
		LogSkeletonBrowserPlugin visualizerPlugin = new LogSkeletonBrowserPlugin();
		if (rightPanel != null) {
			mainPanel.remove(rightPanel);
		}
		rightPanel = visualizerPlugin.run(context, model);
		mainPanel.add(rightPanel, "1, 0");
		mainPanel.validate();
		mainPanel.repaint();
	}

	private XLog filter(XLog log, Set<String> filters) {
		XLog filteredLog = XFactoryRegistry.instance().currentDefault().createLog();
		for (XTrace trace : log) {
			boolean ok = false;
			for (XEvent event : trace) {
				String activity = XConceptExtension.instance().extractName(event);
				if (filters.contains(activity)) {
					ok = true;
				}
			}
			if (ok) {
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
		double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30 } };
		controlPanel.setLayout(new TableLayout(size));
		controlPanel.setOpaque(false);
		DefaultListModel<String> activityModel = new DefaultListModel<String>();
		for (String activity : activities) {
			activityModel.addElement(activity);
		}

		final ProMList<String> activityList = new ProMList<String>("Filters", activityModel);
		activityList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		activityList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selectedActivities = activityList.getSelectedValuesList();
				filters.clear();
				filters.addAll(selectedActivities);
			}
		});
		activityList.setPreferredSize(new Dimension(100, 100));
		controlPanel.add(activityList, "0, 0, 1, 0");

		controlPanel.add(new JLabel("Splitters"), "0, 1, 1, 1");
		final ProMTextField inputs[][] = new ProMTextField[2][10];
		for (int row = 0; row < 10; row++) {
			for (int col = 0; col < 2; col++) {
				inputs[col][row] = new ProMTextField();
				controlPanel.add(inputs[col][row], "" + col + ", " + (row + 2));
			}
		}
		final SlickerButton button = new SlickerButton("   Filter >>");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				splitters = new ArrayList<List<String>>();
				for (int row = 0; row < 10; row++) {
					List<String> filter = new ArrayList<String>();
					for (int col = 0; col < 2; col++) {
						filter.add(inputs[col][row].getText());
					}
					if (!filter.get(0).isEmpty() && !filter.get(1).isEmpty()) {
						System.out.println("[PDC2017LogVisualizerPlugin] Filter added: " + filter);
						splitters.add(filter);
					}
					;
				}
				update();
			}

		});
		controlPanel.add(button, "0, 12, 1, 12");

		return controlPanel;
	}

}
