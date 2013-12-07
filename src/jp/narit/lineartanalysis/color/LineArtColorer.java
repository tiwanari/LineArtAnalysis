package jp.narit.lineartanalysis.color;

import jp.narit.lineartanalysis.analyze.Edge;
import jp.narit.lineartanalysis.analyze.EdgeSet;
import jp.narit.lineartanalysis.decode.Vector2D;
import jp.narit.lineartanalysis.decode.Vector2DSet;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.util.Log;

public class LineArtColorer {
	
	private static final String TAG = "LineArtColorer";
	private static final double LENGTH = 30.0;

	public static Mat colorLineArtByEdgeInfo(Mat target, Vector2DSet vectors, EdgeSet edges) {
		
		for (Vector2D vec : vectors.getVectorList()) {
			// 中点を求める
			Point mid = new Point((vec.from.point.x + vec.to.point.x) / 2.0,
					(vec.from.point.y + vec.to.point.y) / 2.0);
			// 辺の種類で描画を変える
			Edge edge = edges.getEdge(vec.from.name, vec.to.name);
			switch (edge.getCandidate()) {
				case INWARD: {
					Point to = edge.getFrom().equals(vec.from.name) ? vec.from.point : vec.to.point;
					drawArrow(target, mid, to);
					break;
				}
				case MINUS: {
					Core.putText(target, "-", mid, Core.FONT_HERSHEY_TRIPLEX, 2, new Scalar(0, 0, 255, 0));
					break;
				}
				case OUTWARD: {
					Point to = edge.getFrom().equals(vec.from.name) ? vec.to.point : vec.from.point;
					drawArrow(target, mid, to);
					break;
				}
				case PLUS: {
					Core.putText(target, "+", mid, Core.FONT_HERSHEY_TRIPLEX, 2, new Scalar(0, 0, 255, 0));
					break;
				}
				default: {
					Log.d(TAG, "something wrong");
					break;
				}
			}
			
		}
		
		return target;
	}
	
	private static Mat drawArrow(Mat target, Point mid, Point to) {
		
		Point vec = new Point(mid.x - to.x, mid.y - to.y);
		double norm = Math.sqrt(vec.x * vec.x + vec.y * vec.y);
		
		// 長さを揃える
		Point nvec = new Point(LENGTH * vec.x / norm, LENGTH * vec.y / norm);
		
		// 矢印の端を求める
		double theta = Math.PI / 6.0;	// 30度ずらす
		double xRight = Math.cos(theta) * nvec.x + Math.sin(theta) * nvec.y;
		double yRight = -Math.sin(theta) * nvec.x + Math.cos(theta) * nvec.y;
		double xLeft = Math.cos(-theta) * nvec.x + Math.sin(-theta) * nvec.y;
		double yLeft = -Math.sin(-theta) * nvec.x + Math.cos(-theta) * nvec.y;
		
		Point right = new Point(xRight + mid.x, yRight + mid.y);
		Point left = new Point(xLeft + mid.x, yLeft + mid.y);

		Core.line(target, mid, right, new Scalar(0, 0, 255, 0));
		Core.line(target, mid, left, new Scalar(0, 0, 255, 0));
		
;		return target;
	}
}
