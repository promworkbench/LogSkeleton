package org.processmining.logskeleton.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
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

	public void add(int i, XLog aprilLog, XLog mayLog, XLog juneLog, LogSkeleton model) {
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
		List<XLog> logs;
		for (int l = 0 ; l < 3; l++) {
			if (l == 0) {
				buf.append("<h1>April</h1>");
				logs = aprilLogs;
			} else if (l == 1) {
				buf.append("<h1>May</h1>");
				logs = mayLogs;
			} else {
				buf.append("<h1>June</h1>");
				logs = juneLogs;
			}
			buf.append("<table><tr><th></th>");
			for (int n = 1; n < 21; n++) {
				buf.append("<th>trace_" + n + "</th>");
			}
			buf.append("<th>#True</th>");
			buf.append("</tr>");
			for (int i = 0; i < numbers.size(); i++) {
				Set<String> acceptedTraces = new HashSet<String>();
				for (XTrace trace : logs.get(i)) {
					acceptedTraces.add(XConceptExtension.instance().extractName(trace));
				}
				buf.append("<tr><td>model_" + numbers.get(i) + "</td>");
				for (int n = 1; n < 21; n++) {
					buf.append("<td>" + (acceptedTraces.contains("" + n) ? "TRUE" : "FALSE") + "</td>");
				}
				buf.append("<td>" + logs.get(i).size() + "</td>");
				buf.append("</tr>");
			}
			buf.append("</table>");
		}
		if (includeHTMLTags) {
			buf.append("</html>");
		}
		return buf.toString();
	}

}
