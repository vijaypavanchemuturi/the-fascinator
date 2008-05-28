/* 
 * Sun of Fedora - Solr Portal
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
package au.edu.usq.solr.portal;

import java.util.ArrayList;
import java.util.List;

public class Pagination {

    private int page;

    private int totalFound;

    private int startNum;

    private int endNum;

    private int lastPage;

    private List<Page> pages;

    public Pagination(int page, int totalFound, int numPerPage) {

        this.page = page;
        this.totalFound = totalFound;

        startNum = ((page - 1) * numPerPage) + 1;
        endNum = Math.min(page * numPerPage, totalFound);

        lastPage = totalFound / numPerPage + 1;
        if (totalFound % numPerPage == 0) {
            lastPage++;
        }

        pages = new ArrayList<Page>();

        int startPage = 0;
        if (page >= 5) {
            startPage = page - 5;
        }

        int endPage = Math.min(lastPage, page + 5);

        for (int i = startPage; i < endPage; i++) {
            Page p = new Page();
            p.setValue(i + 1);
            p.setStart(i * numPerPage);
            p.setSelected(i == (page - 1));
            pages.add(p);
        }
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public boolean hasNext() {
        return page < lastPage - 1;
    }

    public int getPage() {
        return page;
    }

    public int getTotalFound() {
        return totalFound;
    }

    public int getLastPage() {
        return lastPage;
    }

    public int getStartNum() {
        return startNum;
    }

    public int getEndNum() {
        return endNum;
    }

    public List<Page> getPages() {
        return pages;
    }
}