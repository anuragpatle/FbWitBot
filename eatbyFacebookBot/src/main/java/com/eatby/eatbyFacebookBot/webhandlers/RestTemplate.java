//Copyright Udayan Majumdar, Shoeb Ahmed 2015

package com.eatby.eatbyFacebookBot.webhandlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

//import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.SSLSession;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//import android.util.Log;

//This class would make a rest request
//Inputs would be key value pair
//Output would be JSON response
public class RestTemplate {
	
	private String webserviceURL;
	private int serverResponseCode; // To store server response code. 
	private enum RequestType {
		GET, 
		POST;
	}
	private RequestType requestType;
	private HttpURLConnection connection = null;
	HashMap<String, String> inputParams = new HashMap<String, String>();
	HashMap<String, String> headerParams = new HashMap<String, String>();
	public RestTemplate(String webserviceURL){
		this.webserviceURL = webserviceURL;
	}
	
	public int getServerResponseCode() {
		return serverResponseCode;
	}
	
	//Makes a GET Rest request
	public String getMessageResponse(HashMap<String,String> inputParams, HashMap<String, String> headerParams){
		String jsonResponseString = null;
		try{
			this.inputParams = inputParams;
			this.headerParams = headerParams;
			requestType = RequestType.GET;
			getConnection();				
			setRequestProperties();
			//sendRequest("");
			connection.connect();
			//int i = connection.getResponseCode();
			//System.out.println("Server REST Response code : " + Integer.toString(i) );
			serverResponseCode = connection.getResponseCode();
			jsonResponseString = getResponse();
			//jsonResponseString = jsonResponseString.replaceAll("\\\\", "");
			//System.out.println("Server JSON Response Message: "+ jsonResponseString);
			//logResponseToFile(jsonResponseString);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally {
		      if(connection != null) {
		    	  connection.disconnect();
		      }
		}
		
		return jsonResponseString;
	}

	//Makes a POST Rest request
	public String postMessageResponse(HashMap<String,String> inputParams, HashMap<String, String> headerParams){
		String jsonResponseString = null;
		try{
			this.inputParams = inputParams;
			this.headerParams = headerParams;
			requestType = RequestType.POST;
			getConnection();				
			setRequestProperties();
			sendRequest();
			connection.connect();
			int i = connection.getResponseCode();
			System.out.println("Server REST Response code : " + Integer.toString(i) );
			if ( i != 200) {
				System.out.println("ERROR: Some problem with Rest Call");
			}
			jsonResponseString = getResponse();
			//jsonResponseString = jsonResponseString.replaceAll("\\\\", "");
			//System.out.println("Server JSON Response Message: "+ jsonResponseString);
			logResponseToFile(jsonResponseString);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally {
		      if(connection != null) {
		    	  connection.disconnect();
		      }
		}
		
		return jsonResponseString;
	}
	
	private void getConnection(){
		URL url;
		try {
			String urlString = this.webserviceURL;
			if ( requestType == RequestType.GET) {
				urlString += encodeURL();
			}
			System.out.println("REQUEST URL: "+ urlString);
			url = new URL(urlString);
			connection = (HttpURLConnection)url.openConnection();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	//Set the Headers and type of request
	//The headers are generally static for a request such as for Zomato
	private void setRequestProperties(){
		try {
			connection.setRequestMethod(requestType.name());
			if ( requestType == RequestType.GET ) {
				connection.setRequestProperty("Accept","application/json; charset=UTF-8"); //Added for Neo4J
				connection.setRequestProperty("Content-Type","application/json");
			}
			
			//connection.setRequestProperty("user-key", "251411cf01ef67fae87972fa3260aff0");
			if (headerParams != null) {
				Iterator itr = headerParams.entrySet().iterator();
				while(itr.hasNext())
				{
					Map.Entry m = (Map.Entry)itr.next();
					connection.setRequestProperty(m.getKey().toString(), m.getValue().toString());
				}
			}

			
			//connection.setRequestProperty("Content-Language", "en-US");  
			connection.setUseCaches (false);
			connection.setDoInput(true);
			connection.setDoOutput(true);
	    
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	
	//Not required for GET calls. Required for POST Calls
	private void sendRequest(){
		//Send request
		String jsonRequestMessage = encodeURL();
	    DataOutputStream wr = null;
		try {
			OutputStream os = connection.getOutputStream ();
			wr = new DataOutputStream(os);
			wr.writeBytes (jsonRequestMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if( wr != null){
				try {
					wr.flush ();
					wr.close ();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private String getResponse(){
		 //Get Response	
		InputStream is = null;
		BufferedReader rd = null;
		StringBuffer response = new StringBuffer();

		try {
		
			is = connection.getInputStream();
			rd = new BufferedReader(new InputStreamReader(is));
			String line;
			
			while((line = rd.readLine()) != null) {
				response.append(line);
			}
			 
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			 try {
				 if( rd != null){
					 rd.close(); 
				 }
				 if( is != null){
					 is.close();
				 }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return response.toString();
	}

	//the request params are passed as Key-value pairs
	//Convert the key-value pairs to request URL
	private String encodeURL()
	{
		String response = "";
		try
		{
			if (inputParams != null) {
				Iterator itr = inputParams.entrySet().iterator();
				boolean bAnd = false;
				while(itr.hasNext())
				{
					if ( bAnd == true)  response += "&";
					Map.Entry m = (Map.Entry)itr.next();
					//remove if any & exists in value
					System.out.println("Key:" + m.getKey() + " Value:" + m.getValue() );
					response += m.getKey() + "=" + m.getValue().toString().replace("&", "");
					bAnd = true;
				}
				//response = URLEncoder.encode(response, "UTF-8");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return response;
		
	}
	
	private void logResponseToFile(String sJsonResponse) {
		try {

			String path = RestTemplate.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String decodedPath = URLDecoder.decode(path, "UTF-8");
			String sFileName =  decodedPath + System.currentTimeMillis() + ".log";
			System.out.println("\nWriting JSON response to file " + sFileName);
			File file = new File(sFileName);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(sJsonResponse);
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
