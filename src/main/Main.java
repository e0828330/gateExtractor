package main;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Main {

	/**
	 * @param args
	 * @throws GateException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws GateException, IOException {
		/* Initialize GATE */
		String location = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
		String resourcesFolder = location + "/resources";
		Gate.setGateHome(new File(resourcesFolder));
		Gate.init();
				
		/* Load Application States */
		System.out.println("Loading application states ...");
		SerialAnalyserController tagger = (SerialAnalyserController) PersistenceManager.loadObjectFromFile(new File(resourcesFolder + "/tagger.xgapp"));
		SerialAnalyserController trainer = (SerialAnalyserController) PersistenceManager.loadObjectFromFile(new File(resourcesFolder + "/train.xgapp"));
		SerialAnalyserController main = (SerialAnalyserController) PersistenceManager.loadObjectFromFile(new File(resourcesFolder + "/ml.xgapp"));
		SerialAnalyserController eval = (SerialAnalyserController) PersistenceManager.loadObjectFromFile(new File(resourcesFolder + "/eval.xgapp"));
		
		System.out.println("Done application states!");
	
		/* Load Corpus */
		System.out.println("Loading Corpus ... ");
		Corpus corpus = Factory.newCorpus("My XML Files"); 
		// TODO -this is hardcoded
		File directory = new File("/home/linux/Dokumente/Information Retrieval/train/key-gate"); 
		URL url = directory.toURI().toURL();
		corpus.populate(url, null, null, true);
		Document doc = corpus.get(0);
		System.out.println("Done loading Corpus!");

		/* Do Tagging */
		System.out.println("Running Tagger ...");
		tagger.setCorpus(corpus);
		tagger.execute();
		System.out.println("Tagging done!");
		
		/* Train */
		System.out.println("Running Trainer ...");
		trainer.setCorpus(corpus);
		trainer.execute();
		System.out.println("Training Done!");
		
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
		System.out.println("Start Application ...");
		main.setCorpus(corpus);
		main.execute();
		System.out.println("Application Done!");
		
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
		System.out.println("Running Evaluation ...");
		eval.setCorpus(corpus);
		eval.execute();
		System.out.println("Evaluation done!");
		
		
	}

}
