package org.processmining.logskeleton.plugins;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.parameters.LogSkeletonBrowser;
import org.processmining.logskeleton.parameters.LogSkeletonBrowserParameters;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import com.fluxicon.slickerbox.components.SlickerButton;

@Plugin(name = "Log Skeleton Browser", parameterLabels = { "Log Skeleton" }, returnLabels = { "Log Skeleton Browser" }, returnTypes = { JComponent.class }, userAccessible = true, help = "Log Skeleton Browser")
@Visualizer
public class LogSkeletonBrowserPlugin {

	private LogSkeleton model;
	private LogSkeletonBrowserParameters parameters = new LogSkeletonBrowserParameters();
	private JComponent leftDotPanel = null;
	private JComponent rightDotPanel = null;
	private JPanel mainPanel = null;

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public JComponent run(UIPluginContext context, LogSkeleton model) {

		this.model = model;

		mainPanel = new JPanel();
		double size[][] = { { TableLayoutConstants.FILL, 250 },
				{ TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 30 } };
		mainPanel.setLayout(new TableLayout(size));
		mainPanel.setOpaque(false);

		DefaultListModel<String> activities = new DefaultListModel<String>();
		int[] selectedIndices = new int[model.getActivities().size()];
		int i = 0;
		for (String activity : model.getActivities()) {
			activities.addElement(activity);
			selectedIndices[i] = i;
			i++;
		}
		final ProMList<String> activityList = new ProMList<String>("View Activities", activities);
		activityList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		activityList.setSelectedIndices(selectedIndices);
		parameters.getActivities().addAll(model.getActivities());
		activityList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selectedActivities = activityList.getSelectedValuesList();
				if (!selectedActivities.equals(parameters.getActivities())) {
//					System.out.println("[PDC2017VisualizerPlugin] Selelected nodes = " + selectedActivities);
					parameters.getActivities().clear();
					parameters.getActivities().addAll(selectedActivities);
					updateRight();
				}
			}
		});
		activityList.setPreferredSize(new Dimension(100, 100));
		mainPanel.add(activityList, "1, 0, 1, 1");

		List<LogSkeletonBrowser> list = Arrays.asList(LogSkeletonBrowser.values());
		DefaultListModel<LogSkeletonBrowser> visualizers = new DefaultListModel<LogSkeletonBrowser>();
		for (LogSkeletonBrowser visualizer : list) {
			visualizers.addElement(visualizer);
		}
		final ProMList<LogSkeletonBrowser> visualizerList = new ProMList<LogSkeletonBrowser>("View Constraints", visualizers);
		visualizerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		List<LogSkeletonBrowser> selectedVisualizers = new ArrayList<LogSkeletonBrowser>();
		selectedIndices = new int[2];
//		selectedVisualizers.add(LogSkeletonBrowser.ALWAYSTOGETHER);
//		selectedIndices[0] = list.indexOf(LogSkeletonBrowser.ALWAYSTOGETHER);
		selectedVisualizers.add(LogSkeletonBrowser.ALWAYSBEFORE);
		selectedIndices[0] = list.indexOf(LogSkeletonBrowser.ALWAYSBEFORE);
		selectedVisualizers.add(LogSkeletonBrowser.ALWAYSAFTER);
		selectedIndices[1] = list.indexOf(LogSkeletonBrowser.ALWAYSAFTER);
		visualizerList.setSelectedIndices(selectedIndices);
		parameters.getVisualizers().addAll(selectedVisualizers);
		visualizerList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<LogSkeletonBrowser> selectedVisualizers = visualizerList.getSelectedValuesList();
				if (!selectedVisualizers.equals(parameters.getVisualizers())) {
//					System.out.println("[PDC2017VisualizerPlugin] Selelected edges = " + selectedVisualizers);
					parameters.getVisualizers().clear();
					parameters.getVisualizers().addAll(selectedVisualizers);
					updateRight();
				}
			}
		});
		visualizerList.setPreferredSize(new Dimension(100, 100));
		mainPanel.add(visualizerList, "1, 2");

		final SlickerButton button = new SlickerButton("View Log Skeleton in New Window");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateLeft();
			}
			
		});
		mainPanel.add(button, "1, 3");
		
//		updateLeft();
		updateRight();
		return mainPanel;
	}

	private void updateLeft() {
//		if (leftDotPanel != null) {
//			mainPanel.remove(leftDotPanel);
//		}
		leftDotPanel = new DotPanel(model.createGraph(parameters));
//		mainPanel.add(leftDotPanel, "0, 0, 0, 3");
//		mainPanel.validate();
//		mainPanel.repaint();
		JFrame frame = new JFrame();
		frame.add(leftDotPanel);
		frame.setTitle("Log Skeleton Viewer " + parameters.getVisualizers());
		frame.setSize(1024, 768);
		frame.setVisible(true);
	}

	private void updateRight() {
		if (rightDotPanel != null) {
			mainPanel.remove(rightDotPanel);
		}
		rightDotPanel = new DotPanel(model.createGraph(parameters));
		mainPanel.add(rightDotPanel, "0, 0, 0, 3");
		mainPanel.validate();
		mainPanel.repaint();

	}
}