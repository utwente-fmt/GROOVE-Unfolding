package groove.unfolding.prefix;

import java.util.LinkedHashSet;
import java.util.Set;

import groove.grammar.host.HostEdge;
import groove.grammar.host.HostEdgeSet;
import groove.grammar.rule.RuleEdge;
import groove.grammar.rule.RuleToHostMap;
import groove.transform.Proof;

// Event describes a rule application with a unique match, i.e. a Proof in GROOVE
// An event is based on a Proof, but extended with additional info such as the element it reads, creates and consumes.

public class Event {
	
	private boolean eventAddedToHostGraph;
	
	private Proof proof;
	
	private Set<HostEdge> erasedEdges = new LinkedHashSet<HostEdge>();
	private Set<HostEdge> createdEdges = new LinkedHashSet<HostEdge>();
	private Set<HostEdge> contextEdges = new LinkedHashSet<HostEdge>();

	public Event(final Proof proof) {
		this.proof = proof;
		
		erasedEdges.addAll(computeErasedEdges());
		contextEdges.addAll(proof.getPatternMap().edgeMap().values());
		contextEdges.removeAll(erasedEdges);
	}
	
	public String toString() {
        return "Event of: "+proof.getRule().getFullName()+", "+proof.getNodeValues();
    }

	public Proof getProof() {
		return proof;
	}

	public boolean isEventAddedToHostGraph() {
		return eventAddedToHostGraph;
	}

	public void setEventAddedToHostGraph(boolean eventAddedToHostGraph) {
		this.eventAddedToHostGraph = eventAddedToHostGraph;
	}

	//--------------------
	public Set<HostEdge> getErasedEdges() {
		return erasedEdges;
	}

	public Set<HostEdge> getCreatedEdges() {
		return createdEdges;
	}

	public void setCreatedEdges(Set<HostEdge> createdEdges) {
		if(createdEdges!=null)
		this.createdEdges.addAll(createdEdges);
	}

	public Set<HostEdge> getContextEdges() {
		return contextEdges;
	}

	
	
	//-----------------------
    /**
     * Computes the set of explicitly erased edges, i.e., the images of the LHS
     * eraser edges. Callback method from {@link #getErasedEdges()}.
     */
    private HostEdgeSet computeErasedEdges() {
        HostEdgeSet result = new HostEdgeSet();
        RuleToHostMap anchorMap = proof.getPatternMap();
        RuleEdge[] eraserEdges = proof.getRule().getEraserEdges();
        for (RuleEdge edge : eraserEdges) {
            HostEdge edgeImage = anchorMap.getEdge(edge);
            result.add(edgeImage);
        }
        return result;
    }

}
