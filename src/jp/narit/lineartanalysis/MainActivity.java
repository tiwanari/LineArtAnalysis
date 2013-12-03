package jp.narit.lineartanalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import jp.narit.lineartanalysis.decode.LinearArtDecoder;
import jp.narit.lineartanalysis.graph.Edge;
import jp.narit.lineartanalysis.graph.EdgeSet;
import jp.narit.lineartanalysis.graph.LabelDictionary;
import jp.narit.lineartanalysis.graph.LabelDictionary.Label;
import jp.narit.lineartanalysis.graph.Vertex;
import jp.narit.lineartanalysis.graph.VertexSet;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	private EditText mInputText;
	private EditText mInputBorderText;
	private EditText mOutputText;
	private Button mAnalyze;
	
	private VertexSet mVertedice = new VertexSet();
	private EdgeSet mEdges = new EdgeSet();
	
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
				// analyze();
				// Intent intent = new Intent(MainActivity.this,
				// Tutorial3Activity.class);
				// startActivity(intent);
				test();
			}
		});
	}
	
	void test() {

		ImageView output = (ImageView) findViewById(R.id.imageView01);

		ArrayList<String> data = new ArrayList<String>();
		ArrayList<String> border = new ArrayList<String>();
		Mat res = LinearArtDecoder.decodeLineArt("boxes.png", data, border);
		
		Bitmap dst = Bitmap.createBitmap(res.width(), res.height(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(res, dst);
		
		output.setImageBitmap(dst);
		mInputText.setText(data.toString());
		analyze();
	}
	
	
	private void analyze() {
		setUp();
		Log.d(TAG, "before setup");
		decordInput();
		decordBoder();
		Log.d(TAG, "checking setup");
		checking();
		Log.d(TAG, "showResult setup");
		showResult();
	}
	
	private void setUp() {
		mVertedice.init();
		mEdges.init();
	}
	
	/**
	 * 頂点データの処理
	 */
	private void decordInput() {
		mOutputText.setText("");
		// テキストを改行で分ける
		String[] raws = mInputText.getText().toString().split("\n");
		
		// データに分ける
		String[][] datas = new String[raws.length][];
		for (int i = 0; i < raws.length; i++) {
			datas[i] = raws[i].split(" "); // 細かく分ける
		}
		
		// 処理
		for (int i = 0; i < datas.length; i++) {
			Vertex newVertex;
			int numOfEdge;
			
			// Lのときは他の頂点が2つ
			if (datas[i][0].equals("L")) numOfEdge = 2;
			// それ以外は3つ
			else numOfEdge = 3;
			
			// 新しい頂点を作る
			newVertex = new Vertex(datas[i][1], LabelDictionary.getLabelValue(datas[i][0]));
			
			for (int j = 0; j < numOfEdge; j++) {
				// 辺を作って集合に加える
				// すでにあるならそれが返る
				Edge edge = mEdges.addEdge(new Edge(datas[i][1], datas[i][j + 2]));
				newVertex.addEdge(edge); // 辺を加えておく
			}
			newVertex.initCandidates(); // 候補の初期化
			mVertedice.addVertex(newVertex);
		}
	}
	
	/**
	 * 境界線の処理
	 * 境界線によって，制約を強める
	 */
	private void decordBoder() {
		// テキストを改行で分ける
		String[] raws = mInputBorderText.getText().toString().split("\n");
		
		// データに分ける
		String[][] datas = new String[raws.length][];
		for (int i = 0; i < raws.length; i++) {
			datas[i] = raws[i].split(" "); // 細かく分ける
		}
		
		// 境界線の場合はラベルが矢印となる
		List<Label> removeList = Arrays.asList(Label.PLUS, Label.MINUS);
		
		// いらない候補を消しておく
		for (int i = 0; i < datas.length; i++) {
			Edge border = mEdges.getEdge(datas[i][0], datas[i][1]);
			border.removeCandidates(removeList);
		}
	}
	
	private void checking() {
		int i = 0;
		// 更新がなくなるまで繰り返し
		while (mVertedice.updateVertexLabels()) {
			Log.d(TAG, ++i + "回目");
			Log.d(TAG, "Progress: " + mEdges.showResult());
		}
	}
	
	private void showResult() {
		String result = mEdges.showResult();
		mOutputText.setText(result);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
}
