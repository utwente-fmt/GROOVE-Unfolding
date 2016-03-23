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
package groove.unfolding.dependency;

import groove.grammar.host.DefaultHostEdge;
import groove.grammar.host.HostEdge;
import groove.grammar.host.HostEdgeSet;
import groove.grammar.host.HostGraph;
import groove.grammar.host.HostNode;
import groove.transform.Proof;
import groove.unfolding.tree.ADToolXMLWriter;
import groove.unfolding.tree.DepStruct2Tree;
import groove.unfolding.tree.Tree;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DepUTIL {

    /**
     * Method that constructs a 'depedency structure' for a given proof.
     * @param proof The proof for which the structure is created (e.g. a goal state match)
     * @return A Map that maps each Proof to the proofs it depends on.
     */
    public static Map<Proof,Set<Proof>> constructDependencyStructure(Proof proof) {
        Map dependencies = new LinkedHashMap<Proof,Set<Proof>>();
        findDependency(dependencies, proof);
        return dependencies;
    }

    public static LinkedHashSet<Proof> constructDependencySet(Proof proof) {
        Map dependencies = new LinkedHashMap<Proof,Set<Proof>>();
        findDependency(dependencies, proof);

        LinkedHashSet depSet = new LinkedHashSet<Proof>();
        addDependenciesToSet(depSet, dependencies, proof);
        return depSet;
    }

    private static void addDependenciesToSet(LinkedHashSet<Proof> depSet,
        Map<Proof,Set<Proof>> dependencies, Proof proof) {
        depSet.add(proof);
        for (Proof mapProofs : dependencies.get(proof)) {
            addDependenciesToSet(depSet, dependencies, mapProofs);
        }
    }

    static void findDependency(Map<Proof,Set<Proof>> dependencies, Proof proof) {

        // First develop a set of dependency Proofs.
        Set<Proof> dependencyProofs = new LinkedHashSet<Proof>();

        // For each edge of the context that is created by another proof
        // We add the proof that created that edge to the depedency proofs.
        for (HostEdge edge : proof.getEdgeValues()) {
            DefaultHostEdge dhe = (DefaultHostEdge) edge;
            if (dhe.isCreated()) {
                dependencyProofs.add(dhe.getProof());
            }
        }

        // We add the proof with it's set of depedencies to the depedency structure.
        dependencies.put(proof, dependencyProofs);

        // Then recursivly call this method for all of this rules depedencies.
        for (Proof dProof : dependencyProofs) {
            findDependency(dependencies, dProof);
        }
    }

    /**
     * A printout method for a given 'dependency structure'.
     * @param dependencies the dependency structure
     */
    public static void depenenciesToString(Map<Proof,Set<Proof>> dependencies) {
        for (Map.Entry<Proof,Set<Proof>> entry : dependencies.entrySet()) {
            System.out.print(entry.getKey().getRule().getFullName() + " depends on: ");
            for (Proof proof : entry.getValue()) {
                System.out.print(proofToString(proof) + " ");
            }
            System.out.println();
        }
    }

    public static String proofToString(Proof proof) {
        String result = "";
        result += proof.getRule().getFullName();
        //result += "\n[";
        result += "[";
        boolean first = true;
        for (HostNode host : proof.getNodeValues()) {
            if (first) {
                first = !first;
            } else {
                result += ";";
            }
            result += host.toString();
        }
        result += "]";

        return result;
    }

    /**
     * Method that prints the attack (route) for each of the goal states in the input
     * @param goalMatches set of Proofs for each match of goal state.
     */
    public static void depedenciesToAttacks(List<Proof> goalMatches) {
        // For a simple AT, we simply want all rules that make up the attack. We use a set to prefent duplicates.
        // We dont look at the order of the actions yet.

        Map<Proof,Set<Proof>> dependencies = null;
        int attackcount = 0;

        for (Proof goalMatch : goalMatches) {
            Set<Proof> attackSteps = new LinkedHashSet<Proof>();

            dependencies = new LinkedHashMap<Proof,Set<Proof>>();
            findDependency(dependencies, goalMatch);

            for (Map.Entry<Proof,Set<Proof>> entry : dependencies.entrySet()) {
                attackSteps.add(entry.getKey());
            }

            System.out.print("Attack " + (++attackcount) + ":");
            for (Proof proof : attackSteps) {
                System.out.print(proofToString(proof) + ", ");
            }
            System.out.println();
        }
    }

    public static void removeCycles(List<Proof> goalMatches) {

        Map<Proof,Set<String>> attacks = new LinkedHashMap<Proof,Set<String>>();

        //Set<Set<String>> attacks = new LinkedHashSet<Set<String>>();

        Map<Proof,Set<Proof>> dependencies = null;

        for (Proof goalMatch : goalMatches) {
            Set<String> attackSteps = new LinkedHashSet<String>();
            attacks.put(goalMatch, attackSteps);
            dependencies = new LinkedHashMap<Proof,Set<Proof>>();
            findDependency(dependencies, goalMatch);

            for (Map.Entry<Proof,Set<Proof>> entry : dependencies.entrySet()) {
                attackSteps.add(proofToString(entry.getKey()));
            }
        }

        for (Map.Entry<Proof,Set<String>> entry : attacks.entrySet()) {
            for (Map.Entry<Proof,Set<String>> entry2 : attacks.entrySet()) {
                if (entry2.getKey() != entry.getKey()
                    && entry2.getValue().contains(entry.getValue())) {
                    // Then entry2 contains cycles compared to entry.
                    // Entry 2 should be removed
                    attacks.remove(entry2.getKey());
                    goalMatches.remove(entry.getKey());
                }
            }
        }
    }

    public static void dependencyTreeToString(Map<String,Set<Set<String>>> dependencyTree,
            String depedencyKey, String Parent) {

            System.out.print(Parent + ": ");

            boolean first = true;
            for (Set<String> setDependencies : dependencyTree.get(depedencyKey)) {
                if (first) {
                    first = !first;
                } else {
                    System.out.print(" OR ");
                }
                System.out.print(setDependencies.toString());
            }
            System.out.println();

            for (Set<String> setDependencies : dependencyTree.get(depedencyKey)) {
                for (String dep : setDependencies) {
                    dependencyTreeToString(dependencyTree, depedencyKey + setDependencies + dep, dep);
                }

            }

        }
    
    public static Tree depedencyStructToTree(DependencyStructure depStruct, String root) {
        return DepStruct2Tree.constructTree(depStruct, root);
    }

    public static void treeToXML(Tree tree) {
        ADToolXMLWriter.Tree2ADToolXML(tree);
    }
}
