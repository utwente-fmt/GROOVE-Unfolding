package groove.unfolding;

import groove.grammar.Rule;
import groove.grammar.host.HostGraph;
import groove.transform.Proof;
import groove.unfolding.prefix.Event;
import groove.unfolding.prefix.Prefix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class Unfolding {
	
	private HostGraph hostGraph;
    private LinkedHashMap<Proof, Event> createdEvents;
    private Prefix prefix;

    public Unfolding(HostGraph hostGraph,
			LinkedHashMap<Proof, Event> createdEvents, Prefix prefix) {
		super();
		this.hostGraph = hostGraph;
		this.createdEvents = createdEvents;
		this.prefix = prefix;
	}

    // Find all valid matches of/all routes to an instance of the specified rule.
    // This method is used to identify all matches of a goal condition in the unfolding.
    public ArrayList<Proof> extractRecordedRules(Rule rule){
    	
    	// Find all matches of the rule in the hostgraph
        ArrayList<Proof> matches = new ArrayList<Proof>();
        matches.addAll(rule.getAllMatches(hostGraph, null));
        
        Set<Proof> invalidGoalMatches = new HashSet<Proof>();
        
		for(Proof proof : matches){
			// Determine if we ever found a valid history for this proof
			if(!createdEvents.get(proof).isEventAddedToHostGraph()){
        		invalidGoalMatches.add(proof);
        	}

		}
		matches.removeAll(invalidGoalMatches);
		
		return matches;	
    }
}
