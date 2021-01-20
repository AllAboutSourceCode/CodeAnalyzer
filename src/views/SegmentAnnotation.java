package views;

import org.eclipse.jface.text.source.Annotation;

public class SegmentAnnotation extends Annotation {
	public static final String SEGMENT = "codeanalyzer.segmentAnnotation";
	public SegmentAnnotation(String type, String text) {
		super(type, false, text);
	}
}

