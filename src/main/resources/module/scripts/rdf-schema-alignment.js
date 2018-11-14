var RdfSchemaAlignment = {};

function RdfSchemaAlignmentDialog(schema){
	this._init(schema);
	this._buildBody();

	this._prefixesManager = new RdfPrefixesManager(this, this._schema.prefixes);   
	this._replaceBaseUri(RdfSchemaAlignment._defaultNamespace || location.origin+'/',true);

};

RdfSchemaAlignmentDialog.prototype._init = function(schema) {
	var self = this;

	self._originalSchema = schema || { rootNodes: [] };
	self._schema = cloneDeep(self._originalSchema); // this is what can be munched on

	if (!self._schema.rootNodes.length) {
		self._schema.rootNodes.push(RdfSchemaAlignment.createNewRootNode());
	}

	self._nodeUIs = [];
	RdfSchemaAlignment._defaultNamespace = self._schema.baseUri;		
};

RdfSchemaAlignmentDialog.prototype._buildBody = function() {
	var self = this;

	var dialog = $(DOM.loadHTML("rdf-extension", "scripts/dialogs/rdf-schema-alignment.html"));
	self._elmts = DOM.bind(dialog);

	self._elmts.dialogHeader.text($.i18n._('rdf-ext-align')["header"]);
	self._elmts.rdfext_align_desc.text($.i18n._('rdf-ext-align')["desc"]);
	self._elmts.rdfext_align_baseUri.text($.i18n._('rdf-ext-align')["base-uri"]+":");
	self._elmts.editBaseUriLink.text($.i18n._('rdf-ext-align')["edit"]);
	self._elmts.rdfext_align_skeleton.text($.i18n._('rdf-ext-align')["skeleton"]);
	self._elmts.rdfext_align_preview.text($.i18n._('rdf-ext-align')["preview"]);
	self._elmts.rdfext_align_prefixes.text($.i18n._('rdf-ext-align')["available-prefix"]+":");
	self._elmts.add_another_root_node.text($.i18n._('rdf-ext-align')["add-root"]);
	self._elmts._save_skeleton.text($.i18n._('rdf-ext-align')["save"]);
	self._elmts.rdfext_align_sampleTurtle.html($.i18n._('rdf-ext-align')["sample-turtle"]);
	self._elmts.okButton.html($.i18n._('rdf-ext-buttons')["ok"]);
	self._elmts.cancelButton.text($.i18n._('rdf-ext-buttons')["cancel"]);
	
	self._elmts.cancelButton.click(function() { DialogSystem.dismissUntil(self._level - 1);});

	self._elmts.okButton.click(function () {
		var schema = self.getJSON();
		Refine.postProcess(
				"rdf-extension",
				"save-rdf-schema",
				{},
				{ schema: JSON.stringify(schema) },
				{},
				{   
					onDone: function() {
						DialogSystem.dismissUntil(self._level - 1);
						theProject.overlayModels.rdfSchema = schema;
					}
				}
		);
	});

	var body = self._elmts.dialogBody;  
	self._level = DialogSystem.showDialog(dialog);    
	self._constructBody(body);
	this._renderBody(body);
};

RdfSchemaAlignmentDialog.prototype._constructBody = function(body) {
	var self = this;    
	self._baseUriSpan = self._elmts.baseUriSpan;
	self._rdf_schema_prefixes = self._elmts.rdf_schema_prefixes;
	self._elmts.baseUriSpan.text(RdfSchemaAlignment._defaultNamespace);

	self._elmts.editBaseUriLink.click(function(evt){
		evt.preventDefault();
		self._editBaseUri($(evt.target));
	});
	self._elmts._save_skeleton.click(function(e){
		e.preventDefault();
		var schema = self.getJSON();
		Refine.postProcess(
				"rdf-extension",
				"save-rdf-schema",
				{},
				{ schema: JSON.stringify(schema) },
				{},
				{   
					onDone: function() {
						theProject.overlayModels.rdfSchema = schema;
					}
				}
		);
	});

	self._elmts.add_another_root_node.click(function(e){
		e.preventDefault();
		var newRootNode = RdfSchemaAlignment.createNewRootNode(false);
		self._schema.rootNodes.push(newRootNode);
		self._nodeUIs.push(new RdfSchemaAlignmentDialog.UINode(
				self,
				newRootNode, 
				self._nodeTable, 
				{
					expanded: true,
				}
		));
	});

};

RdfSchemaAlignmentDialog.prototype._previewRdf = function(){
	var self = this;
	var schema = self.getJSON();
	self._previewPane.empty().html('<img src="images/large-spinner.gif" title='+$.i18n._('rdf-ext-schema')["loading"]+'"..."/>');
	$.post(
			"command/rdf-extension/preview-rdf?" + $.param({ project: theProject.id }),
			{ schema: JSON.stringify(schema), engine: JSON.stringify(ui.browsingEngine.getJSON()) },
			function(data) {
				self._previewPane.empty();
				self._previewPane.html(linkify(data.v));
			},
			"json"
	);
};

RdfSchemaAlignmentDialog.prototype._renderBody = function(body) {
	var self = this;

	$("#rdf-schema-alignment-tabs").tabs({
		select:function(evt,tabs){
			if(tabs.index == 1){
				self._previewRdf();
			}
		}
	});
	
	self._canvas = $(".schema-alignment-dialog-canvas");
	self._nodeTable = $('<table></table>')
	.addClass("schema-alignment-table-layout")
	.addClass("rdf-schema-alignment-table-layout")
	.appendTo(self._canvas)[0];

	for (var i = 0; i < self._schema.rootNodes.length; i++) {
		self._nodeUIs.push(new RdfSchemaAlignmentDialog.UINode(
				self,
				self._schema.rootNodes[i], 
				self._nodeTable, 
				{
					expanded: true,
				}
		));
	}

	self._previewPane = $("#rdf-schema-alignment-dialog-preview");
};



RdfSchemaAlignment.createNewRootNode = function(withDefaultChildren) {
	rootNode = { nodeType: "cell-as-resource", expression:"value", isRowNumberCell:true};
	var links = [];
	if(withDefaultChildren === false){
		rootNode.links = links;
		return rootNode;
	}
	var columns = theProject.columnModel.columns;
	for (var i = 0; i < columns.length; i++) {
		var column = columns[i];
		var target = {
				nodeType: "cell-as-literal",
				columnName: column.name,
		};        
		links.push({
			uri: null,
			curie: null,
			target: target
		});
	}
	rootNode.links = links;

	return rootNode;
};

RdfSchemaAlignmentDialog.prototype._editBaseUri = function(src){
	var self = this;
	var menu = MenuSystem.createMenu().width('400px');
	menu.html('<div class="schema-alignment-link-menu-type-search"><input type="text" bind="newBaseUri" size="50"><br/>'+
			'<button class="button" bind="applyButton">'+$.i18n._('rdf-ext-buttons')["apply"]+'</button>' + 
			'<button class="button" bind="cancelButton">'+$.i18n._('rdf-ext-buttons')["cancel"]+'</button></div>'
	);
	MenuSystem.showMenu(menu,function(){});
	MenuSystem.positionMenuLeftRight(menu, src);
	var elmts = DOM.bind(menu);
	elmts.newBaseUri.val(RdfSchemaAlignment._defaultNamespace).focus().select();
	elmts.applyButton.click(function() {

		function endsWith(str, suffix) {
			return str.indexOf(suffix, str.length - suffix.length) !== -1;
		}

		var newBaseUri = elmts.newBaseUri.val();

		if(!endsWith(newBaseUri,"/") && !endsWith(newBaseUri,"#")) {
			var ans = confirm($.i18n._('rdf-ext-schema')["confirm-one"]+"\n" + 
					$.i18n._('rdf-ext-schema')["confirm-two"]);
			if(ans == false) return;
		}

		MenuSystem.dismissAll();
		self._replaceBaseUri(newBaseUri);
	});

	elmts.cancelButton.click(function() {
		MenuSystem.dismissAll();
	});
};
RdfSchemaAlignmentDialog.prototype._replaceBaseUri = function(newBaseUri,doNotSave){
	var self = this;
	RdfSchemaAlignment._defaultNamespace = newBaseUri;
	if(!doNotSave){
		$.post("command/rdf-extension/save-baseURI?" + $.param({project: theProject.id }),{baseURI:newBaseUri},function(data){
			if (data.code === "error"){
				alert($.i18n._('rdf-ext-schema')["error"]+':' + data.message);
				return;
			}else{
				self._baseUriSpan.empty().text(newBaseUri);
			}
		},"json");
	}else{
		self._baseUriSpan.empty().text(newBaseUri);
	}

	//update rdf preview
	self._previewRdf();
};

RdfSchemaAlignmentDialog.prototype.getJSON = function() {
	var rootNodes = [];
	for (var i = 0; i < this._nodeUIs.length; i++) {
		var node = this._nodeUIs[i].getJSON();
		if (node !== null) {
			rootNodes.push(node);
		}
	}

	var prefixes = [];
	if (typeof this._prefixesManager._prefixes != "undefined") {
	    for(var i=0; i<this._prefixesManager._prefixes.length; i++) {
		    prefixes.push({"name": this._prefixesManager._prefixes[i].name,"uri": this._prefixesManager._prefixes[i].uri});
	    }
	}
	return {
		prefixes:prefixes,
		baseUri:RdfSchemaAlignment._defaultNamespace,
		rootNodes: rootNodes
	};
};

RdfSchemaAlignmentDialog._findColumn = function(columnName) {
	var columns = theProject.columnModel.columns;
	for (var i = 0; i < columns.length; i++) {
		var column = columns[i];
		if (column.name == columnName) {
			return column;
		}
	}
	return null;
};
