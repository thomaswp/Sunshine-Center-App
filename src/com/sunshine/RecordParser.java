package com.sunshine;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sunshine.Record.Header;
import com.sunshine.Record.Section;

//Parses records
public class RecordParser implements ContentHandler {

	//Current elements as we work through the XML file
	private Record currentRecord;
	private Section currentSection;
	private Header currentHeader;
	private StringBuilder currentBody;
	
	//Does the current question body contain HTML?
	private boolean bodyContainsHTML;
	//The attributes of the current element
	private Attributes currentAttributes;
	
	public Record getRecord() {
		return currentRecord;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		//Create a new representation for the current element
		//and add it to its parent
		if (Record.isRecord(qName)) {
			currentRecord = new Record(atts);
		} else if (Section.isSection(qName)) {
			currentSection = new Section(atts);
			currentRecord.add(currentSection);
		} else if (Header.isHeader(qName)) {
			currentHeader = new Header(atts);
			currentSection.add(currentHeader);
		} else if (Header.isHeaderElement(qName)) {
			//If we're in a <q> or <an>, we need a new
			//StringBuilder to hold the body
			currentBody = new StringBuilder();
			currentAttributes = new AttributesImpl(atts);
		} else if (currentBody != null){
			//If we have an element inside of a <q> or <an>
			//we need to rewrite it as body text instead
			
			currentBody.append("<" + qName);
			for (int i = 0; i < atts.getLength(); i++) {
				currentBody.append(String.format(
						" %s=\"%s\"",
						atts.getQName(i),
						atts.getValue(i)));
			}
			currentBody.append(">");
			bodyContainsHTML = true;
		}
	}
	

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (Header.isHeaderElement(qName)) {
			//When we finish a header's element, add it to the header
			String body = currentBody.toString();
			body = body.replace("\n", " ").replace("\t", "");
			currentHeader.addElement(qName, currentAttributes, 
					body, bodyContainsHTML);
			currentBody = null;
			bodyContainsHTML = false;
		} else if (currentBody != null){
			//Otherwise if we're still inside a header's element
			//We need to write the closing tag to the HTML in the body
			currentBody.append("</" + qName + ">");
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (currentBody != null) {
			//add the body to the StringBuilder
			currentBody.append(ch, start, length);
		}
		
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}

}
