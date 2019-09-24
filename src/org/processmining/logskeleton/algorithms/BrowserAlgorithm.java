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

import org.deckfour.uitopia.ui.util.ImageLoader;
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
import com.fluxicon.slickerbox.components.RoundedPanel;
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
	private JPanel optionsPanel = null;
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
				TableLayoutConstants.FILL, 30 } };
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
		final ProMList<String> activityList = new ProMList<String>("Select activities to show", activities);
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
		final ProMList<LogSkeletonRelation> visualizerList = new ProMList<LogSkeletonRelation>("Select relations to show",
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

		optionsPanel = new RoundedPanel();
		double[][] popupSize = { { TableLayoutConstants.FILL }, { 30, 30, 30, 30, 30, 30, 30 } };
		optionsPanel.setLayout(new TableLayout(popupSize));
		optionsPanel.setOpaque(false);
		
		final JCheckBox checkBox = SlickerFactory.instance().createCheckBox("Replace a clique of similar arcs by a hyper arc (may be slow...)", false);
		checkBox.setSelected(configuration.isUseHyperArcs());
		checkBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseHyperArcs(checkBox.isSelected());
				updateRight();
			}

		});
		checkBox.setOpaque(false);
		checkBox.setPreferredSize(new Dimension(100, 30));
		optionsPanel.add(checkBox, "0, 1");

		final JCheckBox checkBoxFalseConstraints = SlickerFactory.instance().createCheckBox("Ignore symmetric relations when layering activities",
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
		optionsPanel.add(checkBoxFalseConstraints, "0, 2");

		final JCheckBox checkBoxEdgeColors = SlickerFactory.instance().createCheckBox("Show relation colors", false);
		checkBoxEdgeColors.setSelected(configuration.isUseEdgeColors());
		checkBoxEdgeColors.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseEdgeColors(checkBoxEdgeColors.isSelected());
				updateRight();
			}

		});
		checkBoxEdgeColors.setOpaque(false);
		checkBoxEdgeColors.setPreferredSize(new Dimension(100, 30));
		optionsPanel.add(checkBoxEdgeColors, "0, 3");

		final JCheckBox checkBoxEquivalenceClass = SlickerFactory.instance().createCheckBox("Show Not Co-Existence only between representatives",
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
		optionsPanel.add(checkBoxEquivalenceClass, "0, 4");

		final JCheckBox checkBoxLabels = SlickerFactory.instance().createCheckBox("Replace head/tail labels by arc labels", false);
		checkBoxLabels.setSelected(configuration.isUseHeadTailLabels());
		checkBoxLabels.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseHeadTailLabels(checkBoxLabels.isSelected());
				updateRight();
			}

		});
		checkBoxLabels.setOpaque(false);
		checkBoxLabels.setPreferredSize(new Dimension(100, 30));
		optionsPanel.add(checkBoxLabels, "0, 5");

		final JCheckBox checkBoxNeighbors = SlickerFactory.instance().createCheckBox("Show related (but unselected) activities as well", false);
		checkBoxNeighbors.setSelected(configuration.isUseNeighbors());
		checkBoxNeighbors.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseNeighbors(checkBoxNeighbors.isSelected());
				updateRight();
			}

		});
		checkBoxNeighbors.setOpaque(false);
		checkBoxNeighbors.setPreferredSize(new Dimension(100, 30));
		optionsPanel.add(checkBoxNeighbors, "0, 6");

		final NiceSlider noiseLevelSlider = SlickerFactory.instance().createNiceIntegerSlider("Select noise level in %", 0, 20,
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
		optionsPanel.add(noiseLevelSlider, "0, 0");

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
		final SlickerButton newButton = new SlickerButton("View in new window...");
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateLeft();
			}

		});
		mainPanel.add(newButton, "0, 3");

		final SlickerButton optionsButton = new SlickerButton("Select options...");
		optionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame();
				frame.setIconImage(ImageLoader.load("rotule_30x35.png"));
				frame.add(optionsPanel);
				frame.setTitle("Select options for " + model.getLabel());
				frame.setSize(460, 280);
				frame.setVisible(true);
			}
		});
		
		mainPanel.add(optionsButton, "1, 3");

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
		frame.setIconImage(ImageLoader.load("rotule_30x35.png"));
		frame.add(leftDotPanel);
		frame.setTitle("Log Skeleton viewer on " + model.getLabel());
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
		mainPanel.add(rightDotPanel, "0, 0, 0, 2");
		mainPanel.validate();
		mainPanel.repaint();

	}
}
