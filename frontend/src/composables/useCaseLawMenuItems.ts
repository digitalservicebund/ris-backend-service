import { computed } from "vue"
import type { Ref } from "vue"
import type { RouteLocationNormalizedLoaded } from "vue-router"

export function useCaseLawMenuItems(
  documentNumber: Ref<string | undefined>,
  route: RouteLocationNormalizedLoaded
) {
  const baseRoute = {
    params: { id: documentNumber.value },
    query: route.query,
  }

  return computed(() => [
    {
      label: "Rubriken",
      route: {
        ...baseRoute,
        name: "caselaw-documentUnit-:documentNumber-categories",
      },
      children: [
        {
          label: "Stammdaten",
          route: {
            ...baseRoute,
            name: "caselaw-documentUnit-:documentNumber-categories",
            hash: "#coreData",
          },
        },
        {
          label: "Rechtszug",
          route: {
            ...baseRoute,
            name: "caselaw-documentUnit-:documentNumber-categories",
            hash: "#previousDecisions",
          },
        },
        {
          label: "Kurz- & Langtexte",
          route: {
            ...baseRoute,
            name: "caselaw-documentUnit-:documentNumber-categories",
            hash: "#texts",
          },
        },
        {
          label: "Inhaltliche Erschließung",
          route: {
            ...baseRoute,
            name: "caselaw-documentUnit-:documentNumber-categories",
            hash: "#contentRelatedIndexing",
          },
        },
      ],
    },
    {
      label: "Dokumente",
      route: {
        ...baseRoute,
        name: "caselaw-documentUnit-:documentNumber-files",
      },
    },
    {
      label: "Veröffentlichen",
      route: {
        ...baseRoute,
        name: "caselaw-documentUnit-:documentNumber-publication",
      },
    },
  ])
}
