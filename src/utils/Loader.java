package utils;

import java.io.*;
import java.net.*;
import java.util.*;

import org.json.*;

import game.*;

public class Loader {

	public static void setupResearch(HashMap<String, Research> researches, ArrayList<Research> researchList) {
		HashMap<Research, LinkedList<String>> researchRequirements = new HashMap<>();
		String researchCosts = "";
		URL path = Utils.class.getClassLoader().getResource("resources/costs/ResearchType.json");
		try (BufferedReader br = new BufferedReader(new FileReader(path.getPath()))) {
			StringBuilder builder = new StringBuilder();
			String line;
			while((line = br.readLine()) != null) {
				line = line.replaceAll("\\s+","");
				builder.append(line);
			}
			researchCosts = builder.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(researchCosts);
		JSONObject obj = new JSONObject(researchCosts);
		JSONArray arr = obj.getJSONArray("researches");
		for (int i = 0; i < arr.length(); i++) {
			JSONObject researchObject = arr.getJSONObject(i);
			String researchName = researchObject.getString("name");
			String imagePath = Research.DEFAULT_RESEARCH_IMAGE_PATH;
			if(researchObject.has("image")) {
				imagePath = researchObject.getString("image");
			}
			int points = researchObject.getInt("points");
			int tier = researchObject.getInt("tier");
			Research research = new Research(researchName, imagePath, points, tier);
			researches.put(researchName, research);
			researchList.add(research);
			
			if(researchObject.has("cost")) {
				JSONObject costArray = researchObject.getJSONObject("cost");
				for(String itemName : costArray.keySet()) {
					int quantity = costArray.getInt(itemName);
					System.out.println(researchName + ":" + itemName + ":" + quantity);
					
					research.addCost(ItemType.valueOf(itemName), quantity);
				}
			}
			if(researchObject.has("requirements")) {
				JSONArray requirementArray = researchObject.getJSONArray("requirements");
				LinkedList<String> reqs = new LinkedList<>();
				for(int j = 0; j < requirementArray.length(); j++) {
					String requirement = requirementArray.getString(j);
					reqs.add(requirement);
				}
				researchRequirements.put(research, reqs);
			}
		}
		// Link research requirements
		for(Research research : researchRequirements.keySet()) {
			for(String req : researchRequirements.get(research)) {
				Research requirement = researches.get(req);
				if(requirement != null) {
					research.getRequirement().addRequirement(requirement);
				}
				System.out.println(research + " require  " + requirement);
			}
		}
	}
}
