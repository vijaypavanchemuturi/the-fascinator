/* 
 * The Fascinator - Solr Portal
 * Copyright (C) 2008  University of Southern Queensland
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
package au.edu.usq.fascinator.portal;

import junit.framework.Assert;

import org.junit.Test;

public class PaginationTest {

    @Test
    public void FifteenRecords() {
        Pagination p = new Pagination(1, 15, 10);
        Assert.assertEquals(2, p.getLastPage());
    }

    @Test
    public void TwentyRecords() {
        Pagination p = new Pagination(1, 20, 10);
        Assert.assertEquals(2, p.getLastPage());
    }

    @Test
    public void FiftySixRecords() {
        Pagination p = new Pagination(1, 56, 10);
        Assert.assertEquals(6, p.getLastPage());
    }

    @Test
    public void OneHundredRecords() {
        Pagination p = new Pagination(1, 100, 10);
        Assert.assertEquals(10, p.getLastPage());
    }
}
