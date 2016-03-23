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

import groove.transform.Proof;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author s1018078
 * @version $Revision $
 */
public class DependencyStructure {

    private Map<String,Dependencies> dependencyStructure = new LinkedHashMap<String,Dependencies>();

    public DependencyStructure(List<Proof> goalStateMatches) {
        initalize(goalStateMatches);
    }

    public Map<String,Dependencies> getDepStruct() {
        return this.dependencyStructure;
    }

    public void setDepStruct(Map<String,Dependencies> dependencyStructure) {
        this.dependencyStructure = dependencyStructure;
    }


    /***
     * INITALIZE METHODS
     */

    public void initalize(List<Proof> goalStateMatches) {
        // Initialize reusable map
        Map<Proof,Set<Proof>> goalStateDepedencies = null;

        // For each match of the goal state, we construct it's dependency structure and add it to the shared structure
        for (Proof goalMatch : goalStateMatches) {

            // Construct the dependency structure for this goal state match.
            goalStateDepedencies = new LinkedHashMap<Proof,Set<Proof>>();
            DepUTIL.findDependency(goalStateDepedencies, goalMatch);

            // We then add the individual dependency structure to the dependencyTree.
            addDependencyToTree(goalStateDepedencies, goalMatch, "");

        }
    }

    private void addDependencyToTree(Map<Proof,Set<Proof>> goalStateDepedencies, Proof dependency,
        String preString) {

        // Get the string representation of all direct dependencies
        Set<Proof> depedencies = goalStateDepedencies.get(dependency);
        Set<String> stringDepdencies = new LinkedHashSet<String>();
        for (Proof proof : depedencies) {
            stringDepdencies.add(DepUTIL.proofToString(proof));
        }

        // Determine the generic attack step key string of this element.
        String keyString = preString + DepUTIL.proofToString(dependency);

        // Determine if this generic attack step is already in the structure, if so it is added, otherwise new option is created
        if (getDepStruct().containsKey(keyString)) {
            getDepStruct().get(keyString).getDependencies().add(stringDepdencies);
            getDepStruct().get(keyString).getProofDependencies().add(depedencies);
        } else {
            Dependencies dependencies = new Dependencies();
            dependencies.getDependencies().add(stringDepdencies);
            dependencies.getProofDependencies().add(depedencies);

            getDepStruct().put(keyString, dependencies);

        }

        // Recursively add dependencies of the indirect dependencies of this element.
        for (Proof proof : depedencies) {
            addDependencyToTree(goalStateDepedencies, proof, keyString + stringDepdencies);
        }
    }

    /***
     * TRANSFORM TO DAG (Directed-Acyclic-Graph) METHODS
     */

    public void DependencyTreeToDAG(String rootString) {

        TraverseTree(rootString, new LinkedList<String>(), 0);

    }

    // Traverse a Tree by finding all it's elements as investigating them for possible shared subtrees to all subtrees in tailset.
    public void TraverseTree(String subTree, List<String> tailSubtree, int subTreeCount) {

        // First retrieve and convert all dependencies into orderd lists.
        Set<Set<String>> dependencySets = getDepStruct().get(subTree).getDependencies();
        List<List<String>> dependencyLists = new LinkedList<List<String>>();
        for (Set<String> dependencySet : dependencySets) {
            dependencyLists.add(new LinkedList<String>(dependencySet));
        }

        // Then create a set of all subtrees, as they have to be searched for shared subtrees too
        List<String> subSubTrees = new LinkedList<String>();
        for (List<String> dependencyList : dependencyLists) {
            for (String dependency : dependencyList) {
                subSubTrees.add(subTree + dependencyList + dependency);
            }
        }

        // Then dive into the dependencies of this subtree and determine if they describe shared subtrees.
        for (Set<String> dependencySet : dependencySets) {
            for (String dependency : dependencySet) {
                String dependencyKey = subTree + dependencySet + dependency;

                // Remove this dependencies subsubset (does not have to be searched).
                subSubTrees.remove(dependencyKey);

                // extend the subtrees that need to be searched with this dependencies siblings.
                List<String> extendedTailSubTrees = new LinkedList<String>(tailSubtree);
                extendedTailSubTrees.addAll(subSubTrees);

                // Only investigate the dependency if it has dependencies itself.
                if (DependencyHasDependencies(dependencyKey)) {

                    boolean sharedDependencyFound = false;

                    // Determine if this dependency is contained in any of the other subtrees
                    for (String subtree : extendedTailSubTrees) {
                        sharedDependencyFound =
                            sharedDependencyFound
                                || FindDependencyInSubtree(subtree, dependency, subTreeCount);
                    }

                    // If this dependency describes a shared subtree, we rewrite it.
                    if (sharedDependencyFound) {
                        subTreeCount++;

                        // Rewrite the dependency to indicate it describes a subtree.
                        String oldDependencyName = dependency;
                        String newDependencyName = "<" + dependency + "-subTree>";

                        Set<String> dependencySet2 = new LinkedHashSet<String>(dependencySet);

                        dependencySet2.remove(oldDependencyName);
                        dependencySet2.add(newDependencyName);

                        // Rewrite all key's of this dependency set.
                        for (String dependency2 : dependencySet) {
                            String oldKey = subTree + dependencySet + dependency2;

                            if (!dependency2.equals(oldDependencyName)) {
                                String newKey = subTree + dependencySet2 + dependency2;
                                Dependencies dependencies = getDepStruct().get(oldKey);
                                getDepStruct().put(newKey, dependencies);

                                RewriteSubTree(oldKey, newKey);
                            } else {
                                String newKey = subTree + dependencySet2 + newDependencyName;
                                if (!getDepStruct().containsKey(newKey)) {
                                    Dependencies dependencies = getDepStruct().get(oldKey);
                                    getDepStruct().put(newKey, dependencies);
                                }
                                RewriteSubTree(oldKey, newKey);
                            }

                        }

                        // Finaly, change the dependensy set itself.
                        dependencySet.remove(oldDependencyName);
                        dependencySet.add(newDependencyName);

                        // And change the dependency key
                        dependencyKey = subTree + dependencySet2 + newDependencyName;

                    } else {
                        // This else prevents finding a subtree in a subtree, as this caused issues.
                        // Continue with traverse
                        TraverseTree(dependencyKey, extendedTailSubTrees, subTreeCount);
                    }

                }

            }
        }

    }

    // Find a common dependency subtree in the given subtree.
    // If found, remove the subtree.
    public boolean FindDependencyInSubtree(String rootString, String commmonDependency,
        int subTreeCount) {

        boolean sharedDependencyFound = false;

        // Search through all elements if this subtree
        for (Set<String> dependencySet : getDepStruct().get(rootString).getDependencies()) {
            for (String dependency : dependencySet) {
                //Is the current dependency a shared subtree?
                if (commmonDependency.equals(dependency)) {

                    // Quick hack
                    if (dependency.startsWith("<Subtree")) {
                        break;
                    }

                    // Rewrite the subtree to a single element
                    Set<String> dependencySet2 = new LinkedHashSet(dependencySet);

                    dependencySet2.remove(dependency);
                    dependencySet2.add("<Subtree-" + dependency + ">");

                    // Now add this as empty element to the tree (if it does not already exist)
                    String elementKeyString5 =
                        "" + rootString + dependencySet2 + "<Subtree-" + dependency + ">";
                    if (!getDepStruct().containsKey(elementKeyString5)) {
                        getDepStruct().put(elementKeyString5, new Dependencies());

                    }

                    // Rewrite all key's of this dependency set.
                    for (String dependency2 : dependencySet) {

                        if (!dependency2.equals(dependency)) {
                            String oldKey = rootString + dependencySet + dependency2;
                            String newKey = rootString + dependencySet2 + dependency2;
                            getDepStruct().put(elementKeyString5, new Dependencies());

                            RewriteSubTree(oldKey, newKey);

                        }

                    }

                    // Finally, change the dependency set itself.
                    dependencySet.remove(dependency);
                    dependencySet.add("<Subtree-" + dependency + ">");

                    sharedDependencyFound = true;
                } else {
                    // otherwise we continue to search this subtree.
                    String root = "" + rootString + dependencySet + dependency;
                    sharedDependencyFound =
                        sharedDependencyFound
                            || FindDependencyInSubtree(root, commmonDependency, subTreeCount);
                }
            }
        }
        return sharedDependencyFound;
    }

    /****
     * HELPER METHODS
     ******/

    // HELPER method that rewrites a given subtree to one with a different key.
    public void RewriteSubTree(String oldKey, String newKey) {

        for (Set<String> dependencySet : getDepStruct().get(oldKey).getDependencies()) {
            for (String dependency : dependencySet) {
                String newOldKey = oldKey + dependencySet + dependency;
                String newNewKey = newKey + dependencySet + dependency;

                Dependencies dependencies = getDepStruct().get(newOldKey);
                getDepStruct().put(newNewKey, dependencies);

                RewriteSubTree(newOldKey, newNewKey);
            }
        }

    }

    // HELPER METHOD TO DETERMINE IF DEPENDENCY HAS DEPENDENCIES OR NOT.
    public boolean DependencyHasDependencies(String rootString) {

        for (Set<String> dependencySet2 : getDepStruct().get(rootString).getDependencies()) {
            if (dependencySet2.size() > 0) {
                return true;
            }
        }

        return false;
    }
}
