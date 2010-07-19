
(function($){
    function getDataList(){
        dataList = {};      // name:{get:func(), set:func(d)], name.0:[get:func(i), set:func(i, d)}
        dataList.get = function(name){
            var m, i;
            if(m=name.match(/\.(\d+)/)){
                i=parseInt(m[1]);
                name = name.replace(/\.\d+/, ".0");
                return dataList[name].get(i);
            }else{
                return dataList[name].get();
            }
        };
        dataList.set = function(name, value){
            var m, i;
            try{
                if(m=name.match(/\.(\d+)/)){
                    i=parseInt(m[1]);
                    name = name.replace(/\.\d+/, ".0");
                    return dataList[name].set(i, value);
                }else{
                    return dataList[name].set(value);
                }
            }catch(e){
                
            }
        };
        function add(name, eName, eId){
            if(!eId) eId=name;
            var e=$(eName+"[id='"+eId+"']");
            dataList[name] = {
                get:function(){return e.val()},
                set:function(d){e.val(d);}
            };
        }
        function addi(name, eName, createNewElemFunc){
            // index placeholder = '.0'
            var eId=name;
            dataList[name] = {
                get:function(i){
                    return $(eName.replace(".0", "."+i) +
                        "[id='"+eId.replace(".0", "."+i)+"']").val();
                },
                set:function(i, d){
                    var e=$(eName.replace(".0", "."+i) +
                        "[id='"+eId.replace(".0", "."+i)+"']");
                    if(e.size()==0){
                        if(d=="") return;   // do not add a new empty element
                        createNewElemFunc();
                        e=$(eName.replace(".0", "."+i) +
                            "[id='"+eId.replace(".0", "."+i)+"']");
                    }
                    e.val(d);
                },
                isIndex:true
            };
        }
        add("dc:title", "input");
        add("title", "input", "dc:title");
        add("dc:date", "input");
        // Creators
        addi("dc:creator.0.foaf:Person.foaf:givenName", "input",
                function(){$("*[id='dc:creator.'] .add-tr-item").click();} );
        addi("dc:creator.0.foaf:Person.foaf:familyName", "input",
                function(){$("*[id='dc:creator.'] .add-tr-item").click();} );
        add("dc:abstract", "textarea");
        add("description", "textarea", "dc:abstract");
        // -- More Details --
        // -- Reporting --
        // Field of research codes
        // <input name="dc:subject.anzsrc:for.0.rdf:resouce" value="http://purl.org/anzsrc/for#0403" type="hidden">
        // <input name="dc:subject.anzsrc:for.0.skos:prefLabel" value="0403 - GEOLOGY" type="hidden">
        addi("dc:subject.anzsrc:for.0.rdf:resouce", "input")
        addi("dc:subject.anzsrc:for.0.skos:prefLabel", "input")
        function addAnzsrcForX(i, id, label){
            // wait until we have all the info.
            if(!addAnzsrcForX.ids)addAnzsrcForX.ids=[];
            if(!addAnzsrcForX.labels)addAnzsrcForX.labels=[];
            var ids=addAnzsrcForX.ids, labels=addAnzsrcForX.labels;
            if(id)ids[i]=id;
            if(label)labels[i]=label;
            if(ids[i] && labels[i]) addAnzsrcFor(ids[i], labels[i]);
        }
        dataList["dc:subject.anzsrc:for.0.rdf:resouce"].set = function(i, d){
            addAnzsrcForX(i, d);
        };
        dataList["dc:subject.anzsrc:for.0.skos:prefLabel"].set = function(i, d){
            addAnzsrcForX(i, null, d);
        };
        // Keywords
        addi("dc:subject.usq:folksonomy.0", "input",
                function(){$("*[id='dc:subject.usq:folksonomy.'] .add-tr-item").click();});
        dataList.getNameValues = function(){
            var d= {}, name, value;
            for(var k in dataList){
                if(typeof(dataList[k])!="function"){
                    if(dataList[k].isIndex){
                        for(var i=1; i<1000; i++){
                           name = k.replace(".0", "."+i);
                           value = dataList[k].get(i);
                           if(!value && value!="") break;
                           d[name]=$.trim(value);
                        }
                    }else{
                        d[k]=$.trim(dataList.get(k));
                    }
                }
            }
            return d;
        };
        return dataList;
    }
    _GetDataList = getDataList;

    function savePackage(){
        //messageBox("saved OK");
        var url = packageData.portalPath + "/actions/manifest.ajax";
        var func = "update-package-meta";
        var oid = packageData.oid;
        var metaList = [];
        var data = getDataList().getNameValues();
        for(var k in data){
            metaList.push(k);
        }
        data.func=func; data.oid=oid; data.metaList=metaList;
        $.ajax({ type:"POST", url:url, data:data,
            success:function(data){
                if(data.error || !data.ok){
                    messageBox("Failed to save! (error='"+data.error+"')");
                }else{
                    $("#saved-result").text("Saved OK").
                        css("color", "green").show().fadeOut(4000);
                }
            },
            error:function(xhr, status, e){
                messageBox("Failed to save! (status='"+status+"')");
            },
            dataType:"json"
        });
    }

    function updateForm(data){
        var key, value, setData;
        setData = getDataList();
        for(key in data){
            value = data[key];
            setData.set(key, value);
        }
    }

    function addFile(){
        var addAFile = $("#add-a-file");
        var file = addAFile.find("input[type=file]");  // "z:Attachment.0.dc:title"
        var metaData = {};
        var i, form = addAFile.find("form"), input, numOfFiles=0;
        var mdList, metaDataList = [
                        "z:Attachment.0.dc:title",
                        "z:Attachment.0.z:type",
                        "z:Attachment.0.dc:date",
                        "z:Attachment.0.dc:description",
                        ];
        if(!file.val()){
            messageBox("Please select a file to upload first!");
            return;
        }
        for(i=1; i<1000; i++){
            var givenName = "z:Attachment.0.dc:creator."+i+".foaf:Person.foaf:givenName";
            var familyName = "z:Attachment.0.dc:creator."+i+".foaf:Person.foaf:familyName";
            if(addAFile.find("*[id='"+givenName+"']").size()){
                metaDataList.push(givenName);
                metaDataList.push(familyName);
            }else{
                break;
            }
        }
        $.each(metaDataList, function(i, key){
            metaData[key] = addAFile.find("#[id='"+key+"']").val() || "";
        });
        metaData["title"] = file.val();
        metaData["description"] = metaData["z:Attachment.0.dc:description"];

        form.find("input[type=hidden]").remove();
        mdList = [];
        for(var key in metaData){
            var nkey = key.replace(/\.0/, "." + (numOfFiles+1));
            mdList.push(nkey);
            input = $("<input type='hidden'/>");
            input.attr("name", nkey);
            input.attr("value", metaData[key]);
            form.append(input);
        }
        form.append("<input type='hidden' name='ajax' value='1' />");
        form.attr("action", "/portal/default/workflow.ajax");
        form.append("<input type='hidden' name='upload-file-workflow' value='workflow1' />");
        input = $("<input type='hidden' name='metaDataList' />");
        input.attr("value", mdList.join(","));
        form.append(input);
        form.attr("target", "uploadframe");

        function disableAllFields(tf){
            addAFile.find("input, select, textarea").attr("disabled", tf);
        }
        function resetAllFields(){
            disableAllFields(false);
            file.val("");
            $("#add-a-file .upload-info").html("");
            $.each(metaDataList, function(i, key){
                addAFile.find("#[id='"+key+"']").val("").attr("disabled", false);
            });
        }
        var iframe=$("#uploadframe");
        function getIBody(){
            var ibody;
            if(iframe[0].contentDocument) ibody=iframe[0].contentDocument.body;
            else ibody = iframe[0].contentWindow.document.body;
            return $(ibody);
        }
        form.submit();
        try{
            disableAllFields(true);
            $("#upload-file-loading").show();
            $("#uploading-file-msg").text("Uploading please wait.").
                css("color", "green").show();
            getIBody().text("Uploading please wait...");
        }catch(e){

        }
        try{
           iframe.unbind().load(function() {
                $("#upload-file-loading").hide();
                // add to list of already added files
                var json;
                try{
                    eval("json=" + getIBody().text());
                    resetAllFields();
                    alert(json.toSource());
                    $.getJSON("../actions/manifest",
                        {"itemId":json.oid, "title":metaData["title"],
                            "func":"add", "oid":packageData.oid},
                        function(data){
                            alert(data.toSource());
                        }
                    );
                    $("#uploading-file-msg").text("Uploaded OK");
                }catch(e){
                    $("#uploading-file-msg").css("color", "red").
                        text("Failed to uploading file (ERROR)");
                    disableAllFields(false);
                }
                setTimeout(function(){ $("#uploading-file-msg").fadeOut(2000); }, 3000);
           });
        }catch(e){
            alert("Error: "+e);
        }
    }

    function changeToTabLayout(elem){
        var h, li, ul = $("<ul></ul>");
        elem.children("h3").each(function(c, e){
            h = $(e);
            li = $("<li><a href='#" + h.next().attr("id") + "'><span>" + h.text() + "</span></a></li>");
            ul.append(li);
            h.remove();
        });
        elem.prepend(ul);
        return elem;
    }

    function setupFileUploader(){
        var ifile = $("#add-a-file input[type='file']");
        ifile.change(function(e){displayUploadFileInfo(e.target.files[0]);});
        $("#add-a-file").bind("dragover", function(ev){
            if(ev.target.tagName=="INPUT"){ return true; }
            ev.stopPropagation();ev.preventDefault();
        });
        function handleFileDrop(ev){
            if(ev.target.tagName=="INPUT"){ return true; }
            ev.stopPropagation(); ev.preventDefault();
            displayUploadFileInfo(ev.dataTransfer.files[0]);
            $("#add-a-file input[type=file]").val("");      // reset
        }
        //$("#add-a-file").bind("drop", handleFileDrop);  // Note: binding to the wrong 'drop' event!
        $("#add-a-file")[0].addEventListener("drop", handleFileDrop, false);
    }

    function displayUploadFileInfo(file){
        gFile = file;
        var kSize = parseInt(file.size/1024+0.5);
        if(file.type.search("image/")==0){
            try{
                $("#add-a-file .upload-info").html(
                    "<span>Image: " + file.name + " (" + kSize + "k) " +
                    " <img class='thumbnail' style='vertical-align:middle;' src='" +
                    file.getAsDataURL() +
                    "' title='" + file.name + "'/></span>");
            }catch(e){
                $("#add-a-file .upload-info").html(
                    "<span>Image: " + file.name + " (" + kSize + "k)</span>");
            }
            $("#add-a-file select").val("photo");
        }else if(file.type.match("video|flash")){
            $("#add-a-file .upload-info").html(
                "<span>Video: " + file.name + " (" + kSize + "k)</span>");
            $("#add-a-file select").val("video");
        }else if(file.type.match("text|pdf|doc|soffice|rdf|txt|opendocument")){
            $("#add-a-file .upload-info").html(
                "<span>Document: " + file.name + " (" + kSize + "k)</span>");
            $("#add-a-file select").val("document");
        }else{
            $("#add-a-file .upload-info").html(
                "<span>File: " + file.name + " (" + kSize + "k)</span>");
        }
    }

    function setupMessageDialog(){
        $("#message-dialog-ok").click(function(){
            $("#message-dialog").dialog("close");
        });
        $("#message-dialog").dialog({title:"Message", hide:"blind", 
                modal:true, autoOpen:false });
    }
    function messageBox(msg){
        $("#message-dialog").dialog("open").find("span:first").
            text(msg);
    }

    var addAnzsrcFor;
    function setupJsonMultiSelects(){
        $(".multi-section-json").each(function(c, d){
            var jsonPath, jsonFile, parent, createMultiSelect, addSelection;
            var div, idPrefix, namespace, idPostfix="", labelPostfix="";
            d = $(d);
            div = d.find(":first");
            idPrefix = d.attr("id");
            parent = d.children("span:first");
            jsonPath = packageData.portalPath + "/" +
                        d.find("input[type=hidden][name=__json-path]").val();
            jsonFile = d.find("input[type=hidden][name=__json-file]").val();
            idPostfix = d.find("input[type=hidden][name=__idPostfix]").val();
            labelPostfix = d.find("input[type=hidden][name=__labelPostfix]").val();
            addSelection = function(id, label){
                if(!id.match(/#/)){
                    id=namespace + id;
                }
                var count = div.find("div").size() + 1;
                if($("input[value='" + namespace + id + "']").size()){
                    messageBox("This item has already been added!");
                    return;
                }
                var d = $("<div/>");
                var del = $("<input type='button' value='delete'/>");
                d.attr("id", idPrefix + count);
                d.append(label);
                d.append(del);
                d.append("<input type='hidden' " +
                        "name='" + idPrefix + count + idPostfix + "' " +
                        "id='" + idPrefix + count + idPostfix + "' " +
                        "value='" + id + "' />");
                d.append("<input type='hidden' " +
                        "name='" + idPrefix + count + labelPostfix + "' " +
                        "id='" + idPrefix + count + labelPostfix + "' " +
                        "value='" + label + "' />");
                div.append(d);
                del.click(function(){
                    d.remove();
                    // renumber
                    div.find("div").each(function(c, d){
                        d = $(d);
                        d.attr("id", d.attr("id").replace(/\.\d+/, "."+(c+1)) );
                        d.find("input").each(function(_, h){
                            h=$(h);
                            var idName = h.attr("id").replace(/\.\d+/, "."+(c+1));
                            h.attr("name", idName);
                            h.attr("id", idName);
                        });
                    });
                });
            };
            addAnzsrcFor = addSelection;
            createMultiSelect = function(parent, jsonFile){
                $.getJSON(jsonPath+jsonFile, function(json){
                    if(!namespace) {namespace=json.namespace;}
                    json.id = jsonFile;
                    multiSelectJson(parent, json, createMultiSelect, addSelection);
                });
            }
            createMultiSelect(parent, jsonFile);
        });
    }
    function multiSelectJson(parent, json, createMultiSelect, addSelection){
        var select, option, children={};
        select = $("<select id='"+ json.id +"'/>");
        option=$("<option value=''>Please select one</option>");
        select.append(option);
        $.each(json.list, function(c, i){
            option=$("<option/>");
            option.val(i.id);
            option.text(i.label);
            select.append(option);
            if(i.children) children[i.id]=i.id;
        });
        parent.append(select);
        select.change(function(){
            var val = select.val();
            var but = parent.nextAll("input");
            but.hide();
            select.nextAll("select").remove();
            if(children[val]){
                createMultiSelect(parent, val+".json")
            }
            if(json.selectable){
                but.val("Add '" + val + "'");
                but.show();
                but.unbind();
                but.click(function(){
                    addSelection(val, select.find("option[value="+val+"]").text());
                });
            }
        });
    }


    //=====================
    $(function(){
        function onContentLoaded(){
            var mainTab = changeToTabLayout($(".inputscreens")).tabs();
            // or
            //$(".inputscreens").accordion().find("div.tab-nav").hide();
            function tableUpdate(tbody){
                var rows = tbody.find("tr.sort-item");
                rows.each(function(c, r){
                    r = $(r);
                    r.find("td:eq(1)").text(c+1);
                    r.find("td input").each(function(c2, i){
                        i = $(i);
                        var id = i.attr("id");
                        id = id.replace(/\.[1-9]\d*/g, "."+(c+1));
                        i.attr("id", id);
                    });
                });
                rows.find("td.delete-row:first").show();
                if(rows.size()<2) rows.find("td.delete-row:first").hide();
            }
            $(".row-sortable tbody").sortable({
                    items:".sort-item",
                    update: function(e, ui){
                        //var tbody = $(".row-sortable tbody");
                        var tbody = $(e.target);
                        tableUpdate(tbody);
                    }
                }).each(function(c, tbody){tableUpdate($(tbody));});
            $(".add-tr-item").click(function(ev){
                var tbody = $(ev.target).parents("tbody");
                var tmp = tbody.find("tr.tr-item:last");
                var c = tmp.clone(true);
                c.find("input").val("");
                tmp.after(c);
                tableUpdate(tbody);
            });
            $("td.delete-row").click(function(ev){
                var tr = $(ev.target).parents("tr");
                var tbody = tr.parents("tbody");
                tr.remove();
                tableUpdate(tbody);
                return false;
            });
            $(".prev-tab").click(function(){
                var sel = mainTab.tabs("option", "selected");
                mainTab.tabs("option", "selected", sel-1);
            });
            $(".next-tab").click(function(){
                var sel = mainTab.tabs("option", "selected");
                mainTab.tabs("option", "selected", sel+1);
            });
            $("input.date").datepicker({dateFormat:"yy-mm-dd"});
            try{
                var step="setupMessageDialog";
                setupMessageDialog();
                step = "setupFileUploader";
                setupFileUploader();
                step = "setupJsonMultiSelects";
                setupJsonMultiSelects();
                step = "updateForm";
                updateForm(packageData.metaData);
            }catch(e){
                alert("Error: (step "+step+") "+e);
            }
            $("#save-package").click(savePackage);
            $("#add-file").click(addFile);
        }

        $("#inputForms").load(packageData.portalPath + "/arts/inputForms.html", onContentLoaded);
    });
    //
})($);