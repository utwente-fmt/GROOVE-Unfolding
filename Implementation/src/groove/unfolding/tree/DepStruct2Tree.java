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
package groove.unfolding.tree;

import groove.grammar.host.HostEdge;
import groove.transform.Proof;
import groove.unfolding.dependency.DepUTIL;
import groove.unfolding.dependency.Dependencies;
import groove.unfolding.dependency.DependencyStructure;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author s1018078
 * @version $Revision $
 */
public class DepStruct2Tree {

    public static Tree constructTree(DependencyStructure depStruct, String Key) {
        Tree result = new Tree();
        Node root = new Node(Key, NodeType.OR);
        result.setRoot(root);

        addDependenciesAsNodesToParentNode(depStruct, Key, root);

        return result;
    }

    public static void addDependenciesAsNodesToParentNode(DependencyStructure depStruct,
        String Key, Node n) {

        Map<String,Dependencies> deps = depStruct.getDepStruct();

        // For each depedencySet ds
        // IF ds.length() > 1
        // THEN we create new AND node, add it to parent. make it intermediate 'parent'

        // For each Dependency dep in ds.
        // if dep.NoDependencies()
        // then add dep to as node to parent

        // ELSE (if dep.HasDependencies())
        // THEN add intermedaite AND, add dep as child.
        // ALSO. add a second OR child. Call method again with depedencies and this OR node.

        // Depending on the casesus, we can do different things.
        for (Set<String> dependencySet : deps.get(Key).getDependencies()) {
            Node parent = n;

            // If the the dependencyset has multiple dependencies, we add intermediate AND
            if (dependencySet.size() > 1) {
                Node ANDNode = new Node("AND", NodeType.AND);
                parent.addChild(ANDNode);
                parent = ANDNode;
            }

            for (String dependency : dependencySet) {

                // Reset the parent for every new dependency
                Node runningParent = parent;

                boolean depedencySetEmpty = true;
                //                System.out.println("DependencySetTest key: " + Key + dependency + " result: "
                //                    + dependencyTree.get(Key + dependency));
                for (Set<String> dependencySet2 : deps.get(Key + dependencySet + dependency)
                    .getDependencies()) {
                    if (dependencySet2.size() > 0) {
                        depedencySetEmpty = false;
                    }
                }

                if (depedencySetEmpty) {
                    runningParent.addChild(new Node(dependency));
                } else {

                    // ADD intermediate AND node
                    Node ANDNode2 = new Node("SQAND", NodeType.AND);
                    runningParent.addChild(ANDNode2);
                    runningParent = ANDNode2;

                    Node backupRunningParent = runningParent;

                    // We then have to add the dependencies.
                    // If there are multiple set of dependencies, we add OR, otherwise not.
                    if (deps.get(Key + dependencySet + dependency).getDependencies().size() > 1) {

                    	// TODO: Implement a detection for when XOR gates should be used
//                        // Determining if XOR or OR gate.
//                        LinkedHashSet<Map<HostEdge,Proof>> ConsumedElementMapSet =
//                            new LinkedHashSet<Map<HostEdge,Proof>>();
//
//                        for (Set<Proof> proofs : deps.get(Key + dependencySet + dependency)
//                            .getProofDependencies()) {
//                            LinkedHashMap<HostEdge,Proof> consumedElementMap =
//                                new LinkedHashMap<HostEdge,Proof>();
//                            ConsumedElementMapSet.add(consumedElementMap);
//
//                            for (Proof proof : proofs) {
//                                consumedElementMap.putAll(proof.getConsumedElementsMap());
//                            }
//                        }
//
//                        Node ORNode = null;
//                        if (DepUTIL.findConflict4(ConsumedElementMapSet)) {
//                            ORNode = new Node("XOR", NodeType.OR);
//                        } else {
//                            ORNode = new Node("OR", NodeType.OR);
//                        }



                    	// For now. Simply always add an OR gate.
                        Node ORNode = new Node("OR", NodeType.OR);
                            
                        runningParent.addChild(ORNode);
                        runningParent = ORNode;
                    }

                    addDependenciesAsNodesToParentNode(depStruct,
                        Key + dependencySet + dependency,
                        runningParent);

                    // Finally Add dependency itself as the final child.
                    backupRunningParent.addChild(new Node(dependency));

                }

            }

        }
    }

    public static void treeToString(Node node) {
        System.out.print("Node \\w l:" + node.getLabel() + ", t:" + node.getNodeType()
            + " has children: ");
        for (Node c : node.getChildren()) {
            System.out.print("(" + "Node \\w l:" + c.getLabel() + ", t:" + c.getNodeType() + ")");
        }
        System.out.println();

        for (Node c : node.getChildren()) {
            treeToString(c);
        }
    }

}
