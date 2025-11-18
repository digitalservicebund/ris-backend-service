package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreementIndustry;
import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreementIndustryService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/collective-agreement-industries")
@Slf4j
public class CollectiveAgreementIndustryController {
  private final CollectiveAgreementIndustryService service;

  public CollectiveAgreementIndustryController(CollectiveAgreementIndustryService service) {
    this.service = service;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<CollectiveAgreementIndustry> getCollectiveAgreementIndustries(
      @RequestParam(value = "q", required = false) String searchStr) {
    return service.getCollectiveAgreementIndustries(searchStr);
  }
}
