package jp.narit.lineartanalysis;

import jp.narit.lineartanalysis.analyze.LineArtAnalyzer;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	private EditText mInputText;
	private EditText mInputBorderText;
	private EditText mOutputText;
	private Button mAnalyze;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViews();
	}
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV loaded successfully");
					break;
				}
				default:
				{
					super.onManagerConnected(status);
					break;
				}
			}
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}
	
	void findViews() {
		mInputText = (EditText) findViewById(R.id.ed_inputData);
		mInputBorderText = (EditText) findViewById(R.id.ed_inputBorderData);
		
		mOutputText = (EditText) findViewById(R.id.ed_outputData);
		mOutputText.setEnabled(false);
		mOutputText.setFocusable(false);
		
		mAnalyze = (Button) findViewById(R.id.bt_analyze);
		mAnalyze.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				analyze();
			}
		});
	}
	
	private void analyze() {
		mOutputText.setText(LineArtAnalyzer.analyze(mInputText.getText().toString(), 
				mInputBorderText.getText().toString()));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	// オプションメニューアイテムが選択された時に呼び出されます
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;
		switch (item.getItemId()) {
			default:
				ret = super.onOptionsItemSelected(item);
				break;
			case R.id.it_textAnalysis:
				// 何もしない
				break;
			case R.id.it_photoAnalysis:
				// 画像解析へ
				Intent intent = new Intent(MainActivity.this, PhotoAnalyzeActivity.class);
				startActivity(intent);
				break;
		}
		return ret;
	}
	
}
