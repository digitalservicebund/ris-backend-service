import { NormData } from "../fixtures"
import { NormCategory } from "@/domain/Norm"

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
      { SUBJECT_FNA: ["FNA 315-12"] },
      { SUBJECT_FNA: ["FNA 671-34"] },
      { SUBJECT_GESTA: ["GESTA 123-45"] },
      { SUBJECT_GESTA: ["GESTA 678-90"] },
    ],
    LEAD: [
      { LEAD_JURISDICTION: ["BMVBS"], LEAD_UNIT: ["RS III 2"] },
      { LEAD_JURISDICTION: ["BMI"], LEAD_UNIT: ["Z I 2"] },
    ],
    PARTICIPATION: [
      { PARTICIPATION_TYPE: ["EZ"], PARTICIPATION_INSTITUTION: ["BR"] },
      { PARTICIPATION_TYPE: ["RU"], PARTICIPATION_INSTITUTION: ["NT"] },
    ],
    CITATION_DATE: [{ DATE: ["2023-01-02"] }, { DATE: ["2001-03-10"] }],
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
    DOCUMENT_TYPE: [
      { TYPE_NAME: ["RV"], NORM_CATEGORY: [NormCategory.BASE_NORM] },
    ],
  },
  officialLongTitle:
    "Verordnung zur Anpassung von Rechtsverordnungen an das Tierarzneimittelrecht",
  officialShortTitle: "Angepasstes Tierarzneimittelrecht",
  risAbbreviation: "Tierarznei",
  documentCategory: "NR",
  entryIntoForceDate: "2007-01-01",
  principleEntryIntoForceDate: "2007-01-01",
  divergentEntryIntoForceDateState: "",
  expirationDateState: "unbestimmt (unbekannt)",
  principleExpirationDateState: "unbestimmt (unbekannt)",
  divergentExpirationDateState: "nicht vorhanden",
  announcementDate: "2023-01-06",
  printAnnouncementGazette: "BGBl I",
  printAnnouncementYear: "2023",
  printAnnouncementPage: "3",
  eli: "eli/bgbl-1/2023/s3",
}
