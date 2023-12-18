import { ref } from "vue"
import type { RouteLocationNormalizedLoaded } from "vue-router"
import { useNormMenuItems } from "@/composables/useNormMenuItems"

describe("useNormMenuItems", () => {
  it("adds norm GUID as route parameter to each menu item", () => {
    const guid = ref("fake-guid")
    const route = {} as unknown as RouteLocationNormalizedLoaded

    const menuItems = useNormMenuItems(guid, route)

    for (const menuItem of menuItems.value) {
      expect(menuItem.route.params).toMatchObject({ normGuid: "fake-guid" })
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
      {} as unknown as RouteLocationNormalizedLoaded,
    )

    const topLebelNames = menuItems.value.map((item) => item.label)
    expect(topLebelNames).toContain("Rahmen")
    expect(topLebelNames).toContain("Bestand")
    expect(topLebelNames).toContain("Export")
  })

  it("disables the export item per default", () => {
    const menuItems = useNormMenuItems(
      ref(""),
      {} as unknown as RouteLocationNormalizedLoaded,
    )

    const exportMenuItem = menuItems.value.find(
      (item) => item.label == "Export",
    )

    expect(exportMenuItem).toBeDefined()
    expect(exportMenuItem?.isDisabled).toBeTruthy()
  })

  it("enables the export item based on optional input reference", async () => {
    const exportIsEnabled = ref(true)
    const menuItems = useNormMenuItems(
      ref(""),
      {} as unknown as RouteLocationNormalizedLoaded,
      undefined,
      exportIsEnabled,
    )

    const exportMenuItem = menuItems.value.find(
      (item) => item.label == "Export",
    )

    expect(exportMenuItem).toBeDefined()
    expect(exportMenuItem?.isDisabled).toBeFalsy()
  })

  it("lists expected children menu item for the bestand", () => {
    const menuItems = useNormMenuItems(
      ref("fake-guid"),
      {} as unknown as RouteLocationNormalizedLoaded,
      ref({
        title: "testMarker",
        to: "#",
      }),
    )
    const bestandMenuItem = menuItems.value.find(
      (item) => item.label === "Bestand",
    )
    expect(bestandMenuItem).toBeDefined()

    if (bestandMenuItem?.children) {
      const childMarker = bestandMenuItem.children.map((child) => child.label)
      expect(childMarker).toContain("testMarker")
    }
  })
})
