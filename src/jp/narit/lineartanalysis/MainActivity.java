package jp.narit.lineartanalysis;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

	private EditText mInputText;
	private EditText mOutputText;
	private Button mAnalyze;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	}
	
	void findViews() {
		mInputText = (EditText) findViewById(R.id.ed_inputData);
		
		mOutputText = (EditText) findViewById(R.id.ed_outputData);
		mOutputText.setFocusable(false);
		
		mAnalyze = (Button) findViewById(R.id.bt_analyze);
		mAnalyze.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				analyze();
			}
		});
	}
	
	void analyze() {
		mOutputText.setText("");
		// テキストを改行で分ける
		String[] text = mInputText.getText().toString().split("\n");
		
		for (int i = 0; i < text.length; i++) {
			mOutputText.append(text[i]);
			mOutputText.append("  ");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
}
