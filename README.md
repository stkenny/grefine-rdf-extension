This project adds a graphical user interface(GUI) for exporting data of OpenRefine projects in RDF format. The export is based on mapping the data to a template graph using the GUI.

## INSTALL

- Follow the installation instructions for the [openrefine-maven-shim](https://github.com/DTL-FAIRData/openrefine-maven-shim#usage) project
  - **NOTE:** for this fork of the extension to work you need to use the branch of OpenRefine from here https://github.com/stkenny/openrefine/tree/rdf-extension. This is needed due to conflicts caused by Xerces-J jar files.
- Clone the extension repository to your local machine
- Run `mvn clean compile` and `mvn assembly:single`
- Unpack the zip file in `target` to an `openrefine/extensions/rdf-extension` folder
  
To run

  ./refine run
  
If you have previously installed the extension you will need to replace it in the OpenRefine extensions directory with the newly built version.
