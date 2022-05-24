package org.processmining.logskeleton.algorithms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.logskeleton.components.BrowserComponent;
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

	private LogSkeleton logSkeleton;
	private BrowserConfiguration configuration;
	private JComponent leftDotPanel = null;
	private JComponent rightDotPanel = null;
	private BrowserComponent mainPanel = null;
	private boolean isRepainting = false;

	public BrowserOutput apply(PluginContext context, BrowserInput input, final BrowserConfiguration configuration) {

		this.logSkeleton = input.getLogSkeleton();
		this.configuration = configuration;

		mainPanel = new BrowserComponent() {
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
		double mainSize[][] = { { TableLayoutConstants.FILL, 250 }, { TableLayoutConstants.FILL, 30 } };
		mainPanel.setLayout(new TableLayout(mainSize));
		mainPanel.setOpaque(false);

		//		JTabbedPane tabbedPane = new JTabbedPane();
		final JPanel controlPanel = new JPanel();
		double controlSize[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL, 30 } };
		controlPanel.setLayout(new TableLayout(controlSize));
		controlPanel.setOpaque(false);

		final JPanel basicPanel = new JPanel();
		double basicSize[][] = { { 250 },
				{ TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL } };
		basicPanel.setLayout(new TableLayout(basicSize));
		basicPanel.setOpaque(false);

		DefaultListModel<String> activityModel = new DefaultListModel<String>();
		int[] selectedIndices = new int[configuration.getActivities().size()];
		int i = 0;
		int j = 0;
		for (String activity : logSkeleton.getActivities()) {
			activityModel.addElement(activity);
			if (configuration.getActivities().contains(activity)) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<String> activityList = new ProMList<String>("Select activities to show", activityModel);
		activityList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		activityList.setSelectedIndices(selectedIndices);
		activityList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				Set<String> selectedActivities = new HashSet<String>(activityList.getSelectedValuesList());
				if (!selectedActivities.equals(configuration.getActivities())) {
					System.out.println("[BrowserAlgorithm] Selected activities = " + selectedActivities);
					configuration.getActivities().clear();
					configuration.getActivities().addAll(selectedActivities);
					updateRight();
				}
			}
		});
		activityList.setPreferredSize(new Dimension(100, 100));
		basicPanel.add(activityList, "0, 0, 0, 1");

		List<LogSkeletonRelation> relations = Arrays.asList(LogSkeletonRelation.values());
		DefaultListModel<LogSkeletonRelation> relationModel = new DefaultListModel<LogSkeletonRelation>();
		selectedIndices = new int[configuration.getRelations().size()];
		i = 0;
		j = 0;
		for (LogSkeletonRelation relation : relations) {
			relationModel.addElement(relation);
			if (configuration.getRelations().contains(relation)) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<LogSkeletonRelation> relationList = new ProMList<LogSkeletonRelation>("Select relations to show",
				relationModel);
		relationList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		relationList.setSelectedIndices(selectedIndices);
		relationList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<LogSkeletonRelation> selectedRelations = relationList.getSelectedValuesList();
				if (!selectedRelations.equals(configuration.getRelations())) {
					System.out.println("[BrowserAlgorithm] Selected relations = " + selectedRelations);
					configuration.getRelations().clear();
					configuration.getRelations().addAll(selectedRelations);
					updateRight();
				}
			}
		});
		relationList.setPreferredSize(new Dimension(100, 100));
		basicPanel.add(relationList, "0, 2");

		//		tabbedPane.add("Basic options", basicPanel);

		final JPanel advancedPanel = new JPanel();
		double[][] advancedSize = { { 30, TableLayoutConstants.FILL },
				{ 40, 30, 30, 30, 30, 40, 40, 40, 40, 40, 40, 40, 40, 40, 30, 40, 30, 30 } };
		advancedPanel.setLayout(new TableLayout(advancedSize));
		advancedPanel.setOpaque(false);
		int y = 0;

		advancedPanel.add(new JLabel("Select noise levels in %:"), "0, " + y + ", 1, " + y);
		y++;

		final NiceSlider equivalenceSlider = SlickerFactory.instance().createNiceIntegerSlider("Equivalence", 0, 20,
				100 - configuration.getEquivalenceThreshold(), Orientation.HORIZONTAL);
		equivalenceSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = 100 - equivalenceSlider.getSlider().getValue();
				logSkeleton.setEquivalenceThreshold(value);
				configuration.setEquivalenceThreshold(value);
				logSkeleton.cleanPrePost();
				updateRight();
			}
		});
		equivalenceSlider.setPreferredSize(new Dimension(100, 30));
		advancedPanel.add(equivalenceSlider, "0, " + y + ", 1, " + y);
		y++;

		final NiceSlider responseSlider = SlickerFactory.instance().createNiceIntegerSlider("(Not) Response", 0, 20,
				100 - configuration.getResponseThreshold(), Orientation.HORIZONTAL);
		responseSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = 100 - responseSlider.getSlider().getValue();
				logSkeleton.setResponseThreshold(value);
				configuration.setResponseThreshold(value);
				logSkeleton.cleanPrePost();
				updateRight();
			}
		});
		responseSlider.setPreferredSize(new Dimension(100, 30));
		advancedPanel.add(responseSlider, "0, " + y + ", 1, " + y);
		y++;

		final NiceSlider precedenceSlider = SlickerFactory.instance().createNiceIntegerSlider("(Not) Precedence", 0, 20,
				100 - configuration.getPrecedenceThreshold(), Orientation.HORIZONTAL);
		precedenceSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = 100 - precedenceSlider.getSlider().getValue();
				logSkeleton.setPrecedenceThreshold(value);
				configuration.setPrecedenceThreshold(value);
				logSkeleton.cleanPrePost();
				updateRight();
			}
		});
		precedenceSlider.setPreferredSize(new Dimension(100, 30));
		advancedPanel.add(precedenceSlider, "0, " + y + ", 1, " + y);
		y++;

		final NiceSlider notCoExistenceSlider = SlickerFactory.instance().createNiceIntegerSlider("Not Co-Existence", 0,
				20, 100 - configuration.getNotCoExistenceThreshold(), Orientation.HORIZONTAL);
		notCoExistenceSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = 100 - notCoExistenceSlider.getSlider().getValue();
				logSkeleton.setNotCoExistenceThreshold(value);
				configuration.setNotCoExistenceThreshold(value);
				logSkeleton.cleanPrePost();
				updateRight();
			}
		});
		notCoExistenceSlider.setPreferredSize(new Dimension(100, 30));
		advancedPanel.add(notCoExistenceSlider, "0, " + y + ", 1, " + y);
		y++;
		y++;

		final JCheckBox checkBox = SlickerFactory.instance().createCheckBox("", configuration.isUseHyperArcs());
		checkBox.setSelected(configuration.isUseHyperArcs());
		checkBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseHyperArcs(checkBox.isSelected());
				updateRight();
			}

		});
		checkBox.setOpaque(false);
		checkBox.setPreferredSize(new Dimension(100, 30));
		advancedPanel.add(checkBox, "0, " + y);
		advancedPanel.add(new JLabel("<html>Replace a clique of similar arcs<br>by a hyper arc (may be slow...)"),
				"1, " + y);
		y++;

		final JCheckBox checkBoxFalseConstraints = SlickerFactory.instance().createCheckBox("",
				configuration.isUseFalseConstraints());
		checkBoxFalseConstraints.setSelected(configuration.isUseFalseConstraints());
		checkBoxFalseConstraints.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseFalseConstraints(checkBoxFalseConstraints.isSelected());
				updateRight();
			}

		});
		checkBoxFalseConstraints.setOpaque(false);
		checkBoxFalseConstraints.setPreferredSize(new Dimension(100, 30));
		advancedPanel.add(checkBoxFalseConstraints, "0, " + y);
		advancedPanel.add(new JLabel("<html>Ignore symmetric relations<br>when layering activities"), "1, " + y);
		y++;

		final JCheckBox checkBoxEdgeColors = SlickerFactory.instance().createCheckBox("",
				configuration.isUseEdgeColors());
		checkBoxEdgeColors.setSelected(configuration.isUseEdgeColors());
		checkBoxEdgeColors.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseEdgeColors(checkBoxEdgeColors.isSelected());
				updateRight();
			}

		});
		checkBoxEdgeColors.setOpaque(false);
		checkBoxEdgeColors.setPreferredSize(new Dimension(100, 30));
		advancedPanel.add(checkBoxEdgeColors, "0, " + y);
		advancedPanel.add(new JLabel("<html>Show relation colors"), "1, " + y);
		y++;

		final JCheckBox checkBoxInvertedArrows = SlickerFactory.instance().createCheckBox("",
				configuration.isUseInvertedArrows());
		checkBoxInvertedArrows.setSelected(configuration.isUseInvertedArrows());
		checkBoxInvertedArrows.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseInvertedArrows(checkBoxInvertedArrows.isSelected());
				updateRight();
			}

		});
		checkBoxInvertedArrows.setOpaque(false);
		checkBoxInvertedArrows.setPreferredSize(new Dimension(100, 30));
		advancedPanel.add(checkBoxInvertedArrows, "0, " + y);
		advancedPanel.add(new JLabel("<html>Show inverted arrows for Not Response and Not Precedence"), "1, " + y);
		y++;

		final JCheckBox checkBoxEquivalenceClass = SlickerFactory.instance().createCheckBox("",
				configuration.isUseEquivalenceClass());
		checkBoxEquivalenceClass.setSelected(configuration.isUseEquivalenceClass());
		checkBoxEquivalenceClass.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseEquivalenceClass(checkBoxEquivalenceClass.isSelected());
				updateRight();
			}

		});
		checkBoxEquivalenceClass.setOpaque(false);
		checkBoxEquivalenceClass.setPreferredSize(new Dimension(100, 30));
		advancedPanel.add(checkBoxEquivalenceClass, "0, " + y);
		advancedPanel.add(new JLabel("<html>Show Not Co-Existence<br>between representatives"), "1, " + y);
		y++;

		final JCheckBox checkBoxNCEReductions = SlickerFactory.instance().createCheckBox("",
				configuration.isUseNCEReductions());
		checkBoxNCEReductions.setSelected(configuration.isUseNCEReductions());
		checkBoxNCEReductions.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseNCEReductions(checkBoxNCEReductions.isSelected());
				updateRight();
			}

		});
		checkBoxNCEReductions.setOpaque(false);
		checkBoxNCEReductions.setPreferredSize(new Dimension(100, 30));
		advancedPanel.add(checkBoxNCEReductions, "0, " + y);
		advancedPanel.add(new JLabel("<html>Show Not Co-Existence<br>if no precedent"), "1, " + y);
		y++;

		final JCheckBox checkBoxLabels = SlickerFactory.instance().createCheckBox("",
				configuration.isUseHeadTailLabels());
		checkBoxLabels.setSelected(configuration.isUseHeadTailLabels());
		checkBoxLabels.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseHeadTailLabels(checkBoxLabels.isSelected());
				updateRight();
			}

		});
		checkBoxLabels.setOpaque(false);
		checkBoxLabels.setPreferredSize(new Dimension(100, 30));
		advancedPanel.add(checkBoxLabels, "0, " + y);
		advancedPanel.add(new JLabel("<html>Replace head/tail labels<br>by arc labels"), "1, " + y);
		y++;

		final JCheckBox checkBoxNeighbors = SlickerFactory.instance().createCheckBox("",
				configuration.isUseNeighbors());
		checkBoxNeighbors.setSelected(configuration.isUseNeighbors());
		checkBoxNeighbors.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setUseNeighbors(checkBoxNeighbors.isSelected());
				updateRight();
			}

		});
		checkBoxNeighbors.setOpaque(false);
		checkBoxNeighbors.setPreferredSize(new Dimension(100, 30));
		advancedPanel.add(checkBoxNeighbors, "0, " + y);
		advancedPanel.add(new JLabel("<html>Show related (but unselected)<br>activities as well"), "1, " + y);
		y++;
		y++;

		/*
		 * Add test fields to be able to change font (and representation).
		 */
		advancedPanel.add(new JLabel("Enter fontname (and representation):"), "0, " + y + ", 1, " + y);
		y++;

		final ProMTextField fontnameField = new ProMTextField(configuration.getFontname(), "Fontname (leave blank for default)");
		fontnameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				configuration.setFontname(fontnameField.getText());
				updateRight();
			}
		});
		advancedPanel.add(fontnameField, "0, " + y + ", 1, " + y);
		y++;

		final ProMTextField fontnameRepresentationField = new ProMTextField(configuration.getFontnameRepresentation(), "Representation (leave blank for default)");
		fontnameRepresentationField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				configuration.setFontnameRepresentation(fontnameRepresentationField.getText());
				updateRight();
			}
		});
		advancedPanel.add(fontnameRepresentationField, "0, " + y + ", 1, " + y);
		y++;

		final SlickerButton basicButton = new SlickerButton("Basic options");
		final SlickerButton advancedButton = new SlickerButton("Advanced options");

		basicButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controlPanel.remove(advancedPanel);
				controlPanel.add(basicPanel, "0, 0, 1, 0");
				advancedButton.setEnabled(true);
				basicButton.setEnabled(false);
				controlPanel.validate();
				controlPanel.repaint();
			}

		});
		advancedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controlPanel.remove(basicPanel);
				controlPanel.add(advancedPanel, "0, 0, 1, 0");
				advancedButton.setEnabled(false);
				basicButton.setEnabled(true);
				controlPanel.validate();
				controlPanel.repaint();
			}

		});

		controlPanel.add(basicButton, "0, 1");
		controlPanel.add(advancedButton, "1, 1");

		controlPanel.add(basicPanel, "0, 0, 1, 0");
		advancedButton.setEnabled(true);
		basicButton.setEnabled(false);

		//		tabbedPane.add("Advanced options", optionsPanel);

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
		mainPanel.add(newButton, "0, 1");

		//		final SlickerButton optionsButton = new SlickerButton("Select options...");
		//		optionsButton.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent e) {
		//				JFrame frame = new JFrame();
		//				frame.setIconImage(ImageLoader.load("rotule_30x35.png"));
		//				frame.add(optionsPanel);
		//				frame.setTitle("Select options for " + model.getLabel());
		//				frame.setSize(460, 280);
		//				frame.setVisible(true);
		//			}
		//		});

		mainPanel.add(controlPanel, "1, 0, 1, 1");

		//		updateLeft();
		updateRight();
		return new BrowserOutput(mainPanel);
	}

	private void updateLeft() {
		//		if (leftDotPanel != null) {
		//			mainPanel.remove(leftDotPanel);
		//		}
		logSkeleton.setPrecedenceThreshold(configuration.getPrecedenceThreshold());
		logSkeleton.setResponseThreshold(configuration.getResponseThreshold());
		logSkeleton.setNotCoExistenceThreshold(configuration.getNotCoExistenceThreshold());
		leftDotPanel = new DotPanel(logSkeleton.createGraph(configuration));
		//		mainPanel.add(leftDotPanel, "0, 0, 0, 3");
		//		mainPanel.validate();
		//		mainPanel.repaint();
		JFrame frame = new JFrame();
		frame.setIconImage(ImageLoader.load("rotule_30x35.png"));
		frame.add(leftDotPanel);
		frame.setTitle("Log Skeleton viewer on " + logSkeleton.getLabel());
		frame.setSize(1024, 768);
		frame.setVisible(true);
	}

	private void updateRight() {
		if (rightDotPanel != null) {
			mainPanel.remove(rightDotPanel);
			rightDotPanel = null;
		}
		System.out.println("[LogSkeletonBrowser] Updating Dot panel...");
		rightDotPanel = new DotPanel(logSkeleton.visualize(configuration));
		rightDotPanel.setOpaque(true);
		rightDotPanel.setBackground(Color.white);
		mainPanel.add(rightDotPanel, "0, 0");
		mainPanel.validate();
		mainPanel.repaint();

	}
}
