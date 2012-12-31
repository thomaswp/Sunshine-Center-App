package com.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MenuActivity extends Activity {
	public final static int BACKGROUND_DARK = Color.argb(255, 235, 217, 135);
	public final static int BACKGROUND_LIGHT = Color.argb(255, 252, 247, 217);
	public final static int HIGHLIGHT = Color.argb(255, 7, 45, 92);
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        LinearLayout linearLayoutMain = ((LinearLayout)findViewById(R.id.linearLayoutMain));
        linearLayoutMain.setBackgroundColor(BACKGROUND_DARK);
        
        
        Button buttonSeekers =( Button)findViewById(R.id.buttonSeekers);
        buttonSeekers.setText(Html.fromHtml(
        		"Seekers<br /><small><small><i>of Information</i></small></small>"));
        buttonSeekers.setOnClickListener(
        		new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchRecord("seekers.xml");
			}
		});
        
        Button buttonHolders = (Button)findViewById(R.id.buttonHolders);
        buttonHolders.setText(Html.fromHtml(
        		"Holders<br /><small><small><i>of Information</i></small></small>"));
        buttonHolders.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchRecord("holders.xml");
			}
		});
        
        Button buttonLaws = (Button)findViewById(R.id.buttonLaws);
        buttonLaws.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchRecord("laws.xml");
			}
		});
        
        ImageButton buttonSearch = (ImageButton)findViewById(R.id.buttonSearch);
        buttonSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				search();
			}
		});
        
        new WebView(this); //possibly speeds up the first load of webviews
        
        ((ImageView)findViewById(R.id.imageViewPhone)).setOnClickListener(
        		new OnClickListener() {
			@Override
			public void onClick(View v) {
				highlight(v);
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(
						"tel:3362785506"));
				startActivity(intent);
			}
		});
        
        ((ImageView)findViewById(R.id.imageViewEmail)).setOnClickListener(
        		new OnClickListener() {
			@Override
			public void onClick(View v) {

		        Intent intent = new Intent(Intent.ACTION_VIEW);
		        Uri data = Uri.parse(
		        		"mailto:ncopengov@elon.edu?subject=Question for the Sunshine Center&body=" + 
		        		"Hello,\n\nI am contacting the Sunshine Center of NC because...");
		        intent.setData(data);
		        startActivity(intent);
			}
		});
        
        
        ((ImageView)findViewById(R.id.imageViewFacebook)).setOnClickListener(
        		new OnClickListener() {
			@Override
			public void onClick(View v) {
				openWebpage("http://www.facebook.com/pages/" +
						"Sunshine-Center-of-North-Carolina-Open-Government-Coalition/" +
						"10150100833975217");
			}
        });
        
        ((ImageView)findViewById(R.id.imageViewTwitter)).setOnClickListener(
        		new OnClickListener() {
			@Override
			public void onClick(View v) {
				openWebpage("http://twitter.com/NCOpenGov");
			}
        });
        
        ((ImageView)findViewById(R.id.imageViewSunshine)).setOnClickListener(
        		new OnClickListener() {
			@Override
			public void onClick(View v) {
				openWebpage("http://www.elon.edu/e-web/academics/" +
						"communications/ncopengov/");
			}
        });
    }
    
    private void openWebpage(String url) {
    	Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse(url);
        intent.setData(data);
        startActivity(intent);
    }
    
    private void highlight(View v) {
//    	final TransitionDrawable td = new TransitionDrawable(new Drawable[] {
//    			new ColorDrawable(Color.argb(0, 0, 0, 0)),
//    			getResources().getDrawable(R.drawable.highlight)
//    			//new ColorDrawable(Color.argb(0, 0, 0, 0))
//    	});
//    	v.setBackgroundDrawable(td);
//    	final int time = 150;
//    	td.startTransition(time);
//    	v.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				td.reverseTransition(time);
//			}
//		}, time);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add("Search")
    	.setIcon(android.R.drawable.ic_menu_search)
    	.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				search();
				return false;
			}
		});
    	return true;
    }
    
    @Override
    public boolean onSearchRequested() {
    	search();
    	return true;
    }
    
    private void search() {
    	Intent intent = new Intent(MenuActivity.this, 
				SearchActivity.class);
		startActivity(intent);
    }
    
    private void launchRecord(String name) {
    	Intent intent = new Intent(this, RecordActivity.class);
    	intent.putExtra("record", name);
    	startActivity(intent);
    }
}