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

$(function() {
    var oid = "$oid";
    var filenameNoExt = "$filenameNoExt";

    gFixLinks = fixLinks;
    fixLinks("", "div.content-preview-inline a", "href", "$oid");
    fixLinks("", "div.content-preview-inline img", "src", "$oid");

    $(".open-this").click(function() {
        jQuery.post("$portalPath/open.ajax", { func: "open-file", file: "$oid" } );
        return false;
    });

    $("#reharvest").click(function() {
        jQuery.post("$portalPath/reharvest.ajax", { func: "reharvest", file: "$oid" } );
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

    $("#attachments h2 a").click(function() {
        $("#attachments ul").toggle("blind").toggleClass('visible').toggleClass('hidden');
        return false;
    });

    #if($self.isMetadataOnly())
    $("#preview").after($("#metadata")).remove();
    #end
    
    var dialogOpts = {
        autoOpen: false,
        hide: "blind",
        width: 350,
        modal: true
    };

    $("#delete-dialog").dialog(dialogOpts);
    $("#delete-dialog").dialog('option', 'title', 'Delete Object');
    
    $(".admin-delete-link").click(function() {
        recordId = $(this).attr("rel");
        title = ($(".content-title"));
        if (!title) {
            title = recordId;
        } else {
            title = title.text();
        }
        $("#delete-message").empty();
        $("#delete-error").hide();
        $("#delete-dialog").dialog('open');
        $("#delete-legend").html(title);
        return false;
    });

    
    $("#delete-cancel").click(function() {
        $("#delete-message").empty();
        $("#delete-error").hide();
        $("#delete-dialog").dialog('close');
        return false;
    });

    $("#delete-submit").click(function() {
        $("#delete-message").empty();
        $("#delete-error").hide();
        $("#delete-dialog").dialog('open');

        jQuery.ajax({
            type : "POST",
            url : "$portalPath/actions/delete.ajax",
            success:
                function (data, status) {
                    $("#delete-dialog").dialog('close');
                    window.location.href = "$portalPath/search"
                },
            error:
                function (req, status, e) {
                    $("#delete-error").show();
                    $("#delete-message").html(req.responseText);
                },
            data: {
                record: recordId
            }
        });

        return false;
    });
});
</script>
