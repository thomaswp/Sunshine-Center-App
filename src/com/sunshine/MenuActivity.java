package com.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
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

/**
 * Main menu activity
 * @author Thomas Price
 *
 */
public class MenuActivity extends Activity {
	//Darker yellow
	public final static int BACKGROUND_DARK = Color.argb(255, 235, 217, 135);
	//Lighter yellow
	public final static int BACKGROUND_LIGHT = Color.argb(255, 252, 247, 217);
	//Dark blue
	public final static int HIGHLIGHT = Color.argb(255, 7, 45, 92);
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        LinearLayout linearLayoutMain = ((LinearLayout)findViewById(R.id.linearLayoutMain));
        linearLayoutMain.setBackgroundColor(BACKGROUND_DARK);
        
        
        //Set up button Actions
        
        Button buttonSeekers =( Button)findViewById(R.id.buttonSeekers);
        //Add the mini-text underneath
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
        //Add the mini-text underneath
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
        
        //Set up social bar buttons
        
        //Call
        ((ImageView)findViewById(R.id.imageViewPhone)).setOnClickListener(
        		new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(
						"tel:3362785506"));
				startActivity(intent);
			}
		});
        
        //Email
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
        
        //Facebook
        ((ImageView)findViewById(R.id.imageViewFacebook)).setOnClickListener(
        		new OnClickListener() {
			@Override
			public void onClick(View v) {
				openWebpage("http://www.facebook.com/pages/" +
						"Sunshine-Center-of-North-Carolina-Open-Government-Coalition/" +
						"10150100833975217");
			}
        });
        
        //Twitter
        ((ImageView)findViewById(R.id.imageViewTwitter)).setOnClickListener(
        		new OnClickListener() {
			@Override
			public void onClick(View v) {
				openWebpage("http://twitter.com/NCOpenGov");
			}
        });
        
        //Website
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