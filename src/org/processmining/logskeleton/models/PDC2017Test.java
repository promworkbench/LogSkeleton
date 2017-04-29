package org.processmining.logskeleton.models;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.util.HTMLToString;

public class PDC2017Test implements HTMLToString {

	private List<Integer> numbers;
	private List<XLog> aprilLogs;
	private List<XLog> mayLogs;
	private List<XLog> juneLogs;
	private List<LogSkeleton> models;
	
	public PDC2017Test() {
		numbers = new ArrayList<Integer>();
		aprilLogs = new ArrayList<XLog>();
		mayLogs = new ArrayList<XLog>();
		juneLogs = new ArrayList<XLog>();
		models = new ArrayList<LogSkeleton>();
	}
	
	public void add(int i, XLog aprilLog, XLog mayLog, XLog juneLog,LogSkeleton model) {
		numbers.add(i);
		aprilLogs.add(aprilLog);
		mayLogs.add(mayLog);
		juneLogs.add(juneLog);
		models.add(model);
	}
	
	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buf = new StringBuffer();
		if (includeHTMLTags) {
			buf.append("<html>");
		}
		buf.append("<table><tr><th>Number</th><th>April</th><th>May</th><th>June</th></tr>");
		for (int i = 0; i < numbers.size(); i++) {
			buf.append("<tr><td>" + numbers.get(i) + "</td><td>" + aprilLogs.get(i).size() + "</td><td>" + mayLogs.get(i).size() + "</td><td>" + juneLogs.get(i).size() + "</td></tr>");
		}
		buf.append("</table>");
		if (includeHTMLTags) {
			buf.append("</html>");
		}
		return buf.toString();
	}

}
