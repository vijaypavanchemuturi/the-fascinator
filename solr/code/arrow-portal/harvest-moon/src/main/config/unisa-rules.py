from au.edu.usq.solr.index.rule import RuleManager
from au.edu.usq.solr.index.rule.impl import *

#
# Available objects:
#    self: Harvest instance
#    rules: RuleManager instance
#    item: metadata item
#    pid: registry object pid
#    dsId: datastream id or None if item is object
#    collection: collection title or None if no collection
#
datastreamMode = dsId is not None
if not datastreamMode:
    #
    # full processing mode starts with the dublin core document. this is
    # intended for indexing main item records.
    #
    isCollectionItem = False
    nodes = item.getMetadata().selectNodes("//dc:type")
    for node in nodes:
        dcType = node.getText().strip()
        if dcType == "collection":
            isCollectionItem = True
            title = item.getMetadata().selectSingleNode("//dc:title").getText()
            rules.add(AddFieldRule("collection", title))
    if isCollectionItem:
        rules.add(SkipRule("VITAL Collection object"))
    else:
        # dc to solr transform
        rules.add(TransformRule(self.getResource("/xsl/dc_solr.xsl")))
        # at least one identifier with url value
        rules.add(CheckFieldRule("identifier", "http.*"))
        # at least one non-blank title
        rules.add(CheckFieldRule("title", ".+"))
        # use only the year from date field
        rules.add(ModifyFieldRule("date", ".*(\\d{4}).*", "$1"))
        # delete blank subject
        rules.add(DeleteFieldRule("subject", "^\\s*$"))
        # delete blank creator
        rules.add(DeleteFieldRule("creator", "^\\s*$"))
        # delete invalid dates
        rules.add(DeleteFieldRule("date", "(.*[^0-9].*)|(^\\s*$)"))
        # normalise subjects with RFCD codes
        rules.add(ModifyFieldRule("subject", ".*RFCD:(\\d{6}) \\((.+)\\)$", "$2 ($1)"))
        
        # set repository name
        rules.add(AddFieldRule("repository_name", "UniSA"))
        # set unique identifier (e.g. oai id or fedora pid)
        rules.add(AddFieldRule("id", item.getId()))
        # set registry pid
        rules.add(AddFieldRule("pid", pid))
        # set item type to object
        rules.add(AddFieldRule("item_type", "object"))
        # set item class to document
        rules.add(AddFieldRule("item_class", "document"))
        
        # VITAL collection
        if collection is not None:
            rules.add(AddFieldRule("collection", collection))
        
        # group access
        #   default to "guest"
        rules.add(AddFieldRule("group_access", "guest"))
        
        # full text - get the FULLTEXT datastreams
        if item.hasDatastreams():
            ds = item.getDatastream("FULLTEXT")
            if ds is not None:
                rules.add(AddFieldRule("full_text", ds.getContentAsString()))
else:
    #
    # datastream mode starts with a blank solr document. this is intended for
    # indexing datastreams.
    #
    # skip derived datastreams
    if dsId.startswith("FULLTEXT") or dsId.endswith("-CONTENT"):
        rules.add(SkipRule("Derived datastream"))
    else:
        ds = item.getDatastream(dsId)
        # set unique id
        rules.add(AddFieldRule("id", item.getId() + "/" + dsId))
        # set registry pid
        rules.add(AddFieldRule("pid", pid))
        # set datastream id
        rules.add(AddFieldRule("identifier", dsId))
        # set label
        rules.add(AddFieldRule("title", ds.getLabel()))
        # set content type
        rules.add(AddFieldRule("format", ds.getMimeType()))
        # set item type to datastream
        rules.add(AddFieldRule("item_type", "datastream"))
        # group access
        #   default to "guest"
        rules.add(AddFieldRule("group_access", "guest"))
    
