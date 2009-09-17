/*
 * The Fascinator
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
package au.edu.usq.fascinator.contrib.feedreader;

import org.htmlparser.Parser;
import org.htmlparser.Text;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

/**
 * @author dickinso
 * 
 */
public class PlainTextExtractor {
	public static String getPlainText(String type, String value) {
		if (type.equals("text") || type.equals("text/plain")) {
			return value;
		} else if (type.equals("html") || type.equals("text/html")) {
			Page page = new Page(value);
			Lexer lexer = new Lexer(page);
			Parser parser = new Parser(lexer);
			PlainTextExtractorNodeVisitor extractor = new PlainTextExtractorNodeVisitor();
			try {
				parser.visitAllNodesWith(extractor);
			} catch (ParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return extractor.getPlainText();
		}
		return null;
	}

	static class PlainTextExtractorNodeVisitor extends NodeVisitor {
		StringBuilder plainText = new StringBuilder();

		public void visitStringNode(Text string) {
			plainText.append(string.getText());
		}

		public String getPlainText() {
			return plainText.toString();
		}
	}
}
