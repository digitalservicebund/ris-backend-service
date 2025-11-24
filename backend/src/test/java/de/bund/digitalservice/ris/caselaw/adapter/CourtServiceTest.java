package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresCourtRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.domain.CourtService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtBranchLocationRepository;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({
  CourtService.class,
  PostgresCourtRepositoryImpl.class,
})
class CourtServiceTest {

  @MockitoSpyBean private CourtService service;

  @MockitoBean private CourtRepository courtRepository;
  @MockitoBean private CourtBranchLocationRepository courtBranchLocationRepository;

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

  @Test
  void getBranchLocations_existingCourtWithBranchLocations_returnsBranchLocations() {
    String courtType = "FG";
    String courtLocation = "München";
    UUID courtId = UUID.randomUUID();
    Court court = Court.builder().type(courtType).location(courtLocation).id(courtId).build();
    when(courtRepository.findByTypeAndLocation(courtType, courtLocation))
        .thenReturn(Optional.of(court));
    when(courtBranchLocationRepository.findAllByCourtId(courtId)).thenReturn(List.of("Augsburg"));

    List<String> branchLocations = service.getBranchLocationsForCourt(courtType, courtLocation);

    verify(courtRepository).findByTypeAndLocation(courtType, courtLocation);
    verify(courtBranchLocationRepository).findAllByCourtId(courtId);
    assertThat(branchLocations).isEqualTo(List.of("Augsburg"));
  }

  @Test
  void getBranchLocations_existingCourtWithoutBranchLocations_returnsEmptyList() {
    String courtType = "FG";
    String courtLocation = "München";
    UUID courtId = UUID.randomUUID();
    Court court = Court.builder().type(courtType).location(courtLocation).id(courtId).build();
    when(courtRepository.findByTypeAndLocation(courtType, courtLocation))
        .thenReturn(Optional.of(court));
    when(courtBranchLocationRepository.findAllByCourtId(courtId)).thenReturn(List.of());

    List<String> branchLocations = service.getBranchLocationsForCourt(courtType, courtLocation);

    verify(courtRepository).findByTypeAndLocation(courtType, courtLocation);
    verify(courtBranchLocationRepository).findAllByCourtId(courtId);
    assertThat(branchLocations).isEmpty();
  }

  @Test
  void getBranchLocations_missingCourt_returnsEmptyList() {
    String courtType = "FG";
    String courtLocation = "München";
    UUID courtId = UUID.randomUUID();
    when(courtRepository.findByTypeAndLocation(courtType, courtLocation))
        .thenReturn(Optional.empty());

    List<String> branchLocations = service.getBranchLocationsForCourt(courtType, courtLocation);

    verify(courtRepository).findByTypeAndLocation(courtType, courtLocation);
    verify(courtBranchLocationRepository, never()).findAllByCourtId(courtId);
    assertThat(branchLocations).isEmpty();
  }
}
