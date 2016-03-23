package groove.unfolding.prefix;

import java.util.LinkedHashSet;
import java.util.Set;

import groove.grammar.host.DefaultHostEdge;

/**
 * An EnrichedCondition annotates a graph element with a specific history
 *
 */
public class EnrichedCondition {
	
	public EnrichedCondition(DefaultHostEdge edge, History history) {
		super();
		this.history = history;
		this.edge = edge;
	}

	private DefaultHostEdge edge;
	private History history;

	public String toString() {
        return "<"+edge+","+history+">";
    }
	
	public DefaultHostEdge getEdge() {
		return edge;
	}
	
	public History getHistory() {
		return history;
	}

	private Set<EnrichedCondition> concurrentECs = new LinkedHashSet<EnrichedCondition>();
	private Set<EnrichedCondition> nonConcurrentECs = new LinkedHashSet<EnrichedCondition>();
	
	public Set<EnrichedCondition> getConcurrentECs() {
		return concurrentECs;
	}
	public void addConcurrentEC(EnrichedCondition concurrentEC) {
		this.concurrentECs.add(concurrentEC);
	}

	public Set<EnrichedCondition> getNonConcurrentECs() {
		return nonConcurrentECs;
	}

	public void addNonConcurrentECs(EnrichedCondition nonConcurrentEC) {
		this.nonConcurrentECs.add(nonConcurrentEC);
	}



}
