/*************************************************************************

   Copyright (c) 2014, California Institute of Technology, Pasadena,
   California, under cooperative agreement 0834235 between the California
   Institute of Technology and the National Science  Foundation/National
   Aeronautics and Space Administration.

   All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   of this BSD 3-clause license are met:

   1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

   3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
   HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

   This software was developed by the Infrared Processing and Analysis
   Center (IPAC) for the Virtual Astronomical Observatory (VAO), jointly
   funded by NSF and NASA, and managed by the VAO, LLC, a non-profit
   501(c)(3) organization registered in the District of Columbia and a
   collaborative effort of the Association of Universities for Research
   in Astronomy (AURA) and the Associated Universities, Inc. (AUI).

*************************************************************************/



package ipac.stk.json;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Class for outputting JSON to a
 * {@link java.io.Writer Writer} or {@link java.io.OutputStream OutputStream}.
 */
public class JSONOutput implements Closeable, Flushable {

  /**
   * A class which encapsulates JSON formatting options.
   */
  public static final class Options implements Cloneable {
    private boolean escapeUnicode = false;
    private boolean escapeSolidus = false;
    private boolean alignValues = false;
    private boolean prettyPrint = false;
    private boolean literalKeys = false;
    private Pattern keyPattern = null;
    private String indentation = "\t";
    private String spacer = " ";
    private String separator = ", ";
    private String kvSeparator = ": ";
    private String arrayOpen = "[";
    private String arrayClose = "]";
    private String objectOpen = "{";
    private String objectClose = "}";

    /**
     * Creates default JSON formatting options; output will be UTF-8 encoded.
     * Output will contain no new-lines, and very little formatting whitespace.
     */
    private Options() { }

    @Override public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException ex) {
        // should never happen
        throw new RuntimeException("clone() failed!");
      }
    }

    /**
     * Returns <code>true</code> if unicode characters in keys and strings
     * are escaped.
     */
    public boolean escapeUnicode() {
      return escapeUnicode;
    }

    /**
     * Returns <code>true</code> if solidus characters ('/') in keys and
     * strings are escaped.
     */
    public boolean escapeSolidus() {
      return escapeSolidus;
    }

    /**
     * Returns <code>true</code> if pretty-printing mode is on.
     */
    public boolean prettyPrint() {
      return prettyPrint;
    }

    /**
     * Returns <code>true</code> if numeric array values
     * are left-aligned in pretty-printing mode.
     */
    public boolean alignValues() {
      return alignValues;
    }

    /**
     * Returns <code>true</code> if JSON object keys are
     * written out without being encoded and quoted.
     */
    public boolean literalKeys() {
      return literalKeys;
    }

    /**
     * Returns the pattern JSON object keys must match.
     */
    public Pattern getKeyPattern() {
      return keyPattern;
    }

    /**
     * Returns the string used to indent content in pretty-printing mode.
     */
    public String getIndentation() {
      return indentation;
    }

    /**
     * Returns the string used to pad JSON array/object opening/closing
     * strings.
     */
    public String getSpacer() {
      return spacer;
    }

    /**
     * Returns the string used to separate JSON array values or JSON
     * object key/value pairs.
     */
    public String getSeparator() {
      return separator;
    }

    /**
     * Returns the string used to separate JSON object keys from their values.
     */
    public String getKeyValueSeparator() {
      return kvSeparator;
    }

    /**
     * Returns the string used to open JSON arrays.
     */
    public String getArrayOpen() {
      return arrayOpen;
    }

    /**
     * Returns the string used to close JSON arrays.
     */
    public String getArrayClose() {
      return arrayClose;
    }

    /**
     * Returns the string used to open JSON objects.
     */
    public String getObjectOpen() {
      return objectOpen;
    }

    /**
     * Returns the string used to close JSON objects.
     */
    public String getObjectClose() {
      return objectClose;
    }

    // -- Descriptive COW builder methods ----

    /**
     * Creates an Options object with default properties.
     */
    public static Options create() {
      return new Options();
    }

    /**
     * Creates a copy of <code>proto</code>.
     */
    public static Options create(Options proto) {
      return (Options) proto.clone();
    }

    /**
     * Sets unicode character escaping in keys/strings.
     *
     * @param escUnicode  If <code>true</code>, unicode characters
     *                    in keys and strings are escaped.
     */
    public Options withEscapeUnicode(boolean escUnicode) {
      Options opts = (Options) this.clone();
      opts.escapeUnicode = escUnicode;
      return opts;
    }

    /**
     * Sets solidus character escaping in keys/strings.
     *
     * @param escSolidus  If <code>true</code>, solidus characters ('/')
     *                    in keys and strings are escaped.
     */
    public Options withEscapeSolidus(boolean escSolidus) {
      Options opts = (Options) this.clone();
      opts.escapeSolidus = escSolidus;
      return opts;
    }

    /**
     * Sets JSON pretty-printing mode.
     *
     * @param pretty  If <code>true</code>, JSON is pretty printed.
     */
    public Options withPrettyPrint(boolean pretty) {
      Options opts = (Options) this.clone();
      opts.prettyPrint = pretty;
      return opts;
    }

    /**
     * Sets left alignment of numeric JSON array values. Ignored unless
     * {@link #prettyPrint()} returns <code>true</code>.
     *
     * @param align   If <code>true</code>, numeric JSON array
     *                values are left-aligned.
     */
    public Options withAlignValues(boolean align) {
      Options opts = (Options) this.clone();
      opts.alignValues = align;
      return opts;
    }

    /**
     * Sets literal-mode for JSON object keys.
     *
     * @param literal   If <code>true</code>, JSON object keys
     *                  are not encoded or quoted prior to output.
     */
    public Options withLiteralKeys(boolean literal) {
      Options opts = (Options) this.clone();
      opts.literalKeys = literal;
      return opts;
    }

    /**
     * Sets a regular expression which JSON object keys must match.
     *
     * @param keyRegex  Regular expression which JSON keys must match.
     */
    public Options withKeyRegex(String keyRegex) {
      Options opts = (Options) this.clone();
      opts.keyPattern = keyRegex == null ? null : Pattern.compile(keyRegex);
      return opts;
    }

    /**
     * Sets the string used to indent JSON array values and key/value pairs.
     * Ignored unless {@link #prettyPrint()} returns <code>true</code>.
     *
     * @param indent  String used to indent content in pretty-printing mode.
     */
    public Options withIndentation(String indent) {
      Options opts = (Options) this.clone();
      opts.indentation = indent == null ? "" : indent;
      return opts;
    }

    /**
     * Sets the string used to pad JSON array/object opening/closing strings.
     *
     * @param space   String used to pad array/object opening/closing strings.
     */
    public Options withSpacer(String space) {
      Options opts = (Options) this.clone();
      opts.spacer = space == null ? "" : space;
      return opts;
    }

    /**
     * Sets the string used to separate JSON array values or JSON object
     * key/value pairs. Note that if this string does not consist of
     * whitespace and exactly one comma, invalid JSON will be produced.
     *
     * @param sep   String used to separate JSON array values
     *              or JSON object key value pairs.
     */
    public Options withSeparator(String sep) {
      if (sep == null || sep.length() == 0) {
        throw new IllegalArgumentException("Null or empty separator string");
      }
      Options opts = (Options) this.clone();
      opts.separator = sep;
      return opts;
    }

    /**
     * Sets the string used to separate JSON object keys from their values.
     * Note that if this string does not consist of whitespace and a single
     * colon, invalid JSON will be produced.
     *
     * @param kvSep   String used to separate JSON object keys
     *                from their values.
     */
    public Options withKeyValueSeparator(String kvSep) {
      if (kvSep == null || kvSep.length() == 0) {
        throw new IllegalArgumentException(
          "Null or empty key-value separator string");
      }
      Options opts = (Options) this.clone();
      opts.kvSeparator = kvSep;
      return opts;
    }

    /**
     * Sets the string used to open a JSON array. Note that if this does
     * not consist of whitespace and a single '[', invalid JSON will be
     * produced.
     */
    public Options withArrayOpen(String open) {
      if (open == null || open.length() == 0) {
        throw new IllegalArgumentException(
          "Null or empty array opening string");
      }
      Options opts = (Options) this.clone();
      opts.arrayOpen = open;
      return opts;
    }

    /**
     * Sets the string used to close a JSON array. Note that if this does
     * not consist of whitespace and a single ']', invalid JSON will be
     * produced.
     */
    public Options withArrayClose(String close) {
      if (close == null || close.length() == 0) {
        throw new IllegalArgumentException(
          "Null or empty array closing string");
      }
      Options opts = (Options) this.clone();
      opts.arrayClose = close;
      return opts;
    }

    /**
     * Sets the string used to open a JSON object. Note that if this does
     * not consist of whitespace and a single '{', invalid JSON will be
     * produced.
     */
    public Options withObjectOpen(String open) {
      if (open == null || open.length() == 0) {
        throw new IllegalArgumentException(
          "Null or empty object opening string");
      }
      Options opts = (Options) this.clone();
      opts.objectOpen = open;
      return opts;
    }

    /**
     * Sets the string used to open a JSON object. Note that if this does
     * not consist of whitespace and a single '}', invalid JSON will be
     * produced.
     */
    public Options withObjectClose(String close) {
      if (close == null || close.length() == 0) {
        throw new IllegalArgumentException(
          "Null or empty object closing string");
      }
      Options opts = (Options) this.clone();
      opts.objectClose = close;
      return opts;
    }
  }

  public static final Options DEFAULT = Options.create();
  public static final Options ASCII = Options.create().withEscapeUnicode(true);
  public static final Options PRETTY = Options.create()
      .withPrettyPrint(true)
      .withAlignValues(true);
  public static final Options PRETTY_ASCII = Options.create(PRETTY)
      .withEscapeUnicode(true);
  public static final Options IPAC_SVC = Options.create()
      .withEscapeUnicode(true)
      .withSpacer("")
      .withKeyValueSeparator("=")
      .withArrayOpen("[array ")
      .withArrayClose("]")
      .withObjectOpen("[struct ")
      .withObjectClose("]")
      .withLiteralKeys(true)
      .withKeyRegex("^[a-zA-Z_]+[a-zA-Z0-9_.\\-]*$");

  /**
   * Implementation class for tracking the state of JSON output
   * within a single JSON object or array.
   */
  private static final class State {
    private boolean inObject = false;
    private boolean inArray = false;
    private int level = 0;
    private int n = 0;
    private boolean sawKey = false;

    public State() { }

    private State(boolean inObj, boolean inArr, int lev) {
      inObject = inObj;
      inArray = inArr;
      level = lev;
    }

    private static void indent(Writer writer, String indentation, int level)
      throws IOException {
      for (; level > 0; --level) {
        writer.write(indentation);
      }
    }

    public void emitValue(Options opts, Writer writer, String value)
      throws IOException {
      if (inArray) {
        if (opts.prettyPrint()) {
          if (n > 0) {
            writer.write(opts.getSeparator());
          }
          writer.write("\n");
          indent(writer, opts.getIndentation(), level);
        } else {
          writer.write(n > 0 ? opts.getSeparator() : opts.getSpacer());
        }
      } else if (inObject && sawKey) {
        writer.write(opts.getKeyValueSeparator());
        sawKey = false;
      } else {
        throw new IllegalStateException(
          "JSON values can only be written in JSON arrays or after JSON " +
          "object keys");
      }
      writer.write(value);
      ++n;
    }

    public void emitKey(Options opts, Writer writer, String key)
      throws IOException {
      if (!(inObject && !sawKey)) {
        throw new IllegalStateException(
          "Key value pairs can only be written inside JSON objects");
      }
      if (opts.prettyPrint()) {
        if (n > 0) {
          writer.write(opts.getSeparator());
        }
        writer.write("\n");
        indent(writer, opts.getIndentation(), level);
      } else {
        writer.write(n == 0 ? opts.getSpacer() : opts.getSeparator());
      }
      writer.write(key);
      sawKey = true;
    }

    public State emitArray(Options opts, Writer writer) throws IOException {
      State nextState = this;
      if (inObject) {
        if (!sawKey) {
          throw new IllegalStateException(
            "JSON arrays cannot be written as JSON object keys");
        }
        writer.write(opts.getKeyValueSeparator());
        sawKey = false;
        ++n;
        nextState = new State(false, true, level + 1);
      } else if (inArray) {
        writer.write(n > 0 ? opts.getSeparator() : opts.getSpacer());
        ++n;
        nextState = new State(false, true, level + 1);
      } else {
        inArray = true;
        level = 1;
      }
      writer.write(opts.getArrayOpen());
      return nextState;
    }

    public State emitObject(Options opts, Writer writer) throws IOException {
      State nextState = this;
      if (inArray) {
        if (n > 0) {
          writer.write(opts.getSeparator());
        } else if (opts.prettyPrint()) {
          writer.write("\n");
          indent(writer, opts.getIndentation(), level);
        } else {
          writer.write(opts.getSpacer());
        }
        ++n;
        nextState = new State(true, false, level + 1);
      } else if (inObject) {
        if (!sawKey) {
          throw new IllegalStateException(
            "JSON objects cannot be written as JSON keys");
        }
        writer.write(opts.getKeyValueSeparator());
        ++n;
        sawKey = false;
        nextState = new State(true, false, level + 1);
      } else {
        inObject = true;
        level = 1;
      }
      writer.write(opts.getObjectOpen());
      return nextState;
    }

    public void finish(Options opts, Writer writer) throws IOException {
      if (!inArray && !inObject) {
        throw new IllegalStateException(
          "Cannot finish JSON object/array: no object or array is " +
          "currently open");
      }
      if (opts.prettyPrint() && n > 0) {
        writer.write('\n');
        indent(writer, opts.getIndentation(), level - 1);
      } else {
        writer.write(opts.getSpacer());
      }
      writer.write(inObject ? opts.getObjectClose() : opts.getArrayClose());
    }
  }

  private Writer w = null;
  private Options opts = null;
  private StringBuilder sb = new StringBuilder();
  private ArrayDeque<State> stack = new ArrayDeque<State>();

  /**
   * Creates a new JSONOutput that writes to the given OutputStream with
   * default formatting.
   */
  public JSONOutput(OutputStream stream) {
    this(stream, DEFAULT);
  }

  /**
   * Creates a new JSONOutput that writes to the given OutputStream using
   * the given formatting options.
   */
  public JSONOutput(OutputStream stream, Options options) {
    w = new OutputStreamWriter(stream, Charset.forName("UTF-8"));
    opts = options;
    stack.add(new State());
  }

  /**
   * Creates a new JSONOutput that writes to the given Writer with
   * default formatting.
   */
  public JSONOutput(Writer writer) {
    this(writer, DEFAULT);
  }

  /**
   * Creates a new JSONOutput that writes to the given Writer using
   * the given formatting options.
   */
  public JSONOutput(Writer writer, Options options) {
    w = writer;
    opts = options;
    stack.add(new State());
  }

  /**
   * Returns a copy of the formatting options associated with this
   * JSONOutput instance.
   */
  public Options getOptions() {
    return (Options) opts.clone();
  }

  private static final String ALWAYS_ESCAPE = "\b\f\n\r\t";
  private static final String ESCAPE_TO = "bfnrt";

  /**
   * Appends a JSON encoded version of <code>s</code> to <code>sb</code>.
   */
  public static void appendTo(StringBuilder sb, Options opts, String s) {
    sb.append('"');
    for (int i = 0; i < s.length(); ++i) {
      char c = s.charAt(i);
      if (c <= 0x1f || (c >= 0x7f && opts.escapeUnicode())) {
        int j = ALWAYS_ESCAPE.indexOf(c);
        if (j != -1) {
          sb.append('\\');
          sb.append(ESCAPE_TO.charAt(j));
        } else {
          sb.append("\\u");
          String hex = Integer.toHexString(c);
          for (int k = 0; k < 4 - hex.length(); ++k) {
            sb.append('0');
          }
          sb.append(hex);
        }
      } else {
        if ((c == '/' && opts.escapeSolidus()) || c == '\\' || c == '"') {
          sb.append('\\');
        }
        sb.append(c);
      }
    }
    sb.append('"');
  }

  /**
   * Returns a JSON encoded version of <code>s</code>.
   */
  public String encode(String s) {
    sb.delete(0, sb.length());
    appendTo(sb, opts, s);
    return sb.toString();
  }

  // -- Outputting simple values ----

  protected State peek() {
    State state = stack.peek();
    if (state == null) {
      throw new IllegalStateException(
        "JSONOutput instance has already completely written its output " +
        "and is closed - no further writes are allowed.");
    }
    return state;
  }

  protected JSONOutput emitNull() throws IOException {
    peek().emitValue(opts, w, "null");
    return this;
  }

  protected void emitValue(String val) throws IOException {
    peek().emitValue(opts, w, val);
  }

  public JSONOutput value(boolean val) throws IOException {
    emitValue(Boolean.toString(val));
    return this;
  }

  public JSONOutput value(byte val) throws IOException {
    return value((long) val);
  }

  public JSONOutput value(char val) throws IOException {
    return value(new String(new char[]{val}));
  }

  public JSONOutput value(short val) throws IOException {
    return value((long) val);
  }

  public JSONOutput value(int val) throws IOException {
    return value((long) val);
  }

  public JSONOutput value(long val) throws IOException {
    emitValue(Long.toString(val));
    return this;
  }

  public JSONOutput value(float val) throws IOException {
    return value((double) val);
  }

  public JSONOutput value(double val) throws IOException {
    emitValue(Double.toString(val));
    return this;
  }

  public JSONOutput value(Boolean val) throws IOException {
    if (val == null) {
      return emitNull();
    }
    return value(val.booleanValue());
  }

  public JSONOutput value(Byte val) throws IOException {
    if (val == null) {
      return emitNull();
    }
    return value(val.byteValue());
  }

  public JSONOutput value(Character val) throws IOException {
    if (val == null) {
      return emitNull();
    }
    return value(val.charValue());
  }

  public JSONOutput value(Short val) throws IOException {
    if (val == null) {
      return emitNull();
    }
    return value(val.shortValue());
  }

  public JSONOutput value(Integer val) throws IOException {
    if (val == null) {
      return emitNull();
    }
    return value(val.intValue());
  }

  public JSONOutput value(Long val) throws IOException {
    if (val == null) {
      return emitNull();
    }
    return value(val.longValue());
  }

  public JSONOutput value(Float val) throws IOException {
    if (val == null) {
      return emitNull();
    }
    return value(val.floatValue());
  }

  public JSONOutput value(Double val) throws IOException {
    if (val == null) {
      return emitNull();
    }
    return value(val.doubleValue());
  }

  public JSONOutput value(Object val) throws IOException {
    if (val == null) {
      emitNull();
    } else if (val instanceof Boolean) {
      value((Boolean) val);
    } else if (val instanceof Byte) {
      value((Byte) val);
    } else if (val instanceof Character) {
      value((Character) val);
    } else if (val instanceof Short) {
      value((Short) val);
    } else if (val instanceof Integer) {
      value((Integer) val);
    } else if (val instanceof Long) {
      value((Long) val);
    } else if (val instanceof Float) {
      value((Float) val);
    } else if (val instanceof Double) {
      value((Double) val);
    } else if (val instanceof JSONWritable) {
      write((JSONWritable) val);
    } else {
      emitValue(encode(val.toString()));
    }
    return this;
  }

  // -- Outputting arrays of values ----

  private void emitValues(String[] vals, int width) throws IOException {
    State state = peek();
    for (String v : vals) {
      int n = width - v.length();
      if (n > 0) {
        sb.delete(0, sb.length());
        for (; n > 0; --n) {
          sb.append(' ');
        }
        sb.append(v);
        state.emitValue(opts, w, sb.toString());
      } else {
        state.emitValue(opts, w, v);
      }
    }
  }

  public JSONOutput array() throws IOException {
    State state = peek();
    State next = state.emitArray(opts, w);
    if (next != state) {
      stack.push(next);
    }
    return this;
  }

  public JSONOutput array(boolean[] val) throws IOException {
    return array(val, 0, val.length);
  }

  public JSONOutput array(byte[] val) throws IOException {
    return array(val, 0, val.length);
  }

  public JSONOutput array(char[] val) throws IOException {
    return array(val, 0, val.length);
  }

  public JSONOutput array(short[] val) throws IOException {
    return array(val, 0, val.length);
  }

  public JSONOutput array(int[] val) throws IOException {
    return array(val, 0, val.length);
  }

  public JSONOutput array(long[] val) throws IOException {
    return array(val, 0, val.length);
  }

  public JSONOutput array(float[] val) throws IOException {
    return array(val, 0, val.length);
  }

  public JSONOutput array(double[] val) throws IOException {
    return array(val, 0, val.length);
  }

  public JSONOutput array(Object[] val) throws IOException {
    return array(val, 0, val.length);
  }

  public JSONOutput array(JSONWritable[] val) throws IOException {
    return array(val, 0, val.length);
  }

  public JSONOutput array(boolean[] val, int from, int to) throws IOException {
    array();
    for (int i = from; i < to; ++i) {
      value(val[i]);
    }
    return finish();
  }

  public JSONOutput array(byte[] val, int from, int to) throws IOException {
    array();
    if (opts.alignValues()) {
      String[] strings = new String[val.length];
      int width = 0;
      for (int i = 0; i < val.length; ++i) {
        strings[i] = Byte.toString(val[i]);
        width = Math.max(width, strings[i].length());
      }
      emitValues(strings, width);
    } else {
      for (byte v : val) {
        value(v);
      }
    }
    return finish();
  }

  public JSONOutput array(char[] val, int from, int to) throws IOException {
    array();
    for (int i = from; i < to; ++i) {
      value(val[i]);
    }
    return finish();
  }

  public JSONOutput array(short[] val, int from, int to) throws IOException {
    array();
    if (opts.alignValues()) {
      String[] strings = new String[val.length];
      int width = 0;
      for (int i = 0; i < val.length; ++i) {
        strings[i] = Short.toString(val[i]);
        width = Math.max(width, strings[i].length());
      }
      emitValues(strings, width);
    } else {
      for (short v : val) {
        value(v);
      }
    }
    return finish();
  }

  public JSONOutput array(int[] val, int from, int to) throws IOException {
    array();
    if (opts.alignValues()) {
      String[] strings = new String[val.length];
      int width = 0;
      for (int i = 0; i < val.length; ++i) {
        strings[i] = Integer.toString(val[i]);
        width = Math.max(width, strings[i].length());
      }
      emitValues(strings, width);
    } else {
      for (int v : val) {
        value(v);
      }
    }
    return finish();
  }

  public JSONOutput array(long[] val, int from, int to) throws IOException {
    array();
    if (opts.alignValues()) {
      String[] strings = new String[val.length];
      int width = 0;
      for (int i = 0; i < val.length; ++i) {
        strings[i] = Long.toString(val[i]);
        width = Math.max(width, strings[i].length());
      }
      emitValues(strings, width);
    } else {
      for (long v : val) {
        value(v);
      }
    }
    return finish();
  }

  public JSONOutput array(float[] val, int from, int to) throws IOException {
    array();
    if (opts.alignValues()) {
      String[] strings = new String[val.length];
      int width = 0;
      for (int i = 0; i < val.length; ++i) {
        strings[i] = Float.toString(val[i]);
        width = Math.max(width, strings[i].length());
      }
      emitValues(strings, width);
    } else {
      for (float v : val) {
        value(v);
      }
    }
    return finish();
  }

  public JSONOutput array(double[] val, int from, int to) throws IOException {
    array();
    if (opts.alignValues()) {
      String[] strings = new String[val.length];
      int width = 0;
      for (int i = 0; i < val.length; ++i) {
        strings[i] = Double.toString(val[i]);
        width = Math.max(width, strings[i].length());
      }
      emitValues(strings, width);
    } else {
      for (double v : val) {
        value(v);
      }
    }
    return finish();
  }

  public JSONOutput array(Object[] val, int from, int to) throws IOException {
    array();
    for (int i = from; i < to; ++i) {
      value(val[i]);
    }
    return finish();
  }

  public JSONOutput array(JSONWritable[] val, int from, int to)
    throws IOException {
    array();
    for (int i = from; i < to; ++i) {
      write(val[i]);
    }
    return finish();
  }

  public JSONOutput array(Collection<?> val) throws IOException {
    array();
    for (Object v : val) {
      if (v == null) {
        emitNull();
      } else if (v instanceof Boolean) {
        value((Boolean) v);
      } else if (v instanceof Byte) {
        value((Byte) v);
      } else if (v instanceof Character) {
        value((Character) v);
      } else if (v instanceof Short) {
        value((Short) v);
      } else if (v instanceof Integer) {
        value((Integer) v);
      } else if (v instanceof Long) {
        value((Long) v);
      } else if (v instanceof Float) {
        value((Float) v);
      } else if (v instanceof Double) {
        value((Double) v);
      } else if (v instanceof JSONWritable) {
        write((JSONWritable) v);
      } else {
        value(v);
      }
    }
    return finish();
  }

  // -- Outputting keys ----

  public JSONOutput key(String k) throws IOException {
    if (k == null) {
      throw new IllegalArgumentException("Null JSON key.");
    }
    Pattern pat = opts.getKeyPattern();
    if (pat != null && !pat.matcher(k).matches()) {
      throw new IllegalArgumentException(String.format(
        "Invalid JSON key %s: doesn't match pattern %s", k, pat));
    }
    peek().emitKey(opts, w, opts.literalKeys() ? k : encode(k));
    return this;
  }

  // -- Outputting key/value pairs ----

  public JSONOutput pair(String k, byte val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, char val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, short val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, int val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, long val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, float val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, double val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, Boolean val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, Byte val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, Character val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, Short val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, Integer val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, Long val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, Float val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, Double val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, Object val) throws IOException {
    key(k);
    return value(val);
  }

  public JSONOutput pair(String k, JSONWritable val) throws IOException {
    key(k);
    return write(val);
  }

  // -- Outputting JSON objects ----

  public JSONOutput write(JSONWritable val) throws IOException {
    val.writeJSON(this);
    return this;
  }

  public JSONOutput object() throws IOException {
    State state = peek();
    State next = state.emitObject(opts, w);
    if (next != state) {
      stack.push(next);
    }
    return this;
  }

  public JSONOutput object(Map<String, ?> map) throws IOException {
    object();
    for (Map.Entry<String, ?> e : map.entrySet()) {
      key(e.getKey());
      Object v = e.getValue();
      if (v == null) {
        emitNull();
      } else if (v instanceof Boolean) {
        value((Boolean) v);
      } else if (v instanceof Byte) {
        value((Byte) v);
      } else if (v instanceof Character) {
        value((Character) v);
      } else if (v instanceof Short) {
        value((Short) v);
      } else if (v instanceof Integer) {
        value((Integer) v);
      } else if (v instanceof Long) {
        value((Long) v);
      } else if (v instanceof Float) {
        value((Float) v);
      } else if (v instanceof Double) {
        value((Double) v);
      } else if (v instanceof JSONWritable) {
        write((JSONWritable) v);
      } else {
        value(v);
      }
    }
    finish();
    return this;
  }

  /**
   * Returns a JSON string representation of <code>writable</code>.
   */
  public static String toString(JSONWritable writable, Options opts) {
    try {
      StringWriter w = new StringWriter();
      JSONOutput out = new JSONOutput(w, opts);
      writable.writeJSON(out);
      out.close();
      return w.toString();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  /**
   * Returns a JSON string representation of
   * <code>writable</code>.
   */
  public static String toString(JSONWritable writable) {
    return toString(writable, DEFAULT);
  }

  // -- Closing JSON objects/arrays ----

  /**
   * Finish the currently open JSON object or array. Note that the
   * underlying stream is automatically flushed when the last open
   * JSON object or array is finished.
   */
  public JSONOutput finish() throws IOException {
    return finish(1);
  }

  /**
   * Finish <code>n</code> currently open JSON objects or arrays.
   * Note that the underlying stream is automatically flushed
   * when the last open JSON object or array is finished.
   */
  public JSONOutput finish(int n) throws IOException {
    if (n < 0) {
      throw new IllegalArgumentException(
        "Cannot finish a negative number of JSON objects/arrays");
    }
    if (n > stack.size()) {
      throw new IllegalArgumentException(String.format(
        "Cannot finish %d JSON objects/arrays: only %d are currently open",
        n, stack.size()));
    }
    for (; n > 0; --n) {
      stack.pop().finish(opts, w);
    }
    if (stack.size() == 0) {
      w.flush();
    }
    return this;
  }

  /**
   * Finish all currently open JSON objects or arrays. After calling this
   * method, further writes to this JSONOutput will fail with an exception.
   * The underlying stream is automatically flushed.
   */
  public JSONOutput finishAll() throws IOException {
    return finish(stack.size());
  }

  /**
   * Closes the underlying stream immediately (without finishing open
   * JSON objects or arrays).  A JSONOutput can safely be closed multiple
   * times - only the first call will have an effect.
   */
  @Override public void close() throws IOException {
    w.close();
  }

  /**
   * Flushes the underlying stream. Flushing a finished or closed JSONOutput
   * has no effect. Note that the underlying stream is automatically flushed
   * when the last open JSON object or array is finished.
   */
  @Override public void flush() throws IOException {
    if (stack.size() > 0) {
      w.flush();
    }
  }
}
