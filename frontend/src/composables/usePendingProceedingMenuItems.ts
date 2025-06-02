import { computed, Ref } from "vue"
import { LocationQuery } from "vue-router"
import { useInternalUser } from "@/composables/useInternalUser"
import MenuItem from "@/domain/menuItem"

export function usePendingProceedingMenuItems(
  documentNumber: string | undefined,
  routeQuery: LocationQuery,
): Ref<MenuItem[]> {
  const baseRoute = {
    params: { documentNumber },
    query: routeQuery,
  }

  const isInternalUser = useInternalUser()

  return computed(() => [
    {
      label: "Rubriken",
      route: {
        name: "caselaw-documentUnit-documentNumber-categories",
        ...baseRoute,
      },
      children: [
        ...(isInternalUser.value
          ? [
              {
                label: "Stammdaten",
                route: {
                  ...baseRoute,
                  name: "caselaw-documentUnit-documentNumber-categories",
                  hash: "#coreData",
                },
              },
            ]
          : []),
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
  ])
}
