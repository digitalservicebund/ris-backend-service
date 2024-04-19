import { useCaseLawMenuItems } from "@/composables/useCaseLawMenuItems"

describe("useCaseLawMenuItems", () => {
  it("adds document identifier as route parameter to each menu item", () => {
    const documentNumber = "fake-number"

    const menuItems = useCaseLawMenuItems(documentNumber, {})

    for (const menuItem of menuItems) {
      expect(menuItem.route.params).toMatchObject({
        documentNumber: "fake-number",
      })
    }
  })

  it("clones current route query to menu item", () => {
    const route = {
      query: { foo: "bar" },
    }

    const menuItems = useCaseLawMenuItems("", route.query)

    for (const menuItem of menuItems) {
      expect(menuItem.route.query).toEqual({ foo: "bar" })
    }
  })

  it("lists all expected menu items", () => {
    const menuItems = useCaseLawMenuItems("", {})

    const topLabelNames = menuItems.map((item) => item.label)
    expect(topLabelNames).toContain("Rubriken")
    expect(topLabelNames).toContain("Dokumente")
    expect(topLabelNames).toContain("Veröffentlichen")
  })
})
