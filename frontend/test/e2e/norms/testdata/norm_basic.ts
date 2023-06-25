import { NormData } from "../fixtures"
import { NormCategory, UndefinedDate } from "@/domain/Norm"

export const normData: NormData = {
  jurisZipFileName: "Tierarznei.3-0_multi.zip",
  articles: [
    {
      marker: "Art 2",
      title:
        "Änderung der Verordnung über Stoffe mit pharmakologischer Wirkung",
      paragraphs: [
        {
          marker: "",
          text: "§ 2 der Verordnung über Stoffe mit pharmakologischer Wirkung in der Fassung der Bekanntmachung vom 8. Juli 2009 (BGBl. I S. 1768) wird wie folgt geändert:",
        },
      ],
    },
    {
      title: "Inkrafttreten, Außerkrafttreten",
      marker: "Art 3",
      paragraphs: [
        {
          marker: "(1)",
          text: "Diese Verordnung tritt am Tag nach der Verkündung in Kraft.",
        },
        {
          marker: "(2)",
          text: "Gleichzeitig treten außer Kraft",
        },
      ],
    },
  ],
  metadataSections: {
    NORM: [
      {
        KEYWORD: ["Mantelverordnung", "BGBl I 2023, Nr 003"],
        DIVERGENT_DOCUMENT_NUMBER: ["BJNR0030A0023"],
        RIS_ABBREVIATION_INTERNATIONAL_LAW: [
          "RIS-Abkürzung für völkerrechtliche Vereinbarungen 1",
          "RIS-Abkürzung für völkerrechtliche Vereinbarungen 2",
        ],
        UNOFFICIAL_ABBREVIATION: [
          "Nichtamtliche Buchstabenabkürzung 1",
          "Nichtamtliche Buchstabenabkürzung 2",
        ],
        UNOFFICIAL_SHORT_TITLE: [
          "Nichtamtliche Kurzüberschrift 1",
          "Nichtamtliche Kurzüberschrift 2",
        ],
        UNOFFICIAL_LONG_TITLE: [
          "Nichtamtliche Langüberschrift1",
          "Nichtamtliche Langüberschrift2",
        ],
        UNOFFICIAL_REFERENCE: ["BGBl I 2009, 1102", "BGBl II 2022, 1351"],
        REFERENCE_NUMBER: ["Aktenzeichen 1", "Aktenzeichen 2"],
        DEFINITION: ["Definition 1", "Definition 2"],
        AGE_OF_MAJORITY_INDICATION: ["minderjährig", "volljährig"],
        VALIDITY_RULE: ["Gültigkeitsregelung 1", "Gültigkeitsregelung 2"],
      },
    ],
    SUBJECT_AREA: [
      { SUBJECT_FNA: ["315-12"] },
      { SUBJECT_FNA: ["671-34"] },
      { SUBJECT_GESTA: ["123-45"] },
      { SUBJECT_GESTA: ["678-90"] },
    ],
    LEAD: [
      { LEAD_JURISDICTION: ["BMVBS"], LEAD_UNIT: ["RS III 2"] },
      { LEAD_JURISDICTION: ["BMI"], LEAD_UNIT: ["Z I 2"] },
    ],
    PARTICIPATION: [
      { PARTICIPATION_TYPE: ["EZ"], PARTICIPATION_INSTITUTION: ["BR"] },
      { PARTICIPATION_TYPE: ["RU"], PARTICIPATION_INSTITUTION: ["NT"] },
    ],
    CITATION_DATE: [{ DATE: ["02.01.2023"] }, { DATE: ["10.03.2001"] }],
    AGE_INDICATION: [
      { RANGE_START: ["Lebensjahr 28"] },
      { RANGE_START: ["Monate 8"] },
    ],
    NORM_PROVIDER: [
      { ENTITY: ["DEU"], DECIDING_BODY: ["BT"], RESOLUTION_MAJORITY: [true] },
      {
        ENTITY: ["DEU"],
        DECIDING_BODY: ["BMinI"],
        RESOLUTION_MAJORITY: [false],
      },
    ],
    OFFICIAL_REFERENCE: [
      {
        PRINT_ANNOUNCEMENT: [
          { ANNOUNCEMENT_GAZETTE: ["BGBl I"], YEAR: ["2023"], PAGE: ["3"] },
        ],
      },
      {
        PRINT_ANNOUNCEMENT: [
          { ANNOUNCEMENT_GAZETTE: ["BGBl II"], YEAR: ["2024"], PAGE: ["9"] },
        ],
      },
      {
        DIGITAL_ANNOUNCEMENT: [
          { ANNOUNCEMENT_MEDIUM: ["BGBl I"], EDITION: ["3"], YEAR: ["2023"] },
        ],
      },
      {
        DIGITAL_ANNOUNCEMENT: [
          { ANNOUNCEMENT_MEDIUM: ["BGBl II"], EDITION: ["9"], YEAR: ["2024"] },
        ],
      },
    ],
    DIVERGENT_ENTRY_INTO_FORCE: [
      {
        DIVERGENT_ENTRY_INTO_FORCE_DEFINED: [
          {
            DATE: ["13.01.2023"],
            NORM_CATEGORY: [NormCategory.BASE_NORM],
          },
        ],
      },
      {
        DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED: [
          {
            UNDEFINED_DATE: [UndefinedDate.UNDEFINED_FUTURE],
            NORM_CATEGORY: [NormCategory.AMENDMENT_NORM],
          },
        ],
      },
      {
        DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED: [
          {
            UNDEFINED_DATE: [UndefinedDate.UNDEFINED_UNKNOWN],
            NORM_CATEGORY: [NormCategory.TRANSITIONAL_NORM],
          },
        ],
      },
      {
        DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED: [
          { UNDEFINED_DATE: [UndefinedDate.UNDEFINED_UNKNOWN] },
        ],
      },
    ],
    DIVERGENT_EXPIRATION: [
      {
        DIVERGENT_EXPIRATION_DEFINED: [
          { DATE: ["13.01.2023"], NORM_CATEGORY: [NormCategory.BASE_NORM] },
        ],
      },
      {
        DIVERGENT_EXPIRATION_UNDEFINED: [
          {
            UNDEFINED_DATE: [UndefinedDate.UNDEFINED_FUTURE],
            NORM_CATEGORY: [NormCategory.TRANSITIONAL_NORM],
          },
        ],
      },
    ],
    DOCUMENT_TYPE: [
      { TYPE_NAME: ["RV"], NORM_CATEGORY: [NormCategory.BASE_NORM] },
    ],
    CATEGORIZED_REFERENCE: [
      { TEXT: ["&A 31 &B Art 1 Nr 4 Buchst a &E HGB &E3 § 246 &E6 Abs 1"] },
      {
        TEXT: [
          "&A 32 &B Art 1 Nr 4 Buchst b &E HGB &E3 § 246 &E6 Abs 2 S 2 und 3",
        ],
      },
    ],
    ENTRY_INTO_FORCE: [{ DATE: ["01.01.2007"] }],
    PRINCIPLE_ENTRY_INTO_FORCE: [{ DATE: ["01.01.2007"] }],
    EXPIRATION: [{ UNDEFINED_DATE: [UndefinedDate.UNDEFINED_UNKNOWN] }],
    PRINCIPLE_EXPIRATION: [
      { UNDEFINED_DATE: [UndefinedDate.UNDEFINED_UNKNOWN] },
    ],
  },
  officialLongTitle:
    "Verordnung zur Anpassung von Rechtsverordnungen an das Tierarzneimittelrecht",
  officialShortTitle: "Angepasstes Tierarzneimittelrecht",
  risAbbreviation: "Tierarznei",
  documentCategory: "NR",
  announcementDate: "06.01.2023",
  printAnnouncementGazette: "BGBl I",
  printAnnouncementYear: "2023",
  printAnnouncementPage: "3",
  eli: "eli/bgbl-1/2023/s3",
}
