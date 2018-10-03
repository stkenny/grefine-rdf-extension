function RdfUploadTriplesDialog(onDone) {
	this._onDone = onDone;
	this._params = {};
	var self = this;
	this._dialog = $(DOM.loadHTML("rdf-extension", "scripts/dialogs/rdf-upload-triples.html"));
	this._elmts = DOM.bind(this._dialog);
	//var dismissBusy = DialogSystem.showBusy();

	this._elmts.dialogHeader.text($.i18n._('rdf-ext-virtuoso')["header"]);
	this._elmts.rdfext_virtuoso_endName.text($.i18n._('rdf-ext-virtuoso')["endpoint-name"]);
	this._elmts.rdfext_virtuoso_savedEnd.text($.i18n._('rdf-ext-virtuoso')["saved-endpoints"]+":");
	this._elmts.rdfext_virtuoso_local.text($.i18n._('rdf-ext-virtuoso')["local-virtuoso"]);
	this._elmts.rdfext_virtuoso_lod.text($.i18n._('rdf-ext-virtuoso')["lod"]);
	this._elmts.rdfext_virtuoso_type.text($.i18n._('rdf-ext-virtuoso')["endpoint-type"]+":");
	this._elmts.rdfext_virtuoso_virt.text($.i18n._('rdf-ext-virtuoso')["virt"]);
	this._elmts.rdfext_virtuoso_endUrl.text($.i18n._('rdf-ext-virtuoso')["endpoint-url"]+":");
	this._elmts.rdfext_virtuoso_def.text($.i18n._('rdf-ext-virtuoso')["default-graph"]+":");
	this._elmts.rdfext_virtuoso_auth.text($.i18n._('rdf-ext-virtuoso')["authentication"]+":");
	this._elmts.rdfext_virtuoso_digest.text($.i18n._('rdf-ext-virtuoso')["digest"]);
	this._elmts.rdfext_virtuoso_cred.text($.i18n._('rdf-ext-virtuoso')["credentials"]+":");
	this._elmts.rdfext_virtuoso_user.text($.i18n._('rdf-ext-virtuoso')["username"]+":");
	this._elmts.rdfext_virtuoso_pass.text($.i18n._('rdf-ext-virtuoso')["password"]+":");
	this._elmts.saveEndpointButton.attr('value',$.i18n._('rdf-ext-buttons')["save-endpoint"]);
	this._elmts.okButton.html($.i18n._('rdf-ext-buttons')["ok"]);
	this._elmts.cancelButton.text($.i18n._('rdf-ext-buttons')["cancel"]);
	this._elmts.manageEndpoints.text($.i18n._('rdf-ext-buttons')["manage-endpoint"]);	
	
	this._elmts.okButton.click(function() {

		self._params.endpoint = {}
		self._params.endpoint.name = self._elmts.endpointName.val();
		self._params.endpoint.url = self._elmts.endpointUrl.val();
		self._params.endpoint.type = self._elmts.endpointTypes.children(":selected").val();
		self._params.endpoint.auth = self._elmts.authMethod.children(":selected").val();
		self._params.endpoint.graph = self._elmts.defaultGraph.val();
		
		//TODO: what if we don't want default graph
		self._params.graph = self._elmts.defaultGraph.val();
		self._params.existing = false;
		
		//TODO: what about credentials?
		self._params.endpoint.username = self._elmts.userName.val();
		self._params.endpoint.password = self._elmts.userPwd.val();
		
		if ($('#config-creds').is(':checked')) {
			alert("Username and password are provided at the installation time. " +
					"They should be looked up in config file.")
		}
		
		
		if(!theProject.overlayModels.rdfSchema)
		{
			alert("No schema defined! Please define schema first.");
			DialogSystem.dismissUntil(self._level - 1);
		}
		
		self._onDone(self._params);
		DialogSystem.dismissUntil(self._level - 1);
			
			
	});

	this._elmts.cancelButton.click(function() {
		DialogSystem.dismissUntil(self._level - 1);
	});
	
	this._elmts.endpointsList.change(function () {
		self._elmts.endpointUrl.val(self._elmts.endpointsList.children(":selected").val())
	});
	
	this._elmts.saveEndpointButton.click(function () {
		//TODO: set up endpoint manager
		alert("Saving endpoint in the list...");

	});

	this._level = DialogSystem.showDialog(this._dialog);
	//dismissBusy();
}


RdfUploadTriplesDialog.prototype.uploadData = function() {
	var self = this;
}
