package jp.narit.lineartanalysis.graph;

import java.util.ArrayList;
import java.util.List;

import jp.narit.lineartanalysis.graph.LabelDictionary.Label;

/**
 * 頂点
 * 
 * @author tatsuya
 * 
 */
public class Vertex {
	String name;
	int edgeType;
	private ArrayList<Edge> mEdges = new ArrayList<Edge>();
	private ArrayList<List<Label>> mEdgeLableCandidates = new ArrayList<List<Label>>();
	private ArrayList<List<Label>> mLabelTypeCandidates = new ArrayList<List<Label>>();
	
	Vertex(String name, int type) {
		this.name = name;
		this.edgeType = type;
		initCandidates();
	}
	
	private void initCandidates() {
		switch (edgeType) {
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
		initEdgeLabelCandidates();
	}
	
	private void initEdgeLabelCandidates() {
		mEdgeLableCandidates.clear();
		for (int i = 0; i < mLabelTypeCandidates.get(0).size(); i++) {
			List<Label> each = new ArrayList<Label>();
			// 現在の頂点の候補を取り出していく
			for (List<Label> list : mLabelTypeCandidates) {
				// i番目の要素を追加していく
				each.add(list.get(i));
			}
			mEdgeLableCandidates.add(each);	// i番目に挿入していく
		}
	}
	
	boolean updateEdgeLabelCandidates() {
		boolean isUpdated = false;
		for (int i = 0; i < mEdgeLableCandidates.size(); i++) {
			// Edgeそれぞれに対するLabelのリストを取り出す
			List<Label> list = mEdgeLableCandidates.get(i);
			mEdgeLableCandidates.remove(i); // 消す
			
			// 対応するEdgeを取り出す
			Edge edge = mEdges.get(i);
			if (edge.andCandidates(list)) // 更新する
			isUpdated = true;
			
			// 更新した結果を対応する位置に格納しておく
			mEdgeLableCandidates.add(i, edge.getCandidates());
		}
		return isUpdated;
	}
	
	boolean updateLabelCandidates() {
		boolean isUpdated = false;
		for (int i = 0; i < mEdgeLableCandidates.size(); i++) {
			// i番目のedgeの候補を取得
			List<Label> list = mEdgeLableCandidates.get(i);
			
			ArrayList<List<Label>> newList = new ArrayList<List<Label>>(mLabelTypeCandidates);
			// 現在の頂点のlabelの種類の候補を取得する
			for (List<Label> candidates : newList) {
				// 候補のi番目がi番目のedgeの候補にあるか
				if (!list.contains(candidates.get(i))) {
					mLabelTypeCandidates.remove(candidates);	// 含まれないならば現在の頂点から候補を消す
					isUpdated = true;
				}
			}
		}
		initEdgeLabelCandidates();	// edgeのラベル候補を初期化
		return isUpdated;
	}
	
}
