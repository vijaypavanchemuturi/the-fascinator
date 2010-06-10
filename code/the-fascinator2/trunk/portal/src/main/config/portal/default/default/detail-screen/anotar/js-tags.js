<script type="text/javascript">
$(function() {
    // Tags annotation
    tagConfig = {
        docRoot: "#object-tag-list",
        tagList: "div",
        #if("$!creator" != "")creator: "$creator",#end
        #if("$!creatorUri" != "")creatorUri: "$creatorUri",#end
        contentInput: "#object-tag-input",
        pageUri: "$oid",
        uriAttr: "rel",
        outputInChild: ".object-tags",
        annotationType: "tag",
        stylePrefix: "tags-",
        interfaceLabel: " <img src='$portalPath/images/icons/add.png' title='Add tag' />",
        formPrepend: true,
        interfaceVisible: true,
        formCustom: "object_tag_form",
        formCancel: ".myTag-cancel",
        formSubmit: ".myTag-submit",
        disableReply: true,
        loadAnnotations: function() { loadAnnotations(this, "$oid") },
        submitAnnotation: function(data, annotateDiv) { submitAnnotation(this, "$oid", data, annotateDiv) }
    }
    anotarFactory(jQuery, tagConfig);
});
</script>
