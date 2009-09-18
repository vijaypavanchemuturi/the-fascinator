package au.edu.usq.fascinator.contrib.feedreader;

import java.util.ArrayList;
import java.util.List;

import com.sun.syndication.feed.module.DCModuleImpl;
import com.sun.syndication.feed.module.DCSubject;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.feed.synd.SyndLinkImpl;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.feed.synd.SyndPersonImpl;

public class FeedHelper {

    public static List<SyndPerson> getAuthors(SyndEntry entry) {
        if (entry.getAuthors().size() > 0) {
            return entry.getAuthors();

        }
        ArrayList<SyndPerson> authors = new ArrayList<SyndPerson>();
        SyndPerson author = new SyndPersonImpl();
        author.setName(entry.getAuthor());
        authors.add(author);
        return authors;

    }

    public static List<SyndLink> getLinks(SyndEntry entry) {
        if (entry.getLinks().size() > 0) {
            return entry.getLinks();
        }
        ArrayList<SyndLink> links = new ArrayList<SyndLink>();
        SyndLinkImpl link = new SyndLinkImpl();
        link.setHref(entry.getLink());
        links.add(link);
        return links;
    }

    public static List<SyndContent> getContents(SyndEntry entry) {
        return entry.getContents();
    }

    public static List<SyndCategory> getCategories(SyndEntry entry) {
        ArrayList<SyndCategory> categories = new ArrayList<SyndCategory>();
        categories.addAll(entry.getCategories());

        /*
         * Services such as Slashdot seem to have their tags/categories within
         * the Dublin Core
         */
        DCModuleImpl dc = (DCModuleImpl) entry
                .getModule(com.sun.syndication.feed.module.DCModule.URI);
        if (dc != null) {
            for (DCSubject dcSubject: (List<DCSubject>)dc.getSubjects()){
                SyndCategoryImpl category = new SyndCategoryImpl();
                category.setName(dcSubject.getValue());
                category.setTaxonomyUri(dcSubject.getTaxonomyUri());
                categories.add(category);
            }
        }
        return categories;
    }
}
