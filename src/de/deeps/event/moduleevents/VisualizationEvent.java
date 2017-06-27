package de.deeps.event.moduleevents;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class VisualizationEvent extends Event<VisualizationEvent.Actions> {

	public static enum Actions {
		START_VISUALIZATION_REQUEST, STOP_VISUALIZATION_REQUEST
	};

	public VisualizationEvent() {
		this(Source.UNKNOWN);
	}

	public VisualizationEvent(Source source) {
		super(source);
	}

	public static VisualizationEvent createStartVisualizationRequest(
			Source source) {
		VisualizationEvent event = new VisualizationEvent(source);
		event.setAction(Actions.START_VISUALIZATION_REQUEST);
		event.setType(Type.REQUEST);
		return event;
	}

	public static VisualizationEvent createStopVisualizationRequest(
			Source source) {
		VisualizationEvent event = new VisualizationEvent(source);
		event.setAction(Actions.STOP_VISUALIZATION_REQUEST);
		event.setType(Type.REQUEST);
		return event;
	}

}
