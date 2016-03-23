package groove.unfolding.prefix;

import groove.grammar.host.DefaultHostEdge;
import groove.grammar.host.HostEdge;
import groove.transform.Proof;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Prefix {
	
    // Maintain the set of explored histories for each event
    Map<Event, Set<Set<EnrichedCondition>>> exploredHistories = new LinkedHashMap<Event, Set<Set<EnrichedCondition>>>();

	public Set<EnrichedEvent> identifyPossibleExtensions(Event event){
		Set<EnrichedEvent> result = new HashSet<EnrichedEvent>();
			
		// Obtain a set of valid Histories for this Event 
    	Set<History> validHistories = identifyConcurrentHistories(event, true);
		
    	// For all valid histories, create an enriched event
    	for(History validHistory : validHistories){
    		// Add event itself to the history.
    		validHistory.addEvent(event);
    		
    		EnrichedEvent enrichedEvent = new EnrichedEvent(event, validHistory);
    		result.add(enrichedEvent);
    	}
    	
    	return result;
	}
	
	public Set<History> identifyConcurrentHistories(Event event, boolean useHistory){
		Set<History> result = new LinkedHashSet<History>();

		// Step 1: Construction of possible histories
		
		// The set of possible histories for this event. To be filled later
		Set<Set<EnrichedCondition>> historiesToExplore = new HashSet<Set<EnrichedCondition>>();
		
		// Obtain all valid histories of elements of a match.
		List<Set<EnrichedCondition>> historyCombinations = obtainAllECsfromAllElements(event);
		
		// Then remove the empty sets.
		List<Set<EnrichedCondition>> historyCombinations2 = historyCombinations.stream().filter(u -> u.size() > 0).collect(Collectors.toList());
		
		
		// If there are multiple elements with non-empty histories, construct all combinations
		if(historyCombinations2.size()>=2){
			Set<Set<EnrichedCondition>> result2 = cartesianProduct(historyCombinations2);
			
			historiesToExplore.addAll(result2);
		} 
		
		// Else if there is only 1 element in the match with a non-empty history, add all it's possible histories
		else if (historyCombinations2.size()==1){
			if(historyCombinations2.get(0).size()>0){
				// We add all conditions as a possible combination to be explored
				for(EnrichedCondition condition: historyCombinations2.get(0)){
					Set<EnrichedCondition> conditions = new HashSet<EnrichedCondition>();
					conditions.add(condition);
					historiesToExplore.add(conditions);
				}
			} else {
				historiesToExplore.add(new HashSet<EnrichedCondition>());
			}
		} else {
			historiesToExplore.add(new HashSet<EnrichedCondition>());
		}

		
		// Step 2: Remove all earlier explored histories
	
		// First obtain the previously explored histories.
		if(!exploredHistories.containsKey(event)){
			exploredHistories.put(event, new HashSet<Set<EnrichedCondition>>());
		} 
		Set<Set<EnrichedCondition>> earlierExploredHistories = exploredHistories.get(event);
		
		// Then remove all the previously explored histories
		historiesToExplore.removeAll(earlierExploredHistories);


		// Step 3: Investigate validity of all remaining histories.
		
		// Then perform the actual exploration of all (new) histories
		for ( Set<EnrichedCondition> toExplore : historiesToExplore){
			exploredHistories.get(event).add(toExplore);

			History hist = new History();
			
			if(toExplore.size()==0){
				result.add(hist); // If the elements history is empty, return an empty history
			} else {
				
				// If there is only 1 history, it is always concurrent, so simply return (a clone of) that history
				if(toExplore.size()==1){
					hist.addHistory(toExplore.iterator().next().getHistory());
					result.add(hist);
				} 
				// If there is more then 1 EnrichedCondition, we need to determine if they are concurrent.
				else {	
					if(EnrichedConditionsConcurrent(toExplore)){
						for(EnrichedCondition cond : toExplore){
							hist.addHistory(cond.getHistory());
						}
						result.add(hist);
					}
				}			
			}
		}

		return result;
	}
	

	public boolean EnrichedConditionsConcurrent(Set<EnrichedCondition> conditions){
		// By definition, all combinations of enriched conditions must be concurrent.
		
		List<EnrichedCondition> list = new ArrayList<EnrichedCondition>(conditions);
		
		for (int a = 0; a < list.size(); a++) {
		    for (int b = a + 1; b < list.size(); b++) {
		    	EnrichedCondition eca = list.get(a);
		    	EnrichedCondition ecb = list.get(b);
	            
		    	// For each combination of enriched conditions, determine if they are concurrent
		    	// And store the result for future reference.
		    	if(!TwoEnrichedConditionsConcurrent(eca, ecb)){
		    		eca.addNonConcurrentECs(ecb);
		    		ecb.addNonConcurrentECs(eca);
		    		return false;
		    	} else {
		    		eca.addConcurrentEC(ecb);
		    		ecb.addConcurrentEC(eca);
		    	}
		    }
		}
		return true;
	}
	
	public boolean TwoEnrichedConditionsConcurrent(EnrichedCondition ec1, EnrichedCondition ec2){
		// By definition. Two EnrichedConditions are concurrent IF
		// 1) Their histories are concurrent
		// 2) They are not in each others cutset.
		
		// Was the concurrency of these enriched conditions already checked?
		if(ec1.getConcurrentECs().contains(ec2) || ec1.getNonConcurrentECs().contains(ec2)){
			return true;
		}

		
		// TODO: There are two methods to determine if elements are in each others cutset.
		// Remove one of the two.

		// First determine if the two elements are not in each others cutset
		Map<HostEdge, Proof> map = null; // Elements removed by ec1
		if(ec1.getEdge().isCreated())
			map = ec1.getEdge().getProof().getConsumedElementsMap();
		
    	Map<HostEdge, Proof> map2 = null; // Elements removed by ec2
    	if(ec2.getEdge().isCreated())
    			map2 = ec2.getEdge().getProof().getConsumedElementsMap();

    	if(map!=null){
    		for (Map.Entry<HostEdge,Proof> entry : map.entrySet()) {
    			if (entry.getKey() == ec2.getEdge()){
    				return false;
    			}
    		}
    	}

		if(map2!=null){
			for (Map.Entry<HostEdge,Proof> entry : map2.entrySet()) {
				if (entry.getKey() == ec1.getEdge()){
					return false;
				}
			}
		}

		// Determine if their histories are concurrent.
		Set<Event> h1 = new HashSet<Event>(ec1.getHistory().getHistory());
		Set<Event> h2 = new HashSet<Event>(ec2.getHistory().getHistory());
		
		Set<HostEdge> removedEdgesh1 = new HashSet<HostEdge>();
		
		for(Event event : h1){
			removedEdgesh1.addAll(event.getErasedEdges());
		}
		
		Set<HostEdge> removedEdgesh2 = new HashSet<HostEdge>();
		
		for(Event event : h2){
			removedEdgesh2.addAll(event.getErasedEdges());
		}
		
		if(removedEdgesh1.contains(ec2.getEdge())){
			return false;
		}
		
		if(removedEdgesh2.contains(ec1.getEdge())){
			return false;
		}
		
		// TODO: Rewrite for's to streams
		//Set<HostEdge> deletedEdges = h1.stream().map(n -> n.getErasedEdges()).collect(Collectors.toCollection(HashSet::new));
		
		
		// Remove all shared 
		h2.removeAll(h1);
		h1.removeAll(h2);
		
		for(Event e1 : h1){
			for(Event e2 : h2){
				
				// Determine if the two events have an assymetric conflict.
				// i.e. if they both consume the same item or one
				
				// Find assymetric conflict one way.
				Set<HostEdge> intersect = new HashSet<HostEdge>(e1.getContextEdges());
			    intersect.retainAll(e2.getErasedEdges());
			    
			    if(intersect.size()>0)
			    	return false;
			    
			    // Find it the other way.
				intersect = new HashSet<HostEdge>(e2.getErasedEdges());
			    intersect.retainAll(e1.getContextEdges());
			    
			    if(intersect.size()>0)
			    	return false;
			    
			    // Or simply find a symmetric conflict
				intersect = new HashSet<HostEdge>(e2.getErasedEdges());
			    intersect.retainAll(e1.getErasedEdges());
			    if(intersect.size()>0)
			    	return false;
			}
		}
		return true;
	}
	

	// STATIC HELPER METHODS BELOW
	
	// Method to obtain all EnrichedConditions from all reading + consuming elements of an Event.
	public static List<Set<EnrichedCondition>> obtainAllECsfromAllElements(Event event){
		List<Set<EnrichedCondition>> historyCombinations = new LinkedList<Set<EnrichedCondition>>();
		
		// For each consumed edge, add generating and reading histories
		for(HostEdge edge : event.getErasedEdges()){
			DefaultHostEdge dhe = (DefaultHostEdge)edge;
			Set<EnrichedCondition> conditions = new HashSet<EnrichedCondition>();
			conditions.addAll(dhe.getGenerationHistories());
			conditions.addAll(dhe.getReadingHistories());
			historyCombinations.add(conditions);
		}
		
		// For each context edge, add all generating histories
		for(HostEdge edge : event.getContextEdges()){
			DefaultHostEdge dhe = (DefaultHostEdge)edge;
			Set<EnrichedCondition> conditions = new HashSet<EnrichedCondition>();
			conditions.addAll(dhe.getGenerationHistories());

			historyCombinations.add(conditions);
		}
		return historyCombinations;
	}
	
	// Method to construct the cartesian product of a list containing sets of enriched conditions
	// This method is used to produce all possible histories of an event.
	public static Set<Set<EnrichedCondition>> cartesianProduct(List<Set<EnrichedCondition>> sets) {
	    if (sets.size() < 2)
	        throw new IllegalArgumentException(
	                "Can't have a product of fewer than two sets (got " +
	                sets.size() + ")");

	    return _cartesianProduct(0, sets);
	}

	private static Set<Set<EnrichedCondition>> _cartesianProduct(int index, List<Set<EnrichedCondition>> sets) {
	    Set<Set<EnrichedCondition>> ret = new HashSet<Set<EnrichedCondition>>();
	    if (index == sets.size()) {
	        ret.add(new HashSet<EnrichedCondition>());
	    } else {
	    	
	    	if(sets.get(index).size()>0){
		        for (EnrichedCondition obj : sets.get(index)) {
		            for (Set<EnrichedCondition> set : _cartesianProduct(index+1, sets)) {
		                set.add(obj);
		                ret.add(set);
		            }
		        }
	    	} else {
	    		ret.add(new HashSet<EnrichedCondition>());
	    	}
	    }
	    return ret;
	}

}
