import { expect, test } from "@playwright/test"
import normCleanCars from "./testdata/norm_clean_cars.json"

type MyFixtures = {
  normToImport: object
  createdGuid: string
}

export const testWithImportedNorm = test.extend<MyFixtures>({
  normToImport: normCleanCars,
  createdGuid: async ({ normToImport, request }, use) => {
    const backendHost = process.env.E2E_BASE_URL ?? "http://127.0.0.1"
    const response = await request.post(`${backendHost}/api/v1/norms`, {
      data: normToImport,
    })
    const body = await response.text()
    console.log(body)
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
          type: "text",
          name: "officialLongTitle",
          label: "Amtliche Langüberschrift",
          value: norm.officialLongTitle,
        },
        {
          type: "text",
          name: "risAbbreviation",
          label: "Juris-Abkürzung",
          value: norm.risAbbreviation,
        },
        {
          type: "text",
          name: "risAbbreviationInternationalLaw",
          label: "Juris-Abkürzung für völkerrechtliche Vereinbarungen",
          value: norm.risAbbreviationInternationalLaw,
        },
        {
          type: "text",
          name: "documentNumber",
          label: "Dokumentnummer",
          value: norm.documentNumber,
        },
        {
          type: "text",
          name: "divergentDocumentNumber",
          label: "Abweichende Dokumentnummer",
          value: norm.divergentDocumentNumber,
        },
        {
          type: "text",
          name: "documentCategory",
          label: "Dokumentart",
          value: norm.documentCategory,
        },
        {
          type: "text",
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
          type: "text",
          name: "documentTypeName",
          label: "Typbezeichnung",
          value: norm.documentTypeName,
        },
        {
          type: "text",
          name: "documentNormCategory",
          label: "Art der Norm",
          value: norm.documentNormCategory,
        },
        {
          type: "text",
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
          type: "text",
          name: "providerEntity",
          label: "Staat, Land, Stadt, Landkreis oder juristische Person",
          value: norm.providerEntity,
        },
        {
          type: "text",
          name: "providerDecidingBody",
          label: "Beschließendes Organ",
          value: norm.providerDecidingBody,
        },
        {
          type: "checkbox",
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
          type: "text",
          name: "participationType",
          label: "Art der Mitwirkung",
          value: norm.participationType,
        },
        {
          type: "text",
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
          type: "text",
          name: "leadJurisdiction",
          label: "Ressort",
          value: norm.leadJurisdiction,
        },
        {
          type: "text",
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
          type: "text",
          name: "subjectFna",
          label: "FNA-Nummer",
          value: norm.subjectFna,
        },
        {
          type: "text",
          name: "subjectPreviousFna",
          label: "Frühere FNA-Nummer",
          value: norm.subjectPreviousFna,
        },
        {
          type: "text",
          name: "subjectGesta",
          label: "GESTA-Nummer",
          value: norm.subjectGesta,
        },
        {
          type: "text",
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
          type: "text",
          name: "officialShortTitle",
          label: "Amtliche Kurzüberschrift",
          value: norm.officialShortTitle,
        },
        {
          type: "text",
          name: "officialAbbreviation",
          label: "Amtliche Buchstabenabkürzung",
          value: norm.officialAbbreviation,
        },
        {
          type: "text",
          name: "unofficialLongTitle",
          label: "Nichtamtliche Langüberschrift",
          value: norm.unofficialLongTitle,
        },
        {
          type: "text",
          name: "unofficialShortTitle",
          label: "Nichtamtliche Kurzüberschrift",
          value: norm.unofficialShortTitle,
        },
        {
          type: "text",
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
          type: "text",
          name: "entryIntoForceDate",
          label: "Datum des Inkrafttretens",
          value: norm.entryIntoForceDate,
        },
        {
          type: "dropdown",
          name: "entryIntoForceDateState",
          label: "Unbestimmtes Datum des Inkrafttretens",
          value: norm.entryIntoForceDateState,
        },
        {
          type: "text",
          name: "principleEntryIntoForceDate",
          label: "Grundsätzliches Inkrafttretedatum",
          value: norm.principleEntryIntoForceDate,
        },
        {
          type: "dropdown",
          name: "principleEntryIntoForceDateState",
          label: "Unbestimmtes grundsätzliches Inkrafttretedatum",
          value: norm.principleEntryIntoForceDateState,
        },
        {
          type: "text",
          name: "divergentEntryIntoForceDate",
          label: "Bestimmtes abweichendes Inkrafttretedatum",
          value: norm.divergentEntryIntoForceDate,
        },
        {
          type: "dropdown",
          name: "divergentEntryIntoForceDateState",
          label: "Unbestimmtes abweichendes Inkrafttretedatum",
          value: norm.divergentEntryIntoForceDateState,
        },
      ],
    },
    {
      heading: "Außerkrafttreten",
      fields: [
        {
          type: "text",
          name: "expirationDate",
          label: "Datum des Außerkrafttretens",
          value: norm.expirationDate,
        },
        {
          type: "dropdown",
          name: "expirationDateState",
          label: "Unbestimmtes Datum des Außerkrafttretens",
          value: norm.expirationDateState,
        },
        {
          type: "checkbox",
          name: "isExpirationDateTemp",
          label: "Befristet",
          value: norm.isExpirationDateTemp,
        },
        {
          type: "text",
          name: "principleExpirationDate",
          label: "Grundsätzliches Außerkrafttretedatum",
          value: norm.principleExpirationDate,
        },
        {
          type: "dropdown",
          name: "principleExpirationDateState",
          label: "Unbestimmtes grundsätzliches Außerkrafttretdatum",
          value: norm.principleExpirationDateState,
        },
        {
          type: "text",
          name: "divergentExpirationDate",
          label: "Bestimmtes abweichendes Außerkrafttretedatum",
          value: norm.divergentExpirationDate,
        },
        {
          type: "dropdown",
          name: "divergentExpirationDateState",
          label: "Unbestimmtes abweichendes Außerkrafttretdatum",
          value: norm.divergentExpirationDateState,
        },
        {
          type: "text",
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
          type: "text",
          name: "announcementDate",
          label: "Verkündungsdatum",
          value: norm.announcementDate,
        },
        {
          type: "text",
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
          type: "text",
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
              type: "text",
              name: "printAnnouncementGazette",
              label: "Verkündungsblatt",
              value: norm.printAnnouncementGazette,
            },
            {
              type: "text",
              name: "printAnnouncementYear",
              label: "Jahr",
              value: norm.printAnnouncementYear,
            },
            {
              type: "text",
              name: "printAnnouncementNumber",
              label: "Nummer",
              value: norm.printAnnouncementNumber,
            },
            {
              type: "text",
              name: "printAnnouncementPage",
              label: "Seitenzahl",
              value: norm.printAnnouncementPage,
            },
            {
              type: "text",
              name: "printAnnouncementInfo",
              label: "Zusatzangaben",
              value: norm.printAnnouncementInfo,
            },
            {
              type: "text",
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
              type: "text",
              name: "digitalAnnouncementMedium",
              label: "Verkündungsmedium",
              value: norm.digitalAnnouncementMedium,
            },
            {
              type: "text",
              name: "digitalAnnouncementDate",
              label: "Verkündungsdatum",
              value: norm.digitalAnnouncementDate,
            },
            {
              type: "text",
              name: "digitalAnnouncementEdition",
              label: "Ausgabenummer",
              value: norm.digitalAnnouncementEdition,
            },
            {
              type: "text",
              name: "digitalAnnouncementYear",
              label: "Jahr",
              value: norm.digitalAnnouncementYear,
            },
            {
              type: "text",
              name: "digitalAnnouncementPage",
              label: "Seitenzahlen",
              value: norm.digitalAnnouncementPage,
            },
            {
              type: "text",
              name: "digitalAnnouncementArea",
              label: "Bereich der Veröffentlichung",
              value: norm.digitalAnnouncementArea,
            },
            {
              type: "text",
              name: "digitalAnnouncementAreaNumber",
              label: "Nummer der Veröffentlichung im jeweiligen Bereich",
              value: norm.digitalAnnouncementAreaNumber,
            },
            {
              type: "text",
              name: "digitalAnnouncementInfo",
              label: "Zusatzangaben",
              value: norm.digitalAnnouncementInfo,
            },
            {
              type: "text",
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
              type: "text",
              name: "euAnnouncementGazette",
              label: "Amtsblatt der EU",
              value: norm.euAnnouncementGazette,
            },
            {
              type: "text",
              name: "euAnnouncementYear",
              label: "Jahresangabe",
              value: norm.euAnnouncementYear,
            },
            {
              type: "text",
              name: "euAnnouncementSeries",
              label: "Reihe",
              value: norm.euAnnouncementSeries,
            },
            {
              type: "text",
              name: "euAnnouncementNumber",
              label: "Nummer des Amtsblatts",
              value: norm.euAnnouncementNumber,
            },
            {
              type: "text",
              name: "euAnnouncementPage",
              label: "Seitenzahl",
              value: norm.euAnnouncementPage,
            },
            {
              type: "text",
              name: "euAnnouncementInfo",
              label: "Zusatzangaben",
              value: norm.euAnnouncementInfo,
            },
            {
              type: "text",
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
              type: "text",
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
          type: "text",
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
          type: "text",
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
              type: "text",
              name: "statusNote",
              label: "Änderungshinweis",
              value: norm.statusNote,
            },
            {
              type: "text",
              name: "statusDescription",
              label: "Bezeichnung der Änderungsvorschrift",
              value: norm.statusDescription,
            },
            {
              type: "text",
              name: "statusDate",
              label: "Datum der Änderungsvorschrift",
              value: norm.statusDate,
            },
            {
              type: "text",
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
              type: "text",
              name: "repealNote",
              label: "Änderungshinweis",
              value: norm.repealNote,
            },
            {
              type: "text",
              name: "repealArticle",
              label: "Artikel der Änderungsvorschrift",
              value: norm.repealArticle,
            },
            {
              type: "text",
              name: "repealDate",
              label: "Datum der Änderungsvorschrift",
              value: norm.repealDate,
            },
            {
              type: "text",
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
              type: "text",
              name: "reissueNote",
              label: "Neufassungshinweis",
              value: norm.reissueNote,
            },
            {
              type: "text",
              name: "reissueArticle",
              label: "Bezeichnung der Bekanntmachung",
              value: norm.reissueArticle,
            },
            {
              type: "text",
              name: "reissueDate",
              label: "Datum der Bekanntmachung",
              value: norm.reissueDate,
            },
            {
              type: "text",
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
              type: "text",
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
              type: "text",
              name: "documentStatusWorkNote",
              label: "Bearbeitungshinweis",
              value: norm.documentStatusWorkNote,
            },
            {
              type: "text",
              name: "documentStatusDescription",
              label: "Bezeichnung der Änderungsvorschrift",
              value: norm.documentStatusDescription,
            },
            {
              type: "text",
              name: "documentStatusDate",
              label: "Datum des Änderungsvorschrift",
              value: norm.documentStatusDate,
            },
            {
              type: "text",
              name: "documentStatusReference",
              label: "Fundstelle der Änderungsvorschrift",
              value: norm.documentStatusReference,
            },
            {
              type: "text",
              name: "documentStatusEntryIntoForceDate",
              label: "Datum des Inkrafttretens der Änderung",
              value: norm.documentStatusEntryIntoForceDate,
            },
            {
              type: "text",
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
              type: "text",
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
              type: "text",
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
          type: "text",
          name: "applicationScopeArea",
          label: "Gebiet",
          value: norm.applicationScopeArea,
        },
        {
          type: "text",
          name: "applicationScopeStartDate",
          label: "Anfangsdatum",
          value: norm.applicationScopeStartDate,
        },
        {
          type: "text",
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
          type: "text",
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
          type: "text",
          name: "otherFootnote",
          label: "Sonstige Fußnote",
          value: norm.otherFootnote,
        },
        {
          type: "text",
          name: "footnoteChange",
          label: "Änderungsfußnote",
          value: norm.footnoteChange,
        },
        {
          type: "text",
          name: "footnoteComment",
          label: "Kommentierende Fußnote",
          value: norm.footnoteComment,
        },
        {
          type: "text",
          name: "footnoteDecision",
          label: "BVerfG-Entscheidung",
          value: norm.footnoteDecision,
        },
        {
          type: "text",
          name: "footnoteStateLaw",
          label: "Landesrecht",
          value: norm.footnoteStateLaw,
        },
        {
          type: "text",
          name: "footnoteEuLaw",
          label: "EU/EG-Recht",
          value: norm.footnoteEuLaw,
        },
      ],
    },
    {
      heading: "Gültigkeitsregelung",
      fields: [
        {
          type: "text",
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
          type: "text",
          name: "digitalEvidenceLink",
          label: "Verlinkung",
          value: norm.digitalEvidenceLink,
        },
        {
          type: "text",
          name: "digitalEvidenceRelatedData",
          label: "Zugehörige Dateien",
          value: norm.digitalEvidenceRelatedData,
        },
        {
          type: "text",
          name: "digitalEvidenceExternalDataNote",
          label: "Hinweis auf fremde Verlinkung oder Daten",
          value: norm.digitalEvidenceExternalDataNote,
        },
        {
          type: "text",
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
          type: "text",
          name: "referenceNumber",
          label: "Aktenzeichen",
          value: norm.referenceNumber,
        },
      ],
    },
    {
      heading: "CELEX-Nummer",
      fields: [
        {
          type: "text",
          name: "celexNumber",
          label: "CELEX-Nummer",
          value: norm.celexNumber,
        },
      ],
    },
    {
      heading: "Altersangabe",
      fields: [
        {
          type: "text",
          name: "ageIndicationStart",
          label: "Anfang",
          value: norm.ageIndicationStart,
        },
        {
          type: "text",
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
          type: "text",
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
          type: "text",
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
          type: "text",
          name: "text",
          label: "Text",
          value: norm.text,
        },
      ],
    },
  ]
}
