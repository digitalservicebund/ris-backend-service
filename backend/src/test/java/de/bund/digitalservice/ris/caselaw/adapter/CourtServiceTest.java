package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresCourtRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.domain.CourtService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import java.util.List;
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
    when(courtRepository.findAllByOrderByTypeAscLocationAsc(100)).thenReturn(returnedCourts);

    List<Court> resultCourts = service.getCourts(null, 100);

    Assertions.assertEquals(returnedCourts, resultCourts);

    verify(courtRepository).findAllByOrderByTypeAscLocationAsc(100);
  }

  @Test
  void testGetCourtsWithSearchString() {
    Court courtA = Court.builder().type("ABC").location("Berlin").build();
    Court courtB = Court.builder().type("XYZ").location("Hamburg").build();

    String searchString = " searchString ";
    String trimmedSearchString = searchString.trim();

    List<Court> returnedCourts = List.of(courtA, courtB);
    when(courtRepository.findBySearchStr(trimmedSearchString, 100)).thenReturn(returnedCourts);

    List<Court> resultCourts = service.getCourts(searchString, 100);

    Assertions.assertEquals(returnedCourts, resultCourts);

    verify(courtRepository).findBySearchStr(trimmedSearchString, 100);
  }

  @Test
  void testGetCourtsWithBlankSearchString() {
    Court courtA = Court.builder().type("ABC").location("Berlin").build();
    Court courtB = Court.builder().type("XYZ").location("Hamburg").build();

    List<Court> returnedCourts = List.of(courtA, courtB);
    when(courtRepository.findAllByOrderByTypeAscLocationAsc(100)).thenReturn(returnedCourts);

    List<Court> resultCourts = service.getCourts("", 100);

    Assertions.assertEquals(returnedCourts, resultCourts);

    verify(courtRepository).findAllByOrderByTypeAscLocationAsc(100);
  }
}
