package com.sunshine;

import java.io.InputStream;

import com.sunshine.Record.Header;
import com.sunshine.Record.Section;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
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
		record = RecordCache.parseRector(r, getAssets());
		
		if (record == null) {
			new AlertDialog.Builder(this)
			.setTitle("Error")
			.setMessage("We're sorry, but an error has occured. Please try again later")
			.setPositiveButton("Ok", new Dialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			})
			.show();
		} else {
			createContent();
		}
	}
	
	private void createContent() {
		int id = 1;
		
		LinearLayout host = new LinearLayout(this);
		host.setOrientation(LinearLayout.VERTICAL);
		host.setBackgroundColor(MenuActivity.BACKGROUND_DARK);
		host.setId(id++);
		
		ScrollView sv = new ScrollView(this);
		sv.setId(id++);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setPadding(20, 10, 20, 10);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setId(id++);
		sv.addView(layout);
		
		int WRAP = LayoutParams.WRAP_CONTENT;
		int MATCH = LayoutParams.MATCH_PARENT;
		
		for (Section section : record) {
			LinearLayout secLayout = new LinearLayout(this);
			secLayout.setOrientation(LinearLayout.VERTICAL);
			secLayout.setId(id++);
			
			TextView tv = new TextView(this);
			tv.setText(section.title);
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
			tv.setTextColor(Color.BLACK);
			tv.setId(id++);
			LayoutParams lps = new LayoutParams(WRAP, WRAP);
			lps.gravity = Gravity.CENTER_HORIZONTAL;
			lps.bottomMargin = toPx(15);
			lps.topMargin = toPx(10);
			secLayout.addView(tv, lps);
			
			for (Header header : section) {
				Button button = new Button(this);
				button.setText(header.title);
				button.setPadding(20, 20, 20, 25);
				button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
				button.setId(id++);
				
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
