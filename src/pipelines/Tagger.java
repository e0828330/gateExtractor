package pipelines;

import java.io.File;

import org.apache.log4j.Logger;

import gate.Corpus;
import gate.creole.SerialAnalyserController;
import gate.util.persistence.PersistenceManager;

public class Tagger implements Pipeline {

	Logger log = Logger.getLogger(Tagger.class);
	
	@Override
	public void run(Corpus corpus, String resourcesFolder) throws Exception {
		log.info("Starting tagger ..");
		SerialAnalyserController controller = (SerialAnalyserController) PersistenceManager.loadObjectFromFile(new File(resourcesFolder + "/tagger.xgapp"));
		controller.setCorpus(corpus);
		controller.execute();
		log.info("Tagging done!");
	}

}
