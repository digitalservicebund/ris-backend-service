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
        ...baseRoute,
        name: "caselaw-documentUnit-documentNumber-categories",
      },
      children: [
        {
          label: "Stammdaten",
          route: { ...baseRoute, hash: "#coreData" },
        },
        {
          label: "Rechtszug",
          route: { ...baseRoute, hash: "#proceedingDecisions" },
        },
        {
          label: "Inhaltliche Erschließung",
          route: { ...baseRoute, hash: "#contentRelatedIndexing" },
        },
        {
          label: "Kurz- & Langtexte",
          route: { ...baseRoute, hash: "#texts" },
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
