package de.deeps.modules.webserver;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import com.sun.net.httpserver.HttpExchange;

import de.deeps.Utils;
import de.deeps.event.EventCollector;

/**
 * @author Deeps
 */

public class OverviewHandler extends MyHttpHandler {

	private final String OVERVIEW_FILENAME = "overview.html",
			RULES_DIR = "rules";

	private int pcVolume;
	private HashMap<String, Boolean> availableMachines;
	private StringBuilder rulesHTML;
	private Website website;

	public OverviewHandler(EventCollector eventCollector, Website website) {
		availableMachines = new HashMap<>();
		this.website = website;
		rulesHTML = new StringBuilder();
	}

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		website.updateAll();
		try {
			updateRulesHTML();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		String content = Utils.readStringFromFile(
			new File(Website.WEBSERVER_ROOT_DIR + "/" + OVERVIEW_FILENAME));
		content = parseContent(content);
		httpExchange.getResponseHeaders().add("Content-Type", "text/html");
		sendResponseWithContent(httpExchange, content.getBytes());
	}

	private String parseContent(String content) {
		content = content.replace(
			"#isPIOnline#",
			availableMachines.getOrDefault("raspberry", false).toString());
		content = content.replace(
			"#isPCOnline#",
			availableMachines.getOrDefault("standpc", false).toString());
		content = content
				.replace("#currentVolume#", Integer.toString(pcVolume));
		content = content.replace("#ruleRows#", rulesHTML);
		content = content.replace("#alarmsinjson#", getAlarmsAsJson());
		return content;
	}

	private String getAlarmsAsJson() {
		try {
			String alarms = Utils.readStringFromFile(new File("alarms.txt"));
			JsonArrayBuilder jsonArray = Json.createArrayBuilder();
			for (String alarm : alarms.split("\r\n")) {
				if (alarm.length() == 0) {
					continue;
				}
				jsonArray.add(
					Json.createObjectBuilder().add("tag", alarm).build());
			}
			return jsonArray.build().toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void updateRulesHTML() throws URISyntaxException {
		rulesHTML.delete(0, rulesHTML.length());
		int ruleID = 0;
		for (File file : new File(RULES_DIR).listFiles()) {
			if (!file.isFile() || !(file.getName().endsWith(".json")
					|| file.getName().endsWith(".bak"))) {
				continue;
			}
			ruleID++;
			String isActive = "";
			if (file.getName().endsWith(".json")) {
				isActive = "checked=\"checked\"";
			}
			rulesHTML.append("<tr>");
			rulesHTML.append("	<td>" + ruleID + "</td>");
			rulesHTML
					.append(
						"	<td>"
								+ file.getName().substring(
									0,
									file.getName().lastIndexOf('.'))
								+ "</td>");
			rulesHTML.append("	<td>");
			rulesHTML.append(
				"		<input type=\"checkbox\" class=\"filled-in\" id=\"rule"
						+ ruleID + "checkbox\"" + isActive
						+ " onclick=\"sendRuleUpdate(this);\"/>");
			rulesHTML.append(
				"		<label for=\"rule" + ruleID
						+ "checkbox\">Aktivieren</label>");
			rulesHTML.append("	</td>");
			rulesHTML.append("</tr>");
		}
	}

	public HashMap<String, Boolean> getAvailableMachines() {
		return availableMachines;
	}

	public void setPcVolume(int pcVolume) {
		this.pcVolume = pcVolume;
	}

}
