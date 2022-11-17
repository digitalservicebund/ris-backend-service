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
          label: "Normgeber",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#headlines",
          },
        },
        {
          label: "Mitwirkende Organe",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#organs",
          },
        },
        {
          label: "Sachgebiet",
          route: {
            ...baseRoute,
            name: "norms-norm-:normGuid-frame",
            hash: "#subject",
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
