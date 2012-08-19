package com.sunshine;

import java.util.regex.Pattern;

import com.sunshine.Record.Header;
import com.sunshine.Record.Question;
import com.sunshine.Record.Section;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SearchActivity extends Activity {
	
	CheckBox checkBoxQuestions, checkBoxAnswers;
	
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
		
		String q = ((EditText)findViewById(R.id.editTextSearch)).getText().toString();
		
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
			patternS += Pattern.quote(word);
			if (i < words.length - 1) patternS += "|";
		}
		Pattern pattern = Pattern.compile(patternS, Pattern.CASE_INSENSITIVE);
		
		for (String recordS : records) {
			Record record = RecordCache.parseRector(recordS, getAssets());
			for (Section section : record) {
				for (Header header : section) {
					for (Question question : header) {
						if (searchQs && 
								pattern.matcher(question.question).find()) {
							h.add(question);
						} else if (searchAs &&
								pattern.matcher(question.answer).find()) {
							h.add(question);
						}
					}
				}
			}
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
			intent.putExtra("path", title);
			intent.putExtra("pattern", patternS);
			startActivity(intent);
		}
	}
}
