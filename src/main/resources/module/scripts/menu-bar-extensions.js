ExporterManager.MenuItems.push({});//add separator

// Internationalization init
var lang = navigator.language.split("-")[0]
                || navigator.userLanguage.split("-")[0];
var dictionary = "";
$.ajax({
        url : "/command/core/load-language?",
        type : "POST",
        async : false,
        data : {
          module : "rdf-extension",
//              lang : lang
        },
        success : function(data) {
                dictionary = data['dictionary'];
                lang = data['lang'];
        }
});
$.i18n().load(dictionary, lang);
// End internationalization

ExporterManager.MenuItems.push(
		{
			"id" : "exportRdfXml",
          	"label": $.i18n("rdf-ext-menu/rdf-xml"),
          	"click": function() { RdfExporterMenuBar.exportRDF("rdf", "rdf");}
		}
);
ExporterManager.MenuItems.push(
		{
			"id" : "exportRdfTurtle",
        	"label": $.i18n("rdf-ext-menu/rdf-turtle"),
        	"click": function() { RdfExporterMenuBar.exportRDF("Turtle", "ttl"); }
		}
);

RdfExporterMenuBar = {};

RdfExporterMenuBar.exportRDF = function(format, ext) {
    if (!theProject.overlayModels.rdfSchema) {
        alert(
            $.i18n("rdf-ext-menu/alert-no-align")
        );
    } else {
        RdfExporterMenuBar.rdfExportRows(format, ext);
    }
};

RdfExporterMenuBar.rdfExportRows = function(format, ext) {
    var name = $.trim(theProject.metadata.name.replace(/\W/g, ' ')).replace(/\s+/g, '-');
    var form = document.createElement("form");
    $(form)
        .css("display", "none")
        .attr("method", "post")
        .attr("action", "command/core/export-rows/" + name + "." + ext)
        .attr("target", "gridworks-export");

    $('<input />')
        .attr("name", "engine")
        .attr("value", JSON.stringify(ui.browsingEngine.getJSON()))
        .appendTo(form);
    $('<input />')
        .attr("name", "project")
        .attr("value", theProject.id)
        .appendTo(form);
    $('<input />')
        .attr("name", "format")
        .attr("value", format)
        .appendTo(form);

    document.body.appendChild(form);

    window.open("about:blank", "gridworks-export");
    form.submit();

    document.body.removeChild(form);
};

RdfExporterMenuBar.editRdfSchema = function(reset) {
    new RdfSchemaAlignmentDialog(reset ? null : theProject.overlayModels.rdfSchema);
};

var RdfReconciliationManager = {};

RdfReconciliationManager.newSparqlService = function(){
	new ReconciliationSparqlServiceDialog();
};
RdfReconciliationManager.newRdfService = function(){
	new ReconciliationRdfServiceDialog();
};
RdfReconciliationManager.newStanbolService = function(){
	new ReconciliationStanbolServiceDialog();
};

RdfReconciliationManager.registerService = function(data,level){
	if (data.code === "error"){
		alert($.i18n('rdf-ext-menu/error')+': ' + data.message);
	} else {
	    if (typeof data.service != "undefined") {
		    var url = location.href;  // entire url including querystring - also: window.location.href;
		    var baseURL = url.substring(0,url.lastIndexOf('/'));
		    var service_url = baseURL + '/extension/rdf-extension/services/' + data.service.id;
		
		    //ReconciliationManager does not call this method upon unregister.. this is why I am calling it myself
		    ReconciliationManager._rebuildMap();
		
		    if(!ReconciliationManager.getServiceFromUrl(service_url)){
			    ReconciliationManager.registerStandardService(service_url);
		    }
		}
		if(level){
			DialogSystem.dismissUntil(level - 1);
		}
	}
};

function ReconciliationStanbolServiceDialog() {

    var self = this;

    var dialog = $(DOM.loadHTML("rdf-extension", "scripts/stanbol-service.html"));
    this._elmts = DOM.bind(dialog);

    this._elmts.rdfext_stanbol_header.text($.i18n('rdf-ext-stanbol/header'));
    this._elmts.rdfext_stanbol_subheader.text($.i18n('rdf-ext-stanbol/subheader'));
    this._elmts.rdfext_stanbol_uriLabel.text($.i18n('rdf-ext-stanbol/uri-label'));
    this._elmts.rdfext_stanbol_help.text($.i18n('rdf-ext-stanbol/help'));
    this._elmts.rdfext_stanbol_readMore.text($.i18n('rdf-ext-stanbol/read-more'));
    this._elmts.rdfext_stanbol_readMoreconnect.text($.i18n('rdf-ext-stanbol/read-more-connect'));
    this._elmts.rdfext_stanbol_readMore.text($.i18n('rdf-ext-stanbol/read-more'));
    this._elmts.rdfext_stanbol_readMoreconnect.text($.i18n('rdf-ext-stanbol/read-more-connect'));
    this._elmts.cancelBtn.text($.i18n('rdf-ext-buttons/cancel'));
    this._elmts.registerBtn.text($.i18n('rdf-ext-buttons/register'));

    var inputUri = dialog.find("input#stanbol-uri");

    dialog.find("button#cancel").click(function() {
        DialogSystem.dismissUntil(self._level - 1);
    });

    dialog.find("button#register").click(function() {

        if ($("img#validation-img").length) { $("img#validation-img").remove(); }

            var uri = inputUri.val();

            if(uri.charAt(uri.length-1) == "/") {
                    uri = uri.slice(0, -1);
                    inputUri.val(uri);
            }

            if (validateURI(uri)) {
                inputUri.attr("disabled", "disabled");
                inputUri.after($('<img src="extension/rdf-extension/images/spinner.gif" width="14" height="14" alt="'+$.i18n('rdf-ext-menu/fetching')+'..." class="validation" id="validation-img" />'));
                Refine.wrapCSRF(function(token) {
                    $.post("command/rdf-extension/addStanbolService",
                        {
                            "uri": uri,
                            "engine": JSON.stringify(ui.browsingEngine.getJSON()),
                            "project": theProject.id,
                            "csrf_token": token
                        },
                        function(data) {

                            var registering = $("dl#stanbol-registering");
                            registering.parent().height($("p#stanbol-help").height());
                            registering.parent().fadeIn("slow");
                            $("p#stanbol-help").hide();
                            $.each(data, function(i, obj) {
                                //check issue #579: http://code.google.com/p/google-refine/issues/detail?id=579
                                if (ReconciliationManager.getServiceFromUrl(obj.uri)) {
                                    self.printAddedService(registering, obj, false);
                                } else {
                                    ReconciliationManager.registerStandardService(obj.uri, function(index) {
                                        self.printAddedService(registering, obj, true);
                                    });
                                }
                            });
                            $("img#validation-img").remove();
                            //DialogSystem.dismissUntil(self._level - 1);
                            dialog.find("button#register").hide();
                            dialog.find("button#cancel").text($.i18n('rdf-ext-buttons/close'));
                        },
                        "json");
                });
                } else {
                    inputUri.addClass("error");
                    inputUri.after($('<img src="extension/rdf-extension/images/no.png" width="16" height="16" alt="invalid" class="validation" id="validation-img" />'));
                    alert($.i18n('rdf-ext-menu/not-valid-uri'));
                }
    });
	
	var frame = DialogSystem.createDialog();
    frame.width("500px");
    dialog.appendTo(frame);
    
	self._level = DialogSystem.showDialog(frame);
	
};

ReconciliationStanbolServiceDialog.prototype.printAddedService = function(container, obj, registered) {
	var cached = (obj.local ? $.i18n('rdf-ext-menu/cached') : $.i18n('rdf-ext-menu/not-cached'));
    var image = (registered ? "yes" : "no");
    var label = (registered ? $.i18n('rdf-ext-menu/registered') : $.i18n('rdf-ext-menu/not-added'));
    var sniper = '<dt><a href="' + obj.uri + '">' + obj.uri + '</a> <img src="extension/rdf-extension/images/' + image + '.png" width="16" height="16" alt="' + label + '" title="' + label + '" /></dt><dd><strong>' + obj.name + '</strong>, ' + cached + '</dd>';
    if (!registered) {
        sniper += '<dd>' + label + '</dd>';
    }
    container.append(sniper).fadeIn("slow");
};

function ReconciliationRdfServiceDialog(){
	var self = this;
    var dialog = $(DOM.loadHTML("rdf-extension","scripts/rdf-service-dialog.html"));
    this._elmts = DOM.bind(dialog);

    this._elmts.rdfext_rdf_desc.text($.i18n('rdf-ext-rdf/desc'));
    this._elmts.rdfext_rdf_name.text($.i18n('rdf-ext-rdf/name')+":");
    this._elmts.rdfext_rdf_detail.text($.i18n('rdf-ext-rdf/detail'));
    this._elmts.rdfext_rdf_fileDet.text($.i18n('rdf-ext-rdf/file-details'));
    this._elmts.rdfext_rdf_load.text($.i18n('rdf-ext-rdf/load-url') +":");
    this._elmts.rdfext_rdf_upload.text($.i18n('rdf-ext-rdf/file-upload')+":");
    this._elmts.rdfext_rdf_format.text($.i18n('rdf-ext-rdf/format')+":");
    this._elmts.rdfext_rdf_autoDetect.text($.i18n('rdf-ext-rdf/autodetect'));
    this._elmts.rdfext_rdf_turtle.text($.i18n('rdf-ext-rdf/turtle'));
    this._elmts.rdfext_rdf_rdfxml.text($.i18n('rdf-ext-rdf/rdfxml'));
    this._elmts.rdfext_rdf_ntriple.text($.i18n('rdf-ext-rdf/ntriple'));
    this._elmts.rdfext_rdf_labprop.text($.i18n('rdf-ext-rdf/label-prop'));
    this._elmts.rdfext_rdf_important.text($.i18n('rdf-ext-rdf/important'));
    this._elmts.rdfext_rdf_impDesc.html($.i18n('rdf-ext-rdfimp-desc'));
    this._elmts.rdfext_rdf_other.text($.i18n('rdf-ext-rdf/other')+"...");
    this._elmts.rdfext_rdf_fullUri.text($.i18n('rdf-ext-rdf/full-uri'));

    this._elmts.other_label_chk.click(function(){
        if($(this).attr("checked")){
            self._elmts.other_properties.show();
        } else {
            self._elmts.other_properties.hide();
        }
    });

    this._elmts.file_source_upload.add(this._elmts.file_source_url).bind("click", function(){
        var upload = self._elmts.file_source_upload.attr("checked");
        if(upload){
            self._elmts.file_upload_input.attr("disabled",false);
            self._elmts.file_url_input.attr("disabled",true);
        } else {
            self._elmts.file_upload_input.attr("disabled",true);
            self._elmts.file_url_input.attr("disabled",false);
        }
    });

    var frame = DialogSystem.createDialog();

    frame.width("600px");

    $('<div></div>').addClass("dialog-header").text($.i18n('rdf-ext-menu/add-file-recon')).appendTo(frame);
    $('<div></div>').addClass("dialog-body").append(dialog).appendTo(frame);
    var footer = $('<div></div>').addClass("dialog-footer").appendTo(frame);

    this._level = DialogSystem.showDialog(frame);
    this._elmts.other_properties.hide();
    this._footer(footer);
}

ReconciliationRdfServiceDialog.prototype._footer = function(footer){
	var self = this;
	$('<button></button>').addClass('button').html($.i18n('rdf-ext-buttons/ok')).click(function() {
		self._dismissBusy = DialogSystem.showBusy($.i18n('rdf-ext-menu/add-new-recon'));
	    var name = self._elmts.service_name.val();
	    if(name.trim()===""){
	    	alert($.i18n('rdf-ext-menu/alert-name-required'));
	    	self._dismissBusy();
	    	return;
	    }
	    var props = self._elmts.label_prop_container.find('input[name="label_prop"]:checked');
	    var prop_uris = "";
	    for(var i=0;i<props.length;i++){
	    	prop_uris += " " + $(props[i]).val();
	    }
	    if(self._elmts.other_label_chk.prop("checked")){
	    	prop_uris += " " + self._elmts.other_properties_textarea.val();
	    }
	    if(prop_uris===""){
	    	alert($.i18n('rdf-ext-menu/alert-label-provided'));
	    	self._dismissBusy();
	    	return;
	    }
	    
	    if (self._elmts.file_source_url.attr('checked')){
	    	var file_url = self._elmts.file_url_input.val();
	    	var file_format = self._elmts.file_format_input.val();
	    	if(file_url.trim()===""){
		    	alert($.i18n('rdf-ext-menu/alert-url-required'));
		    	self._dismissBusy();
		    	return;
		    }
	    	
	    	var services = ReconciliationManager.getAllServices();
	    	Refine.wrapCSRF(function(token) {
	    	    $.post("command/rdf-extension/addService",
				{
				    "csrf_token":token,
				    "datasource":"file_url",
					"name":name,"url":file_url,
					properties:prop_uris,
					"file_format":file_format
				},
				function(data){
					self._dismissBusy();
					RdfReconciliationManager.registerService(data,self._level);
			    },"json");
			});
	    	return;
	    }
	    
	    self._elmts.hidden_service_name.val(name);
	    self._elmts.hidden_properties.val(prop_uris);

        Refine.wrapCSRF(function(token) {
	        self._elmts.file_upload_form.ajaxSubmit({
	    	    dataType: 'json',
	    	    type:'post',
	    	    headers: { 'X-CSRF-TOKEN': token },
	    	    success: function(data) {
	    		    self._dismissBusy();
	    		    RdfReconciliationManager.registerService(data,self._level);
			    }
		    });
		});

	}).appendTo(footer);
	
	$('<button></button>').addClass('button').text($.i18n('rdf-ext-buttons/cancel')).click(function() {
	    DialogSystem.dismissUntil(self._level - 1);
	}).appendTo(footer);
};


function ReconciliationSparqlServiceDialog(){
        var self = this;
        var dialog = $(DOM.loadHTML("rdf-extension","scripts/sparql-service-dialog.html"));
        this._elmts = DOM.bind(dialog);

        this._elmts.rdfext_sparql_name.text($.i18n('rdf-ext-sparql/name')+":");
        this._elmts.rdfext_sparql_nameDesc.text($.i18n('rdf-ext-sparql/name-desc'));
        this._elmts.rdfext_sparql_endDet.text($.i18n('rdf-ext-sparql/endpoint'));
        this._elmts.rdfext_sparql_endUrl.text($.i18n('rdf-ext-sparql/endpoint-url')+":");
        this._elmts.rdfext_sparql_graphUri.text($.i18n('rdf-ext-sparql/graph-uri')+":");
        this._elmts.rdfext_sparql_graphDesc.text($.i18n('rdf-ext-sparql/graph-desc'));
        this._elmts.rdfext_sparql_type.text($.i18n('rdf-ext-sparql/type')+":");
        this._elmts.rdfext_sparql_genericSparql.text($.i18n('rdf-ext-sparql/generic-sparql'));
        this._elmts.rdfext_sparql_virtuoso.text($.i18n('rdf-ext-sparql/virtuoso'));
        this._elmts.rdfext_sparql_jenaText.text($.i18n('rdf-ext-sparql/jena-text'));
        this._elmts.rdfext_sparql_bigowlim.text($.i18n('rdf-ext-sparql/bigowlim'));
        this._elmts.rdfext_sparql_detSyntax.text($.i18n('rdf-ext-sparql/det-syntax'));
        this._elmts.rdfext_sparql_labelProp.text($.i18n('rdf-ext-sparql/label-prop'));
        this._elmts.rdfext_sparql_propDesc.text($.i18n('rdf-ext-sparql/prop-desc')+":");
        this._elmts.rdfext_sparql_other.text($.i18n('rdf-ext-sparql/other')+"...");
        this._elmts.rdfext_sparql_fullUri.text($.i18n('rdf-ext-sparql/full-uri'));

        this._elmts.other_label_chk.click(function(){
                if($(this).attr("checked")){
                        self._elmts.other_properties.show();
                }else{
                        self._elmts.other_properties.hide();
                }
        });

        var frame = DialogSystem.createDialog();

        frame.width("600px");

        $('<div></div>').addClass("dialog-header").text($.i18n('rdf-ext-menu/add-sparql-recon')).appendTo(frame);
        $('<div></div>').addClass("dialog-body").append(dialog).appendTo(frame);
        var footer = $('<div></div>').addClass("dialog-footer").appendTo(frame);

        this._level = DialogSystem.showDialog(frame);
        this._elmts.other_properties.hide();
        this._footer(footer);
}

ReconciliationSparqlServiceDialog.prototype._footer = function(footer){
        var self = this;
        $('<button></button>').addClass('button').html($.i18n('rdf-ext-buttons/ok')).click(function() {
                self._dismissBusy = DialogSystem.showBusy($.i18n('rdf-ext-menu/add-new-recon'));
                var name = self._elmts.service_name.val();
                var endpoint = self._elmts.endpoint_url.val();
                var graph_uri = self._elmts.graph_uri.val();
                if(name.trim()===""){
                        alert($.i18n('rdf-ext-menu/alert-name-required'));
                        self._dismissBusy();
                        return;
                }
                if(endpoint.trim()===""){
                        alert($.i18n('rdf-ext-menu/alert-endpoint-required'));
                        self._dismissBusy();
                        return;
                }
                var type = self._elmts.endpoint_type.val();

                var props = self._elmts.label_prop_container.find('input[name="label_prop"]:checked');
                var prop_uris = "";
                for(var i=0;i<props.length;i++){
                        prop_uris += " " + $(props[i]).val();
                }
                if(self._elmts.other_label_chk.prop("checked")){
                        prop_uris += " " + self._elmts.other_properties_textarea.val();
                }
                if(prop_uris===""){
                        alert($.i18n('rdf-ext-menu/alert-label-provided'));
                        self._dismissBusy();
                        return;
                }

                RdfReconciliationManager.synchronizeServices(
                                function(){
                                    Refine.wrapCSRF(function(token) {
                                        $.post("command/rdf-extension/addService",
                                              {
                                                "datasource":"sparql",
                                                "name":name,
                                                "url":endpoint,
                                                "type":type,
                                                "graph":graph_uri,properties:prop_uris,
                                                "csrf_token":token
                                              },
                                                function(data){
                                                    self._dismissBusy();
                                                    RdfReconciliationManager.registerService(data,self._level);
                                                 },"json");
                                    });
                                }
                );
        }).appendTo(footer);

        $('<button></button>').addClass('button').text($.i18n('rdf-ext-buttons/cancel')).click(function() {
                DialogSystem.dismissUntil(self._level - 1);
        }).appendTo(footer);
};

RdfReconciliationManager.synchronizeServices = function(onDone){
	var services = ReconciliationManager.getAllServices();
	var ids = [];
	for(var i=0;i<services.length;i++){
		if(services[i].url){
			ids.push(services[i].url);
		}
	}
	Refine.wrapCSRF(function(token) {
	    $.post("command/rdf-extension/initializeServices",
	    {
	        "csrf_token": token,
	        "services":JSON.stringify(ids)
	    },
	    function(data){
		RdfReconciliationManager.registerService(data);
		if(onDone){
			onDone();
		}
	    },"json");
	});
};


//extend the column header menu
$(function(){
    
	ExtensionBar.MenuItems.push(
			{
				"id":"reconcile",
				"label": "RDF",
				"submenu" : [
					{
						"id": "rdf/edit-rdf-schema",
						label: $.i18n('rdf-ext-menu/edit-skeleton')+"...",
						click: function() { RdfExporterMenuBar.editRdfSchema(false); }
					},
					{
						"id": "rdf/reset-rdf-schema",
						label: $.i18n('rdf-ext-menu/reset-skeleton')+"...",
						click: function() { RdfExporterMenuBar.editRdfSchema(true); }
					},
					{},
			        {
			        	"id": "rdf/reconcile",
			            label: $.i18n('rdf-ext-menu/add-recon-service'),
			            submenu:[
			                     {
			                    	 "id" :"rdf/reconcile/sparql",
			                    	 label: $.i18n('rdf-ext-menu/based-sparql')+"...",
			                    	 click: function() { RdfReconciliationManager.newSparqlService(); }
			                     },
			                     {
			                    	 "id":"rdf/reconcile/dump",
			                    	 label: $.i18n('rdf-ext-menu/based-rdf')+"...",
			                    	 click: function() { RdfReconciliationManager.newRdfService(); }        	 
			                     },
			                     {
			                    	 "id" : "rdf/reconcile/stanbol",
			                    	 label: $.i18n('rdf-ext-menu/based-entityhub')+"...",
			                    	 click: function() { RdfReconciliationManager.newStanbolService(); }        	 
			                     }
						]
					
			        }
			    ]
			}
	);
	DataTableColumnHeaderUI.extendMenu(function(column, columnHeaderUI, menu) {
		MenuSystem.appendTo(menu, [ "core/reconcile" ], [
		                                             {},
		                                         ]);
	});
	
	RdfReconciliationManager.synchronizeServices();
});

