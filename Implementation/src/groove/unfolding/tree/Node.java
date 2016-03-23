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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author s1018078
 * @version $Revision $
 */
public class Node {

    private String label;
    private NodeType nodeType;
    private Set<Node> children;

    public Node(String label) {
        this.setLabel(label);
        this.setChildren(new LinkedHashSet<Node>());
    }

    public Node(String label, NodeType nodeType) {
        this(label);
        this.setNodeType(nodeType);
    }

    public NodeType getNodeType() {
        return this.nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public Set<Node> getChildren() {
        return this.children;
    }

    public void setChildren(Set<Node> children) {
        this.children = children;
    }

    public void addChild(Node n) {
        this.children.add(n);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
