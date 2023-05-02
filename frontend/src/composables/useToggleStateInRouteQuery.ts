import { computed, ref, watch } from "vue"
import { RouteLocationNormalizedLoaded, Router } from "vue-router"

export function useToggleStateInRouteQuery(
  queryParameterName: string,
  route: RouteLocationNormalizedLoaded,
  routerCallback: Router["replace"],
  defaultState = true
) {
  const toggleState = ref(defaultState)
  const queryParameter = computed(() => route.query[queryParameterName])

  watch(
    queryParameter,
    (parameter) => {
      if (parameter) {
        toggleState.value = parameter === "true"
      }
    },
    { immediate: true }
  )

  watch(toggleState, () => {
    routerCallback({
      ...route,
      query: {
        ...route.query,
        [queryParameterName]: toggleState.value.toString(),
      },
    })
  })

  return toggleState
}
