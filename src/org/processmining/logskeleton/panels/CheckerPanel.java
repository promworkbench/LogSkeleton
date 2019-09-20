package org.processmining.logskeleton.panels;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.processmining.log.dialogs.ClassifierPanel;
import org.processmining.logskeleton.configurations.CheckerConfiguration;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class CheckerPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5707483507050383273L;

	public CheckerPanel(XLog log, CheckerConfiguration configuration) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30, 30, 30, 30 } };
		setLayout(new TableLayout(size));
		add(new ClassifierPanel(log.getClassifiers(), configuration), "0, 0");
		
		final JCheckBox check1 = SlickerFactory.instance().createCheckBox("Check Equivalence",
				false);
		check1.setSelected(configuration.getChecks()[0]);
		check1.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setCheck(0, check1.isSelected());
			}

		});
		check1.setOpaque(false);
		check1.setPreferredSize(new Dimension(100, 30));
		add(check1, "0, 1");

		final JCheckBox check2 = SlickerFactory.instance().createCheckBox("Check Response and Precedence",
				false);
		check2.setSelected(configuration.getChecks()[1]);
		check2.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setCheck(1, check2.isSelected());
			}

		});
		check2.setOpaque(false);
		check2.setPreferredSize(new Dimension(100, 30));
		add(check2, "0, 2");

		final JCheckBox check3 = SlickerFactory.instance().createCheckBox("Check Cardinalities",
				false);
		check3.setSelected(configuration.getChecks()[2]);
		check3.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setCheck(2, check3.isSelected());
			}

		});
		check3.setOpaque(false);
		check3.setPreferredSize(new Dimension(100, 30));
		add(check3, "0, 3");

		final JCheckBox check4 = SlickerFactory.instance().createCheckBox("Stop on First Violation",
				false);
		check4.setSelected(configuration.isStopAtFirstViolation());
		check4.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				configuration.setStopAtFirstViolation(check4.isSelected());
			}

		});
		check4.setOpaque(false);
		check4.setPreferredSize(new Dimension(100, 30));
		add(check4, "0, 4");
	}
}
