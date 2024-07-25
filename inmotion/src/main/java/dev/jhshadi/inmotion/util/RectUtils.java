package dev.jhshadi.inmotion.util;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.ArrayList;

import dev.jhshadi.inmotion.detectors.Face;


public class RectUtils {

	public static boolean contains(Point p, Rect r, double scalar) {
		Rect check;

		if (scalar == 1) {
			check = r;
		} else {
			check = r.clone();

			int scalarPixelsWidth = (int) ((r.width - (r.width * scalar)) / 2);
			int scalarPixelsHeight = (int) ((r.height - (r.height * scalar)) / 2);

			check.width *= scalar;
			check.height *= scalar;
			check.x += scalarPixelsWidth;
			check.y += scalarPixelsHeight;
		}

		return check.contains(p);
	}

	public static android.graphics.Point getCenter(Rect r) {
		return new android.graphics.Point(r.x + (r.width / 2), r.y
				+ (r.height / 2));
	}

	public static ArrayList<Face> toFaceArray(Rect[] arr) {
		ArrayList<Face> result = new ArrayList<>(arr.length);

		for (int i = 0; i < arr.length; i++) {
			result.add(new Face(arr[i]));
		}

		return result;
	}
}
