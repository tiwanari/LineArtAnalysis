package jp.narit.lineartanalysis.graph;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import jp.narit.lineartanalysis.graph.LabelDictionary.Label;

/**
 * 頂点
 * 
 * @author tatsuya
 * 
 */
public class Vertex {
	private static final String TAG = "Vertex";
	private String mName;
	private int mEdgeType;
	private ArrayList<Edge> mEdges = new ArrayList<Edge>();
	private ArrayList<List<Label>> mEdgeLableCandidates = new ArrayList<List<Label>>();
	private ArrayList<List<Label>> mLabelTypeCandidates = new ArrayList<List<Label>>();
	
	public Vertex(String name, int type) {
		this.mName = name;
		this.mEdgeType = type;
	}
	
	/**
	 * 辺を登録し終わった時点で候補の初期化を行う
	 */
	public void initCandidates() {
		switch (mEdgeType) {
			case LabelDictionary.L:
				mLabelTypeCandidates.addAll(LabelDictionary.LTYPES);
				break;
			case LabelDictionary.A:
				mLabelTypeCandidates.addAll(LabelDictionary.ATYPES);
				break;
			case LabelDictionary.Y:
				mLabelTypeCandidates.addAll(LabelDictionary.YTYPES);
				break;
			case LabelDictionary.T:
				mLabelTypeCandidates.addAll(LabelDictionary.TTYPES);
				break;
		}
		Log.d(TAG, "(" + mName + ") EdgeType: " + LabelDictionary.getLabelString(mEdgeType) + ", LabelTypeCandidates: " + mLabelTypeCandidates.toString());
		initEdgeLabelCandidates();
	}
	
	/**
	 * edgeの候補の初期化
	 */
	private void initEdgeLabelCandidates() {
		for (int i = 0; i < mEdges.size(); i++) {
			List<Label> each = new ArrayList<Label>();
			// 現在の頂点の候補を取り出していく
			for (List<Label> list : mLabelTypeCandidates) {
				// 含んでいないならi番目の要素を追加していく
				if (!each.contains(list.get(i)))
					each.add(list.get(i));
			}
			mEdgeLableCandidates.add(each);	// i番目に挿入していく
		}
		Log.d(TAG, "(" + mName + ") initialized edge label: " + mEdgeLableCandidates.toString());
	}

	/**
	 * edgeの候補を再設定
	 */
	private void assignEdgeLabelCandidates() {
		mEdgeLableCandidates.clear();
		for (int i = 0; i < mEdges.size(); i++) {
			List<Label> each = new ArrayList<Label>();
			// 現在の頂点の候補を取り出していく
			for (List<Label> list : mLabelTypeCandidates) {
				// 含んでいないならi番目の要素を追加していく
				if (!each.contains(list.get(i)))
					each.add(list.get(i));
			}
			mEdgeLableCandidates.add(each);	// i番目に挿入していく
		}
	}
	
	/**
	 * Edgeを頂点と紐付ける
	 * @param edge
	 * @return
	 */
	public boolean addEdge(Edge edge) {
		if (mEdges.contains(edge))
			return false;
		
		mEdges.add(edge);
		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean updateEdgeLabelCandidates() {
		boolean isUpdated = false;
		Log.d(TAG, "(" + mName + ") update edge label: " + mEdgeLableCandidates.toString());
		for (int i = 0; i < mEdgeLableCandidates.size(); i++) {
			// Edgeそれぞれに対するLabelのリストを取り出す
			List<Label> list = mEdgeLableCandidates.get(i);
			mEdgeLableCandidates.remove(i); // 消す
			
			// 対応するEdgeを取り出す
			Edge edge = mEdges.get(i);
			if (edge.andCandidates(list, mName)) // 更新する
				isUpdated = true;
			
			// 更新した結果を対応する位置に格納しておく
			mEdgeLableCandidates.add(i, edge.getCandidatesConsideredDirection(mName));
		}
		Log.d(TAG, "(" + mName + ") updated edge label: " + mEdgeLableCandidates.toString());
		return isUpdated;
	}
	
	/**
	 * 周囲のedgeのlabelの候補を現在の頂点の種類から更新
	 * @return
	 */
	public boolean updateLabelCandidates() {
		boolean isUpdated = false;
		Log.d(TAG, "(" + mName + ") update vertex label: " + mLabelTypeCandidates.toString());
		for (int i = 0; i < mEdgeLableCandidates.size(); i++) {
			// i番目のedgeの候補を取得
			List<Label> list = mEdgeLableCandidates.get(i);
			
			ArrayList<List<Label>> newList = new ArrayList<List<Label>>(mLabelTypeCandidates);
			
			assert(mLabelTypeCandidates.equals(newList));	// 同じもののはず
			
			// 現在の頂点のlabelの種類の候補を取得する
			for (List<Label> candidates : newList) {
				// 候補のi番目がi番目のedgeの候補にあるか
				if (!list.contains(candidates.get(i))) {
					mLabelTypeCandidates.remove(candidates);	// 含まれないならば現在の頂点から候補を消す
					isUpdated = true;
				}
			}
		}
		Log.d(TAG, "(" + mName + ") updated vertex label: " + mLabelTypeCandidates.toString());
		assignEdgeLabelCandidates();	// edgeの候補を再設定
		return isUpdated;
	}
	
}
