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
 * $Id: EncodedExplorationMode.java 5479 2014-07-19 12:20:13Z rensink $
 */
package groove.explore.encode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Used for setting on the fly or offline model exploration.
 * @author Vincent de Bruijn
 * @version $Revision $
 */
public class EncodedExplorationMode extends EncodedFixedEnumeratedType<Boolean> {

    /**
     * Keyword for an on the fly model exploration mode, the exploration
     * strategy is obtained from the remote server.
     */
    public static final String POSITIVE = "On-the-fly";

    /**
    * Keyword for an offline model exploration mode, the exploration is done 
    * locally, the result sent to the remote server.
    */
    public static final String NEGATIVE = "Offline";

    private static final String POSITIVE_TEXT =
        "Online: the model is explored on the fly.";
    private static final String NEGATIVE_TEXT =
        "Offline: the model is explored locally.";

    @Override
    public Map<String,String> fixedOptions() {
        Map<String,String> result = new LinkedHashMap<String,String>();
        result.put(POSITIVE, POSITIVE_TEXT);
        result.put(NEGATIVE, NEGATIVE_TEXT);
        return result;
    }

    @Override
    public Map<String,Boolean> fixedValues() {
        Map<String,Boolean> result = new LinkedHashMap<String,Boolean>();
        result.put(POSITIVE, true);
        result.put(NEGATIVE, false);
        return result;
    }

}
