import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.DEBUG

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender


appender("STDOUT", ConsoleAppender) {
	encoder(PatternLayoutEncoder) {
	  pattern = "%-5level Behave2UnitGen [%logger{0}] - %msg%n"
	}
  }
root(WARN, ["STDOUT"])