package jp.narit.lineartanalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import jp.narit.lineartanalysis.FileSelectDialog.OnFileSelectDialogListener;
import jp.narit.lineartanalysis.analyze.EdgeSet;
import jp.narit.lineartanalysis.analyze.LineArtAnalyzer;
import jp.narit.lineartanalysis.color.LineArtColorer;
import jp.narit.lineartanalysis.decode.LineArtDecoder;
import jp.narit.lineartanalysis.decode.Vector2DSet;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 画像を解析するアクティビティ
 * @author tatsuya
 *
 */
public class PhotoAnalyzeActivity extends Activity implements OnFileSelectDialogListener {
	
	private static final String TAG = "PhotoAnalyzeActivity";

	public static final int PREFERENCE_INIT = 0;
	public static final int PREFERENCE_BOOTED = 1;
	String state = Environment.getExternalStorageState();

	private LinearLayout mLayout;
	private Button mAnalyze;
	private ImageView mOutputImage;
	private TextView mTextView;
	
	private CheckBox mIsPhotoImage;
	
	private EdgeSet mEdges;
	private Vector2DSet mVectors;
	
	private String mFilePath;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_analyze);
		findViews();
	}
	
	private void findViews() {
		mLayout = (LinearLayout) findViewById(R.id.ll_photoAnalyze);
		mAnalyze = (Button) findViewById(R.id.bt_analyzePhoto);
		mAnalyze.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selectFile();
			}
		});
		mIsPhotoImage = (CheckBox) findViewById(R.id.cb_isThinning);
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
		// ファイル選択ダイアログを表示
		FileSelectDialog dialog = new FileSelectDialog(this);
		dialog.setOnFileSelectDialogListener(this);
		
		// 表示
		dialog.show(Environment.getExternalStorageDirectory().getPath());
	}
	
	private void analyze() {
		Mat res = LineArtDecoder.decodeLineArt(mFilePath, mIsPhotoImage.isChecked());
		mVectors = LineArtDecoder.getAnalizedVectorSet();
		
		ArrayList<String> data = LineArtDecoder.getAnalizedInput(); // ラベル情報を取得する
		ArrayList<String> border = LineArtDecoder.getAnalizedBorder(); // 境界情報を取得する
		
		// 入力が不正だった場合
		if (data == null) {
			setResultToTextView("不可能図形であるか入力が不正です");
			drawBmpFromMat(res);
			return ;
		}

		String result = LineArtAnalyzer.analyze(getStringFromStringArray(data), getStringFromStringArray(border));
		mEdges = LineArtAnalyzer.getAnalizedEdgeSet();
		
		// 一意にきまっているならラベルを貼る
		if (mEdges.didSolved()) {
			res = LineArtColorer.colorLineArtByEdgeInfo(res, mVectors, mEdges); // カラーリング
			drawBmpFromMat(res);
			mLayout.removeView(mTextView);
		}
		else {
			setResultToTextView(result.replace("\n", " "));
			drawBmpFromMat(res);
		}
	}
	
	private void setResultToTextView(String res) {
		mTextView.setText(res);
		mLayout.removeView(mTextView);
		mLayout.addView(mTextView); // TextViewも表示
	}
	
	private String getStringFromStringArray(ArrayList<String> array) {
		StringBuffer buffer = new StringBuffer();
		for (String str : array) {
			buffer.append(str);
		}
		Log.d(TAG, array.toString());
		
		return buffer.toString();
	}
	
	private void drawBmpFromMat(Mat target) {
		Bitmap dst = Bitmap.createBitmap(target.width(), target.height(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(target, dst);
		
		// 頂点を描画した図の設定
		mOutputImage.setImageBitmap(dst);
		mLayout.removeView(mOutputImage);
		mLayout.addView(mOutputImage);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		
		// 初回時のみサンプルファイルを保存
		if (PREFERENCE_INIT == getState()) {
			setState(PREFERENCE_BOOTED);
			saveSamples();
		}
	}
	
	private void saveSamples() {
		// 読み書き可能
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			String folderPath = getExternalFilesDir(null).toString();
			for (int i = 0; i < 7; i++) {
				int bmpId = getResources().getIdentifier("sample" + i, "drawable", getPackageName());
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), bmpId);
				try {
					FileOutputStream fos = new FileOutputStream(new File(folderPath, "sample" + i + ".png"));
					bmp.compress(CompressFormat.PNG, 100, fos);
					fos.close();
				}
				catch (FileNotFoundException e) {
					Log.e(TAG, "sample" + i + "が存在しない");
					e.printStackTrace();
				}
				catch (IOException e) {
					Log.e(TAG, "ファイルが閉じられない");
					e.printStackTrace();
				}
			}
		}
		else {
			// 書き込めない
			Toast.makeText(this, "sampleが保存できませんでした", Toast.LENGTH_SHORT).show();
		}
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
			case R.id.it_textAnalysis:
				Intent intent = new Intent(PhotoAnalyzeActivity.this, TextAnalyzeActivity.class);
				startActivity(intent);
				break;
			case R.id.it_photoAnalysis:
				break;
			default:
				ret = super.onOptionsItemSelected(item);
				break;
		}
		return ret;
	}
	
	@Override
	public void onClickFileSelect(File file) {
		if (file != null) {
			mFilePath = file.getPath();
			Log.d(TAG, "file" + mFilePath);
			analyze();
		}
	}
	
	// データ保存
	private void setState(int state) {
		// SharedPreferences設定を保存
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.edit().putInt("InitState", state).commit();
	}
	
	// データ読み出し
	private int getState() {
		// 読み込み
		int state;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		state = sp.getInt("InitState", PREFERENCE_INIT);
		return state;
	}
}
