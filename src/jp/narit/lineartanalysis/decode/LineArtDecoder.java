package jp.narit.lineartanalysis.decode;

import java.util.ArrayList;
import java.util.List;
import jp.narit.lineartanalysis.analyze.LabelDictionary;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class LineArtDecoder {
	private static final String TAG = "LinearArtAnalyzer";
	
	private static ArrayList<Vertex2D> vertexList = new ArrayList<Vertex2D>();
	private static Vector2DSet vectorSet = new Vector2DSet();
	
	private static final double EPSILON_THETA = 0.001;
	private static final int EPSILON_POSITION = 5;
	
	private static Mat mTarget = new Mat();
	private static Mat mGray = new Mat();
	private static Mat mBin = new Mat();
	
	private static Bitmap mBmp;
	
	private static ArrayList<String> mInput = new ArrayList<String>();
	private static ArrayList<String> mBorder = new ArrayList<String>();
	
	private static boolean isEnabledPreprocessing;
	
	private static void init() {
		vertexList.clear();
		vectorSet.init();
	}
	
	public static Mat decodeLineArt(String filename, boolean photo) {
		init();
		
		// 細線化の有無
		isEnabledPreprocessing = photo;
		
		// String filename = "boxes.png";
		mBmp = BitmapFactory.decodeFile(filename).copy(Bitmap.Config.ARGB_8888, true);
		Utils.bitmapToMat(mBmp, mTarget);
		
		// 下処理
		preprocessImage(mTarget);
		
		mBorder = decordVerticesAndEdges(); // 点と辺の抽出し，境界の辺を文字列のリストとして返す
		mInput = decordVerticesType(); // 頂点のラベルを振り文字列リストとして返す
		
		return mTarget;
	}
	
	
	private static void preprocessImage(Mat mat) {
		// 細線化
		if (isEnabledPreprocessing) 
			mGray = LineArtPreprocessor.preprocessing(mat);
		else
			Imgproc.cvtColor(mat, mGray, Imgproc.COLOR_RGB2GRAY);
		
		Imgproc.adaptiveThreshold(mGray, mBin, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 15, 4);
	}
	
	/**
	 * 頂点と辺を画像から取得する
	 */
	private static ArrayList<String> decordVerticesAndEdges() {
		
		// 頂点の抽出
		MatOfPoint corners = new MatOfPoint();
		
		// 細線化の有無で場合分け
		if (isEnabledPreprocessing)
			Imgproc.goodFeaturesToTrack(mGray, corners, 80, 0.1, 3);
		else
			Imgproc.goodFeaturesToTrack(mGray, corners, 80, 0.05, 3);
			
		ArrayList<Point> points = new ArrayList<Point>(corners.toList());
		ArrayList<Point> temp = new ArrayList<Point>(corners.toList());
		for (int i = 0; i < temp.size(); i++) {
			Point p0 = temp.get(i);
			// 外側に出てくる無駄な点を削除(原因不明)
			if (p0.x > mBmp.getWidth() - EPSILON_POSITION || p0.x < EPSILON_POSITION) points.remove(p0);
			// 近すぎる頂点は除く
			else {
				for (int j = i + 1; j < temp.size(); j++) {
					Point p1 = temp.get(j);
					if (Math.abs(p1.x - p0.x) < 2 * EPSILON_POSITION
							&& Math.abs(p1.y - p0.y) < 2 * EPSILON_POSITION) {
						points.remove(p1);
						break;
					}
				}
			}
		}
		
		// 頂点の登録
		for (int i = 0; i < points.size(); i++) {
			String name = "" + (char) ('A' + i);
			Point point = points.get(i);
			Log.d(TAG, "point[" + name + "]: (" + point.x + ", " + point.y + ")");
			vertexList.add(new Vertex2D(name, point)); // 頂点と頂点名の対応を保存
			Core.putText(mTarget, name, point, Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0, 0)); // 点の対応を表示
		}
		
		// 輪郭の抽出
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(mBin, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
		
		// 境界をなぞってv0->v1->v2->v0のように頂点を見つけ，隣り合う点を結んで辺を作る
		for (int i = 0; i < contours.size(); i++) {
			MatOfPoint m = contours.get(i);
			Vertex2D first = null; // 境界の始点
			Vertex2D temp0 = null; // 1つ目の頂点
			
			// 境界をなぞっていく
			for (Point pt : m.toList()) {
				for (Vertex2D vt : vertexList) {
					// 頂点(付近)を通ったら
					if (Math.abs(pt.x - vt.point.x) <= EPSILON_POSITION && Math.abs(pt.y - vt.point.y) <= EPSILON_POSITION) {
						Vertex2D temp1 = vt; // 2つ目の頂点
						
						if (temp0 == null) first = temp1; // 最初の頂点を保存
						else {
							// v0->v1やv1->v2を想定
							vectorSet.addVector(new Vector2D(temp0, temp1));
							vectorSet.addVector(new Vector2D(temp1, temp0)); // 入れ替えたものも登録しておく
						}
						temp0 = temp1; // 更新しておく
					}
				}
			}
			// v2->v0を想定
			if (temp0 != null) vectorSet.addVector(new Vector2D(temp0, first));
		}
		
		// 辺の確認
		for (Vector2D edge : vectorSet.getVectorList()) {
			Log.d(TAG, "v0:" + edge.from.name + ", v1:" + edge.to.name);
		}
		
		
		// 一番外側の境界をたどって境界の点を探す
		ArrayList<Vertex2D> borderPoints = new ArrayList<Vertex2D>();
		for (Vertex2D vt : vertexList) {
			if (Imgproc.pointPolygonTest(new
					MatOfPoint2f(contours.get(0).toArray()), vt.point, true) < 3.0) {
				borderPoints.add(vt);
			}
		}
		
		// 境界の辺を列挙する
		ArrayList<String> border = new ArrayList<String>();
		for (Vertex2D vt0 : borderPoints) {
			for (Vertex2D vt1 : borderPoints) {
				if (vectorSet.getVector(vt0, vt1) != null) {
					StringBuffer buffer = new StringBuffer();
					buffer.append(vt0.name + " " + vt1.name + "\n");
					border.add(buffer.toString());
				}
			}
		}
		
		return border;
	}
	
	/**
	 * 頂点に対してL,A,Y,Tの割り当てと頂点の対応を作る
	 * 
	 * @return
	 */
	private static ArrayList<String> decordVerticesType() {
		ArrayList<String> datas = new ArrayList<String>();
		for (Vertex2D from : vertexList) {
			int label = -1;
			
			// 始点から辺のリストを抽出
			ArrayList<Vector2D> members = vectorSet.getVectors(from);
			Log.d(TAG, "checking... " + from.name);
			ArrayList<Vector2D> sorted = sortEdges(members, members.size() == 2); // ラベル格納に見合うように並び替える
			// L型
			if (sorted.size() == 2) {
				label = LabelDictionary.L;
			}
			// A,Y,T型の振り分け
			else if (sorted.size() == 3) {
				double angle = calcMaximumAngle(sorted);
				Log.d(TAG, "angle :" + angle);
				if (angle >= Math.PI - EPSILON_THETA && angle <= Math.PI + EPSILON_THETA) label = LabelDictionary.T;
				else if (angle >= Math.PI) label = LabelDictionary.A;
				else label = LabelDictionary.Y;
				
				Log.d(TAG, "Label: " + LabelDictionary.getLabelString(label));
			}
			else {
				// 不可能図形か入力が間違っている時
				Log.d(TAG, "不可能図形or入力が間違っている(辺:" + sorted.size() + ")");
				return null;
			}
			StringBuffer buffer = new StringBuffer();
			buffer.append(LabelDictionary.getLabelString(label) + " " + from.name); // 始点
			for (Vector2D vec : sorted) {
				buffer.append(" " + vec.to.name); // 終端点列
			}
			buffer.append("\n"); // 改行
			datas.add(buffer.toString());
		}
		
		return datas;
	}
	
	/**
	 * 辺を入れ替える
	 * 
	 * @param members
	 * @param isLType
	 * @return
	 */
	private static ArrayList<Vector2D> sortEdges(ArrayList<Vector2D> members, boolean isLType) {
		ArrayList<Vector2D> temp = new ArrayList<Vector2D>(members);
		boolean isUpdated;
		do {
			isUpdated = false;
			// 順番に並び替える(バブルソート)
			for (int i = 0; i < temp.size() - 1; i++) {
				Vector2D vec0 = temp.get(i);
				Vector2D vec1 = temp.get(i + 1);
				
				// 位置関係を見て並び替える(L,Yは逆向き)
				if (isLType) {
					if (vec0.isClockwizedAround(vec1)) {
						swap(temp, i, i + 1);
						isUpdated = true;
					}
				}
				else {
					if (!(vec0.isClockwizedAround(vec1))) {
						swap(temp, i, i + 1);
						isUpdated = true;
					}
				}
			}
		} while (isUpdated);
		
		return temp;
	}
	
	/**
	 * 配列の要素の入れ替え
	 * 
	 * @param list
	 * @param index1
	 * @param index2
	 */
	static void swap(List<Vector2D> list, int index1, int index2) {
		Vector2D tmp = list.set(index1, list.get(index2));
		list.set(index2, tmp);
	}
	
	/**
	 * 3つの辺の間の最大角を求める
	 * 
	 * @param sorted
	 * @return
	 */
	private static double calcMaximumAngle(ArrayList<Vector2D> sorted) {
		Vector2D vec0 = sorted.get(0);
		Vector2D vec1 = sorted.get(1);
		Vector2D vec2 = sorted.get(2);
		
		// 3つの辺の間の角を考える
		// 並び替えられているので1,2番目の辺と2,3番目の辺の間の角を足して360度から引けば良い
		double sumOfAngles = vec0.angleBetween(vec1) + vec1.angleBetween(vec2);
		Log.d(TAG, "angle0 : " + vec0.angleBetween(vec1));
		Log.d(TAG, "angle1 : " + vec1.angleBetween(vec2));
		
		return 2 * Math.PI - sumOfAngles;
	}
	
	/**
	 * 解析された入力データを取得
	 * decodeLineArtの後に呼ぶ
	 * 
	 * @return
	 */
	public static ArrayList<String> getAnalizedInput() {
		return mInput;
	}
	
	/**
	 * 解析された境界データを取得
	 * decodeLineArtの後に呼ぶ
	 * 
	 * @return
	 */
	public static ArrayList<String> getAnalizedBorder() {
		return mBorder;
	}
	
	/**
	 * 解析されたベクトル情報を取得
	 * decodeLineArtの後に呼ぶ
	 * 
	 * @return
	 */
	public static Vector2DSet getAnalizedVectorSet() {
		return vectorSet;
	}
}
