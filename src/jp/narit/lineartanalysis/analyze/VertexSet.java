package jp.narit.lineartanalysis.analyze;

import java.util.ArrayList;


/**
 * 頂点集合
 * @author tatsuya
 *
 */
public class VertexSet {
	ArrayList<Vertex> mSet = new ArrayList<Vertex>();
	
	public VertexSet() {
	}
	
	public void init() {
		mSet.clear();
	}
	
	/**
	 * 頂点の追加
	 * @param vertex
	 * @return 追加に成功したかどうか
	 */
	public boolean addVertex(Vertex vertex) {
		if (mSet.contains(vertex))
			return false;
		
		mSet.add(vertex);
		return true;
	}
	
	public boolean updateVertexLabels() {
		boolean isUpdate = false;
		for (Vertex v : mSet) {
			if (v.updateEdgeLabelCandidates() || v.updateLabelCandidates())
				isUpdate = true;
		}
		return isUpdate;
	}
}
