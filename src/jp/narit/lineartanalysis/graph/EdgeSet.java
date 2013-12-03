package jp.narit.lineartanalysis.graph;

import java.util.ArrayList;

import android.util.Log;

/**
 * 辺の集合
 * @author tatsuya
 *
 */
public class EdgeSet {
	private static final String TAG = "EdgeSet";
	private ArrayList<Edge> mSet = new ArrayList<Edge>();
	
	public EdgeSet() {
		
	}
	
	public void init() {
		mSet.clear();
	}
	
	public Edge addEdge(Edge edge) {
		String from0 = edge.getFrom();
		String to0 = edge.getTo();
		
		// Setを調べる
		for (Edge e : mSet) {
			String from1 = e.getFrom();
			String to1 = e.getTo();
			
			// 向きを入れ替えたものも同じとする
			if ((from0.equals(from1) && to0.equals(to1))
					|| (to0.equals(from1) && from0.equals(to1)))
				return e;	// 同じものがあればそれを返す
		}
		mSet.add(edge);	// なければ追加
		return edge;
	}
	
	public String showResult() {
		StringBuffer result = new StringBuffer();
		for (Edge e : mSet) {
			if (e.getCandidatesConsideredDirection(e.getFrom()).isEmpty())
				return "この図形は不可能図形です．";
				
		}
		for (Edge e : mSet) {
			result.append(e.showLine() + ": " + e.showCandidates());
		}
		
		return result.toString();
	}

	/**
	 * 頂点からedgeを探して返す
	 * @param v0
	 * @param v1
	 * @return
	 */
	public Edge getEdge(String v0, String v1) {
		Log.d(TAG, "v0: " + v0 + ", v1:" + v1);
		// Setを調べる
		for (Edge e : mSet) {
			String from1 = e.getFrom();
			String to1 = e.getTo();
			Log.d(TAG, "from: " + from1 + ", to:" + to1);
			
			// 向きを入れ替えたものも同じとする
			if ((v0.equals(from1) && v1.equals(to1))
					|| (v1.equals(from1) && v0.equals(to1)))
				return e;	// 同じものがあればそれを返す
		}
		
		return null;
	}
}
