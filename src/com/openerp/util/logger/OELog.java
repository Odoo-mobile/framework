package com.openerp.util.logger;

public class OELog {
	/** The logger. */
	private final java.util.logging.Logger logger = java.util.logging.Logger
			.getLogger(this.getClass().toString());

	public static void log(String message) {
		OELog log = new OELog();
		log._log(message);
	}

	public void _log(String message) {
		logger.severe(message);
	}
}
