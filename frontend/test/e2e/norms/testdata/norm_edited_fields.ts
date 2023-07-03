import {
  NormCategory,
  OtherType,
  ProofIndication,
  ProofType,
  UndefinedDate,
} from "../../../../src/domain/Norm"
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
        OFFICIAL_LONG_TITLE: [
          "Verordnung zur Anpassung von Rechtsverordnungen an das Tierarzneimittelrecht",
        ],
        OFFICIAL_SHORT_TITLE: ["officialShortTitle"],
        OFFICIAL_ABBREVIATION: ["officialAbbreviation"],
        RIS_ABBREVIATION: ["risAbbreviation"],
        CELEX_NUMBER: ["celexNumber"],
        COMPLETE_CITATION: ["completeCitation"],
        DOCUMENT_CATEGORY: ["documentCategory"],
        DOCUMENT_NUMBER: ["documentNumber"],
        TEXT: ["text"],
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
    CITATION_DATE: [{ DATE: ["02.03.2023"] }, { YEAR: ["1990"] }],
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
            ADDITIONAL_INFO: ["additional info 1", "additional info 2"],
            EXPLANATION: ["explanation 1", "explanation 2"],
          },
        ],
      },
      {
        DIGITAL_ANNOUNCEMENT: [
          {
            ANNOUNCEMENT_MEDIUM: ["ABC"],
            DATE: ["10.10.2023"],
            YEAR: ["2023"],
            EDITION: ["2"],
            AREA_OF_PUBLICATION: ["ABC"],
            NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA: ["ABC"],
            ADDITIONAL_INFO: ["additional info 1", "additional info 2"],
            EXPLANATION: ["explanation 1", "explanation 2"],
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
            ADDITIONAL_INFO: ["additional info 1", "additional info 2"],
            EXPLANATION: ["explanation 1", "explanation 2"],
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
            DATE: ["23.02.2015"],
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
            UNDEFINED_DATE: [UndefinedDate.UNDEFINED_UNKNOWN],
            NORM_CATEGORY: [
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
            DATE: ["10.10.1990"],
            NORM_CATEGORY: [
              NormCategory.BASE_NORM,
              NormCategory.TRANSITIONAL_NORM,
            ],
          },
        ],
      },
      {
        DIVERGENT_EXPIRATION_UNDEFINED: [
          {
            UNDEFINED_DATE: [UndefinedDate.UNDEFINED_NOT_PRESENT],
            NORM_CATEGORY: [NormCategory.TRANSITIONAL_NORM],
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
    CATEGORIZED_REFERENCE: [
      {
        TEXT: ["foo"],
      },
      {
        TEXT: ["bar"],
      },
    ],
    ENTRY_INTO_FORCE: [{ DATE: ["01.11.2022"] }],
    PRINCIPLE_ENTRY_INTO_FORCE: [
      { UNDEFINED_DATE: [UndefinedDate.UNDEFINED_NOT_PRESENT] },
    ],
    EXPIRATION: [{ DATE: ["01.11.2022"] }],
    PRINCIPLE_EXPIRATION: [
      { UNDEFINED_DATE: [UndefinedDate.UNDEFINED_NOT_PRESENT] },
    ],
    DIGITAL_EVIDENCE: [
      {
        LINK: ["link"],
        RELATED_DATA: ["related data"],
        EXTERNAL_DATA_NOTE: ["external data note"],
        APPENDIX: ["appendix"],
      },
    ],
    DOCUMENT_STATUS_SECTION: [
      {
        DOCUMENT_STATUS: [
          {
            WORK_NOTE: ["work note"],
            DESCRIPTION: ["description"],
            DATE: ["12.05.2022"],
            REFERENCE: ["reference"],
            ENTRY_INTO_FORCE_DATE_NOTE: ["400"],
            PROOF_INDICATION: [ProofIndication.NOT_YET_CONSIDERED],
          },
        ],
      },
      {
        DOCUMENT_TEXT_PROOF: [
          { PROOF_TYPE: [ProofType.TEXT_PROOF_FROM], TEXT: ["text"] },
        ],
      },
      {
        DOCUMENT_OTHER: [{ OTHER_TYPE: [OtherType.TEXT_IN_PROGRESS] }],
      },
    ],
    FOOTNOTES: [
      {
        FOOTNOTE: [
          { FOOTNOTE_REFERENCE: ["§ 7 Abs. 1a Satz 1 u. 2"] },
          { FOOTNOTE_CHANGE: ["eine ganze Menge Text"] },
          { FOOTNOTE_EU_LAW: ["irgendwas halt"] },
        ],
      },
    ],
    PUBLICATION_DATE: [{ DATE: ["01.11.2022"] }],
    STATUS_INDICATION: [
      {
        STATUS: [
          {
            NOTE: ["Status note"],
            DESCRIPTION: ["Status description"],
            DATE: ["09.11.2018"],
            REFERENCE: ["Status reference 1", "Status reference 2"],
          },
        ],
      },
      {
        REISSUE: [
          {
            NOTE: ["Reissue note"],
            ARTICLE: ["Reissue article"],
            DATE: ["09.11.2018"],
            REFERENCE: ["Reissue reference"],
          },
        ],
      },
      {
        REPEAL: [
          {
            TEXT: ["Repeal text"],
          },
        ],
      },
      {
        OTHER_STATUS: [{ NOTE: ["Other status note"] }],
      },
    ],
    ANNOUNCEMENT_DATE: [
      { DATE: ["01.10.2021"], TIME: ["08:45"] },
      { YEAR: ["2023"] },
    ],
  },
  eli: "europeanLegalIdentifier",
}
