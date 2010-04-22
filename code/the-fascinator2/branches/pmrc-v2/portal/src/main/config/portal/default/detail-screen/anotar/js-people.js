<script type="text/javascript">
    $(function() {
        // People Tags annotation
        peopleConfig={
            //debug: true,
            docRoot: "#people-tag-list",
            tagList: "p",
            contentInput: "#txtPeople",
            pageUri: "$oid",
            uriAttr: "rel",
            outputInChild: ".people-tags",
            annotationType: "tag",
            stylePrefix: "tags-",
            interfaceLabel: " <img src='$portalPath/images/icons/add.png'/>",
            creator: "$creator",
            creatorUri: "$creatorUri",
            //formPrepend: true,
            interfaceVisible: true,
            formCustom: "people_form",
            formCancel: ".peopleTag-cancel",
            formSubmit: ".peopleTag-submit",
            displayCustom: "people_display", 
            formPopup: true,
            formPopupSettings: {
                width: 500,
                title: "People Tag",
                zIndex: 5000
            },
            onFormDisplay: function(node){
                $("#txtFirstName").attr("value", "");
                $("#txtSurname").attr("value", "");
                $(".people_search_detail").show();
                $(".peopleTag-submit").hide();
                $(".search_results").hide();
                $(".search_results").html("");
            },
            getContentUri: function(node) {
                var rel = node.attr("rel");
                if (rel == null) {
                    // content is not a URI
                    return { isUri: false };
                }
                
                return { isUri: true, contentUri: rel };
            },
            serverAddress: "$portalPath/actions/anotar.ajax",
            disableReply: true,
            serverMode: "fascinator"
        }
        // Use formCloseFunction to overwrite close function
        peopleTag = anotarFactory(jQuery, peopleConfig);
    });
    
    $(".peopleTag-search").live("click", function() {
        jQuery.post("$portalPath/actions/people.ajax", { 
            func: "searchName", 
            file: "$oid",
            firstName: $("#txtFirstName").attr("value"),
            surname: $("#txtSurname").attr("value")
        }, function(data) {
            if (data.indexOf("No veteran") < 0 && data.indexOf("Could not connect") < 0) {
                $(".people_search_detail").hide();
                $(".peopleTag-submit").show();
            } else {
                $(".people_search_detail").show();
                $(".peopleTag-submit").hide();
            }
            $(".search_results").show();
            $(".search_results").html(data);
        });
    });
    
    $("#selected_people").live("click", function() {
        $("#txtPeople").attr("value", $(this).attr("value"));
        $("#people-tag-list").attr("rel", $(this).attr("rel"));
    });
</script>