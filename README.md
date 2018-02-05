This project adds a graphical user interface(GUI) for exporting data of OpenRefine projects in RDF format. The export is based on mapping the data to a template graph using the GUI.

## INSTALL

- Clone and build the branch of OpenRefine from here https://github.com/stkenny/openrefine/tree/rdf-extension. This is needed due to conflicts caused by Xerces-J and HttpClient jar files.
- Clone this extension repository to your local machine
- Run `mvn clean compile` and `mvn assembly:single`
- Unpack the zip file in `target` to a sub-directory of the extensions folder in the OpenRefine fork e.g., `extensions/rdf-extension`

To run

  ./refine run
  
If you have previously installed the extension you will need to replace it in the OpenRefine extensions directory with the newly built version.
