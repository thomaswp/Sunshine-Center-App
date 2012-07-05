package com.sunshine;

import java.io.Serializable;
import java.util.LinkedList;

import org.xml.sax.Attributes;

public class Record extends LinkedList<Record.Section> {
	private static final long serialVersionUID = 1L;
	
	public String name;
	
	public Record(Attributes atts) {
		this.name = atts.getValue("name");
	}
	
	public static boolean isRecord(String qName) {
		return "record".equalsIgnoreCase(qName);
	}
	
	public static class Section extends LinkedList<Record.Header> {
		private static final long serialVersionUID = 1L;
		
		public String title;
		
		public Section(Attributes atts) {
			this.title = atts.getValue("title");
		}
		
		public static boolean isSection(String qName) {
			return "section".equalsIgnoreCase(qName);
		}
	}
	
	public static class Header extends LinkedList<Question> {
		private static final long serialVersionUID = 1L;
		
		public String title;
		
		public Header(Attributes atts) {
			this.title = atts.getValue("title");
		}
		
		public void addElement(String qName, String body, boolean containsHTML) {
			if (isQuestion(qName)) {
				add(new Question(body));
			} else if (isAnswer(qName)) {
				get(size() - 1).answer = body;
				get(size() - 1).containsHTML = containsHTML;
			}
		}
		
		public static boolean isHeader(String qName) {
			return "header".equalsIgnoreCase(qName);
		}
		
		public static boolean isHeaderElement(String qName) {
			return isQuestion(qName) || isAnswer(qName);
		}
		
		public static boolean isQuestion(String qName) {
			return "q".equalsIgnoreCase(qName);
		}
		
		public static boolean isAnswer(String qName) {
			return "an".equalsIgnoreCase(qName);
		}
		
		public static boolean isListItem(String qName) {
			return  qName.equalsIgnoreCase("li");
		}
	}
	
	public static class Question implements Serializable {
		private static final long serialVersionUID = 1L;
		public String question, answer;
		public boolean containsHTML;
		
		public Question(String question) {
			this.question = question;
			this.answer = "";
		}
		
	}
}
