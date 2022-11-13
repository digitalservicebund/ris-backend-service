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
      name: "NORMKOMPLEX",
      route: {
        ...baseRoute,
        name: "norms-norm-:normGuid",
      },
    },
    {
      name: "RAHMEN",
      route: {
        ...baseRoute,
        name: "norms-norm-:normGuid",
      },
      isDisabled: true,
      children: [
        {
          name: "Normgeber",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-edit",
            hash: "#normgeber",
          },
        },
        {
          name: "Mitwirkende Organe",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-edit",
            hash: "#mitwirkendeOrgane",
          },
        },
        {
          name: "Sachgebiet",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-edit",
            hash: "#sachgebiet",
          },
        },
      ],
    },
    {
      name: "Bestand",
      route: {
        ...baseRoute,
        name: "norms",
      },
      isDisabled: true,
    },
    {
      name: "Abgabe",
      route: {
        ...baseRoute,
        name: "norms",
      },
      isDisabled: true,
    },
  ])
}
