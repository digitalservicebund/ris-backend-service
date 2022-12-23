import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import type { Router, RouteRecordRaw, RouteLocationRaw } from "vue-router"
import NavbarSide from "@/components/NavbarSide.vue"
import { generateString } from "~/test-helper/dataGenerators"

describe("NavbarSide", () => {
  it("displays the go back label with related route", async () => {
    await renderComponent({
      goBackLabel: "Zurück",
      goBackRoute: "/origin-route",
    })

    const goBackItem = screen.getByLabelText("Zurück")

    expect(goBackItem).toBeVisible()
    expect(goBackItem?.getAttribute("href")).toBe("/origin-route")
  })

  it("renders sidenav with multiple items and correct routes", async () => {
    const menuItems = [
      { label: "first item", route: "/first-route" },
      { label: "second item", route: "/second-route" },
    ]

    await renderComponent({ menuItems })
    const itemList = screen.getAllByLabelText("Menü Eintrag")
    const firstItem = itemList[0] as HTMLElement
    const secondItem = itemList[1] as HTMLElement

    expect(itemList.length).toBe(2)
    expect(firstItem).toBeVisible()
    expect(secondItem).toBeVisible()

    expect(firstItem).toHaveAttribute("href", "/first-route")
    expect(secondItem).toHaveAttribute("href", "/second-route")
  })

  it("allows to render level one item with level two items as children", async () => {
    const menuItems = [
      {
        label: "level one",
        route: "/",
        children: [
          { label: "first level two", route: "/first-level-two" },
          { label: "second level two", route: "/second-level-two" },
        ],
      },
    ]

    await renderComponent({ menuItems })
    const subItemList = screen.getAllByLabelText("Submenü Eintrag")
    const firstSubItem = subItemList[0] as HTMLElement
    const secondSubItem = subItemList[1] as HTMLElement

    expect(subItemList.length).toBe(2)
    expect(firstSubItem).toBeVisible()
    expect(secondSubItem).toBeVisible()

    expect(firstSubItem).toHaveAttribute("href", "/first-level-two")
    expect(secondSubItem).toHaveAttribute("href", "/second-level-two")
  })

  it("allows to disable a menu item", async () => {
    const menuItems = [
      { label: "disabled item", route: "/route", isDisabled: true },
    ]

    await renderComponent({ menuItems })
    const disabledItem = screen.queryByText("disabled item")

    expect(disabledItem?.getAttribute("disabled")).toBeDefined()
  })

  describe("highlighting of the currently active menu item", () => {
    it("applies special class to menu item which matches current route", async () => {
      const menuItems = [
        { label: "active item", route: { path: "/matching" } },
        { label: "passive item", route: { path: "/not-matching" } },
      ]
      await renderComponent({
        menuItems,
        activeRoute: { path: "/matching" },
      })

      expect(screen.getByText("active item")).toHaveClass("bg-blue-200")
      expect(screen.getByText("passive item")).not.toHaveClass("bg-blue-200")
    })

    it("routes match also by name", async () => {
      const menuItems = [
        { label: "active item", route: { name: "active" } },
        { label: "passive item", route: { name: "passive" } },
      ]
      await renderComponent({
        menuItems,
        activeRoute: { name: "active" },
      })

      expect(screen.getByText("active item")).toHaveClass("bg-blue-200")
      expect(screen.getByText("passive item")).not.toHaveClass("bg-blue-200")
    })

    it("routes match includes hash if given", async () => {
      const menuItems = [
        { label: "active item", route: { path: "/foo", hash: "#matching" } },
        { label: "passive item", route: { path: "/foo", hash: "#no-match" } },
      ]
      await renderComponent({
        menuItems,
        activeRoute: { path: "/foo", hash: "#matching" },
      })

      expect(screen.getByText("active item")).toHaveClass("bg-blue-200")
      expect(screen.getByText("passive item")).not.toHaveClass("bg-blue-200")
    })

    it("ignores queries of any to match route", async () => {
      const menuItems = [
        {
          label: "active item",
          route: { name: "foo", query: { key: "value" } },
        },
      ]
      await renderComponent({
        menuItems,
        activeRoute: { name: "foo", query: { key: "other-value" } },
      })

      expect(screen.getByText("active item")).toHaveClass("bg-blue-200")
    })

    it("can also match with URL encoded hashes", async () => {
      const menuItems = [
        { label: "active item", route: "/foo#matching" },
        { label: "passive item", route: "/foo#not-matching" },
      ]
      await renderComponent({
        menuItems,
        activeRoute: { path: "/foo", hash: "#matching" },
      })

      expect(screen.getByText("active item")).toHaveClass("bg-blue-200")
      expect(screen.getByText("passive item")).not.toHaveClass("bg-blue-200")
    })

    it("ignore level one item if any of its level two matches active route ", async () => {
      const menuItems = [
        {
          label: "level one",
          route: "/matching",
          children: [
            { label: "first level two", route: "/matching#hash" },
            { label: "second level two", route: "/not-matching" },
          ],
        },
      ]

      await renderComponent({
        menuItems,
        activeRoute: "/matching#hash",
      })

      expect(screen.getByText("level one")).not.toHaveClass("bg-blue-200")
      expect(screen.getByText("first level two")).toHaveClass("bg-blue-200")
      expect(screen.getByText("second level two")).not.toHaveClass(
        "bg-blue-200"
      )
    })
  })

  describe("expansion of level one items", () => {
    it("hides all level two items per default", async () => {
      const menuItems = [
        {
          label: "first level one",
          route: "/not-matching",
          children: [{ label: "first level two", route: "/not-matching" }],
        },
        {
          label: "second level one",
          route: "/not-matching",
          children: [{ label: "second level two", route: "/not-matching" }],
        },
      ]

      await renderComponent({ menuItems })

      expect(screen.queryByText("first level one")).toBeVisible()
      expect(screen.queryByText("first level two")).not.toBeVisible()
      expect(screen.queryByText("second level one")).toBeVisible()
      expect(screen.queryByText("second level two")).not.toBeVisible()
    })

    it("shows all level two items of an active level one item", async () => {
      const menuItems = [
        {
          label: "first level one",
          route: "/matching",
          children: [
            { label: "first level two", route: "/not-matching" },
            { label: "second level two", route: "/not-matching" },
          ],
        },
        {
          label: "second level one",
          route: "/not-matching",
          children: [{ label: "third level two", route: "/not-matching" }],
        },
      ]

      await renderComponent({
        menuItems,
        activeRoute: "/matching",
      })

      expect(screen.queryByText("first level one")).toBeVisible()
      expect(screen.queryByText("first level two")).toBeVisible()
      expect(screen.queryByText("second level two")).toBeVisible()
      expect(screen.queryByText("second level one")).toBeVisible()
      expect(screen.queryByText("third level two")).not.toBeVisible()
    })

    it("shows all siblings of an active level two item", async () => {
      const menuItems = [
        {
          label: "first level one",
          route: "/not-matching",
          children: [
            { label: "first level two", route: "/matching" },
            { label: "second level two", route: "/not-matching" },
          ],
        },
        {
          label: "second level one",
          route: "/not-matching",
          children: [{ label: "third level two", route: "/not-matching" }],
        },
      ]

      await renderComponent({
        menuItems,
        activeRoute: "/matching",
      })

      expect(screen.queryByText("first level one")).toBeVisible()
      expect(screen.queryByText("first level two")).toBeVisible()
      expect(screen.queryByText("second level two")).toBeVisible()
      expect(screen.queryByText("second level one")).toBeVisible()
      expect(screen.queryByText("third level two")).not.toBeVisible()
    })
  })
})

interface MenuItem {
  label: string
  route: RouteLocationRaw
  children?: MenuItem[]
  isDisabled?: boolean
}

async function renderComponent(options?: {
  goBackLabel?: string
  goBackRoute?: RouteLocationRaw
  menuItems?: MenuItem[]
  activeRoute?: RouteLocationRaw
}) {
  const goBackRoute = options?.goBackRoute ?? "/go-back-route"
  const menuItems = options?.menuItems ?? []
  const activeRoute = options?.activeRoute ?? "/"
  const router = buildRouter(goBackRoute, menuItems)
  router.replace(activeRoute)
  await router.isReady()
  const global = { plugins: [router] }
  const props = {
    goBackLabel: options?.goBackLabel ?? "go back label",
    goBackRoute,
    menuItems: options?.menuItems ?? [],
  }
  return render(NavbarSide, { props, global })
}

function buildRouter(
  goBackRoute: RouteLocationRaw,
  menuItems: MenuItem[]
): Router {
  const routes = []
  routes.push(generateRouterRoute({ path: "/" }))
  routes.push(generateRouterRoute(goBackRoute))

  for (const item of menuItems) {
    routes.push(generateRouterRoute(item.route))

    for (const child of item.children ?? []) {
      routes.push(generateRouterRoute(child.route))
    }
  }

  return createRouter({ routes, history: createWebHistory() })
}

function generateRouterRoute(routeLocation: RouteLocationRaw): RouteRecordRaw {
  if (typeof routeLocation === "string") {
    const routeAsUrl = new URL(routeLocation, "https://fake.com")
    return { path: routeAsUrl.pathname, component: {} }
  } else {
    const path =
      "path" in routeLocation
        ? routeLocation.path
        : generateString({ prefix: "/path-" })
    return { ...routeLocation, path, component: {} }
  }
}
