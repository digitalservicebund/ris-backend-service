import { computed } from "vue"
import type { Ref } from "vue"
import type { RouteLocationNormalizedLoaded } from "vue-router"

export function useNormMenuItems(
  normGuid: Ref<string>,
  route: RouteLocationNormalizedLoaded,
  exportIsEnabled?: Ref<boolean>,
) {
  const baseRoute = {
    params: { normGuid: normGuid.value },
    query: route.query,
  }

  const getChildItem = (label: string, id: string) => ({
    label: label,
    route: {
      ...baseRoute,
      name: "norms-norm-normGuid-frame",
      hash: `#${id}`,
    },
  })

  return computed(() => [
    {
      label: "Normenkomplex",
      route: {
        ...baseRoute,
        name: "norms-norm-normGuid",
      },
    },
    {
      label: "Rahmen",
      route: {
        ...baseRoute,
        name: "norms-norm-normGuid-frame",
      },
      children: [
        getChildItem("Allgemeine Angaben", "OFFICIAL_LONG_TITLE"),
        getChildItem("Dokumenttyp", "documentTypes"),
        getChildItem("Normgeber", "normProviders"),
        getChildItem("Mitwirkende Organe", "participatingInstitutions"),
        getChildItem("Federführung", "leads"),
        getChildItem("Sachgebiet", "subjectAreas"),
        getChildItem(
          "Überschriften und Abkürzungen",
          "norm/frame/officialShortTitle",
        ),
        getChildItem("Inkrafttreten", "entryIntoForces"),
        getChildItem("Außerkrafttreten", "expirations"),
        getChildItem("Verkündungsdatum", "announcementDate"),
        getChildItem("Zitierdatum", "citationDates"),
        getChildItem("Amtliche Fundstelle", "officialReferences"),
        getChildItem(
          "Nichtamtliche Fundstelle",
          "norm/frame/unofficialReferences",
        ),
        getChildItem("Vollzitat", "norm/frame/completeCitation"),
        getChildItem("Stand-Angabe", "statusIndication"),
        getChildItem(
          "Stand der dokumentarischen Bearbeitung",
          "documentStatus",
        ),
        getChildItem("Aktivverweisung", "categorizedReferences"),
        getChildItem("Fußnoten", "footnotes"),
        getChildItem("Gültigkeitsregelung", "norm/frame/validityRules"),
        getChildItem("Elektronischer Nachweis", "digitalEvidence"),
        getChildItem("Aktenzeichen", "norm/frame/referenceNumbers"),
        getChildItem("ELI", "eli"),
        getChildItem("CELEX-Nummer", "norm/frame/celexNumber"),
        getChildItem("Altersangabe", "ageIndications"),
        getChildItem("Definition", "norm/frame/definitions"),
        getChildItem(
          "Angaben zur Volljährigkeit",
          "norm/frame/ageOfMajorityIndications",
        ),
        getChildItem("Text", "norm/frame/text"),
      ],
    },
    {
      label: "Bestand",
      route: {
        ...baseRoute,
        name: "norms-norm-normGuid-frame",
      },
    },
    {
      label: "Export",
      route: {
        ...baseRoute,
        name: "norms-norm-normGuid-export",
      },
      isDisabled: !(exportIsEnabled?.value ?? false),
    },
  ])
}
