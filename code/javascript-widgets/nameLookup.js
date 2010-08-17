
(function(){
  $(function(){
    function debug(msg){
      $("#debugMsg").text(msg.toString());
    }
    $("#refresh").click(function(){
      location.href = location.href;
    });
    debug("Reloaded ok");

    var dialog=$("<div>Test dialog message!</div>").dialog({
                  title:"Name lookup", 
                  height:"auto",
                  width:300,
                  modal:false,
                  resizable:true,
                  position:"center",
                  draggable:false,
                  //buttons:{"OK":function(){$(this).dialog("close");}},
                  autoOpen:false
                });
    var detailDialog=$("<div/>").dialog({
                  title:"Detail information", 
                  height:"auto",
                  width:300,
                  modal:false,
                  resizable:true,
                  position:"center",
                  draggable:false,
                  //buttons:{"OK":function(){$(this).dialog("close");}},
                  autoOpen:false
    });

    var nameLookupUrl = $("#nameLookup-url").val() || "nameLookup.json";
    var nameLookup = $(".nameLookup");
    nameLookup.change(function(){
      debug("change ok");
    });
    nameLookup.keypress(function(){
      debug("keypress ");
      var e=$(this);
      setTimeout(function(){checkNameLookup(e)}, 10);
    });
    nameLookup.each(function(c, e){
      e = $(e);
      var s = e.next(".nameLookup-link");
      e.after(s);
      checkNameLookup(e);
      s.click(function(){
        doNameLookup(e, s.position());
        return false;
      });
    });
    function checkNameLookup(e){
      $.trim(e.val())===""?e.next().hide():e.next().show();
    }

    function doNameLookup(e, position){
      var displayDetails, queryStr=e.val();
      $.getJSON(nameLookupUrl, {query:queryStr}, function(data){
        var div, d, a;
        //alert(data.toSource());
        div = $("<div/>");
        div.text("Search result for '" + queryStr + "'");
        div.append("<hr/>");
        $.each(data.items, function(c, i){
          var r;
          d = $("<div/>");
          r = $("<input type='radio' name='name' value='"+i.uri+"'/>");
          d.append(r);
          d.append(i.title);
          a = $("<a style='float:right;' href='#'>details</a>");
          d.append(a);
          div.append(d);
          a.click(function(){
            var pos = $(this).position();
            pos.left += position.left+70;
            pos.top += position.top;
            displayDetails(i.title, i.details, pos, r);
            return false;
          });
        });
        
        dialog.html(div);
        dialog.dialog({position:position, buttons:{
                        "OK":function(){
                            var value=div.find("input[name=name]:checked").val();
                            e.parent().find(".nameLookup-nameUrl").val(value);
                            alert(value);
                            dialog.dialog("close");
                          },
                          "Cancel":function(){
                            dialog.dialog("close");
                          }
                        },
                      }).dialog("open");
      });
      displayDetails = function(name, details, position, link){
        detailDialog.text(name);
        detailDialog.append("<hr/>");
        $.each(details, function(k, v){
          var d=$("<div/>");
          d.html("<span>"+k.replace(/^./, function(m){return m.toUpperCase();})+":&#160;</span>");
          d.append(v);
          detailDialog.append(d);
        });
        detailDialog.dialog({position:position, buttons:{
                                "OK": function(){link.click(); detailDialog.dialog("close");},
                                "Cancel": function(){detailDialog.dialog("close");}
                              }
                            }).dialog("open").dialog("moveToTop");
      };
    }
  });
})($);



