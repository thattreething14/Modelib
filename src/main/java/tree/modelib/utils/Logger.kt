package tree.modelib.utils

import org.bukkit.Bukkit

class Logger private constructor() {
    init {
        throw IllegalStateException("Utility class")
    }

    companion object {
        /**
         * The logger used to log messages to the console.
         */
        private val LOGGER = Bukkit.getLogger()

        /**
         * Logs an info message to the console.
         *
         * @param message The message to log.
         */
        fun info(message: String) {
            LOGGER.info("[Modelib] $message")
        }

        /**
         * Logs a warning message to the console.
         *
         * @param message The message to log.
         */
        fun warn(message: String) {
            LOGGER.warning("[Modelib] $message")
        }
    }
}