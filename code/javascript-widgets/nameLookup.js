

(function(){
    var dialog, detailDialog;
    $(function(){
        //setTimeout(function(){
            dialog=$("<div>Test dialog message!</div>").dialog({
                  title:"Name lookup",
                  height:350,  maxHeight:600,
                  width:400,
                  modal:false,
                  resizable:true,
                  position:"center",
                  draggable:true,
                  autoOpen:false
                });
            detailDialog=$("<div/>").dialog({
                  title:"Detail information",
                  height:"auto",
                  width:400,
                  modal:false,
                  resizable:true,
                  position:"center",
                  draggable:false,
                  autoOpen:false
                });
            gdialog = dialog;
        //}, 10);
    });

    var displayNameLookups = function(json, position, queryTerm, callback,
                getJson){
        var div, display;
        display=function(){
            var tbody, tr, td, label, id;
            div.html("");
            if(queryTerm) div.text("Search result for '" + queryTerm + "'");
            if(getJson){
                var i, s;
                div.append("<br/>");
                i=$("<input type='text'/>");
                i.val(queryTerm);
                div.append(i);
                s=$("<input type='button' value='Search'/>");
                div.append(s);
                i.keypress(function(e){if(e.which==13)s.click();});
                s.click(function(){
                    queryTerm=$.trim(i.val());
                    if(queryTerm){
                        getJson(queryTerm, function(jdata){
                            if(jdata.error){ alert(jdata.error); }
                            else{
                                json=jdata; display();
                                _i = i;
                                setTimeout(i.focus, 10);
                            }
                        });
                    }
                });
            }
            div.append("<hr/>");
            div.append($("<table/>").append(tbody=$("<tbody/>")));
            $.each(json.results, function(c, result){
              var r, a, name;
              id="rId"+c;
              tr=$("<tr/>").append(td=$("<td/>"));
              r=$("<input type='radio' name='name' />").attr("id", id);
              r.val(JSON.stringify(result));
              label=$("<label/>").attr("for", id);
              name = result["rdfs:label"] || result["foaf:name"];
              label.append(name);
              if(result["dc:description"]){
                  label.append(" - "+result["dc:description"]);
              }
              //
              td.append(r);
              td.append(label);
              //
              tr.append(td=$("<td/>"));
              a=$("<a style='color:blue;' href='#'>details</a>");
              td.append(a);
              tbody.append(tr);
              label.dblclick(function(){dialog.dialog("option", "buttons").OK();});
              a.click(function(e){
                var pos = dialog.parent().position();
                pos.left += 10;
                pos.top += 20;
                displayDetails(name, result, pos, r);
                return false;
              });
            });
        };

        div = $("<div/>");
        display();
        dialog.html(div);
        dialog.dialog("option", "buttons", {
                        "OK":function(){
                            var value=div.find("input[name=name]:checked").val();
                            dialog.dialog("close");
                            detailDialog.dialog("close");
                            callback(true, JSON.parse(value));
                          },
                          "Cancel":function(){
                            dialog.dialog("close");
                            detailDialog.dialog("close");
                            callback(false);
                          }
                        });
        // //dialog.dialog("option", "position", [position.left, position.top]);
        dialog.dialog("open");
        //dialog.parent().css("top", position.top+"px").css("left", position.left+"px");
    }

    var displayDetails = function(name, details, pos, link){
        detailDialog.html("");
        detailDialog.text(name);
        detailDialog.append("<hr/>");
        $.each(details, function(k, v){
            var d=$("<div/>");
            d.text(k+" : "+v);
            detailDialog.append(d);
        });
        detailDialog.dialog("option", "buttons", {
                            "OK": function(){link.click(); detailDialog.dialog("close");},
                            "Cancel": function(){detailDialog.dialog("close");}
                          });
        //detailDialog.dialog("option", "position", [pos.left, pos.top]);
        detailDialog.dialog("open").dialog("moveToTop");
        detailDialog.parent().css("top", pos.top+"px").css("left", pos.left+"px");
    };

//
    function init(){
        $(".nameLookup-section").each(function(c, e){
            nameLookupSection($(e));
        });
    }

    function nameLookupSection(ctx){
        var url = ctx.find(".nameLookup-url").val();
        var valueNs = ctx.find(".nameLookup-value-ns").val();
        var textNs = ctx.find(".nameLookup-text-ns").val();
        function debug(msg){
            ctx.find(".nameLookup-debugMsg").text(msg.toString());
        }
        function getJson(queryUrl, timeoutSec, callback){
            $.ajax({url:queryUrl, dataType:"json", timeout:timeoutSec*1000,
                success:callback,
                error:function(x,s,e){callback({error:s});}
            });
        }
        //ctx.find(".nameLookup-lookup").unbind().click(function(e){
        ctx.find(".nameLookup-lookup").die().live("click", function(e){
            var target, parent, queryTerm, queryUrl, cGetJson
            var selectedNameCallback;
            target=$(e.target);
            _gtarget = target;
            parent=target;
            while(parent.size() && (parent.find(".nameLookup-name").size()==0)){
                parent=parent.parent();
            }
            queryTerm = parent.find(".nameLookup-name").map(function(i, e){return e.value;});
            queryTerm = $.trim($.makeArray(queryTerm).join(" "));
            if(queryTerm==="") return false;
            // Note: double escape the parameter because it is being passed as a parameter inside a parameter
            selectedNameCallback=function(ok, result){
                debug(ok);
                if(ok){
                    function xUpdate(ns, what){
                        var nsp, k;
                        if(!ns) return;
                        nsp = ns+"-";
                        parent.find("."+ns).each(function(c, e){
                            e=$(e);
                            $.each(e.attr("class").split(/\s+/), function(_, cls){
                                if(cls.substr(0, nsp.length)===nsp){
                                    k = cls.substr(nsp.length);
                                    if(result[k]){
                                        e[what](result[k]);
                                    }
                                }
                            });
                        });
                    }
                    xUpdate(valueNs, "val");
                    xUpdate(textNs, "text");
                }
            };
            // curry getJson
            cGetJson=function(queryTerm, callback){
                queryUrl = url.replace("{searchTerms}", escape(escape(queryTerm)));
                debug("clicked queryUrl="+queryUrl);
                getJson(queryUrl, 6, callback);
            };
            cGetJson(queryTerm, function(json){
                if(json.error){
                    alert(json.error);
                }else{
                    //alert(json.results.length);
                    var position = target.position();
                    displayNameLookups(json, position, queryTerm, 
                        selectedNameCallback, cGetJson);
                }
            });
            return false;
        });
        debug("loaded ok");
    }

    $(function(){
        init();
    });

    nameLookup = {init:init};
})($);


