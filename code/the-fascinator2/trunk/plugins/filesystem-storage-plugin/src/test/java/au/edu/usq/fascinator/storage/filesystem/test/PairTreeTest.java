/* 
 * The Fascinator - File System storage plugin
 * Copyright (C) 2009 University of Southern Queensland
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package au.edu.usq.fascinator.storage.filesystem.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import au.edu.usq.fascinator.storage.filesystem.PairTree;

public class PairTreeTest {

    private PairTree pt = new PairTree();

    @Test
    public void getFile() {
        File readme = pt.getFile("abcd", "README.txt");
        Assert.assertEquals("./ab/cd/obj/README.txt", readme.getPath());
    }

    @Test
    public void getIdFromFile() {
        String id = pt.getId(new File("./ab/cd/obj/README.txt"));
        Assert.assertEquals("abcd", id);
    }

    @Test
    public void getPairPath() {
        String ppath = pt.getPairPath("abcd");
        Assert.assertEquals("ab/cd/", ppath);
        ppath = pt.getPairPath("abcde");
        Assert.assertEquals("ab/cd/e/", ppath);
        ppath = pt.getPairPath("12-986xy4");
        Assert.assertEquals("12/-9/86/xy/4/", ppath);
        ppath = pt.getPairPath("ark:/13030/xt12t3");
        Assert.assertEquals("ar/k+/=1/30/30/=x/t1/2t/3/", ppath);
        ppath = pt.getPairPath("http://n2t.info/urn:nbn:se:kb:repos-1");
        Assert.assertEquals(
                "ht/tp/+=/=n/2t/,i/nf/o=/ur/n+/nb/n+/se/+k/b+/re/po/s-/1/",
                ppath);
        ppath = pt.getPairPath("what-the-*@?#!^!?");
        Assert.assertEquals("wh/at/-t/he/-^/2a/@^/3f/#!/^5/e!/^3/f/", ppath);
    }

    @Test
    public void getId() {
        String id = pt.getId("ab/cd/");
        Assert.assertEquals("abcd", id);
        id = pt.getId("ab/cd/e/");
        Assert.assertEquals("abcde", id);
        id = pt.getId("12/-9/86/xy/4/");
        Assert.assertEquals("12-986xy4", id);
        id = pt.getId("ar/k+/=1/30/30/=x/t1/2t/3/");
        Assert.assertEquals("ark:/13030/xt12t3", id);
        id = pt
                .getId("ht/tp/+=/=n/2t/,i/nf/o=/ur/n+/nb/n+/se/+k/b+/re/po/s-/1/");
        Assert.assertEquals("http://n2t.info/urn:nbn:se:kb:repos-1", id);
        id = pt.getId("wh/at/-t/he/-^/2a/@^/3f/#!/^5/e!/^3/f/");
        Assert.assertEquals("what-the-*@?#!^!?", id);
    }

    @Test
    public void clean() {
        String cleanId = pt.clean("ark:/13030/xt12t3");
        Assert.assertEquals("ark+=13030=xt12t3", cleanId);
        cleanId = pt.clean("http://n2t.info/urn:nbn:se:kb:repos-1");
        Assert.assertEquals("http+==n2t,info=urn+nbn+se+kb+repos-1", cleanId);
        cleanId = pt.clean("what-the-*@?#!^!?");
        Assert.assertEquals("what-the-^2a@^3f#!^5e!^3f", cleanId);
    }

    @Test
    public void unclean() {
        String id = pt.unclean("ark+=13030=xt12t3");
        Assert.assertEquals("ark:/13030/xt12t3", id);
        id = pt.unclean("http+==n2t,info=urn+nbn+se+kb+repos-1");
        Assert.assertEquals("http://n2t.info/urn:nbn:se:kb:repos-1", id);
        id = pt.unclean("what-the-^2a@^3f#!^5e!^3f");
        Assert.assertEquals("what-the-*@?#!^!?", id);
    }
}
