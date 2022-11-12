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
      name: "Rubriken",
      route: {
        ...baseRoute,
        name: "caselaw-documentUnit-:documentNumber-categories",
      },
      children: [
        {
          name: "Stammdaten",
          route: {
            ...baseRoute,
            name: "caselaw-documentUnit-:documentNumber-categories",
            hash: "#coreData",
          },
        },
        {
          name: "Rechtszug",
          route: {
            ...baseRoute,
            name: "caselaw-documentUnit-:documentNumber-categories",
            hash: "#previousDecisions",
          },
        },
        {
          name: "Kurz- & Langtexte",
          route: {
            ...baseRoute,
            name: "caselaw-documentUnit-:documentNumber-categories",
            hash: "#texts",
          },
        },
      ],
    },
    {
      name: "Dokumente",
      route: {
        ...baseRoute,
        name: "caselaw-documentUnit-:documentNumber-files",
      },
    },
    {
      name: "Bearbeitungsstand",
      route: {
        ...baseRoute,
        name: "caselaw",
      },
      isDisabled: true,
    },
    {
      name: "Veröffentlichen",
      route: {
        ...baseRoute,
        name: "caselaw-documentUnit-:documentNumber-publication",
      },
    },
  ])
}
