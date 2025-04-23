
# PCD Assignment 2

## 1. Async (Vertx)
Alternatives: 
* Future & Promises
* EventBus & Topics
A more idiomatic or modular approach?

### Future Composition
Composing a future for each file. 
When all files' futures have completed, the package future completes.
When all packages' futures have completed, the project future completes. 

## 2. Reactive (Rx)
A full subscription requires: onNext(), onError(), onCompleted()

Collect dependencies in progressively larger objects, or just have a unique stream?
You actually need to distinguish between different classes and packages dependencies.

A stream of dependencies for each file.
A stream of dependencies for each package.
When all streams have completed, the program completes.

You also need to re-render the gui after each new dependency.
It depends, maybe the GUI can just render after each new dependency only on the last layer. 

Otherwise, you need a class that it's emitted which contains the filename and the package name. 
Based on that info, you can draw accordingly. Therefore, is just a single big stream?
It makes sense since you have to parse a full project.



