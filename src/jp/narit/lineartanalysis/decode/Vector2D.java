package jp.narit.lineartanalysis.decode;

import org.opencv.core.Point;

import android.util.Log;

public class Vector2D {
	private static final String TAG = "Vector2D";
	public Vertex2D from;
	public Vertex2D to;
	
	public Vector2D(Vertex2D pt0, Vertex2D pt1) {
		this.from = pt0;
		this.to = pt1;
	}

	/**
	 * 現在のベクトルを時計回りに回転させたものか
	 * @param vec
	 * @return
	 */
	public boolean isClockwizedAround(Vector2D vec) {
		return crossProduct(vec) > 0.0;	// 外積を使って判定する
	}

	/**
	 * 大きさを返す
	 * @param vec
	 * @return
	 */
	public double norm() {
		Point a = new Point(to.point.x - from.point.x, to.point.y - from.point.y);
		return Math.sqrt(a.x * a.x + a.y * a.y);
	}

	/**
	 * 2つベクトルの内積
	 * 	A ・ B ≡ Ax * Bx + Ax * By
	 * を計算する
	 * @param vec
	 * @return
	 */
	public double innerProduct(Vector2D vec) {
		Point a = new Point(this.to.point.x - this.from.point.x, this.to.point.y - this.from.point.y);
		Point b = new Point(vec.to.point.x  - vec.from.point.x,  vec.to.point.y  - vec.from.point.y);
		return a.x * b.x + a.y * b.y;
	}
	
	/**
	 * 2つベクトルの外積
	 * 	A × B ≡ Ax * By － Ay * Bx
	 * を計算する
	 * @param vec
	 * @return
	 */
	public double crossProduct(Vector2D vec) {
		Point a = new Point(this.to.point.x - this.from.point.x, this.to.point.y - this.from.point.y);
		Point b = new Point(vec.to.point.x  - vec.from.point.x,  vec.to.point.y  - vec.from.point.y);
		Log.d(TAG, "a:(" + a.x + ", " + a.y + "), b:(" + b.x + ", " + b.y + ")");
		return - (a.x * b.y - a.y * b.x);	// y軸が下向き
	}
	
	/**
	 * 2辺の間の角を返す
	 * @param vec
	 * @return radian
	 */
	public double angleBetween(Vector2D vec) {
		double res = this.innerProduct(vec) / (this.norm() * vec.norm());
		Log.d(TAG, "inner:" + this.innerProduct(vec) + ", this.norm: " + this.norm() + ", vec.norm: " + vec.norm());
		return Math.acos(res);
	}
}
