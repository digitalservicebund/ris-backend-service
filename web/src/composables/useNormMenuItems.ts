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
            hash: "#coreData",
          },
        },
        {
          label: "Dokumenttyp",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#documentType",
          },
        },
        {
          label: "Überschriften und Abkürzungen",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#headings_abbreviations",
          },
        },
        {
          label: "Normgeber",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#normOriginator",
          },
        },
        {
          label: "Federführung",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#leadManagement",
          },
        },
        {
          label: "Sachgebiet",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#subjectArea",
          },
        },
        {
          label: "Mitwirkende Organe",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#participatingInstitutions",
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
