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
package au.edu.usq.fascinator.storage.filesystem;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * An implementation of the pairtree storage scheme v0.1. Specification
 * available at http://www.cdlib.org/inside/diglib/pairtree/pairtreespec.html
 * 
 * @author Oliver Lucido
 */
public class PairTree {

    private final Integer[] encodeChars = { 0x22, 0x2a, 0x2b, 0x2c, 0x3c, 0x3d,
            0x3e, 0x3f, 0x5e, 0x7c };

    private final List<Integer> encodeList = Arrays.asList(encodeChars);

    private final static String TERMINAL = "obj";

    private File homeDir;

    public PairTree() {
        homeDir = new File(".");
    }

    public PairTree(File homeDir) {
        this.homeDir = homeDir;
    }

    public File getFile(String id, String filename) {
        String ppath = getPairPath(id);
        File parentDir = new File(homeDir, ppath + File.separator + TERMINAL);
        return new File(parentDir, filename);
    }

    public String getPairPath(String id) {
        String cleanId = clean(id);
        int len = cleanId.length();
        StringBuilder pairPath = new StringBuilder();
        for (int i = 0; i < len; i += 2) {
            String pair = cleanId.substring(i, Math.min(i + 2, len));
            pairPath.append(pair);
            pairPath.append(File.separator);
        }
        return pairPath.toString();
    }

    public String getId(String pairPath) {
        String id = pairPath.replaceAll(File.separator, "");
        return unclean(id);
    }

    public String getId(File path) {
        String name = "";
        File pairPath = path;
        while (!TERMINAL.equals(name)) {
            name = pairPath.getName();
            pairPath = pairPath.getParentFile();
        }

        return getId(pairPath.toString().replace(homeDir.getPath(), ""));
    }

    public String clean(String id) {
        StringBuilder cleanId = new StringBuilder();
        for (int i = 0; i < id.length(); i++) {
            int ch = id.charAt(i);
            if ((ch >= 0x21 && ch <= 0x7e) && !encodeList.contains(ch)) {
                switch (ch) {
                case '/':
                    ch = '=';
                    break;
                case ':':
                    ch = '+';
                    break;
                case '.':
                    ch = ',';
                    break;
                }
                cleanId.append((char) ch);
            } else {
                cleanId.append('^');
                cleanId.append(Integer.toHexString(ch));
            }
        }
        return cleanId.toString();
    }

    public String unclean(String cleanId) {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < cleanId.length(); i++) {
            int ch = cleanId.charAt(i);
            switch (ch) {
            case '=':
                ch = '/';
                break;
            case '+':
                ch = ':';
                break;
            case ',':
                ch = '.';
                break;
            case '^':
                ch = Integer.parseInt(cleanId.substring(i + 1, i + 3), 16);
                i += 2;
                break;
            }
            id.append((char) ch);
        }
        return id.toString();
    }
}
