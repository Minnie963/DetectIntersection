
package Extraction;

import java.io.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.*;

public class Extraction {	
	
/**
* @param args
* @throws IOException
*/

	public static void main(String[] args) throws IOException {
				
		String filePath = "gps.dat";
		List<Trace> gps = ReadWriteTrace.readFile(filePath);
	
		// Lat/long of ComputerScience department
		// String baseLat = "43.0713085";
		// String baseLng = "-89.40631809999999";
		
		// Lat/Long of Camp Randall Stadium
		String baseLat = "43.0695977";
		String baseLng = "-89.4119546";
		
		HashMap<String, String> globalMap = new HashMap<String, String>();
		
		// String res = Extraction.FetchHTTPResponse(baseLat, baseLng, topLat,
		// baseLng);
		// "43.07334729999999","-89.45303319999999");
		
		// HashMap stepsMap = JSONParser.parseJSON(res);
		// File to which the intersection points are updated, along with
		// confidence value
		File file = new File("IntersectionGPSData.txt");
		file.createNewFile();
		FileWriter writer = new FileWriter(file);
		
		/*
		* String origLat = baseLat; String origLng = baseLng;
		* 
		* //System.out.println("$$DEBUG$$: origLng = " + origLng);
		*
		* //System.out.println("Trying original route!!");
		* 
		* //String res = Extraction.FetchHTTPResponse(baseLat, baseLng,
		* "43.07334729999999","-89.45303319999999"); //HashMap stepsMap =
		* JSONParser.parseJSON(res); //globalMap =
		* validateIntersection(stepsMap, writer, globalMap, "nothing");
		*/
		
		processGPS(writer, globalMap,gps);
	
		System.out.println("#############################################################");
		System.out.println("Intersection list generated:");
		System.out.println("#############################################################");
		Set set = globalMap.entrySet();
		Iterator i = set.iterator();
		int k = 0;
		List<Trace> intersections = new ArrayList<Trace>();
		while (i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			System.out.print(me.getKey() + ": ");
			System.out.println(me.getValue());
		}
		
		writer.close();
	}
	
	private static boolean insert(List<Trace> intersections, double lat0, double lng0)
	{
		Trace trace = new Trace();
		trace.values[0] = lng0;
		trace.values[1] = lat0;
		if(minDist(lat0, lng0, intersections) > 10) {
			intersections.add(trace);
			return true;
		}
		return false;
	}
	
	private static double speed(double lat0, double lng0, List<Trace> gps) {
		int sz = gps.size();
		double res = 1000;
		int index = -1;
		for(int i = 0; i< sz - 1; ++i) {
			double dist = distance(lat0, lng0, gps.get(i).values[1], gps.get(i).values[0]);
			if(dist < res) {
				res = dist;
			    index = i;	
			}
		}
		Trace tr0 = gps.get(index);
		Trace tr1 = gps.get(index + 1);
		double dist = distance(tr0.values[1], tr0.values[0], tr1.values[1], tr1.values[0]);	
		double time = (tr1.time - tr0.time)/1000.0;
		return dist/time;		
		
	}
	
	private static double minDist(double lat0, double lng0, List<Trace> gps) {
		int sz = gps.size();
		double res = 1000;
		for(int i = 0; i< sz; ++i) {
			double dist = distance(lat0, lng0, gps.get(i).values[1], gps.get(i).values[0]);
			if(dist < res)
				res = dist;
		}
		return res;
	}
	
	public static HashMap processGPS(FileWriter writer, HashMap globalMap, List<Trace> gps) throws IOException {
	
		FileReader fr = new FileReader("gps.dat");
		BufferedReader reader = new BufferedReader(fr);
		String line = null;
		String prevLat = null;
		String prevLng = null;
		
		Double distLimit = 20.0;
		
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split("\t");
			String lat = parts[2];
			String lng = parts[1];
			if (prevLat == null) {
				// Intiatializing for the first time alone
				prevLat = lat;
				prevLng = lng;
				continue;
			}
			
			if (lat.equals(prevLat) && lng.equals(prevLng)) {
				// If two GPS points are the same, iterate over next point
				// In the case when vehicle is stationary
				continue;
			}

			if (!lat.equals(prevLat) && !lng.equals(prevLng)) {
				System.out.println("Previous value is: " + prevLat + "\t" + prevLng);
				System.out.println("Current value is: " + lat + "\t" + lng);
				Double dblprevLat = Double.parseDouble(prevLat);
				Double dblprevLng = Double.parseDouble(prevLng);
				Double dblLat = Double.parseDouble(lat);
				Double dblLng = Double.parseDouble(lng);
				Double dist = distance(dblprevLat, dblprevLng, dblLat,dblLng);
				if(dist <= distLimit) {
					//Unless we find a point at least 20 m away keep iterating again
					continue;
				}
				
				/*
				* String street = null; 
				* String street1 = null; 
				* String streets = null;
				* 
				* String res = FetchStreetAddress(lat, lng); 
				* String address = JSONParser.parseStreetAddress(res); 
				* String[] addrParts = address.split(","); String pattern="[0-9]* (.*)"; 
				* Pattern regex = Pattern.compile(pattern); 
				* Matcher match = regex.matcher(addrParts[0]); 
				* if(match.find()) { 
				* street = match.group(1); 
				* // System.out.println("Street is: " + match.group(1)); }
				* 
				* res = FetchStreetAddress(prevLat, prevLng); 
				* String address1 = JSONParser.parseStreetAddress(res); 
				* addrParts = address1.split(","); 
				* match = regex.matcher(addrParts[0]);
				* if(match.find()) { 
				* 	street1 = match.group(1); //
				* 	System.out.println("Street is: " + match.group(1)); 
				* }
				* 
				* if(!street.equals(street1)) { 
				* 	streets = street + "::" + street1; 
				* } else { 
				* streets = street + "::"; 
				* }
				*/
				// System.out.println("Before calling: " + streets);
				// globalMap = genRectFromGPS(prevLat, prevLng, lat, lng,
				// writer, globalMap, streets);
				boolean latExists = globalMap.containsKey(lat + "|" + lng);
				if (latExists == true) {
					continue;
				} else {
					Double dblPrevLat = Double.parseDouble(prevLat);
					Double dblPrevLng = Double.parseDouble(prevLng);
					dblLat = Double.parseDouble(lat);
					dblLng = Double.parseDouble(lng);
					Double degree = direction(dblPrevLat, dblPrevLng, dblLat, dblLng);
					//System.out.println("Degree is : " + degree);
					globalMap = VerifyRightLeftPoints(prevLat, prevLng, lat, lng, writer,globalMap, degree, gps);
					//return globalMap;
				}
			} 
	
			prevLat = lat;
			prevLng = lng;
		}

		return globalMap;
	}

	public static double direction(Double firstLat, Double firstLng,
		Double secondLat, Double secondLng) {
		double lat1 = Math.toRadians(firstLat);
		double lon1 = Math.toRadians(firstLng);
		double lat2 = Math.toRadians(secondLat);
		double lon2 = Math.toRadians(secondLng);
		
		double y = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)* Math.cos(lat2) * Math.cos(lon2 - lon1);
		double x = Math.sin(lon2 - lon1) * Math.cos(lat2);
		double res = Math.atan2(y, x);
		
		double degree = Math.toDegrees(res);
		if (degree < 0.0)
		degree += 360.0;
		
		if (degree >= 360.0 || degree < 0.0) {
			System.out.println("Direction error! " + degree);
			return -1;
		}
		return degree;
	}

	public static HashMap VerifyRightLeftPoints(String prevLat, String prevLng, String lat, String lng,
			FileWriter writer, HashMap globalMap, Double degree, List<Trace> gps)
					throws IOException {
	
		String startLat = prevLat;
		String startLng = prevLng;
		
		Double dblLat = Double.parseDouble(lat);
		Double dblLng = Double.parseDouble(lng);
		Double delta = 0.004;
		Double distanceThresh = 20.0;
		Double leftPointLng = dblLng - delta;
		Double leftPointLat = dblLat;
		Double rightPointLng = dblLng + delta;
		Double rightPointLat = dblLat;		
		Double bottomPointLat = dblLat - delta;
		Double bottomPointLng = dblLng;
		Double topPointLat = dblLat + delta;
		Double topPointLng = dblLng;
		
		boolean TB = false;
		boolean LR = false;
		
		if (degree > 180) {
			degree = 360 - degree;
		}
		if (degree >= 45 && degree <= 135) {
			System.out.println("Left and right points chosen!");
			LR = true;
		} else {
			System.out.println("Top and bottom points chosen!");
			TB = true;
		}
		
		//Start point is always the previous lat/long values from the trace file
		//Only changing the end points accordingly
		if (LR == true) {
			//Chose left and right points
			//Route between reference point and left point
			String endLat = Double.toString(leftPointLat);
			String endLng = Double.toString(leftPointLng);
			HashMap stepsMapInternal = processSingleRoute(startLat, startLng, endLat, endLng);
			int hashSize = stepsMapInternal.size();
			globalMap = processRouteUncontrolled(stepsMapInternal, globalMap, writer, gps, distanceThresh);
			waitTime();
			if (hashSize < 1) {
				stepsMapInternal = processSingleRoute(endLat, endLng, startLat, startLng);
				globalMap = processRouteUncontrolled(stepsMapInternal, globalMap, writer, gps, distanceThresh);
				waitTime();
			}
	
			//Route between reference point and right point
			endLat = Double.toString(rightPointLat);
			endLng = Double.toString(rightPointLng);
			stepsMapInternal = processSingleRoute(startLat, startLng, endLat, endLng);
			hashSize = stepsMapInternal.size();
			globalMap = processRouteUncontrolled(stepsMapInternal, globalMap, writer, gps, distanceThresh);
			waitTime();
			if (hashSize < 1) {
				stepsMapInternal = processSingleRoute(endLat, endLng, startLat, startLng);
				globalMap = processRouteUncontrolled(stepsMapInternal, globalMap, writer, gps, distanceThresh);
				waitTime();
			}
		
		} else {
			// Choose top and bottom points
			//Route between reference point and top point
			String endLat = Double.toString(topPointLat);
			String endLng = Double.toString(topPointLng);
			HashMap stepsMapInternal = processSingleRoute(startLat, startLng, endLat, endLng);
			int hashSize = stepsMapInternal.size();
			globalMap = processRouteUncontrolled(stepsMapInternal, globalMap, writer, gps, distanceThresh);
			waitTime();
			if (hashSize < 1) {
				stepsMapInternal = processSingleRoute(endLat, endLng, startLat, startLng);
				globalMap = processRouteUncontrolled(stepsMapInternal, globalMap, writer, gps, distanceThresh);
				waitTime();
			}
			
			//Route between reference point and bottom point
			endLat = Double.toString(bottomPointLat);
			endLng = Double.toString(bottomPointLng);
			stepsMapInternal = processSingleRoute(startLat, startLng, endLat, endLng);
			hashSize = stepsMapInternal.size();
			globalMap = processRouteUncontrolled(stepsMapInternal, globalMap, writer, gps, distanceThresh);
			waitTime();
			if (hashSize < 1) {
				stepsMapInternal = processSingleRoute(endLat, endLng, startLat, startLng);
				globalMap = processRouteUncontrolled(stepsMapInternal, globalMap, writer, gps, distanceThresh);
				waitTime();
			}
		
		}
		
		return globalMap;
	}
	
	public static HashMap processSingleRoute(String startLat, String startLng, String endLat, String endLng) throws IOException{
	
		String resInternal = Extraction.FetchHTTPResponse(startLat, startLng, endLat, endLng);
		HashMap stepsMapInternal = JSONParser.parseJSON(resInternal);
		return stepsMapInternal;
	}

	public static HashMap processRouteUncontrolled(HashMap stepsMapInternal, HashMap globalMap, Writer writer, List<Trace> gps, Double distanceThresh) throws IOException {
		
		int hashSize = stepsMapInternal.size();
		if (hashSize >= 1) {
				// System.out.println("Entering hash size > 1");
			Set set = stepsMapInternal.entrySet();
			Iterator i = set.iterator();
				
			int k = 0;
			while (i.hasNext()) {
				Map.Entry me = (Map.Entry) i.next();
				HashMap finalVal = (HashMap) me.getValue();
				String intLat = (String) finalVal.get("Lat");
				String intLng = (String) finalVal.get("Lng");
				
				//System.out.println("Lat: " + intLat + "\t Long: " + intLng);
				String latLng = intLat + "|" + intLng;
				
				boolean latExists = globalMap.containsKey(intLat + "|" + intLng);
				if (latExists != true) {
					Double dblIntLat = Double.parseDouble(intLat);
					Double dblIntLng = Double.parseDouble(intLng);
					
					//Find the minimum distance between the identified point and the route points
					Double dist = minDist(dblIntLat, dblIntLng, gps);
				
					if (dist <= distanceThresh) {
						writer.write("0\t");
						writer.write(intLat + "\t");
						writer.write(intLng + "\n");
						
						globalMap.put(intLat + "|" + intLng, "1");
					}
				}
			}
		}	
		return globalMap;
	}
	
	public static void waitTime() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
	
	public static double distance(Double firstLat, Double firstLng, Double secondLat, Double secondLng) {
		double lat1 = Math.toRadians(firstLat);
		double lng1 = Math.toRadians(firstLng);
		double lat2 = Math.toRadians(secondLat);
		double lng2 = Math.toRadians(secondLng);
		
		double p1 = Math.cos(lat1) * Math.cos(lat2) * Math.cos(lng1 - lng2);
		double p2 = Math.sin(lat1) * Math.sin(lat2);
		
		double res = Math.acos(p1 + p2);
		return res * 6371 * 1000;
	}

	
	public static HashMap processOneRectangleRoute(String startLat, String startLng, String endLat, String endLng, 
			FileWriter writer, HashMap globalMap, String streets) throws IOException {
			
		String res = Extraction.FetchHTTPResponse(startLat, startLng, endLat, endLng);
		HashMap stepsMap = JSONParser.parseJSON(res);
		globalMap = validateIntersection(stepsMap, writer, globalMap, streets);
		
		return globalMap;
	}

	public static HashMap genRectFromGPS(String firstLat, String firstLng,
			String secondLat, String secondLng, FileWriter writer,
				HashMap globalMap, String streets) throws IOException {
	
		//Generating rectangle based on the reference GPS points
		Double dblfirstLat = Double.parseDouble(firstLat);
		Double dblfirstLng = Double.parseDouble(firstLng);
		Double dblsecondLat = Double.parseDouble(secondLat);
		Double dblsecondLng = Double.parseDouble(secondLng);
		Double delta = 0.0004;
		Double dblRightLng = dblfirstLng + delta;
		Double dblRightLat = dblfirstLat;
		Double dblBaseLng = dblfirstLng - delta;
		Double dblBaseLat = dblfirstLat;
		Double dblTopRightLng = dblsecondLng + delta;
		Double dblTopRightLat = dblsecondLat;
		Double dblTopLng = dblsecondLng - delta;
		Double dblTopLat = dblsecondLat;
		
		String baseLng = dblBaseLng.toString();
		String topLng = dblTopLng.toString();
		String topRightLng = dblTopRightLng.toString();
		String rightLng = dblRightLng.toString();
		
		//System.out.println("Route between base point and top point:");
		globalMap = processOneRectangleRoute(firstLat, baseLng, secondLat, topLng, writer, globalMap, streets);
	
		//System.out.println("Route between top point and base point:");
		globalMap = processOneRectangleRoute(secondLat, topLng, firstLat, baseLng, writer, globalMap, streets);
		
		//System.out.println("Route between base point and right point:");
		globalMap = processOneRectangleRoute(firstLat, baseLng, firstLat, rightLng, writer, globalMap, streets);
		
		//System.out.println("Route between right point and base point:");
		globalMap = processOneRectangleRoute(firstLat, rightLng, firstLat, baseLng, writer, globalMap, streets);
		
		//System.out.println("Route between right point and top right point:");
		globalMap = processOneRectangleRoute(firstLat, rightLng, secondLat, topRightLng, writer, globalMap, streets);
		
		//System.out.println("Route between top right point and right point:");
		globalMap = processOneRectangleRoute(secondLat, topRightLng, firstLat, rightLng, writer, globalMap, streets);
		
		//System.out.println("Route between top point and top right point:");
		globalMap = processOneRectangleRoute(secondLat, topLng, secondLat, topRightLng, writer, globalMap, streets);
		
		//System.out.println("Route between top right point and top point:");
		globalMap = processOneRectangleRoute(secondLat, topRightLng, secondLat, topLng, writer, globalMap, streets);
		
		//System.out.println("Route between top point and right point:");
		globalMap = processOneRectangleRoute(secondLat, topLng, firstLat, rightLng, writer, globalMap, streets);
		
		//System.out.println("Route between right point and top point:");
		globalMap = processOneRectangleRoute(firstLat, rightLng, secondLat, topLng, writer, globalMap, streets);
	
		//System.out.println("Route between base point and top right point:");
		globalMap = processOneRectangleRoute(firstLat, baseLng, secondLat, topRightLng, writer, globalMap, streets);
	
		//System.out.println("Route between top right point and base point:");
		globalMap = processOneRectangleRoute(secondLat, topRightLng, firstLat, baseLng, writer, globalMap, streets);
		
		return globalMap;
	
	}

	public static String generateNextBaseRightSide(String baseLng) {
		
		Double dblBaseLng = Double.parseDouble(baseLng);
		
		Double delta = 0.0008;
		//The max limit of the longitude
		//Must be near end of the map for the city
		Double lngLimit = -89.39631809999999;
		String newLng = "false";
		
		if (dblBaseLng < lngLimit) {
			Double dblNewLng = dblBaseLng + delta;
			newLng = dblNewLng.toString();
		} 
		
		return newLng;
	}

	public static String generateNextBaseUp(String baseLat) {
	
		Double dblBaseLat = Double.parseDouble(baseLat);
		
		Double delta = 0.0008;
		//The max limit of the longitude
		//Must be near end of the map for the city
		Double latLimit = 43.0813085;
		String newLat = "false";
		
		if (dblBaseLat < latLimit) {
			Double dblNewLat = dblBaseLat + delta;
			newLat = dblNewLat.toString();
		}
		
		return newLat;
	}

	public static HashMap BeginRectRouteProcessor(String baseLat, String baseLng, FileWriter writer, HashMap globalMap)
			throws IOException {
		
		/*
		* Function description: Arguments: 1. Base Latitude and longitude for
		* generating the rectangle 2. Object to write to the GPS file 3. The
		* global HashMap, which stores the existing intersection points Return
		* value: Global HashMap
		* 
		* Taking base lat and long, generate three points of a rectangle using
		* delta value as 0.0008: 
		* 	1. Generate the top point by incrementing base lat value by delta. 
		*      (long value remains the same for the top point)
		* 	2. Generate the right point by incrementing base long value by delta.
		*      (lat value remains the same for the right point) 
		*   3. Top right point will be combination of lat for top point and long for right point.
		* 
		* Validate routes between: 
		* 	1. Base point and top point 
		* 	2. Base point and right point 
		* 	3. Top point and top right point 
		* 	4. Right point and top right point 
		* 	5. Base point and top right point 
		* 	6. Right point and top point
		*/
		
		Double dblBaseLat = Double.parseDouble(baseLat);
		Double dblBaseLng = Double.parseDouble(baseLng);
		Double delta = 0.0008;
		Double dblTopLat = dblBaseLat + delta;
		Double dblRightLng = dblBaseLng + delta;
		
		String topLat = dblTopLat.toString();
		String rightLng = dblRightLng.toString();
		
		//System.out.println("Route between base point and top point:");
		globalMap = processOneRectangleRoute(baseLat, baseLng, topLat, baseLng, writer, globalMap, "nothing");
		
		//System.out.println("Route between top point and base point:");
		globalMap = processOneRectangleRoute(topLat, baseLng, baseLat, baseLng, writer, globalMap, "nothing");
		
		//System.out.println("Route between base point and right point:");
		globalMap = processOneRectangleRoute(baseLat, baseLng, baseLat, rightLng, writer, globalMap, "nothing");
		
		//System.out.println("Route between right point and base point:");
		globalMap = processOneRectangleRoute(baseLat, rightLng, baseLat, baseLng, writer, globalMap, "nothing");
		
		//System.out.println("Route between right point and top right point:");
		globalMap = processOneRectangleRoute(baseLat, rightLng, topLat, rightLng, writer, globalMap, "nothing");
		
		//System.out.println("Route between top right point and right point:");
		globalMap = processOneRectangleRoute(topLat, rightLng, baseLat, rightLng, writer, globalMap, "nothing");
		
		//System.out.println("Route between top point and top right point:");
		globalMap = processOneRectangleRoute(baseLat, rightLng, topLat, rightLng, writer, globalMap, "nothing");
		
		//System.out.println("Route between top right point and top point:");
		globalMap = processOneRectangleRoute(topLat, rightLng, baseLat, rightLng, writer, globalMap, "nothing");
		
		//System.out.println("Route between top point and right point:");
		globalMap = processOneRectangleRoute(baseLat, rightLng, topLat, rightLng, writer, globalMap, "nothing");
		
		//System.out.println("Route between right point and top point:");
		globalMap = processOneRectangleRoute(topLat, rightLng, baseLat, rightLng, writer, globalMap, "nothing");
		
		//System.out.println("Route between base point and top right point:");
		globalMap = processOneRectangleRoute(baseLat, rightLng, topLat, rightLng, writer, globalMap, "nothing");
		
		//System.out.println("Route between top right point and base point:");
		globalMap = processOneRectangleRoute(topLat, rightLng, baseLat, rightLng, writer, globalMap, "nothing");
		
		return globalMap;

	}

	public static HashMap validateIntersection(HashMap stepsMap, FileWriter writer, HashMap globalMap, String street)
			throws IOException {
		/*
		* Function description: Arguments: 1. HashMap of steps which contained
		* turn instructions: Lat, long and direction 2. Object to write to the
		* GPS file 3. The global HashMap, which stores the existing
		* intersection points Return value: Global HashMap
		* 
		* Processes the hash and invokes deltaApproxVerification function for
		* each of the points in the hash with a 500 millisecond delay to avoid
		* Google API returning empty JSON
		*/
		Set set = stepsMap.entrySet();
		Iterator i = set.iterator();
		int j = 1;
		while (i.hasNext()) {
			Map.Entry me = (Map.Entry) i.next();
			globalMap = deltaApproxVerification((HashMap) stepsMap.get(j), writer, globalMap, street);
			waitTime();
			j++;
		}
		
		return globalMap;
	}

	public static HashMap deltaApproxVerification(HashMap individualStep, FileWriter writer, HashMap globalMap, String street)
			throws IOException {
		/*
		* Function description: 
		* 		Arguments: 
		* 				1. Single Hashmap Object which contains lat, long and direction 
		* 				2. Object to write to the GPS file
		* 				3. The global HashMap, which stores the existing intersection points
		* 		Return value: Global HashMap
		* 
		* If globalMap already contains the input Lat/Long, then we don't do
		* any processing (as we already recorded that particular point)
		* 
		* Taking given lat and long, generate four points using delta value as
		* 0.0006 to validate if the point has intersection: 
		* 		1. Generate the top point by incrementing given lat value by delta. 
		* 		   (long value remains the same for the top point) 
		* 		2. Generate the bottom point by decrementing given lat value by delta. 
		* 		   (long value remains the same for the bottom point) 
		* 		3. Generate the right point by incrementing given long value by delta. 
		*          (lat value remains the same for the right point) 
		*       4. Generate the left point by decrementing given long value by delta. 
		*          (lat value remains the same for the left point)
		* 
		* Validate routes between(stop whenever at least two routes returned a
		* hashMap of size 1, that is: the route contained exactly one turn
		* instruction): 
		* 		1. Bottom point and left point. 
		* 		2. Left point and bottom point(if step 1 failed - to confirm opposite route in case of
		* 		   one way). 
		*       3. Bottom point and right point. 
		*       4. Right point and bottom point(if step 3 failed). 
		*       5. Top point and left point. 
		*       6. Left point and top point(if step 5 failed). 
		*       7. Top point and right point. 
		*       8. Right point and top point(if step 7 failed).
		* 
		* Then, the globalMap is updated with key as the concatenation for
		* lat/long values and the value will be: 1. 1 if at least two routes
		* with single turn were detected. 2. 0.25 otherwise(we still store this
		* because this method will fail in case both the roads are one-way's.
		* We want to retain this data for future fix or manual processing)
		*/
		
		String Lat = (String) individualStep.get("Lat");
		String Lng = (String) individualStep.get("Lng");
		String direction = (String) individualStep.get("Direction");
		
		// Verifying if the lat, long has already been marked as intersection
		boolean latExists = globalMap.containsKey(Lat + "|" + Lng);
		if (latExists == true) {
			return globalMap;
		}
		
		Double dblLat = Double.parseDouble(Lat);
		Double dblLng = Double.parseDouble(Lng);
		Double delta = 0.0004;
		int foundProof = 0;
		
		// Generating the required lat-long combination for delta approximation
		Double bottomPoint = dblLat - delta;
		Double topPoint = dblLat + delta;
		Double leftPoint = dblLng - delta;
		Double rightPoint = dblLng + delta;
		
		// Verifying route between bottom point and left point
		boolean BtoL = false;
		String startLat = Double.toString(bottomPoint);
		String startLng = Lng;
		String endLat = Lat;
		String endLng = Double.toString(leftPoint);
		
		HashMap stepsMapInternal = processSingleRoute(startLat, startLng, endLat, endLng);
		int hashSize = stepsMapInternal.size();
		if (hashSize == 1) {
			foundProof++;
			BtoL = true;
		}
		
		waitTime();
		
		if (BtoL == false) {
			// Verifying route between left point to bottom point -- In case the roads are one-way
			stepsMapInternal = processSingleRoute(startLat, startLng, endLat, endLng);
			hashSize = stepsMapInternal.size();
			if (hashSize == 1) {
				foundProof++;
			}
			waitTime();
		}
		
		// Verifying route between bottom point and right point
		boolean BtoR = false;
		startLat = Double.toString(bottomPoint);
		startLng = Lng;
		endLat = Lat;
		endLng = Double.toString(rightPoint);
		stepsMapInternal = processSingleRoute(startLat, startLng, endLat, endLng);
		hashSize = stepsMapInternal.size();
		if (hashSize == 1) {
			foundProof++;
			BtoR = true;
		}
		waitTime();
		
		if (BtoR == false) {
			// Verifying route between right point to bottom point -- In case the roads are one-way
			stepsMapInternal = processSingleRoute(startLat, startLng, endLat, endLng);
			hashSize = stepsMapInternal.size();
			if (hashSize == 1) {
				foundProof++;
			}
			waitTime();
		}
		
		if (foundProof < 2) {
			waitTime();
		
			// Verifying route between top point and left point
			Boolean TtoL = false;
			startLat = Double.toString(topPoint);
			startLng = Lng;
			endLat = Lat;
			endLng = Double.toString(leftPoint);
			
			stepsMapInternal = processSingleRoute(startLat, startLng, endLat, endLng);
			hashSize = stepsMapInternal.size();
			if (hashSize == 1) {
				foundProof++;
				TtoL = true;
			}
			
			if (TtoL == false) {
				waitTime();
				// Verifying route between left point to top point -- In case the roads are one-way
				stepsMapInternal = processSingleRoute(startLat, startLng, endLat, endLng);
				hashSize = stepsMapInternal.size();
				if (hashSize == 1) {
					foundProof++;
				}
			}
			
			if (foundProof < 2) {
				waitTime();
			
				// Verifying route between top point and right point
				Boolean TtoR = false;
				startLat = Double.toString(topPoint);
				startLng = Lng;
				endLat = Lat;
				endLng = Double.toString(rightPoint);
				
				stepsMapInternal = processSingleRoute(startLat, startLng, endLat, endLng);
				hashSize = stepsMapInternal.size();
				if (hashSize == 1) {
					foundProof++;
					TtoR = true;
				}
				
				if (TtoR == false) {
					waitTime();
				
				// Verifying route between right point to top point -- In case the roads are one-way
				// System.out.println("Route between right point and top point:");
					stepsMapInternal = processSingleRoute(startLat, startLng, endLat, endLng);
					hashSize = stepsMapInternal.size();
				
					if (hashSize == 1) {
						foundProof++;
					}
				}
			}
		}
		if (street.equals("nothing")) {
			writer.write("0\t");
			writer.write(Lat + "\t");
			writer.write(Lng + "\t");
			writer.write(direction + "\t");
			if (foundProof == 2) {
				// Writing confidence level as 1
				writer.write("1\n");
				globalMap.put(Lat + "|" + Lng, "1");
			} else {
			// Writing confidence level as 0.25 as it still could be an intersection
			// Two side one ways cannot be identified using this method
				writer.write("0.25\n");
				globalMap.put(Lat + "|" + Lng, "0.25");
			}
			writer.flush();
		} else {
			String curStreet = null;
			String res = FetchStreetAddress(Lat, Lng);
			String address = JSONParser.parseStreetAddress(res);
			String[] addrParts = address.split(",");
			String pattern = "[0-9]+ (.*)";
			Pattern regex = Pattern.compile(pattern);
			Matcher match = regex.matcher(addrParts[0]);
			if (match.find()) {
				curStreet = match.group(1);
			}
			Boolean foundMatch = false;
			
			String[] streetParts = street.split("::");
			int strLen = streetParts.length;
			int i;
			for (i = 0; i < strLen; i++) {
				if (curStreet.equals(streetParts[i])) {
					foundMatch = true;
					System.out.println("Found match :" + streetParts[i]);
				}
			}
			if (foundMatch == true) {
				writer.write("0\t");
				writer.write(Lat + "\t");
				writer.write(Lng + "\t");
				writer.write(direction + "\t");
				if (foundProof == 2) {
					writer.write("1\n");
					globalMap.put(Lat + "|" + Lng, "1");
				} else {
					writer.write("0.25\n");
					globalMap.put(Lat + "|" + Lng, "0.25");
				}
				writer.flush();
			}
		}
		
		return globalMap;
	}

	public static String FetchHTTPResponse(String startLat, String startLng, String endLat, String endLng) {
		/*
		* Function description: 
		* 		Arguments: 
		* 			1. Start point lat and long 
		* 			2. End point lat and long 
		* 		Return value: JSON string for the HTTP query
		* 
		* Taking given start & end lat and long, generate HTTP query for the
		* route. Do a GET query for the generated URL. Return the JSON response
		* obtained for the query.
		*/

		// Google Maps request URL
		String Request = "http://maps.googleapis.com/maps/api/directions/json?origin=%28" + startLat + ","
				+ startLng + "%29&destination=%28" + endLat	+ "," + endLng + "%29&sensor=false";

		//System.out.println("URL: " + Request);
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(Request);
		HttpResponse response = null;
		
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Get the response with JSON output
		BufferedReader rd = null;
		try {
			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String line = "";
		String res = "";
		// Concatenating the response into a single string
		try {
			while ((line = rd.readLine()) != null) {
				res = res.concat(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	return res;
	}
	
	public static String FetchStreetAddress(String lat, String lng) {
		/*
		* Function description: 
		* 		Arguments: 
		* 			1. Latitude 
		* 			2. Longitude 
		* 		Return value: JSON string for the HTTP query for reverse geocoding
		* 
		* Taking given lat and long, generate HTTP query for the reverse Geocoding process
		* (obtain street name from lat/long values).
		* Do a GET query for the generated URL. 
		* Return the JSON response obtained for the query.
		*/

		// Google Maps request URL
		String Request = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
				+ lat + "," + lng + "&sensor=false";
		
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(Request);
		HttpResponse response = null;
		
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Get the response with JSON output
		BufferedReader rd = null;
		try {
			rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String line = "";
		String res = "";
		// Concatenating the response into a single string
		try {
			while ((line = rd.readLine()) != null) {
				res = res.concat(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
}

