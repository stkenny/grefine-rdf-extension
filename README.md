This project adds a graphical user interface(GUI) for exporting data of OpenRefine projects in RDF format. The export is based on mapping the data to a template graph using the GUI.

## INSTALL

- Check out and build the latest version of OpenRefine from the Github repository, https://github.com/OpenRefine/OpenRefine
- Clone this extension repository to your local machine
- Run `mvn clean compile` and `mvn assembly:single`
- Unpack the zip file created in the `target` directory to a sub-directory of the extensions folder in the OpenRefine fork e.g., `extensions/rdf-extension`

To run

  ./refine run
  
If you have previously installed the extension you will need to replace it in the OpenRefine extensions directory with the newly built version.
