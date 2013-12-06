package jp.narit.lineartanalysis.decode;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.util.Log;

/**
 * 画像の細線化を行う
 * 参考URL:
 * http://www.eml.ele.cst.nihon-u.ac.jp/~momma/wiki/wiki.cgi/OpenCV/%E7%B4%B0%E7
 * %B7%9A%E5%8C%96.html#h7
 * 
 * @author tatsuya
 * 
 */
public class LineArtPreprocessor {
	private static final String TAG = "LineThinner";
	// Filter2D用のカーネル
	// アルゴリズムでは白，黒のマッチングとなっているのをmWhiteKernelsカーネルと二値画像，
	// mBlackKernelsカーネルと反転した二値画像の2組に分けて畳み込み，その後でANDをとる
	@SuppressWarnings("serial")
	private static ArrayList<Mat> mWhiteKernels = new ArrayList<Mat>(8) {
		{
			// Kernel1
			Mat mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(1, 1, 1.0); mat.put(1, 2, 1.0); mat.put(2, 1, 1.0);
			add(mat);
			// Kernel2
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(1, 1, 1.0); mat.put(2, 0, 1.0); mat.put(2, 1, 1.0);
			add(mat);
			// Kernel3
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(1, 0, 1.0); mat.put(1, 1, 1.0); mat.put(2, 1, 1.0);
			add(mat);
			// Kernel4
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(0, 0, 1.0); mat.put(1, 0, 1.0); mat.put(1, 1, 1.0);
			add(mat);
			// Kernel5
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(0, 1, 1.0); mat.put(1, 1, 1.0); mat.put(1, 0, 1.0);
			add(mat);
			// Kernel6
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(0, 2, 1.0); mat.put(0, 1, 1.0); mat.put(1, 1, 1.0);
			add(mat);
			// Kernel7
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(0, 1, 1.0); mat.put(1, 1, 1.0); mat.put(1, 2, 1.0);
			add(mat);
			// Kernel8
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(1, 1, 1.0); mat.put(1, 2, 1.0); mat.put(2, 2, 1.0);
			add(mat);
		}
	};
	@SuppressWarnings("serial")
	private static ArrayList<Mat> mBlackKernels = new ArrayList<Mat>(8) {
		{
			// Kernel1
			Mat mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(0, 0, 1.0); mat.put(0, 1, 1.0); mat.put(1, 0, 1.0);
			add(mat);
			// Kernel2
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(0, 0, 1.0); mat.put(0, 1, 1.0); mat.put(0, 2, 1.0);
			add(mat);
			// Kernel3
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(0, 1, 1.0); mat.put(0, 2, 1.0); mat.put(1, 2, 1.0);
			add(mat);
			// Kernel4
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(0, 2, 1.0); mat.put(1, 2, 1.0); mat.put(2, 2, 1.0);
			add(mat);
			// Kernel5
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(1, 2, 1.0); mat.put(2, 2, 1.0); mat.put(2, 1, 1.0);
			add(mat);
			// Kernel6
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(2, 0, 1.0); mat.put(2, 1, 1.0); mat.put(2, 2, 1.0);
			add(mat);
			// Kernel7
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(1, 0, 1.0); mat.put(2, 0, 1.0); mat.put(2, 1, 1.0);
			add(mat);
			// Kernel8
			mat = new Mat(3, 3, CvType.CV_8UC1, new Scalar(0));
			mat.put(0, 0, 1.0); mat.put(1, 0, 1.0); mat.put(2, 0, 1.0);
			add(mat);
		}
	};
	
	/**
	 * 前処理を行う
	 * @param target
	 * @return
	 */
	public static Mat preprocessing(Mat target) {
		Mat res = thining(target);
		return smoothing(res);
	}
	
	/**
	 * 画像を細線化する
	 * 対象は白地に黒
	 * @param target
	 * @return
	 */
	public static Mat thining(Mat target) {
		Mat gray = new Mat(target.size(), CvType.CV_32F);
		Mat res = new Mat(target.size(), CvType.CV_32F);	// 結果
		Mat bin = new Mat(target.size(), CvType.CV_32F);	// 作業用
		Mat binInv = new Mat(target.size(), CvType.CV_32F);	// 作業用(反転)
		
		
		// グレー画像へ
		Imgproc.cvtColor(target, gray, Imgproc.COLOR_RGB2GRAY);
		
		// 2値化
		Imgproc.threshold(gray, res, 0, 1, Imgproc.THRESH_BINARY_INV|Imgproc.THRESH_OTSU);
		Imgproc.threshold(gray, bin, 0, 1, Imgproc.THRESH_BINARY_INV|Imgproc.THRESH_OTSU);
		Imgproc.threshold(gray, binInv, 0, 1, Imgproc.THRESH_BINARY|Imgproc.THRESH_OTSU);
		
		// 1ターンでマッチしてなければ終了
		double sum;
		do {
			sum = 0.0;
			assert(mBlackKernels.size() == mWhiteKernels.size());
			for (int i = 0; i < mBlackKernels.size(); i++) {
				Imgproc.filter2D(bin, bin, bin.depth(), mWhiteKernels.get(i));
				Imgproc.filter2D(binInv, binInv, binInv.depth(), mBlackKernels.get(i));
				// 各カーネルで注目するのは3画素ずつなので，マッチした注目画素の濃度は3となる
				Imgproc.threshold(bin, bin, 2.99, 1, Imgproc.THRESH_BINARY);
				Imgproc.threshold(binInv, binInv, 2.99, 1, Imgproc.THRESH_BINARY);
				// ここまではよい
				
				Core.bitwise_and(bin, binInv, bin);
				
				//この時点でのsrc_wが消去候補点となり，全カーネルで候補点が0となった時に処理が終わる
				sum += Core.sumElems(bin).val[0];
				
				// 原画像から候補点を消去(二値画像なのでXor)
				Core.bitwise_xor(res, bin, res);
				
				// 作業バッファを更新
				res.copyTo(bin);
				Imgproc.threshold(res, binInv, 0.5, 1, Imgproc.THRESH_BINARY_INV);
			}
			Log.d(TAG, "sum: " + sum);
		} while (sum > 0);
		Core.multiply(res, new Scalar(255), res);	// 255に戻す
		
		return res;
	}
	
	/**
	 * なめらかな図形に直す
	 * @param mat
	 * @return
	 */
	public static Mat smoothing(Mat mat) {
		Mat res = new Mat(mat.size(), mat.type());
		
		// 輪郭の抽出
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(mat, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// RGB画像へ
		Imgproc.cvtColor(res, res, Imgproc.COLOR_GRAY2BGRA);

		// 境界をなぞっていく
		for (int i = 0; i < contours.size(); i++) {
			MatOfPoint m = contours.get(i);
			
			for (int j = 0; j < m.rows() - 1; j++) {
				Point p0 = new Point(m.get(j, 0)[0], m.get(j, 0)[1]);
				Point p1 = new Point(m.get(j+1, 0)[0], m.get(j+1, 0)[1]);
				Core.line(res, p0, p1, new Scalar(255, 255, 255, 0), 1);
			}
		}
		// ガウシアンフィルタを掛ける
		Imgproc.GaussianBlur(res, res, new Size(3, 3), 0);
		
		// グレー画像へ
		Imgproc.cvtColor(res, res, Imgproc.COLOR_RGB2GRAY);
		
		return res;
	}
}