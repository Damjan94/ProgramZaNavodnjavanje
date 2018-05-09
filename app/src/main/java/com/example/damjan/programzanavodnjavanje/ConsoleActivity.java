package com.example.damjan.programzanavodnjavanje;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;

public class ConsoleActivity extends AppCompatActivity
{

	public final static StringBuilder LOG = new StringBuilder();
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_console);

		TextView console = findViewById(R.id.consoleTextView);
		console.setMovementMethod(new ScrollingMovementMethod());
		console.setText(LOG);

		Button refresh = findViewById(R.id.consoleButton);
		refresh.setOnClickListener(v->
		{
			console.setText(LOG);
		});
		refresh.setOnLongClickListener(v->
		{
			LOG.delete(0, LOG.length());
			return true;
		});

	}
}
