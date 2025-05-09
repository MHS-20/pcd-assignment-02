
# PCD Assignment 2

## 1. Async (Vertx)
Alternatives:
* Future & Promises
* EventBus & Topics

### TODO pt1:
- Check return types (list<future>)
- Explain return types in report

* Appunti lezione: 
Le promise vengono usate per scrivere metodi asincroni.
Creo la promise tipata in base al risultato che devo ritornare,
ed immediatamente ritorno la future associata.

Un metodo è asincrono solo se restituisce un future.
Per restituire un future deve crearsi una promise.

Le promise sono thread-safe, perché le posso passare ad altri thread,
ed usarle per attendere il risultato.

Nei verticle è l'event-loop che esegue le callback, 
anche se un'operazione finisce prima di aver agganciato l'handler, 
non viene perso perché l'event loop lo controlla al giro dopo,
basta associare l'handler prima di terminare il metodo.

ExecuteBlocking serve a far eseguire il codice ad un worker, e non all'avent-loop.
Però non bisogna accedere a variabili condivise, altrimenti ci sono corse critiche con l'event-loop.


### Future Composition
Composing a future for each file.
When all files' futures have completed, the package future completes.
When all packages' futures have completed, the project future completes.

Project Analyzer works recursively to create parallel PackageAnalyzers for all sub packages.
While they terminate, it updates the project dependencies map.
Each PackageAnalyzers create parallel ClassAnalyzers to parse the dependencies of source files. 

## 2. Reactive (Rx)
- A full subscription requires: onNext(), onError(), onCompleted()
- Without using schedulers, by default all the computation, is done by the calling thread

- observeOn: move the downstream computation to the specified scheduler, 
delegates the reception of items
- subscribeOn: move the computational work of an Observable on a specified scheduler, 
delegates the production of items

Collect dependencies in progressively larger objects.
A class which contains the filename and the package name is emitted.
Based on that info, the gui draws the node accordingly.

It's needed to distinguish between different classes and packages dependencies.
The GUI is re-rendered after each source file, since are the smallest unit processable.
I may consider to render each dependency, creating a smaller object than the file dependencies class.

### TODO pt2:
- Schedulers?
- Single Dependency Result?
- Backpressure? Timeout? 
- Package Clustering?
- GUI clicks can be handled reactively
