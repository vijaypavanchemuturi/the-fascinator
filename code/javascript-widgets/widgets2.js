
var widgets={forms:[], globalObject:this};


(function($){
  var globalObject=widgets.globalObject;
  var formClassName="widget-form";

  if(!$.fn.dataset){     // if dataset function not defined
      $.fn.dataset=function(name, value){return this.attr("data-"+name, value);};
  }
  if(!$.fn.getDatasets){
      $.fn.getDatasets=function(ff){
          var atts, d={}, item, name, value;
          if(this[0]){
            atts=this[0].attributes;
            for(var i=0,l=atts.length; i<l; i++){
                item=atts.item(i);
                name=item.name;
                if(name.substr(0,5)=="data-"){
                    name=name.substr(5);
                    value=item.value;
                    if(!ff || ff(name, value)){
                        d[name]=value;
                    }
                }
            }
          }
          return d;
      };
  }

  // Add these functions if not already defined (in jQuery 1.4.x)
  if(!$.fn.first){
      $.fn.first=function(){return this.eq(0);};
  }
  if(!$.fn.last){
      $.fn.last=function(){return this.eq(this.length-1);};
  }
  if(!$.isEmptyObject){
      $.isEmptyObject=function(a){for(var b in a){return false;}return true;};
  }

  function fn(s){ // inline function generator
      // fn("a,b->a+b")(3, 2)   or fn("$1+$2")(3, 2)
      var a, b, re=/^([\w$,\s]+)-\>/, re2=/;|([^\s\=]\s*\{)/;
      if(re.test(s)){
          b=s.replace(re, function(_, a1){a=a1;return "";});
      }else{
          a="$1,$2,$3,$4";
          b=s;
      }
      if(!re2.test(b)) b="return "+b;
      return new Function(a, b);
  };
  _fn = fn;

  function log(type, msg){
      if(globalObject.console){
          try{ globalObject.console[type](msg); }catch(e){}
      }
  }
  function logInfo(msg){ log("info", msg); }
  function logError(msg){ log("error", msg); }
  function logWarning(msg){ log("warn", msg); }
  function logDebug(msg){ log("debug", msg); }

  var _idNum=1;
  function getIdNum(){
      return _idNum++;
  };

  function trim(s){
    return $.trim(s);
    return s.replace(/^\s+|\s+$/g, "")
  };

  function keys(d, f){
    var keys=[], k;
    for(k in d){
        if(!f || f(k)){
            keys.push(k);
        }
    }
    return keys;
  };
  function values(d){
      var values=[], k;
      for(k in d){values.push(d[k]);}
      return values;
  };

  function getById(id){
    var e=document.getElementById(id);
    if(e){
        return $(e);
    }else{
        return $("#_doesNotExist_.-_");
    }
  };

  function reduce(c, func, i){
    if(!i)i=0;
    $.each(c, function(k, v){
      i = func(v, i);
    });
    return i;
  };

  function any(c, func){
    var k;
    for(k in c){
        if(func(k, c[k])) return true;
    }
    return false;
  };

  function isFunction(func){
      return typeof(func)==="function";
  };

  function callIfFunction(func, a, b, c){
      if($.isFunction(func)){
          try{ return func(a, b, c); }catch(e){}
      }
  };

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
  };

  function validator(){
    var iterReader, isOkToX, testTest, getExpr, getWhen
    var hideAllMessages, setup
/*
(                                           1
  \s*                                           any white spaces
  (\w+)                                     2   name (word)
  \s*                                           any white spaces
  (                                         3
    \(                                          (
    (                                       4
      (                                     5
        ( ' ( [^'\\] | \\. )* ' )               'str\e'  6&7
        |
        ( " ( [^"\\] | \\. )* " )               "str\e"  8&9
        |
        ( \/ ( [^\/\\] | \\. )* \/ )            /reg\e/  10&11
        |
        (                                   12
          ( [^'"\)\(\/\\] | \\. )*              anything but ' " ) ( \ /  13
        )
      )*
    )
    \)                                          )
  )
  \s*
  (; | $)                                   14  ; or end of string
  \s*
)
|
(                                           15
  \s* ( ; | $ )                             16
)
 */
    //var reg1=/(\s*(\w+)\s*(\(((('([^'\\]|\\.)*')|("([^"\\]|\\.)*")|(\/([^\/\\]|\\.)*\/)|(([^'"\/\\]|\\.)*?))*?)\))\s*(;|$)\s*)|(\s*(;|$))/g; // 2, 4, 14, 15=Err
    //var reg2=/(\()|(\))|('([^'\\]|\\.)*')|("([^"\\]|\\.)*")|(\/([^\/\\]|\\.)*\/)|(\w[\w\d\._]*)|(([^\(\)\w\s'"\/\\]|\\.)+)/g;
    //var reg3=/(\s*(rule)\s*(\{((('([^'\\]|\\.)*')|("([^"\\]|\\.)*")|(\/([^\/\\]|\\.)*\/)|(([^'"\/\\]|\\.)*?))*?)\}))/g; // 4
    var reg1=/(\s*(\w+)\s*(\(((('([^'\\]|\\.)*')|("([^"\\]|\\.)*")|(\/([^\/\\]|\\.)*\/)|(([^'"\)\(\/\\]|\\.)*))*)\))\s*(;|$)\s*)|(\s*(;|$))/g; // 2, 4, 14, 15=Err
    var reg2=/(\()|(\))|('([^'\\]|\\.)*')|("([^"\\]|\\.)*")|(\/([^\/\\]|\\.)*\/)|(\w[\w\d\._]*)|(([^\(\)\w\s'"\/\\]|\\.)+)/g;
    var reg3=/(\s*(rule)\s*(\{((('([^'\\]|\\.)*')|("([^"\\]|\\.)*")|(\/([^\/\\]|\\.)*\/)|(([^'"\)\(\/\\]|\\.)*))*)\}))/g; // 4
    var allTests=[], actionTests={}, namedTests={};
    var results={};
    iterReader=function(arr){
      var pos=-1; l=arr.length;
      function current(){ return arr[pos]; }
      function next(){pos+=1; return current();}
      function hasMore(){return (pos+1)<l;}
      function lookAHead(){return arr[pos+1];}
      return {current:current, next:next, hasMore:hasMore, lookAHead:lookAHead};
    };
    isOkToX=function(x){
      var e=false, r;
      hideAllMessages();
      results[x]=[];
      $.each(actionTests[x]||[], function(c,f){
        r=f();
        if(!!r){results[x].push(f);}
        e|=r;
      });
      return !e;
    };
    hideAllMessages=function(){$(".validation-err-msg").hide();};
    testTest=function(name){
        var test=namedTests[name];
        if(test && test._testFunc){
            try{
                return !test._testFunc();
            }catch(e){
            }
        }
        return false;
    };
    getExpr=function(reader){
      // tests: !=str, =str, /regex/, !/regex/, empty, notEmpty, email, date, [>1], (), ex1 AND ex2, ex1 OR ex2,
      var v=reader.next(), expr="", getNumber;
      var vl=v.toLowerCase();
      getNumber=function(){
          var v, n = NaN;
          v=reader.next();
          if(v=="("){v=reader.next();if(reader.lookAHead()==")")reader.next();}
          return v*1;
      }
      if(vl=="email"){v="/.+?\\@.+\\..+?/";}
      else if(vl=="empty"){expr="(v=='')";}
      else if(vl=="yyyy"){    v="/^[12]\\d{3}$/";}
      else if(vl=="yyyymm"){  v="/^[12]\\d{3}([\\/\\\\\\-](0?[1-9]|1[012])|((0[1-9])|(1[012])))$/";}
      else if(vl=="yyyymmdd"){v="/^[12]\\d{3}([\\/\\\\\\-](0?[1-9]|1[012])|((0[1-9])|(1[012])))([\\/\\\\\\-](0?[1-9]|[12]\\d|3[01])|((0[1-9])|[12]\\d|(3[01])))$/";}
      else if(vl=="leneq" || vl=="lengtheq"){
          var n = getNumber();
          if(isNaN(n)){
             // Error
             return "";
          }
          v="/^.{"+n+"}$/"; }
      else if(vl=="lengt" || vl=="lengthgt"){
          var n = getNumber();
          if(isNaN(n)){ 
            // Error
            return "";
          }
          v="/^.{"+(n+1)+",}$/"; }
      else if(vl=="lenlt" || vl=="lengthlt"){
          var n = getNumber();
          if(isNaN(n)){ 
            // Error
            return "";
          }
          v="/^.{0,"+(n-1)+"}$/"; }
      if(vl=="notempty"){expr="(v!='')";}
      else if(vl=="checked"){expr='target.attr("checked")';}
      else if(vl=="notchecked"){expr='!target.attr("checked")';}
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
    };
    getWhen=function(reader){
        var d, action, target;
        if(!reader.hasMore()) return null;
        d=reader.next();
        while(d=="," || d==";")d=reader.next();
        if(!d) return null;
        if(d[0]=="'" || d[0]=='"'){
            target=$(eval(d));
            if(reader.lookAHead()=="."){reader.next();d=reader.next();}
            else{
                return {};
                // ERROR
            }
        }
        action = d.toLowerCase();
        if(action.substr(0,2)=="on")action=action.substr(2);
        while(reader.hasMore()){
            // read upto the next , or ;
            d=reader.next();
            if(d=="," || d==";") break;
        }
        return {action:action, target:target};
    };
    setup=function(ctx, onLoadTest){
        var rule, match;
        var m, w, va, f, a, t;
        var validationsFor={}, validationsForClass={};
        //var matchQuotedStr = '("([^"\\]|\\.)*")';     // continues matching until closing (unescaped) quote
        var vLabels=ctx.find("label.validation-err-msg");
        $(".validation-err-msg").hide();
        //value="for('dc:title');test(notEmpty);when(onChange)"
        rule=function(v){
            var dict={};
            match=function(){
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
            };
            v.replace(reg1, match);
            f=dict["for"];
            dict["when"]=(dict["when"]||"");
            if(f){
                a = validationsFor[f];
                if(!a) a=validationsFor[f]=[];
                a.push(dict);
                if(dict["name"]){
                    namedTests[dict["name"]]=dict;
                }else if(!namedTests[f]){   // default name is 'for' id
                    namedTests[f]=dict;
                }
            }
            f=dict["forclass"];
            if(f){
                a = validationsForClass[f];
                if(!a) a=validationsForClass[f]=[];
                a.push(dict);
                if(dict["name"]){
                    namedTests[dict["name"]]=dict;
                } // no default name
            }
            allTests.push(dict);
        };
        ctx.find(".validation-rule").each(function(c, v){
            v = $(v).val() || $(v).text();
            rule(v);
        });
        ctx.find(".validation-rules").each(function(c, v){
            v = $(v).text();
            v.replace(reg3, function(){ 
                var v=arguments[4]; // from reg3 match
                if(v)rule(v);
            });
        });
        $.each(validationsFor, function(f, l){
            var target = $(document.getElementById(f));
            var reader, getValue, showValidationMessage;
            var testStr, func;
            if(true){
                var vLabel=vLabels.filter("[for="+f+"]");
                getValue=function(){ return target.val(); };
                showValidationMessage=function(show){
                    show?vLabel.show():vLabel.hide();
                    return show;
                };
                $.each(l, function(c, d){
                    testStr="";
                    if(d.test){
                      reader = iterReader(d.test.match(reg2));
                      testStr = getExpr(reader);
                      try{
                        func="func=function(){var v=getValue();"+
                            "return showValidationMessage(!("+testStr+"));};";
                        eval(func);
                        func.x=d;
                        d._testFunc=func;
                        d.getValue=getValue;
                        d.target=target;
                        d.showValidationMessage=showValidationMessage;
                        d.vLabel=vLabel;
                      } catch(e){
                        alert(e + ", func="+func);
                      }
                      //if(onLoadTest){func();}
                      m = d.when.match(reg2);
                      if(m){
                        reader = iterReader(m);
                        while(!!(w=getWhen(reader))){
                          w.target = w.target||target;
                          w.target.bind(w.action, function(){func();return true;});
                          if(!actionTests[w.action]) actionTests[w.action]=[];
                          actionTests[w.action].push(func);
                        }
                      }
                    }
                });
            }
        });
        // forClass(test)
        $.each(validationsForClass, function(fc, l){
            $.each(l, function(c, d){
              var cls, m, reader, testStr, testCode;
              cls = d.forclass;
              if(d.test){
                reader = iterReader(d.test.match(reg2));
                testStr = getExpr(reader);
                testCode="showValidationMessage(!("+testStr+"));";
                m = d.when.match(reg2);
                if(m){
                  reader = iterReader(m);
                  while(!!(w=getWhen(reader))){
                    var checkFunc=function(){
                      var target, id, v, vLabel, showValidationMessage;
                      target = $(this);
                      id = target.attr("id");
                      v = target.val();
                      vLabel=ctx.find("label.validation-err-msg[for="+id+"]");
                      showValidationMessage=function(show){
                          show?vLabel.show():vLabel.hide(); return show;
                      };
                      try{
                          eval(testCode);
                      }catch(e){
                          alert("ERROR executing testCode: " +e.message);
                      }
                    }
                    ctx.find("."+cls).die().live(w.action, checkFunc);
                  }
                }
              }
            });
        });
    };
    return {
      setup:setup,
      test:function(){},
      isOkToSave:function(){return isOkToX("save");},
      isOkToSubmit:function(){return isOkToX("submit");},
      allTests:allTests,
      actionTests:actionTests,
      namedTests:namedTests,
      results:results,
      hideAllMessages:hideAllMessages,
      parseErrors:{}
    };
  };

  function helpWidget(e){
    var helpContent, showText, hideText, url;
    var showLink, hideLink, doNext;
    var show, hide;
    helpContent = $("#" + e.dataset("help-content-id"));
    helpContent.hide();
    url=e.dataset("help-content-url");
    showLink = e.find(".helpWidget-show");
    hideLink = e.find(".helpWidget-hide").hide();
    show=function(){
        if($.trim(helpContent.text())=="" && url){
            helpContent.text("Loading help. Please wait...");
            helpContent.load(url);
        }
        helpContent.slideDown();
        showLink.hide(); hideLink.show();
        doNext=hide;
    };
    hide=function(){
        helpContent.slideUp();
        showLink.show(); hideLink.hide();
        doNext=show;
    };
    doNext=show;
    e.click(function(){
        doNext();
    });
  }

  function showHideCheck(e){
    var s, t, p=e;
    try{
        s=e.dataset("target-nearest-selector");
        // find the nearest matching element
        while(p[0].tagName!="BODY" && p.size()){
            t = p.find(s);
            if(t.size()){
                t.toggle(e.attr("checked"));
                e.change(function(){
                    t.toggle(e.attr("checked"));
                });
                e.click(function(){     // required for IE7
                    t.toggle(e.attr("checked"));
                });
                break;
            }
            p = p.parent();
        }
    }catch(e){
        alert("Error in showHideCheck() - "+e.message);
    }
  }

  function listInput(c, i){
      var liSect, count, tmp, visibleItems, displayRowTemplate, displaySelector;
      var add, del, getDelFuncFor, reorder, addUniqueOnly=false;
      var maxSize, minSize, addButton, addButtonDisableTest;
      var xfind, regFirst0, regLast;
      regFirst0 = /\.0(?=\.|$)/;
      // find a .digit. that is not followed by a .digit. -- not followed= (?!.*\.\d+(?=(\.|$)))
      regLast = /\.\d+(?=(\.|$))(?!.*\.\d+(?=(\.|$)))/;
      liSect = $(i);
      xfind = function(selector){
        // find all selector(ed) elements but not ones that are in a sub '.input-list'
        // return liSect.find(selector);
        return liSect.find(selector).not(liSect.find(".input-list").find(selector));
      };
      
      maxSize = liSect.dataset("max-size")*1;
      if(isNaN(maxSize)){
          maxSize=100;
      }
      if(maxSize<1)maxSize=1;
      minSize = liSect.dataset("min-size")*1;
      if(isNaN(minSize)){
          minSize=0;
      }
      if(minSize>maxSize)minSize=maxSize;
      if(liSect.hasClass("sortable")){
        //xfind("tbody").first().sortable({
        liSect.sortable({
          items:".sortable-item",
          update:function(e, ui){ reorder(); }
        });
      }
      addButton=xfind(".add-another-item, .item-add");
      liSect[0].addButton = addButton;
      addButtonDisableTest=function(){
          visibleItems = xfind(displaySelector+".count-this");
          count = visibleItems.size();
          if(count>=maxSize){addButton.attr("disabled", true);}
      }
      addButton.bind("disableTest", addButtonDisableTest);
      del=function(e){
        e.remove();
        addButton.attr("disabled", false);
        addButton.trigger("disableTest");
        reorder();
        return false;
      }
      getDelFuncFor=function(e){
          return function(){return del(e);};
      }
      var _add=function(e, force){
      }
// check all variable names
      if(xfind(".item-display").size()){
        if(xfind(".item-input-display").size()){
          alert("Error: .input-list can not have both 'item-display' and 'item-input-display' table row classes");
          return;
        }
        // For handling 'item-display' (where there is a separate/special row for handling the display of added items)
        //    Note: if there is an 'item-display' row then it is expected that there will also be an
        //        'item-input' row as well an 'item-add' button/link
        displaySelector = ".item-display";
        tmp=xfind(displaySelector).hide();
        displayRowTemplate=tmp.eq(0);
        contentDisable(displayRowTemplate);
        add=function(e, force){
          // get item value(s) & validate (if no validation just test for is not empty)
          var values=[];
          var test=[];
          xfind(".item-input input[type=text]").each(function(c, i){
            values[c]=[$.trim($(i).val()), i.id];
            test[c]=values[c][0];
            if($(i).parents(".data-source-drop-down").size()==0)$(i).val("");   // reset
          }).eq(0).focus();
          //
          tmp = displayRowTemplate.clone().show().addClass("count-this");
          visibleItems = xfind(displaySelector+".count-this");
          if(!force){
              if(!any(values, function(_, v){ return v[0]!==""; })) return;
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
          }
          count = visibleItems.size()+1;
          tmp.find("*[id]").each(function(c, i){
              i.id = i.id.replace(regFirst0, "."+count);
          });
          tmp.find(".item-display-item").each(function(c, i){
            var id = values[c][1].replace(regFirst0, "."+count);
            $(i).append("<input type='text' id='"+id+"' value='"+values[c][0]+
                        "' readonly='readonly' size='40' />");
          });
          tmp.find(".sort-number").text(count);
          xfind(displaySelector).last().after(tmp);
          visibleItems.find(".delete-item").show();
          tmp.find(".delete-item").click(getDelFuncFor(tmp));
          addButton.trigger("disableTest");
          if(count>=maxSize){addButton.attr("disabled", true);}
          contentSetup(tmp);
        }
        addButton.click(add);
        addButton[0].forceAdd = function(){add(null, true);};
        addButton[0].add = add;
        addUniqueOnly = addButton.hasClass("add-unique-only");
        xfind(".item-input input[type=text]").last().keypress(function(e){
          if(e.which==13){
            add();
          }
        });
      }else if(xfind(".item-input-display").size()){
        // For handling 'item-input-display' type lists
        //   Note: if there is an 'item-input-display' row then it is also excepted that there
        //      will be an 'add-another-item' button or link
        displaySelector=".item-input-display";
        if(minSize==0)minSize=1;
        tmp=xfind(displaySelector).hide();
        displayRowTemplate=tmp.eq(0);
        contentDisable(displayRowTemplate);
        add=function(){
          tmp = displayRowTemplate.clone(true).show().addClass("count-this");
          visibleItems = xfind(displaySelector+".count-this");
          count = visibleItems.size()+1;
          tmp.find("*[id]").each(function(c, i){
              //$(i).addClass(i.id);
              i.id = i.id.replace(regFirst0, "."+count);
          });
          tmp.find("label[for]").each(function(c, i){
              i=$(i);
              i.attr("for", i.attr("for").replace(regFirst0, "."+count));
          });
          tmp.find(".sort-number").text(count);
          xfind(displaySelector).last().after(tmp);
          if(count<=minSize){tmp.find(".delete-item").hide();}
          else{
              visibleItems=visibleItems.add(tmp);
              //visibleItems.find(".delete-item").show();
              visibleItems.find(".delete-item").not(visibleItems.find(".input-list .delete-item")).show();
          }
          //tmp.find(".delete-item").click(getDelFuncFor(tmp));
          tmp.find(".delete-item").not(tmp.find(".input-list .delete-item")).click(getDelFuncFor(tmp));
          if(count>=maxSize){addButton.attr("disabled", true);}
          contentSetup(tmp);
        }
        for(var x=0;x<minSize;x++){add();}
        addButton.click(add);
      }

      reorder=function(){
        var xf, regFirst = /\.\d+(?=\.|$)/;
        // reorder last digit only in our direct input-list only
        visibleItems = xfind(displaySelector+".count-this");
        if(visibleItems.filter(".item-input-display").size()<=minSize){
          xfind(".item-input-display .delete-item").hide();
        }
        visibleItems.each(function(c, i){
            i=$(i);
            xf=function(selector){
                return i.find(selector).not(i.find(".input-list").find(selector));
            }
            try{
                xf("*[id]").each(function(_, i2){
                    var labels = i.find("label[for="+i2.id+"]");
                    i2.id = i2.id.replace(regLast, "."+(c+1));
                    labels.attr("for", i2.id);
                });
                // re-number the id's of sub-input-list's too
                // HACK: this currently only supports a second level list only
                // TODO: add support for any level/depths of lists.
                i.find(".input-list *[id]").each(function(_, i2){
                    i2.id = i2.id.replace(regFirst, "."+(c+1));
                });
                xf(".sort-number").text(c+1);
            }catch(e){alert(e.message)}
        });
      }
  }

  var pendingWork = {};
  var trackPendingWork = false;
  var pendingWorkAllDoneFunc = null;
  function pendingWorkStart(id){
    var workId, pendingWorkDone;
    if(trackPendingWork){
        workId = getIdNum();
        pendingWork[workId]=id;
        pendingWorkDone=function(){
            delete pendingWork[workId];
            if(pendingWorkAllDoneFunc && $.isEmptyObject(pendingWork)){
                pendingWorkAllDoneFunc();
            }
        }
    }else{ pendingWorkDone=function(){}; }
    pendingWorkDone.workId = workId;
    return pendingWorkDone;
  }

  function getJsonGetter(jsonSourceUrl, jsonStrData){
      var jsonGetter, pendingWorkDone;
      var jsonBaseUrl, jsonInitUrlId, jsonCache={}, jsonData;
      if(jsonSourceUrl){
          if(/\//.test(jsonSourceUrl)){
            jsonInitUrlId=jsonSourceUrl.split(/\/(?=[^\/]*$)/)[1];
            jsonBaseUrl=jsonSourceUrl.split(/\/(?=[^\/]*$)/)[0]+"/";  // split at the last /
          }else{
              jsonInitUrlId=jsonSourceUrl;
              jsonBaseUrl="";
          }
      }else{
          jsonBaseUrl="";
          jsonInitUrlId="";
      }
      if(jsonStrData){
          try{
              jsonData = $.parseJSON(jsonStrData);
              jsonCache=jsonData;
              jsonCache[""]=jsonData;
          }catch(e){
              alert("Not valid json! - "+e);
          }
      }
      // root or base json urlId is just an empty string e.g. ""
      jsonGetter = function(urlId, onJson){
        var j, url;
        j=jsonCache[urlId];
        if(j){
            if(false){          // false to simulate a delay
                onJson(j);
            }else{
                pendingWorkDone = pendingWorkStart(url);
                setTimeout(function(){
                        try{
                            onJson(j);
                        }catch(e){
                            alert("-onJson error: "+e.message);
                        }
                        pendingWorkDone();
                    }, 10);
            }
            return;
        }
        if(urlId){
            url=jsonBaseUrl+urlId;
        }else{
            url=jsonBaseUrl+jsonInitUrlId;
        }
        if(!/\.json$/.test(url)) url+=".json";
        pendingWorkDone = pendingWorkStart(url);
        $.getJSON(url, function(data){
            if(!urlId){
                jsonCache=data;
            }
            jsonCache[urlId]=data;
            try{
                onJson(data);
            }catch(e){
                alert("onJson error: "+e.message+"  (for url='"+url+"')");
                //alert(onJson);
            }
            pendingWorkDone();
        });
      }
      return jsonGetter;
  }
  _gjg = getJsonGetter;
  _g = {"json":[]};

  function makeSelectList(json){
      var s, o, ns=json.namespace||"", _default=json["default"], list=json.list;
      s = $("<select/>");
      if(!_default){
        s.append($("<option value=''>Please select one</option>"));
      }
      $.each(list, function(_c, i){
        if(!i)return;
        o = $("<option/>");
        o.attr("value", ns+i.id);
        if((ns+i.id)==_default) o.attr("selected", "selected");
        o.text(i.label);
        s.append(o);
      });
      return s;
  }

  function dropDownListJson(_count, e){
    var selectId=e.id, jsonGetter, onJson, select;
    e = $(e);
    if(e.dataset("done")){ return; }
    selectId=e.dataset("id") || selectId;
    jsonGetter=getJsonGetter(e.dataset("json-source-url"),
                                e.dataset("json-data"));
    onJson=function(json){
        select=makeSelectList(json);
        if(selectId) select.attr("id", selectId);
        select.val(e.dataset("value"));
        if(!select.val()){select.val($.trim(e.text()));}
        if(!select.val()){select.val(e.val());}
        e.replaceWith(select);
    }
    jsonGetter("", onJson);
    e.dataset("done", "1");
  }

  // ==============
  // Multi-dropdown selection
  // ==============
  function buildSelectList(json, parent, jsonGetter, onSelection){
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
                try{
                    // IE7 fixup - remove excess spaces (padding)
                    var p, n;
                    p=s.parent()[0];
                    n=p.childNodes[p.childNodes.length-1];
                    if(n.nodeValue==false){
                        p.removeChild(n);
                    }
                }catch(e){}
                s.remove();
            }
            removeSelects(s.next("select"));
        }
        if(child.url){
          jsonGetter(child.url, function(j){
              s.after(buildSelectList(j, child, jsonGetter, onSelection));
              s.after(" ");     // break point for IE7
              onSelection(child);
          });
        }
        onSelection(child);
      }
      s.change(onChange);
      setTimeout(onChange, 10);
      return s;
  }

  function sourceDropDown(c, dsdd){
    try{
      var ds=$(dsdd), id=dsdd.id, jsonUrl, jsonDataStr, jsonGetter;
      var selAdd, selAddNs, selAddId, selAddLabel, addButtonDisableTest;
      var lastSelectionSelectable=false, selectId;
      var onSelection, onJson, jsonDataSrc=ds.find(".json-data-source");
      if(ds.dataset("done")){ return; }
      ds.children("*:not(select)").hide();
      selectId=ds.dataset("id");
      selAdd=ds.parent().find(".selection-add");
      jsonUrl=ds.dataset("json-source-url") || ds.find(".json-data-source-url").val();
      //jsonDataStr=ds.find(".json-data-source");
      jsonDataStr=ds.dataset("json-data") || jsonDataSrc.val() || jsonDataSrc.text();
      jsonGetter=getJsonGetter(jsonUrl, jsonDataStr);

      addButtonDisableTest = function(){
        if(lastSelectionSelectable==false){
            if(/BUTTON|INPUT/.test(selAdd[0].tagName)){
                selAdd.attr("disabled", true);
            }else{
                selAdd.hide();
            }
        }
      };
      selAdd.bind("disableTest", addButtonDisableTest);
      onSelection = function(info){
        //info.namespace, info.id, info.label, info.selectable, info.parent
        while(info.selectable!==false && info.selectable!==true){
          if(info.parent) info = info.parent;
          else info.selectable=false;
        }
        lastSelectionSelectable=info.selectable;
        if(/BUTTON|INPUT/.test(selAdd[0].tagName)){
          selAdd.attr("disabled", lastSelectionSelectable?"":"disabled");
        }else{
          lastSelectionSelectable?selAdd.show():selAdd.hide();
        }
        selAdd.trigger("disableTest");
        if(lastSelectionSelectable){
          selAddNs=info.namespace; selAddId=info.id; selAddLabel=info.label;
        }else{
          selAddNs=""; selAddId=""; selAddLabel="";
        }
        ds.find(".selection-add-id").val(selAddId);
        ds.find(".selection-add-label").val(selAddLabel);
        selAdd.find(".selection-add-id").text(selAddId);
        selAdd.find(".selection-add-label").text(selAddLabel);
      };
      onJson = function(json){
        try{
            // OK now build the select-option
            var o = buildSelectList(json, {}, jsonGetter, onSelection);
            if(selectId){
                o.attr("id", selectId);
                selectId=null;
            }
            ds.append(o);
            ds.append(" "); // line break point of IE7
        }catch(e){
            alert("Error in sourceDropDown onJson function - " + e.message);
            throw e;
        }
      };
      jsonGetter("", onJson);
      ds.dataset("done", 1);
    }catch(e){
        alert("Error in sourceDropDown() - "+e.message);
    }
  }

  function formWidget(ctx, globalObject, validator){
      // functions
      var addListener, removeListener, removeListeners, raiseEvents
      var onSubmit, onSave, onRestore, onReset, hasChanges, lastData={};
      var submit, save, submitSave, getFormData, restore
      var reset, setupFileUploader, getFileUploadInfo, createFileSubmitter, init
      // variables
      var widgetForm={};
      var listeners={};
      var ctxInputs;

      addListener=function(name, func){
          var l;
          l=listeners[name];
          if(!l){
              l = [];
              listeners[name]=l;
          }
          l.push(func);
      };
      removeListener=function(name, func){
          var l, i;
          l=listeners[name]||[];
          i=$.inArray(name, l);
          if(i>-1)l.splice(i, 1);
      };
      removeListeners=function(name){
          delete listeners[name];
      };
      raiseEvents=function(name){
          var l=listeners[name]||[];
          for(var k in l){
              var f = l[k];
              try{
                  if(f()===false) return false; // cancel event
              }catch(e){}
          }
      };

      onSubmit=function(){
        if(raiseEvents("onSubmit")===false){
            return false;
        }
        submit();
        return true;
      };
      onSave=function(){
        if(raiseEvents("onSave")===false){
            return false;
        }
        save();
        return true;
      };
      onRestore=function(data){
        if(raiseEvents("onRestore")==false){
            return false;
        }
        //messageBox(JSON.stringify(data))
        restore(data);
        lastData = getFormData();
        return true;
      };
      onReset=function(data){
        if(raiseEvents("onReset")==false){
            return false;
        }
        reset(data);
        return true;
      };

      hasChanges=function(){
          var cData, u, lv;
          cData=getFormData();
          return any(cData, function(k,v){
              lv=lastData[k];
              if(v===lv)return false;
              if(lv===u || v===u)return true;   // if either is undefined
              return v.toString()!=lv.toString();
          });
      };

      submit=function(){
          submitSave("submit");
      };

      save=function(){
          submitSave("save");
      };

      submitSave=function(stype){
        var data, url;
        var xPreFunc, xFunc, xResultFunc;
        var replaceFunc, completed;
        replaceFunc=function(s){
            s = s.replace(/[{}()]/g, "");   // make it safe - no function calls
            return eval(s);
        };
        if(globalObject){
            xPreFunc = globalObject[ctx.dataset("pre-"+stype+"-func")];
            xFunc = globalObject[ctx.dataset(stype+"-func")];
            xResultFunc = globalObject[ctx.dataset(stype+"-result-func")];
        }
        if(callIfFunction(xPreFunc, widgetForm)===false){
            callIfFunction(xResultFunc, widgetForm, {error:"canceled by pre-"+stype+"-func"});
            return false;
        }
        data = getFormData();
        completed=function(data){
            if(typeof(data)=="string"){
                var dataStr=data;
                try{
                    data = JSON.parse(data);
                }catch(e){
                    data = {error:e};
                }
            }
            callIfFunction(xResultFunc, widgetForm, data);
            if(data.error || !data.ok){
                if(!data.ok && !data.error) data.error="Failed to receive an 'ok'!";
                messageBox("Failed to "+stype+"! (error='"+data.error+"') data='"+dataStr+"'");
            }else{
                if(stype=="save"){
                    ctx.findx(".saved-result").text("Saved OK").
                        css("color", "green").show().fadeOut(4000);
                }else if(stype=="submit"){
                    ctx.findx(".submit-result").text("Submitted OK").
                        css("color", "green").show().fadeOut(4000);
                }
                lastData = getFormData();
            }
        };
        if(data.title===null)data.title=data["dc:title"];
        if(data.description===null)data.description=data["dc:description"];
        if(callIfFunction(xFunc, widgetForm, data)===false){
            callIfFunction(xResultFunc, widgetForm, {error:"canceled by "+stype+"-func"});
            return false;
        }
        url = ctx.dataset(stype+"-url") || ctx.findx(".form-fields-"+stype+"-url").val();
        //logInfo(stype+" url="+url);
        if(url){
            url = url.replace(/{[^}]+}/g, replaceFunc);
            if(widgetForm.hasFileUpload){
                var elems=[], h=$("<input type='text' name='json' />");
                var fileSubmitter = createFileSubmitter();
                h.val(JSON.stringify(data));
                elems.push(h[0]);
                $.each(data, function(k, v){
                    h=$("<input type='text' />");
                    h.attr("name", k); h.val(v);
                    elems.push(h[0]);
                });
                ctx.findx("input[type=file]").each(function(c, f){
                    elems.push(f);
                });
                fileSubmitter.submit(url, elems, completed);
            }else{
                // data.json = JSON.stringify(data);
                $.ajax({type:"POST", url:url, data:data,
                    success:completed,
                    error:function(xhr, status, e){ completed({error:"status='"+status+"'"}); },
                    dataType:"json"
                });
            }
        }else{
            completed({});
        }
      };

      getFormData=function(){
        var data={}, s, v, e, formFields;
        var getValue, getXValue;
        var regFirst0=/\.0(?=\.|$)/;
        formFields = ctx.dataset("form-fields") || ctx.findx(".form-fields").val()
        formFields += ","+ (ctx.dataset("form-fields-readonly") || ctx.findx(".form-fields-readonly").val());
        formFields = $.grep(formFields.split(/[\s,]+/),
                                function(i){return /\S/.test(i)});
        getValue=function(i){
          e = getById(i);
          if(e.size()==0) e=ctxInputs.filter("[name="+i+"]");
          if(e.size()==0){return null;}
          v = e.val();
          if(e.attr("type")==="checkbox"){
            if(!e.attr("checked")){v="";}
          } else if(e.attr("type")==="radio"){
            v = e.filter(":checked").val();
          }
          return v;
        };
        getXValue=function(i){
            var id, count=1;
            while(true){
             try{
              id = i.replace(regFirst0, "."+count);
              if(regFirst0.test(id)){
                  if(getXValue(id)==1){
                      // stop counting when x.1 does not get a value
                      return count;
                  }
              }else{
                v=getValue(id);
                if(v===null) return count;
                data[id]=v;
              }
              count+=1;
             }catch(e){
                 alert(e);
                 throw e;
             }
            }
        }
        $.each(formFields, function(c, i){
            if(/\.0+(\.|$)/.test(i)){
                getXValue(i);
            }else{
                v=getValue(i);
                data[i]=v;
            }
        });
        if(data.metaList=="[]" || data.metaDataList=="[]"){
            s = [];
            $.each(data, function(k, v){if(k!="metaList")s.push(k);});
            if(data.metaList=="[]"){ data.metaList=s; }
            if(data.metaDataList=="[]"){ data.metaDataList=s; }
        }
        return data;
      };

      restore=function(data){
          var keys=[], skeys=[], input, t, formFields, regAll, regLast;
          regAll = /\.\d+(?=(\.|$))/;
          // find a .digit. that is not followed by a .digit. -- not followed= (?!.*\.\d+(?=(\.|$)))
          regLast = /\.\d+(?=(\.|$))(?!.*\.\d+(?=(\.|$)))/;
          ctxInputs = ctx.findx("input, textarea, select");
          formFields = ctx.dataset("form-fields") || ctx.findx(".form-fields:first").val();
          formFields = $.grep(formFields.split(/[\s,]+/),
                                function(i){return /\S/.test(i)});
          $.each(data, function(k, v){keys.push(k);});
          keys.sort();
          skeys = $.grep(keys, function(i){return /\.\d+(\.|$)/.test(i);}, true);
          _gc = ctxInputs;
          $.each(skeys, function(c, v){
              if($.inArray(v, formFields)!=-1){
                  ctxInputs.filter("[id="+v+"]").val(data[v]);
              }
          });
          // list items
          skeys = $.grep(keys, function(i){return /\.\d+(\.|$)/.test(i);});
          $.each(skeys, function(c, v){
              var k, il, addButton;
              k = v.replace(regLast, ".0");
              try{
                  if($.inArray(k.replace(regAll,".0"), formFields)!=-1){
                      input = ctxInputs.filter("[id="+v+"]");
                      if(input.size()==0){
                        input = ctxInputs.filter("[id="+k+"]");
                        il=input.parents(".input-list").first();
                        if(il.size()){
                            addButton=il[0].addButton;
                            if(addButton.size()){
                                if(addButton[0].forceAdd){
                                    addButton[0].forceAdd();
                                }else{
                                    addButton.click();
                                }
                            }
                        }
                        // update inputs - this could be done better
                        ctxInputs = ctx.findx("input, textarea, select");
                        input = ctxInputs.filter("[id="+v+"]");
                        if(input.size()==0){
                            alert("id '"+v+"' not found!");
                        }
                      }else{
                          //alert("found '"+v+"' ok");
                      }
                      if(input.attr("type")=="checkbox"){
                        input.attr("checked", !!data[v]);
                        input.change();
                      }else{
                        input.val(data[v]);
                      }
                  }
              }catch(e){
                  alert("Error in restore() - "+e.message);
              }
          });
      };

      reset=function(data){
          if(!data)data={};
          //
      };

      setupFileUploader=function(fileUploadSections, onChange){
        if(!fileUploadSections) fileUploadSections=ctx.findx(".file-upload-section");
        fileUploadSections.each(function(c, e){
            var handleFileDrop;
            var ifile, fileUploadSection;
            fileUploadSection = $(e);
            ifile = fileUploadSection.find("input[type=file]");
            if(!onChange){
                onChange=function(fileInfo, fileUploadSection){
                    var s;
                    s=["<span>", fileInfo.typeName, ": ", fileInfo.name, " (",
                        fileInfo.kSize, "k) </span>"];
                    s = $(s.join(""));
                    if(fileInfo.createImage) s.append(fileInfo.createImage());
                    fileUploadSection.find(".file-upload-info").html(s);
                };
            }
            ifile.change(function(e){
                var fileInfo=getFileUploadInfo(e.target.files[0]);
                onChange(fileInfo, fileUploadSection);
            });
            fileUploadSection.bind("dragover", function(ev){
                if(ev.target.tagName=="INPUT"){ return true; }
                ev.stopPropagation(); ev.preventDefault();
            });
            handleFileDrop=function(ev){
                var file, fileInfo;
                if(ev.target.tagName=="INPUT"){ return true; }
                ev.stopPropagation(); ev.preventDefault();
                file=ev.dataTransfer.files[0];
                fileInfo=getFileUploadInfo(file);
                onChange(fileInfo, fileUploadSection);
                ifile.val("");      // reset
                //gDroppedFile=file;
                //ifile[0].files[0]=file;
                return;
            }
            //fileUploadSection.bind("drop", handleFileDrop);  // Note: binding to the wrong 'drop' event!
            if(fileUploadSection[0].addEventListener){
                fileUploadSection[0].addEventListener("drop", handleFileDrop, false);
            }
        });
      };

      getFileUploadInfo=function(file){
        var fileInfo = {};
        fileInfo.file = file;
        fileInfo.size = file.size;
        fileInfo.kSize = parseInt(file.size/1024+0.5);
        fileInfo.type = file.type;
        fileInfo.name = file.name;
        try{
            fileInfo.encodedData=file.getAsDataURL();
        }catch(e){ }
        if(file.type.search("image/")==0){
            fileInfo.image=true;
            fileInfo.typeName = "Image";
            if(fileInfo.encodedData){
                fileInfo.createImage=function(){
                    var i;
                    i=$("<img class='thumbnail' style='vertical-align:middle;'/>");
                    i.attr("src", fileInfo.encodedData);
                    i.attr("title", fileInfo.name);
                    return i;
                };
            }
        }else if(file.type.match("video|flash")){
            fileInfo.video=true;
            fileInfo.typeName = "Video";
        }else if(file.type.match("text|pdf|doc|soffice|rdf|txt|opendocument")){
            fileInfo.document=true;
            fileInfo.typeName = "Document";
        }else{
            fileInfo.typeName = "File";
        }
        return fileInfo;
      };

      createFileSubmitter=function(){
          var iframe, getBody, submit;
          iframe = $("<iframe id='upload-iframe' style='display:none; height:8ex; width:80em; border:1px solid red;'/>");
          $("body").append(iframe);
          if(iframe[0].contentDocument){
              getBody=function(){ return $(iframe[0].contentDocument.body); };
          }else{
              getBody=function(){ return $(iframe[0].contentWindow.document.body); };
          }
          submit=function(url, elems, callback){
              // callback(resultText, iframeBodyElement);
              var form = $("<form method='POST' enctype='multipart/form-data' />");
              iframe.unbind();
              if(!url)url=window.location.href+"";
              form.attr("action", url);
              $.each(elems, function(c, e){
                  var c=$(e).clone();
                  if(c.attr("name")===""){c.attr("name", e.id);}
                  form.append(c);
              });
              getBody().append(form);
              setTimeout(function(){
                  iframe.load(function(){
                      var ibody=getBody();
                      callback(ibody.text(), ibody);
                  });
                  form.submit();
              }, 10);
          };
          // submit(url, elems, callback)
          //    url = url to sumbit to
          //    elems = 'input' elements to be submitted (cloned)
          //    callback = function(textResult, iframeBody)
          return {submit:submit, iframe:iframe, getBody:getBody};
      };

      init=function(_ctx, validator){
        var id;
        if(!_ctx)_ctx=$("body");
        ctx = _ctx;
        id=ctx.attr("id");
        widgetForm.id=id;
        ctx.findx=function(selector){
            // find all selector(ed) elements but not ones that are in a subform
            var nsel=(","+selector).split(",").join(", ."+formClassName+" ");
            return ctx.find(selector).not(ctx.find(nsel));
        };
        ctxInputs=ctx.findx("input, textarea, select");
        //
        widgetForm.hasFileUpload= (ctx.findx("input[type=file]").size()>0);
        if(widgetForm.hasFileUpload){ setupFileUploader(); }
        if(validator){
            var v = validator();
            v.setup(ctx);
            widgetForm.validator = v;
            addListener("onSave", v.isOkToSave);
            addListener("onSubmit", v.isOkToSubmit);
        }
        ctx.findx(".form-fields-save").click(onSave);
        ctx.findx(".form-fields-submit").click(onSubmit);
        ctx.findx(".form-fields-restore").click(onRestore);
        ctx.findx(".form-fields-reset").click(onReset);
        widgetForm.ctx = ctx;
      };

      widgetForm.submit=onSubmit;
      widgetForm.save=onSave;
      widgetForm.restore=onRestore;
      widgetForm.reset=onReset;
      widgetForm.hasChanges=hasChanges;
      widgetForm.addListener=addListener;
      widgetForm.removeListener=removeListener;
      widgetForm.removeListeners=removeListeners;
      widgetForm._createFileSubmitter=createFileSubmitter;   // for testing only
      widgetForm._getFormData=getFormData;

      if(ctx) init(ctx, validator);
      return widgetForm;
  }


    function datepickerOnClose(dateText, inst){
        var month = $("#ui-datepicker-div .ui-datepicker-month :selected").val();
        var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
        if(!month)month=0;
        $(this).datepicker('setDate', new Date(year, month, 1));
        $(this).blur();
    }
    function datepickerBeforeShow(input, inst){
        inst = $(inst.input);
        if(inst.hasClass("dateMY") || inst.hasClass("dateYM") || inst.hasClass("dateY")){
            setTimeout(function(){
                $(".ui-datepicker-calendar").remove();
                $(".ui-datepicker-current").remove();
                $(".ui-datepicker-close").text("OK");
                if(inst.hasClass("dateY")) $(".ui-datepicker-month").remove();
            }, 10);
        }
    }
    function contentSetup(ctx, completedCallback){
        //
      try{
        ctx.find(".helpWidget").each(function(c, e){
            helpWidget($(e));
        });
        ctx.find(".show-hide-widget").each(function(c, e){
            showHideCheck($(e));
        });
        // ==============
        // Date inputs
        // ==============
        ctx.find("input.dateYMD, input.date").datepicker({
            dateFormat:"yy-mm-dd", changeMonth:true, changeYear:true, showButtonPanel:false
        });
        ctx.find('input.dateYM').datepicker({
            changeMonth: true, changeYear: true, showButtonPanel: true, dateFormat: 'yy-mm',
            onClose: datepickerOnClose,
            beforeShow:datepickerBeforeShow,
            onChangeMonthYear:function(year, month, inst){ datepickerBeforeShow(null, inst); },
            onSelect:function(dateText, inst){}
        });
        ctx.find('input.dateMMY').datepicker({
            changeMonth: true, changeYear: true, showButtonPanel: true, dateFormat: 'MM yy',
            onClose: datepickerOnClose,
            beforeShow:datepickerBeforeShow,
            onChangeMonthYear:function(year, month, inst){ datepickerBeforeShow(null, inst); },
            onSelect:function(dateText, inst){}
        });
        ctx.find('input.dateY').datepicker({
            changeMonth: false, changeYear: true, showButtonPanel: true, dateFormat: 'yy',
            //onClose: datepickerOnClose,
            onClose: function(dateText){  // for IE7
                var year = $("#ui-datepicker-div .ui-datepicker-year :selected").val();
                $(this).val(year);
            },
            beforeShow:datepickerBeforeShow,
            onChangeMonthYear:function(year, month, inst){ datepickerBeforeShow(null, inst); },
            onSelect:function(dateText, inst){}
        });
        //
        trackPendingWork = true;
        pendingWorkAllDoneFunc=function(){
            //alert("all pending work (setup work) done!");
            trackPendingWork = false;
            // ==============
            // Simple (text) list input type
            // ==============
            ctx.find(".input-list").not(ctx.find(".input-list .input-list")).each(listInput);
            if(completedCallback){
                completedCallback();
            }
        };

        ctx.find(".drop-down-list-json").each(dropDownListJson);
        // ==============
        // Multi-dropdown selection
        // ==============
        //alert("sourceDropDown");
        ctx.find(".data-source-drop-down").each(sourceDropDown);
        if($.isEmptyObject(pendingWork)){
            // there is no pendingWork to wait for!
            pendingWorkAllDoneFunc();
        }
        gPendingWork=pendingWork;
      }catch(e){
          alert("Error in contentSetup() - "+e.message);
      }
    }

    function contentDisable(ctx){
        ctx.find("input").filter(".dateYMD, .date, .dateYM, .dateMMY, .dateY").datepicker("destroy");
    }

    function contentLoaded(completedCallback){
        //alert("contentLoaded");
        contentSetup($("body"), function(){
            $("."+formClassName).each(function(c, e){
                try{
                    var widgetForm=formWidget($(e), 
                                            widgets.globalObject,
                                            widgets.validator);
                    widgets.forms.push(widgetForm);
                    widgets.formsById[widgetForm.id] = widgetForm;
                }catch(e){
                    alert("Error: "+e);
                }
            });
            if(completedCallback){
                completedCallback();
            }
        });
    }

    widgets.forms=[];
    widgets.formsById={};
    widgets.messageBox = messageBox;
    widgets.changeToTabLayout = changeToTabLayout;
    widgets.contentLoaded = contentLoaded;
    widgets.validator = validator;
    widgets.formWidget = formWidget;
})(jQuery);







