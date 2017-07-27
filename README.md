This project adds a graphical user interface(GUI) for exporting data of OpenRefine projects in RDF format. The export is based on mapping the data to a template graph using the GUI.

## INSTALL

- Follow the installation instructions for the [openrefine-maven-shim](https://github.com/DTL-FAIRData/openrefine-maven-shim#usage) project
  - **NOTE:** for this to fork of the extension to work you need to use the Jetty 9 branch of OpenRefine from here https://github.com/stkenny/openrefine/tree/jetty9_migration
- Clone the extension repository to your local machine
- Run `mvn clean compile` and `mvn assembly:single`
- Unpack the zip file in `target` to your `openrefine/extensions` folder
  
To run

  ./refine run
  
If you have previously installed the extension you will need to replace it in the OpenRefine extensions directory with the newly built version.
