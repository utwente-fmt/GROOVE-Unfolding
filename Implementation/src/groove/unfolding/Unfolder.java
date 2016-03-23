package groove.unfolding;

import groove.grammar.Grammar;
import groove.grammar.Rule;
import groove.grammar.host.DefaultHostEdge;
import groove.grammar.host.HostEdge;
import groove.grammar.host.HostGraph;
import groove.grammar.model.GrammarModel;
import groove.transform.Proof;
import groove.unfolding.extension.RuleGlueApplication;
import groove.unfolding.extension.UnfoldingGrammarModel;
import groove.unfolding.prefix.EnrichedCondition;
import groove.unfolding.prefix.EnrichedEvent;
import groove.unfolding.prefix.Event;
import groove.unfolding.prefix.History;
import groove.unfolding.prefix.Prefix;
import groove.util.parse.FormatException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Unfolder {
    private final UnfoldingGrammarModel grammarModel;
    private Grammar grammar;
    
    public static final int ITERATION_TRESHOLD = 8;
    /**
     * Constructs an unfolder based on the grammar found at a given location.
     * @throws IOException if the grammar cannot be loaded from the given location
     */
    public Unfolder(File grammarLocation) throws IOException {
        this(UnfoldingGrammarModel.newInstance(grammarLocation));
    }

    /**
     * Constructs an unfolder based on a given grammar model.
     */
    public Unfolder(UnfoldingGrammarModel grammarModel) {
        this.grammarModel = grammarModel;
    }

    /** Returns the grammar model wrapped in this unfolder. */
    public UnfoldingGrammarModel getGrammarModel() {
        return this.grammarModel;
    }

    
    public Unfolding constructUnfolding() throws FormatException{
        // Initialize model to Grammar
        grammar = grammarModel.toGrammar();

        // Retrieve a set of rules
        Set<Rule> rules = grammar.getAllRules();

        // Retrieve the Start Graph, set as the host graph
        HostGraph hostGraph = grammar.getStartGraph();
        
        // Add empty generation history condition to all elements (edges) in the startgraph/initial hostgraph
        for(HostEdge edge: hostGraph.edgeSet()){
        	EnrichedCondition generationCondition = new EnrichedCondition((DefaultHostEdge) edge, new History());
        	((DefaultHostEdge)edge).addGenerationHistory(generationCondition);
        }

        
        LinkedHashMap<Proof, Event> createdEvents = new LinkedHashMap<Proof, Event>();
        
        Prefix prefix = new Prefix();
        
        int newMatches = -1;
        int iteration = 0;
        
        while (newMatches != 0 && iteration < ITERATION_TRESHOLD) {
        	
        	// STEP 1: IDENTIFYING all possible extensions
        	
        	// First find all possible rule applications (events) in the host graph.
        	List<Event> events = new ArrayList<Event>();
        	
			for (Rule rule : grammar.getAllRules()) {
				for(Proof proof : rule.getAllMatches(hostGraph, null)){
					if(!createdEvents.containsKey(proof)){
	            		createdEvents.put(proof, new Event(proof));
	            	}
	            	events.add(createdEvents.get(proof));
				}
			}

            
            // For each possible match/event, we want to obtain all the valid histories/Enriched events
            List<EnrichedEvent> enrichedEvents = new ArrayList<EnrichedEvent>();
            
            for(Event event : events){ 	
            	enrichedEvents.addAll(prefix.identifyPossibleExtensions(event));
            }
            
            // STEP 2: ADDING PossibleExtension to the PREFIX

            // For each possible enriched event identified in this iteration, add it to prefix
            for(EnrichedEvent enrichedEvent : enrichedEvents){
            	
            	Event event = enrichedEvent.getEvent();
            	History hist = enrichedEvent.getHistory();
            	

                if(!event.isEventAddedToHostGraph()){
                  // Add event to HostGraph
                  hostGraph = new RuleGlueApplication(event, hostGraph).getTarget(); 
                }
                
                // Add Histories to reading and created elements.
                for(HostEdge edge : event.getCreatedEdges()){
                	EnrichedCondition generationCondition = new EnrichedCondition((DefaultHostEdge) edge, hist);
                	((DefaultHostEdge)edge).addGenerationHistory(generationCondition);
                }
                
                for(HostEdge edge : event.getContextEdges()){
                	EnrichedCondition readingCondition = new EnrichedCondition((DefaultHostEdge) edge, hist);
                	((DefaultHostEdge)edge).addReadingHistories(readingCondition);
                } 
                
                // Optional TODO. Improves efficiency.
                // TODO: Add list of concurrernt conditions (Y) en list of nonConcurrent conditions (X) to each created EnrichedCondition.
            }
          
          newMatches = enrichedEvents.size();
          iteration++;
        }
        
        Unfolding unfolding = new Unfolding(hostGraph, createdEvents, prefix);
    	return unfolding;
    }
    
    public Rule getAttackerGoalCondition(){
    	 return grammar.getRule("goal");
    }

}
