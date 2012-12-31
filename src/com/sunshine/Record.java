package com.sunshine;

import java.io.Serializable;
import java.util.Iterator;
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
	
	//Should be unnecessary now that html displays in utf-8
	public static String removeSpecialChars(String s) {
		if (s == null) return null;
		return s;
//		return s.replace("”", "\"")
//		.replace("“", "\"")
//		.replace("’", "'")
//		.replace("‘", "'")
//		.replace("–", "-")
//		.replace("-", "-");
	}
	
	public static class Section extends LinkedList<Record.Header> {
		private static final long serialVersionUID = 1L;
		
		public String title;
		
		public Section(Attributes atts) {
			this.title = removeSpecialChars(atts.getValue("title"));
		}
		
		public static boolean isSection(String qName) {
			return "section".equalsIgnoreCase(qName);
		}
	}
	
	public static class Header implements Serializable, Iterable<Question> {
		private static final long serialVersionUID = 1L;
	
		public LinkedList<Question> questions = new LinkedList<Record.Question>();
	
		public String title;
		public String tip;
		
		public Header(Attributes atts) {
			this.title = removeSpecialChars(atts.getValue("title"));
			this.tip = removeSpecialChars(atts.getValue("tip"));
		}
		
		public Header(String title) {
			this.title = title;
		}
		
		public Question get(int index) {
			return questions.get(index);
		}
		
		public int size() {
			return questions.size();
		}
		
		public void add(Question question) {
			questions.add(question);
		}

		@Override
		public Iterator<Question> iterator() {
			return questions.iterator();
		}
		
		
		public void addElement(String qName, Attributes atts, String body, boolean containsHTML) {
			if (isQuestion(qName)) {
				add(new Question(this, removeSpecialChars(body), atts));
			} else if (isAnswer(qName)) {
				get(size() - 1).answer = removeSpecialChars(body);
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
		public String question, answer, anchor;
		public boolean containsHTML;
		public Header parent;
		
		public Question(Header parent, String question, Attributes atts) {
			this.parent = parent;
			this.question = question;
			this.answer = "";
			this.anchor = atts.getValue("anchor");
		}
		
	}
}
