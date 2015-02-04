package com.sg.visionadapter;

import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

public class PMUtils {

	public static String serialize(Map<String, Object> data) {
		return JSON.serialize(new BasicDBObject(data));
	}
}
