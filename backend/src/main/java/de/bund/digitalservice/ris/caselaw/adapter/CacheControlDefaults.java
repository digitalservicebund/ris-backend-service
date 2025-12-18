package de.bund.digitalservice.ris.caselaw.adapter;

import java.util.concurrent.TimeUnit;
import org.springframework.http.CacheControl;

public final class CacheControlDefaults {
  private CacheControlDefaults() {}

  /** These are typically used for static values (Wertetabellen) that rarely change like courts. */
  public static CacheControl staticValues() {
    return CacheControl.maxAge(12, TimeUnit.HOURS).staleWhileRevalidate(1, TimeUnit.DAYS);
  }
}
