package com.sunshine;

import java.io.InputStream;

import com.sunshine.Record.Header;
import com.sunshine.Record.Section;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class RecordActivity extends Activity {
	Record record;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String r = getIntent().getExtras().getString("record");
		try {
			InputStream is = getAssets().open(r);
			RecordParser parser = new RecordParser();
			Xml.parse(is, Xml.Encoding.UTF_8, parser);
			record = parser.getRecord();
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		createContent();
	}
	
	private void createContent() {
		
		LinearLayout host = new LinearLayout(this);
		host.setOrientation(LinearLayout.VERTICAL);
		host.setBackgroundColor(MenuActivity.BACKGROUND);
		
		ScrollView sv = new ScrollView(this);
		LinearLayout layout = new LinearLayout(this);
		layout.setPadding(20, 10, 20, 10);
		layout.setOrientation(LinearLayout.VERTICAL);
		sv.addView(layout);
		
		int WRAP = LayoutParams.WRAP_CONTENT;
		int MATCH = LayoutParams.MATCH_PARENT;
		
		for (Section section : record) {
			LinearLayout secLayout = new LinearLayout(this);
			secLayout.setOrientation(LinearLayout.VERTICAL);
			
			TextView tv = new TextView(this);
			tv.setText(section.title);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
			tv.setTextColor(Color.BLACK);
			LayoutParams lps = new LayoutParams(WRAP, WRAP);
			lps.gravity = Gravity.CENTER_HORIZONTAL;
			lps.bottomMargin = toPx(15);
			lps.topMargin = toPx(10);
			secLayout.addView(tv, lps);
			
			for (Header header : section) {
				Button button = new Button(this);
				button.setText(header.title);
				button.setPadding(20, 20, 20, 20);
				button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
				
				final Header fHeader = header;
				button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(RecordActivity.this, QnAActivity.class);
						intent.putExtra("header", fHeader);
						startActivity(intent);
					}
				});
				
				lps = new LayoutParams(MATCH, WRAP);
				lps.bottomMargin = toPx(15);
				secLayout.addView(button, lps);
			}
			
			layout.addView(secLayout);
			
		}
		
		host.addView(sv);
		setContentView(host, new LayoutParams(MATCH, MATCH));
	}
	
	private int toPx(int dip) {
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, 
				getResources().getDisplayMetrics());
	}
}
