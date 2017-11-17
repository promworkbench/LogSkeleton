package org.processmining.logskeleton.pdc2016.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.logskeleton.pdc2016.parameters.PDC2016TestParameters;
import org.processmining.pdc2016.algorithms.PDC2016Set;

public class PDC2016TestDialog extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4347149448035402690L;
	private PDC2016TestParameters parameters;
	
	public PDC2016TestDialog(final PDC2016TestParameters parameters) {
		this.parameters = parameters;
		
		double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		setOpaque(false);

		DefaultListModel<PDC2016Set> setListModel = new DefaultListModel<PDC2016Set>();
		int[] selectedIndices = new int[parameters.getAllSets().size()];
		int i = 0;
		int j = 0;
		for (PDC2016Set set : parameters.getAllSets()) {
			setListModel.addElement(set);
			if (parameters.getSets().contains(set)) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<PDC2016Set> setList = new ProMList<PDC2016Set>("Select log set(s)", setListModel);
		setList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setList.setSelectedIndices(selectedIndices);
		setList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<PDC2016Set> selectedSets = setList.getSelectedValuesList();
				parameters.setSets(new HashSet<PDC2016Set>(selectedSets));
			}
		});
		setList.setPreferredSize(new Dimension(100, 100));
		add(setList, "0, 0");

		DefaultListModel<Integer> nrListModel = new DefaultListModel<Integer>();
		selectedIndices = new int[parameters.getAllNrs().size()];
		i = 0;
		j = 0;
		for (int nr : parameters.getAllNrs()) {
			nrListModel.addElement(nr);
			if (parameters.getNrs().contains(nr)) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<Integer> nrList = new ProMList<Integer>("Select log number(s)", nrListModel);
		nrList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		nrList.setSelectedIndices(selectedIndices);
		nrList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<Integer> selectedNrs = nrList.getSelectedValuesList();
				parameters.setNrs(new HashSet<Integer>(selectedNrs));
			}
		});
		nrList.setPreferredSize(new Dimension(100, 100));
		add(nrList, "1, 0");
		
	}
}
