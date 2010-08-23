
var widgets=null;


(function($){
  var validator;

  function trim(s){
    return $.trim(s);
    return s.replace(/^\s+|\s+$/g, "")
  }

  function getById(id){
    return $(document.getElementById(id));
  }

  function reduce(c, func, i){
    if(!i)i=0;
    $.each(c, function(k, v){
      i = func(v, i);
    });
    return i;
  }

  function any(c, func){
    var flag=false;
    $.each(c, function(k, v){
      if(func(v)) flag=true;
    });
    return flag;
  }

  function messageBox(msg){
      var msgBox=messageBox.msgBox;
      if(!msgBox){  // setup
          var div, i;
          msgBox = $("<div class='box hidden' style='text-align:center;'/>");
          msgBox.append($("<span/>"));
          div = $("<div style='padding-top:1em;''/>");
          msgBox.append(div);
          i = $("<input type='button' value='OK'/>");
          div.append(i);
          messageBox.msgBox=msgBox;
          i.click(function(){msgBox.dialog("close");});
          msgBox.dialog({title:"Message", hide:"blind",
                modal:true, autoOpen:false });
      }
      msgBox.dialog("open").find("span:first").text(msg);
  }

  function save(){
    var data={}, s, v, e, url, formFields, replaceFunc;
    formFields = $("#formFields").val() + $("#formFields-readonly").val();
    formFields = $.grep(formFields.split(/[\s,]+/),
                            function(i){return /\S/.test(i)});
    url = $("#saveFormFieldsUrl").val();
    replaceFunc=function(s){
        s = s.replace(/[{}()]/g, "");   // make it safe - no function calls
        return eval(s);
    };
    url = url.replace(/{[^}]+}/g, replaceFunc);
    function getValue(i){
      e = $("*[id="+i+"]");
      if(e.size()==0) e=$("input[name="+i+"]");
      if(e.size()==0){return null;}
      v = e.val();
      if(e.attr("type")==="checkbox"){
        if(!e.attr("checked"))v="";
      }
      return v;
    }
    $.each(formFields, function(c, i){
      s = /\.\d+(\.|$)/.test(i);
      if(s){
        var id, count=1;
        while(true){
          id = i.replace(/\.0(?=\.|$)/, "."+count);
          v=getValue(id);
          if(v===null) break;
          data[id]=v;
          count+=1;
        }
      }else{
        v=getValue(i);
        data[i]=v;
      }
    });
    if(data.metaList=="[]"){
        s = [];
        $.each(data, function(k, v){if(k!="metaList")s.push(k);});
        data.metaList=s;
    }
    $.ajax({type:"POST", url:url, data:data,
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

  function restore(data){
      var keys=[], skeys=[], inputs, input;
      var formFields = $.grep($("#formFields").val().split(/[\s,]+/),
                            function(i){return /\S/.test(i)});
  //alert(data.toSource());
      $.each(data, function(k, v){keys.push(k);});
      keys.sort();
      skeys = $.grep(keys, function(i){return /\.\d+(\.|$)/.test(i);}, true);
      //alert(skeys.toSource());
      //alert(formFields.toSource());
      inputs = $("input, textarea, select");
      $.each(skeys, function(c, v){
          if($.inArray(v, formFields)!=-1){
              inputs.filter("[id="+v+"]").val(data[v]);
          }
      });
      // list items
      skeys = $.grep(keys, function(i){return /\.\d+(\.|$)/.test(i);});
      $.each(skeys, function(c, v){
          var k = v.replace(/\.\d+(?=(\.|$))/, ".0");
          if($.inArray(k, formFields)!=-1){
              input = inputs.filter("[id="+v+"]");
        //if(data[v]=="")data[v]="[Blank]";
        //alert(v + ", "+ data[v]);
              if(input.size()>0){
                input.val(data[v]);
              }else{
                input = inputs.filter("[id="+k+"]");
                input.parents(".input-list:first").find(".add-another-item, .item-add").click();
                // update inputs - this could be done better
                inputs = $("input, textarea, select");
                input = inputs.filter("[id="+v+"]");
          if(input.size()==0){
              alert("id '"+v+"' not found!");
          }
                input.val(data[v]);
              }
          }
      });
  }

  function reset(){
  }

  function validation(){
    var reg1=/(\s*(\w+)\s*(\(((('([^'\\]|\\.)*')|("([^"\\]|\\.)*")|(\/([^\/\\]|\\.)*\/)|(([^'"\/\\]|\\.)*?))*?)\))\s*(;|$)\s*)|([^;]*(;|$))/g; // 2, 4, 14, 15=Err
    var reg2=/(\()|(\))|('([^'\\]|\\.)*')|("([^"\\]|\\.)*")|(\/([^\/\\]|\\.)*\/)|(\w[\w\d\._]*)|(([^\(\)\w\s'"\/\\]|\\.)+)/g;
    var tests={}, namedTests={};
    function iterReader(arr){
      var pos=-1; l=arr.length;
      function current(){ return arr[pos]; }
      function next(){pos+=1; return current();}
      function hasMore(){return (pos+1)<l;}
      function lookAHead(){return arr[pos+1];}
      return {current:current, next:next, hasMore:hasMore, lookAHead:lookAHead};
    }
    function isOkToX(x){
      var e=false;
      $.each(tests[x]||[], function(c,f){
        e|=f();
      });
      return !e;
    }
    function testTest(name){
        var test=namedTests[name];
        if(test && test._testFunc){
            try{
                return !test._testFunc();
            }catch(e){
            }
        }
        return false;
    }
    function getExpr(reader){
      // tests: !=str, =str, /regex/, !/regex/, empty, notEmpty, email, date, [>1], (), ex1 AND ex2, ex1 OR ex2,
      var v=reader.next(), expr="", getNumber;
      getNumber=function(){
          return reader.next()*1;
      }
      if(v=="email"){v="/.+?\\@.+\\..+?/";}
      else if(v=="empty"){expr="(v=='')";}
      else if(v=="YYYY"){    v="/^[12]\\d{3}$/";}
      else if(v=="YYYYMM"){  v="/^[12]\\d{3}([\\/\\\\\\-](0?[1-9]|1[012])|((0[1-9])|(1[012])))$/";}
      else if(v=="YYYYMMDD"){v="/^[12]\\d{3}([\\/\\\\\\-](0?[1-9]|1[012])|((0[1-9])|(1[012])))([\\/\\\\\\-](0?[1-9]|[12]\\d|3[01])|((0[1-9])|[12]\\d|(3[01])))$/";}
      else if(v=="len"){
          v=reader.next();
          if(v=="("){v=reader.next();if(v==")")v=reader.next();}
          var n = getNumber();
          if(isNaN(n)){ /*Error*/ return "";}
          if(v=="="){v="/^.{"+n+"}$/";}
          else if(v==">"){v="/^.{"+(n+1)+",}$/";}
          else if(v=="<"){v="/^.{0,"+(n-1)+"}$/";}
          else { /*Error*/ return "";}
      }
      //
      if(v=="notEmpty"){expr="(v!='')";}
      else if(v=="="){expr="(v=="+reader.next()+")";}
      else if(v=="!="){expr="(v!="+reader.next()+")";}
      else if(v[0]=="/"){expr="("+v+".test(v))";}
      else if(v.substr(0,2)=="!/"){expr="("+v+".test(v))";}
      else if(v=="("){expr+="("+getExpr(reader)+")";if(reader.next()!=")"){alert("expected a ')'!");}; }
      else if(/^[\w\d\._]+$/.test(v)){expr+="testTest('"+v+"')";}
      v=reader.lookAHead();
      if(v){
        v=v.toUpperCase();
        if(v=="AND"){reader.next(); expr+="&&"+getExpr(reader);}
        else if(v=="OR"){reader.next(); expr+="||"+getExpr(reader);}
      }
      return expr;
    }
    function getWhen(reader){
        var d, action, target;
        if(!reader.hasMore()) return null;
        d=reader.next();
        while(d=="," || d==";")d=reader.next();
        if(!d) return null;
        if(d[0]=="'" || d[0]=='"'){
            target=$(eval(d));
            if(reader.lookAHead()=="."){reader.next();d=reader.next();}
            else{ return {}; /* ERROR */ }
        }
        action = d.toLowerCase();
        if(action.substr(0,2)=="on")action=action.substr(2);
        while(reader.hasMore()){
            // read upto the next , or ;
            d=reader.next();
            if(d=="," || d==";") break;
        }
        return {action:action, target:target};
    }
    return {
      setup:function(onLoadTest){
        var m, w, va, f, a, t;
        var validationsFor={}, valdationsForLists={};
        //var matchQuotedStr = '("([^"\\]|\\.)*")';     // continues matching until closing (unescaped) quote
        var vLabels=$("label.validation-err-msg");
        $(".validation-err-msg").hide();
        //value="for('dc:title'),notEQ(''),when(onChange)"
        $(".validation-rule").each(function(c, v){
            var dict={};
            v = $(v).val() || $(v).text();
            function match(){
                m=arguments;     //2, 4, 14, 15=Err
                if(m[0].length==0)return "";
                if(m[15]){
                    alert("Error: '"+m[15]+"' in '"+v+"'");
                    return "";
                }
                w=m[2].toLowerCase();
                va=m[4];
                dict[w]=va;
                return "";
            }
            v.replace(reg1, match);
            f=dict["for"];
            dict["when"]=(dict["when"]||"");
            if(f){
                if(/\.0(?=\.|$)/.test(dict[f])){
                    a = validationsForLists[f];
                    if(!a) a=validationsForLists[f]=[];
                }else{
                    a = validationsFor[f];
                    if(!a) a=validationsFor[f]=[];
                }
                a.push(dict);
                if(dict["name"]){
                    namedTests[dict["name"]]=dict;
                }
            }
        });
        //alert(validationsFor.toSource());
        $.each(validationsFor, function(f, l){
            var target = $(document.getElementById(f));
            var reader, getValue, showValidationMessage;
            var testStr, func;
            if(true){
                var vLabel=vLabels.filter("[for="+f+"]");
                getValue=function(){ return target.val(); };
                showValidationMessage=function(show){ show?vLabel.show():vLabel.hide(); return show;};

                $.each(l, function(c, d){
                    testStr="";
                    //alert(d.toSource());
                    if(d.test){
                      reader = iterReader(d.test.match(reg2));
                      testStr = getExpr(reader);
                      try{
                        func="func=function(){var v=getValue();return showValidationMessage(!("+testStr+"));};";
                        eval(func);
                        d._testFunc = func;
                      } catch(e){
                        alert(e + ", func="+func);
                      }
                      //if(onLoadTest){func();}
                      m = d.when.match(reg2);
                      if(m){
                        reader = iterReader(m);
                        while(w=getWhen(reader)){
                          w.target = w.target||target;
                          w.target.bind(w.action, func);
                          if(!tests[w.action]) tests[w.action]=[];
                          tests[w.action].push(func);
                        }
                      }
//                      $.each(d.when.split(/(,|\s)+/), function(c, w){
//                        if(w.substr(0,2)=="on") w=w.substr(2);
//                        target.bind(w, func);
//                        if(!tests[w]) tests[w]=[];
//                        tests[w].push(func);
//                      });
                    }
                });
            }
        });
      },
      test:function(){},
      isOkToSave:function(){return isOkToX("save");},
      isOkToSubmit:function(){return isOkToX("submit");},
      parseErrors:{}
    }
  };
  
  function changeToTabLayout(elem){
    var h, li, ul = $("<ul></ul>");
    elem.children("h3").each(function(c, e){
        h = $(e);
        li = $("<li><a href='#" + h.next().attr("id") + "'><span>" + h.text() + "</span></a></li>");
        ul.append(li);
        h.remove();
    });
    if(true){
        var sel;
        elem.find(".prev-tab").click(function(){
            sel=elem.tabs("option", "selected");
            elem.tabs("option", "selected", sel-1);
        });
        elem.find(".next-tab").click(function(){
            sel=elem.tabs("option", "selected");
            elem.tabs("option", "selected", sel+1);
        });
    }
    elem.prepend(ul);
    return elem;
  }

  function onContentLoaded(){
    // ==============
    // Date inputs
    // ==============
try{
    $("input.dateYMD, input.date").datepicker({dateFormat:"yy-mm-dd", changeMonth:true, changeYear:true, showButtonPanel:false});
    function datepickerOnClose(dateText, inst){
        var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
        var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
        if(!month)month=0;
        $(this).datepicker('setDate', new Date(year, month, 1));
        $(this).blur();
    }
    function datepickerBeforeShow(input, inst){
        inst = $("#" + inst.id);
        if(inst.hasClass("dateMY") || inst.hasClass("dateYM") || inst.hasClass("dateY")){
          setTimeout(function(){
              $(".ui-datepicker-calendar").remove();
              $(".ui-datepicker-current").remove();
              $(".ui-datepicker-close").text("OK");
              if(inst.hasClass("dateY")) $(".ui-datepicker-month").remove();
            }, 10);
        }
    }
    $('input.dateYM').datepicker({
      changeMonth: true, changeYear: true, showButtonPanel: true, dateFormat: 'yy-mm',
      onClose: datepickerOnClose,
      beforeShow:datepickerBeforeShow,   
      onChangeMonthYear:function(year, month, inst){ datepickerBeforeShow(null, inst); },
      onSelect:function(dateText, inst){}
    });
    $('input.dateMMY').datepicker({
      changeMonth: true, changeYear: true, showButtonPanel: true, dateFormat: 'MM yy',
      onClose: datepickerOnClose,
      beforeShow:datepickerBeforeShow,   
      onChangeMonthYear:function(year, month, inst){ datepickerBeforeShow(null, inst); },
      onSelect:function(dateText, inst){}
    });
    $('input.dateY').datepicker({
      changeMonth: false, changeYear: true, showButtonPanel: true, dateFormat: 'yy',
      onClose: datepickerOnClose,
      beforeShow:datepickerBeforeShow,   
      onChangeMonthYear:function(year, month, inst){ datepickerBeforeShow(null, inst); },
      onSelect:function(dateText, inst){}
    });
}catch(e){/*alert(e);*/}
    // ==============
    
    // ==============
    // Simple (text) list input type
    // ==============
    $("table.input-list").each(function(c, i){
      var table, count, tmp, visibleItems, displayRowTemplate, displaySelector;
      var add, del, reorder, addUniqueOnly=false;
      table = $(i);
      if(table.hasClass("sortable")){
        table.find("tbody:first").sortable({
          items:"tr.sortable-item",
          update:function(e, ui){ reorder(); }
        });
      }
// check all variable names 
      if(table.find(".item-display").size()){
        if(table.find("tr.item-input-display").size()){
          alert("Error: table.input-list can not have both 'item-display' and 'item-input-display' table row classes");
          return;
        }
        // For handling 'item-display' (where there is a separate/special row for handling the display of added items)
        //    Note: if there is an 'item-display' row then it is expected that there will also be an 
        //        'item-input' row as well an 'item-add' button/link
        displaySelector = ".item-display";
        tmp=table.find(displaySelector).hide();
        displayRowTemplate=tmp.eq(0);
        add=function(){
          // get item value(s) & validate (if no validation just test for is not empty)
          var values=[];
          var test=[];
          table.find("tr.item-input input[type=text]").each(function(c, i){
            values[c]=[$.trim($(i).val()), i.id];
            test[c]=values[c][0];
            $(i).val("");   // reset
          }).eq(0).focus();
          if(!any(values, function(v){ return v[0]!==""; })) return;
          //
          visibleItems = table.find(displaySelector+":visible");
          if(addUniqueOnly){
            // Check that this entry is unique
            var unique=true;
            visibleItems.each(function(c, i){
              i=$(i);
              var same=true;
              i.find("input").each(function(c2, i){
                if(test[c2]!=i.value)same=false;
              });
              if(same)unique=false;
            });
            if(!unique){
                alert("Selection is already in the list. (not a unique value)");
                return;
            }
          }
          tmp = displayRowTemplate.clone().show();
          count = visibleItems.size()+1;
          tmp.find("*[id]").each(function(c, i){ i.id = i.id.replace(/\.0(?=\.|$)/, "."+count); });
          tmp.find(".item-display-item").each(function(c, i){
            var id = values[c][1].replace(/\.0(?=\.|$)/, "."+count);
            //$(i).text(values[c][0]);
            //$(i).append("<input type='hidden' id='"+id+"' value='"+values[c][0]+"'/>");
            $(i).append("<input type='text' id='"+id+"' value='"+values[c][0]+
                        "' readonly='readonly' size='64' />");
          });
          tmp.find(".sort-number").text(count);
          table.find(displaySelector+":last").after(tmp);
          visibleItems.find(".delete-item").show();
          //if(count==1) tmp.find(".delete-item").hide();
          tmp.find(".delete-item").click(del);
        }

        del=function(e){
          $(this).parents("tr").remove();
          //if(table.find(displaySelector+":visible").size()==1){
          //  table.find(displaySelector+":visible .delete-item").hide();
          //}
          reorder();
          return false;
        }
        
        table.find(".item-add").click(add);
        addUniqueOnly = table.find(".item-add").hasClass("add-unique-only");
        table.find("tr.item-input input[type=text]:last").keypress(function(e){
          if(e.which==13){
            add();
          }
          if(e.which==8 && false){      // backspace delete/recall exp.
            if($(e.target).val()==""){
              if(table.find(displaySelector+":visible").size()>0){
                var i=table.find(displaySelector+":visible:last input:last");
                del.apply(i);
                $(e.target).val(i.val());
                return false;
              }
            }
          }
        });
      }else if(table.find("tr.item-input-display").size()){
        // For handling 'item-input-display' type lists
        //   Note: if there is an 'item-input-display' row then it is also excepted that there
        //      will be an 'add-another-item' button or link
        displaySelector="tr.item-input-display";
        tmp=table.find(displaySelector).hide();
        displayRowTemplate=tmp.eq(0);

        add=function(){
          visibleItems = table.find(displaySelector+".count-this");
          tmp = displayRowTemplate.clone().show().addClass("count-this");
          count = visibleItems.size()+1;
          tmp.find("*[id]").each(function(c, i){ i.id = i.id.replace(/\.0(?=\.|$)/, "."+count); });
          tmp.find(".sort-number").text(count);
          table.find(displaySelector+":last").after(tmp);
          visibleItems.find(".delete-item").show();
          if(count==1) tmp.find(".delete-item").hide();
          tmp.find(".delete-item").click(del);
        }

        del=function(e){
          $(this).parents("tr").remove();
          if(table.find(displaySelector+":visible").size()==1){
            table.find(displaySelector+":visible .delete-item").hide();
          }
          reorder();
          return false;
        }

        add();
        table.find(".add-another-item").click(add);
      }

      reorder=function(){
        table.find(displaySelector+":visible").each(function(c, i){
          $(i).find("*[id]").each(function(_, i){
            i.id = i.id.replace(/\.\d+(?=\.|$)/, "."+(c+1));
          });
          $(i).find(".sort-number").text(c+1);
        });
      }

    });
    // ==============
    
  
    // ==============
    // Multi-dropdown selection
    // ==============
    //function buildSelectList(list, _default, selectable, ns, getJson, onSelection){
    function buildSelectList(json, parent, getJson, onSelection){
      var s, o, children={}, ns, selectable;
      ns = (json.namespace || "") || (parent.namespace || "");
      selectable = (json.selectable==null)?(!!parent.selectable):(!!json.selectable);
      s = $("<select/>");
      if(!json["default"]){
        o = $("<option value=''>Please select one</option>");        
        s.append(o);
      }
      $.each(json.list, function(c, i){
        var sel=!!(i.selectable!=null?i.selectable:selectable);
        children[i.id]={url:(i.children==1?i.id:i.children), label:i.label, id:i.id, 
                        selectable:sel, namespace:ns, parent:parent};
        o = $("<option/>");
        o.attr("value", i.id);
        if(i.id==json["default"]) o.attr("selected", "selected");
        o.text(i.label);
        s.append(o);
      });
      function onChange(){
        var id, child, j;
        id = s.val();
        child = children[id] || {parent:parent};
        if(s.nextUntil){
            s.nextUntil(":not(select)").remove();
        }else{
            function removeSelects(s){
                if(s.size()==0)return;
                removeSelects(s.next("select"));
                s.remove();
            }
            removeSelects(s.next("select"));
        }
        if(child.url){
          getJson(child.url, false, function(j){
              s.after(buildSelectList(j, child, getJson, onSelection));
              onSelection(child);
          });
        }
        onSelection(child);
      }      
      s.change(onChange);
      setTimeout(onChange, 10);
      return s;
    }

    $(".data-source-drop-down").each(function(c, dsdd){
      var ds=$(dsdd), id=dsdd.id, jsonUrl, json=[], selAdd;
      var selAddNs, selAddId, selAddLabel;
      var getJson, onSelection, onJson;
      ds.hide();
      selAdd = ds.parent().find(".selection-add");
      // ".json-data-source-url" val(), ".json-data-source" text(),
      //    ".selection-add"
      getJson = function(urlId, absolute, onJson){
        var j, url=urlId;
        if(json) j=json[urlId]
        if(j){
            onJson(j);
            return;
        }
        if(!absolute){
          url=jsonUrl+urlId;
          if(!/\.json$/.test(url)) url+=".json";
        }
        $.getJSON(url, function(data){
          json=data;
          onJson(json);
        });
      }
      onSelection = function(info){
        var sel;
        //info.namespace, info.id, info.label, info.selectable, info.parent
        while(info.selectable!==false && info.selectable!==true){
          if(info.parent) info = info.parent;
          else info.selectable=false;
        }
        sel=info.selectable;
        if(/BUTTON|INPUT/.test(selAdd[0].tagName)){
          selAdd.attr("disabled", sel?"":"disabled");
        }else{
          sel?selAdd.show():selAdd.hide();
        }
        if(sel){
          selAddNs=info.namespace; selAddId=info.id; selAddLabel=info.label;
        }else{
          selAddNs=""; selAddId=""; selAddLabel="";
        }
        ds.find(".selection-add-id").val(selAddId);
        ds.find(".selection-add-label").val(selAddLabel);
        selAdd.find(".selection-add-id").text(selAddId);
        selAdd.find(".selection-add-label").text(selAddLabel);
      }
      onJson = function(json){
        // OK now build the select-option
        var o = buildSelectList(json, {}, getJson, onSelection);
        ds.after(o);
        //o.after(selAdd);
      }
      jsonUrl=ds.find(".json-data-source-url").val();
      if(jsonUrl){
        json=getJson(jsonUrl, true, onJson);
        if(/\//.test(jsonUrl)){
          jsonUrl=jsonUrl.split(/\/([^\/]*$)/)[0]+"/";  // split at the last /
        }else{
          jsonUrl="";
        }
      }else{
        json=$(".json-data-source");
        json = json.val() || json.text();
        try{
          //json = eval("("+json+")");
          json = $.parseJSON(json);
          onJson(json);
        }catch(e){
          alert("Not valid json!");
          json = null;
          return;
        }
      }
    });
    // ==============

    validator = validation();
    validator.setup();
    $("#saveFormFields").click(save);
    $("#restoreFormFields").click(restore);
    $("#resetFormFields").click(reset);

  }

  $(function(){
    onContentLoaded();
  });

  widgets = {
    changeToTabLayout:changeToTabLayout,
    restore:restore,
    getValidator:function(){return validator;},
    onContentLoaded:onContentLoaded
  };

})(jQuery);



