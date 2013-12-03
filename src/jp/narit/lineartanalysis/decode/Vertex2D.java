package jp.narit.lineartanalysis.decode;

import org.opencv.core.Point;

public class Vertex2D {
	Point point;
	String name;
	
	/**
	 * コンストラクタ
	 * @param name
	 * @param point
	 */
	public Vertex2D(String name, Point point) {
		this.name = name;
		this.point = point;
	}
}
