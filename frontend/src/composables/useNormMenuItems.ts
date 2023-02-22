import { computed } from "vue"
import type { Ref } from "vue"
import type { RouteLocationNormalizedLoaded } from "vue-router"

export function useNormMenuItems(
  normGuid: Ref<string>,
  route: RouteLocationNormalizedLoaded,
  exportIsEnabled?: Ref<boolean>
) {
  const baseRoute = {
    params: { guid: normGuid.value },
    query: route.query,
  }

  const getChildItem = (label: string, id: string) => ({
    label: label,
    route: {
      ...baseRoute,
      name: "norms-norm-:normGuid-frame",
      hash: `#${id}`,
    },
  })

  return computed(() => [
    {
      label: "Normenkomplex",
      route: {
        ...baseRoute,
        name: "norms-norm-:normGuid",
      },
    },
    {
      label: "Rahmen",
      route: {
        ...baseRoute,
        name: "norms-norm-:normGuid-frame",
      },
      children: [
        getChildItem("Allgemeine Angaben", "generalDataFields"),
        getChildItem("Dokumenttyp", "documentTypeFields"),
        getChildItem("Normgeber", "normProviderFields"),
        getChildItem("Mitwirkende Organe", "participatingInstitutionsFields"),
        getChildItem("Federführung", "leadFields"),
        getChildItem("Sachgebiet", "subjectAreaFields"),
        getChildItem(
          "Überschriften und Abkürzungen",
          "headingsAndAbbreviations"
        ),
        getChildItem("Inkrafttreten", "entryIntoForceFields"),
        getChildItem("Außerkrafttreten", "expirationFields"),
        getChildItem("Verkündungsdatum", "announcementDateFields"),
        getChildItem("Zitierdatum", "citationDateFields"),
        getChildItem("Amtliche Fundstelle", "officialAnnouncementFields"),
        getChildItem("Nichtamtliche Fundstelle", "unofficialReferenceFields"),
        getChildItem("Vollzitat", "completeCitationFields"),
        getChildItem("Stand-Angabe", "statusIndicationFields"),
        getChildItem(
          "Stand der dokumentarischen Bearbeitung",
          "documentProcessingStatusFields"
        ),
        getChildItem("Aktivverweisung", "categorizedReferenceFields"),
        getChildItem("Fußnote", "otherFootnoteFields"),
        getChildItem("Gültigkeitsregelung", "validityRuleFields"),
        getChildItem("Elektronischer Nachweis", "digitalEvidenceFields"),
        getChildItem("Aktenzeichen", "referenceNumberFields"),
        getChildItem("ELI", "eliFields"),
        getChildItem("CELEX-Nummer", "celexNumberFields"),
        getChildItem("Altersangabe", "ageIndicationFields"),
        getChildItem("Definition", "definitionFields"),
        getChildItem(
          "Angaben zur Volljährigkeit",
          "ageOfMajorityIndicationFields"
        ),
        getChildItem("Text", "textFields"),
      ],
    },
    {
      label: "Bestand",
      route: {
        ...baseRoute,
        name: "norms",
      },
    },
    {
      label: "Export",
      route: {
        ...baseRoute,
        name: "norms-norm-:normGuid-export",
      },
      isDisabled: !(exportIsEnabled?.value ?? false),
    },
  ])
}
