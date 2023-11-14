package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresCourtRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.domain.CourtService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({
  CourtService.class,
  PostgresCourtRepositoryImpl.class,
})
class CourtServiceTest {

  @SpyBean private CourtService service;

  @MockBean private CourtRepository courtRepository;

  @Test
  void testGetTwoDifferentCourts() {
    Court courtA = Court.builder().type("ABC").location("Berlin").build();
    Court courtB = Court.builder().type("XYZ").location("Hamburg").build();

    List<Court> returnedCourts = List.of(courtA, courtB);
    when(courtRepository.findAllByOrderByTypeAscLocationAsc()).thenReturn(returnedCourts);

    List<Court> resultCourts = service.getCourts(Optional.empty());

    Assertions.assertEquals(returnedCourts, resultCourts);

    verify(courtRepository).findAllByOrderByTypeAscLocationAsc();
  }

  @Test
  void testGetCourtsWithSearchString() {
    Court courtA = Court.builder().type("ABC").location("Berlin").build();
    Court courtB = Court.builder().type("XYZ").location("Hamburg").build();

    String searchString = " searchString ";
    String trimmedSearchString = searchString.trim();

    List<Court> returnedCourts = List.of(courtA, courtB);
    when(courtRepository.findBySearchStr(trimmedSearchString)).thenReturn(returnedCourts);

    List<Court> resultCourts = service.getCourts(Optional.of(searchString));

    Assertions.assertEquals(returnedCourts, resultCourts);

    verify(courtRepository).findBySearchStr(trimmedSearchString);
  }

  @Test
  void testGetCourtsWithBlankSearchString() {
    Court courtA = Court.builder().type("ABC").location("Berlin").build();
    Court courtB = Court.builder().type("XYZ").location("Hamburg").build();

    List<Court> returnedCourts = List.of(courtA, courtB);
    when(courtRepository.findAllByOrderByTypeAscLocationAsc()).thenReturn(returnedCourts);

    List<Court> resultCourts = service.getCourts(Optional.of(""));

    Assertions.assertEquals(returnedCourts, resultCourts);

    verify(courtRepository).findAllByOrderByTypeAscLocationAsc();
  }
}
