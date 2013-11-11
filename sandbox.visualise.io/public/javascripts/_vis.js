//initialise socket
var socket = new easyXDM.Socket({
	onMessage:function(message, origin) {
		returnMessage = {}
		message = JSON.parse(message)
		if (typeof message.source !== 'undefined' && typeof message.source.callback !== 'undefined') {
    		_vis.common.getFunctionFromString(message.source.callback)(message.body)
    	}
    	else if (message.action === "load"){
			if (message.resource === 'template') {	
				_vis.template.load(message.body)
			}
			else if (message.resource === 'visualisation') {
				_vis.visualisation.load(message.body)
			}
			else {
				console.error = "This message type("+message.action+" " + message.resource + ") is not supported at " + origin
			}
		}
		else if (message.action === "update"){
			if (message.resource === 'template') {	
				_vis.template.update(message.body)
			}
			else if (message.resource === 'visualisation') {
				_vis.visualisation.update(message.body)
			}
			else {
				console.error = "This message type("+message.action+" " + message.resource + ") is not supported at " + origin
			}
		}
		else if(message.action === "get"){
			if (message.resource === 'definition') {
				returnMessage = _vis.definition.get()
			}
			else {
				console.error = "This message type("+message.action+" " + message.resource + ") is not supported at " + origin
			}
		} else if (message.action === "resize" && message.resource === "window") {
			_vis.common.resize(message.body)
		}
		else {
			console.error = "This message type("+message.action+" " + message.resource + ") is not supported at " + origin
		}



		// Send a message back if a callback is defined
		if (typeof message.callback !== 'undefined') {
			returnMessage.action = "response"
			returnMessage.source = message
			socket.postMessage(JSON.stringify(returnMessage));
		}
	}
})

// Visualisation
_vis = {
	// Datasets
	datasets : {
		data : {
			data : ''
		}
	},
	
	// menu
	menu : {
		id : "menu",
		panels : [],
		element : {}
	},
	
	// visualisation
	visualisation : {
		id : 1,
		parameters : {}
	},
	
	template : {
		id : 1
	},
	
	common : {
		settings : {}
	},
	definition : {}
}

_vis.common.resize = function(body){
	_vis.menu.height = body.height;
	$('.menu-content').css("height",body.height)
	$('visualisation').css("height",body.height)
}
//initialize common functions
_vis.common.isNumber = function isNumber(n) {
	return !isNaN(parseFloat(n)) && isFinite(n);
}

_vis.common.escape = function escape(value) {
	/*
	The function _vis.common.escape is under the following copyright:

	<copyright>
		Copyright Mathias Bynens <http://mathiasbynens.be/>

		Permission is hereby granted, free of charge, to any person obtaining
		a copy of this software and associated documentation files (the
		"Software"), to deal in the Software without restriction, including
		without limitation the rights to use, copy, modify, merge, publish,
		distribute, sublicense, and/or sell copies of the Software, and to
		permit persons to whom the Software is furnished to do so, subject to
		the following conditions:

		The above copyright notice and this permission notice shall be
		included in all copies or substantial portions of the Software.

		THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
		EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
		MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
		NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
		LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
		OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
		WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
	</copyright>
	<source>
		https://github.com/mathiasbynens/jsesc
	</source>
	*/

	jsCache = {
	    	// http://es5.github.com/#x7.8.4
	    	// Table 4 — String Single Character Escape Sequences
	    	'\b': '\\b',
	    	'\t': '\\t',
	    	'\n': '\\n',
	    	'\v': '\\x0b', // In IE < 9, '\v' == 'v'
	    	'\f': '\\f',
	    	'\r': '\\r',
	    	// escape double quotes, \u2028, and \u2029 too, as they break input
	    	'\"': '\\\"',
	    	'\u2028': '\\u2028',
	    	'\u2029': '\\u2029',
	    	// we’re wrapping the string in single quotes, so escape those too
	    	'\'': '\\\'',
	    	'\\': '\\\\'
	    }
	// http://mathiasbynens.be/notes/css-escapes
	function cssEscape(string, escapeNonASCII) {
		// Based on `ucs2decode` from http://mths.be/punycode
		var firstChar = string.charAt(0),
		    output = '',
		    counter = 0,
		    length = string.length,
		    value,
		    character,
		    charCode,
		    surrogatePairCount = 0,
		    extraCharCode; // low surrogate

		while (counter < length) {
			character = string.charAt(counter++);
			charCode = character.charCodeAt();
			// if it’s a non-ASCII character and those need to be escaped
			if (escapeNonASCII && (charCode < 32 || charCode > 126)) {
				if ((charCode & 0xF800) == 0xD800) {
					surrogatePairCount++;
					extraCharCode = string.charCodeAt(counter++);
					if ((charCode & 0xFC00) != 0xD800 || (extraCharCode & 0xFC00) != 0xDC00) {
						throw Error('UCS-2(decode): illegal sequence');
					}
					charCode = ((charCode & 0x3FF) << 10) + (extraCharCode & 0x3FF) + 0x10000;
				}
				value = '\\' + charCode.toString(16) + ' ';
			} else {
				// \r is already tokenized away at this point
				// `:` can be escaped as `\:`, but that fails in IE < 8
				if (/[\t\n\v\f:]/.test(character)) {
					value = '\\' + charCode.toString(16) + ' ';
				} else if (/[ !"#$%&'()*+,./;<=>?@\[\\\]^`{|}~]/.test(character)) {
					value = '\\' + character;
				} else {
					value = character;
				}
			}
			output += value;
		}

		if (/^_/.test(output)) { // Prevent IE6 from ignoring the rule altogether
			output = '\\_' + output.slice(1);
		}
		if (/^-[-\d]/.test(output)) {
			output = '\\-' + output.slice(1);
		}
		if (/\d/.test(firstChar)) {
			output = '\\3' + firstChar + ' ' + output.slice(1);
		}

		return {
			'surrogatePairCount': surrogatePairCount,
			'output': output
		};
	}

	function jsEscape(str) {
		return str.replace(/[\s\S]/g, function(character) {
			var charCode = character.charCodeAt(),
			    hexadecimal = charCode.toString(16),
			    longhand = hexadecimal.length > 2,
			    escape;
			if (/[\x20-\x26\x28-\x5b\x5d-\x7e]/.test(character)) {
				// it’s a printable ASCII character that is not `'` or `\`; don’t escape it
				return character;
			}
			if (jsCache[character]) {
				return jsCache[character];
			}
			escape = jsCache[character] = '\\' + (longhand ? 'u' : 'x') + ('0000' + hexadecimal).slice(longhand ? -4 : -2);
			return escape;
		});
	}
	
	return cssEscape(value.replace(/\r\n?/g, '\n'), true).output
}

//Get function from string, with or without scopes (by Nicolas Gauthier)
_vis.common.getFunctionFromString = function(string) {
    var scope = window;
    var scopeSplit = string.split('.');
    for (i = 0; i < scopeSplit.length - 1; i++)
    {
        scope = scope[scopeSplit[i]];

        if (scope == undefined) return;
    }

    return scope[scopeSplit[scopeSplit.length - 1]];
}

_vis.common.settings.update = function updateSetting(setting, value) {
	message = {
		action : "update",
		resource : 'setting',
		body : {
			settings : []
		}
	}
	message.body.settings.push(setting)
	message.body[setting] = value
	socket.postMessage(JSON.stringify(message));
}

_vis.datasets.data.getHeaders = function getHeaders(data) {
	return data.split('\n')[0].split(',');
}

_vis.datasets.data.getNumericHeaders = function getNumericHeaders(data) {
	var columnHeaders = data.split('\n')[0].split(','),
		numericColumnHeaders = []
	$.each(columnHeaders, function(index, value) {
		if(data.split('\n').length === 0 || _vis.common.isNumber(data.split('\n')[1].split(',')[index])) {
			numericColumnHeaders.push(value)
		}
	})
	return numericColumnHeaders
}

_vis.datasets.data.getColumnValuesByHeader = function getColumnValuesByHeader(header) {
	values = []
	columnHeaders = _vis.datasets.data.data.split('\n')[0].split(',')
	columnIndex = -1
	
	$.each(columnHeaders, function(index, value) {
		if(value === header) {
			columnIndex = index
		}
	})
	
	if(columnIndex >= 0) {
		$.each(this.data.split('\n'), function(index, value) {
			//skip headers
			if (index > 0) {
				columnValue = value.split(',')[columnIndex]
				if ($.inArray(columnValue, values) == -1) values.push(columnValue);
			}
		})
	}
	return values
}

_vis.datasets.data.update = function update() {
	if (typeof _vis.datasets.data.data === 'undefined' || _vis.datasets.data.data === null) {
		_vis.datasets.data.headers = []
		_vis.datasets.data.numericHeaders = []
	}
	else {
		_vis.datasets.data.headers = _vis.datasets.data.getHeaders(_vis.datasets.data.data)
		_vis.datasets.data.numericHeaders = _vis.datasets.data.getNumericHeaders(_vis.datasets.data.data)
	}
}

_vis.menu.addPanel = function addPanel(id, heading) {
	panel = _vis.menu.element.panel(id, heading)
	_vis.menu.panels.push(panel)
	return panel
}

_vis.menu.toDOM = function toDOM() {
	$defaultDOM = $('<div class="col-lg-12 menu-content"></div>').css('height',_vis.menu.height)
	$.each(this.panels, function(index, panel) {
		$defaultDOM.append(panel.toDOM(panel))
	})
	return $defaultDOM
}

_vis.menu.element.input = function input(id){
	oInput = {
		type: "input",
		id: id,
		input: true,
		eventListeners: [],
		value: null
	}

	oInput.addEventListener = function addEventListener(event, handler) {
		oInput.eventListeners.push({event: event, handler:handler})
	}
	
	oInput.val = function val(value) {
		if (typeof value === 'undefined'){
			return this.value
		}
		else {
			this.value = value
		}
	}

	oInput.setLabel = function setLabel(label) {
		this.label = label
	}
	oInput.setPlaceholder = function setPlaceholder(placeholder) {
		this.placeholder = placeholder
	}
	
	oInput.toDOM = function toDOM(input) {
		$defaultInputDOM = $('<div class="form-group"><div id="inputType"><div id="input" class="input-group"><input type="text" class="form-control" id="value" /></div></div></div>')
		
		if (typeof input.label !== 'undefined') {
			$defaultInputDOM.find("#inputType").prepend($('<div id="label" class="col-lg-2 control-label" for="input_value"></div>').text(input.label))
			$defaultInputDOM.find('#input').addClass("col-lg-10").find("#value").val(input.value).attr("id",input.id).attr("placeholder",this.placeholder)
		}
		else {
			$defaultInputDOM.find('#input').addClass("col-lg-12").find("#value").val(input.value).attr("id",input.id).attr("placeholder",this.placeholder)
		}
		return $defaultInputDOM
	}
	return oInput
}

_vis.menu.element.select = function select(id){
	oSelect = {
		type: "select",
		id: id,
		input: true,
		value: null,
		options: []
	}

	oSelect.val = function val(value) {
		if (typeof value === 'undefined'){
			return this.value
		}
		else {
			this.value = value
		}
	}

	oSelect.setLabel = function setLabel(label) {
		this.label = label
	}
	
	oSelect.addOption = function addOption(value, label) {
		option = {
			value: value,
			label: label
		}
		
		option.toDOM = function toDOM(option, isSelected) {
			$defaultOptionDOM = $('<option></option>')
			$defaultOptionDOM.attr("value",option.value)
			$defaultOptionDOM.text(option.label)
			if (isSelected) $defaultOptionDOM.attr('selected', true)
			return $defaultOptionDOM
		}
		oSelect.options.push(option)
		return option;
	}

	oSelect.toDOM = function toDOM(input) {
		$defaultInputDOM = $('<div class="form-group"><div id="inputType"><div id="label" class="col-lg-2 control-label" for="input_value"></div><div id="input" class="col-lg-10 input-group"><select id="value" class="form-control"></select></div></div></div>')
		$defaultInputDOM.find("#label").text(input.label)
		
		$defaultInputDOM.find("#value").attr("id",input.id)
		$.each(input.options, function(index, option){
			//selected = 
			$defaultInputDOM.find("select").append(option.toDOM(option, option.value === input.value ? true : false))
		})
		return $defaultInputDOM
	}
	
	return oSelect

}
_vis.menu.element.textarea = function textarea(id){
	oTextarea = {
		type: "textarea",
		id: id,
		input: true,
		value: null
	}

	oTextarea.val = function val(value) {
		if (typeof value === 'undefined'){
			return this.value
		}
		else {
			this.value = value
		}
	}

	oTextarea.setLabel = function setLabel(label) {
		this.label = label
	}
	oTextarea.setPlaceholder = function setPlaceholder(placeholder) {
		this.placeholder = placeholder
	}
	oTextarea.toDOM = function toDOM(input) {
		$defaultInputDOM = $('<div class="form-group"><div id="inputType"><div id="label" class="col-lg-2 control-label" for="input_value"></div><div id="input" class="col-lg-10 input-group"><textarea type="text" class="form-control" id="value"></textarea></div></div></div>')
		$defaultInputDOM.find("#label").text(input.label)
		$defaultInputDOM.find("#value").attr("id",input.id).val(input.value)
		return $defaultInputDOM
	}

	return oTextarea
}

_vis.menu.element.panel= function addPanel(id, heading) {
	panel = {
		id: id,
		heading: heading,
		groups: [],
		input : false,
		type : "panel"
	}

	panel.toDOM = function toDOM(panel) {
		$defaultPanelDOM = $('<div class="panel form-horizontal"></div>')
		$panelHeadingDOM = $('<div class="panel-heading">'+this.heading+'</div>')
		$panelHeadingDOM.on('click',function() {
			$(this).parent().find(".group").toggle();
		})

		$defaultPanelDOM.append($panelHeadingDOM).attr("id",this.id)
		
		$.each(panel.groups, function(index, group) {
			$defaultPanelDOM.append(group.toDOM(group))
		})
		
		return $defaultPanelDOM
	}
	
	panel.addGroup = function addGroup(id) {
		group = _vis.menu.element.group(id)
		this.groups.push(group)
		return group
	}
	return panel
}

_vis.menu.element.slider = function slider(id, min, max, step) {
	if (typeof step === 'undefined') {
		step = (max-min)/100;
	}

	oSlider =  {
		id : id,
		input : true,
		type : "slider",
		value: null,
		label: null,
		min: min,
		max: max,
		step: step
	}

	oSlider.setLabel = function setLabel(label) {
		this.label = label
	}

	oSlider.toDOM = function toDOM(slider) {
		$sliderDOM = $('<div class="form-group"><div id="inputType"><div id="input" class="col-lg-10 input-group slider"></div></div></div>')
		$labelDOM = $('<div id="label" class="col-lg-2 control-label" for="input_value"></div>').text(slider.label)
		if(slider.label !== null) {
			$sliderDOM.prepend($labelDOM)
		}
		$inputDOM = $('<input type="text" data-slider-min="'+slider.min+'" data-slider-max="'+slider.max+'" data-slider-step="'+slider.step+'" data-slider-value="'+slider.value+'"/>').attr("id",slider.id)

		$sliderDOM.find("#input").append($inputDOM)
		return $sliderDOM
	}

	return oSlider;
}

_vis.menu.element.colorpicker = function colorpicker(id) {
	oColorpicker =  {
		id : id,
		input : true,
		type : "colorpicker",
		value: null,
		label: null
	}

	oColorpicker.setLabel = function setLabel(label) {
		this.label = label
	}

	oColorpicker.toDOM = function toDOM(colorpicker) {
		$colorpickerDOM = $('<div class="form-group"><div id="inputType"><div id="input" class="col-lg-10 input-group"></div></div></div>')
		$labelDOM = $('<div id="label" class="col-lg-2 control-label" for="input_value"></div>').text(colorpicker.label)
		if(colorpicker.label !== null) {
			$colorpickerDOM.prepend($labelDOM)
		}
		$inputDOM = $('<div id="'+colorpicker.id+'" class="circle"></div>').css("background-color",colorpicker.value)
		
		$colorpickerDOM.find("#input").append($inputDOM)

		$defaultInputDOM.find("#value").attr("id",input.id).val(input.value)

		return $colorpickerDOM
	}

	return oColorpicker;
}

_vis.menu.element.group = function addGroup(id) {
	group = {
		id: id,
		label: null,
		elements: [],
		input : false,
		type : "group"
	}
	
	group.setLabel = function setLabel(label) {
		this.label = label
	}
	
	group.toDOM = function toDOM(group) {
		$defaultInputGroupDOM = $('<div class="group"></div>').attr("id",group.id)
		
		if (typeof group.label !== 'undefined' && group.label !== null && group.label.length > 0) {
			$defaultInputGroupLabelDOM = $('<label></label>')
			$defaultInputGroupDOM.append($defaultInputGroupLabelDOM.text(group.label))
		}
		$.each(group.elements, function (index, element) {
			$defaultInputGroupDOM.append(element.toDOM(element))
		})
		
		return $defaultInputGroupDOM
	}
	
	group.addInput = function addInput(id) {
		input = _vis.menu.element.input(id)
		this.elements.push(input)
		return input
	}

	group.addSelect = function addSelect(id) {
		select = _vis.menu.element.select(id)
		this.elements.push(select)
		return select
	}

	group.addTextarea = function addTextarea(id) {
		textarea = _vis.menu.element.textarea(id)
		this.elements.push(textarea)
		return textarea
	}

	group.addTable = function addTable(id) {
		table = _vis.menu.element.table(id)
		this.elements.push(table)
		return table
	}

	group.addColorpicker = function addColorpicker(id) {
		colorpicker = _vis.menu.element.colorpicker(id)
		this.elements.push(colorpicker)
		return colorpicker
	}

	group.addSlider = function addSlider(id, begin, end, step) {
		slider = _vis.menu.element.slider(id, begin, end, step)
		this.elements.push(slider)
		return slider
	}

	return group
}

_vis.menu.element.table = function table(id) {
	oTable = {
		id: id,
		rows: [],
		headers: [],
		input: false,
		type : "table"
	}
	oTable.setHeaders = function(header1, header2) {this.headers = [header1,header2]}
	oTable.addRow = function(col1, col2) {
		row = {
			col1: col1,
			col2: col2
		}
		
		row.toDOM = function toDOM(index, row) {
			$defaultRowDOM = $('<tr><td id="1" class="table-label"><span></span></td><td id="2"></td></tr>')
			$defaultRowDOM.attr("id",index)
			$defaultRowDOM.find("td#1 span").text(row.col1)
			if(row.col2.constructor === String) {
				$defaultRowDOM.find("td#2").text(row.col2)
			}
			else if (row.col2.constructor === Object && row.col2.input === true) {
				$defaultRowDOM.find("td#2").append(row.col2.toDOM(row.col2))
			}
			return $defaultRowDOM
		}
		
		this.rows.push(row)
		return row
	}
	
	oTable.toDOM = function toDOM(input) {
		$defaultTableInputDOM = $('<table id="table" class="table form-horizontal"><thead><tr></tr></thead><tbody></tbody></table>').attr("id",input.id)
		if (input.headers.length > 0) {
			$defaultTableInputDOM.find('thead tr').append('<th class="col-lg-2" style="text-align:right">' + input.headers[0] + '</th>')
			$defaultTableInputDOM.find('thead tr').append('<th class="col-lg-10">' + input.headers[1] + '</th>')
		}
		$.each(input.rows, function(index, row){
			$defaultTableInputDOM.find('tbody').append(row.toDOM(index, row))
		})
		return $defaultTableInputDOM
	}
	return oTable
}

_vis.template.load = function load(body) {
	_vis.datasets.data.data = body.data
	_vis.visualisation.parameters = body.parameters
	_vis.template.definition = body.javascript
	_vis.datasets.data.update()
	_vis.menu.initialise()
	//evaluate the users javascript
	try {
		eval(_vis.template.definition)
	}
	catch(e) {
		alert('Parsing the javascript definition throws error(s)!')
		console.log(e)
	}
	_vis.visualisation.parse()
	_vis.menu.height = body.height;
	$('.menu-content').css("height",body.height)
	$('visualisation').css("height",body.height)
}

_vis.menu.initialise = function initialise() {
	//clear the menu
	_vis.menu.panels = []
	// Define the necessary inputs (name and description)
	panel_configuration = _vis.menu.addPanel("configuration","Settings")
	group_onfiguration = panel_configuration.addGroup("visualisation")
	input_name = group_onfiguration.addInput("name")
	input_name.setLabel("Name")
	textarea_description = group_onfiguration.addTextarea("description")
	textarea_description.setLabel("Desc")
}

_vis.visualisation.load = function load(body) {
	_vis.datasets.data.data = body.data
	_vis.visualisation.parameters = body.parameters
	_vis.datasets.data.update()
	_vis.visualisation.parse()
	$('.menu-content').css("height",body.height)
	$('visualisation').css("height",body.height)
}

_vis.visualisation.parse = function parse() {
	_vis.menu.draw()
	_vis.visualisation.draw()
}

_vis.definition.get = function get(body) {
	returnMessage = {
		body : {
			parameters : _vis.visualisation.parameters,
			DOM : _vis.visualisation.DOM.html(),
			draw : String(_vis.visualisation.definition)
		}
	}

	return returnMessage
}

_vis.menu.draw = function draw() {
	$.each(_vis.menu.panels, function(index_panel, panel) {
		if(typeof _vis.visualisation.parameters[_vis.common.escape(panel.id)] === 'undefined') _vis.visualisation.parameters[_vis.common.escape(panel.id)] = {}
		$.each(panel.groups, function (index_group, group) {
			if(typeof _vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)] === 'undefined') _vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)] = {}
			$.each(group.elements, function(index_element, element) {
				if (element.input === true) {
					if (typeof _vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)] !== 'undefined') {
						element.value = _vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)]

						if (element.type === 'select') {
							optionExists = false;

							$.each(element.options, function (index, option) {
								if (element.value === option.value) {
									optionExists = true
								}
							})
							if (optionExists === false) {
								if(element.options.length > 0) {
									element.value = element.options[0].value
									_vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)] = element.options[0].value
								} else {
									element.value = null;
									_vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)] = null
								}
							}
						}
					}
				}
				if (element.type === 'table') {
					if(typeof _vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)] === 'undefined')  _vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)] = {}
					$.each(element.rows, function (index_row, row) {
						if (row.col2.input === true) {
							if (typeof _vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)][_vis.common.escape(row.col2.id)] !== 'undefined') {
								row.col2.value = _vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)][_vis.common.escape(row.col2.id)]
							}
						}
					})
				}
			})
		})
	})
	$('#menu').empty().append(_vis.menu.toDOM())
	$.each(_vis.menu.panels, function(index_panel, panel) {
		$.each(panel.groups, function (index_group, group) {
			$.each(group.elements, function(index_element, element) {
				if (element.input === true) {
					identifier = '#menu #' + _vis.common.escape(panel.id) + ' #' + _vis.common.escape(group.id) + ' #input #' + _vis.common.escape(element.id)
					if (element.type === "colorpicker") {
						$(identifier + ".circle").ColorPicker({
							color: _vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)],
							onShow: function (colpkr) {
								$(colpkr).fadeIn(500);
								return false;
							},
							onHide: function (colpkr) {
								$(colpkr).fadeOut(500);
								_vis.visualisation.parse();
								_vis.common.settings.update('objectHasChanges',true)
								return false;
							},
							onChange: function (hsb, hex, rgb) {
								$(this).css('backgroundColor', '#' + hex);
								_vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)] = '#' + hex;
								
							}
						});
					} else if (element.type === "slider") {
						$(identifier).slider().on('slideStop', function(ev){
						     _vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)] = $(this).data('slider').getValue();
						     _vis.visualisation.parse()
							_vis.common.settings.update('objectHasChanges',true)
						});
					} else {
						$(identifier).change(function() {
							val = $(this).val()
							_vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)] = val
							element.value = val
							_vis.menu.initialise()
							//evaluate the users javascript
							try {
								eval(_vis.template.definition)
							}
							catch(e) {
								console.log(e)
								alert('Parsing the javascript definition throws error(s)!')
							}
							_vis.visualisation.parse()
							_vis.common.settings.update('objectHasChanges',true)
						})
					}
				}
				if (element.type === 'table') {
					$.each(element.rows, function (index_row, row) {
						if (row.col2.input === true) {
							identifier = '#menu #' + _vis.common.escape(panel.id) + ' #' + _vis.common.escape(group.id) + ' #' + _vis.common.escape(element.id) + ' #' + _vis.common.escape(row.col2.id)

							if (row.col2.type === "colorpicker") {
								if (typeof _vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)][_vis.common.escape(row.col2.id)] !== 'undefined') {
									color = _vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)][_vis.common.escape(row.col2.id)].substring(1,7)
								} else {
									color = "cccccc"
								}

								$(identifier + ".circle").ColorPicker({
									color: color,
									onShow: function (colpkr) {
										$(colpkr).fadeIn(500);
										return false;
									},
									onHide: function (colpkr) {
										$(colpkr).fadeOut(500);
										_vis.visualisation.parse();
										_vis.common.settings.update('objectHasChanges',true)
										return false;
									},
									onChange: function (hsb, hex, rgb) {
										$(this).css('backgroundColor', '#' + hex);
										_vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)][_vis.common.escape(row.col2.id)] = '#' + hex;
									}
								});
							} else if (element.type === "slider") {
								$(identifier).slider().on('slideStop', function(ev){
								     _vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)][_vis.common.escape(row.col2.id)] = $(this).data('slider').getValue();
								     _vis.visualisation.parse()
							     	_vis.common.settings.update('objectHasChanges',true)
								  });
							}  else {
								$(identifier).on('change', function() {
									val = $(this).val()
									row.col2.value = val
									_vis.visualisation.parameters[_vis.common.escape(panel.id)][_vis.common.escape(group.id)][_vis.common.escape(element.id)][_vis.common.escape(row.col2.id)] = val
									_vis.menu.initialise()
									//evaluate the users javascript
									try {
										eval(_vis.template.definition)
									}
									catch(e) {
										console.log(e)
										alert('Parsing the javascript definition throws error(s)!')
									}
									_vis.visualisation.parse()
									_vis.common.settings.update('objectHasChanges',true)
								})
							}
						}
					})
				}
			})
		})
	})
}

_vis.visualisation.draw = function draw(definition) {
	if (typeof definition !== 'undefined') {
		_vis.visualisation.definition = definition
	}
	if (typeof _vis.visualisation.definition !== 'undefined') {
		try {
			_vis.visualisation.DOM = d3.select("visualisation")
			_vis.visualisation.DOM.html(null)
			_vis.visualisation.definition(_vis.visualisation.DOM, _vis.visualisation.parameters, _vis.datasets)
		} catch(e) {
			console.log(e)
			alert('The draw function throws error(s)!')
		}
	}
}

_vis.template.update = function update(body) {
	_vis.template.definition = body.javascript
	_vis.datasets.data.update()
	_vis.menu.initialise()
	
	//evaluate the users javascript
	try {
		eval(_vis.template.definition)
	}
	catch(e) {
		console.log(e)
		alert('Parsing the javascript definition throws error(s)!')
	}
	_vis.visualisation.parse()
}

_vis.visualisation.update = function update(body) {
	_vis.datasets.data.data = body.data
	_vis.datasets.data.update()
	_vis.menu.initialise()
	
	//evaluate the users javascript
	try {
		eval(_vis.template.definition)
	}
	catch(e) {
		console.log(e)
		alert('Parsing the javascript definition throws error(s)!')
	}
	_vis.visualisation.parse()
}