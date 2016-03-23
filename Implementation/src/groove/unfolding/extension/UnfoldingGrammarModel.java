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
 * $Id$
 */
package groove.unfolding.extension;

import groove.grammar.aspect.AspectEdge;
import groove.grammar.aspect.AspectGraph;
import groove.grammar.aspect.AspectKind;
import groove.grammar.aspect.AspectLabel;
import groove.grammar.aspect.AspectParser;
import groove.grammar.model.GrammarModel;
import groove.grammar.model.GraphBasedModel;
import groove.grammar.model.HostModel;
import groove.grammar.model.RuleModel;
import groove.grammar.model.TypeModel;
import groove.io.store.SystemStore;
import groove.io.store.SystemStoreFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Alternative GrammarModel that modifies some production rules on loading.
 */
public class UnfoldingGrammarModel extends GrammarModel {

    public UnfoldingGrammarModel(SystemStore store) {
        super(store);
    }

    /**
     * Creates a graph-based resource model for a given graph.
     */
    @Override
    public GraphBasedModel<?> createGraphModel(AspectGraph graph) {
        GraphBasedModel<?> result = null;
        switch (graph.getRole()) {
        case HOST:
            result = new HostModel(this, graph);
            break;
        case RULE:
            if (graph.getName().equals("goal")) {
                removeReadingEdge(graph);
            } 
            // TODO: Use if we support embargo edges
            // Otherwise use it to detect invalid rule elements.
//            else {
//                removeEmbargoEdges(graph);
//            }
            result = new RuleModel(this, graph);
            break;
        case TYPE:
            result = new TypeModel(this, graph);
            break;
        default:
            assert false;
        }
        return result;
    }

    public void removeReadingEdge(AspectGraph graph) {

        // Change atleast one edge to a 'remove' edge
        AspectEdge newEdge = null;
        AspectEdge oldEdge = null;
        for (AspectEdge edge : graph.edgeSet()) {
            if (edge.source() != edge.target()) {

                AspectLabel label =
                    AspectParser.getInstance().parse("del:\n" + edge.label(), graph.getRole());

                oldEdge = edge;

                newEdge =
                    new AspectEdge(edge.source().clone(), label, edge.target().clone(),
                        edge.getNumber());
            }

        }
        graph.removeEdge(oldEdge);
        graph.addEdgeContext(newEdge);

    }

    public void removeEmbargoEdges(AspectGraph graph) {
        // For now, we only delete embargo edges ( perhaps embargo nodes lateron)
        Set<AspectEdge> embaroEdges = new LinkedHashSet<AspectEdge>();
        for (AspectEdge edge : graph.edgeSet()) {
            if (edge.source() != edge.target()) {

                if (edge.getKind() == AspectKind.EMBARGO) {
                    embaroEdges.add(edge);
                }
            }

        }
        graph.removeEdgeSet(embaroEdges);
    }

    /**
     * Creates an instance based on a given file.
     * @param file the file to load the grammar from
     * @throws IOException if the store exists  does not contain a grammar
     */
    static public UnfoldingGrammarModel newInstance(File file) throws IOException {
        return newInstance(file, false);
    }

    /**
     * Creates an instance based on a given file.
     * @param file the file to load the grammar from
     * @param create if <code>true</code> and <code>file</code> does not yet
     *        exist, attempt to create it.
     * @throws IOException if an error occurred while creating the store, or
     * if the store exists but does not contain a grammar
     */
    static public UnfoldingGrammarModel newInstance(File file, boolean create) throws IOException {
        SystemStore store = SystemStoreFactory.newStore(file, create);
        store.reload();
        UnfoldingGrammarModel result = store.toUnfoldingGrammarModel();
        return result;
    }
}
