package pipelines;

import gate.Corpus;
import gate.creole.SerialAnalyserController;
import gate.util.persistence.PersistenceManager;

import java.io.File;

import org.apache.log4j.Logger;

public class Trainer implements Pipeline {

	Logger log = Logger.getLogger(Trainer.class);
	
	public void run(Corpus corpus, String resourcesFolder) throws Exception {
		log.info("Starting trainer ..");
		SerialAnalyserController controller = (SerialAnalyserController) PersistenceManager.loadObjectFromFile(new File(resourcesFolder + "/train.xgapp"));
		controller.setCorpus(corpus);
		controller.execute();
		log.info("Training done!");
	}


}
