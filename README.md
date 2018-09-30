[![Build Status](https://travis-ci.com/stkenny/grefine-rdf-extension.svg?branch=orefine)](https://travis-ci.com/stkenny/grefine-rdf-extension)

This project adds a graphical user interface(GUI) for exporting data of OpenRefine projects in RDF format. The export is based on mapping the data to a template graph using the GUI. It also provides a service for reconciling data against SPARQL endpoints (e.g., DBpedia).

** Requires OpenRefine 3.0 **

## INSTALL

### To build from source
- Clone this extension repository to your local machine
- Checkout the orefine branch `git checkout orefine`
- Run `mvn clean compile` and `mvn assembly:single`
- Unpack the zip file created in the `target` directory to a sub-directory of the extensions folder of OpenRefine e.g., `extensions/rdf-extension`
  
If you have previously installed the extension you will need to replace it in the OpenRefine extensions directory with the newly built version.

## Documentation
* [Documentation Wiki](https://github.com/stkenny/grefine-rdf-extension/wiki)
