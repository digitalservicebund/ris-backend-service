import { LocationQuery } from "vue-router"
import MenuItem from "@/domain/menuItem"

export function usePeriodicalEvaluationMenuItems(
  editionId: string | undefined,
  routeQuery: LocationQuery, // Replace with the appropriate type for route query
): MenuItem[] {
  const baseRoute = {
    params: { editionId },
    query: routeQuery,
  }

  return [
    {
      label: "Ausgabe",
      route: {
        name: "caselaw-periodical-evaluation-uuid-edition",
        ...baseRoute,
      },
    },
    {
      label: "Fundstellen",
      route: {
        ...baseRoute,
        name: "caselaw-periodical-evaluation-uuid-references",
      },
    },
  ]
}
