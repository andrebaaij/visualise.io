// Initialise the _vis object
var _vis;

_vis = {
    datasets : {
        data : {
            data : ''
        }
    },
    // menu
    menu : {
        id : "menu",
        panels : []
    },
    // visualisation
    visualisation : {
        id : 1,
        parameters : {},
        thumbnail : {
            selection : {
                x1 : 0,
                y1 : 0,
                width : 580,
                height : 290
            }
        },
        UI : {},
        "export" : {}
    },
    template : {
        id : 1,
        UI : {}
    },
    common : {
        settings : {
            objectHasChanges : false,
            thumbnailSelection : {
                x1 : 30,
                y1 : 45,
                x2 : 545,
                y2 : 320
            }
        }
    }
};

_vis.common.updateSettings = function (body) {
    $.each(body.settings, function (index, setting) {
        _vis.common.settings[setting] = body[setting];
    });
};
//Define all common functions;
_vis.common.correctIframe = function correctIframe() {
    $("iframe").height($(window).height() - 94);
};

_vis.common.escape = function escape(value) {
    /*!
    The function _vis.common.escape is under the following license:

    <license>
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
    </license>
    <source>
        https://github.com/mathiasbynens/jsesc
    </source>
    */

    /*
    var jsCache = {
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
    };
    */
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
/*
    function jsEscape(str) {
        return str.replace(/[\s\S]/g, function (character) {
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
*/
    return cssEscape(value.replace(/\r\n?/g, '\n'), true).output;
};

//Get function from string, with or without scopes (by Nicolas Gauthier)
_vis.common.getFunctionFromString = function (string) {
    var scope = window,
        scopeSplit = string.split('.');
    for (var i = 0; i < scopeSplit.length - 1; i++)
    {
        scope = scope[scopeSplit[i]];
        if (scope === undefined) return;
    }

    return scope[scopeSplit[scopeSplit.length - 1]];
};



_vis.common.prepareExport = function (body) {
    _vis.visualisation.parameters = body.parameters;
    _vis.visualisation.DOM = body.DOM;
    _vis.visualisation.thumbnail.set(body);

    var embed = '<div id="visualisation"></div>\n<script src="http://d3js.org/d3.v3.min.js" charset="utf-8"></script>\n<script>\n/*\n    The _vis object and it\'s child objects and functions are published under\n     following license unless otherwise stated within a child object or function.\n\n    <copyright>\n        Copyright visualise.io <https://visualise.io/>\n\n        Permission is hereby granted, free of charge, to any person obtaining\n        a copy of this software and associated documentation files (the\n        "Software"), to deal in the Software without restriction, including\n        without limitation the rights to use, copy, modify, merge, publish,\n        distribute, sublicense, and/or sell copies of the Software, and to\n        permit persons to whom the Software is furnished to do so, subject to\n        the following conditions:\n\n        The above copyright notice and this permission notice shall be\n        included in all copies or substantial portions of the Software.\n\n        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,\n        EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF\n        MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND\n        NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE\n        LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION\n        OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION\n        WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.\n    </copyright>\n*/\n\n_vis = {parameters : ' + JSON.stringify(body.parameters) + ',datasets : ' + JSON.stringify(_vis.datasets) + ', draw : '+body.draw+', common : { escape : '+ String(_vis.common.escape) +'}}\n    _vis.draw(d3.select("div#visualisation"), _vis.parameters, _vis.datasets);\n</script>\n';
    $('#modal_export_embed_html').val(embed).text(embed);
};

// All template related functions

// This function is called once when the template is loaded from the server, and initialises
// all the needed information.
_vis.template.load = function load(definition) {
    if (typeof definition !== 'undefined') {
        if (typeof definition.datasets !== 'undefined') {
            $('#tab_data #data').val(definition.datasets.data);
            _vis.datasets.data.data = definition.datasets.data;
        }
        else {
            _vis.datasets.data.data = $('#tab_data #data').val();
        }

        if (typeof definition.settings !== 'undefined') {
            $('#tab_data #data').val(definition.datasets.data);
            _vis.common.settings = definition.settings;
        }

        if (typeof definition.javascript !== 'undefined') {
            _vis.template.definition = definition.javascript;
        }
        else {
            _vis.template.definition = $('#editor').text();
        }
        
        if (typeof definition.parameters !== 'undefined') {
            _vis.visualisation.parameters = definition.parameters;
        }
        else {
            _vis.visualisation.parameters = {};
        }
    }
    else {
        _vis.datasets.data.data = $('#tab_data #data').val();
        _vis.template.definition = $('#editor').val();
        _vis.visualisation.parameters = {};
    }

    var message = {
        action : "load",
        resource : 'template',
        callback : '_vis.common.correctIframe',
        body : {
            data : _vis.datasets.data.data,
            parameters : _vis.visualisation.parameters,
            javascript : _vis.template.definition,
            height : $(window).height()-94
        }
    };
    socket.postMessage(JSON.stringify(message));
};

// This function get's called when the user wants to publish this template
_vis.template.publish = function publish() {
    var bSuccess = true;
    var error = null;
    $('#modal_publish').modal('hide');
    
    Pixastic.process(document.getElementById("thumbnail_visualisation_publish"), "crop", {
        rect : {
            left :_vis.common.settings.thumbnailSelection.x1,
            top : _vis.common.settings.thumbnailSelection.y1,
            width : _vis.common.settings.thumbnailSelection.width,
            height : _vis.common.settings.thumbnailSelection.height
        }
    });

    _vis.visualisation.thumbnail.image = document.getElementById('thumbnail_visualisation_publish').toDataURL("image/png");

    var putData = {
        name : _vis.template.name,
        description : _vis.template.description,
        definition : {
            parameters : _vis.visualisation.parameters,
            datasets : {
                data : _vis.datasets.data.data
            },
            javascript : _vis.template.definition,
            settings : _vis.common.settings
        },
        thumbnail : _vis.visualisation.thumbnail.image,
        tags : _vis.template.tags,
        type : $("#modal_publish_input_type").val()
    };

    $.ajax({
        type: "POST",
        contentType: "text/json",
        async: false,
        url: "/templates/" + _vis.template.id + "/publish",
        data: JSON.stringify(putData),
        error: function () {
            bSuccess = false;
        }
    }).done(function ( data, textStatus, jqXHR ) {
        if (jqXHR.responseText.length > 0) {
            bSuccess = false;
            error = "session-timeout";
        }
    });

    if (bSuccess === true) {
        $('#modal_save').modal('hide');
        alert('Your template has been succesfully published.');
        _vis.common.settings.objectHasChanges = false;
    }
    else if (error === "session-timeout"){
        Pixastic.revert(document.getElementById("thumbnail_visualisation_save"));
        $('#modal_login').modal({
          show: true
        });
    } else {
        alert('An error occurred, please try again.');
    }
};

// This function get's called when the user wants to save this template
_vis.template.save = function save() {
    var bSuccess = true;
    var error = null;

    Pixastic.process(document.getElementById("thumbnail_visualisation_save"), "crop", {
        rect : {
            left :_vis.common.settings.thumbnailSelection.x1,
            top : _vis.common.settings.thumbnailSelection.y1,
            width : _vis.common.settings.thumbnailSelection.width,
            height : _vis.common.settings.thumbnailSelection.height
        }
    });

    _vis.visualisation.thumbnail.image = document.getElementById('thumbnail_visualisation_save').toDataURL("image/png");

    var putData = {
        name : _vis.template.name,
        description : _vis.template.description,
        definition : {
            parameters : _vis.visualisation.parameters,
            datasets : {
                data : _vis.datasets.data.data
            },
            javascript : _vis.template.definition,
            settings : _vis.common.settings
        },
        thumbnail : _vis.visualisation.thumbnail.image,
        tags : _vis.template.tags
    };

    $.ajax({
        type: "PUT",
        contentType: "text/json",
        async: false,
        url: "/templates/" + _vis.template.id,
        data: JSON.stringify(putData),
        error: function () {
            bSuccess = false;
        }
    }).done(function ( data, textStatus, jqXHR ) {
        if (jqXHR.responseText.length > 0) {
            bSuccess = false;
            error = "session-timeout";
        }
    });

    if (bSuccess === true) {
        $('#modal_save').modal('hide');
        alert('Your template has been succesfully saved.');
        _vis.common.settings.objectHasChanges = false;
    }
    else if (error === "session-timeout"){
        Pixastic.revert(document.getElementById("thumbnail_visualisation_save"));
        $('#modal_login').modal({
          show: true
        });
    } else {
        alert('An error occurred, please try again.');
    }
};

// Must the template be updated because the templates definition was altered? It all happens in here.
_vis.template.update = function update() {
    var message = {
        action : "update",
        resource : "template",
        body : {
            javascript : _vis.template.definition
        }
    };
    socket.postMessage(JSON.stringify(message));
    _vis.common.settings.objectHasChanges = true;
};

// Initialise the edit template UI
_vis.template.UI.initialise = function initialise() {
    // Initialise editor
    $("#editor").height($(window).height()-94);
    
    $('#menu').each(function () {
        $(this).height($(window).height()-94);
    });

    _vis.editor = ace.edit("editor");
    _vis.editor.setTheme("ace/theme/xcode");
    _vis.editor.getSession().setMode("ace/mode/javascript");
    if (typeof _vis.template.definition !== 'undefined') {
        _vis.editor.setValue(_vis.template.definition)      ; 
    }
    _vis.editor.clearSelection();
    
    _vis.editor.on('blur', function () {
        _vis.template.definition = _vis.editor.getValue();
        _vis.template.update();
    });

    // Initialise thumbnail selection
    _vis.visualisation.thumbnail.save = $('#modal_save img').imgAreaSelect({ fadeSpeed:500, instance: true, aspectRatio: '2:1', onSelectEnd: _vis.visualisation.thumbnail.preview, parent: '#modal_save .modal-content', x1: 5, y1: 5, x2: 5, y2: 5 });
    $('#modal_save canvas').hide();
    $('#modal_save').on('shown.bs.modal', function () {
        var selection = _vis.common.settings.thumbnailSelection;
        _vis.visualisation.thumbnail.save.setSelection(selection.x1,selection.y1,selection.x2,selection.y2,true);
        _vis.visualisation.thumbnail.save.setOptions({ show: true });
        _vis.visualisation.thumbnail.save.update();
    });

    _vis.visualisation.thumbnail.publish = $('#modal_publish img').imgAreaSelect({ fadeSpeed:500,  instance: true, aspectRatio: '2:1', onSelectEnd: _vis.visualisation.thumbnail.preview, parent: '#modal_publish .modal-content'}); 
    $('#modal_publish canvas').hide();
    $('#modal_publish').on('shown.bs.modal', function () {
        var selection = _vis.common.settings.thumbnailSelection;
        _vis.visualisation.thumbnail.publish.setSelection(selection.x1,selection.y1,selection.x2,selection.y2,true);
        _vis.visualisation.thumbnail.publish.setOptions({ show: true });
        _vis.visualisation.thumbnail.publish.update();
    });
    // Template name initialise + change event
    _vis.template.name = $('#tab_template #menu #template #input_name').val();
    $('#tab_template #menu #template #input_name').bind('blur', function () {
        _vis.template.name = $(this).val();
    });
    
    // Template description initialise + change event
    _vis.template.description = $('#tab_template #menu #template #input_description').val();
    $('#tab_template #menu #template #input_description').bind('blur', function () {
        _vis.template.description = $(this).val();
    });

    // Template tags initialise + change event
    _vis.template.tags = $('#modal_publish #input_tags').val();
    $('#modal_publish #input_tags').bind('blur', function () {
        _vis.template.tags = $(this).val();
    });
    
    // Data change event
    $('#tab_data #data').blur(function () {
        _vis.datasets.data.data = $(this).val();
        _vis.visualisation.update();
    });
    
    // menu - save
    $('#link_save').click(function () {
        //$('#modal_save img').imgAreaSelect({show: true, x1: _vis.common.settings.thumbnailSelection.x1, y1: _vis.common.settings.thumbnailSelection.y1, x2 : _vis.common.settings.thumbnailSelection.x2, y2 : _vis.common.settings.thumbnailSelection.y2 });
        $('#modal_save').modal({
          show: true
        });

        var message = {
            action : "get",
            resource : "definition",
            callback : "_vis.visualisation.thumbnail.set"
        };
        socket.postMessage(JSON.stringify(message));
        return false;
    });

    // modal-save save
    $('#modal_save_link_save').click(function () {
        _vis.template.save();
        return false;
    });

        // modal-save save
    $('#modal_publish_link_publish').click(function () {
        _vis.template.publish();
        return false;
    });
    
    // menu - publish
    $('#link_publish').on('click',function () {
        $('#modal_publish').modal({
          show: true
        });

        var message = {
            action : "get",
            resource : "definition",
            callback : "_vis.visualisation.thumbnail.set"
        };
        socket.postMessage(JSON.stringify(message));
        return false;
    });
    
    $('#link_export').on('click',function () {
        $('#modal_export').modal({
          show: true
        });

        var message = {
            action : "get",
            resource : "definition",
            callback : "_vis.common.prepareExport"
        };
        socket.postMessage(JSON.stringify(message));
        return false;
    });

    $('#modal_export_link_svg').on('click', function () {

        var downloadDataURI = function ($, options) {
            if(!options)
                return;
            $.isPlainObject(options) || (options = {data: options});
            options.filename || (options.filename = "download." + options.data.split(",")[0].split(";")[0].substring(5).split("/")[1]);
            options.url || (options.url = "/files/download");
            $('<form method="post" action="'+options.url+'" style="display:none"><input type="hidden" name="filename" value="'+options.filename+'"/><input type="hidden" name="data" value="'+options.data+'"/></form>').submit().remove();
        };

        downloadDataURI($, {filename: _vis.template.name + ".svg",data:"data:image/svg+xml;base64," + btoa(_vis.visualisation.DOM)});

        return false;
    });

    $('#modal_export_link_png').on('click', function () {

        var downloadDataURI = function ($, options) {
            if(!options)
                return;
            $.isPlainObject(options) || (options = {data: options});
            options.filename || (options.filename = "download." + options.data.split(",")[0].split(";")[0].substring(5).split("/")[1]);
            options.url || (options.url = "/files/download");
            $('<form method="post" action="'+options.url+'" style="display:none"><input type="hidden" name="filename" value="'+options.filename+'"/><input type="hidden" name="data" value="'+options.data+'"/></form>').submit().remove();
        };

        downloadDataURI($, {filename: _vis.template.name + ".png",data:_vis.visualisation.thumbnail.png});
    });

    // window resize
    $( window ).resize(function () {
        var message = {
            action : "resize",
            resource : 'window',
            callback : '_vis.common.correctIframe',
            body : {
                data : _vis.datasets.data.data,
                parameters : _vis.visualisation.parameters,
                javascript : _vis.template.definition,
                height : $(window).height()-94
            }
        };
        socket.postMessage(JSON.stringify(message));

        $(".editor").height($(window).height()-94);

        $('#menu').each(function () {
            $(this).height($(window).height()-94);
        });
    });
};

// All visualisation related functions
_vis.visualisation.thumbnail.preview = function preview(img, selection) {
    _vis.common.settings.thumbnailSelection = selection;
} ;

_vis.visualisation.UI.initialise = function initialise() {
    //save
    _vis.visualisation.thumbnail.save = $('#modal_save img').imgAreaSelect({ fadeSpeed:500, instance: true, aspectRatio: '2:1', onSelectEnd: _vis.visualisation.thumbnail.preview, parent: '#modal_save .modal-content', x1: 5, y1: 5, x2: 5, y2: 5 }); 
    $('#modal_save canvas').hide();
    $('#link_save').click(function () {
        $('#modal_save').modal({
            show: true
        });

        var message = {
                action : "get",
                resource : 'definition',
                callback : '_vis.visualisation.thumbnail.set'
            };
        socket.postMessage(JSON.stringify(message));

        return false;
    });
    $('#modal_save_link_save').click(function () {
        _vis.visualisation.save();
        return false;
    });
    $('#modal_save').on('shown.bs.modal', function () {
        var selection = _vis.common.settings.thumbnailSelection;
        _vis.visualisation.thumbnail.save.setSelection(selection.x1,selection.y1,selection.x2,selection.y2,true);
        _vis.visualisation.thumbnail.save.setOptions({ show: true });
        _vis.visualisation.thumbnail.save.update();
    });

    //export
    $('#link_export').on('click',function () {
        $('#modal_export').modal({
          show: true
        });

        var message = {
            action : "get",
            resource : "definition",
            callback : "_vis.common.prepareExport"
        };
        socket.postMessage(JSON.stringify(message));
        return false;
    });

    $('#modal_export_link_svg').on('click', function () {

        var downloadDataURI = function ($, options) {
            if(!options)
                return;
            $.isPlainObject(options) || (options = {data: options});
            options.filename || (options.filename = "download." + options.data.split(",")[0].split(";")[0].substring(5).split("/")[1]);
            options.url || (options.url = "/files/download");
            $('<form method="post" action="'+options.url+'" style="display:none"><input type="hidden" name="filename" value="'+options.filename+'"/><input type="hidden" name="data" value="'+options.data+'"/></form>').submit().remove();
        };

        downloadDataURI($, {filename: _vis.visualisation.parameters.configuration.visualisation.name + ".svg",data:"data:image/svg+xml;base64," + btoa(_vis.visualisation.DOM)});

        return false;
    });

    $('#modal_export_link_png').on('click', function () {

        var downloadDataURI = function ($, options) {
            if(!options)
                return;
            $.isPlainObject(options) || (options = {data: options});
            options.filename || (options.filename = "download." + options.data.split(",")[0].split(";")[0].substring(5).split("/")[1]);
            options.url || (options.url = "/files/download");
            $('<form method="post" action="'+options.url+'" style="display:none"><input type="hidden" name="filename" value="'+options.filename+'"/><input type="hidden" name="data" value="'+options.data+'"/></form>').submit().remove();
        };

        downloadDataURI($, {filename: _vis.visualisation.parameters.configuration.visualisation.name + ".png",data:_vis.visualisation.thumbnail.png});
    });

    $('#tab_data #data').blur(function () {
        _vis.datasets.data.data = $(this).val();
        _vis.visualisation.update();
    });

        // window resize
    $( window ).resize(function () {
        var message = {
            action : "resize",
            resource : 'window',
            callback : '_vis.common.correctIframe',
            body : {
                data : _vis.datasets.data.data,
                parameters : _vis.visualisation.parameters,
                javascript : _vis.template.definition,
                height : $(window).height()-94
            }
        };
        socket.postMessage(JSON.stringify(message));
    });
};

_vis.visualisation.update = function update() {
    var message = {
        action : "update",
        resource : "visualisation",
        body : {
            data : _vis.datasets.data.data
        }
    };
    socket.postMessage(JSON.stringify(message));
    _vis.common.settings.objectHasChanges = true;
};

_vis.visualisation.load = function load(name, description, definition) {
    if (typeof definition !== 'undefined') {
        if (typeof definition.datasets !== 'undefined' && typeof definition.datasets.data !== 'undefined') {
            $('#tab_data #data').val(definition.datasets.data);
            _vis.datasets.data.data = definition.datasets.data;;
        }
        if (typeof definition.parameters !== 'undefined') {
            _vis.visualisation.parameters = definition.parameters;
        }

        if (typeof definition.settings !== 'undefined') {
            $('#tab_data #data').val(definition.datasets.data);
            _vis.common.settings = definition.settings;
        }
    }
    
    _vis.visualisation.parameters.configuration.visualisation.name = name;
    _vis.visualisation.parameters.configuration.visualisation.description = description;
    
    var message = {
        action : "load",
        resource : 'visualisation',
        body : {
            data : _vis.datasets.data.data,
            parameters : _vis.visualisation.parameters,
            height : $(window).height()-94
        }
    };
    socket.postMessage(JSON.stringify(message));
};

/*
    // Obsolete code?
_vis.visualisation.publish = function publish() {
    $('#modal_publish').modal('hide')
    
    Pixastic.process(document.getElementById("visualisation"), "crop", {
        rect : {
            left :_vis.common.settings.thumbnailSelection.x1,
            top : _vis.common.settings.thumbnailSelection.y1,
            width : _vis.common.settings.thumbnailSelection.width,
            height : _vis.common.settings.thumbnailSelection.height
        }
    });
    
    _vis.visualisation.thumbnail.image = document.getElementById('thumbnail_visualisation_publish').toDataURL("image/png")
    
    definition = {}
    definition.parameters = _vis.visualisation.parameters
    definition.datasets = {
        data : _vis.datasets.data.data
    }
    definition.settings = _vis.common.settings

    putData = {}
    putData.name =_vis.template.name
    putData.description = _vis.template.description
    putData.definition = definition
    putData.thumbnail = _vis.visualisation.thumbnail.image


    $.ajax({
        type: "PUT",
        contentType: "text/json",
        async: false,
        url: "/templates/" + _vis.visualisation.id + "/publish",
        data: JSON.stringify(putData),
        error: function (data) {
            bSuccess = false
        }.done(function ( data, textStatus, jqXHR ) {
        if (jqXHR.responseText.length > 0) {
            bSuccess = false
            error = "session-timeout"
        }
    })

    if (bSuccess === true) {
        alert('Your visualisation has been succesfully published.');
        _vis.common.settings.objectHasChanges = false;
        $('#modal_save').modal('hide')
    }
    else if (error === "session-timeout"){
        Pixastic.revert(document.getElementById("thumbnail_visualisation_save"));
        $('#modal_login').modal({
          show: true
        })
    } else {
        alert('An error occurred, please try again.');
    }
}
*/
_vis.visualisation.thumbnail.set = function getThumbnail(body) {
    _vis.visualisation.parameters = body.parameters;
    _vis.visualisation.DOM = body.DOM;

    Pixastic.revert(document.getElementById("thumbnail_visualisation_save"));
    canvg(document.getElementById('canvas'), svgfix(_vis.visualisation.DOM), 
        {
            renderCallback : function () {
                $("img.thumbnail").each(
                    function () {
                        $(this).attr(
                            'src',document.getElementById('canvas').toDataURL("image/png")
                        );
                    }
                )
            }
        }
    )

    var pngBase64 = document.getElementById('canvas').toDataURL("image/png");
    _vis.visualisation.thumbnail.png = pngBase64;
};

_vis.visualisation.save = function save() {
    var bSuccess = true;
    var error = null;
    
    Pixastic.process(document.getElementById("thumbnail_visualisation_save"), "crop", {
        rect : {
            left :_vis.common.settings.thumbnailSelection.x1,
            top : _vis.common.settings.thumbnailSelection.y1,
            width : _vis.common.settings.thumbnailSelection.width,
            height : _vis.common.settings.thumbnailSelection.height
        }
    });
    
    _vis.visualisation.thumbnail.image = document.getElementById('thumbnail_visualisation_save').toDataURL("image/png");

    var putData = {
        name : _vis.template.name,
        description : _vis.template.description,
        definition : {
            parameters : _vis.visualisation.parameters,
            datasets : {
                data : _vis.datasets.data.data
            },
            javascript : _vis.template.definition,
            settings : _vis.common.settings
        },
        thumbnail : _vis.visualisation.thumbnail.image,
        tags : _vis.template.tags
    };

    $.ajax({
        type: "PUT",
        contentType: "text/json",
        async: false,
        url: "/visualisations/" + _vis.visualisation.id,
        data: JSON.stringify(putData),
        error: function () {
            bSuccess = false;
        }
    }).done(function ( data, textStatus, jqXHR ) {
        if (jqXHR.responseText.length > 0) {
            bSuccess = false;
            error = "session-timeout";
        }
    });

    if (bSuccess === true) {
        $('#modal_save').modal('hide');
        alert('Your visualisation has been succesfully saved.');
        _vis.common.settings.objectHasChanges = false;
    }
    else if (error === "session-timeout"){
        Pixastic.revert(document.getElementById("thumbnail_visualisation_save"));
        $('#modal_login').modal({
          show: true
        });
    } else {
        alert('An error occurred, please try again.');
    }
};
