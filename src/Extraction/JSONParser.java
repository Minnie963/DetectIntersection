package Extraction;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;

import java.util.regex.*;
import java.util.*;

public class JSONParser {

	public static HashMap parseJSON(String jsonString) throws IOException {
		/*Function description:
		 * 		Argument: JSON string containing output of the Google Maps API Query
		 * 		Return value: Hash of Lat, Long and direction of the steps which contain turn instruction
		 * 
		 * 		Parse the given JSON string and extract each step.
		 * 		Verify if the instruction corresponding to each step contains
		 * 		a turn indication.
		 * 		If it contains a turn, then store the instruction and lat long in a 
		 * 		hash corresponding to that step.
		 */
		
		//Parse the input JSON string
		JSONObject obj=(JSONObject)JSONValue.parse(jsonString);
		
		/*Structure of Google Maps JSON, which is of interest to the current problem:
		 * routes array(containing single element)
		 * 		=> legs array(containing single element)
		 * 				=> steps array(containing the set of steps for this 
		 *                 particular route)
		 * 						=> start_location => JSON string containing 
		 *                         lat and long values of starting point
		 *                         in a particular route step
		 *                      => end_location => JSON string containing
		 *                         lat and long values of ending point
		 *                         in a particular route step
		 *                      => html_instructions => contains the 
		 *                         instruction for this particular step
		 */
		
		HashMap<Integer,HashMap<String,String>> stepsMap = new HashMap<Integer,HashMap<String,String>>();
		int hashIndex=1;

		JSONArray routesArray=(JSONArray)obj.get("routes");
		
		int noOfRoutes=routesArray.size();
		for (int routesIndex=0;routesIndex<noOfRoutes;routesIndex++) {
			JSONObject currentRouteObject=(JSONObject)routesArray.get(routesIndex);
			
			JSONArray legsArray=(JSONArray)currentRouteObject.get("legs");
			int noOfLegs=legsArray.size();
			
			for (int legsIndex=0;legsIndex<noOfLegs;legsIndex++) {
				JSONObject currentLegObject=(JSONObject)legsArray.get(legsIndex);
				
				JSONArray stepsArray=(JSONArray)currentLegObject.get("steps");
				int noOfSteps=stepsArray.size();
								
				for (int stepsIndex=0;stepsIndex<noOfSteps;stepsIndex++) {
					//Each step which has a turn instruction will be a hash
					//which contains instruction, start and end lat long values
					HashMap<String, String> step = new HashMap<String,String>();
				
					JSONObject  stepObject=(JSONObject)stepsArray.get(stepsIndex);
					//System.out.println(stepObject.get("html_instructions"));
					String instruction = (String)stepObject.get("html_instructions");
					JSONObject startLocation=(JSONObject)stepObject.get("start_location");
					
					//Regex to match if an instruction contains right/left keywords 
					//in order to identify a turn point
					//Matching onto keyword also, as sometime the instruction will be like "Take left and the destination will be on the left"
					//We avoid counting in these instructions by this
					String pattern=".*(right|left).*onto.*";
					
					Pattern regex = Pattern.compile(pattern);
					
					Matcher match = regex.matcher(instruction);
					if(match.find()) {
						//hashIndex variable maintains the list of indices to the hash
						stepsMap.put(hashIndex++, step);
						Double latDbl = (Double)startLocation.get("lat");
						Double lngDbl = (Double)startLocation.get("lng");	
						step.put("Lat", Double.toString(latDbl));
						step.put("Lng", Double.toString(lngDbl));
						step.put("Direction", match.group(1));
					} 
				}
			}
		}
		Set set = stepsMap.entrySet();
		Iterator i = set.iterator();
		int k = 0;
		while(i.hasNext()) {
		    	Map.Entry me = (Map.Entry)i.next();
	            System.out.print(me.getKey() + ": ");
	            System.out.println(me.getValue());
		}
		return stepsMap;
	}
	
	public static String parseStreetAddress(String jsonString) throws IOException {
	
		JSONObject obj=(JSONObject)JSONValue.parse(jsonString);

		JSONArray resultsArray=(JSONArray)obj.get("results");
		JSONObject currentRouteObject=(JSONObject)resultsArray.get(0);
		String address = (String)currentRouteObject.get("formatted_address");

		return address;
	}
}