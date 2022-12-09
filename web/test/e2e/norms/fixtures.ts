import { expect, test } from "@playwright/test"
import normCleanCars from "./testdata/norm_clean_cars.json"

// Declare the types of your fixtures.
type MyFixtures = {
  createdGuid: string
}

export const testWithImportedNorm = test.extend<MyFixtures>({
  createdGuid: async ({ request }, use) => {
    const backendHost = process.env.E2E_BASE_URL ?? "http://localhost:8080"
    const response = await request.post(`${backendHost}/api/v1/norms`, {
      data: normCleanCars,
    })
    expect(response.ok()).toBeTruthy()
    const location = response.headers()["location"]
    const normsGuid = location.slice(location.lastIndexOf("/") + 1)

    await use(normsGuid)
  },
})

export function getNormBySections(norm) {
  return [
    {
      heading: "Allgemeine Angaben",
      fields: [
        {
          isCheckbox: false,
          name: "officialLongTitle",
          label: "Amtliche Langüberschrift",
          value: norm.officialLongTitle,
        },
        {
          isCheckbox: false,
          name: "risAbbreviation",
          label: "Juris-Abkürzung",
          value: norm.risAbbreviation,
        },
        {
          isCheckbox: false,
          name: "risAbbreviationInternationalLaw",
          label: "Juris-Abkürzung für völkerrechtliche Vereinbarungen",
          value: norm.risAbbreviationInternationalLaw,
        },
        {
          isCheckbox: false,
          name: "documentNumber",
          label: "Dokumentennummer",
          value: norm.documentNumber,
        },
        {
          isCheckbox: false,
          name: "divergentDocumentNumber",
          label: "Abweichende Dokumentnummer",
          value: norm.divergentDocumentNumber,
        },
        {
          isCheckbox: false,
          name: "documentCategory",
          label: "Dokumentart",
          value: norm.documentCategory,
        },
        {
          isCheckbox: false,
          name: "frameKeywords",
          label: "Schlagwörter im Rahmenelement",
          value: norm.frameKeywords,
        },
      ],
    },
    {
      heading: "Dokumenttyp",
      fields: [
        {
          isCheckbox: false,
          name: "documentTypeName",
          label: "Typbezeichnung",
          value: norm.documentTypeName,
        },
        {
          isCheckbox: false,
          name: "documentNormCategory",
          label: "Art der Norm",
          value: norm.documentNormCategory,
        },
        {
          isCheckbox: false,
          name: "documentTemplateName",
          label: "Bezeichnung gemäß Vorlage",
          value: norm.documentTemplateName,
        },
      ],
    },
    {
      heading: "Normgeber",
      fields: [
        {
          isCheckbox: false,
          name: "providerEntity",
          label: "Staat, Land, Stadt, Landkreis oder juristische Person",
          value: norm.providerEntity,
        },
        {
          isCheckbox: false,
          name: "providerDecidingBody",
          label: "Beschließendes Organ",
          value: norm.providerDecidingBody,
        },
        {
          isCheckbox: true,
          name: "providerIsResolutionMajority",
          label: "Beschlussfassung mit qualifizierter Mehrheit",
          value: norm.providerIsResolutionMajority,
        },
      ],
    },
    {
      heading: "Mitwirkende Organe",
      fields: [
        {
          isCheckbox: false,
          name: "participationType",
          label: "Art der Mitwirkung",
          value: norm.participationType,
        },
        {
          isCheckbox: false,
          name: "participationInstitution",
          label: "Mitwirkendes Organ",
          value: norm.participationInstitution,
        },
      ],
    },
    {
      heading: "Federführung",
      fields: [
        {
          isCheckbox: false,
          name: "leadJurisdiction",
          label: "Ressort",
          value: norm.leadJurisdiction,
        },
        {
          isCheckbox: false,
          name: "leadUnit",
          label: "Organisationseinheit",
          value: norm.leadUnit,
        },
      ],
    },
    {
      heading: "Sachgebiet",
      fields: [
        {
          isCheckbox: false,
          name: "subjectFna",
          label: "FNA-Nummer",
          value: norm.subjectFna,
        },
        {
          isCheckbox: false,
          name: "subjectPreviousFna",
          label: "Frühere FNA-Nummer",
          value: norm.subjectPreviousFna,
        },
        {
          isCheckbox: false,
          name: "subjectGesta",
          label: "GESTA number",
          value: norm.subjectGesta,
        },
        {
          isCheckbox: false,
          name: "subjectBgb3",
          label: "Bundesgesetzblatt Teil III",
          value: norm.subjectBgb3,
        },
      ],
    },
    {
      heading: "Überschriften und Abkürzungen",
      fields: [
        {
          isCheckbox: false,
          name: "officialShortTitle",
          label: "Amtliche Kurzüberschrift",
          value: norm.officialShortTitle,
        },
        {
          isCheckbox: false,
          name: "officialAbbreviation",
          label: "Amtliche Buchstabenabkürzung",
          value: norm.officialAbbreviation,
        },
        {
          isCheckbox: false,
          name: "unofficialLongTitle",
          label: "Nichtamtliche Langüberschrift",
          value: norm.unofficialLongTitle,
        },
        {
          isCheckbox: false,
          name: "unofficialShortTitle",
          label: "Nichtamtliche Kurzüberschrift",
          value: norm.unofficialShortTitle,
        },
        {
          isCheckbox: false,
          name: "unofficialAbbreviation",
          label: "Nichtamtliche Buchstabenabkürzung",
          value: norm.unofficialAbbreviation,
        },
      ],
    },
    {
      heading: "Inkrafttreten",
      fields: [
        {
          isCheckbox: false,
          name: "entryIntoForceDate",
          label: "Datum des Inkrafttretens",
          value: norm.entryIntoForceDate,
        },
        {
          isCheckbox: false,
          name: "entryIntoForceDateState",
          label: "Unbestimmtes Datum des Inkrafttretens",
          value: norm.entryIntoForceDateState,
        },
        {
          isCheckbox: false,
          name: "principleEntryIntoForceDate",
          label: "Grundsätzliches Inkrafttretedatum",
          value: norm.principleEntryIntoForceDate,
        },
        {
          isCheckbox: false,
          name: "principleEntryIntoForceDateState",
          label: "Unbestimmtes Grundsätzliches Inkrafttretedatum",
          value: norm.principleEntryIntoForceDateState,
        },
        {
          isCheckbox: false,
          name: "divergentEntryIntoForceDate",
          label: "Abweichendes Inkrafttretedatum",
          value: norm.divergentEntryIntoForceDate,
        },
        {
          isCheckbox: false,
          name: "divergentEntryIntoForceDateState",
          label: "Unbestimmtes Abweichendes Inkrafttretedatum",
          value: norm.divergentEntryIntoForceDateState,
        },
      ],
    },
    {
      heading: "Außerkrafttreten",
      fields: [
        {
          isCheckbox: false,
          name: "expirationDate",
          label: "Datum des Außerkrafttretens",
          value: norm.expirationDate,
        },
        {
          isCheckbox: false,
          name: "expirationDateState",
          label: "Unbestimmtes Datum des Außerkrafttretens",
          value: norm.expirationDateState,
        },
        {
          isCheckbox: true,
          name: "isExpirationDateTemp",
          label: "Befristet",
          value: norm.isExpirationDateTemp,
        },
        {
          isCheckbox: false,
          name: "principleExpirationDate",
          label: "Grundsätzliches Außerkrafttretedatum",
          value: norm.principleExpirationDate,
        },
        {
          isCheckbox: false,
          name: "principleExpirationDateState",
          label: "Unbestimmtes Grundsätzliches Außerkrafttretdatum",
          value: norm.principleExpirationDateState,
        },
        {
          isCheckbox: false,
          name: "divergentExpirationDate",
          label: "Abweichendes Außerkrafttretedatum",
          value: norm.divergentExpirationDate,
        },
        {
          isCheckbox: false,
          name: "divergentExpirationDateState",
          label: "Unbestimmtes Abweichendes Außerkrafttretdatum",
          value: norm.divergentExpirationDateState,
        },
        {
          isCheckbox: false,
          name: "expirationNormCategory",
          label: "Art der Norm",
          value: norm.expirationNormCategory,
        },
      ],
    },
    {
      heading: "Verkündungsdatum",
      fields: [
        {
          isCheckbox: false,
          name: "announcementDate",
          label: "Verkündungsdatum",
          value: norm.announcementDate,
        },
        {
          isCheckbox: false,
          name: "publicationDate",
          label: "Veröffentlichungsdatum",
          value: norm.publicationDate,
        },
      ],
    },
    {
      heading: "Zitierdatum",
      fields: [
        {
          isCheckbox: false,
          name: "citationDate",
          label: "Zitierdatum",
          value: norm.citationDate,
        },
      ],
    },
    {
      heading: "Amtliche Fundstelle",
      sections: [
        {
          heading: "Papierverkündung",
          fields: [
            {
              isCheckbox: false,
              name: "printAnnouncementGazette",
              label: "Verkündungsblatt",
              value: norm.printAnnouncementGazette,
            },
            {
              isCheckbox: false,
              name: "printAnnouncementYear",
              label: "Jahr",
              value: norm.printAnnouncementYear,
            },
            {
              isCheckbox: false,
              name: "printAnnouncementNumber",
              label: "Nummer",
              value: norm.printAnnouncementNumber,
            },
            {
              isCheckbox: false,
              name: "printAnnouncementPage",
              label: "Seitenzahl",
              value: norm.printAnnouncementPage,
            },
            {
              isCheckbox: false,
              name: "printAnnouncementInfo",
              label: "Zusatzangaben",
              value: norm.printAnnouncementInfo,
            },
            {
              isCheckbox: false,
              name: "printAnnouncementExplanations",
              label: "Erläuterungen",
              value: norm.printAnnouncementExplanations,
            },
          ],
        },
        {
          heading: "Elektronisches Verkündungsblatt",
          fields: [
            {
              isCheckbox: false,
              name: "digitalAnnouncementMedium",
              label: "Verkündungsmedium",
              value: norm.digitalAnnouncementMedium,
            },
            {
              isCheckbox: false,
              name: "digitalAnnouncementDate",
              label: "Verkündungsdatum",
              value: norm.digitalAnnouncementDate,
            },
            {
              isCheckbox: false,
              name: "digitalAnnouncementEdition",
              label: "Ausgabenummer",
              value: norm.digitalAnnouncementEdition,
            },
            {
              isCheckbox: false,
              name: "digitalAnnouncementYear",
              label: "Jahr",
              value: norm.digitalAnnouncementYear,
            },
            {
              isCheckbox: false,
              name: "digitalAnnouncementPage",
              label: "Seitenzahlen",
              value: norm.digitalAnnouncementPage,
            },
            {
              isCheckbox: false,
              name: "digitalAnnouncementArea",
              label: "Bereich der Veröffentlichung",
              value: norm.digitalAnnouncementArea,
            },
            {
              isCheckbox: false,
              name: "digitalAnnouncementAreaNumber",
              label: "Nummer der Veröffentlichung im jeweiligen Bereich",
              value: norm.digitalAnnouncementAreaNumber,
            },
            {
              isCheckbox: false,
              name: "digitalAnnouncementInfo",
              label: "Zusatzangaben",
              value: norm.digitalAnnouncementInfo,
            },
            {
              isCheckbox: false,
              name: "digitalAnnouncementExplanations",
              label: "Erläuterungen",
              value: norm.digitalAnnouncementExplanations,
            },
          ],
        },
        {
          heading: "Amtsblatt der EU",
          fields: [
            {
              isCheckbox: false,
              name: "euAnnouncementGazette",
              label: "Amtsblatt der EU",
              value: norm.euAnnouncementGazette,
            },
            {
              isCheckbox: false,
              name: "euAnnouncementYear",
              label: "Jahresangabe",
              value: norm.euAnnouncementYear,
            },
            {
              isCheckbox: false,
              name: "euAnnouncementSeries",
              label: "Reihe",
              value: norm.euAnnouncementSeries,
            },
            {
              isCheckbox: false,
              name: "euAnnouncementNumber",
              label: "Nummer des Amtsblatts",
              value: norm.euAnnouncementNumber,
            },
            {
              isCheckbox: false,
              name: "euAnnouncementPage",
              label: "Seitenzahl",
              value: norm.euAnnouncementPage,
            },
            {
              isCheckbox: false,
              name: "euAnnouncementInfo",
              label: "Zusatzangaben",
              value: norm.euAnnouncementInfo,
            },
            {
              isCheckbox: false,
              name: "euAnnouncementExplanations",
              label: "Erläuterungen",
              value: norm.euAnnouncementExplanations,
            },
          ],
        },
        {
          heading: "Sonstige amtliche Fundstelle",
          fields: [
            {
              isCheckbox: false,
              name: "otherOfficialAnnouncement",
              label: "Sonstige amtliche Fundstelle",
              value: norm.otherOfficialAnnouncement,
            },
          ],
        },
      ],
    },

    {
      heading: "Nichtamtliche Fundstelle",
      fields: [
        {
          isCheckbox: false,
          name: "unofficialReference",
          label: "Nichtamtliche Fundstelle",
          value: norm.unofficialReference,
        },
      ],
    },
    {
      heading: "Vollzitat",
      fields: [
        {
          isCheckbox: false,
          name: "completeCitation",
          label: "Vollzitat",
          value: norm.completeCitation,
        },
      ],
    },
    {
      heading: "Stand-Angabe",
      sections: [
        {
          heading: "Stand",
          fields: [
            {
              isCheckbox: false,
              name: "statusNote",
              label: "Änderungshinweis",
              value: norm.statusNote,
            },
            {
              isCheckbox: false,
              name: "statusDescription",
              label: "Bezeichnung der Änderungsvorschrift",
              value: norm.statusDescription,
            },
            {
              isCheckbox: false,
              name: "statusDate",
              label: "Datum der Änderungsvorschrift",
              value: norm.statusDate,
            },
            {
              isCheckbox: false,
              name: "statusReference",
              label: "Fundstellen der Änderungsvorschrift",
              value: norm.statusReference,
            },
          ],
        },
        {
          heading: "Aufhebung",
          fields: [
            {
              isCheckbox: false,
              name: "repealNote",
              label: "Änderungshinweis",
              value: norm.repealNote,
            },
            {
              isCheckbox: false,
              name: "repealArticle",
              label: "Artikel der Änderungsvorschrift",
              value: norm.repealArticle,
            },
            {
              isCheckbox: false,
              name: "repealDate",
              label: "Datum der Änderungsvorschrift",
              value: norm.repealDate,
            },
            {
              isCheckbox: false,
              name: "repealReferences",
              label: "Fundstellen der Änderungsvorschrift",
              value: norm.repealReferences,
            },
          ],
        },
        {
          heading: "Neufassung",
          fields: [
            {
              isCheckbox: false,
              name: "reissueNote",
              label: "Neufassungshinweis",
              value: norm.reissueNote,
            },
            {
              isCheckbox: false,
              name: "reissueArticle",
              label: "Bezeichnung der Bekanntmachung",
              value: norm.reissueArticle,
            },
            {
              isCheckbox: false,
              name: "reissueDate",
              label: "Datum der Bekanntmachung",
              value: norm.reissueDate,
            },
            {
              isCheckbox: false,
              name: "reissueReference",
              label: "Fundstelle der Bekanntmachung",
              value: norm.reissueReference,
            },
          ],
        },
        {
          heading: "Sonstiger Hinweis",
          fields: [
            {
              isCheckbox: false,
              name: "otherStatusNote",
              label: "Sonstiger Hinweis",
              value: norm.otherStatusNote,
            },
          ],
        },
      ],
    },
    {
      heading: "Stand der dokumentarischen Bearbeitung",
      sections: [
        {
          heading: "Stand der dokumentarischen Bearbeitung",
          fields: [
            {
              isCheckbox: false,
              name: "documentStatusWorkNote",
              label: "Bearbeitungshinweis",
              value: norm.documentStatusWorkNote,
            },
            {
              isCheckbox: false,
              name: "documentStatusDescription",
              label: "Bezeichnung der Änderungsvorschrift",
              value: norm.documentStatusDescription,
            },
            {
              isCheckbox: false,
              name: "documentStatusDate",
              label: "Datum des Änderungsvorschrift",
              value: norm.documentStatusDate,
            },
            {
              isCheckbox: false,
              name: "documentStatusReference",
              label: "Fundstelle der Änderungsvorschrift",
              value: norm.documentStatusReference,
            },
            {
              isCheckbox: false,
              name: "documentStatusEntryIntoForceDate",
              label: "Datum des Inkrafttretens der Änderung",
              value: norm.documentStatusEntryIntoForceDate,
            },
            {
              isCheckbox: false,
              name: "documentStatusProof",
              label:
                "Angaben zum textlichen und/oder dokumentarischen Nachweis",
              value: norm.documentStatusProof,
            },
          ],
        },
        {
          heading: "Textnachweis",
          fields: [
            {
              isCheckbox: false,
              name: "documentTextProof",
              label: "Textnachweis",
              value: norm.documentTextProof,
            },
          ],
        },
        {
          heading: "Sonstiger Hinweis",
          fields: [
            {
              isCheckbox: false,
              name: "otherDocumentNote",
              label: "Sonstiger Hinweis",
              value: norm.otherDocumentNote,
            },
          ],
        },
      ],
    },
    {
      heading: "Räumlicher Geltungsbereich",
      fields: [
        {
          isCheckbox: false,
          name: "applicationScopeArea",
          label: "Gebiet",
          value: norm.applicationScopeArea,
        },
        {
          isCheckbox: false,
          name: "applicationScopeStartDate",
          label: "Anfangsdatum",
          value: norm.applicationScopeStartDate,
        },
        {
          isCheckbox: false,
          name: "applicationScopeEndDate",
          label: "Endedatum",
          value: norm.applicationScopeEndDate,
        },
      ],
    },
    {
      heading: "Aktivverweisung",
      fields: [
        {
          isCheckbox: false,
          name: "categorizedReference",
          label: "Aktivverweisung",
          value: norm.categorizedReference,
        },
      ],
    },
    {
      heading: "Fußnote",
      fields: [
        {
          isCheckbox: false,
          name: "otherFootnote",
          label: "Sonstige Fußnote",
          value: norm.otherFootnote,
        },
      ],
    },
    {
      heading: "Gültigkeitsregelung",
      fields: [
        {
          isCheckbox: false,
          name: "validityRule",
          label: "Gültigkeitsregelung",
          value: norm.validityRule,
        },
      ],
    },
    {
      heading: "Elektronischer Nachweis",
      fields: [
        {
          isCheckbox: false,
          name: "digitalEvidenceLink",
          label: "Verlinkung",
          value: norm.digitalEvidenceLink,
        },
        {
          isCheckbox: false,
          name: "digitalEvidenceRelatedData",
          label: "Zugehörige Dateien",
          value: norm.digitalEvidenceRelatedData,
        },
        {
          isCheckbox: false,
          name: "digitalEvidenceExternalDataNote",
          label: "Hinweis auf fremde Verlinkung oder Daten",
          value: norm.digitalEvidenceExternalDataNote,
        },
        {
          isCheckbox: false,
          name: "digitalEvidenceAppendix",
          label: "Zusatz zum Nachweis",
          value: norm.digitalEvidenceAppendix,
        },
      ],
    },
    {
      heading: "Aktenzeichen",
      fields: [
        {
          isCheckbox: false,
          name: "referenceNumber",
          label: "Aktenzeichen",
          value: norm.referenceNumber,
        },
      ],
    },
    {
      heading: "ELI",
      fields: [
        {
          isCheckbox: false,
          name: "europeanLegalIdentifier",
          label: "ELI",
          value: norm.europeanLegalIdentifier,
        },
      ],
    },
    {
      heading: "CELEX-Nummer",
      fields: [
        {
          isCheckbox: false,
          name: "celexNumber",
          label: "Celex Nummer",
          value: norm.celexNumber,
        },
      ],
    },
    {
      heading: "Altersangabe",
      fields: [
        {
          isCheckbox: false,
          name: "ageIndicationStart",
          label: "Anfang",
          value: norm.ageIndicationStart,
        },
        {
          isCheckbox: false,
          name: "ageIndicationEnd",
          label: "Ende",
          value: norm.ageIndicationEnd,
        },
      ],
    },
    {
      heading: "Definition",
      fields: [
        {
          isCheckbox: false,
          name: "definition",
          label: "Definition",
          value: norm.definition,
        },
      ],
    },
    {
      heading: "Angaben zur Volljährigkeit",
      fields: [
        {
          isCheckbox: false,
          name: "ageOfMajorityIndication",
          label: "Angaben zur Volljährigkeit",
          value: norm.ageOfMajorityIndication,
        },
      ],
    },
    {
      heading: "Text",
      fields: [
        {
          isCheckbox: false,
          name: "text",
          label: "Text",
          value: norm.text,
        },
      ],
    },
  ]
}
