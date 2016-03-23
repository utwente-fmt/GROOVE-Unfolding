package groove.unfolding;

import static groove.io.FileType.GRAMMAR;
import groove.explore.ExploreResult;
import groove.grammar.Grammar;
import groove.grammar.Rule;
import groove.grammar.model.GrammarModel;
import groove.lts.GTS;
import groove.lts.GraphState;
import groove.lts.GraphTransition;
import groove.transform.Transformer;
import groove.util.parse.FormatException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Class that constructs a reachability graph and identifies all routes to goal states in this
 * Used comparison in evaluation of scalability unfolding approach
 */
public class ReachabilityGraphConstruction {

	public static void main(String[] args) throws IOException, FormatException {
	
        /************************************************************
         *** Step 1: Load the GTS
         ************************************************************/
		
        File dir = new File(GRAMMAR.addExtension(args[0]));
        GrammarModel grammarModel = GrammarModel.newInstance(dir);
        
        // Initialize model to Grammar
        Grammar grammar = grammarModel.toGrammar();
        
        Rule goalCondition = grammar.getRule("goal");
        
        /************************************************************
         *** Step 2: Construct the Reachability graph
         ************************************************************/
        
        long startTime = System.nanoTime();
        
        Transformer t = new Transformer(grammarModel);
        ExploreResult result = t.explore();

		System.out.println("That took " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
		

        /************************************************************
         *** Step 3: Identify all goal states + adjecent map
         ************************************************************/
		
        GTS gts = result.getGTS();
        Set<? extends GraphTransition> edgeSet = gts.edgeSet();
        
        
        Map<GraphState, Set<GraphState>> adjecent =  new LinkedHashMap<GraphState,Set<GraphState>>();
        Set<GraphState> goalStates = new HashSet<GraphState>();
        
        for(GraphTransition ts : edgeSet){
        	if(ts.getAction() == goalCondition){
        		goalStates.add(ts.source());
        	}
        	
        	if(ts.source()!=ts.target()){
        		if(!adjecent.containsKey(ts.source())){
        			adjecent.put(ts.source(), new HashSet<GraphState>());
        		}
    			adjecent.get(ts.source()).add(ts.target());
        	}
        }
        
        System.out.println("States: "+gts.nodeSet().size());
        System.out.println("Goal states: "+goalStates.size());
        
        /************************************************************
         *** Step 4: Perform a BFS search using the adjecent map to find all routes to goal states
         ************************************************************/
        
        LinkedList<GraphState> visited =  new LinkedList<GraphState>();
        visited.add(gts.nodeSet().iterator().next());
        
        Counter counter = new Counter();
        counter.count = 0;
        breadthFirst(adjecent, visited, goalStates, counter);
        
        System.out.println("Search completed");
        System.out.println("Found attacks: "+counter.count);
        System.out.println("That took " + (System.nanoTime() - startTime)/1000000 + " milliseconds");
	}

	public static void breadthFirst(Map<GraphState, Set<GraphState>> adjecent, LinkedList<GraphState> visited, Set<GraphState> goalStates, Counter counter) {
		
	    Set<GraphState> nodes = adjecent.get(visited.getLast());
	    // examine adjacent nodes
	    for (GraphState node : nodes) {
	        if (visited.contains(node)) {
	            continue;
	        } else if (goalStates.contains(node)) {
	            visited.add(node);
	            counter.count++;
	            visited.removeLast();
	            continue;

	        } else {
	        	visited.addLast(node);
		        breadthFirst(adjecent, visited, goalStates, counter);
		        visited.removeLast();
	        }
	    }

	}


	// Used to enforce pass-by-reference integer;
	private static class Counter{
		public int count;
	}
}
