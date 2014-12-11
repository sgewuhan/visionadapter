package com.sg.visionconnector;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Connector {
	
	protected final String userId;
	
	protected final String password;

	protected final String data;

	protected final String type;
	
	public static final String PART_DOCUMENT = "partdocument";

	public Connector(String userId,String password,String data,String type){
		this.userId = userId;
		this.password = password;
		this.data = data;
		this.type = type;
	}

	public String getConnectorHandler() {
		String s = new String();
		s += "\n";
		s += "u"+userId;
		s += "\n";
		s += "p"+password;
		s += "\n";
		s += "d"+data;
		s += "\n";
		s += "t"+type;
		s += "\n";
		s += "o"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		s += "\n";
		s += "sperspective.documentviewer";
		
		try {
			return Coder.encryptBASE64(s.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		String handler = new Connector("zhonghua", "1", "ssss10", PART_DOCUMENT).getConnectorHandler();
		System.out.println(handler);
	}
	
	

}
