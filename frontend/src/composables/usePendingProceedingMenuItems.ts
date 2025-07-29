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
        name: "caselaw-pending-proceeding-documentNumber-categories",
        ...baseRoute,
      },
      children: [
        ...(isInternalUser.value
          ? [
              {
                label: "Formaldaten",
                route: {
                  ...baseRoute,
                  name: "caselaw-pending-proceeding-documentNumber-categories",
                  hash: "#coreData",
                },
              },
            ]
          : []),
        {
          label: "Rechtszug",
          route: {
            ...baseRoute,
            name: "caselaw-pending-proceeding-documentNumber-categories",
            hash: "#proceedingDecisions",
          },
        },
        {
          label: "Inhaltliche Erschließung",
          route: {
            ...baseRoute,
            name: "caselaw-pending-proceeding-documentNumber-categories",
            hash: "#contentRelatedIndexing",
          },
        },
        {
          label: "Kurztexte",
          route: {
            ...baseRoute,
            name: "caselaw-pending-proceeding-documentNumber-categories",
            hash: "#texts",
          },
        },
      ],
    },
    ...(isInternalUser.value
      ? [
          {
            label: "Fundstellen",
            route: {
              ...baseRoute,
              name: "caselaw-pending-proceeding-documentNumber-references",
            },
          },
        ]
      : []),
    ...(isInternalUser.value
      ? [
          {
            label: "Verwaltungsdaten",
            route: {
              ...baseRoute,
              name: "caselaw-pending-proceeding-documentNumber-managementdata",
            },
          },
        ]
      : []),
  ])
}
