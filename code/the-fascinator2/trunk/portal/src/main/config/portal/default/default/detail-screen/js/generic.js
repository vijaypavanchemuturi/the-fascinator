<script type="text/javascript" src="$portalPath/js/amq/amq_jquery_adapter.js"></script>
<script type="text/javascript" src="$portalPath/js/amq/amq.js"></script>
<script type="text/javascript">

function fixLinks(baseUrl, selector, attrName, oid) {
    $(selector).each(function() {
        var attr = $(this).attr(attrName);
        if (attr != null) {
            // fix for IE7 attr() returning resolved URLs - strip base URL
            var href = window.location.href;
            hrefBase = href.substring(0, href.lastIndexOf("/"));
            attrBase = attr.substring(0, hrefBase.length);
            if (hrefBase == attrBase) {
                attr = attr.substring(hrefBase.length + 1);
            }
            if (attr.indexOf("tfObject:") == 0) {
                // First class objects (links only, not images)
                var relUrl = "$portalPath/detail/";
                var tmpUrl = attr.substring(9);
                $(this).attr(attrName, escape(relUrl + tmpUrl));
            } else {
                // Payloads, images and links
                if (attr.indexOf("#") != 0 && attr.indexOf("://") == -1) {
                    var relUrl = "$portalPath/detail/" + oid + "/";
                    var tmpUrl = baseUrl.substring(baseUrl.indexOf("/", 8));
                    tmpUrl = tmpUrl.replace(relUrl, "");
                    tmpUrl = tmpUrl.substring(0, tmpUrl.lastIndexOf("/")+1);
                    $(this).attr(attrName, escape(relUrl + tmpUrl + attr));
                }
            }
        }
    });
}

function loadAnnotations(config, rootUri) {
    jQuery.post("$portalPath/actions/anotar.ajax",
        {   "action": "getList",
            "rootUri": rootUri,
            "type": config.annoType
        },
        function(data, status) {
            jQuery.each(data, function(count, item) {
                try{
                    config.loadAnnotation(item);
                } catch(e) {
                    if (window.console) {
                        console.log("error:'" + e +
                                "' rootUri:'" + item.annotates.rootUri +
                                "' content:'" + item.content.literal + "'");
                    }
                }
            });
        },
        "json");
}

function submitAnnotation(config, rootUri, data, annotateDiv) {
    jQuery.post("$portalPath/actions/anotar.ajax",
        {   "action": "put",
            "rootUri": rootUri,
            "json": JSON.stringify(data)
        },
        function(data, status) {
            try {
                config.loadAnnotation(data);
            } catch(e) {
                if (window.console) {
                    console.log("error:'" + e +
                            "' rootUri:'" + data.annotates.rootUri +
                            "' content:'" + data.content.literal + "'");
                }
            }
        },
        "json");
}

function deleteComment(node, uriAttr) {
    var commentNode = $(node).parent();
    var pidList = [];
    var rootUri = commentNode.find("input[name='rootUri']").attr("value");
    jQuery.each(commentNode.find(".anno-inline-annotation").andSelf(),
            function(count, item) {
                var id = $(item).attr(uriAttr);
                pidList.push(id.substring(id.lastIndexOf("#") + 1));
            });
    
    $("#delete-annotation .cancel").one("click", function() {
        $("#delete-annotation .delete").unbind("click");
        $("#delete-annotation").dialog("close");
    });
    
    $("#delete-annotation .delete").one("click", function() {
        $("#delete-annotation .cancel").unbind("click");
        jQuery.ajax({
            type: "POST",
            url: "$portalPath/actions/anotar.ajax",
            data: {
                "action": "delete",
                "rootUri": rootUri,
                "pidList": pidList
            },
            success: function(data, status) {
                $("#delete-annotation").dialog('close');
                commentNode.fadeOut(function() {
                    commentNode.remove();
                });
            },
            error: function(xhr, status, e) {
                $("#delete-annotation .error").show();
                $("#delete-annotation .message").html(xhr.responseText);
            }
        });
    });
    
    var content = commentNode.find(".anno-anno-content").html();
    var maxlen = 35;
    if (content.length > maxlen) {
        content = content.substring(0, maxlen) + "...";
    }
    $("#delete-annotation .type").html("comment and its replies");
    $("#delete-annotation .content").html(content);
    $("#delete-annotation").dialog("option", "title", "Delete Comment");
    $("#delete-annotation").dialog("open");
}

$(function() {
    var oid = "$oid";
    var filenameNoExt = "$filenameNoExt";

    gFixLinks = fixLinks;
    fixLinks("", "div.content-preview-inline a", "href", "$oid");
    fixLinks("", "div.content-preview-inline img", "src", "$oid");

    $(".open-this").click(function() {
        jQuery.post("$portalPath/open.ajax", { oid: "$oid" },
            function(data, status) {
                if (data.message) {
                    alert("Failed to open file: " + data.message);
                }
            }, "json");
        return false;
    });

    $("#reharvest").click(function() {
        var amq = org.activemq.Amq;
        var clientId = "reharvest_$oid";
        var clientTopic = "topic://message";
        function waitRender(message) {
            var json = this.JSON.parse(message.nodeValue);
            $("#reharvest-progress").append("<li>" + json.message + "</li>");
            if (json.status == "renderComplete" && json.id == "$oid") {
                amq.removeListener(clientId, clientTopic);
                $("#reharvest-loading").hide();
                $("#reharvest-complete").show();
            }
        }
        $("#reharvest-form").show();
        jQuery.post("$portalPath/reharvest.ajax",
            { func: "reharvest", oid: "$oid" },
            function(data, status) {
                amq.init({ uri: "$contextPath/amq/", timeout: 10 });
                amq.addListener(clientId, clientTopic, waitRender);
            }, "json");
        return false;
    });

    function addRendition(href, label) {
        $("#actions").append('<li><a href="' + href + '" target="blank">' + label + '</a></li>');
    }
    $('li.payload[rel="application/pdf"] a:first').each(function() {
        addRendition($(this).attr("href"), "PDF version");
    });
    $("span.slide-link > a").each(function() {
        addRendition($(this).attr("href"), "View slide show");
    });
    $("div.rendition-links").remove();

    #if($self.isMetadataOnly())
    $("#preview").after($("#metadata")).remove();
    #end
});
</script>
