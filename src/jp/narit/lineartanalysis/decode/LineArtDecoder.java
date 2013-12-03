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
	
	private static final double EPSILON = 0.5;
	
	private static Mat gray = new Mat();
	private static Mat bin = new Mat();
	
	private static Bitmap mBmp;
	
	private static ArrayList<String> mInput = new ArrayList<String>();
	private static ArrayList<String> mBorder = new ArrayList<String>();
	
	private static void init() {
		vertexList.clear();
		vectorSet.init();
	}
	
	public static Mat decodeLineArt(String filename) {
		init();
		
		// String filename = "boxes.png";
		mBmp = BitmapFactory.decodeFile(filename)
				.copy(Bitmap.Config.ARGB_8888, true);
		
		Mat mat = new Mat();
		Utils.bitmapToMat(mBmp, mat);
		// グレー画像へ
		Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY);
		// 2値化
		Imgproc.adaptiveThreshold(gray, bin, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 4);
		
		mBorder = decordVerticesAndEdges(); // 点と辺の抽出し，境界の辺を文字列のリストとして返す
		mInput = decordVerticesType(); // 頂点のラベルを振り文字列リストとして返す
		
		return gray;
	}
	
	public static ArrayList<String> getAnalizedInput() {
		return mInput;
	}
	
	public static ArrayList<String> getAnalizedBorder() {
		return mBorder;
	}
	
	public static Vector2DSet getAnalizedVectorSet() {
		return vectorSet;
	}
	
	/**
	 * 頂点と辺を画像から取得する
	 */
	private static ArrayList<String> decordVerticesAndEdges() {
		
		// 頂点の抽出
		MatOfPoint corners = new MatOfPoint();
		Imgproc.goodFeaturesToTrack(gray, corners, 80, 0.01, 5);
		
		// 外側に出てくる無駄な点を削除(原因不明)
		ArrayList<Point> points = new ArrayList<Point>(corners.toList());
		ArrayList<Point> temp = new ArrayList<Point>(corners.toList());
		for (Point point : temp) {
			if (point.x > mBmp.getWidth() - 5) points.remove(point);
		}
		
		// BGRA画像へ戻す
		Imgproc.cvtColor(gray, gray, Imgproc.COLOR_GRAY2BGRA, 4);
		
		// 頂点の登録
		for (int i = 0; i < points.size(); i++) {
			String name = "" + (char) ('A' + i);
			Point point = points.get(i);
			Log.d(TAG, "point: (" + point.x + ", " + point.y + ")");
			vertexList.add(new Vertex2D(name, point)); // 頂点と頂点名の対応を保存
			Core.putText(gray, name, point, Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0, 0)); // 点の対応を表示
		}
		
		// 輪郭の抽出
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(bin, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
		
		// 境界をなぞってv0->v1->v2->v0のように頂点を見つけ，隣り合う点を結んで辺を作る
		for (int i = 0; i < contours.size(); i++) {
			MatOfPoint m = contours.get(i);
			Vertex2D first = null; // 境界の始点
			Vertex2D temp0 = null; // 1つ目の頂点
			
			// 境界をなぞっていく
			for (Point pt : m.toList()) {
				for (Vertex2D vt : vertexList) {
					// 頂点(付近)を通ったら
					if (Math.abs(pt.x - vt.point.x) < 3 && Math.abs(pt.y - vt.point.y) < 3) {
						Vertex2D temp1 = vt; // 2つ目の頂点
						
						if (temp0 == null) first = temp1; // 最初の頂点を保存
						else {
							// v0->v1やv1->v2を想定
							vectorSet.addEdge(new Vector2D(temp0, temp1));
							vectorSet.addEdge(new Vector2D(temp1, temp0)); // 入れ替えたものも登録しておく
						}
						temp0 = temp1; // 更新しておく
					}
				}
			}
			// v2->v0を想定
			if (temp0 != null) vectorSet.addEdge(new Vector2D(temp0, first));
		}
		
		// 辺の確認
		for (Vector2D edge : vectorSet.getEdgeList()) {
			Log.d(TAG, "v0:" + edge.vt0.name + ", v1:" + edge.vt1.name);
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
				if (vectorSet.getEdge(vt0, vt1) != null) {
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
			ArrayList<Vector2D> members = vectorSet.getEdges(from);
			Log.d(TAG, "checking... " + from.name);
			ArrayList<Vector2D> sorted = sortEdges(members, members.size() == 2); // ラベル格納に見合うように並び替える
			// L型
			if (sorted.size() == 2) {
				label = LabelDictionary.L;
			}
			// A,Y,T型の振り分け
			else if (sorted.size() == 3){
				double angle = calcMaximumAngle(sorted);
				if (angle >= Math.PI - EPSILON && angle <= Math.PI + EPSILON)
					label = LabelDictionary.T;
				else if (angle > Math.PI)
					label = LabelDictionary.A;
				else
					label = LabelDictionary.Y;
			}
			else {
				continue;
			}
			StringBuffer buffer = new StringBuffer();
			buffer.append(LabelDictionary.getLabelString(label) + " " + from.name); // 始点
			for (Vector2D vec : sorted) {
				buffer.append(" " + vec.vt1.name); // 終端点列
			}
			buffer.append("\n"); // 改行
			datas.add(buffer.toString());
		}
		
		return datas;
	}
	
	/**
	 * 辺を入れ替える
	 * @param members
	 * @param isLType
	 * @return
	 */
	private static ArrayList<Vector2D> sortEdges(ArrayList<Vector2D> members, boolean isLType) {
		ArrayList<Vector2D> temp = new ArrayList<Vector2D>(members);
		
		// 順番に並び替える(バブルソート)
		for (int i = 0; i < temp.size() - 1; i++) {
			Vector2D vec0 = temp.get(i);
			Vector2D vec1 = temp.get(i + 1);
			
			// 位置関係を見て並び替える
			if (isLType) {
				if (vec0.isClockwizedAround(vec1)) swap(temp, i, i + 1);
			}
			else {
				if (!(vec0.isClockwizedAround(vec1))) swap(temp, i, i + 1);
			}
		}
		
		return temp;
	}
	
	/**
	 * 配列の要素の入れ替え
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
		return 2 * Math.PI - sumOfAngles;
	}
}
