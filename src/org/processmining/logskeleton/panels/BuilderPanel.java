package org.processmining.logskeleton.panels;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.log.dialogs.ClassifierPanel;
import org.processmining.log.parameters.UpdateParameter;
import org.processmining.logskeleton.configurations.BuilderConfiguration;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class BuilderPanel extends JPanel implements UpdateParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1040026869625729765L;
	
	private XLog log = null;
	private BuilderConfiguration configuration = null;
	
	private ProMList<String> boundaryActivityList = null;

	private List<String> getActivities() {
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

	public BuilderPanel(XLog log, final BuilderConfiguration configuration) {
		this.log = log;
		this.configuration = configuration;
		
		double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30 } };
		setLayout(new TableLayout(size));
		add(new ClassifierPanel(log.getClassifiers(), configuration, this), "0, 0");

		update();

		final NiceSlider horizonSlider = SlickerFactory.instance().createNiceIntegerSlider(
				"Horizon (0 means no horizon)", 0, 20, configuration.getHorizon(), Orientation.HORIZONTAL);
		horizonSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				int value = horizonSlider.getSlider().getValue();
				configuration.setHorizon(value);
			}
		});
		horizonSlider.setPreferredSize(new Dimension(100, 30));
		add(horizonSlider, "0, 1, 1, 1");
	}
	
	public void update() {
		if (log == null || configuration == null) {
			return;
		}
		
		final Set<String> boundaryActivities = configuration.getBoundaryActivities();
		List<String> activities = getActivities();
		DefaultListModel<String> boundaryActivityModel = new DefaultListModel<String>();
		for (String activity : activities) {
			boundaryActivityModel.addElement(activity);
		}
		if (boundaryActivityList != null) {
			remove(boundaryActivityList);
		}
		boundaryActivityList = new ProMList<String>("Select boundary activities",
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
		add(boundaryActivityList, "1, 0");
		
		revalidate();
		repaint();
	}
}
