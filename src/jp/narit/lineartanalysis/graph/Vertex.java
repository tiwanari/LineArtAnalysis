package jp.narit.lineartanalysis.graph;

import java.util.ArrayList;
import java.util.List;

import jp.narit.lineartanalysis.graph.LabelDictionary.Label;

/**
 * 頂点
 * @author tatsuya
 *
 */
public class Vertex {
	String name;
	int edgeType;
	private ArrayList<Edge> mEdges = new ArrayList<Edge>();
	private ArrayList<List<Label>> mEdgeTypes = new ArrayList<List<Label>>();
	ArrayList<List<Label>> labelTypeCandidates = new ArrayList<List<Label>>();
	
	Vertex(String name, int type) {
		this.name = name;
		this.edgeType = type;
		initCandidates();
	}
	
	private void initCandidates() {
		switch (edgeType) {
			case LabelDictionary.L:
				labelTypeCandidates.addAll(LabelDictionary.LTYPES);
				break;
			case LabelDictionary.A:
				labelTypeCandidates.addAll(LabelDictionary.ATYPES);
				break;
			case LabelDictionary.Y:
				labelTypeCandidates.addAll(LabelDictionary.YTYPES);
				break;
			case LabelDictionary.T:
				labelTypeCandidates.addAll(LabelDictionary.TTYPES);
				break;
		}
		initEdgeLabels();
	}
	
	private void initEdgeLabels() {
		for (int i = 0; i < labelTypeCandidates.get(0).size(); i++) {
			List<Label> each = new ArrayList<Label>();
			for (List<Label> list : labelTypeCandidates) {
				each.add(list.get(i));
			}
			mEdgeTypes.add(each);
		}
	}

	boolean updateEdges() {
		boolean isUpdated = false;
		
		for (int i = 0; i < mEdgeTypes.size(); i++) {
			List<Label> list = mEdgeTypes.get(i);
			Edge edge = mEdges.get(i);
			edge.andCandidates(list);
		}
		return isUpdated;
	}
	
}
