import { NormCategory, UndefinedDate } from "../../../../src/domain/Norm"
import { NormData } from "../fixtures"

export const newNorm: NormData = {
  jurisZipFileName: "",
  articles: [
    {
      marker: "§ 1",
      title: "Allgemeiner Anwendungsbereich",
      paragraphs: [
        {
          marker: "(1)",
          text: "Dieses Gesetz regelt Mindestziele und deren Sicherstellung bei der Beschaffung bestimmter Straßenfahrzeuge und Dienstleistungen, für die diese Straßenfahrzeuge eingesetzt werden, durch öffentliche Auftraggeber und Sektorenauftraggeber.",
        },
        {
          marker: "(2)",
          text: "Soweit in diesem Gesetz oder aufgrund dieses Gesetzes nichts anderes geregelt ist, sind die allgemeinen vergaberechtlichen Vorschriften anzuwenden.",
        },
      ],
    },
  ],
  metadataSections: {
    NORM: [
      {
        KEYWORD: ["Neues Schlagwort 1 ", "Neues Schlagwort 2"],
        DIVERGENT_DOCUMENT_NUMBER: [
          "Neue abweichende Dokumentennummer 1",
          "Neue abweichende Dokumentennummer 2",
        ],
        RIS_ABBREVIATION_INTERNATIONAL_LAW: [
          "Neue Juris-Abkürzung für völkerrechtliche Vereinbarungen 1",
          "Neue Juris-Abkürzung für völkerrechtliche Vereinbarungen 2",
        ],
        UNOFFICIAL_ABBREVIATION: [
          "Neue Nichtamtliche Buchstabenabkürzung 1",
          "Neue Nichtamtliche Buchstabenabkürzung 2",
        ],
        UNOFFICIAL_SHORT_TITLE: [
          "Neue Nichtamtliche Kurzüberschrift 1",
          "Neue Nichtamtliche Kurzüberschrift 2",
        ],
        UNOFFICIAL_LONG_TITLE: [
          "Neue Nichtamtliche Langüberschrift1",
          "Neue Nichtamtliche Langüberschrift2",
        ],
        UNOFFICIAL_REFERENCE: [
          "Neue nichtamtliche Fundstelle 1",
          "Neue nichtamtliche Fundstelle 2",
        ],
        REFERENCE_NUMBER: ["Neues Aktenzeichen 1", "Neues Aktenzeichen 2"],
        DEFINITION: ["Neue Definition 1", "Neue Definition 2"],
        AGE_OF_MAJORITY_INDICATION: [
          "Neue Volljährigkeit 1",
          "Neue Volljährigkeit 2",
        ],
        VALIDITY_RULE: [
          "Neue Gültigkeitsregelung 1",
          "Neue Gültigkeitsregelung 2",
        ],
      },
    ],
    SUBJECT_AREA: [
      { SUBJECT_FNA: ["Neue FNA 1"], SUBJECT_GESTA: ["Neue GESTA 1"] },
      { SUBJECT_FNA: ["Neue FNA 2"], SUBJECT_GESTA: ["Neue GESTA 2"] },
    ],
    LEAD: [
      {
        LEAD_JURISDICTION: ["Neues Ressort 1"],
        LEAD_UNIT: ["Neue Organisationseinheit 1"],
      },
      {
        LEAD_JURISDICTION: ["Neues Ressort 2"],
        LEAD_UNIT: ["Neue Organisationseinheit 2"],
      },
    ],
    PARTICIPATION: [
      {
        PARTICIPATION_TYPE: ["Neue Art der Mitwirkung 1"],
        PARTICIPATION_INSTITUTION: ["Neues mitwirkendes Organ 1"],
      },
      {
        PARTICIPATION_TYPE: ["Neue Art der Mitwirkung 2"],
        PARTICIPATION_INSTITUTION: ["Neues mitwirkendes Organ 2"],
      },
    ],
    CITATION_DATE: [{ DATE: ["2022-03-02"] }, { YEAR: ["1990"] }],
    AGE_INDICATION: [
      {
        RANGE_START: ["1 Jahr"],
        RANGE_END: ["2 Jahre"],
      },
      {
        RANGE_START: ["3 Monate"],
        RANGE_END: ["4 Monate"],
      },
    ],
    NORM_PROVIDER: [
      {
        ENTITY: ["providerEntity"],
        DECIDING_BODY: ["providerDecidingBody"],
        RESOLUTION_MAJORITY: [false],
      },
    ],
    OFFICIAL_REFERENCE: [
      {
        PRINT_ANNOUNCEMENT: [
          {
            ANNOUNCEMENT_GAZETTE: ["ABC"],
            YEAR: ["2023"],
            NUMBER: ["1"],
            PAGE: ["2"],
            ADDITIONAL_INFO: ["TEST"],
            EXPLANATION: ["ABC"],
          },
        ],
      },
      {
        DIGITAL_ANNOUNCEMENT: [
          {
            ANNOUNCEMENT_MEDIUM: ["ABC"],
            DATE: ["2023-10-10"],
            YEAR: ["2023"],
            EDITION: ["2"],
            AREA_OF_PUBLICATION: ["ABC"],
            NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA: ["ABC"],
            ADDITIONAL_INFO: ["ABC"],
            EXPLANATION: ["ABC"],
          },
        ],
      },
      {
        EU_ANNOUNCEMENT: [
          {
            YEAR: ["2023"],
            SERIES: ["5"],
            NUMBER: ["1"],
            PAGE: ["2"],
            ADDITIONAL_INFO: ["ABC"],
            EXPLANATION: ["ABC"],
          },
        ],
      },
      {
        OTHER_OFFICIAL_ANNOUNCEMENT: [{ OTHER_OFFICIAL_REFERENCE: ["BGBl I"] }],
      },
    ],
    DIVERGENT_ENTRY_INTO_FORCE: [
      {
        DIVERGENT_ENTRY_INTO_FORCE_DEFINED: [
          {
            DATE: ["ABC"],
            NORM_CATEGORY: [
              NormCategory.BASE_NORM,
              NormCategory.TRANSITIONAL_NORM,
              NormCategory.AMENDMENT_NORM,
            ],
          },
        ],
      },
      {
        DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED: [
          {
            UNDEFINED_DATE: [
              UndefinedDate.UNDEFINED_UNKNOWN,
              UndefinedDate.UNDEFINED_FUTURE,
              UndefinedDate.UNDEFINED_NOT_PRESENT,
            ],
            NORM_CATEGORY: [
              NormCategory.BASE_NORM,
              NormCategory.TRANSITIONAL_NORM,
              NormCategory.AMENDMENT_NORM,
            ],
          },
        ],
      },
    ],
    DIVERGENT_EXPIRATION: [
      {
        DIVERGENT_EXPIRATION_DEFINED: [
          {
            DATE: ["2023-10-10"],
            NORM_CATEGORY: [
              NormCategory.BASE_NORM,
              NormCategory.TRANSITIONAL_NORM,
              NormCategory.AMENDMENT_NORM,
            ],
          },
        ],
      },
      {
        DIVERGENT_EXPIRATION_UNDEFINED: [
          {
            UNDEFINED_DATE: [
              UndefinedDate.UNDEFINED_UNKNOWN,
              UndefinedDate.UNDEFINED_FUTURE,
              UndefinedDate.UNDEFINED_NOT_PRESENT,
            ],
            NORM_CATEGORY: [
              NormCategory.BASE_NORM,
              NormCategory.TRANSITIONAL_NORM,
              NormCategory.AMENDMENT_NORM,
            ],
          },
        ],
      },
    ],
    DOCUMENT_TYPE: [
      {
        TYPE_NAME: ["abc"],
        NORM_CATEGORY: [
          NormCategory.BASE_NORM,
          NormCategory.TRANSITIONAL_NORM,
          NormCategory.AMENDMENT_NORM,
        ],
        TEMPLATE_NAME: ["foo", "bar", "foobar"],
      },
    ],
  },
  officialLongTitle:
    "Verordnung zur Anpassung von Rechtsverordnungen an das Tierarzneimittelrecht",
  officialShortTitle: "officialShortTitle",
  officialAbbreviation: "officialAbbreviation",
  risAbbreviation: "risAbbreviation",
  publicationDate: "2022-11-01",
  isExpirationDateTemp: false,
  categorizedReference: "categorizedReference",
  celexNumber: "celexNumber",
  completeCitation: "completeCitation",
  digitalEvidenceAppendix: "digitalEvidenceAppendix",
  digitalEvidenceExternalDataNote: "digitalEvidenceExternalDataNote",
  digitalEvidenceLink: "digitalEvidenceLink",
  digitalEvidenceRelatedData: "digitalEvidenceRelatedData",
  divergentEntryIntoForceDate: "2022-11-01",
  divergentEntryIntoForceDateState: "unbestimmt (unbekannt)",
  divergentExpirationDate: "2022-11-01",
  divergentExpirationDateState: "nicht vorhanden",
  documentCategory: "documentCategory",
  documentNumber: "documentNumber",
  documentStatusDate: "2022-11-01",
  documentStatusDescription: "documentStatusDescription",
  documentStatusEntryIntoForceDate: "2022-11-01",
  documentStatusProof: "documentStatusProof",
  documentStatusReference: "documentStatusReference",
  documentStatusWorkNote: "documentStatusWorkNote",
  documentTextProof: "documentTextProof",
  entryIntoForceDate: "2022-11-01",
  entryIntoForceDateState: "unbestimmt (zukünftig)",
  eli: "europeanLegalIdentifier",
  expirationDate: "2022-11-01",
  expirationDateState: "unbestimmt (unbekannt)",
  expirationNormCategory: "expirationNormCategory",
  otherDocumentNote: "otherDocumentNote",
  otherFootnote: "otherFootnote",
  footnoteChange: "footnoteChange",
  footnoteComment: "footnoteComment",
  footnoteDecision: "footnoteDecision",
  footnoteStateLaw: "footnoteStateLaw",
  footnoteEuLaw: "footnoteEuLaw",
  otherStatusNote: "otherStatusNote",
  principleEntryIntoForceDate: "2022-11-01",
  principleEntryIntoForceDateState: "nicht vorhanden",
  principleExpirationDate: "2022-11-01",
  principleExpirationDateState: "unbestimmt (zukünftig)",
  reissueArticle: "reissueArticle",
  reissueDate: "2022-11-01",
  reissueNote: "reissueNote",
  reissueReference: "reissueReference",
  repealArticle: "repealArticle",
  repealDate: "2022-11-01",
  repealNote: "repealNote",
  repealReferences: "repealReferences",
  statusDate: "2022-11-01",
  statusDescription: "statusDescription",
  statusNote: "statusNote",
  statusReference: "statusReference",
  text: "text",
}
