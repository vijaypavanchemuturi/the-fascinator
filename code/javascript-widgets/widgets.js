
(function($){

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
/* */

  function save(){
    var data={}, s, v, e;
    var fields = $("#formFields").val().split(/[\s,]+/);
    function getValue(i){
      e = $("*[id="+i+"]");
      if(e.size()==0) e=$("input[name="+i+"]");
      if(e.size()==0) return null;
      v = e.val();
      if(e.attr("type")==="checkbox"){
        if(!e.attr("checked"))v="";
      }
      return v;
    }
    $.each(fields, function(c, i){
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
    alert(data.toSource());
  }

  function restore(){
  }

  function reset(){
  }

  $(function(){
    // ==============
    // Date inputs
    // ==============
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
              i.find("input[type=hidden]").each(function(c2, i){
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
            $(i).text(values[c][0]);
            $(i).append("<input type='hidden' id='"+id+"' value='"+values[c][0]+"'/>");
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
          visibleItems = table.find(displaySelector+":visible");
          tmp = displayRowTemplate.clone().show();
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
      if(!json.default){
        o = $("<option value=''>Please select one</option>");        
        s.append(o);
      }
      $.each(json.list, function(c, i){
        var sel=!!(i.selectable!=null?i.selectable:selectable);
        children[i.id]={url:(i.children==1?i.id:i.children), label:i.label, id:i.id, 
                        selectable:sel, namespace:ns, parent:parent};
        o = $("<option/>");
        o.attr("value", i.id);
        if(i.id==json.default) o.attr("selected", "selected");
        o.text(i.label);
        s.append(o);
      });
      function onChange(){
        var id, child, j;
        id = s.val();
        child = children[id] || {parent:parent};
        s.nextUntil(":not(select)").remove();
        if(child.url){
          j = getJson(child.url);
          s.after(buildSelectList(j, child, getJson, onSelection));
        }
        onSelection(child);
      }      
      s.change(onChange);
      setTimeout(onChange, 10);
      return s;
    }

    $(".data-source-drop-down").each(function(c, dsdd){
      var ds=$(dsdd), id=dsdd.id, jsonUrl, json, selAdd;
      var selAddNs, selAddId, selAddLabel;
      ds.hide();
      selAdd = ds.parent().find(".selection-add");
      // ".json-data-source-url" val(), ".json-data-source" text(),
      //    ".selection-add"
      function getJson(urlId, absolute){
        var j=json[urlId], url=urlId;
        if(j) return j;
        if(!absolute){
          url=jsonUrl+urlId;
        }
        $.getJSON(url, function(data){
          json=data;
          return json;
        });
      }
      jsonUrl=$(".json-data-source-url").val();
      if(jsonUrl){
        json=getJson(jsonUrl, true);
        if(/\//.test()){
          jsonUrl=jsonUrl.split(/\/([^\/]*$)/)[0]+"/";  // split at the last /
        }else{
          jsonUrl="";
        }
      }else{
        json=$(".json-data-source");
        json = json.val() || json.text();
      }
      function onSelection(info){
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
      if(json){
        try{
          json = eval("("+json+")");
        }catch(e){
          alert("Not valid json!"); return;
        }
        // OK now build the select-option

        var o = buildSelectList(json, {}, getJson, onSelection);
        ds.after(o);
        //o.after(selAdd);
      }

      $("#save").click(save);
      $("#restore").click(restore);
      $("#reset").click(reset);
    });
    // ==============

    alert("loaded ok ");
  });

})($);



