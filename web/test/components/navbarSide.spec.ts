import { render } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import type { Router, RouteRecordRaw, RouteLocationRaw } from "vue-router"
import NavbarSide from "@/components/NavbarSide.vue"
import { generateString } from "~/test-helper/dataGenerators"

describe("NavbarSide", () => {
  it("displays the go back label with related route", async () => {
    const { queryByText } = await renderComponent({
      goBackLabel: "return",
      goBackRoute: "/origin-route",
    })

    const goBackItem = queryByText("return")?.closest("a")

    expect(goBackItem).toBeVisible()
    expect(goBackItem?.getAttribute("href")).toBe("/origin-route")
  })

  it("shows a router link for each configured menu item", async () => {
    const menuItems = [
      { label: "first item", route: "/first-route" },
      { label: "second item", route: "/second-route" },
    ]

    const { queryByText } = await renderComponent({ menuItems })
    const firstItem = queryByText("first item")?.closest("a")
    const secondItem = queryByText("second item")?.closest("a")

    expect(firstItem).toBeVisible()
    expect(firstItem?.getAttribute("href")).toBe("/first-route")

    expect(secondItem).toBeVisible()
    expect(secondItem?.getAttribute("href")).toBe("/second-route")
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

    const { queryByText } = await renderComponent({ menuItems })
    const firstLevelTwo = queryByText("first level two")?.closest("a")
    const secondLevelTwo = queryByText("second level two")?.closest("a")

    expect(firstLevelTwo).toBeVisible()
    expect(firstLevelTwo?.getAttribute("href")).toBe("/first-level-two")

    expect(secondLevelTwo).toBeVisible()
    expect(secondLevelTwo?.getAttribute("href")).toBe("/second-level-two")
  })

  it("allows to disable a menu item", async () => {
    const menuItems = [
      { label: "disabled item", route: "/route", isDisabled: true },
    ]

    const { queryByText } = await renderComponent({ menuItems })
    const disabledItem = queryByText("disabled item")

    expect(disabledItem?.getAttribute("disabled")).toBeDefined()
  })

  describe("highlighting of the currently active menu item", () => {
    it("applies special class to menu item which matches current route", async () => {
      const menuItems = [
        { label: "active item", route: { path: "/matching" } },
        { label: "passive item", route: { path: "/not-matching" } },
      ]
      const { getByText } = await renderComponent({
        menuItems,
        activeRoute: { path: "/matching" },
      })

      expect(getByText("active item")).toHaveClass("bg-blue-200")
      expect(getByText("passive item")).not.toHaveClass("bg-blue-200")
    })

    it("routes match also by name", async () => {
      const menuItems = [
        { label: "active item", route: { name: "active" } },
        { label: "passive item", route: { name: "passive" } },
      ]
      const { getByText } = await renderComponent({
        menuItems,
        activeRoute: { name: "active" },
      })

      expect(getByText("active item")).toHaveClass("bg-blue-200")
      expect(getByText("passive item")).not.toHaveClass("bg-blue-200")
    })

    it("routes match includes hash if given", async () => {
      const menuItems = [
        { label: "active item", route: { path: "/foo", hash: "#matching" } },
        { label: "passive item", route: { path: "/foo", hash: "#no-match" } },
      ]
      const { getByText } = await renderComponent({
        menuItems,
        activeRoute: { path: "/foo", hash: "#matching" },
      })

      expect(getByText("active item")).toHaveClass("bg-blue-200")
      expect(getByText("passive item")).not.toHaveClass("bg-blue-200")
    })

    it("ignores queries of any to match route", async () => {
      const menuItems = [
        {
          label: "active item",
          route: { name: "foo", query: { key: "value" } },
        },
      ]
      const { getByText } = await renderComponent({
        menuItems,
        activeRoute: { name: "foo", query: { key: "other-value" } },
      })

      expect(getByText("active item")).toHaveClass("bg-blue-200")
    })

    it("can also match with URL encoded hashes", async () => {
      const menuItems = [
        { label: "active item", route: "/foo#matching" },
        { label: "passive item", route: "/foo#not-matching" },
      ]
      const { getByText } = await renderComponent({
        menuItems,
        activeRoute: { path: "/foo", hash: "#matching" },
      })

      expect(getByText("active item")).toHaveClass("bg-blue-200")
      expect(getByText("passive item")).not.toHaveClass("bg-blue-200")
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

      const { getByText } = await renderComponent({
        menuItems,
        activeRoute: "/matching#hash",
      })

      expect(getByText("level one")).not.toHaveClass("bg-blue-200")
      expect(getByText("first level two")).toHaveClass("bg-blue-200")
      expect(getByText("second level two")).not.toHaveClass("bg-blue-200")
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
