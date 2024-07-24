RdfSchemaAlignmentDialog.UINode = function(dialog, node, table, options) {
	this._dialog = dialog;
    this._node = node;
    this._options = options;
    
    this._linkUIs = [];
    this._detailsRendered = false;
        
    this._tr = table.insertRow(table.rows.length);
    this._tdMain = this._tr.insertCell(0);
    this._tdToggle = this._tr.insertCell(1);
    this._tdDetails = this._tr.insertCell(2);
    
    $(this._tdMain).addClass("schema-alignment-node-main").attr("width", "250").addClass("padded");
    $(this._tdToggle).addClass("schema-alignment-node-toggle").attr("width", "1%").addClass("padded").hide();
    $(this._tdDetails).addClass("schema-alignment-node-details").attr("width", "62%").hide();
    
    
    this._renderMain();
    //this._renderTypes();
    
    this._expanded = options.expanded;
    if (this._isExpandable()) {
        this._showExpandable();
    }
};

RdfSchemaAlignmentDialog.UINode._maximum_node_length = 35;
RdfSchemaAlignmentDialog.UINode._shortenLiteral = function(str){
	if(str && str.length>RdfSchemaAlignmentDialog.UINode._maximum_node_length){
		return str.substring(0,RdfSchemaAlignmentDialog.UINode._maximum_node_length-3) + '...';
	}else{
		return str;
	}
	
};

RdfSchemaAlignmentDialog.UINode._shortenResource = function(str){
	if(str && str.length>RdfSchemaAlignmentDialog.UINode._maximum_node_length){
		if(str.match(/^http:\/\//)){
			var localname = str.indexOf('#')==-1?str.substring(str.lastIndexOf('/')):str.substring(str.indexOf('#'));
			return 'http://...' + localname;
		}else{
			return RdfSchemaAlignmentDialog.UINode._shortenLiteral(str);
		}
	}else{
		return str;
	}
	
};

RdfSchemaAlignmentDialog.UINode.prototype._renderMain = function() {
    $(this._tdMain).empty();
    var self = this;
    
    var type_html = !this._isExpandable()? '' : '<tr>' +
    	   '<td>' +
  	      	'<table width="100%" bind="rdfTypesTable" class="rdfTypesTable">' +
  	      	    '<tr bind="rdfTypesTr"><td bind="rdfTypesTd">&nbsp;</td></tr>' +
  	      		'<tr bind="addRdfTyprTr">' + 
  	      			'<td>' +
  	      				'<div class="padded">' +
  	      					'<a bind="addRdfTypeLink" href="#" class="action">'+$.i18n('rdf-ext-schema/add-type')+'</a>' +
  	      				'</div>' + 
  	      			'</td>' +
  	      		'</tr>' +
  	      	'</table>' +
	       '</td>' +
  	  '</tr>';
    var html = $(
    	'<table width="100%">' +
    	  '<tr>' +
    	    '<td bind="nodeLabel">' +
    	    '</td>' +
    	  '</tr>' +
    	  type_html + 
    	'</table>'
    	  ).appendTo(this._tdMain)
    	;
    
    var elmts = DOM.bind(html);
    this._tdNodeLabel = elmts.nodeLabel;
    if(elmts.addRdfTypeLink){
    	var typesTable = $('<table width="100%"></table>')[0];
    	if(self._node.rdfTypes && self._node.rdfTypes.length>0){
    		var func = function(i){
    			return function(){
    				self._removeRdfType(i);
					self._renderMain();
    			};
    		};
    		for(var i=0;i<self._node.rdfTypes.length;i++){
    			//var f = func(i);
    			var tr = typesTable.insertRow(typesTable.rows.length);
    			var img = $('<img />').attr("title", "remove type").attr("src", "images/close.png").css("cursor", "pointer").click(
    				
    						func(i)
    	           
    			);
    			$(tr).append($('<td>').append(img));
    			$(tr).append($('<td>').text(RdfSchemaAlignmentDialog.UINode._shortenResource(self._getTypeName(self._node.rdfTypes[i]))));
    		}
    		elmts.rdfTypesTd.html(typesTable);
    	}else{
    		elmts.rdfTypesTr.remove();
    	}
    	elmts.addRdfTypeLink.click(function(evt){
    		evt.preventDefault();
    		self._addRdfType(evt.target);
    	});
    }
    var a = $('<a href="javascript:{}"></a>')
        .addClass("schema-alignment-node-tag")
        .appendTo(this._tdNodeLabel)
        .click(function(evt) {
            self._showNodeConfigDialog();
        });
        
    if (this._node.nodeType == "cell-as-resource" || 
        this._node.nodeType == "cell-as-literal") {
        
    	var literal = this._node.nodeType === "cell-as-literal";
    	if(this._node.isRowNumberCell){
    		a.html(literal?'':' URI');
    		$('<span></span>')
        	.text('('+$.i18n('rdf-ext-schema/row-index')+')')
        	.addClass("schema-alignment-node-column")
        	.prependTo(a);
    	}else{
    		if ("columnName" in this._node) {
    			a.html(literal?" "+$.i18n('rdf-ext-schema/cell'):" URI");
            
    			$('<span></span>')
                	.text(this._node.columnName)
                	.addClass("schema-alignment-node-column")
                	.prependTo(a);
    		} else {
    			a.html("Configure?");
    		}
    	}
    } else if (this._node.nodeType == "resource") {
        if ("value" in this._node) {
            a.html(RdfSchemaAlignmentDialog.UINode._shortenResource(this._node.value));
        } else {
            a.html($.i18n('rdf-ext-schema/which-res'));
        }
    } else if (this._node.nodeType == "literal") {
        if ("value" in this._node) {
            a.html(RdfSchemaAlignmentDialog.UINode._shortenLiteral(this._node.value));
        } else {
            a.html($.i18n('rdf-ext-schema/what-val'));
        }
    } else if (this._node.nodeType == "blank") {
        a.html("(blank) ");
    }else if (this._node.nodeType == "cell-as-blank") {
    	a.html("(blank) "+$.i18n('rdf-ext-schema/cell'));
    }
    
    //Types
    /*var aux_table = $('<table>').appendTo($(this._tdMain));
    aux_table.append($('<tr>').append(td));
    this._typesTd = $('<td>').attr("width", "250").appendTo($('<tr>').appendTo(aux_table));
    this._renderTypes();*/
};


RdfSchemaAlignmentDialog.UINode.prototype._isExpandable = function() {
    return this._node.nodeType == "cell-as-resource" ||
        this._node.nodeType == "blank" ||
        this._node.nodeType == "resource" ||
        this._node.nodeType == "cell-as-blank" ;
};

RdfSchemaAlignmentDialog.UINode.prototype._showExpandable = function() {
    $(this._tdToggle).show();
    $(this._tdDetails).show();
    
    if (this._detailsRendered) {
        return;
    }
    this._detailsRendered = true;
    
    this._collapsedDetailDiv = $('<div></div>').appendTo(this._tdDetails).addClass("padded").html("...");
    this._expandedDetailDiv = $('<div></div>').appendTo(this._tdDetails).addClass("schema-alignment-detail-container");
    
    this._renderDetails();
    
    var self = this;
    var show = function() {
        if (self._expanded) {
            self._collapsedDetailDiv.hide();
            self._expandedDetailDiv.show();
        } else {
            self._collapsedDetailDiv.show();
            self._expandedDetailDiv.hide();
        }
    };
    show();
    
    //$(this._tdToggle).html("&nbsp;");
    $('<img />')
        .attr("src", this._expanded ? "extension/rdf-extension/images/expanded.png" : "extension/rdf-extension/images/collapsed.png")
        .appendTo(this._tdToggle)
        .click(function() {
            self._expanded = !self._expanded;
            
            $(this).attr("src", self._expanded ? "extension/rdf-extension/images/expanded.png" : "extension/rdf-extension/images/collapsed.png");
            
            show();
        });
};

RdfSchemaAlignmentDialog.UINode.prototype._renderDetails = function() {
    var self = this;

    this._tableLinks = $('<table></table>')
    	.addClass("schema-alignment-table-layout")
    	.appendTo(this._expandedDetailDiv)[0];
    
    if ("links" in this._node && this._node.links !== null) {
        for (var i = 0; i < this._node.links.length; i++) {
            this._linkUIs.push(new RdfSchemaAlignmentDialog.UILink(
                this._dialog, 
                this._node.links[i], 
                this._tableLinks, 
                { expanded: true }, 
                this
            ));
        }
    }
    
    var divFooter = $('<div></div>').addClass("padded").appendTo(this._expandedDetailDiv);
    
    $('<a href="javascript:{}"></a>')
        .addClass("action")
        .text($.i18n('rdf-ext-schema/add-prop'))
        .appendTo(divFooter)
        .click(function() {
            var newLink = {
                property: null,
                target: {
                    nodeType: "cell-as-literal"
                }
            };
            self._linkUIs.push(new RdfSchemaAlignmentDialog.UILink(
                self._dialog,
                newLink, 
                self._tableLinks,
                {
                    expanded: true,
                    mustBeCellTopic: false
                },
                self
            ));
        });
};

RdfSchemaAlignmentDialog.UINode.prototype._showNodeConfigDialog = function(){
	var self = this;
    var frame = DialogSystem.createDialog();
    
    frame.width("610px");
    
    $('<div></div>').addClass("dialog-header").text($.i18n('rdf-ext-schema/rdf-node')).appendTo(frame);
    var body = $('<div class="grid-layout layout-full"></div>').addClass("dialog-body rdf-schema").appendTo(frame);
    var footer = $('<div></div>').addClass("dialog-footer").appendTo(frame);
    
    /*--------------------------------------------------
     * Body
     *--------------------------------------------------
     */
    var html = $(
        '<table>' +
            '<tr>' +
                '<td>' +
                    '<table class="grid-layout layout-tight rdf-node-table">' +
                        '<tr>' +
                            '<td>' +
                                '<div class="rdf-schema-alignment-node-dialog-step-header">' +
                                $.i18n('rdf-ext-schema/use-content')+'...' +
                                '</div>' +
                            '</td>' +
                            '<td>' +
                            	'<div class="rdf-schema-alignment-node-dialog-step-header">' +
                            	$.i18n('rdf-ext-schema/content-used')+' ...' + 
                            	'</div>' +
        					'</td>'+
                        '</tr>' +
                        '<tr>' +
                            '<td><div class="rdf-schema-alignment-node-dialog-step rdf-schema-alignment-node-dialog-column-list" bind="divColumns"></div></td>' +
                            '<td>' +
                              '<div class="rdf-schema-alignment-node-dialog-step">' + 
                                '<table width="100%" cols="2">' +
                                    '<tr>' +
                                        '<td><input type="radio" name="rdf-content-radio" value="resource" bind="rdf_content_uri_radio" /></td>' +
                                        '<td>'+$.i18n('rdf-ext-as/uri')+'</td>' +
                                    '</tr>' +
                                    '<tr>' +
                                        '<td><input type="radio" name="rdf-content-radio" value="literal" bind="rdf_content_txt_radio" /></td>' +
                                        '<td>'+$.i18n('rdf-ext-as/text')+'</td>' +
                                    '</tr>' +
                                    '<tr>' +
                                    	'<td><input type="radio" name="rdf-content-radio" value="literal" id="rdf-content-lang-radio" bind="rdf_content_lang_radio" /></td>' +
                                    	'<td>'+$.i18n('rdf-ext-as/lang')+' <input type="text" id="rdf-content-lang-input" bind="rdf_content_lang_input" size="4"/></td>' +
                                    '</tr>' +
                                    '<tr>' +
                                    	'<td><input type="radio" name="rdf-content-radio" value="literal" bind="rdf_content_int_radio" id="rdf-content-int-radio"/></td>' +
                                    	'<td>'+$.i18n('rdf-ext-as/int')+'</td>' +
                                    '</tr>' +
                                    '<tr>' +
                                    	'<td><input type="radio" name="rdf-content-radio" value="literal" bind="rdf_content_non_int_radio" id="rdf-content-non-int-radio" /></td>' +
                                    	'<td>'+$.i18n('rdf-ext-as/nonint')+'</td>' +
                                    '</tr>' +
                                    '<tr>' +
                                    	'<td><input type="radio" name="rdf-content-radio" value="literal" bind="rdf_content_date_radio" id="rdf-content-date-radio" /></td>' +
                                    	'<td>'+$.i18n('rdf-ext-as/date')+' <span class="rdf-node-info">(YYYY-MM-DD)</span></td>' +
                                    '</tr>' +
                                    '<tr>' +
                                    	'<td><input type="radio" name="rdf-content-radio" value="literal" bind="rdf_content_date_time_radio" id="rdf-content-date-time-radio" /></td>' +
                                    	'<td>'+$.i18n('rdf-ext-as/datetime')+' <span class="rdf-node-info">(YYYY-MM-DD HH:MM:SS)</span></td>' +
                                    '</tr>' +
                                    '<tr>' +
                                		'<td><input type="radio" name="rdf-content-radio" value="literal" bind="rdf_content_boolean_radio" id="rdf-content-boolean-radio"/></td>' +
                                		'<td>'+$.i18n('rdf-ext-as/bool')+'</td>' +
                                	'</tr>' +
                                    '<tr>' +
                                    	'<td><input type="radio" name="rdf-content-radio" value="literal" bind="rdf_content_type_radio" id="rdf-content-type-radio" /></td>' +
                                    	'<td>'+$.i18n('rdf-ext-as/custom')+' <span class="rdf-node-info">(specify type URI)</span></td>' +
                                    '</tr>' +
                                    '<tr>' +
                                    	'<td></td>' +
                                		'<td colspan="2"><input type="text" size="25" id="rdf-content-type-input" bind="rdf_content_type_input"/></td>' +
                                	'</tr>' +
                                    '<tr>' +
                                	    '<td><input type="radio" name="rdf-content-radio" value="blank" bind="rdf_content_blank_radio" /></td>' +
                                	    '<td>'+$.i18n('rdf-ext-as/blank')+'</td>' +
                                    '</tr>' +
                                  '</table>' +
                                '</div>' +
                                '<div class="rdf-schema-alignment-node-dialog-step-header">' +
                                $.i18n('rdf-ext-schema/use-exp')+'...' + 
                                '</div>' + 
                                '<div class="rdf-schema-alignment-node-dialog-step" style="margin-top:3px">' +
                                	'<table width="100%" class="grid-layout">' +
                                		'<tr>' +
                                			'<td><span class="rdf-value-expression" bind="rdf_cell_expr" id="rdf-cell-expr"></span></td>' +
                                		'</tr>' +
                                		'<tr>' +
                                			'<td><a href="#" bind="rdf_cell_expr_preview">'+$.i18n('rdf-ext-schema/preview-edit')+'</a></td>' +
                                		'</tr>' + 
                                	'</table>' +
                                '</div>' +
                            '</td>' +                                        
                        '</tr>' +
                    '</table>' +
                '</td>' +
            '</tr>' +
        '</table>'
    ).appendTo(body);
    
    var elmts = DOM.bind(html);
    
    var tableColumns = $('<table></table>')
    //.attr("cellspacing", "5")
   // .attr("cellpadding", "0")
    .appendTo(elmts.divColumns)[0];
    
    var makeColumnChoice = function(column) {
        var tr = tableColumns.insertRow(tableColumns.rows.length);
        var radio = $('<input />')
            .attr("type", "radio")
            .val(column.name)
            .attr("name", "rdf-column-radio")
            .appendTo(tr.insertCell(0))
            .bind("click",function(){
            	$("#rdf-constant-value-input").prop("disabled","disabled");
            })
            ;
            
        var td = tr.insertCell(1);
        if (column.name == self._node.columnName) {
            radio.prop("checked", "true");
        }
        $('<span></span>').text(column.name).appendTo(td);
    };
    
    var makeRowIndexChoice = function(checked) {
        var tr = tableColumns.insertRow(tableColumns.rows.length);
        $('<input />')
            .attr("type", "radio")
            .prop("checked", checked)
            .val("")
            .attr("name", "rdf-column-radio")
            .attr("id","rdf-row-index-column-radio")
            .appendTo(tr.insertCell(0))
            .bind("click",function(){
            	$("#rdf-constant-value-input").prop("disabled","disabled");
            })
            ;
            
        var td = tr.insertCell(1);
       	$(td).addClass('rdf-schema-bottom-separated');
        $('<span></span>').text('('+$.i18n('rdf-ext-schema/row-index')+')').appendTo(td);
    };
    
    var makeConstantValueChoice = function(checked,value){
    	var tr = tableColumns.insertRow(tableColumns.rows.length);
        $('<input />')
            .attr("type", "radio")
            .prop("checked", checked)
            .val("")
            .attr("name", "rdf-column-radio")
            .attr("id","rdf-constant-value-radio")
            .appendTo(tr.insertCell(0))
            .bind("click",function(){
            	$("#rdf-constant-value-input").prop("disabled", false);
            })
            ;
            
        var td = tr.insertCell(1);
        $(td).addClass('rdf-schema-top-separated');
        $('<span></span>').text($.i18n('rdf-ext-schema/constant-val')).appendTo(td);
        var initStr = checked ?  'value="' + value + '"':'disabled="disabled"';
        $('<div><input id="rdf-constant-value-input" type="text" ' + initStr + ' size="25" /></div>').appendTo(td);
    };
    
    //interrogation
    var isCellNode = self._node.nodeType.match(/^cell-as-/);
    var isRowIndex = self._node.isRowNumberCell!== undefined && self._node.isRowNumberCell;
    var isNewNode = !isRowIndex && isCellNode;
    
    //Add Row Number
    makeRowIndexChoice(isRowIndex || isNewNode);
    
    var columns = theProject.columnModel.columns;
    for (var i = 0; i < columns.length; i++) {
        makeColumnChoice(columns[i]);
    }
    
    
    //Add constant value
    makeConstantValueChoice(!isCellNode,isCellNode?'':self._node.value);
    
    
    var initInputs = function(){
    	elmts.rdf_content_lang_input.prop("disabled","disabled");
    	elmts.rdf_content_type_input.prop("disabled","disabled");
    	//setContentOptions();
    	if(self._node.nodeType==='resource' || self._node.nodeType==='cell-as-resource'){
    		elmts.rdf_content_uri_radio.prop("checked", true);
    	}else if(self._node.nodeType==='cell-as-literal' || self._node.nodeType==='literal'){
    		if(self._node.lang){
    			elmts.rdf_content_lang_radio.prop("checked", true);
    			elmts.rdf_content_lang_input.prop("disabled", false).val(self._node.lang);
    		}else{
    			if(self._node.valueType){
    				if(self._node.valueType==='http://www.w3.org/2001/XMLSchema#int'){
    					elmts.rdf_content_int_radio.prop("checked",true);
    				}else if(self._node.valueType==='http://www.w3.org/2001/XMLSchema#double'){
    					elmts.rdf_content_non_int_radio.prop("checked",true);
    				}else if(self._node.valueType==='http://www.w3.org/2001/XMLSchema#date'){
    					elmts.rdf_content_date_radio.prop("checked",true);
    				}else if(self._node.valueType==='http://www.w3.org/2001/XMLSchema#dateTime'){
    					elmts.rdf_content_date_time_radio.prop("checked",true);
    				}else if(self._node.valueType==='http://www.w3.org/2001/XMLSchema#boolean'){
    					elmts.rdf_content_boolean_radio.prop("checked",true);
    				}
    				else{
    					elmts.rdf_content_type_radio.prop("checked",true);
    					elmts.rdf_content_type_input.prop("disabled", false).val(self._node.valueType);
    				}
    			}else{
    				elmts.rdf_content_txt_radio.prop("checked",true);
    			}
    		}
    	}else{
    		//blank node
    		elmts.rdf_content_blank_radio.prop("checked",true);
    	}
    	
    	//set cell expression
    	if(self._node.expression){
    		expr = self._node.expression;
    	}else{
    		expr = 'value';//default expression
    	}
    	elmts.rdf_cell_expr.empty().text(expr);
    	
    	//click events
    	elmts.rdf_content_uri_radio.bind("click",function(){
    		$('input.rdf-text-attributes-input').add("#rdf-content-type-input").prop("disabled","disabled");
    	});
    	
    	elmts.rdf_content_txt_radio.add(elmts.rdf_content_int_radio[0]).add(elmts.rdf_content_non_int_radio[0])
    	.add(elmts.rdf_content_date_radio[0]).add(elmts.rdf_content_date_time_radio[0]).add(elmts.rdf_content_blank_radio[0])
    	.bind("click",function(){
    		$('#rdf-content-lang-input').add("#rdf-content-type-input").prop("disabled","disabled");
    	});
    	
    	elmts.rdf_content_lang_radio.bind("click",function(){
    		$('#rdf-content-lang-input').prop("disabled", false);
    	});
    	
    	elmts.rdf_content_type_radio.bind("click",function(){
    		$('#rdf-content-type-input').prop("disabled", false);
    	});
    	
    	//edit/preview
    	elmts.rdf_cell_expr_preview.bind("click",function(e){
    		e.preventDefault();
    		var nodeSubtype = $("input[name='rdf-content-radio']:checked")[0].value;
    		if($("#rdf-constant-value-radio").prop('checked')){
    			//constant node
    			var val = $('#rdf-constant-value-input').val();
    			if(nodeSubtype === 'blank'){
    				//constant blank
    				alert($.i18n('rdf-ext-schema/alert-shared'));
    			}else if(nodeSubtype==='literal'){
    				//constant literal
    				alert($.i18n('rdf-ext-schema/alert-literal')+" '" +  val + "'");
    			}else if(nodeSubtype==='resource'){
    				//constant resource
    				alert($.i18n('rdf-ext-schema/alert-resource')+" <" +  val + ">");
    			}
    		}else{
    			//cell-based node
    			var columnName = $("input[name='rdf-column-radio']:checked")[0].value;
		        var expr = $("#rdf-cell-expr").text();
    			if(nodeSubtype === 'blank'){
    				//blank... not much to do
    				alert($.i18n('rdf-ext-schema/alert-blank-create'));
    			}else if(nodeSubtype==='literal'){
    				//literal... expression preview
    				self._preview(expr,columnName,false);
    			}else{
    				//resource... URI preview
    		        self._preview(expr,columnName,true);
    			}
    		}
    	});
    };
    
    initInputs();
    /*--------------------------------------------------
     * Footer
     *--------------------------------------------------
     */
    var getResultJSON = function() {
    	var nodeType = $("#rdf-constant-value-radio").prop('checked')?'':'cell-as-';
    	var nodeSubtype = $("input[name='rdf-content-radio']:checked")[0].value;
        var node = {
            nodeType: nodeType + nodeSubtype
        };
        if (nodeSubtype === "literal") {
        	//get language
        	if($('#rdf-content-lang-radio').prop('checked')){
        		node.lang = $('#rdf-content-lang-input').val();
        	}else{
        		//get value type
        		if($('#rdf-content-int-radio').prop('checked')){
        			node.valueType = 'http://www.w3.org/2001/XMLSchema#int';
        		}else if($('#rdf-content-non-int-radio').prop('checked')){
        			node.valueType = 'http://www.w3.org/2001/XMLSchema#double';
        		}else if($('#rdf-content-date-radio').prop('checked')){
        			node.valueType = 'http://www.w3.org/2001/XMLSchema#date';
        		}else if($('#rdf-content-date-time-radio').prop('checked')){
        			node.valueType = 'http://www.w3.org/2001/XMLSchema#dateTime';
        		}else if($('#rdf-content-boolean-radio').prop('checked')){
        			node.valueType = 'http://www.w3.org/2001/XMLSchema#boolean';
        		}else if($('#rdf-content-type-radio').prop('checked')){
        			//check custom datatype URI
        			var val = $('#rdf-content-type-input').val();
        			if(!val){
            			alert('Enter the custom type URI');
            			return null;
            		}
        			node.valueType = val;
        		}
        	}
        }
        
        if(nodeType==='cell-as-'){
        	//get columnName
        	//get isRowNumberCell        	
        	var colName = $("input[name='rdf-column-radio']:checked")[0].value;
        	if(colName && colName!=''){
        		node.isRowNumberCell = false;
        		node.columnName = colName;
        	}else{
        		node.isRowNumberCell = true;
        	}
        	//get expression if not blank
        	if(nodeSubtype!=='blank'){
        		node.expression = $('#rdf-cell-expr').text();
        	}
        }else{
        	//get value if not blank
        	if(nodeSubtype!=='blank'){
        		//check that value is entered
        		var val = $('#rdf-constant-value-input').val();
        		if(!val){
        			alert($.i18n('rdf-ext-schema/alert-enter-const'));
        			return null;
        		}
        		node.value = val;
        	}
        }
        return node;
    };
    
    $('<button></button>').addClass('button').html($.i18n('rdf-ext-buttons/ok')).click(function() {
    	var node = getResultJSON();
        if (node !== null) {
        	if(self._node.rdfTypes){
        		node.rdfTypes = cloneDeep(self._node.rdfTypes);
        	}
        	
            DialogSystem.dismissUntil(self._level - 1);
            
            self._node = node;
            /*if('columnIndex' in node){
            	if(node.columnIndex!==-1){
            		self._node.columnName = theProject.columnModel.columns[node.columnIndex].name;
            	}else{
            		self._node.isRowNumberCell = true;
            	}
            }*/
            self.render();
            //self._dialog.preview();
        }
    }).appendTo(footer);
    
    $('<button></button>').addClass('button').text($.i18n('rdf-ext-buttons/cancel')).click(function() {
        DialogSystem.dismissUntil(self._level - 1);
    }).appendTo(footer);
    
    
    self._level = DialogSystem.showDialog(frame);
};

RdfSchemaAlignmentDialog.UINode.prototype._preview = function(expr,columnName,isUri){
	//var self = this;
	if(isUri){
		RdfDataTableView.previewUrisOnVisibleRows(
				{
					"cellIndex":columnName?RdfSchemaAlignmentDialog._findColumn(columnName).cellIndex:0,
					"columnName":columnName
				},
				$.i18n('rdf-ext-schema/preview-uri-val'), 
				expr,
				!columnName,
				RdfSchemaAlignment._defaultNamespace,
				function(expression){
					expression = expression.substring(5);//grel:
					$("#rdf-cell-expr").empty().text(expression);
				}
			);
	}else{
		RdfDataTableView.previewExprsOnVisibleRows(
				{
					"cellIndex":columnName?RdfSchemaAlignmentDialog._findColumn(columnName).cellIndex:0,
					"columnName":columnName
				},
				$.i18n('rdf-ext-schema/preview-uri-val'), 
				expr,
				!columnName,
				function(expression){
					expression = expression.substring(5);//grel:
					$("#rdf-cell-expr").empty().text(expression);
				}
			);
	}
	        
};


RdfSchemaAlignmentDialog.UINode.prototype.render = function() {
    this._renderMain();
    if (this._isExpandable()) {
        this._showExpandable();
    } else {
        this._hideExpandable();
    }
};

RdfSchemaAlignmentDialog.UINode.prototype.removeLink = function(linkUI) {
    for (var i = this._linkUIs.length - 1; i >= 0; i--) {
        if (this._linkUIs[i] === linkUI) {
            this._linkUIs.splice(i, 1);
            //this._node.links.splice(i, 1);
            break;
        }
    }
};

RdfSchemaAlignmentDialog.UINode.prototype._hideExpandable = function() {
    $(this._tdToggle).hide();
    $(this._tdDetails).hide();
};

RdfSchemaAlignmentDialog.UINode.prototype._addRdfType = function(src){
	var self = this;
	new RdfSchemaAlignmentDialog.RdfResourceDialog(src,'class',theProject.id,self._dialog, self._dialog._prefixesManager,function (obj) {
			self._addNodeRdfType(obj.id,obj.name);
	});
};

RdfSchemaAlignmentDialog.UINode.prototype._removeRdfType = function(index){
	var self = this;
	self._node.rdfTypes.splice(index,1);
};

RdfSchemaAlignmentDialog.UINode.prototype._addNodeRdfType = function(uri,curie){
	if(!this._node.rdfTypes){
		this._node.rdfTypes = [];
	}
    this._node.rdfTypes.push({uri:uri,curie:curie});
    this._renderMain();
};

RdfSchemaAlignmentDialog.UINode.prototype._getTypeName = function(t){
	if(!t){return '';}
	if(t.curie !== undefined && t.curie!==''){
		return t.curie;
	}else{
		return t.uri;
	}
};

RdfSchemaAlignmentDialog.UINode.prototype.getJSON = function() {
    var result = null;
    var getLinks = false;
    
    if (this._node.nodeType.match(/^cell-as-/)) {
        if (! ("columnName" in this._node || "isRowNumberCell" in this._node)) {
            return null;
        }
        
        if (this._node.nodeType == "cell-as-resource") {
       		result = {
        				nodeType: this._node.nodeType,
        				expression: this._node.expression,
        		};
            getLinks = true;
        } else if (this._node.nodeType == "cell-as-literal") {
            result = {
                nodeType: this._node.nodeType,
                expression: this._node.expression
            };
       		if(this._node.valueType){
       			result.valueType =  this._node.valueType;
       		}
       		if(this._node.lang){
       			result.lang =  this._node.lang;
       		}
        } else if (this._node.nodeType == "cell-as-blank") {
            result = {
                nodeType: this._node.nodeType,
            };
            getLinks = true;
        }
        
        if(this._node.columnName){
   			result.columnName =  this._node.columnName;
   		}
        result.isRowNumberCell = this._node.isRowNumberCell;
    } else if (this._node.nodeType == "resource") {
    	if (!("value" in this._node) || !this._node.value) {
            return null;
        }
        result = {
            nodeType: this._node.nodeType,
            value: this._node.value
        };
        getLinks = true;
    } else if (this._node.nodeType == "literal") {
        if (!("value" in this._node) || !this._node.value) {
            return null;
        }
        result = {
            nodeType: this._node.nodeType,
            value: this._node.value,
        };
        if(this._node.valueType){
   			result.valueType =  this._node.valueType;
   		}
   		if(this._node.lang){
   			result.lang =  this._node.lang;
   		}
    } else if (this._node.nodeType == "blank") {
        result = {
            nodeType: this._node.nodeType,
        };
        getLinks = true;
    }
    
    if (!result) {
        return null;
    }
    if (getLinks) {
    	var rdfTypes = [];
    	if(this._node.rdfTypes){
    		for(var i=0;i<this._node.rdfTypes.length; i++){
    			rdfTypes.push({uri:this._node.rdfTypes[i].uri,
    							curie:this._node.rdfTypes[i].curie
    				});
    		}
    	}
    	result.rdfTypes = rdfTypes;
        var links = [];
        for (var i = 0; i < this._linkUIs.length; i++) {
            var link = this._linkUIs[i].getJSON();
            if (link !== null) {
                links.push(link);
            }
        }
        result.links = links;
    }
    
    return result;
};

RdfSchemaAlignmentDialog.NewRdfResourceDialog = function(elmt,defaultVal,onDone){
	var menu = MenuSystem.createMenu().width('400px');
	menu.html('<div class="schema-alignment-link-menu-type-search">' + 
			'<span class="schema-alignment-node-column">URI: <small>('+$.i18n('rdf-ext-schema/resolve')+')</small></span>' + 
			'<input type="text" bind="newResourceUri" size="50"><br/>' +
			'<button class="button" bind="applyBtn">'+$.i18n('rdf-ext-buttons/apply')+'</button>' + 
			'<button class="button" bind="cancelBtn">'+$.i18n('rdf-ext-buttons/cancel')+'</button>'
			);
	MenuSystem.showMenu(menu,function(){});
	MenuSystem.positionMenuLeftRight(menu, $(elmt));
	var elmts = DOM.bind(menu);
	elmts.newResourceUri.val(defaultVal).focus().select();
	elmts.cancelBtn.click(function(){
		MenuSystem.dismissAll();
	});
	
	elmts.applyBtn.click(function(){
		var val = elmts.newResourceUri.val();
		if(!val){
			alert($.i18n('rdf-ext-schema/alert-uri'));
			return;
		}
		MenuSystem.dismissAll();
		if(val.substring(0,1)===':'){
			val = val.substring(1);
		}
		var obj = {
				id:val,
				name:val.match('^http://')?val:':'+val
		};
		onDone(obj);
	});
};
RdfSchemaAlignmentDialog.RdfResourceDialog = function(elmt,lookFor,projectId,parent,prefixesManager,onDone){
	//var self = this;
	var menu = MenuSystem.createMenu().width('250px');
	menu.html('<div class="schema-alignment-link-menu-type-search">' + 
			'<span>'+$.i18n('rdf-ext-schema/search-for')+' ' + lookFor + ':</span>' + 
			'<input type="text" bind="newResourceUri" >' 
			);
	MenuSystem.showMenu(menu,function(){});
	MenuSystem.positionMenuLeftRight(menu, $(elmt));
	var elmts = DOM.bind(menu);
	
	
	elmts.newResourceUri.suggestterm({type:''+projectId,type_strict:lookFor,parent:'.schema-alignment-link-menu-type-search'}).bind('fb-select',function(e,data){
		MenuSystem.dismissAll();
		if(onDone){
			onDone(data);
		}
	}).bind('fb-select-new',function(e,val){
		MenuSystem.dismissAll();
		if(RdfPrefixesManager.isPrefixedQname(val)){
			//check that the prefix is defined
			var prefix = RdfPrefixesManager.getPrefix(val);
			if(prefixesManager._hasPrefix(prefix)){
				var uri = RdfPrefixesManager.getFullUri(val);
				onDone({name:val,id:uri});
				MenuSystem.dismissAll();
				return;
			}else{
				parent._prefixesManager._addPrefix('<em>' + prefix + '</em> '+$.i18n('rdf-ext-schema/unknown-pref'),prefix);
			}
		}else{
			new RdfSchemaAlignmentDialog.NewRdfResourceDialog(elmt,val,onDone);	
		}
		
	});
	elmts.newResourceUri.focus();
};
