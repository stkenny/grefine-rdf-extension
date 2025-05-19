![Build Status](https://github.com/stkenny/grefine-rdf-extension/workflows/Java%20CI%20with%20Maven/badge.svg)

This project adds a graphical user interface(GUI) for exporting data of OpenRefine projects in RDF format. The export is based on mapping the data to a template graph using the GUI. It also provides a service for reconciling data against SPARQL endpoints (e.g., DBpedia).

## DOWNLOAD

### Latest release

[RDF Extension v1.5.0](https://github.com/stkenny/grefine-rdf-extension/releases/download/v1.5.0/rdf-extension-1.5.0.zip)

## INSTALL

### Prerequisites

You need to have Java and OpenRefine installed on your machine.
  * Java 11
  * OpenRefine 3.8 **Not currently compatible with 3.9**

### From compiled release

1. If it does not exist, create a folder named **extensions/rdf-extension** under your user workspace directory for OpenRefine. The workspace should be located in the following places depending on your operating system (see [OpenRefine FAQ](https://github.com/OpenRefine/OpenRefine/wiki/FAQ-Where-Is-Data-Stored) for more details):
    * Linux ~/.local/share/OpenRefine
    * Windows C:/Documents and Settings/<user>/Application Data/OpenRefine OR C:/Documents and Settings/<user>/Local Settings/Application Data/OpenRefine
    * Mac OSX ~/Library/Application Support/OpenRefine
2. Unzip the downloaded release (ensuring it is a rdf-extension-x.x.x-*.zip **not** a source code .zip or tar.gz) into the extensions/rdf-extension folder (step 1).
It is recommended that you have an active internet connection during the first run of the extension, as it will try to download a set of predefined vocabularies (rdf, rdfs, owl and foaf). You can add them later also.
3. Restart OpenRefine (OpenRefine usage instructions are provided in the [user documentation](https://github.com/OpenRefine/OpenRefine/wiki/Installation-Instructions#release-version))

### To build from source
- Clone this extension repository to your local machine
- Checkout the main branch `git checkout main`
- Run `mvn clean compile` and `mvn assembly:single`
- Unpack the zip file created in the `target` directory to a sub-directory of the extensions folder of OpenRefine e.g., `extensions/rdf-extension`

If you have previously installed the extension you will need to replace it in the OpenRefine extensions directory with the newly built version.

## Documentation
* [Documentation Wiki](https://github.com/stkenny/grefine-rdf-extension/wiki)
