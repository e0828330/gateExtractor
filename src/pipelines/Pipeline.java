package pipelines;

import gate.Corpus;

public interface Pipeline {
	public void run(Corpus corpus, String resourcesFolder) throws Exception;
}
