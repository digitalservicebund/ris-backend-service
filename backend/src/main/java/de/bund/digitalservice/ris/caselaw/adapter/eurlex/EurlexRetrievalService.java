package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

public interface EurlexRetrievalService {

  String requestEurlexResultList(String url, String payload);

  String requestSingleEurlexDocument(String sourceUrl);
}
