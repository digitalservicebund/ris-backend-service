package de.bund.digitalservice.ris.domain;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataR2dbcTest
class DocUnitRepositoryTest {
  @Autowired DocUnitRepository docUnitRepo;

  @Autowired DatabaseClient client;
}
