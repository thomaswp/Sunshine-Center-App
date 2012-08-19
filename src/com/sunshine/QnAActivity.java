package com.sunshine;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sunshine.Record.Header;
import com.sunshine.Record.Question;
import com.sunshine.Record.Section;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spannable;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class QnAActivity extends Activity {

	private List<Question> header;
	private WebClientIntercept intercept;
	private ExpandableListView listView;
	private String path;
	private QnAExpandableListAdapter adapter;
	private Pattern pattern;

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		intercept = new WebClientIntercept();

		Serializable s = getIntent().getExtras().getSerializable("header");
		header = (List<Question>)s;
		if (header == null) {
			finish();
			return;
		}
		path = getIntent().getExtras().getString("path");
		String patternS = getIntent().getExtras().getString("pattern");
		if (patternS != null) pattern = Pattern.compile(patternS, 
				Pattern.CASE_INSENSITIVE);

		createContent(savedInstanceState);
		
		if (savedInstanceState != null) {
			int orientation = 
				getResources().getConfiguration().orientation;
			int oldOrientation =
				savedInstanceState.getInt("orientation");
			
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
		outState.putInt("firstPos",
				listView.getFirstVisiblePosition());
	}

	private void createContent(Bundle savedInstanceState) {
		int id = 1;

		LinearLayout host = new LinearLayout(this);
		host.setOrientation(LinearLayout.VERTICAL);
		host.setBackgroundColor(MenuActivity.BACKGROUND_DARK);
		host.setId(id++);

		int WRAP = LayoutParams.WRAP_CONTENT;
		int MATCH = LayoutParams.MATCH_PARENT;
	

		listView = new ExpandableListView(this);
		listView.setId(id++);
		LayoutParams lps = new LayoutParams(MATCH, MATCH);
		lps.weight = 1;
		host.addView(listView, lps);

		adapter = new QnAExpandableListAdapter();
		listView.setAdapter(adapter);

		setContentView(host, new LayoutParams(MATCH, MATCH));

		if (savedInstanceState == null) {
			String question = getIntent().getExtras().getString("question");
			if (question != null) {
				openQuestion(question);
			}
		}
		
		
		TextView tv = new TextView(this);
		tv.setText(Html.fromHtml("<b>" + path + "</b>"));
		tv.setTextSize(14);
		tv.setGravity(Gravity.CENTER);
		tv.setId(id++);
		tv.setPadding(5, 5, 5, 5);
		tv.setTextColor(MenuActivity.BACKGROUND_LIGHT);
		tv.setBackgroundColor(MenuActivity.HIGHLIGHT);
		lps = new LayoutParams(MATCH, WRAP);
		host.addView(tv, lps);
	}

	private void openQuestion(String question) {
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

				int[] padding = new int[] {
						view.getPaddingLeft(),
						view.getPaddingTop(),
						view.getPaddingRight(),
						view.getPaddingTop()
				};
				Drawable[] layers = new Drawable[2];
				layers[0] = new ColorDrawable(MenuActivity.BACKGROUND_DARK);
				layers[1] = new ColorDrawable(MenuActivity.HIGHLIGHT);
				final TransitionDrawable drawable = new TransitionDrawable(layers);
				view.setBackgroundDrawable(drawable);
				final int time = 300;
				drawable.startTransition(time);
				view.setPadding(padding[0], padding[1], padding[2], padding[3]);
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
				

				//This is to force the listview to update, which
				//it seems disinclined to do after the first time
				padding = new int[] {
						listView.getPaddingLeft(),
						listView.getPaddingTop(),
						listView.getPaddingRight(),
						listView.getPaddingTop()
				};
				layers[0] = new ColorDrawable(MenuActivity.BACKGROUND_DARK);
				layers[1] = new ColorDrawable(MenuActivity.BACKGROUND_DARK);
				TransitionDrawable drawable2 = new TransitionDrawable(layers);
				listView.setBackgroundDrawable(drawable2);
				drawable2.startTransition(time * 2 + 100);
				listView.setPadding(padding[0], padding[1], padding[2], padding[3]);
			}
		}
	}

	private int toPx(int dip) {
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, 
				getResources().getDisplayMetrics());
	}

	private class QnAExpandableListAdapter extends BaseExpandableListAdapter {

		private LinkedList<View> parents, children;
		String css;
		Typeface typeface;

		public QnAExpandableListAdapter() {
			super();

			css = "<head><style media='screen' type='text/css'>";
			try {
				Scanner sc = new Scanner(getAssets().open("style.css"));
				while (sc.hasNext()) css += sc.nextLine(); 
			} catch (IOException e) {
				e.printStackTrace();
			}
			css += "</style></head>";
			parents = new LinkedList<View>();
			children = new LinkedList<View>();

			typeface = Typeface.createFromAsset(getAssets(), "fonts/tw.ttf"); 

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

		public Object getChild(int groupPosition, int childPosition) {
			return header.get(groupPosition).answer;
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public int getChildrenCount(int groupPosition) {
			return 1;
		}

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
			textView.setPadding(76, 5, 20, 5);
			return textView;
		}
		
		private String highlightString(String text) {
			if (pattern == null) {
				return text;
			}
			
			Matcher matcher = pattern.matcher(text);
			StringBuffer sb = new StringBuffer(text.length());
			while (matcher.find()) {
				matcher.appendReplacement(sb, "<b>$0</b>");
			}
			matcher.appendTail(sb);
			return sb.toString();
		}
		
		private CharSequence highlight(String text) {
			if (pattern == null) {
				return text;
			}
			
			return Html.fromHtml(highlightString(text));
		}

		private View generateChildView(int groupPosition, int childPosition) {
			Question q = header.get(groupPosition);
			//int color = Color.parseColor("#FFDDAA");
			int color = MenuActivity.BACKGROUND_LIGHT;
			int fontSize = 18;
			int margin = toPx(20);
			
			if (!q.containsHTML) {
				TextView textView = getGenericView();
					textView.setText(highlight(
							getChild(groupPosition, childPosition).toString()));
				
				textView.setPadding(margin, 5, 5, 5);
				textView.setBackgroundColor(color);
				textView.setTextSize(fontSize);
				return textView;
			}

			LinearLayout ll = new LinearLayout(QnAActivity.this);
			ll.setPadding(margin - 5, 5, 5, 5);
			ll.setBackgroundColor(color);
			WebView wv = new WebView(QnAActivity.this);
			String html = highlightString(getChild(
					groupPosition, childPosition).toString());
			wv.loadData(css + html, "text/html", "UTF-8");
			wv.setHorizontalScrollBarEnabled(false);
			wv.setBackgroundColor(color);
			wv.getSettings().setDefaultFontSize(fontSize);
			wv.setWebViewClient(intercept);
			ll.addView(wv);
			return ll;
		}

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

		private View generateParentView(int groupPosition) {
			TextView textView = getGenericView();
			textView.setText(highlight(header.get(groupPosition).question));
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
			textView.setTypeface(typeface);
			textView.setBackgroundColor(MenuActivity.BACKGROUND_DARK);
			return textView;
		}

		public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
				ViewGroup parent) {
			while (parents.size() <= groupPosition) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//Log.d("" + groupPosition, "" + isExpanded);
			return parents.get(groupPosition);
		}

		public Object getGroup(int groupPosition) {
			return header.get(groupPosition);
		}

		public int getGroupCount() {
			return header.size();
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

		public boolean hasStableIds() {
			return true;
		}

	}

	public class WebClientIntercept extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			if (url.startsWith("record:")) {
				url = url.replace("record://", "");
				String[] sections = url.split("\\.");
				if (sections.length > 0) {
					String recordName = sections[0] + ".xml";

					if (sections.length == 1) {
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
									if (h.title.replace(" ", "").equalsIgnoreCase(headerName)) {
										header = h;
									}
								}
							}
							if (header != null) {
								if (header.title.equals(path) && 
										header.size() == QnAActivity.this.header.size()) {
									if (sections.length > 2) {
										openQuestion(sections[2]);
									}
								} else {
									Intent intent = new Intent(QnAActivity.this, QnAActivity.class);
									intent.putExtra("header", header);
									intent.putExtra("path", header.title);
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
				return true;
			} else {
				//		        View v = getLayoutInflater().inflate(R.layout.browser, null);
				//		        ((WebView)v.findViewById(R.id.webView)).loadUrl(url);
				//		        new AlertDialog.Builder(QnAActivity.this)
				//		        .setTitle("Link")
				//		        .setView(v)
				//		        .show();
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
						Uri.parse(url));
				startActivity(browserIntent);

				return true;
			}
		}
	}

}
