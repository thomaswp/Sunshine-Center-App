package com.sunshine;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sunshine.Record.Header;
import com.sunshine.Record.Section;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

//Shows a list of questions and answers (a header) in a drop-down style
public class QnAActivity extends Activity {

	protected Header header;
	//Intercept outgoing html links
	protected WebClientIntercept intercept;
	protected ExpandableListView listView;
	protected QnAExpandableListAdapter adapter;
	//A pattern string, in case this is the result of a search
	protected String patternString;
	//The group indicator drawable, if we need to draw it ourselves
	protected StateListDrawable groupIndicator;
	
	//Drawable states for the indicator
	private final static int[] STATE_NORMAL = new int[] { };
	private final static int[] STATE_EXPANDED = new int[] { android.R.attr.state_expanded }; 

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		intercept = new WebClientIntercept();

		Serializable s = getIntent().getExtras().getSerializable("header");
		header = (Header)s;
		if (header == null) {
			//Don't even bother if we have no header
			finish();
			return;
		}
		//will be null unless this is a search result
		patternString = getIntent().getExtras().getString("pattern");

		createContent(savedInstanceState);

		//This was an attempt to find the place in the scrollview
		//ourselves when orientation changed... unsuccessful so far
		if (savedInstanceState != null) {
			int orientation = 
					getResources().getConfiguration().orientation;
			int oldOrientation =
					savedInstanceState.getInt("orientation");

			//Try to find our place?
			if (orientation != oldOrientation) {
				//				int firstPos = savedInstanceState.getInt("firstPos");
				//				if (firstPos > 0) {
				//					listView.collapseGroup(firstPos - 1);
				//				}
				//				listView.setSelection(firstPos);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("orientation", 
				getResources().getConfiguration().orientation);
		if (listView != null) {
			//first visible view index
			outState.putInt("firstPos",
					listView.getFirstVisiblePosition());
		}
	}

	protected void createContent(Bundle savedInstanceState) {
		//Assign ID so orientation changes work
		int id = 1;

		//Main layout
		LinearLayout host = new LinearLayout(this);
		host.setOrientation(LinearLayout.VERTICAL);
		host.setBackgroundColor(MenuActivity.BACKGROUND_DARK);
		host.setId(id++);

		int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;
		int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;

		//Display the tip at the top if there is one
		if (header.tip != null) {
			TextView tip = getHeaderView();
			tip.setId(id++);
			tip.setText(Html.fromHtml(String.format("<i>%s</i>", header.tip)));
			host.addView(tip);
		}


		listView = new ExpandableListView(this);
		listView.setId(id++);
		LayoutParams lps = new LayoutParams(MATCH, MATCH);
		lps.weight = 1;
		host.addView(listView, lps);

		if (patternString != null) {
			//If this is a search, we need to get the group indicator (little triangle
			//that indicated a dropdown) and draw it ourselves because it will not draw
			//properly if we add section header for the search
			try {
				Resources.Theme theme = getTheme();
				TypedValue typedValue = new TypedValue();
				theme.resolveAttribute(android.R.attr.expandableListViewStyle, typedValue , true);
				TypedArray typedArray = theme.obtainStyledAttributes(typedValue.resourceId, new int[] { android.R.attr.groupIndicator });
				groupIndicator = (StateListDrawable)typedArray.getDrawable(0);
				if (groupIndicator == null) throw new RuntimeException("No group indicator");
				listView.setGroupIndicator(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		adapter = new QnAExpandableListAdapter();
		listView.setAdapter(adapter);

		TextView tv = getHeaderView();
		tv.setText(Html.fromHtml("<b>" + header.title + "</b>"));
		tv.setId(id++);
		lps = new LayoutParams(MATCH, WRAP);
		host.addView(tv, lps);

		setContentView(host, new LayoutParams(MATCH, MATCH));

		if (savedInstanceState == null) {
			String question = getIntent().getExtras().getString("question");
			if (question != null) {
				//If we're opening for the first time and
				//something linked here, highlight the question that
				//was linked to
				openQuestion(question);
			}
		}
	}

	//Scrolls to, opens and highlights a question
	protected void openQuestion(String question) {
		for (int i = 0; i < header.size(); i++) {
			if (question.equalsIgnoreCase(header.get(i).anchor)) {
				if (i > 0) {
					//because scrolling to a list item means
					//scrolling to the bottom of the item before,
					//if it's not visible the child has an assumed
					//height=0 even if it doesn't. So we have to 
					//close the child to make its height=0
					listView.collapseGroup(i - 1);
				}

				listView.expandGroup(i);
				listView.setSelection(i);
				View view = adapter.getGroupView(i, true, null, listView);

				//save the padding of the view
				int[] padding = new int[] {
						view.getPaddingLeft(),
						view.getPaddingTop(),
						view.getPaddingRight(),
						view.getPaddingTop()
				};
				
				//Create two layers, one with a highlight color, the other with
				//the default background
				Drawable[] layers = new Drawable[2];
				layers[0] = new ColorDrawable(MenuActivity.BACKGROUND_DARK);
				layers[1] = new ColorDrawable(MenuActivity.HIGHLIGHT);
				
				//Transition between the two
				final TransitionDrawable drawable = new TransitionDrawable(layers);
				view.setBackgroundDrawable(drawable);
				final int time = 300;
				drawable.startTransition(time);
				view.setPadding(padding[0], padding[1], padding[2], padding[3]);
				
				//Set a handler to fade the drawable back when it's done
				final Handler h = new Handler();
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(time);
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						h.post(new Runnable() {
							@Override
							public void run() {
								drawable.reverseTransition(time);
							}
						});
					}
				});
				thread.start();
			}
		}
	}

	//Creates a nice header view for displaying titles or tips
	protected TextView getHeaderView() {
		TextView tv = new TextView(this);
		tv.setTextSize(14);
		tv.setGravity(Gravity.CENTER);
		tv.setPadding(5, 5, 5, 5);
		tv.setTextColor(MenuActivity.BACKGROUND_LIGHT);
		tv.setBackgroundColor(MenuActivity.HIGHLIGHT);
		return tv;
	}

	//Converts dip to pixels since most size methods take pixels as an argument
	protected int toPx(int dip) {
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, 
				getResources().getDisplayMetrics());
	}

	//Adapter for the ExpandableListView
	protected class QnAExpandableListAdapter extends BaseExpandableListAdapter {

		//We store the parents and children ourselves
		//Could be optimized to reuse view as the ListView is intented to
		private LinkedList<View> parents, children;
		
		//CSS for webviews
		private String css;
		//Typeface for parent views
		private Typeface typeface;
		//assign ids for orientation changes
		private int id = 100;

		//id of ImageViews with group indicators
		private final int IMAGE_VIEW_ID = 10000;

		public QnAExpandableListAdapter() {
			super();

			//We can't link to CSS so we write it in the header of the html
			//The charset is VERY important, since the XML files have UTF8 characters
			css = "<head><meta charset='utf-8'>" +
					"<style media='screen' type='text/css'>";
			try {
				Scanner sc = new Scanner(getAssets().open("style.css"));
				while (sc.hasNext()) css += sc.nextLine(); 
			} catch (IOException e) {
				e.printStackTrace();
			}
			css += "</style></head>";
			parents = new LinkedList<View>();
			children = new LinkedList<View>();

			//load a nice font for the questions
			typeface = Typeface.createFromAsset(getAssets(), "fonts/tw.ttf"); 

			//Load the children in a thread instead of when they are needed
			//to increate response time when opening a question
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < header.size(); i++) {
						parents.add(generateParentView(i));
						children.add(generateChildView(i, 0));
					}
				}
			});
			thread.run();
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return header.get(groupPosition).answer;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			//All groups (questions) have 1 child (the answer)
			return 1;
		}

		//Generic text view (currently only use as base for parents)
		public TextView getGenericView() {
			// Layout parameters for the ExpandableListView
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

			TextView textView = new TextView(QnAActivity.this);
			textView.setLayoutParams(lp);
			// Center the text vertically
			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
			textView.setMinimumHeight(64);
			textView.setTextColor(Color.BLACK);

			// Set the text starting position
			textView.setPadding(0, 5, 20, 5);
			return textView;
		}

		//Returns an HTML string with any elements of
		//the pattern bolded
		private String highlightString(String text) {
			if (patternString == null) {
				return text;
			}

			Pattern pattern = Pattern.compile(patternString, 
					Pattern.CASE_INSENSITIVE);
			
			Matcher matcher = pattern.matcher(text);
			StringBuffer sb = new StringBuffer(text.length());
			while (matcher.find()) {
				matcher.appendReplacement(sb, "<b>$0</b>");
			}
			matcher.appendTail(sb);
			return sb.toString();
		}

		//Highlights a searched string and returns an HTML CharSequence 
		private CharSequence highlight(String text) {
			if (patternString == null) {
				return text;
			}

			return Html.fromHtml(highlightString(text));
		}

		private View generateChildView(int groupPosition, int childPosition) {
			//We're just going to pretend everything has HTML
			//and display it the same, but to optimize you
			//could handle it differently if it has no HTML
			
			int color = MenuActivity.BACKGROUND_LIGHT;
			int fontSize = 18;
			int margin = toPx(20);

			LinearLayout ll = new LinearLayout(QnAActivity.this);
			ll.setPadding(margin - 5, 5, 5, 5);
			ll.setBackgroundColor(color);
			WebView wv = new WebView(QnAActivity.this);
			String html = highlightString(getChild(
					groupPosition, childPosition).toString());
			//Again, charset is very important here
			wv.loadData(css + html, "text/html; charset=UTF-8", null);
			wv.setHorizontalScrollBarEnabled(false);
			wv.setBackgroundColor(color);
			wv.getSettings().setDefaultFontSize(fontSize);
			wv.setWebViewClient(intercept);
			ll.addView(wv);
			return ll;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
				View convertView, ViewGroup parent) {
			while (children.size() <= groupPosition) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return children.get(groupPosition);
		}
		

		//When groups are expanded or collapsed, we may need to
		//Update the drawable that replaced the indicator
		@Override
		public void onGroupCollapsed(int groupPosition) {
			super.onGroupCollapsed(groupPosition);
			setState(groupPosition, STATE_NORMAL);
		}

		@Override
		public void onGroupExpanded(int groupPosition) {
			super.onGroupExpanded(groupPosition);
			setState(groupPosition, STATE_EXPANDED);
		}
		
		private void setState(int groupPosition, int[] state) {
			ImageView iv = (ImageView)parents.get(groupPosition).findViewById(IMAGE_VIEW_ID);
			Drawable drawable;
			//If we have an ImageView for the indicator and it has a drawable...
			if (iv != null && (drawable = iv.getDrawable()) != null) {
				//Set the appropriate state of the drawable
				
				//Doesn't update properly
				//all the time, but mostly works...
				drawable.setState(state);
			}
		}

		private View generateParentView(int groupPosition) {
			TextView textView = getGenericView();
			textView.setText(highlight(header.get(groupPosition).question));
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
			textView.setTypeface(typeface);
			
			//We add an imageview, either to add padding where the group indicator
			//goes, or to replace it if the is a search result
			
			//We must replace it if this is a search result because if
			//we add headers to the search results, the indicators no longer
			//appear to be vertically centered
			LinearLayout imageLayout = new LinearLayout(QnAActivity.this);
			imageLayout.setOrientation(LinearLayout.HORIZONTAL);
			ImageView iv = new ImageView(QnAActivity.this);
			if (groupIndicator != null) {
				//Only not null if this is a search result
				iv.setImageDrawable(groupIndicator.getConstantState().newDrawable());
			}
			iv.setMinimumWidth(toPx(35));
			iv.setId(IMAGE_VIEW_ID);
			LayoutParams lps = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			lps.gravity = Gravity.CENTER;
			lps.leftMargin = toPx(3);
			lps.rightMargin = toPx(3);
			imageLayout.addView(iv, lps);
			imageLayout.addView(textView);
			imageLayout.setBackgroundColor(MenuActivity.BACKGROUND_DARK);
			
			//If this isn't a search result, or if we failed to find the 
			//group indicator, stop here
			if (patternString == null || groupIndicator == null) {
				return imageLayout;
			}

			//Otherwise add a header for this question, showing what
			//header it came from
			
			//Unless it's the same as the one above, then don't bother
			if (groupPosition > 0 && header.get(groupPosition - 1).parent ==
					header.get(groupPosition).parent) {
				return imageLayout;
			}

			//Add the header
			LinearLayout ll = new LinearLayout(QnAActivity.this);
			ll.setId(id++);
			ll.setOrientation(LinearLayout.VERTICAL);

			TextView headerView = getHeaderView();
			headerView.setText(Html.fromHtml(header.get(groupPosition).parent.title));

			ll.addView(headerView);
			ll.addView(imageLayout);

			return ll;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
				ViewGroup parent) {
			while (parents.size() <= groupPosition) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if (isExpanded) {
				onGroupExpanded(groupPosition);
			} else {
				onGroupCollapsed(groupPosition);
			}

			return parents.get(groupPosition);
		}

		@Override
		public Object getGroup(int groupPosition) {
			return header.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return header.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}

	//Intercepts when the user clicks a link in a WebView
	//incase the link is to another qna
	public class WebClientIntercept extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			//These links start with "record://"
			if (url.startsWith("record:")) {
				try {
					url = url.replace("record://", "");
					String[] sections = url.split("\\.");
					
					//Parse out each part of the link, such as record://recordName.sectionName.questionAnchor
					if (sections.length > 0) {
						String recordName = sections[0] + ".xml";

						if (sections.length == 1) {
							//You could link just to a record
							Intent intent = new Intent(QnAActivity.this, RecordActivity.class);
							intent.putExtra("record", recordName);
							startActivity(intent);
						} else {
							Record record = RecordCache.parseRector(recordName, getAssets());
							if (record != null) {
								String headerName = sections[1];
								Header header = null;
								for (Section section : record) {
									for (Header h : section) {
										//can we find a header whose name (without case or spaces) equals the link's
										if (h.title.replace(" ", "").equalsIgnoreCase(headerName)) {
											header = h;
										}
									}
								}
								if (header != null) {
									if (header == QnAActivity.this.header) {
										if (sections.length > 2) {
											//If we're linking to a question on this page,
											//just display it
											openQuestion(sections[2]);
										}
									} else {
										//Otherwise open a new QnA for the question
										Intent intent = new Intent(QnAActivity.this, QnAActivity.class);
										intent.putExtra("header", header);
										if (sections.length > 2) {
											intent.putExtra("question", sections[2]);
										}
										startActivity(intent);
									}
								} else {
									Log.d("Sunshine", "No Header: '" + headerName + "'");
								}
							} else {
								Log.d("Sunshine", "No Record: '" + recordName + "'");
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			} else {
				//If it's not a special link, open it in the browser
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
						Uri.parse(url));
				startActivity(browserIntent);

				return true;
			}
		}
	}

}
