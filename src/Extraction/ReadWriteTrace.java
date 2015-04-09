package Extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

public class ReadWriteTrace {
	
	/*given a file path, read the traces and return*/
	static public List<Trace> readFile(String path) {
		int dim = 3;
		return readFile(path, dim);
	}

	static public List<Trace> readFile(String filePath, int dim)
	{
		List<Trace> data = new ArrayList<Trace>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(filePath);
			br = new BufferedReader(fr); 
			String line; 
			int index = 0;
			while((line = br.readLine()) != null) { 
				Trace trace = new Trace(dim);
				trace.setTraceIndex(index++);
				trace.getTrace(line);
				data.add(trace);
			} 
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data;  
	}
	
	public static void createFolder(String folder) {
		File dir = new File(folder);
		if (!dir.exists()) {
			System.out.println("creating folder: " + folder);
		    boolean result = dir.mkdirs();
		    if(!result){    
		       System.out.println("create folder failed: " + folder);  
		     }  
		}
	}
	
	/*write traces into a file*/
	public static void writeFile(List<Trace> traces, String filePath) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filePath));
			for (Trace tr: traces) {
				String line = tr.toString() + "\n";
				bw.write(line);	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	

	

	static public double[][] readFeedback(String filePath, int rounds, int passes)
	{
		FileReader fr = null;
		BufferedReader br = null;
		double [][]data = new double[rounds][passes];
		int counter = 0;
		try {
			fr = new FileReader(filePath);
			br = new BufferedReader(fr); 
			String line; 
			while((line = br.readLine()) != null) { 
				String [] part = line.split("\t");
			//	Log.log(line);
				for(int i = 0; i < part.length; ++i) {
					//Log.log(part[i]);
					data[counter][i] = Double.parseDouble(part[i]);
				}
				counter++;
			} 
			fr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data;  
	}
	

}

