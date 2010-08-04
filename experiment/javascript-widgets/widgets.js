
(function($){

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

  function trim(s){
    return $.trim(s);
    return s.replace(/^\s+|\s+$/g, "")
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
  $(function(){
    // Date inputs
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
    // ====
    
    // Simple (text) list input type
    $("table.input-list").each(function(c, i){
      var table, count, tmp, visibleItems, displayRowTemplate;
      var displaySelector;
      var add, del, reorder;
      table = $(i);
// check all variable names 
      if(table.find("tr.item-display").size()){
        if(table.find("tr.item-input-display").size()){
          alert("Error: table.input-list can not have both 'item-display' and 'item-input-display' table row classes");
          return;
        }
        displaySelector = "tr.item-display";
        tmp=table.find(displaySelector).hide();
        displayRowTemplate=tmp.eq(0);
        add=function(){
          // get item value(s) & validate (if no validation just test for is not empty)
          var values=[];
          table.find("tr.item-input input[type=text]").each(function(c, i){
            values[c]=$.trim($(i).val());
            $(i).val("");
          }).eq(0).focus();
          if(!any(values, function(v){ return $.trim(v)!==""; })) return;
          visibleItems = table.find(displaySelector+":visible");
          tmp = displayRowTemplate.clone().show();
          count = visibleItems.size()+1;
          tmp.find("*[id]").each(function(c, i){ i.id = i.id.replace(/\.0(?=\.|$)/, "."+count); });
          tmp.find("td.item-display").each(function(c, i){
            $(i).text(values[c]);
          });
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
        
        table.find(".item-add").click(add);
        table.find("tr.item-input input[type=text]:last").keypress(function(e){
          if(e.which==13){
            add();
          }
        });
/* */
      }else if(table.find("tr.item-input-display").size()){
        displaySelector="tr.item-input-display";
        tmp=table.find(displaySelector).hide();
        displayRowTemplate=tmp.eq(0);

        add=function(){
          visibleItems = table.find(displaySelector+":visible");
          tmp = displayRowTemplate.clone().show();
          count = visibleItems.size()+1;
          tmp.find("*[id]").each(function(c, i){ i.id = i.id.replace(/\.0(?=\.|$)/, "."+count); });
          table.find(displaySelector+":last").after(tmp);
          visibleItems.find(".delete-item").show();
          if(count==1) tmp.find(".delete-item").hide();
          tmp.find(".delete-item").click(del);
        }
        add();

        del=function(e){
          $(this).parents("tr").remove();
          if(table.find(displaySelector+":visible").size()==1){
            table.find(displaySelector+":visible .delete-item").hide();
          }
          reorder();
          return false;
        }

        table.find(".add-another-item").click(add);
      }
      reorder=function(){
        $(displaySelector+":visible").each(function(c, i){
          $(i).find("*[id]").each(function(_, i){
            i.id = i.id.replace(/\.\d+(?=\.|$)/, "."+(c+1));
          });
        });
      }

    });
    alert("loaded ok ");
  });

})($);



