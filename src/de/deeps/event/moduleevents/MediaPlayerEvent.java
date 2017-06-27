package de.deeps.event.moduleevents;

import de.deeps.event.Event;

/**
 * @author Deeps
 */

public class MediaPlayerEvent extends Event<MediaPlayerEvent.Actions> {

	public static enum Actions {
		PLAY_MP3, STOP_ALL
	};

	public MediaPlayerEvent() {
		this(Source.UNKNOWN);
	}

	public MediaPlayerEvent(Source source) {
		super(source);
	}

	public static MediaPlayerEvent createPlayMP3Request(Source source,
			String filepath) {
		MediaPlayerEvent event = new MediaPlayerEvent(source);
		event.setAction(Actions.PLAY_MP3);
		event.setType(Type.REQUEST);
		event.setValue(filepath);
		return event;
	}

	public static MediaPlayerEvent createStopAllRequest(Source source) {
		MediaPlayerEvent event = new MediaPlayerEvent(source);
		event.setAction(Actions.STOP_ALL);
		event.setType(Type.REQUEST);
		return event;
	}

}
