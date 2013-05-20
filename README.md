gateExtractor
=============

## Build

To build a runnable jar just run "ant" in the source code root folder,
make sure to not move the jar it requires the files from resources to run.

## Running

The application is controlled throught a command line interface:

```
java -Xmx2G -jar gateExtractor.jar -i <inputpath> [-t] [-r <outputpath>] [-e]
```

The first parameter -i is mandatory and provides the path to the corpus.

The -t (training) parameter starts the training mode, this has to be done
atleast once with the testing corpus for the actual application to run.

The -r (run) parameter applies the learned rules on the corpus specified
using the -i option. The outputpath is a directory where the results will
be saved (HTML files).

The -e (evaluate) paramter does a k-fold cross validation using k=4.

Only one of the three modes (training, run, evaluation) can be selected at
a time.

Depending on the performance of your system (number of cpus, cpu power)
training and especially evaluation can take a long time.

Typical use would be:

#####Training:

```
java -Xmx2G -jar gateExtractor.jar -i trainingCorpus/ -t
```

#####Evaluating:
```
java -Xmx2G -jar gateExtractor.jar -i trainingCorpus/ -e
```

#####Running on some documents:
```
java -Xmx2G -jar gateExtractor.jar -i documents/ -r outputFolder/
```




