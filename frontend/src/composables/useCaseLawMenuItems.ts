import { LocationQuery } from "vue-router"
import MenuItem from "@/domain/menuItem"

export function useCaseLawMenuItems(
  documentNumber: string | undefined,
  routeQuery: LocationQuery, // Replace with the appropriate type for route query
): MenuItem[] {
  const baseRoute = {
    params: { documentNumber },
    query: routeQuery,
  }

  return [
    {
      label: "Rubriken",
      route: {
        name: "caselaw-documentUnit-documentNumber-categories",
        ...baseRoute,
      },
      children: [
        {
          label: "Stammdaten",

          route: {
            ...baseRoute,
            name: "caselaw-documentUnit-documentNumber-categories",
            hash: "#coreData",
          },
        },
        {
          label: "Rechtszug",
          route: {
            ...baseRoute,
            name: "caselaw-documentUnit-documentNumber-categories",
            hash: "#proceedingDecisions",
          },
        },
        {
          label: "Inhaltliche Erschließung",
          route: {
            ...baseRoute,
            name: "caselaw-documentUnit-documentNumber-categories",
            hash: "#contentRelatedIndexing",
          },
        },
        {
          label: "Kurz- & Langtexte",
          route: {
            ...baseRoute,
            name: "caselaw-documentUnit-documentNumber-categories",
            hash: "#texts",
          },
        },
      ],
    },
    {
      label: "Dokumente",
      route: {
        ...baseRoute,
        name: "caselaw-documentUnit-documentNumber-files",
      },
    },
    {
      label: "Veröffentlichen",
      route: {
        ...baseRoute,
        name: "caselaw-documentUnit-documentNumber-publication",
      },
    },
  ]
}
