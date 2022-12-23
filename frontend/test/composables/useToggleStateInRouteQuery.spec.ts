import { nextTick, reactive } from "vue"
import type { RouteLocationNormalizedLoaded } from "vue-router"
import { useToggleStateInRouteQuery } from "@/composables/useToggleStateInRouteQuery"

describe("useToggleStateInRouteQuery", () => {
  it("sets toggle state to initial state if query parameter does not exist", () => {
    const route = {
      query: { otherParameter: "false" },
    } as unknown as RouteLocationNormalizedLoaded

    const toggleState = useToggleStateInRouteQuery(
      "parameter",
      route,
      vi.fn(),
      true
    )

    expect(toggleState.value).toBe(true)
  })

  it("sets toggle state to query parameter if defined", () => {
    const route = {
      query: { parameter: "true" },
    } as unknown as RouteLocationNormalizedLoaded

    const toggleState = useToggleStateInRouteQuery(
      "parameter",
      route,
      vi.fn(),
      false
    )

    expect(toggleState.value).toBe(true)
  })

  it("calls router callback with same route and change query parameter", async () => {
    const route = {
      name: "foo",
      query: { otherParameter: "false" },
    } as unknown as RouteLocationNormalizedLoaded
    const routerCallback = vi.fn()

    const toggleState = useToggleStateInRouteQuery(
      "parameter",
      route,
      routerCallback,
      false
    )
    toggleState.value = true
    await nextTick()

    expect(routerCallback).toHaveBeenCalledOnce()
    expect(routerCallback).toHaveBeenLastCalledWith({
      name: "foo",
      query: { otherParameter: "false", parameter: "true" },
    })
  })

  it("updates the toggle state if the query parameter changes", async () => {
    const route = reactive({
      name: "foo",
      query: { parameter: "false" },
    } as unknown as RouteLocationNormalizedLoaded)

    const toggleState = useToggleStateInRouteQuery("parameter", route, vi.fn())

    expect(toggleState.value).toBe(false)

    route.query.parameter = "true"
    await nextTick()

    expect(toggleState.value).toBe(true)
  })
})
