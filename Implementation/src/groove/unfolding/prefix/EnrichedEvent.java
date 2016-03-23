package groove.unfolding.prefix;

/**
 * EnrichedEvent is an event annotated with a unique history
 */
public class EnrichedEvent {
	
	public EnrichedEvent(Event event, History history) {
		super();
		this.event = event;
		this.history = history;
	}
	
	private Event event;
	private History history;

	public Event getEvent() {
		return event;
	}

	public History getHistory() {
		return history;
	}

}
