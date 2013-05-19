package main;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;

import pipelines.Evaluator;
import pipelines.Extractor;
import pipelines.Pipeline;
import pipelines.Tagger;
import pipelines.Trainer;

public class Main {

	private static Logger log = Logger.getLogger(Main.class); 
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		/* Initialize GATE */
		String location = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
		String resourcesFolder = location + "/resources";
		Gate.setGateHome(new File(resourcesFolder));
		Gate.init();

		/* Load Corpus */
		log.info("Loading Corpus ... ");
		Corpus corpus = Factory.newCorpus("Training Corpus"); 
		// TODO -this is hardcoded
		File directory = new File("/home/linux/Dokumente/Information Retrieval/train/key-gate"); 
		URL url = directory.toURI().toURL();
		corpus.populate(url, null, null, true);
		Document doc = corpus.get(0);
		log.info("Done loading Corpus!");

		Pipeline pipeline = null;

		/* Do Tagging */
		pipeline = new Tagger();
		pipeline.run(corpus, resourcesFolder);

		/* Train */
		pipeline = new Trainer();
		pipeline.run(corpus, resourcesFolder);
		
		/* Print out some data */
		System.out.println("List annotations of first document ...");
		AnnotationSet newAnnotations = doc.getAnnotations("Key");
		System.out.println("Showing document: " + doc.getName());
		for (Annotation annot : newAnnotations.get()) {
			if (annot.getType().equals("IE")) {
				System.out.print(annot.getFeatures().get("type") + " : ");
				System.out.println(doc.getContent().getContent(annot.getStartNode().getOffset(), annot.getEndNode().getOffset()));
			}
		}
		
		/* Apply learned rules */
		pipeline = new Extractor();
		pipeline.run(corpus, resourcesFolder);
		
		/* Print out some data */
		System.out.println("List annotations of first document ...");
		newAnnotations = doc.getAnnotations("Result");
		System.out.println("Showing document: " + doc.getName());
		for (Annotation annot : newAnnotations.get()) {
			if (annot.getType().equals("IE")) {
				System.out.print(annot.getFeatures().get("type") + " : ");
				System.out.println(doc.getContent().getContent(annot.getStartNode().getOffset(), annot.getEndNode().getOffset()));
			}
		}
		
		/* Evaluate results */
		pipeline = new Evaluator();
		pipeline.run(corpus, resourcesFolder);
	}

}
