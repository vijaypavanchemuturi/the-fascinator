var jQ=jQuery;


jQ(function(){
    var rvt = rvtFactory(jQ);
    rvt.getManifestJson();
});

var rvtFactory = function(jQ){
    var rvt = {};
    var pages = {};     // cache
    var homePage;
    var nodes;
    
    rvt.contentSelector = "#content";
    rvt.titleSelector = "#contents-title";
    rvt.tocSelector = "#toc .toc";
    rvt.updateContent = function(content) {
        jQ(rvt.contentSelector).html(content);
        if(window.location.hash) setTimeout(function(){window.location.hash=window.location.hash;}, 500);
    };
    rvt.setTitle = function(title) {
        jQ(rvt.titleSelector).text(title);
    };
    rvt.getManifestJson = function(jsonFilename) {
        if(!jsonFilename) jsonFilename="manifest.json";
        jQ.get(jsonFilename, {}, processManifestJson, "json");       // "imsmanifest.json"
    };
    rvt.displayTOC = function(nodes) {
        function getList(data){
            var items = [];
            var list = "";
            data.forEach(function(i){
                // title, relPath, visible
                if(i.visible!==false) {
                    var href = (i.relPath || i.attributes.id);
                    var title = i.title || i.data;
                    var children = "";
                    if(i.children) children=getList(i.children);
                    if(href.substring(href.length-4)===".htm") href="#"+href;
                    items.push("<li><a href='"+href+"'>"+title+"</a>"+children+"</li>");
                }
            });
            if(items.length) { list = "<ul>\n" + items.join("\n") + "</ul>\n"; }
            return list;
        }
        jQ(rvt.tocSelector).html(getList(nodes));
        function onLocationChange(location, data){
            hash = data.hash;
            hash = hash.split("#")[0];
            jQ("a").removeClass("link-selected");
            jQ("a[href='#"+hash+"']").addClass("link-selected");
        }
        jQ(window.location).change(onLocationChange);
    };
    rvt.loadingMessage = "Loading. Please wait...";

    function processManifestJson(data) {
        rvt.setTitle(data.title);
        // Note: homePage & nodes are package level variables
        homePage = data.homePage;
        nodes = data.toc || data.nodes;
        nodes.forEach(function(i){
            var visible=(i.visible!==false);
            var id=(i.relPath || i.attributes.id);
            var title=(i.title || i.data);
            i.visible=visible;
            i.relPath=id;
            if(!i.attributes) i.attributes={};
            i.attributes.id=id;
            i.title=title; i.data=title;
        });
        if(!homePage || homePage=="toc.htm") {
            if(nodes.length>0){
                homePage=nodes[0].relPath;
                window.location.hash = homePage;
            }else{
                homePage=""; 
                rvt.updateContent("[No content]");
            }
        }
        rvt.displayTOC(nodes);
        
        checkForLocationChange();
        setInterval(checkForLocationChange, 10);
    }

    function checkForLocationChange() {
        if(checkForLocationChange.href!==window.location.href){
            var hashOnly=false; hash = window.location.hash;
            if(hash.length) hash=hash.substring(1);
            if(checkForLocationChange.href){
                hashOnly=(checkForLocationChange.href.split("#",1)[0])===(window.location.href.split("#",1)[0]);
            }
            checkForLocationChange.href = window.location.href;
            jQ(window.location).trigger("change", {hash:hash, hashOnly:hashOnly});
        }
    }

    function onLocationChange(location, data){
        hash = data.hash;
        hash = hash.split("#")[0];
        if(data.hashOnly){
            if(hash===onLocationChange.hash) return;
        }
        onLocationChange.hash = hash;
        if(hash==="") hash=homePage;
        if(pages[hash]) {
            rvt.updateContent(pages[hash]);
            return;
        }
        function getHPath(p) {
            // returns hPath or nUll (make a relative path an absolute hash path)
            var ourParts, ourDepth, upCount=0, depth, hPath;
            if(p.slice(0,1)==="/" || p.search("://")>0) return null;
            ourParts = hash.split("/");
            ourDepth = ourParts.length-1;
            p = p.replace(/[^\/\.]+\/\.\.\//g, "");
            p = p.replace(/\.\.\//g, function(m){upCount+=1; return "";});
            depth = ourDepth-upCount;
            if(depth<0) return null;
            hPath = ourParts.slice(0,depth).concat(p.split("/")).join("/");
            return hPath
        }
        function callback(data) {   // ICE content data
            var h, anchors={};
            var pageToc = jQ(data).find("div.page-toc");
            var body = jQ(data).find("div.body");
            body.find("div.title").show().after(pageToc);
            body.find("a").each(function(c, a) { 
                a=jQ(a); h = a.attr("href"); 
                if(h){
                    if(h.substring(0,1)==="#"){
                        a.attr("href", "#"+hash+h);
                        anchors[h.substring(1)]=hash+h;
                    }else{
                        if(h=getHPath(h)){
                            a.attr("href", "#"+h);
                        }
                    }
                }
            });
            body.find("a").each(function(c, a) { 
                var id, name;
                a=jQ(a); h = a.attr("href");
                id=a.attr("id"); name=a.attr("name");
                if(anchors[id]){
                    a.attr("name", anchors[id]);
                }else if(anchors[name]){
                    a.attr("name", anchors[name]);
                }
            });
            var html = body.html();
            pages[hash]=html;
            rvt.updateContent(pages[hash]);
        }
        rvt.updateContent(rvt.loadingMessage);
        jQ.get(hash, {}, callback, "html");
    }

    jQ(window.location).change(onLocationChange);

    return rvt;
};






