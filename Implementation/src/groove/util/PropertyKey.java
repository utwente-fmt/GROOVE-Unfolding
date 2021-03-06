/* GROOVE: GRaphs for Object Oriented VErification
 * Copyright 2003--2011 University of Twente
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * $Id: PropertyKey.java 5479 2014-07-19 12:20:13Z rensink $
 */
package groove.util;

import groove.util.parse.ParsableKey;

/**
 * Interface for property keys; that is,
 * keys that are used in a {@link Properties} object.
 * @author Arend Rensink
 * @version $Id: PropertyKey.java 5479 2014-07-19 12:20:13Z rensink $
 */
public interface PropertyKey<V> extends ParsableKey<V> {
    /** Short description for user consumption. */
    public String getKeyPhrase();

    /** Indicates if this is a system key. */
    public boolean isSystem();

    /**
     * Start character that distinguishes system properties from user-definable
     * properties. Any string starting with this character is a system key.
     */
    static public final String SYSTEM_KEY_PREFIX = "$";
}
