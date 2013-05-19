gateExtractor
=============

1. Build

To build a runnable jar just run "ant" in the source code root folder,
make sure to not move the jar it requires the files from resources to run.

2. Running

The application is controlled throught a command line interface:

# java -Xmx4G -jar gateExtractor.jar -i <inputpath> [-t] [-r <outputpath>] [-e]

The first parameter -i is mandatory and provides the path to the corpus.

The -t (training) parameter starts the training mode, this has to be done
atleast once with the testing corpus for the actual application to run.

The -r (run) parameter applies the learned rules on the corpus specified
using the -i option. The outputpath is a directory where the results will
be saved (HTML files).

The -e (evaluate) paramter does a k-fold cross validation using k=4.

The parameters can be combined i.e do just run training application
and evaluation one can just do:

# java -Xmx4G -jar gateExtractor.jar -i corpusDir/ -t -r outputDir/ -e




