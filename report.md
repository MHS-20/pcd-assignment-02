
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

Collect dependencies in progressively larger objects.
A class which contains the filename and the package name is emitted.
Based on that info, the gui draws the node accordingly.

It's needed to distinguish between different classes and packages dependencies.
The GUI is re-rendered after each source file, since are the smallest unit processable.
I may consider to render each dependency, creating a smaller object than the file dependencies class.

### TODO:
- Schedulers?
- Single Dependency Result?
- Backpressure? Swing? Timeout? 
- Package Clustering?

- Sub-folders are not counted as different packages
