import { computed, Ref } from "vue"
import { LocationQuery } from "vue-router"
import { useInternalUser } from "@/composables/useInternalUser"
import MenuItem from "@/domain/menuItem"

export function useCaseLawMenuItems(
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
    ...(isInternalUser.value
      ? [
          {
            label: "Dokumente",
            route: {
              ...baseRoute,
              name: "caselaw-documentUnit-documentNumber-attachments",
            },
          },
        ]
      : []),
    ...(isInternalUser.value
      ? [
          {
            label: "Fundstellen",
            route: {
              ...baseRoute,
              name: "caselaw-documentUnit-documentNumber-references",
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
              name: "caselaw-documentUnit-documentNumber-managementdata",
            },
          },
        ]
      : []),
    {
      label: "Übergabe an jDV",
      route: {
        ...baseRoute,
        name: "caselaw-documentUnit-documentNumber-handover",
      },
    },
  ])
}
