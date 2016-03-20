package dv.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

public class PassmanTransport {
	public List<String[]> readConfig() {
		int i;
		List<String[]> config = new ArrayList<String[]>();
		String configSrc = "";
		
        try {
			InputStream fin = getClass().getResourceAsStream("/dv/utils/config.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
			String line;
			while ((line = reader.readLine()) != null) {
			   String[] pair = line.split(" ");
			   config.add(new String[] {pair[0], pair[1]});
			}
			reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
		return config;
	}
	
	public String[] readFromFile(String filename) {
		String[] filedata = new String[2];
		int i;
		
        try {
			File fin = new File(filename);
			if (fin.exists()) {
				byte[] bytes = new byte[(int) fin.length()];
				FileInputStream inStr = new FileInputStream(fin);
				inStr.read(bytes);
				inStr.close();
				
				String[] lines = new String(bytes).split("\n");
				filedata[0] = lines[1]; // the IV
				lines[0] = "";
				lines[1] = "";

				filedata[1] = String.join("", lines); // the PEM source
			}

        } catch (Exception ex) { }
		
		return filedata;
	}
	
	public void writeToFile(String filename, String initVector, String pemCode) {
		int i;
		String strOut = "";
		File fout = new File(filename);
		
        if (!fout.exists()) {
            try {
                fout.createNewFile();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
		
        try {
			String[] chunks = pemCode.split("(?<=\\G.{64})");
			
            FileOutputStream outStr = new FileOutputStream(fout);
            outStr.write(("# Passwords' manager storage file. Don't edit it!\n" + initVector + "\n" + String.join("\n", chunks)).getBytes("UTF-8"));
            outStr.close();
        } catch (Exception ex) { }
	}
	
	public String[] readFromFirebase(String url, String user, String key) {
		String[] urldata = new String[2];
		String response = "";
		
		try {
			URL fbUrl = new URL(url + user + ".json?auth=" + key);
			HttpURLConnection connection = (HttpURLConnection) fbUrl.openConnection();
			connection.setRequestMethod("GET");
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
			   response += line;
			}
			reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		String jsonString = "\"iv\"\\:\"([^\\:]*)\",\"pem\"\\:\"([^\\:]*)\"";
		Pattern jsonPattern = Pattern.compile(jsonString);
		Matcher jsonMatcher = jsonPattern.matcher(response);

		if (jsonMatcher.find( )) {
			urldata[0] = jsonMatcher.group(1);
			urldata[1] = jsonMatcher.group(2);
		} else {
			urldata[0] = "";
			urldata[1] = "";
		}
		
		return urldata;
	}
	
	public void writeToFirebase(String url, String user, String key, String IV, String PEM) {
		HttpURLConnection connection = null;
		String response = "";
		String urlData = "{\"" + user + "\":{\"iv\":\"" + IV + "\",\"pem\":\"" + PEM + "\"}}";
		
		try {
			URL fbUrl = new URL(url + ".json?auth=" + key);
			connection = (HttpURLConnection)fbUrl.openConnection();
			connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
			connection.setRequestMethod("POST");
			
			connection.setRequestProperty("Content-Length", 
			Integer.toString(urlData.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");  
			
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			
			DataOutputStream strOut = new DataOutputStream (
			connection.getOutputStream());
			strOut.writeBytes(urlData);
			strOut.close();
			
			InputStream inStr = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(inStr));
			String line;
			while((line = rd.readLine()) != null) {
				response += line;
			}
			rd.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if(connection != null) {
				connection.disconnect(); 
			}
		}	
	}
}