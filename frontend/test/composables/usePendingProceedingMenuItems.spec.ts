import { vi } from "vitest"
import { ref } from "vue"
import { usePendingProceedingMenuItems } from "@/composables/usePendingProceedingMenuItems"

const isInternalUser = ref(false)
vi.mock("@/composables/useInternalUser", () => {
  return {
    useInternalUser: () => isInternalUser,
  }
})

describe("usePendingProceedingMenuItems", () => {
  it("adds document identifier as route parameter to each menu item", () => {
    isInternalUser.value = true
    const documentNumber = "fake-number"

    const menuItems = usePendingProceedingMenuItems(documentNumber, {})

    for (const menuItem of menuItems.value) {
      expect(menuItem.route.params).toMatchObject({
        documentNumber: "fake-number",
      })
    }
  })

  it("clones current route query to menu item", () => {
    isInternalUser.value = true
    const route = {
      query: { foo: "bar" },
    }

    const menuItems = usePendingProceedingMenuItems("", route.query)

    for (const menuItem of menuItems.value) {
      expect(menuItem.route.query).toEqual({ foo: "bar" })
    }
  })

  it("lists all expected menu items", () => {
    isInternalUser.value = true
    const menuItems = usePendingProceedingMenuItems("", {})

    const topLabelNames = menuItems.value.map((item) => item.label)
    expect(topLabelNames).toContain("Rubriken")
  })

  it("lists all expected menu items for external user", () => {
    isInternalUser.value = false
    const menuItems = usePendingProceedingMenuItems("", {})

    const topLabelNames = menuItems.value.map((item) => item.label)
    expect(topLabelNames).toContain("Rubriken")

    const categoriesSubMenu = menuItems.value.find(
      (menu) => menu.label === "Rubriken",
    )?.children
    expect(categoriesSubMenu?.map((menu) => menu.label)).toEqual([
      "Rechtszug",
      "Inhaltliche Erschließung",
      "Kurztexte",
    ])
  })
})
