// IMS Viewer

$(function(){
    var jQ=jQuery;
    var myUrl = window.location.href.split("#")[0];
    var url = myUrl.replace("/detail/", "/jsonIms/");
    //==  http://localhost:9997/portal/default/download/3be1c9f2-e80e-4cef-a09a-f24f5e3842a3/skin%2Fusq.001.css
    var dUrl = myUrl.replace("/detail/", "/download/");
    var k1 = "<link href='"+ dUrl + "/skin/usq.001.css" + "' rel='stylesheet' type='text/css'/>";
    var k2 = "<link rel='stylesheet' href='"+dUrl + "/skin/default.css"+"'/>";
    jQ("head").append(k1);
    jQ("head").append(k2);
    //==
    function previewDataCallback(data){
        var h=jQ(data);
        var d=jQ("div.content-preview-inline>div");
        d.html(h.find(".body"));
        d.prepend(h.find("div.page-toc"));
        d.prepend(h.find("h1.content-title"));
        gFixLinks("div.content-preview-inline a", "href");
        gFixLinks("div.content-preview-inline img", "src");
        //
        d.css("background-color", "white");
        d.css("padding", "0.7em");
        d.css("border", "1px solid #cccccc");
        d.parent().parent().css("padding", "0.5em");
    }
    function getDocument(id){
        var url = myUrl.replace("/detail/", "/download/");
        url += "/" + id;
        //alert(url)
        jQ.get(url, {}, previewDataCallback, "text");
        jQ(".content-preview-inline>div").replaceWith("<div><a name='"+id+"'>loading..</a></div>");
        window.location.href = myUrl + "#" + id;
    }
    function createTree(nodes) {
        opts = { data: {type:"json", opts:{"static":nodes}},
                ui: {dots:false},
                types: {"default":{draggable:false}},
                rules: {},
                lang: {},
                callback: {onselect:
                        function(node, t){
                            var id=jQ(node).attr("id");
                            getDocument(id);
                        }
                }
            }
        var d = jQ("<div class='box'><h2>TOC</h2><div class='block'></div></div>");
        jQ("#metadata").before(d);
        d.find("div").tree(opts);
    }
    function callback(data){
        if(data.title){
            //alert(data.title);
            createTree(data.nodes)
            jQ(".content-preview-inline").append("<div/>");
            var id = window.location.href.split("#")[1];
            if(id){
                getDocument(id);
            }
        }
    }
    jQ.get(url, {}, callback, "json");
});

