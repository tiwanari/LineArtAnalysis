package jp.narit.lineartanalysis.graph;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import jp.narit.lineartanalysis.graph.LabelDictionary.Label;

/**
 * 頂点
 * @author tatsuya
 *
 */
public class Edge {
	private static final String TAG = "Edge";
	private String mFrom;
	private String mTo;
	private ArrayList<Label> mLabelCandidates = new ArrayList<Label>(4);
	
	public Edge(String from, String to) {
		this.mFrom = from;
		this.mTo = to;
		initCandidates();
	}
	
	public String getFrom() {
		return mFrom;
	}
	
	public String getTo() {
		return mTo;
	}

	
	/**
	 * 候補を初期化する
	 * @param candidate
	 */
	private void initCandidates() {
		mLabelCandidates.add(Label.INWARD);
		mLabelCandidates.add(Label.OUTWARD);
		mLabelCandidates.add(Label.PLUS);
		mLabelCandidates.add(Label.MINUS);
	}

	/**
	 * 候補を消す
	 * @param candidate
	 */
	public void removeCandidate(Label candidate) {
		mLabelCandidates.remove(candidate);
	}

	/**
	 * 複数の候補を消す
	 * @param candidates
	 */
	public void removeCandidates(List<Label> candidates) {
		mLabelCandidates.removeAll(candidates);
	}
	
	/**
	 * 反転すべきときに，LabelがINWARD，OUTWARDであれば入れ替える
	 * @param label ラベル
	 * @param flag 反転すべきか
	 * @return
	 */
	private Label changeDirection(Label label, boolean flag) {
		// 反転すべきでないときは，そのままかえす
		if (!flag)
			return label;
		
		// 反転すべきときは，INWARDとOUTWARDのときだけ反転
		if (label == Label.INWARD)
			return Label.OUTWARD;
		else if (label == Label.OUTWARD)
			return Label.INWARD;
		
		return label;
	}
	
	/**
	 * 2つのEdgeの候補の共通部分をとって返す
	 * @param list
	 * @return boolean 更新されたかどうか
	 */
	public boolean andCandidates(List<Label> list, String from) {
		ArrayList<Label> newCandidates = new ArrayList<Label>();
		
		// 開始点が違うか判定
		boolean flag = !from.equals(this.mFrom);
		Log.d(TAG, "(" + showLine() + ") update label candidates: " + mLabelCandidates.toString());
		Log.d(TAG, "arg list: " + list.toString());
		
		// 開始点が逆の線分で共通部分を調べる際には，INWARDとOUTWARDが逆転する
		for (Label type: list) {
			// 反転を考慮した論理積を取る
			if (mLabelCandidates.contains(changeDirection(type, flag))) {
				newCandidates.add(changeDirection(type, flag));
			}
		}
		// 更新されたかどうか
		boolean notUpdated = mLabelCandidates.containsAll(newCandidates);
		
		mLabelCandidates = newCandidates;	// 更新
		
		Log.d(TAG, "(" + showLine() + ") updated label candidates: " + mLabelCandidates.toString());
		return !notUpdated;
	}
	
	/**
	 * この辺のラベル候補を返す
	 * 
	 * @param from 開始点の名前
	 * @return
	 */
	public List<Label> getCandidatesConsideredDirection(String from) {
		ArrayList<Label> newCandidates = new ArrayList<Label>();
		// 開始点が違うか判定
		boolean flag = !from.equals(this.mFrom);
		
		// 開始点が逆の線分で共通部分を調べる際には，INWARDとOUTWARDが逆転する
		for (Label label : mLabelCandidates) {
			newCandidates.add(changeDirection(label, flag));
		}
		return newCandidates;
	}

	public String showLine() {
		return mFrom + "-" + mTo;
	}
	
	public String showCandidates() {
		StringBuffer candidates = new StringBuffer();
		for (Label label : mLabelCandidates) {
			candidates.append(label.toString() + ", ");
		}
		candidates.append("\n");
		return candidates.toString();
	}
}
