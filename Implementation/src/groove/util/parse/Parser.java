/*
 * GROOVE: GRaphs for Object Oriented VErification Copyright 2003--2007
 * University of Twente
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * $Id: Parser.java 5628 2014-11-04 09:59:50Z rensink $
 */
package groove.util.parse;

import groove.io.HTMLConverter;
import groove.util.Groove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface for basic parser functionality.
 * @param <T> the type being parsed
 * @author Arend Rensink
 * @version $Id $
 */
abstract public interface Parser<T> {
    /**
     * Returns a HTML-formatted description of the parsable strings, starting with uppercase.
     */
    public String getDescription();

    /**
     * Indicates if a given (possibly {@code null}) textual value can be parsed.
     * The {@code null} and empty string can only be accepted if the parser
     * has a default value.
     */
    public boolean accepts(String text);

    /**
     * Converts a given (possibly {@code null}) textual value to an instance of the
     * type of this parser.
     * Will return the default value on {@code null} or the empty string, if
     * the parser has a default value.
     * @param input the text to be parsed; if {@code null} or empty,
     * @return a value corresponding to {@code text}
     */
    public T parse(String input) throws FormatException;

    /**
     * Turns a given value into a string that, when fed into {@link #parse(String)},
     * will return the original value.
     * @param value a non-{@code null} value of the type of this parser;
     * should satisfy {@link #isValue(Object)}
     */
    public String toParsableString(Object value);

    /** Returns the type of values that this parser parses to. */
    public Class<? extends T> getValueType();

    /**
     * Tests if a given value of the type of this parser can be
     * represented as a string that can be parsed back.
     */
    public boolean isValue(Object value);

    /**
     * Indicates if this parser has a default value.
     * If there is a default value, then this is the value that both
     * {@code null} and the empty string parse to.
     */
    public boolean hasDefault();

    /** Tests whether a given value is the default value. */
    public boolean isDefault(Object value);

    /**
     * Returns the default value of this parser, if any.
     * If the empty and {@code null} string are accepted, then they parse to this value.
     * Only valid if the parser has a default value according to {@link #hasDefault()}.
     * @return the default value if {@link #hasDefault()} holds; may be {@code null},
     * so use {@link #isDefault(Object)} rather than {@link Object#equals(Object)} to test equality!
     * @throws UnsupportedOperationException if the parser has no default value
     * @see #hasDefault()
     */
    public T getDefaultValue() throws UnsupportedOperationException;

    /**
     * Returns a human-readable string representation of the default value.
     * The default string may be (but does not have to be) the empty string.
     * Only valid if the parser has a default value according to {@link #hasDefault()}.
     * Note that if the empty string is accepted, it parses to the same object
     * as the default string.
     * @return a non-{@code null} string representation of the default value
     * @see #hasDefault()
     * @see #getDefaultValue()
     * @throws UnsupportedOperationException if the parser has no default value
     */
    public String getDefaultString() throws UnsupportedOperationException;

    /** Integer number parser. */
    public static IntParser integer = new IntParser(true);
    /** Natural number parser. */
    public static IntParser natural = new IntParser(false);
    /** Splitting parser based on whitespace. */
    public static SplitParser splitter = new SplitParser();
    /** Boolean parser with default value {@code false}. */
    public static BooleanParser boolTrue = new BooleanParser(true);
    /** Boolean parser with default value {@code true}. */
    public static BooleanParser boolFalse = new BooleanParser(false);

    /** Identity string parser. */
    static abstract public class AbstractStringParser<S> implements Parser<S> {
        /**
         * Constructor for subclassing.
         * @param trim if {@code true}, spaces are stripped of the values.
         */
        protected AbstractStringParser(Class<S> valueType, boolean trim) {
            this(valueType, "", trim);
        }

        /**
         * Constructor for subclassing.
         * @param defaultString the default string value
         * @param trim if {@code true}, spaces are stripped of the values.
         */
        protected AbstractStringParser(Class<S> valueType, String defaultString, boolean trim) {
            this.trim = trim;
            this.valueType = valueType;
            this.defaultValue = createContent(defaultString);
            this.defaultString = defaultString;
        }

        private final boolean trim;

        @Override
        public boolean accepts(String text) {
            return true;
        }

        @Override
        public S parse(String input) {
            if (input == null || input.length() == 0) {
                return getDefaultValue();
            } else {
                return createContent(this.trim ? input.trim() : input);
            }
        }

        /** Callback factory method to create the right content object. */
        protected abstract S createContent(String value);

        @Override
        public String getDescription() {
            return "Any string value";
        }

        @Override
        public Class<S> getValueType() {
            return this.valueType;
        }

        private final Class<S> valueType;

        @Override
        public boolean hasDefault() {
            return true;
        }

        @Override
        public S getDefaultValue() {
            return this.defaultValue;
        }

        private final S defaultValue;

        @Override
        public String getDefaultString() {
            return this.defaultString;
        }

        private final String defaultString;

        @Override
        public boolean isDefault(Object value) {
            return getDefaultValue().equals(value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public String toParsableString(Object value) {
            return "" + extractValue((S) value);
        }

        /** Callback method to extract an integer value from a content object. */
        protected abstract String extractValue(S content);
    }

    /** Integer parser. */
    static abstract public class AbstractIntParser<I> implements Parser<I> {
        /** Creates a parser, with a parameter to determine if
         * negative values are allowed.
         * @param neg if {@code true}, the parser allows negative values.
         */
        protected AbstractIntParser(Class<I> valueType, int defaultValue, boolean neg) {
            this.valueType = valueType;
            this.neg = neg;
            this.defaultValue = createContent(defaultValue);
            this.defaultString = "" + defaultValue;
        }

        /**
         * Indicates if negative numbers are allowed.
         */
        final protected boolean allowsNeg() {
            return this.neg;
        }

        private final boolean neg;

        @Override
        public boolean accepts(String text) {
            if (text == null || text.length() == 0) {
                return true;
            }
            try {
                int number = Integer.parseInt(text);
                return this.neg || number >= 0;
            } catch (NumberFormatException ext) {
                return false;
            }
        }

        @Override
        public I parse(String input) throws FormatException {
            if (input == null || input.length() == 0) {
                return getDefaultValue();
            } else {
                try {
                    return createContent(Integer.parseInt(input));
                } catch (NumberFormatException exc) {
                    throw new FormatException(exc.getMessage());
                }
            }
        }

        /** Callback factory method to create the right content object. */
        protected abstract I createContent(int value);

        @Override
        public String getDescription() {
            StringBuffer result = new StringBuffer(this.neg ? "Integer value" : "Natural number");
            result.append(" (default " + getDefaultString() + ")");
            return result.toString();
        }

        @Override
        public Class<I> getValueType() {
            return this.valueType;
        }

        private final Class<I> valueType;

        @Override
        public boolean hasDefault() {
            return true;
        }

        @Override
        public I getDefaultValue() {
            return this.defaultValue;
        }

        private final I defaultValue;

        @Override
        public String getDefaultString() {
            return this.defaultString;
        }

        private final String defaultString;

        @Override
        public boolean isDefault(Object value) {
            return value != null && value.equals(getDefaultValue());
        }

        @SuppressWarnings("unchecked")
        @Override
        public String toParsableString(Object value) {
            return "" + extractValue((I) value);
        }

        /** Callback method to extract an integer value from a content object. */
        protected abstract int extractValue(I content);
    }

    /** Integer parser. */
    static public class IntParser extends AbstractIntParser<Integer> {
        private IntParser(boolean neg) {
            super(Integer.class, 0, neg);
        }

        @Override
        public boolean isValue(Object value) {
            return value instanceof Integer && (allowsNeg() || ((Integer) value).intValue() >= 0);
        }

        @Override
        public boolean isDefault(Object value) {
            return value instanceof Integer && ((Integer) value).intValue() == 0;
        }

        @Override
        protected Integer createContent(int value) {
            return new Integer(value);
        }

        @Override
        protected int extractValue(Integer content) {
            return content;
        }
    }

    /** Parser that concatenates and splits lines at whitespaces. */
    static public class SplitParser implements Parser<List<String>> {
        /** Constructs a parser. */
        @SuppressWarnings("unchecked")
        public SplitParser() {
            this.valueType = (Class<List<String>>) new ArrayList<String>().getClass();
        }

        @Override
        public boolean accepts(String text) {
            return true;
        }

        @Override
        public List<String> parse(String input) {
            try {
                return input == null || input.length() == 0 ? getDefaultValue()
                    : Arrays.asList(handler.split(input, " "));
            } catch (FormatException exc) {
                assert false; // we recognise no quotes or brackets, so exceptions can't occur
                return null;
            }
        }

        @Override
        public String getDescription() {
            return "A space-separated list of names";
        }

        @Override
        public String toParsableString(Object value) {
            return Groove.toString(((Collection<?>) value).toArray(), "", "", " ");
        }

        @Override
        public Class<List<String>> getValueType() {
            return this.valueType;
        }

        private final Class<List<String>> valueType;

        @Override
        public boolean isValue(Object value) {
            boolean result = value instanceof Collection;
            if (result) {
                for (Object part : (Collection<?>) value) {
                    if (!(part instanceof String) || ((String) part).indexOf(' ') >= 0) {
                        result = false;
                        break;
                    }
                }
            }
            return result;
        }

        @Override
        public boolean hasDefault() {
            return true;
        }

        @Override
        public List<String> getDefaultValue() {
            return Collections.<String>emptyList();
        }

        @Override
        public String getDefaultString() {
            return "";
        }

        @Override
        public boolean isDefault(Object value) {
            return value instanceof List && ((List<?>) value).size() == 0;
        }

        /** String parser recognising no quotes or brackets. */
        private static StringHandler handler = new StringHandler("");
    }

    /**
     * Parser for boolean values, with a default value for the empty string.
     * @author Arend Rensink
     * @version $Revision $
     */
    static public class BooleanParser implements Parser<Boolean> {
        /**
         * Constructs an instance that accepts the empty string as
         * a given default value.
         */
        public BooleanParser(boolean defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public String getDescription() {
            StringBuffer result = new StringBuffer("Either ");
            result.append(TRUE_LINE);
            if (this.defaultValue) {
                result.append(" (default)");
            }
            result.append(" or ").append(FALSE_LINE);
            if (!this.defaultValue) {
                result.append(" (default)");
            }
            return result.toString();
        }

        @Override
        public boolean accepts(String text) {
            return TRUE.equals(text) || FALSE.equals(text) || "".equals(text) || text == null;
        }

        @Override
        public Boolean parse(String input) {
            Boolean result = null;
            if (input == null || input.length() == 0) {
                result = getDefaultValue();
            } else if (TRUE.equals(input)) {
                result = true;
            } else if (FALSE.equals(input)) {
                result = false;
            }
            return result;
        }

        @Override
        public String toParsableString(Object value) {
            if (value.equals(getDefaultValue())) {
                return "";
            } else {
                return value.toString();
            }
        }

        @Override
        public Class<Boolean> getValueType() {
            return Boolean.class;
        }

        @Override
        public boolean isValue(Object value) {
            return value instanceof Boolean;
        }

        @Override
        public boolean hasDefault() {
            return true;
        }

        @Override
        public Boolean getDefaultValue() {
            return this.defaultValue;
        }

        @Override
        public String getDefaultString() {
            return "" + this.defaultValue;
        }

        @Override
        public boolean isDefault(Object value) {
            return value instanceof Boolean
                && ((Boolean) value).booleanValue() == this.defaultValue;
        }

        /** Value that the empty string converts to. */
        private final boolean defaultValue;

        /** Representation of <code>true</code>. */
        static private final String TRUE = Boolean.toString(true);
        static private final String TRUE_LINE = HTMLConverter.ITALIC_TAG.on(TRUE);
        /** Representation of <code>false</code>. */
        static private final String FALSE = Boolean.toString(false);
        static private final String FALSE_LINE = HTMLConverter.ITALIC_TAG.on(FALSE);
    }

    /**
     * Properties subclass that tests whether a given value is a correct value
     * of an {@link Enum} type (passed in as a type parameter).
     */
    static public class EnumParser<T extends Enum<T>> implements Parser<T> {
        /**
         * Constructs an instance with a flag to indicate if the empty string
         * should be approved.
         * @param enumType the enum type supported by this property
         * @param defaultValue the value of {@code T} represented
         * by the empty string
         */
        public EnumParser(Class<T> enumType, T defaultValue, String... texts) {
            this.defaultValue = defaultValue;
            this.toStringMap = new EnumMap<T,String>(enumType);
            this.toValueMap = new HashMap<String,T>();
            this.valueType = enumType;
            T[] values = enumType.getEnumConstants();
            assert values.length == texts.length;
            for (int i = 0; i < values.length; i++) {
                if (texts[i] != null) {
                    this.toStringMap.put(values[i], texts[i]);
                    T oldValue = this.toValueMap.put(texts[i], values[i]);
                    assert oldValue == null : "Duplicate key " + texts[i];
                }
            }
            this.toValueMap.put("", defaultValue);
            this.toValueMap.put(null, defaultValue);
        }

        /**
         * Constructs an instance with a flag to indicate if the empty string
         * should be approved.
         * @param enumType the enum type supported by this property
         * @param defaultValue if non-{@code null}, the value of {@code T} represented
         * by the empty string
         */
        public EnumParser(Class<T> enumType, T defaultValue) {
            this(enumType, defaultValue, camel(enumType.getEnumConstants()));
        }

        @Override
        public boolean hasDefault() {
            return true;
        }

        @Override
        public T getDefaultValue() {
            return this.defaultValue;
        }

        @Override
        public String getDefaultString() {
            String result = this.toStringMap.get(this.defaultValue);
            return result == null ? "" : result;
        }

        @Override
        public boolean isDefault(Object value) {
            return value == this.defaultValue;
        }

        /** Flag indicating if the empty string is approved. */
        private final T defaultValue;

        @Override
        public String getDescription() {
            StringBuffer result = new StringBuffer("One of ");
            int i = 0;
            for (Map.Entry<T,String> e : this.toStringMap.entrySet()) {
                result = result.append(HTMLConverter.ITALIC_TAG.on(e.getValue()));
                if (isDefault(e.getKey())) {
                    result.append(" (default)");
                }
                if (i < this.toStringMap.size() - 2) {
                    result.append(", ");
                } else if (i < this.toStringMap.size() - 1) {
                    result.append(" or ");
                }
                i++;
            }
            return result.toString();
        }

        @Override
        public boolean accepts(String text) {
            return this.toValueMap.containsKey(text);
        }

        @Override
        public T parse(String input) {
            return this.toValueMap.get(input);
        }

        @Override
        public String toParsableString(Object value) {
            return isDefault(value) ? "" : this.toStringMap.get(value);
        }

        @Override
        public Class<T> getValueType() {
            return this.valueType;
        }

        private final Class<T> valueType;

        @Override
        public boolean isValue(Object value) {
            return this.toStringMap.containsKey(value);
        }

        private final Map<T,String> toStringMap;
        private final Map<String,T> toValueMap;

        private static final <T extends Enum<T>> String[] camel(T[] vals) {
            String[] result = new String[vals.length];
            for (int i = 0; i < vals.length; i++) {
                result[i] = StringHandler.toCamel(vals[i].name());
            }
            return result;
        }
    }
}
