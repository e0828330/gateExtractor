package pipelines;

import gate.Corpus;
import gate.Factory;
import gate.creole.SerialAnalyserController;
import gate.event.StatusListener;
import gate.util.persistence.PersistenceManager;

import java.io.File;

import org.apache.log4j.Logger;

public class Tagger implements Pipeline {

	Logger log = Logger.getLogger(Tagger.class);
	
	@Override
	public void run(Corpus corpus, String resourcesFolder) throws Exception {
		log.info("Starting tagger ..");
		SerialAnalyserController controller = (SerialAnalyserController) PersistenceManager.loadObjectFromFile(new File(resourcesFolder + "/tagger.xgapp"));
		final int numDocs = corpus.size();
		controller.addStatusListener(new StatusListener() {
			private int processed = 0;
			@Override
			public void statusChanged(String msg) {
				if (msg.startsWith("Finished")) {
					processed++;
					log.info("Processed " + processed + " out of " + numDocs + " documents.");
				}
			}
		});
		controller.setCorpus(corpus);
		controller.execute();
		log.info("Tagging done!");
		Factory.deleteResource(controller);
	}

}
