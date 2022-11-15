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
      name: "Normkomplex",
      route: {
        ...baseRoute,
        name: "norms-norm-:normGuid",
      },
    },
    {
      name: "Rahmen",
      route: {
        ...baseRoute,
        name: "norms-norm-:normGuid-frame",
      },
      isDisabled: true,
      children: [
        {
          name: "Normgeber",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#headlines",
          },
        },
        {
          name: "Mitwirkende Organe",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
          },
        },
        {
          name: "Sachgebiet",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
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
