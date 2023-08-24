import { NormData } from "../fixtures"
import { DocumentSectionType, NormCategory, UndefinedDate } from "@/domain/norm"

export const normData: NormData = {
  jurisZipFileName: "Tierarznei.3-0_multi.zip",
  documentation: [
    {
      type: DocumentSectionType.SECTION,
      marker: "Art 2",
      heading:
        "Änderung der Verordnung über Stoffe mit pharmakologischer Wirkung",
      documentation: [
        {
          marker: "",
          text: "§ 2 der Verordnung über Stoffe mit pharmakologischer Wirkung in der Fassung der Bekanntmachung vom 8. Juli 2009 (BGBl. I S. 1768) wird wie folgt geändert:",
        },
      ],
    },
    {
      type: DocumentSectionType.SECTION,
      marker: "Inkrafttreten, Außerkrafttreten",
      heading: "Art 3",
      documentation: [
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
        OFFICIAL_LONG_TITLE: [
          "Verordnung zur Anpassung von Rechtsverordnungen an das Tierarzneimittelrecht",
        ],
        OFFICIAL_SHORT_TITLE: ["Angepasstes Tierarzneimittelrecht"],
        RIS_ABBREVIATION: ["Tierarznei"],
        DOCUMENT_CATEGORY: ["NR"],
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
    ANNOUNCEMENT_DATE: [{ DATE: ["06.01.2023"] }],
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
    STATUS_INDICATION: [
      {
        STATUS: [
          {
            NOTE: ["Zuletzt geändert durch Art. 6 G v. 22.12.2020 I 3328"],
            DESCRIPTION: ["Art. 6 G"],
            DATE: ["22.12.2020"],
            REFERENCE: ["3328"],
          },
        ],
      },
      {
        STATUS: [
          {
            NOTE: ["Zuletzt geändert durch Art. 7"],
            DESCRIPTION: ["Art. 7"],
          },
        ],
      },
      {
        REISSUE: [
          {
            NOTE: ["Neufassung durch Art. 35 v. 15.7.1999 I 3249"],
            ARTICLE: ["Art. 35"],
            DATE: ["15.07.1999"],
            REFERENCE: ["3249"],
          },
        ],
      },
      {
        REPEAL: [
          {
            TEXT: [
              "V aufgeh. durch Art. 4 Nr. 1 G v. 15.7.1999 I 1618 (StARefG) mWv 1.1.2000",
            ],
          },
        ],
      },
      {
        REPEAL: [
          {
            TEXT: [
              "V aufgeh. durch Art. 10 Nr. 9 G v. 29.12.1999 I 8172 (StARefG) mWv 3.2.2001",
            ],
          },
        ],
      },
      {
        OTHER_STATUS: [{ NOTE: ["Eine sonstige Stand-Angabe"] }],
      },
    ],
    FOOTNOTES: [
      {
        FOOTNOTE: [
          {
            FOOTNOTE_OTHER: [
              "Überschrift: Das G ist gem. Art. 4 d. G v. 27.8.1986 I 1410\n" +
                "am 1.11.1986 in Kraft getreten*.\n" +
                "*&",
            ],
          },
          {
            FOOTNOTE_OTHER: [
              "(+++ Textnachweis ab:                             1.11.1986 +++)",
            ],
          },
          {
            FOOTNOTE_OTHER: [
              "(+++ Änderungen aufgrund EinigVtr vgl. §§ 8a, 9a, 10a u. 32 +++)",
            ],
          },
          {
            FOOTNOTE_OTHER: [
              "(+++ Stand:    Änderung durch Art. 2 G v. 12. 9.1996 I 1354 +++)\n" +
                "*&",
            ],
          },
          {
            FOOTNOTE_CHANGE: [
              "G aufgeh. durch Art. 13 Satz 3 G v. 27.9.1994 I 2705 mWv 6.10.1996,\n" +
                "§§ 5a und 5b nach Maßgabe d. § 64 KrW-/AbfG 2929-27-2 G v. 27.9.1994\n" +
                "I 2705; §§ 5a u. 5b abgelöst durch § 11 V v. 27.10.1987 I 2335 idF\n" +
                "d. Art. 1 Nr. 6 V v. 16.4.2002 I 1360 mWv 1.5.2002",
            ],
          },
        ],
      },
      {
        FOOTNOTE: [
          {
            FOOTNOTE_COMMENT: [
              'Überschrift u. Art. 1 Satz 1 Kursivdruck: "Schwerbeschädigter" jetzt "Schwerbehinderter" gem. Art. III § 4 G v. 24.4.1974 I 981',
            ],
          },
        ],
      },
      {
        FOOTNOTE: [
          {
            FOOTNOTE_DECISION: [
              "Nichtig gem. BVerfGE v. 25.6.1969 I 1444 - 2 BvR 128/66 -",
            ],
          },
        ],
      },
      {
        FOOTNOTE: [
          {
            FOOTNOTE_STATE_LAW: [
              "Das G gilt nach Maßgabe d. Art. 9 Abs. 2 u. d. Art. 9 Abs. 5\n" +
                "\n" +
                "EinigVtr v. 31.8.1990 iVm Art. 1 G v. 23.9.1990 II 885, 1194 in den\n" +
                "\n" +
                "beigetretenen Bundesländern (Art. 1 Abs. 1 EinigVtr) als Landesrecht\n" +
                "\n" +
                "fort*",
            ],
          },
          {
            FOOTNOTE_OTHER: ["Nur mit Überschrift aufgenommen\n" + "\n" + "*&"],
          },
        ],
      },
      {
        FOOTNOTE: [
          { FOOTNOTE_EU_LAW: ["EGRL 81/2009 (CELEX Nr: 32009L0081)"] },
          { FOOTNOTE_EU_LAW: ["EGRL 81/2009 (CELEX Nr: 32009L0081) vgl."] },
          { FOOTNOTE_EU_LAW: ["Art. 2 d. G v. 25.3.2020 I 674"] },
          { FOOTNOTE_EU_LAW: ["EURL 23/2014 (CELEX Nr: 32014L0023) vgl."] },
          { FOOTNOTE_EU_LAW: ["Art. 2 d. G v. 25.3.2020 I 674"] },
          { FOOTNOTE_EU_LAW: ["EURL 24/2014 (CELEX Nr: 32014L0024) vgl."] },
          { FOOTNOTE_EU_LAW: ["Art. 2 d. G v. 25.3.2020 I 674"] },
          { FOOTNOTE_EU_LAW: ["EURL 25/2014 (CELEX Nr: 32014L0025) vgl."] },
          { FOOTNOTE_EU_LAW: ["Art. 2 d. G v. 25.3.2020 I 674"] },
        ],
      },
      {
        FOOTNOTE: [
          {
            FOOTNOTE_OTHER: [
              "Das G wurde als Artikel 1 G v. 20.7.2000 I 1045 (SeuchRNeuG) vom Bundestag mit Zustimmung des Bundesrates beschlossen. Es ist gem. Art. 5 Abs. 1 Satz 1 dieses G mWv 1.1.2001, §§ 37 und 38 mWv 26.7.2000 in Kraft getreten.",
            ],
          },
        ],
      },
    ],
    DOCUMENT_STATUS_SECTION: [
      {
        DOCUMENT_TEXT_PROOF: [{ TEXT: ["Textnachweis ab: 26.6.2017"] }],
      },
      {
        DOCUMENT_STATUS: [
          {
            WORK_NOTE: ["PRÄTEXT(2)"],
            DESCRIPTION: ["BGBl I"],
            YEAR: ["2014"],
            REFERENCE: ["610"],
          },
        ],
      },
      {
        DOCUMENT_STATUS: [
          {
            WORK_NOTE: ["PRÄTEXT(16)"],
            DESCRIPTION: ["BGBl I"],
            YEAR: ["2008"],
            REFERENCE: ["561"],
          },
        ],
      },
    ],
  },
  eli: "eli/bgbl-1/2023/s3",
}
