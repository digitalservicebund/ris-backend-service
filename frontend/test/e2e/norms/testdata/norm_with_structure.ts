import { DocumentSectionType, MetadatumType } from "@/domain/norm"
import { TestNormNoGuid } from "~/e2e/norms/fixtures"

export const norm_with_structure: TestNormNoGuid = {
  metadataSections: [
    {
      name: "NORM",
      metadata: [
        {
          type: MetadatumType.OFFICIAL_LONG_TITLE,
          value: "Patentanwaltsordnung",
        },
      ],
    },
  ],
  documentation: [
    {
      marker: "Erster Teil",
      heading: "Der Patentanwalt",
      type: DocumentSectionType.PART,
      documentation: [
        {
          marker: "§ 1",
          heading: "Stellung in der Rechtspflege",
          paragraphs: [],
        },
        {
          marker: "§ 2",
          heading: "Beruf des Patentanwalts",
          paragraphs: [],
        },
        {
          marker: "§ 3",
          heading: "Recht zur Beratung und Vertretung",
          paragraphs: [],
        },
      ],
    },
    {
      marker: "Zweiter Teil",
      heading: "Zulassung und allgemeine Vorschriften",
      type: DocumentSectionType.PART,
      documentation: [
        {
          marker: "Erster Abschnitt",
          heading: "Zulassung zur Patentantwaltschaft",
          type: DocumentSectionType.SECTION,
          documentation: [
            {
              marker: "Erster Unterabschnitt",
              heading: "Allgemeine Voraussetzungen",
              type: DocumentSectionType.SUBSECTION,
              documentation: [
                {
                  marker: "§ 5",
                  heading: "Zugang zum Beruf des Patentanwalts",
                  paragraphs: [],
                },
                {
                  marker: "§ 6",
                  heading: "Technische Befähigung",
                  paragraphs: [],
                },
                {
                  marker: "§ 7",
                  heading:
                    "Ausbildung auf dem Gebiet des gewerblichen Rechtsschutzes",
                  paragraphs: [],
                },
              ],
            },
          ],
        },
      ],
    },
    {
      marker: "Siebenter Teil",
      heading: "Berufsgerichtliches Verfahren",
      type: DocumentSectionType.PART,
      documentation: [
        {
          marker: "Erster Abschnitt",
          heading: "Allgemeines",
          type: DocumentSectionType.SECTION,
          documentation: [
            {
              marker: "Erster Unterabschnitt",
              heading: "Allgemeine Verfahrensregeln",
              type: DocumentSectionType.SUBSECTION,
              documentation: [
                {
                  marker: "§ 98",
                  heading:
                    "Vorschriften für das Verfahren und den Rechtsschutz bei überlangen Gerichtsverfahren",
                  paragraphs: [],
                },
                {
                  marker: "§ 99",
                  heading: "Keine Verhaftung des Patentanwalts",
                  paragraphs: [],
                },
                {
                  marker: "§ 100",
                  heading: "Verteidigung",
                  paragraphs: [],
                },
              ],
            },
            {
              marker: "Zweiter Unterabschnitt",
              heading:
                "Berufsgerichtliches Verfahren gegen Berufsausübungsgesellschaften",
              type: DocumentSectionType.SUBSECTION,
              documentation: [
                {
                  marker: "§ 103",
                  heading:
                    "Berufsgerichtliches Verfahren gegen Leitungspersonen und Berufsausübungsgesellschaften",
                  paragraphs: [],
                },
                {
                  marker: "§ 103a",
                  heading: "Vertretung von Berufsausübungsgesellschaften",
                  paragraphs: [],
                },
              ],
            },
          ],
        },
        {
          marker: "Zweiter Abschnitt",
          heading: "Verfahren im ersten Rechtszug",
          type: DocumentSectionType.SECTION,
          documentation: [
            {
              marker: "Erster Unterabschnitt",
              heading: "Allgemeine Vorschriften",
              type: DocumentSectionType.SUBSECTION,
              documentation: [
                {
                  marker: "§ 104",
                  heading: "Zuständigkeit",
                  paragraphs: [],
                },
                {
                  marker: "§ 105",
                  heading: "Mitwirkung der Staatsanwaltschaft",
                  paragraphs: [],
                },
              ],
            },
            {
              marker: "Zweiter Unterabschnitt",
              heading: "Einleitung des berufsgerichtlichen Verfahrens",
              type: DocumentSectionType.SUBSECTION,
              documentation: [
                {
                  marker: "§ 106",
                  heading: "Einleitung des berufsgerichtlichen Verfahrens",
                  paragraphs: [],
                },
                {
                  marker: "§§ 109 bis 114",
                  heading: "----",
                  paragraphs: [],
                },
              ],
            },
          ],
        },
      ],
    },
  ],
}
