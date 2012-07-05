package com.sunshine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.sunshine.Record.Header;
import com.sunshine.Record.Question;
import com.sunshine.Record.Section;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class QnAActivity extends Activity {

	private List<Question> header;

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Serializable s = getIntent().getExtras().getSerializable("header");
		header = (List<Question>)s;
		createContent();
	}

	private void createContent() {
		LinearLayout host = new LinearLayout(this);
		host.setOrientation(LinearLayout.VERTICAL);
		host.setBackgroundColor(MenuActivity.BACKGROUND);

		int WRAP = LayoutParams.WRAP_CONTENT;
		int MATCH = LayoutParams.MATCH_PARENT;

		ExpandableListView listView = new ExpandableListView(this);
		//listView.setBackgroundColor(Color.parseColor("#FFDD77"));
		host.addView(listView, new LayoutParams(MATCH, WRAP));

		QnAExpandableListAdapter adapter = new QnAExpandableListAdapter();
		listView.setAdapter(adapter);

		setContentView(host, new LayoutParams(MATCH, MATCH));
	}

	private int toPx(int dip) {
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, 
				getResources().getDisplayMetrics());
	}

	private class QnAExpandableListAdapter extends BaseExpandableListAdapter {

		private LinkedList<View> children;
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
			children = new LinkedList<View>();
			for (int i = 0; i < header.size(); i++) {
				children.add(generateChildView(i, 0));
			}
			

			typeface = Typeface.createFromAsset(getAssets(), "fonts/tw.ttf"); 
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

		private View generateChildView(int groupPosition, int childPosition) {
			Question q = header.get(groupPosition);
			//int color = Color.parseColor("#FFDDAA");
			int color = Color.argb(255, 252, 247, 217);
			int fontSize = 18;
			int margin = toPx(20);

			if (!q.containsHTML) {
				TextView textView = getGenericView();
				//textView.setText(Html.fromHtml(getChild(groupPosition, childPosition).toString()));
				textView.setText(getChild(groupPosition, childPosition).toString());
				textView.setPadding(margin, 5, 5, 5);
				textView.setBackgroundColor(color);
				textView.setTextSize(fontSize);
				return textView;
			}

			LinearLayout ll = new LinearLayout(QnAActivity.this);
			ll.setPadding(margin - 5, 5, 5, 5);
			ll.setBackgroundColor(color);
			WebView wv = new WebView(QnAActivity.this);
			String html = getChild(groupPosition, childPosition).toString();
			Log.d("html", html);
			wv.loadData(css + html, "text/html", "UTF-8");
			wv.setHorizontalScrollBarEnabled(false);
			wv.setBackgroundColor(color);
			wv.getSettings().setDefaultFontSize(fontSize);
			ll.addView(wv);
			return ll;
		}

		public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
				View convertView, ViewGroup parent) {
			return children.get(groupPosition);
		}

		public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
				ViewGroup parent) {
			TextView textView = getGenericView();
			textView.setText(header.get(groupPosition).question);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
			textView.setTypeface(typeface);
			textView.setBackgroundColor(MenuActivity.BACKGROUND);
			return textView;
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

}
