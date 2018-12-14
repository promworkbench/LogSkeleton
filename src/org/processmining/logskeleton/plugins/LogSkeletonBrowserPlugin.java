package org.processmining.logskeleton.plugins;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.components.SlickerButton;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

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
				{ TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 30, 30, 30, 30, 30, 30, 30 } };
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
					System.out.println("[LogSkeletonBrowserPlugin] Selected nodes = " + selectedActivities);
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
		selectedIndices = new int[3];
//		selectedVisualizers.add(LogSkeletonBrowser.ALWAYSTOGETHER);
//		selectedIndices[0] = list.indexOf(LogSkeletonBrowser.ALWAYSTOGETHER);
		selectedVisualizers.add(LogSkeletonBrowser.ALWAYSBEFORE);
		selectedIndices[0] = list.indexOf(LogSkeletonBrowser.ALWAYSBEFORE);
		selectedVisualizers.add(LogSkeletonBrowser.ALWAYSAFTER);
		selectedIndices[1] = list.indexOf(LogSkeletonBrowser.ALWAYSAFTER);
		selectedVisualizers.add(LogSkeletonBrowser.NEVERTOGETHER);
		selectedIndices[2] = list.indexOf(LogSkeletonBrowser.NEVERTOGETHER);
		visualizerList.setSelectedIndices(selectedIndices);
		parameters.getVisualizers().addAll(selectedVisualizers);
		visualizerList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<LogSkeletonBrowser> selectedVisualizers = visualizerList.getSelectedValuesList();
				if (!selectedVisualizers.equals(parameters.getVisualizers())) {
					System.out.println("[LogSkeletonBrowserPlugin] Selected edges = " + selectedVisualizers);
					parameters.getVisualizers().clear();
					parameters.getVisualizers().addAll(selectedVisualizers);
					updateRight();
				}
			}
		});
		visualizerList.setPreferredSize(new Dimension(100, 100));
		mainPanel.add(visualizerList, "1, 2");

		final JCheckBox checkBox = SlickerFactory.instance().createCheckBox("Use Hyper Arcs (may be slow...)", false);
		checkBox.setSelected(parameters.isUseHyperArcs());
		checkBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setUseHyperArcs(checkBox.isSelected());
				updateRight();
			}

		});
		checkBox.setOpaque(false);
		checkBox.setPreferredSize(new Dimension(100, 30));
		mainPanel.add(checkBox, "1, 3");

		final JCheckBox checkBoxFalseConstraints = SlickerFactory.instance().createCheckBox("Use False Constraints", false);
		checkBoxFalseConstraints.setSelected(parameters.isUseFalseConstraints());
		checkBoxFalseConstraints.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setUseFalseConstraints(checkBoxFalseConstraints.isSelected());
				updateRight();
			}

		});
		checkBoxFalseConstraints.setOpaque(false);
		checkBoxFalseConstraints.setPreferredSize(new Dimension(100, 30));
		mainPanel.add(checkBoxFalseConstraints, "1, 4");

		final JCheckBox checkBoxEdgeColors = SlickerFactory.instance().createCheckBox("Use Edge Colors", false);
		checkBoxEdgeColors.setSelected(parameters.isUseEdgeColors());
		checkBoxEdgeColors.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setUseEdgeColors(checkBoxEdgeColors.isSelected());
				updateRight();
			}

		});
		checkBoxEdgeColors.setOpaque(false);
		checkBoxEdgeColors.setPreferredSize(new Dimension(100, 30));
		mainPanel.add(checkBoxEdgeColors, "1, 5");

		final JCheckBox checkBoxEquivalenceClass = SlickerFactory.instance().createCheckBox("Use Equivalence Class", false);
		checkBoxEquivalenceClass.setSelected(parameters.isUseEquivalenceClass());
		checkBoxEquivalenceClass.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setUseEquivalenceClass(checkBoxEquivalenceClass.isSelected());
				updateRight();
			}

		});
		checkBoxEquivalenceClass.setOpaque(false);
		checkBoxEquivalenceClass.setPreferredSize(new Dimension(100, 30));
		mainPanel.add(checkBoxEquivalenceClass, "1, 6");

		final JCheckBox checkBoxNeighbors = SlickerFactory.instance().createCheckBox("Show Neighbors", false);
		checkBoxNeighbors.setSelected(parameters.isUseNeighbors());
		checkBoxNeighbors.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				parameters.setUseNeighbors(checkBoxNeighbors.isSelected());
				updateRight();
			}

		});
		checkBoxNeighbors.setOpaque(false);
		checkBoxNeighbors.setPreferredSize(new Dimension(100, 30));
		mainPanel.add(checkBoxNeighbors, "1, 7");

		final NiceSlider thresholdSlider = SlickerFactory.instance().createNiceIntegerSlider("R/P Percentage", 80, 100,
				parameters.getThreshold(), Orientation.HORIZONTAL);
		thresholdSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				parameters.setThreshold(thresholdSlider.getSlider().getValue());
				updateRight();
			}
		});
		thresholdSlider.setPreferredSize(new Dimension(100,30));
		mainPanel.add(thresholdSlider, "1, 8");


		final SlickerButton button = new SlickerButton("View Log Skeleton in New Window");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateLeft();
			}
			
		});
		mainPanel.add(button, "1, 9");
		
//		updateLeft();
		updateRight();
		return mainPanel;
	}

	private void updateLeft() {
//		if (leftDotPanel != null) {
//			mainPanel.remove(leftDotPanel);
//		}
		model.setThreshold(parameters.getThreshold());
		leftDotPanel = new DotPanel(model.createGraph(parameters));
//		mainPanel.add(leftDotPanel, "0, 0, 0, 3");
//		mainPanel.validate();
//		mainPanel.repaint();
		JFrame frame = new JFrame();
		frame.add(leftDotPanel);
		frame.setTitle("Log Skeleton Viewer on " + model.getLabel());
		frame.setSize(1024, 768);
		frame.setVisible(true);
	}

	private void updateRight() {
		if (rightDotPanel != null) {
			mainPanel.remove(rightDotPanel);
		}
		rightDotPanel = new DotPanel(model.visualize(parameters));
		mainPanel.add(rightDotPanel, "0, 0, 0, 9");
		mainPanel.validate();
		mainPanel.repaint();

	}
}