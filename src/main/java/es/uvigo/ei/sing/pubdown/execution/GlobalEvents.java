package es.uvigo.ei.sing.pubdown.execution;

import static java.util.Collections.unmodifiableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class GlobalEvents {
	public static final String ACTION_MARKER = "#";

	public static final String ACTION_STARTED = "Started";
	public static final String ACTION_FINISHED = "Finished";
	public static final String ACTION_ABORTED = "Aborted";
	public static final String ACTION_SCHEDULED = "Scheduled";
	public static final String ACTION_SUBTASK_STARTED = "SubtaskStarted";
	public static final String ACTION_SUBTASK_FINISHED = "SubtaskFinished";
	public static final String ACTION_SUBTASK_ABORTED = "SubtaskAborted";
	public static final String ACTION_SUBTASK_ERROR = "SubtaskError";

	public static final String SUFFIX_STARTED = ACTION_MARKER + ACTION_STARTED;
	public static final String SUFFIX_FINISHED = ACTION_MARKER + ACTION_FINISHED;
	public static final String SUFFIX_ABORTED = ACTION_MARKER + ACTION_ABORTED;
	public static final String SUFFIX_SCHEDULED = ACTION_MARKER + ACTION_SCHEDULED;
	public static final String SUFFIX_SUBTASK_STARTED = ACTION_MARKER + ACTION_SUBTASK_STARTED;
	public static final String SUFFIX_SUBTASK_FINISHED = ACTION_MARKER + ACTION_SUBTASK_FINISHED;
	public static final String SUFFIX_SUBTASK_ABORTED = ACTION_MARKER + ACTION_SUBTASK_ABORTED;
	public static final String SUFFIX_SUBTASK_ERROR = ACTION_MARKER + ACTION_SUBTASK_ERROR;

	public static final String EVENT_REPOSITORY_QUERY = "eventRepositoryQuery";
	public static final String EVENT_REPOSITORY_QUERY_STARTED = EVENT_REPOSITORY_QUERY + SUFFIX_STARTED;
	public static final String EVENT_REPOSITORY_QUERY_FINISHED = EVENT_REPOSITORY_QUERY + SUFFIX_FINISHED;
	public static final String EVENT_REPOSITORY_QUERY_ABORTED = EVENT_REPOSITORY_QUERY + SUFFIX_ABORTED;
	public static final String EVENT_REPOSITORY_QUERY_SCHEDULED = EVENT_REPOSITORY_QUERY + SUFFIX_SCHEDULED;
	public static final String EVENT_REPOSITORY_QUERY_SUBTASK_STARTED = EVENT_REPOSITORY_QUERY + SUFFIX_SUBTASK_STARTED;
	public static final String EVENT_REPOSITORY_QUERY_SUBTASK_FINISHED = EVENT_REPOSITORY_QUERY
			+ SUFFIX_SUBTASK_FINISHED;
	public static final String EVENT_REPOSITORY_QUERY_SUBTASK_ABORTED = EVENT_REPOSITORY_QUERY + SUFFIX_SUBTASK_ABORTED;
	public static final String EVENT_REPOSITORY_QUERY_SUBTASK_ERROR = EVENT_REPOSITORY_QUERY + SUFFIX_SUBTASK_ERROR;

	public static final String SUFFIX_QUERIES = ACTION_MARKER + "QUERIES";
	public static final String SUFFIX_REPOSITORIES = ACTION_MARKER + "REPOSITORIES";
	public static final String SUFFIX_USERS = ACTION_MARKER + "USERS";
	public static final String EVENT_REFRESH_DATA = "eventRefreshData";
	public static final String EVENT_REFRESH_DATA_QUERIES = EVENT_REFRESH_DATA + SUFFIX_QUERIES;
	public static final String EVENT_REFRESH_DATA_REPOSITORIES = EVENT_REFRESH_DATA + SUFFIX_REPOSITORIES;

	private static final String[] SUFFIXES = new String[] { SUFFIX_STARTED, SUFFIX_FINISHED, SUFFIX_ABORTED,
			SUFFIX_SCHEDULED, SUFFIX_SUBTASK_ABORTED, SUFFIX_SUBTASK_FINISHED, SUFFIX_SUBTASK_STARTED, SUFFIX_QUERIES,
			SUFFIX_REPOSITORIES, SUFFIX_USERS };
	public static final String[] EVENTS = new String[] { EVENT_REPOSITORY_QUERY, EVENT_REFRESH_DATA };

	private final static Map<String, List<String>> EVENT_GLOBAL_COMMANDS = Collections
			.synchronizedMap(new HashMap<String, List<String>>());

	private GlobalEvents() {
	}

	public final static void fullRegisterGlobalCommand(final String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			for (final String eventId : GlobalEvents.EVENTS) {
				GlobalEvents.fullStatesRegisterGlobalCommand(eventId, globalCommand);
			}
		}
	}

	public final static void fullUnregisterGlobalCommand(final String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			for (final String eventId : GlobalEvents.SUFFIXES) {
				GlobalEvents.fullStatesUnregisterGlobalCommand(eventId, globalCommand);
			}
		}
	}

	public final static void fullStatesRegisterGlobalCommand(final String eventId, final String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			for (final String suffix : GlobalEvents.SUFFIXES) {
				GlobalEvents.registerGlobalCommand(eventId + suffix, globalCommand);
			}
		}
	}

	public final static void fullStatesUnregisterGlobalCommand(final String eventId, final String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			for (final String suffix : GlobalEvents.SUFFIXES) {
				GlobalEvents.unregisterGlobalCommand(eventId + suffix, globalCommand);
			}
		}
	}

	public final static void fullActionRegisterGlobalCommand(final String action, final String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			for (final String eventId : GlobalEvents.EVENTS) {
				GlobalEvents.registerGlobalCommand(eventId + GlobalEvents.ACTION_MARKER + action, globalCommand);
			}
		}
	}

	public final static void fullActionUnregisterGlobalCommand(final String action, final String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			for (final String eventId : GlobalEvents.EVENTS) {
				GlobalEvents.unregisterGlobalCommand(eventId + GlobalEvents.ACTION_MARKER + action, globalCommand);
			}
		}
	}

	public final static void registerGlobalCommand(final String eventId, final String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			if (!EVENT_GLOBAL_COMMANDS.containsKey(eventId)) {
				EVENT_GLOBAL_COMMANDS.put(eventId, new LinkedList<String>());
			}

			if (!EVENT_GLOBAL_COMMANDS.get(eventId).contains(globalCommand)) {
				EVENT_GLOBAL_COMMANDS.get(eventId).add(globalCommand);
			}
		}
	}

	public final static void unregisterGlobalCommand(final String eventId, final String globalCommand) {
		synchronized (EVENT_GLOBAL_COMMANDS) {
			if (EVENT_GLOBAL_COMMANDS.containsKey(eventId)) {
				EVENT_GLOBAL_COMMANDS.get(eventId).remove(globalCommand);

				if (EVENT_GLOBAL_COMMANDS.get(eventId).isEmpty()) {
					EVENT_GLOBAL_COMMANDS.remove(eventId);
				}
			}
		}
	}

	public final static List<String> getEventGlobalCommands(final String eventId) {
		final List<String> commands = EVENT_GLOBAL_COMMANDS.get(eventId);

		return (commands == null) ? new ArrayList<>() : new ArrayList<>(commands);
	}

	public final static Map<String, List<String>> getEventGlobalCommands() {
		return unmodifiableMap(EVENT_GLOBAL_COMMANDS);
	}
}
