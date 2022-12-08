import { computed } from "vue"
import type { Ref } from "vue"
import type { RouteLocationNormalizedLoaded } from "vue-router"

export function useNormMenuItems(
  normGuid: Ref<string>,
  route: RouteLocationNormalizedLoaded
) {
  const baseRoute = {
    params: { guid: normGuid.value },
    query: route.query,
  }

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
      isDisabled: true,
      children: [
        {
          label: "Allgemeine Angaben",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#generalDataFields",
          },
        },
        {
          label: "Dokumenttyp",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#documentTypeFields",
          },
        },
        {
          label: "Normgeber",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#normProviderFields",
          },
        },
        {
          label: "Mitwirkende Organe",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#participatingInstitutionsFields",
          },
        },
        {
          label: "Federführung",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#leadFields",
          },
        },
        {
          label: "Sachgebiet",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#subjectAreaFields",
          },
        },
        {
          label: "Überschriften und Abkürzungen",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#headingsAndAbbreviations",
          },
        },
        {
          label: "Inkrafttreten",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#entryIntoForceFields",
          },
        },
        {
          label: "Außerkrafttreten",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#expirationFields",
          },
        },
        {
          label: "Verkündungsdatum",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#announcementDateFields",
          },
        },
        {
          label: "Zitierdatum",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#citationDateFields",
          },
        },
        {
          label: "Amtliche Fundstelle",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#officialAnnouncementFields",
          },
        },
        {
          label: "Nichtamtliche Fundstelle",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#unofficialReferenceFields",
          },
        },
        {
          label: "Vollzitat",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#completeCitationFields",
          },
        },
        {
          label: "Stand-Angabe",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#statusIndicationFields",
          },
        },
        {
          label: "Stand der dokumentarischen Bearbeitung",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#documentProcessingStatusFields",
          },
        },
        {
          label: "Räumlicher Geltungsbereich",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#applicationScopeFields",
          },
        },
        {
          label: "Aktivverweisung",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#categorizedReferenceFields",
          },
        },
        {
          label: "Fußnote",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#otherFootnoteFields",
          },
        },
        {
          label: "Gültigkeitsregelung",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#validityRuleFields",
          },
        },
        {
          label: "Elektronischer Nachweis",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#digitalEvidenceFields",
          },
        },
        {
          label: "Aktenzeichen",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#referenceNumberFields",
          },
        },
        {
          label: "ELI",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#europeanLegalIdentifierFields",
          },
        },
        {
          label: "CELEX-Nummer",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#celexNumberFields",
          },
        },
        {
          label: "Altersangabe",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#ageIndicationFields",
          },
        },
        {
          label: "Definition",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#definitionFields",
          },
        },
        {
          label: "Angaben zur Volljährigkeit",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#ageOfMajorityIndicationFields",
          },
        },
        {
          label: "Text",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#textFields",
          },
        },
      ],
    },
    {
      label: "Bestand",
      route: {
        ...baseRoute,
        name: "norms",
      },
      isDisabled: true,
    },
    {
      label: "Abgabe",
      route: {
        ...baseRoute,
        name: "norms",
      },
      isDisabled: true,
    },
  ])
}
