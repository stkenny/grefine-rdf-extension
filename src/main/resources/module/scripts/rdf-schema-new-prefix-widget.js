function NewPrefixWidget(manager){
	this._prefixesManager = manager;
}

NewPrefixWidget.prototype.show = function(msg,def_prefix, onDone){
	var self = this;
    
    var dialog = $(DOM.loadHTML("rdf-extension","scripts/dialogs/new-prefix-widget.html"));
    self._elmts = DOM.bind(dialog);
    self._level = DialogSystem.showDialog(dialog);
    
    self._elmts.dialogHeader.html($.i18n('rdf-ext-prefix/header'));
    self._elmts.rdfext_prefix_pre.html($.i18n('rdf-ext-prefix/pre')+":");
    self._elmts.rdfext_prefix_uri.html($.i18n('rdf-ext-prefix/uri')+":");
    self._elmts.rdfext_prefix_force.html($.i18n('rdf-ext-prefix/force')+":");
    self._elmts.rdfext_prefix_voc.html($.i18n('rdf-ext-prefix/voc'));
    self._elmts.rdfext_prefix_fetch.html($.i18n('rdf-ext-prefix/fetch'));
    self._elmts.rdfext_prefix_imp.html($.i18n('rdf-ext-prefix/imp'));
    self._elmts.rdfext_prefix_file.html($.i18n('rdf-ext-prefix/file')+":");
    self._elmts.rdfext_prefix_format.html($.i18n('rdf-ext-prefix/format')+":");
    self._elmts.rdfext_prefix_auto.html($.i18n('rdf-ext-prefix/auto'));
    self._elmts.rdfext_prefix_turtle.html($.i18n('rdf-ext-prefix/turtle'));
    self._elmts.rdfext_prefix_rdfxml.html($.i18n('rdf-ext-prefix/rdfxml'));
    self._elmts.rdfext_prefix_n3.html($.i18n('rdf-ext-prefix/n3'));
    self._elmts.rdfext_prefix_ntriple.html($.i18n('rdf-ext-prefix/ntriple'));
    self._elmts.okButton.html($.i18n('rdf-ext-buttons/ok'));
    self._elmts.cancelButton.html($.i18n('rdf-ext-buttons/cancel'));
    self._elmts.advancedButton.html($.i18n('rdf-ext-buttons/advanced')+"...");
    
    if(msg){
    	self._elmts.message.addClass('message').html(msg);
    }

    if(def_prefix){
    	self._elmts.prefix.val(def_prefix);
    	self.suggestUri(def_prefix);
    }

   Refine.wrapCSRF(function(token) {
   self._elmts.file_upload_form.submit(function(e){
	   e.preventDefault();
	   
        var fetchOption = self._elmts.fetching_options_table.find('input[name="vocab_fetch_method"]:checked').val();
	   	
    	var name = self._elmts.prefix.val();
    	var uri = self._elmts.uri.val();
    	if(self._prefixesManager._hasPrefix(name)){
    		alert($.i18n('rdf-ext-prefix/pref')+' "' + name + '" '+$.i18n('rdf-ext-prefix/defined'));
    		return;
    	}

    	var force_import = (self._elmts.forceImport.attr('checked') !== undefined);

    	var dismissBusy;

    	if(fetchOption === 'file'){
    		//prepare values
    		$('#vocab-hidden-prefix').val(name);
    		$('#vocab-hidden-uri').val(uri);
    		$('#vocab-hidden-project').val(theProject.id);

    		dismissBusy = DialogSystem.showBusy($.i18n('rdf-ext-prefix/voc-upload')+' ');

    		    $(this).ajaxSubmit({
    				url: "command/rdf-extension/upload-file-add-prefix",
    				type: "post",
    				dataType: "json",
    				headers: { 'X-CSRF-TOKEN': token },
    				success:function(data) {
    					dismissBusy();
    					if (data.code === 'error')
    					{
    						alert("Error: " + data.message);
    					} else {
	    			    	if(onDone){
	    						onDone(name,uri);
	    						self._dismiss();
	    			    	}	
    					}
    			    }
    		    });


    		return false;
    	}
    	});
    	
		dismissBusy = DialogSystem.showBusy($.i18n('rdf-ext-prefix/try-upload')+' ' + uri);
    	Refine.wrapCSRF(function(token) {
		    $.post("command/rdf-extension/add-prefix",
    			{
    			    "csrf_token": token,
    				name:name,
    				uri:uri,
    				"fetch-url":uri,
    				project: theProject.id,
    				"force-import": force_import,
    				fetch:fetchOption
				},
				function(data)
				{
					dismissBusy();
		    		if (data.code === "error"){
		    			alert('Error:' + data.message);
		    		}else{
		    			if(onDone){
		    				onDone(name,uri);
		    				self._dismiss();
		    			}
		    		}

				}
			);
		});
    });
    
    
    self._elmts.okButton.click(function() {
    	self._elmts.file_upload_form.submit();
    	   	
    });
    
    self._elmts.cancelButton.click(function() {
        self._dismiss();
    });
    
    self._elmts.advancedButton.click(function() {
        self._elmts.fetching_options_table.show();
        $('#advanced_options_button').hide();
        $('#advanced_options_button').attr("disabled", "true");
    });
    
    self._elmts.fetching_options_table
	.hide()
	.find('input[name="vocab_fetch_method"]')
	.click(
			function(){
				var upload = $(this).val()!=='file';
				self._elmts.fetching_options_table.find('.upload_file_inputs').attr('disabled',upload);
			}
		);
    
    self._elmts.prefix.bind('change',function(){
    	self.suggestUri($(this).val());
    	}).focus();
    
    self._elmts.prefix.change(function(){
    	self.suggestUri($(this).val());
    	}).focus();
};

NewPrefixWidget.prototype.suggestUri = function(prefix){
	var self = this;
	$.get(
			'command/rdf-extension/get-prefix-cc-uri',
			{prefix:prefix},
			function(data){
				if(!self._elmts.uri.val() && data.uri){
					self._elmts.uri.val(data.uri);
					if(self._elmts.message.text()){
						self._elmts.uri_note.html('('+$.i18n('rdf-ext-prefix/sugg')+' <em><a target="_blank" href="http://prefix.cc">prefix.cc</a></em> '+$.i18n('rdf-ext-prefix/provided')+')');
					}else{
						self._elmts.uri_note.html('('+$.i18n('rdf-ext-prefix/suggested')+' <a target="_blank" href="http://prefix.cc">prefix.cc</a>)');
					}
				}
			},
			"json"
		);
};

NewPrefixWidget.prototype._dismiss =  function() {
	var self = this;
	DialogSystem.dismissUntil(self._level - 1);
};
