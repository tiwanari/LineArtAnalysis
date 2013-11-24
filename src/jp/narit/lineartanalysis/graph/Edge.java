package jp.narit.lineartanalysis.graph;

import java.util.ArrayList;
import java.util.List;

import jp.narit.lineartanalysis.graph.LabelDictionary.Label;

/**
 * 頂点
 * @author tatsuya
 *
 */
public class Edge {
	Vertex from;
	Vertex to;
	private ArrayList<Label> mLabelCandidates = new ArrayList<Label>(4);
	
	public Edge(Vertex from, Vertex to) {
		this.from = from;
		this.to = to;
	}
	
	/**
	 * Edgeの種類の候補を返す
	 * @return
	 */
	ArrayList<Label> getCandidates() {
		return mLabelCandidates;
	}
	
	/**
	 * 候補を追加する
	 * @param candidate
	 */
	void addCandidate(Label candidate) {
		if (!mLabelCandidates.contains(candidate))
			mLabelCandidates.add(candidate);
	}
	
	/**
	 * 候補を消す
	 * @param candidate
	 */
	void removeCandidate(LabelDictionary candidate) {
		mLabelCandidates.remove(candidate);
	}
	
	/**
	 * 2つのEdgeの候補の共通部分をとって返す
	 * @param list
	 * @return boolean 更新されたかどうか
	 */
	boolean andCandidates(List<Label> list) {
		ArrayList<Label> newCandidates = new ArrayList<Label>();
		for (Label type: list) {
			if (mLabelCandidates.contains(type)) {
				newCandidates.add(type);
			}
		}
		boolean notUpdated = mLabelCandidates.containsAll(newCandidates);
		mLabelCandidates = newCandidates;	// 更新
		
		return !notUpdated;
	}
}
