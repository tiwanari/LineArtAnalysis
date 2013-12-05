package jp.narit.lineartanalysis.decode;

import java.util.ArrayList;


/**
 * vectorの集合
 * @author tatsuya
 *
 */
public class Vector2DSet {
	@SuppressWarnings("unused")
	private static final String TAG = "Vector2DSet";
	private ArrayList<Vector2D> mSet = new ArrayList<Vector2D>();
	
	public Vector2DSet() {
		init();
	}
	
	public void init() {
		mSet.clear();
	}
	
	public Vector2D addVector(Vector2D edge) {
		Vertex2D from = edge.from;
		Vertex2D to = edge.to;

		if (from.equals(to))	return null;	// 同じなら追加しない
		
		Vector2D ret;
		if ((ret = getVector(from, to)) == null) {
			mSet.add(edge);	// なければ追加
			return edge;
		}
		
		return ret;
	}

	/**
	 * 頂点からvectorを探して返す
	 * @param v0
	 * @param v1
	 * @return
	 */
	public Vector2D getVector(Vertex2D v0, Vertex2D v1) {
		// Setを調べる
		for (Vector2D e : mSet) {
			Vertex2D from = e.from;
			Vertex2D to = e.to;
			
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
	public ArrayList<Vector2D> getVectors(Vertex2D v) {
		ArrayList<Vector2D> list = new ArrayList<Vector2D>();
		// Setを調べる
		for (Vector2D e : mSet) {
			Vertex2D from = e.from;
			
			// どちらかにあるか
			if (v.equals(from))
				list.add(e);	// リストに追加
		}
		
		return list;
	}
	
	public ArrayList<Vector2D> getVectorList() {
		return mSet;
	}
}
