package jp.narit.lineartanalysis;

import java.io.File;
import java.util.ArrayList;

import jp.narit.lineartanalysis.analyze.EdgeSet;
import jp.narit.lineartanalysis.analyze.LineArtAnalyzer;
import jp.narit.lineartanalysis.decode.LineArtDecoder;
import jp.narit.lineartanalysis.decode.Vector2DSet;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PhotoAnalyzeActivity extends Activity {
	
	private static final String TAG = "PhotoAnalyzeActivity";
	
	private LinearLayout mLayout;
	private Button mTakePhoto;
	private Button mAnalyze;
	private ImageView mOutputImage;
	private TextView mTextView;
	
	private EdgeSet mEdges;
	private Vector2DSet mVectors;
	
	private File mFilePath;
	
	private int[] images = {R.drawable.box};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_analysis);
		findViews();
	}
	
	private void findViews() {
		mLayout = (LinearLayout) findViewById(R.id.ll_photoAnalyze);
		mTakePhoto = (Button) findViewById(R.id.bt_takePhoto);
		mTakePhoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(PhotoAnalyzeActivity.this, TakePhotoActivity.class);
				startActivity(intent);
			}
		});
		mAnalyze = (Button) findViewById(R.id.bt_analyzePhoto);
		mAnalyze.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectFile();
			}
		});
		mOutputImage = new ImageView(this);
		mTextView = new TextView(this);
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
	
	private void selectFile() {
		// ファイルの一覧を検索するディレクトリパスを指定する
		String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM";
		
		// 選択ボックスで表示するファイル名のリストを作成
		File dir = new File(path);
		final File[] files = dir.listFiles();
		final String[] str_items;
		str_items = new String[files.length + 1];
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			str_items[i] = file.getName();
		}
		str_items[files.length] = "キャンセル";
		
		// ファイルの選択ボックスを表示
		new AlertDialog.Builder(this)
				.setTitle("解析するファイルを選択")
				.setItems(str_items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (which < files.length) {
							mFilePath = files[which];
							analyze();
						}
					}
				}
				)
				.show();
	}
	
	private void analyze() {
		
		Mat res = LineArtDecoder.decodeLineArt(mFilePath.toString());
		mVectors = LineArtDecoder.getAnalizedVectorSet();
		
		Bitmap dst = Bitmap.createBitmap(res.width(), res.height(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(res, dst);
		
		mOutputImage.setImageBitmap(dst);
		
		ArrayList<String> data = LineArtDecoder.getAnalizedInput(); // ラベル情報を取得する
		StringBuffer dataBuffer = new StringBuffer();
		for (String str : data) {
			dataBuffer.append(str);
		}
		
		ArrayList<String> border = LineArtDecoder.getAnalizedBorder(); // 境界情報を取得する
		StringBuffer borderBuffer = new StringBuffer();
		for (String str : border) {
			borderBuffer.append(str);
		}
		
		String result = LineArtAnalyzer.analyze(dataBuffer.toString(), borderBuffer.toString());
		mEdges = LineArtAnalyzer.getAnalizedEdgeSet();
		
		mLayout.addView(mOutputImage);
		mTextView.setText(result);
		mLayout.addView(mTextView); // TextViewも表示
	}
	
	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;
		switch (item.getItemId()) {
			default:
				ret = super.onOptionsItemSelected(item);
				break;
			case R.id.it_textAnalysis:
				finish(); // 終了
				break;
			case R.id.it_photoAnalysis:
				break;
		}
		return ret;
	}
}
