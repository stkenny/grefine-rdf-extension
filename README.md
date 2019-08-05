[![Build Status](https://travis-ci.com/stkenny/grefine-rdf-extension.svg?branch=orefine)](https://travis-ci.com/stkenny/grefine-rdf-extension)

This project adds a graphical user interface(GUI) for exporting data of OpenRefine projects in RDF format. The export is based on mapping the data to a template graph using the GUI. It also provides a service for reconciling data against SPARQL endpoints (e.g., DBpedia).

## DOWNLOAD LATEST RELEASE
Download the release for your OpenRefine version:

[OpenRefine 3.2](https://github.com/stkenny/grefine-rdf-extension/releases/tag/v1.1.3-orefine-3.2)

[OpenRefine 3.1](https://github.com/stkenny/grefine-rdf-extension/releases/tag/v1.1.2)

## INSTALL

### From compiled release
You need to have OpenRefine installed on your machine.

1. If it does not exist, create a folder named **extensions/rdf-extension** under your user workspace directory for OpenRefine. The workspace should be located in the following places depending on your operating system:
    * Linux ~/.local/share/OpenRefine
    * Windows C:/Documents and Settings/<user>/Application Data/OpenRefine OR C:/Documents and Settings/<user>/Local Settings/Application Data/OpenRefine
    * Mac OSX ~/Library/Application Support/OpenRefine
2. Unzip the downloaded release into the extensions/rdf-extension folder (step 1).
It is recommended that you have an active internet connection during the first run of the extension, as it will try to download a set of predefined vocabularies (rdf, rdfs, owl and foaf). You can add them later also.
3. Restart OpenRefine

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
