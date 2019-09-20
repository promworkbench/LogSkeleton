package org.processmining.logskeleton.algorithms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import org.processmining.contexts.util.CompositePanel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.logskeleton.configurations.BrowserConfiguration;
import org.processmining.logskeleton.inputs.BrowserInput;
import org.processmining.logskeleton.models.LogSkeleton;
import org.processmining.logskeleton.models.LogSkeletonRelation;
import org.processmining.logskeleton.outputs.BrowserOutput;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.components.SlickerButton;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class BrowserAlgorithm {

	private LogSkeleton model;
	private BrowserConfiguration configuration;
	private JComponent leftDotPanel = null;
	private JComponent rightDotPanel = null;
	private JPanel mainPanel = null;
	private boolean isRepainting = false;

	public BrowserOutput apply(PluginContext context, BrowserInput input, final BrowserConfiguration configuration) {

		this.model = input.getLogSkeleton();
		this.configuration = configuration;

		mainPanel = new CompositePanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2184897562083625426L;

			public JComponent getMainComponent() {
				if (rightDotPanel instanceof CompositePanel) {
					return ((CompositePanel) rightDotPanel).getMainComponent();
				}
				return rightDotPanel;
			}
		};
		double size[][] = { { TableLayoutConstants.FILL, 250 }, { TableLayoutConstants.FILL, TableLayoutConstants.FILL,
				TableLayoutConstants.FILL, 30, 30, 30, 30, 30, 30, 30, 30 } };
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
		configuration.getActivities().addAll(model.getActivities());
		activityList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				Set<String> selectedActivities = new HashSet<String>(activityList.getSelectedValuesList());
				if (!selectedActivities.equals(configuration.getActivities())) {
					System.out.println("[LogSkeletonBrowserPlugin] Selected nodes = " + selectedActivities);
					configuration.getActivities().clear();
					configuration.getActivities().addAll(selectedActivities);
					updateRight();
				}
			}
		});
		activityList.setPreferredSize(new Dimension(100, 100));
		mainPanel.add(activityList, "1, 0, 1, 1");

		boolean doNotUseNotCoExistence = model.hasManyNotCoExistenceArcs(configuration);

		List<LogSkeletonRelation> list = Arrays.asList(LogSkeletonRelation.values());
		DefaultListModel<LogSkeletonRelation> visualizers = new DefaultListModel<LogSkeletonRelation>();
		for (LogSkeletonRelation visualizer : list) {
			visualizers.addElement(visualizer);
		}
		final ProMList<LogSkeletonRelation> visualizerList = new ProMList<LogSkeletonRelation>("View Constraints",
				visualizers);
		visualizerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		List<LogSkeletonRelation> selectedVisualizers = new ArrayList<LogSkeletonRelation>();
		selectedIndices = new int[doNotUseNotCoExistence ? 4 : 5];
		//		selectedVisualizers.add(LogSkeletonBrowser.ALWAYSTOGETHER);
		//		selectedIndices[0] = list.indexOf(LogSkeletonBrowser.ALWAYSTOGETHER);
		selectedVisualizers.add(LogSkeletonRelation.ALWAYSBEFORE);
		selectedIndices[0] = list.indexOf(LogSkeletonRelation.ALWAYSBEFORE);
		selectedVisualizers.add(LogSkeletonRelation.ALWAYSAFTER);
		selectedIndices[1] = list.indexOf(LogSkeletonRelation.ALWAYSAFTER);
		selectedVisualizers.add(LogSkeletonRelation.NEVERBEFORE);
		selectedIndices[2] = list.indexOf(LogSkeletonRelation.NEVERBEFORE);
		selectedVisualizers.add(LogSkeletonRelation.NEVERAFTER);
		selectedIndices[3] = list.indexOf(LogSkeletonRelation.NEVERAFTER);
		if (!doNotUseNotCoExistence) {
			/*
			 * Only include in the first visualization if not too many Not
			 * Co-Existence constraints.
			 */
			selectedVisualizers.add(LogSkeletonRelation.NEVERTOGETHER);
			selectedIndices[4] = list.indexOf(LogSkeletonRelation.NEVERTOGETHER);
		}
		visualizerList.setSelectedIndices(selectedIndices);
		configuration.getRelations().addAll(selectedVisualizers);
		visualizerList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<LogSkeletonRelation> selectedVisualizers = visualizerList.getSelectedValuesList();
				if (!selectedVisualizers.equals(configuration.getRelations())) {
					System.out.println("[LogSkeletonBrowserPlugin] Selected edges = " + selectedVisualizers);
					configuration.getRelations().clear();
					configuration.getRelations().addAll(selectedVisualizers);
					updateRight();
				}
			}
		});
		visualizerList.setPreferredSize(new Dimension(100, 100));
		mainPanel.add(visualizerList, "1, 2");

		final JCheckBox checkBox = SlickerFactory.instance().createCheckBox("Use Hyper Arcs (may be slow...)", false);
		checkBox.setSelected(configuration.isUseHyperArcs());
		checkBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseHyperArcs(checkBox.isSelected());
				updateRight();
			}

		});
		checkBox.setOpaque(false);
		checkBox.setPreferredSize(new Dimension(100, 30));
		mainPanel.add(checkBox, "1, 4");

		final JCheckBox checkBoxFalseConstraints = SlickerFactory.instance().createCheckBox("Use False Constraints",
				false);
		checkBoxFalseConstraints.setSelected(configuration.isUseFalseConstraints());
		checkBoxFalseConstraints.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseFalseConstraints(checkBoxFalseConstraints.isSelected());
				updateRight();
			}

		});
		checkBoxFalseConstraints.setOpaque(false);
		checkBoxFalseConstraints.setPreferredSize(new Dimension(100, 30));
		mainPanel.add(checkBoxFalseConstraints, "1, 5");

		final JCheckBox checkBoxEdgeColors = SlickerFactory.instance().createCheckBox("Use Edge Colors", false);
		checkBoxEdgeColors.setSelected(configuration.isUseEdgeColors());
		checkBoxEdgeColors.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseEdgeColors(checkBoxEdgeColors.isSelected());
				updateRight();
			}

		});
		checkBoxEdgeColors.setOpaque(false);
		checkBoxEdgeColors.setPreferredSize(new Dimension(100, 30));
		mainPanel.add(checkBoxEdgeColors, "1, 6");

		final JCheckBox checkBoxEquivalenceClass = SlickerFactory.instance().createCheckBox("Use Equivalence Class",
				false);
		checkBoxEquivalenceClass.setSelected(configuration.isUseEquivalenceClass());
		checkBoxEquivalenceClass.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseEquivalenceClass(checkBoxEquivalenceClass.isSelected());
				updateRight();
			}

		});
		checkBoxEquivalenceClass.setOpaque(false);
		checkBoxEquivalenceClass.setPreferredSize(new Dimension(100, 30));
		mainPanel.add(checkBoxEquivalenceClass, "1, 7");

		final JCheckBox checkBoxLabels = SlickerFactory.instance().createCheckBox("Use Head/Tail Labels", false);
		checkBoxLabels.setSelected(configuration.isUseHeadTailLabels());
		checkBoxLabels.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseHeadTailLabels(checkBoxLabels.isSelected());
				updateRight();
			}

		});
		checkBoxLabels.setOpaque(false);
		checkBoxLabels.setPreferredSize(new Dimension(100, 30));
		mainPanel.add(checkBoxLabels, "1, 8");

		final JCheckBox checkBoxNeighbors = SlickerFactory.instance().createCheckBox("Show Neighbors", false);
		checkBoxNeighbors.setSelected(configuration.isUseNeighbors());
		checkBoxNeighbors.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseNeighbors(checkBoxNeighbors.isSelected());
				updateRight();
			}

		});
		checkBoxNeighbors.setOpaque(false);
		checkBoxNeighbors.setPreferredSize(new Dimension(100, 30));
		mainPanel.add(checkBoxNeighbors, "1, 9");

		final NiceSlider noiseLevelSlider = SlickerFactory.instance().createNiceIntegerSlider("Noise Level in %", 0, 20,
				100 - configuration.getPrecedenceThreshold(), Orientation.HORIZONTAL);
		noiseLevelSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = 100 - noiseLevelSlider.getSlider().getValue();
				model.setEquivalenceThreshold(value);
				configuration.setPrecedenceThreshold(value);
				configuration.setResponseThreshold(value);
				model.setPrecedenceThreshold(value);
				model.setResponseThreshold(value);
				configuration.setNotCoExistenceThreshold(value);
				model.setNotCoExistenceThreshold(value);
				model.cleanPrePost();
				updateRight();
			}
		});
		noiseLevelSlider.setPreferredSize(new Dimension(100, 30));
		mainPanel.add(noiseLevelSlider, "1, 3");

		//		final NiceSlider notCoExistenceThresholdSlider = SlickerFactory.instance().createNiceIntegerSlider(
		//				"NCE Threshold", 80, 100, parameters.getPrecedenceThreshold(), Orientation.HORIZONTAL);
		//		notCoExistenceThresholdSlider.addChangeListener(new ChangeListener() {
		//
		//			public void stateChanged(ChangeEvent e) {
		//				parameters.setNotCoExistenceThreshold(notCoExistenceThresholdSlider.getSlider().getValue());
		//				model.setNotCoExistenceThreshold(notCoExistenceThresholdSlider.getSlider().getValue());
		//				updateRight();
		//			}
		//		});
		//		notCoExistenceThresholdSlider.setPreferredSize(new Dimension(100, 30));
		//		mainPanel.add(notCoExistenceThresholdSlider, "1, 10");
		//
		final SlickerButton button = new SlickerButton("View Log Skeleton in New Window");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateLeft();
			}

		});
		mainPanel.add(button, "1, 10");

		//		updateLeft();
		updateRight();
		return new BrowserOutput(mainPanel);
	}

	private void updateLeft() {
		//		if (leftDotPanel != null) {
		//			mainPanel.remove(leftDotPanel);
		//		}
		model.setPrecedenceThreshold(configuration.getPrecedenceThreshold());
		model.setResponseThreshold(configuration.getResponseThreshold());
		model.setNotCoExistenceThreshold(configuration.getNotCoExistenceThreshold());
		leftDotPanel = new DotPanel(model.createGraph(configuration));
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
			rightDotPanel = null;
		}
		System.out.println("[LogSkeletonBrowser] Updating Dot panel...");
		rightDotPanel = new DotPanel(model.visualize(configuration)) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -1188986522911680121L;

			public void repaint() {
				// Prevent nesting of repaints, as resetView may trigger a new repaint.
				if (!isRepainting) {
					isRepainting = true;
					System.out.println("[LogSkeletonBrowser] Repainting Dot panel...");
					// Make sure all initialization has been done.
					if (rightDotPanel != null) {
						try {
							// Without this, the dot panel disappears in the Filterd visualizer 
							// when a drop-down menu is selected (?)
							((DotPanel) rightDotPanel).resetView();
						} catch (NoninvertibleTransformException e) {
//							e.printStackTrace();
						}
					}
					super.repaint();
					isRepainting = false;
					System.out.println("[LogSkeletonBrowser] Repainted Dot panel...");
				}
			}
		};
		rightDotPanel.setOpaque(true);
		rightDotPanel.setBackground(Color.white);
		mainPanel.add(rightDotPanel, "0, 0, 0, 10");
		mainPanel.validate();
		mainPanel.repaint();

	}
}
