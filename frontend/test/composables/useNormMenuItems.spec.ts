import { ref } from "vue"
import type { RouteLocationNormalizedLoaded } from "vue-router"
import { useNormMenuItems } from "@/composables/useNormMenuItems"

describe("useNormMenuItems", () => {
  it("adds norm GUID as route parameter to each menu item", () => {
    const guid = ref("fake-guid")
    const route = {} as unknown as RouteLocationNormalizedLoaded

    const menuItems = useNormMenuItems(guid, route)

    for (const menuItem of menuItems.value) {
      expect(menuItem.route.params).toContain({ guid: "fake-guid" })
    }
  })

  it("clones current route query to menu item", () => {
    const route = {
      query: { foo: "bar" },
    } as unknown as RouteLocationNormalizedLoaded

    const menuItems = useNormMenuItems(ref(""), route)

    for (const menuItem of menuItems.value) {
      expect(menuItem.route.query).toEqual({ foo: "bar" })
    }
  })

  it("lists all expected parent menu items", () => {
    const menuItems = useNormMenuItems(
      ref(""),
      {} as unknown as RouteLocationNormalizedLoaded
    )

    const topLebelNames = menuItems.value.map((item) => item.label)
    expect(topLebelNames).toContain("Normenkomplex")
    expect(topLebelNames).toContain("Rahmen")
    expect(topLebelNames).toContain("Bestand")
    expect(topLebelNames).toContain("Export")
  })
})
