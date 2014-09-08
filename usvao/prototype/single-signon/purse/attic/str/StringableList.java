package org.nvo.sso.sample.reg.str;

import java.util.ArrayList;
import java.text.ParseException;
import java.io.*;

public class StringableList extends ArrayList<String> implements Stringable {
    public static final String AVOID = "\n";

    public StringableList() { }
    public StringableList(String s) throws ParseException { fromString(s); }

    public void fromString(String s) throws ParseException {
        clear();
        LineNumberReader reader = new LineNumberReader(new StringReader(s));
        String line;
        while (true) {
            try {
                line = reader.readLine();
                if (line == null) break;
                add(StringableKit.unescape(line));
            } catch (IOException e) { throw new IllegalStateException("What the?"); }
        }
    }

    public String toString() {
        StringWriter result = new StringWriter();
        PrintWriter writer = new PrintWriter(result);
        for (String s : this) writer.println(StringableKit.escape(s, AVOID));
        return result.toString();
    }

    public static void main(String[] args) throws ParseException {
        StringableList before = new StringableList();
        before.add("this is\ntwo lines");
        StringableMap map = new StringableMap();
        map.put("a=b","only if b\nequals a");
        map.put("left side", "right side");
        before.add(map.toString());
        before.add("four; two / three");
        System.out.println(before);
        StringableList after = new StringableList(before.toString());
        assert before.equals(after);
        assert new StringableMap(after.get(1)).equals(map);
    }
}
