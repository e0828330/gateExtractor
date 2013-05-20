package main;

import gate.Corpus;
import gate.Factory;
import gate.Gate;
import gnu.getopt.Getopt;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import output.OutputGenerator;
import pipelines.Evaluator;
import pipelines.Extractor;
import pipelines.Pipeline;
import pipelines.Tagger;
import pipelines.Trainer;

public class Main {

	private static Logger log = Logger.getLogger(Main.class);

	private static File outputFile_mlConfigThreads;

	private static void usage(String msg) {
		if (msg != null) {
			System.err.println(msg);
		}
		System.err.println("Usage:");
		System.err.println("java -jar gateExtractor.jar -i <inputpath> [-t] [-r <outputpath>] [-e]");
		System.exit(1);
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		/* Parse command line arguments */
		Getopt g = new Getopt("gateExtractor", args, "i:r:te");
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
				case 'r':
					run = true;
					arg = g.getOptarg();
					if (arg == null || arg.isEmpty()) {
						usage("Please provide an output path");
					}
					outputPath = arg;
					break;
				case 't':
					train = true;
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

		if (train && eval) {
			usage("Only one mode allowed at a time");
		}
		
		if (train && run) {
			usage("Only one mode allowed at a time");
		}
		
		if (eval && run) {
			usage("Only one mode allowed at a time");
		}
		
		/* Initialize GATE */
		String location = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
		String resourcesFolder = location + "/resources";
		Gate.setGateHome(new File(resourcesFolder));

		/* Create ml-config.xml with threads */

		createConfig(resourcesFolder + File.separator);
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

		/* Clean up */
		Factory.deleteResource(corpus);
		outputFile_mlConfigThreads.delete();
	}

	private static void createConfig(String path) throws IOException, ParserConfigurationException, SAXException, TransformerException {
		String mlConfig = path + "ml-config.xml";
		String mlConfigThreads = path + "ml-config-threads.xml";

		log.debug("Source for mlConfig:  " + mlConfig);
		log.debug("Source for mlConfigThreads:  " + mlConfigThreads);

		File inputFile = new File(mlConfig);
	    outputFile_mlConfigThreads = new File(mlConfigThreads);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		log.debug("Put " + mlConfig + " into dbBuilder for parsing");
		Document doc = dBuilder.parse(inputFile);

		//optional, but recommended
		doc.getDocumentElement().normalize();

		NodeList elements = doc.getElementsByTagName("multiClassification2Binary");
		if (elements.getLength() == 1) {
			Element element = (Element) elements.item(0);
			element.setAttribute("thread-pool-size", String.valueOf(Runtime.getRuntime().availableProcessors()));

		}

		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(outputFile_mlConfigThreads);

		transformer.transform(source, result);
	}


}
