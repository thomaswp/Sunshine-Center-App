package com.sunshine;

import java.io.InputStream;
import java.util.HashMap;

import android.content.res.AssetManager;
import android.util.Xml;

//Holds all the records, so we only have to parse them once
public class RecordCache {
	
	public final static String[] RECORDS = new String[] {
		"seekers.xml",
		"holders.xml",
		"laws.xml"
	}; 
	
	private static HashMap<String, Record> recordCache = new HashMap<String, Record>();
	
	public static Record parseRector(String fileName, AssetManager assets) {
		if (recordCache.containsKey(fileName)) {
			return recordCache.get(fileName);
		} else {
			try {
				InputStream is = assets.open(fileName);
				RecordParser parser = new RecordParser();
				Xml.parse(is, Xml.Encoding.UTF_8, parser);
				Record record = parser.getRecord();
				recordCache.put(fileName, record);
				return record;
				
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
