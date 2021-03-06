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
 * $Id: ValueOracle.java 5479 2014-07-19 12:20:13Z rensink $
 */
package groove.match;

import groove.algebra.Constant;
import groove.grammar.Condition;
import groove.grammar.rule.VariableNode;

/** Interface to provide values for unbound variable nodes during matching. */
public interface ValueOracle {
    /** Returns a range of possible values for a given variable node. */
    Iterable<Constant> getValues(Condition condition, VariableNode var);
}
