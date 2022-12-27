package de.bund.digitalservice.ris.caselaw;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

public class TestMemoryAppender extends ListAppender<ILoggingEvent> {
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
}
