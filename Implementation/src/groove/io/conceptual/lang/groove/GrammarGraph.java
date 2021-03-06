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
 * $Id: GrammarGraph.java 5479 2014-07-19 12:20:13Z rensink $
 */
package groove.io.conceptual.lang.groove;

import groove.graph.GraphRole;
import groove.io.conceptual.Acceptor;
import groove.io.conceptual.graph.AbsGraph;
import groove.io.conceptual.graph.AbsNode;

import java.util.HashMap;
import java.util.Map;

public class GrammarGraph {
    public AbsGraph m_graph;
    public String m_graphName;
    public GraphRole m_graphRole;

    public Map<Acceptor,AbsNode> m_nodes = new HashMap<Acceptor,AbsNode>();
    // Node array map. Used by instance models for container values
    public Map<Acceptor,AbsNode[]> m_multiNodes = new HashMap<Acceptor,AbsNode[]>();

    public GrammarGraph(String graphName, GraphRole graphRole) {
        m_graph = new AbsGraph();
        m_graphName = graphName;
        m_graphRole = graphRole;
    }

    public GrammarGraph(AbsGraph graph, String graphName, GraphRole graphRole) {
        m_graph = graph;
        m_graphName = graphName;
        m_graphRole = graphRole;
    }

    public AbsGraph getGraph() {
        // Reset the graph and rebuild it from the nodes that were added to the node map
        m_graph.clear();
        for (AbsNode node : m_nodes.values()) {
            m_graph.addNode(node);
        }
        // Also for node arrays
        for (AbsNode[] nodes : m_multiNodes.values()) {
            for (AbsNode node : nodes) {
                m_graph.addNode(node);
            }
        }
        return m_graph;
    }

    public String getGraphName() {
        return m_graphName;
    }

    public GraphRole getGraphRole() {
        return m_graphRole;
    }
}
