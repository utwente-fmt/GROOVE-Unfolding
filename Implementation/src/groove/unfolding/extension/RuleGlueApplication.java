package groove.unfolding.extension;

import groove.grammar.host.DefaultHostEdge;
import groove.grammar.host.HostEdge;
import groove.grammar.host.HostGraph;
import groove.transform.DeltaTarget;
import groove.transform.Proof;
import groove.transform.RuleApplication;
import groove.transform.RuleEffect;
import groove.transform.RuleEvent;
import groove.unfolding.prefix.EnrichedEvent;
import groove.unfolding.prefix.Event;

/**
 * Class extending the regular RuleApplication to a Glue Application.
 * On GlueApplication, only the new elements of a rule are added, nothing
 * gets deleted from the HostGraph.
 * @author David Huistra
 *
 */
public class RuleGlueApplication extends RuleApplication {
    
    public RuleGlueApplication(Event event, HostGraph source) {
        super(event.getProof().newEvent(null), source);
        this.event = event;
        this.proof = event.getProof();
    }

    private Event event;
    private Proof proof;

    /**
     * Overrides the default applyDelta to prevent the removal of elements.
     */
    @Override
    public void applyDelta(DeltaTarget target) {
        if (getRule().isModifying()) {
            RuleEffect record = getEffect();

            if (record.getAddedEdges() != null) {
                // Extension. We add the proof that created each (edge) element to the elements (for easier reference later on)
                for (HostEdge edge : record.getAddedEdges()) {
                    DefaultHostEdge dhe = (DefaultHostEdge) edge;
                    dhe.setCreated(true);
                    dhe.setProof(this.proof);
                }
            }

            RecordEffectInProof(record);
            RecordEffectInEvent(record);

            //removeEdges(record, target);
            //removeNodes(record, target);
            addNodes(record, target);
            addEdges(record, target);
            //removeIsolatedValueNodes(target);

        }
    }

    /**
     * Override method to simply give the existing hostgraph back (instead of clone)
     * We want to maintain the same GraphInstance, so we can compare previous matches with current ones.
     */
    @Override
    protected HostGraph createTarget() {
        return getSource();
    }
    
    protected void RecordEffectInEvent(RuleEffect effect){
    	event.setCreatedEdges(effect.getCreatedEdges());
//    	event.setErasedEdges(effect.getRemovedEdges());
//    	event.setContextEdges(effect.getContextEdges());
    	
    	event.setEventAddedToHostGraph(true);
    }

    // Add all consumed elements in the history of a proof to a proof
    protected void RecordEffectInProof(RuleEffect effect) {

        if (effect.getRemovedEdges() != null) {
            for (HostEdge removedEdge : effect.getRemovedEdges()) {
                this.proof.getConsumedElementsMap().put(removedEdge, this.proof);
            }
        }

        // For each edge of the context that is created by another proof
        // We add the consumed elements of that proofs history
        for (HostEdge edge : this.proof.getEdgeValues()) {
            DefaultHostEdge dhe = (DefaultHostEdge) edge;
            if (dhe.isCreated()) {
                this.proof.getConsumedElementsMap().putAll(dhe.getProof().getConsumedElementsMap());
            }
        }
    }
}
