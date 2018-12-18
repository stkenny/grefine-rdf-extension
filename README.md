[![Build Status](https://travis-ci.com/stkenny/grefine-rdf-extension.svg?branch=orefine)](https://travis-ci.com/stkenny/grefine-rdf-extension)

This project adds a graphical user interface(GUI) for exporting data of OpenRefine projects in RDF format. The export is based on mapping the data to a template graph using the GUI. It also provides a service for reconciling data against SPARQL endpoints (e.g., DBpedia).

## DOWNLOAD LATEST RELEASE
[https://github.com/stkenny/grefine-rdf-extension/releases](https://github.com/stkenny/grefine-rdf-extension/releases)

## INSTALL

### To build from source
- Clone this extension repository to your local machine
- Checkout the orefine branch `git checkout orefine`
- Run `mvn clean compile` and `mvn assembly:single`
- Unpack the zip file created in the `target` directory to a sub-directory of the extensions folder of OpenRefine e.g., `extensions/rdf-extension`

If you have previously installed the extension you will need to replace it in the OpenRefine extensions directory with the newly built version.

### Issues
If you are upgrading from an older version (prior to v1.0.0-rc4) you may see an error similar to the below when starting OpenRefine:
```
Exception in thread "main" java.lang.Error: class org.apache.lucene.index.IndexFormatTooOldException:
org.apache.lucene.index.IndexFormatTooOldException: Format version is not supported (resource 
BufferedChecksumIndexInput(MMapIndexInput(path="C:\PATH\OpenRefine\cache\rdfExtension\export\luceneIndex
\segments_1"))): -11 (needs to be between 1071082519 and 1071082519). 
This version of Lucene only supports indexes created with 
release 5.0 and later. 
```
This is due to an upgrade in the version of Lucene used. 
To solve this you will need to remove the old Lucene index and restart OpenRefine, i.e., for the example above delete 
```C:\PATH\OpenRefine\cache\rdfExtension\export\luceneIndex```.
For Linux this will be under ```~/.local/share/openrefine/cache/rdfExtension/export/luceneIndex```

## Documentation
* [Documentation Wiki](https://github.com/stkenny/grefine-rdf-extension/wiki)
