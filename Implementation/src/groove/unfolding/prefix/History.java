package groove.unfolding.prefix;

import java.util.LinkedHashSet;
import java.util.Set;

public class History {
	
	private Set<Event> history = new LinkedHashSet<Event>();

	public Set<Event> getHistory() {
		return history;
	}

	public void addHistory(History hist) {
		this.history.addAll(hist.getHistory());
	}
	
	public void addEvents(Set<Event> history) {
		this.history.addAll(history);
	}
	
	public void addEvent(Event history) {
		this.history.add(history);
	}
	

	public String toString() {
        return history.toString();
    }
}
