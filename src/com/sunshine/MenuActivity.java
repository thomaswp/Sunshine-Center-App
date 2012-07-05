package com.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class MenuActivity extends Activity {
	public final static int BACKGROUND = Color.argb(255, 235, 217, 135);
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ((LinearLayout)findViewById(R.id.linearLayoutMain)).setBackgroundColor(BACKGROUND);
        
        ((Button)findViewById(R.id.buttonSeekers)).setOnClickListener(
        		new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchRecord("seekers.xml");
			}
		});
    }
    
    private void launchRecord(String name) {
    	Intent intent = new Intent(this, RecordActivity.class);
    	intent.putExtra("record", "seekers.xml");
    	startActivity(intent);
    }
}