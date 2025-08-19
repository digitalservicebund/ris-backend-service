package de.bund.digitalservice.ris.caselaw;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.slf4j.event.KeyValuePair;

public class TestMemoryAppender extends ListAppender<ILoggingEvent> {
  private final Logger logger;

  public TestMemoryAppender(Class<?> clazz) {
    super();
    setContext((LoggerContext) LoggerFactory.getILoggerFactory());

    logger = (Logger) LoggerFactory.getLogger(clazz);
    logger.addAppender(this);
    this.start();
  }

  public void detachLoggingTestAppender() {
    this.stop();
    logger.detachAppender(this);
  }

  public long count(Level level) {
    return list.stream().filter(event -> event.getLevel() == level).count();
  }

  public String getMessage(Level level, int index) {
    var messageList =
        list.stream()
            .filter(event -> event.getLevel() == level)
            .map(ILoggingEvent::getFormattedMessage)
            .toList();

    if (messageList.size() > index) {
      return messageList.get(index);
    }

    return "no logging message";
  }

  public List<KeyValuePair> getKeyValuePairs(Level level, int index) {
    var keyValueList =
        list.stream()
            .filter(event -> event.getLevel() == level)
            .map(ILoggingEvent::getKeyValuePairs)
            .toList();

    if (keyValueList.size() > index) {
      return keyValueList.get(index);
    }

    return null;
  }

  public IThrowableProxy getCause(Level level, int index) {
    var messageList =
        list.stream()
            .filter(event -> event.getLevel() == level)
            .map(ILoggingEvent::getThrowableProxy)
            .toList();

    if (messageList.size() > index) {
      return messageList.get(index);
    }

    return null;
  }
}
