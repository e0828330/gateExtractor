package main;

import gate.Corpus;
import gate.Factory;
import gate.Gate;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import output.OutputGenerator;
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

		boolean train = false;
		boolean eval = false;
		
		/* Load Corpus */
		log.info("Loading Corpus ... ");
		Corpus corpus = Factory.newCorpus("Training Corpus"); 
		// TODO -this is hardcoded
		File directory = new File("/home/linux/Dokumente/Information Retrieval/train/key-gate"); 
		URL url = directory.toURI().toURL();
		corpus.populate(url, null, null, true);
		log.info("Done loading Corpus!");

		Pipeline pipeline = null;

		/* Do Tagging */
		pipeline = new Tagger();
		pipeline.run(corpus, resourcesFolder);

		/* Train */
		// TODO: Run when specified on the cmd line
		if (train) {
			pipeline = new Trainer();
			pipeline.run(corpus, resourcesFolder);
		}
		
		/* Apply learned rules */
		pipeline = new Extractor();
		pipeline.run(corpus, resourcesFolder);

		ExecutorService executorService = Executors.newFixedThreadPool(20);
		for (int i = 0; i < corpus.size(); i++) {
			executorService.execute(new OutputGenerator("/tmp/results/", corpus.get(i)));
		}
		executorService.shutdown();
		executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		
		/* Evaluate results */
		// TODO: Run when specified on the cmd line
		if (eval) {
			pipeline = new Evaluator();
			pipeline.run(corpus, resourcesFolder);
		}
	}

}
