package output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.util.InvalidOffsetException;

public class OutputGenerator implements Runnable {

	private String targetDirectory;
	private Document doc;
	
	public OutputGenerator(String targetDirectory, Document doc) {
		this.targetDirectory = targetDirectory;
		this.doc = doc;
	}
	
	@Override
	public void run() {
		StringBuilder sb = new StringBuilder();
		sb.append(getHTMLhead(doc.getName()));

		AnnotationSet resultAnnotations = doc.getAnnotations("Result");
		for (Annotation annot : resultAnnotations.get()) {
			if (annot.getType().equals("IE")) {
				sb.append("<tr>");
				sb.append("<td>" + annot.getFeatures().get("type") + "</td>");
				try {
					String value = doc.getContent().getContent(annot.getStartNode().getOffset(), annot.getEndNode().getOffset()).toString();
					sb.append("<td>" + value + "</td>");
				} catch (InvalidOffsetException e) {
					sb.append("<td>!ERROR!</td>");
				}
				sb.append("</tr>");
			}
		}
		sb.append(getHTMLfoot());
		
		FileWriter fstream;
		try {
			fstream = new FileWriter(targetDirectory + "/" + doc.getName() + ".html");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(sb.toString());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String getHTMLhead(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>");
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<title>" + name + "</title>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("<h1>" + name + "</h1>");
		sb.append("<table style='border-collapse: collapse;' cellpadding='8' border='1>");
		sb.append("<tr>");
		sb.append("<th>Attribute</th>");
		sb.append("<th>Value</th>");
		sb.append("</tr>");

		return sb.toString();
	}
	
	private String getHTMLfoot() {
		StringBuilder sb = new StringBuilder();
		sb.append("</table>");
		sb.append("</body>");
		sb.append("</html>");

		return sb.toString();
	}

}
