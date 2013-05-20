package pipelines;

import java.io.File;

import org.apache.log4j.Logger;

import gate.Corpus;
import gate.Factory;
import gate.creole.SerialAnalyserController;
import gate.util.persistence.PersistenceManager;

public class Evaluator implements Pipeline {

	Logger log = Logger.getLogger(Evaluator.class);
	
	@Override
	public void run(Corpus corpus, String resourcesFolder) throws Exception {
		log.info("Starting evaluator ..");
		SerialAnalyserController controller = (SerialAnalyserController) PersistenceManager.loadObjectFromFile(new File(resourcesFolder + "/eval.xgapp"));
		controller.setCorpus(corpus);
		controller.execute();
		log.info("Evaluation done!");
		Factory.deleteResource(controller);
	}


}
