import { ref } from "vue"
import type { RouteLocationNormalizedLoaded } from "vue-router"
import { useCaseLawMenuItems } from "@/composables/useCaseLawMenuItems"

describe("useCaseLawMenuItems", () => {
  it("adds document identifier as route parameter to each menu item", () => {
    const documentNumber = ref("fake-number")
    const route = {} as unknown as RouteLocationNormalizedLoaded

    const menuItems = useCaseLawMenuItems(documentNumber, route)

    for (const menuItem of menuItems.value) {
      expect(menuItem.route.params).toContain({ id: "fake-number" })
    }
  })

  it("clones current route query to menu item", () => {
    const route = {
      query: { foo: "bar" },
    } as unknown as RouteLocationNormalizedLoaded

    const menuItems = useCaseLawMenuItems(ref(""), route)

    for (const menuItem of menuItems.value) {
      expect(menuItem.route.query).toEqual({ foo: "bar" })
    }
  })

  it("lists all expected menu items", () => {
    const menuItems = useCaseLawMenuItems(
      ref(""),
      {} as unknown as RouteLocationNormalizedLoaded
    )

    const topLebelNames = menuItems.value.map((item) => item.label)
    expect(topLebelNames).toContain("Rubriken")
    expect(topLebelNames).toContain("Dokumente")
    expect(topLebelNames).toContain("Bearbeitungsstand")
    expect(topLebelNames).toContain("Veröffentlichen")
  })
})
