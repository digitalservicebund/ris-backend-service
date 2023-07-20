package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.domain.export.juris.response.MessageWrapper;
import de.bund.digitalservice.ris.domain.export.juris.response.StatusImporterException;
import jakarta.mail.Message;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JurisMessageWrapperFactory {
  private final List<Class<? extends MessageWrapper>> messageWrappers;

  public JurisMessageWrapperFactory(List<Class<? extends MessageWrapper>> messageWrappers) {
    this.messageWrappers = messageWrappers;
  }

  public Optional<MessageWrapper> getResponsibleWrapper(Message message) {
    for (Class<? extends MessageWrapper> wrapper : messageWrappers) {
      try {
        if ((boolean) wrapper.getMethod("canHandle", Message.class).invoke(null, message)) {
          return Optional.of(wrapper.getConstructor(Message.class).newInstance(message));
        }
      } catch (InstantiationException
          | NoSuchMethodException
          | InvocationTargetException
          | IllegalAccessException e) {
        throw new StatusImporterException("Could not get responsible wrapper: " + e);
      }
    }
    return Optional.empty();
  }
}
