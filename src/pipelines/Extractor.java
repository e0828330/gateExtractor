package pipelines;

import java.io.File;

import org.apache.log4j.Logger;

import gate.Corpus;
import gate.Factory;
import gate.creole.SerialAnalyserController;
import gate.util.persistence.PersistenceManager;

public class Extractor implements Pipeline {

	Logger log = Logger.getLogger(Extractor.class);
	
	@Override
	public void run(Corpus corpus, String resourcesFolder) throws Exception {
		log.info("Starting extractor ..");
		SerialAnalyserController controller = (SerialAnalyserController) PersistenceManager.loadObjectFromFile(new File(resourcesFolder + "/ml.xgapp"));
		controller.setCorpus(corpus);
		controller.execute();
		log.info("Extraction done!");
		Factory.deleteResource(controller);
	}

}
