package com.sunshine;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sunshine.Record.Header;
import com.sunshine.Record.Question;
import com.sunshine.Record.Section;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SearchActivity extends Activity {
	
	private final static int MAX_RESULTS = 15;
	private CheckBox checkBoxQuestions, checkBoxAnswers;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_activity);
		
		((LinearLayout)findViewById(R.id.linearLayoutMain)).setBackgroundColor(
				MenuActivity.BACKGROUND_DARK);
		
		((Button)findViewById(R.id.buttonSearch)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				search();
			}
		});
		
		checkBoxQuestions = (CheckBox)findViewById(R.id.checkBoxQuestions);
		checkBoxAnswers = (CheckBox)findViewById(R.id.checkBoxAnswers);
	}
	
	private void search() {
		boolean searchQs = checkBoxQuestions.isChecked();
		boolean searchAs = checkBoxAnswers.isChecked();
		
		if (!searchQs && !searchAs) {
			return;
		}
		
		String q = ((EditText)findViewById(R.id.editTextSearch)).getText().toString().toLowerCase();
		
		if (q.length() == 0) return;
		
		String[] records = new String[] {
				"seekers.xml",
				"holders.xml",
				"laws.xml"
		};
		
		String title = "Search: '" + q + "'";
		Header h = new Header(title);
		
		String[] words = q.split(" "); 
		if (words.length < 1) return;
		String patternS = "";
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			patternS += "\\b" + Pattern.quote(word) + "\\b";
			if (i < words.length - 1) patternS += "|";
		}
		Pattern pattern = Pattern.compile(patternS, Pattern.CASE_INSENSITIVE);
		
		final HashMap<Question, Integer> map = new HashMap<Record.Question, Integer>();
		
		for (String recordS : records) {
			Record record = RecordCache.parseRector(recordS, getAssets());
			for (Section section : record) {
				for (Header header : section) {
					for (Question question : header) {
						int count = 0;
						if (searchQs) {
							Matcher matcherQs = pattern.matcher(question.question);
							while (matcherQs.find()) {
								count += 3;
							}
							String qLower = question.question.toLowerCase();
							int index = qLower.indexOf(q);
							while (index >= 0) {
								count += 30;
								index += q.length();
								index = qLower.indexOf(q, index);
							}
						}
						if (searchAs) {
							Matcher matcherAs = pattern.matcher(question.answer);
							while (matcherAs.find()) {
								count++;
								if (words.length > 0 && matcherAs.group().equalsIgnoreCase(q.trim())) {
									count += 10;
								}
							}
							String qLower = question.answer.toLowerCase();
							int index = qLower.indexOf(q);
							while (index >= 0) {
								count += 10;
								index += q.length();
								index = qLower.indexOf(q, index);
							}
						}
						if (count > 0) {
							h.add(question);
							map.put(question, count);
						}
					}
				}
			}
		}
		
		
		Collections.sort(h.questions, new Comparator<Question>() {
			@Override
			public int compare(Question lhs, Question rhs) {
				return map.get(rhs) - map.get(lhs);
			}
		});
		
		while (h.size() > MAX_RESULTS) {
			h.questions.removeLast();
		}
		
		if (h.size() < 1) {
			new AlertDialog.Builder(this)
			.setTitle("No Results")
			.setMessage("No results were found for '" + q + "'.")
			.setPositiveButton("Ok", null)
			.show();
		} else {
			Intent intent = new Intent(this, QnAActivity.class);
			intent.putExtra("header", h);
			intent.putExtra("pattern", patternS);
			startActivity(intent);
		}
	}
}
