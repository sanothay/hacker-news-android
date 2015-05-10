package com.dudev.android.hackernews.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dudev.android.hackernews.R;

public class CommentReplyView extends LinearLayout {
	private String replyText = "";
	private TextView textViewReplyText;
	private View spacer;
	private View view;
	private Context context;

	public CommentReplyView(Context context) {
		super(context);
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.comment_reply_item, this);
		initViews(context, null);
	}

	public CommentReplyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initViews(context, attrs);
	}

	public CommentReplyView(Context context, AttributeSet attrs, int defStyle) {
		this(context, attrs);
		this.context = context;
		initViews(context, attrs);
	}

	private void initViews(Context context, AttributeSet attrs) {
//		LayoutInflater.from(context).inflate(R.layout.comment_reply_item, this);
		LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.comment_reply_item, null);
		textViewReplyText = (TextView) view.findViewById(R.id.reply_text);
		spacer = (View) view.findViewById(R.id.spacer);
	}

	public void setReplyText(String text) {
		textViewReplyText.setText(TextUtils.isEmpty(text) ? "" : text);
		((TextView)view.findViewById(R.id.reply_text)).setText("alibaba...");
		this.addView(view);
	}
	public String getReplyText() {
		replyText = textViewReplyText.getText().toString();
		return replyText;

	}

	public void setSpacerColor(int color) {
		spacer.setBackgroundColor(color);
	}
}