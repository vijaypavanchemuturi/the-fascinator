#if(!$self.mimeType.startsWith("audio/") && !$self.mimeType.startsWith("video/"))
<script type="text/javascript">
$(function() {
    commentConfig = {
        pageUri: "$oid",
        #if("$!creator" != "")creator: "$creator",#end
        #if("$!creatorUri" != "")creatorUri: "$creatorUri",#end
        tagList: "p, h1, h2, h3, h4, h5, h6, .annotatable",
        interfaceLabel: " <img src='$portalPath/images/icons/comment_add.png' title='Add new comment' /> Comments:",
        replyLabel: " <img src='$portalPath/images/icons/comments_add.png' title='Reply to this comment' />",
        interfaceVisible: $self.mimeType.startsWith("image/").toString().toLowerCase(),
        displayCustom: "object_comment_display",
        loadAnnotations: function() { loadAnnotations(this, "$oid") },
        submitAnnotation: function(data, annotateDiv) { submitAnnotation(this, "$oid", data, annotateDiv) }
    }
    anotarFactory(jQuery, commentConfig);
});
</script>
#end
