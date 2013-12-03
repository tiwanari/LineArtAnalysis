package jp.narit.lineartanalysis.analyze;

import java.util.Arrays;
import java.util.List;

import jp.narit.lineartanalysis.analyze.LabelDictionary.Label;
import android.util.Log;

public class LineArtAnalyzer {
	
	private static final String TAG = "LinearArtAnalyzer";
	private static VertexSet mVertedice = new VertexSet();
	private static EdgeSet mEdges = new EdgeSet();
	
	public static String analyze(String inputString, String borderString) {
		setUp();
		decordInput(inputString);
		decordBorder(borderString);
		checking();
		return showResult();
	}
	
	private static void setUp() {
		mVertedice.init();
		mEdges.init();
	}

	/**
	 * 頂点データの処理
	 */
	private static void decordInput(String inputString) {
		// テキストを改行で分ける
		String[] raws = inputString.split("\n");
		
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
	private static void decordBorder(String borderString) {
		// テキストを改行で分ける
		String[] raws = borderString.split("\n");
		
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
			if (border == null) {
				Log.d(TAG, "edge: " + datas[i][0] + "-" + datas[i][1]);
				continue;
			}
			border.removeCandidates(removeList);
		}
	}
	
	private static void checking() {
		int i = 0;
		// 更新がなくなるまで繰り返し
		while (mVertedice.updateVertexLabels()) {
			Log.d(TAG, ++i + "回目");
			Log.d(TAG, "Progress: " + mEdges.showResult());
		}
	}
	
	private static String showResult() {
		return mEdges.showResult();
	}
	
	public static EdgeSet getAnalizedEdgeSet() {
		return mEdges;
	}
}
