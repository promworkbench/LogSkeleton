package org.processmining.logskeleton.pdc2017.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.logskeleton.algorithms.LogPreprocessorAlgorithm;
import org.processmining.logskeleton.pdc2017.parameters.PDC2017TestParameters;

public class PDC2017TestDialog extends JComponent {

	private PDC2017TestParameters parameters;
	
	public PDC2017TestDialog(final PDC2017TestParameters parameters) {
		this.parameters = parameters;
		
		double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		setOpaque(false);

		DefaultListModel<String> logNames = new DefaultListModel<String>();
		int[] selectedIndices = new int[parameters.getLogNames().size()];
		int i = 0;
		int j = 0;
		List<String> orderedLogNames = new ArrayList<String>(parameters.getAllLogNames());
		Collections.sort(orderedLogNames);
		for (String logName : orderedLogNames) {
			logNames.addElement(logName);
			if (parameters.getLogNames().contains(logName)) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<String> logNameList = new ProMList<String>("Select log names", logNames);
		logNameList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		logNameList.setSelectedIndices(selectedIndices);
		logNameList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selectedLogNames = logNameList.getSelectedValuesList();
				parameters.getLogNames().clear();
				parameters.getLogNames().addAll(selectedLogNames);
			}
		});
		logNameList.setPreferredSize(new Dimension(100, 100));
		add(logNameList, "0, 0");
		
		DefaultListModel<String> collectionNames = new DefaultListModel<String>();
		selectedIndices = new int[parameters.getCollectionNames().size()];
		i = 0;
		j = 0;
		List<String> orderedCollectionNames = new ArrayList<String>(parameters.getAllCollectionNames());
		Collections.sort(orderedCollectionNames);
		for (String collectionName : orderedCollectionNames) {
			collectionNames.addElement(collectionName);
			if (parameters.getCollectionNames().contains(collectionName)) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<String> collectionNameList = new ProMList<String>("Select collections", collectionNames);
		collectionNameList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		collectionNameList.setSelectedIndices(selectedIndices);
		collectionNameList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selectedCollectionNames = collectionNameList.getSelectedValuesList();
				parameters.getCollectionNames().clear();
				parameters.getCollectionNames().addAll(selectedCollectionNames);
			}
		});
		collectionNameList.setPreferredSize(new Dimension(100, 100));
		add(collectionNameList, "1, 0");
		
		DefaultListModel<LogPreprocessorAlgorithm> preprocessors = new DefaultListModel<LogPreprocessorAlgorithm>();
		selectedIndices = new int[1];
		i = 0;
		j = 0;
		for (LogPreprocessorAlgorithm preprocessor : parameters.getAllPreprocessors()) {
			preprocessors.addElement(preprocessor);
			if (parameters.getPreprocessor().equals(preprocessor)) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<LogPreprocessorAlgorithm> preprocessorList = new ProMList<LogPreprocessorAlgorithm>("Select preprocessor", preprocessors);
		preprocessorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		preprocessorList.setSelectedIndices(selectedIndices);
		preprocessorList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<LogPreprocessorAlgorithm> selectedPreprocessors = preprocessorList.getSelectedValuesList();
				if (selectedPreprocessors.size() == 1) {
					parameters.setPreprocessor(selectedPreprocessors.iterator().next());
				}
			}
		});
		preprocessorList.setPreferredSize(new Dimension(100, 100));
		add(preprocessorList, "2, 0");

	}
}
