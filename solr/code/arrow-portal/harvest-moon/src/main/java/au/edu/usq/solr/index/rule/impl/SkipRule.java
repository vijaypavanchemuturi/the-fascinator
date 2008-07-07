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
package au.edu.usq.solr.index.rule.impl;

import java.io.Reader;
import java.io.Writer;

import org.apache.log4j.Logger;

import au.edu.usq.solr.index.rule.AbstractRule;
import au.edu.usq.solr.index.rule.RuleException;

public class SkipRule extends AbstractRule {

    private Logger log = Logger.getLogger(SkipRule.class);

    private String reason;

    public SkipRule(String reason) {
        super("Skip", true);
        this.reason = reason;
    }

    @Override
    public void run(Reader in, Writer out) throws RuleException {
        String msg = "Item will not be indexed: " + reason;
        log.info(msg);
        throw new RuleException(msg);
    }
}
