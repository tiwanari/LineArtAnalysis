package jp.narit.lineartanalysis.decode;

import java.util.ArrayList;

import android.util.Log;

/**
 * 辺の集合
 * @author tatsuya
 *
 */
public class Vector2DSet {
	private static final String TAG = "Edge2DSet";
	private ArrayList<Vector2D> mSet = new ArrayList<Vector2D>();
	
	public Vector2DSet() {
		init();
	}
	
	public void init() {
		mSet.clear();
	}
	
	public Vector2D addEdge(Vector2D edge) {
		Vertex2D from = edge.vt0;
		Vertex2D to = edge.vt1;

		if (from.equals(to))	return null;	// 同じなら追加しない
		
		Vector2D ret;
		if ((ret = getEdge(from, to)) == null) {
			mSet.add(edge);	// なければ追加
			return edge;
		}
		
		return ret;
	}

	/**
	 * 頂点からedgeを探して返す
	 * @param v0
	 * @param v1
	 * @return
	 */
	public Vector2D getEdge(Vertex2D v0, Vertex2D v1) {
		Log.d(TAG, "v0: " + v0 + ", v1:" + v1);
		// Setを調べる
		for (Vector2D e : mSet) {
			Vertex2D from = e.vt0;
			Vertex2D to = e.vt1;
			Log.d(TAG, "from: " + from + ", to:" + to);
			
			if (v0.equals(from) && v1.equals(to))
				return e;	// 同じものがあればそれを返す
		}
		
		return null;
	}
	
	/**
	 * 始点からedgeを探してリストとして返す
	 * @param v
	 * @return
	 */
	public ArrayList<Vector2D> getEdges(Vertex2D v) {
		ArrayList<Vector2D> list = new ArrayList<Vector2D>();
		// Setを調べる
		for (Vector2D e : mSet) {
			Vertex2D from = e.vt0;
			Vertex2D to = e.vt1;
			Log.d(TAG, "from: " + from + ", to:" + to);
			
			// どちらかにあるか
			if (v.equals(from))
				list.add(e);	// リストに追加
		}
		
		return list;
	}
	
	public ArrayList<Vector2D> getEdgeList() {
		return mSet;
	}
}
