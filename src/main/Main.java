package main;

import gate.Corpus;
import gate.Factory;
import gate.Gate;
import gnu.getopt.Getopt;

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
	
	private static void usage(String msg) {
		if (msg != null) {
			System.err.println(msg);
		}
		System.err.println("Usage:");
		System.err.println("java -jar gateExtractor.jar -i <inputpath> [-t] [-r -o <outputpath>] [-e]");
		System.exit(1);
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		/* Parse command line arguments */
		Getopt g = new Getopt("gateExtractor", args, "i:o:tre");
		g.setOpterr(false);
		
		String inputPath = "";
		String outputPath = "";
		
		boolean train = false;
		boolean eval = false;
		boolean run = false;
		
		int c;
		String arg;
		while ((c = g.getopt()) != -1) {
			switch (c) {
				case 'i':
					arg = g.getOptarg();
					if (arg == null || arg.isEmpty()) {
						usage("Please provide an input path");
					}
					inputPath = arg;
					break;
				case 'o':
					outputPath = g.getOptarg();
					break;
				case 't':
					train = true;
					break;
				case 'r':
					run = true;
					break;
				case 'e':
					eval = true;
					break;
				case '?':
				default:
					usage(null);
					
			}
		}
		
		if (args.length == 0 || (!run && !train && !eval)) {
			usage("Nothing to do.");
		}
		
		if (inputPath == null || inputPath.isEmpty()) {
			usage("Please provide an input path");
		}
		
		if (run && (outputPath == null || outputPath.isEmpty())) {
			usage("Please provide an output directory!");
		}

		
		/* Initialize GATE */
		String location = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
		String resourcesFolder = location + "/resources";
		Gate.setGateHome(new File(resourcesFolder));
		Gate.init();
		
		/* Load Corpus */
		log.info("Loading Corpus ... ");
		Corpus corpus = Factory.newCorpus("Training Corpus"); 
		File directory = new File(inputPath); 
		URL url = directory.toURI().toURL();
		corpus.populate(url, null, null, true);
		log.info("Done loading Corpus!");

		Pipeline pipeline = null;

		/* Do Tagging */
		pipeline = new Tagger();
		pipeline.run(corpus, resourcesFolder);

		/* Train */
		if (train) {
			pipeline = new Trainer();
			pipeline.run(corpus, resourcesFolder);
		}
		
		/* Apply learned rules */
		if (run) {
			pipeline = new Extractor();
			pipeline.run(corpus, resourcesFolder);
	
			ExecutorService executorService = Executors.newFixedThreadPool(20);
			for (int i = 0; i < corpus.size(); i++) {
				executorService.execute(new OutputGenerator(outputPath, corpus.get(i)));
			}
			executorService.shutdown();
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		}
		
		/* Evaluate results */
		if (eval) {
			pipeline = new Evaluator();
			pipeline.run(corpus, resourcesFolder);
		}
	}

}
