package com.sunshine;

import java.io.InputStream;
import java.util.HashMap;

import android.content.res.AssetManager;
import android.util.Xml;

public class RecordCache {
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
