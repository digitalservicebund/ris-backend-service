import { render } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import type { Router, RouteRecordRaw, RouteLocationRaw } from "vue-router"
import NavbarSide from "@/components/NavbarSide.vue"
import { generateString } from "~/test-helper/dataGenerators"

describe("NavbarSide", () => {
  it("displays the go back label with related route", () => {
    const { queryByText } = renderComponent({
      goBackLabel: "return",
      goBackRoute: "/origin-route",
    })

    const goBackItem = queryByText("return")?.closest("a")

    expect(goBackItem).toBeVisible()
    expect(goBackItem?.getAttribute("href")).toBe("/origin-route")
  })

  it("shows a router link for each configured menu item", () => {
    const menuItems = [
      { label: "first item", route: "/first-route" },
      { label: "second item", route: "/second-route" },
    ]

    const { queryByText } = renderComponent({ menuItems })
    const firstItem = queryByText("first item")?.closest("a")
    const secondItem = queryByText("second item")?.closest("a")

    expect(firstItem).toBeVisible()
    expect(firstItem?.getAttribute("href")).toBe("/first-route")

    expect(secondItem).toBeVisible()
    expect(secondItem?.getAttribute("href")).toBe("/second-route")
  })

  it("allows to render level one item with level two items as children", () => {
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

    const { queryByText } = renderComponent({ menuItems })
    const firstLevelTwo = queryByText("first level two")?.closest("a")
    const secondLevelTwo = queryByText("second level two")?.closest("a")

    expect(firstLevelTwo).toBeVisible()
    expect(firstLevelTwo?.getAttribute("href")).toBe("/first-level-two")

    expect(secondLevelTwo).toBeVisible()
    expect(secondLevelTwo?.getAttribute("href")).toBe("/second-level-two")
  })

  it("allows to disable a menu item", () => {
    const menuItems = [
      { label: "disabled item", route: "/route", isDisabled: true },
    ]

    const { queryByText } = renderComponent({ menuItems })
    const disabledItem = queryByText("disabled item")

    expect(disabledItem?.getAttribute("disabled")).toBeDefined()
  })
})

interface MenuItem {
  label: string
  route: RouteLocationRaw
  children?: MenuItem[]
  isDisabled?: boolean
}

function renderComponent(options?: {
  goBackLabel?: string
  goBackRoute?: RouteLocationRaw
  menuItems?: MenuItem[]
}) {
  const goBackRoute = options?.goBackRoute ?? "/go-back-route"
  const menuItems = options?.menuItems ?? []
  const router = buildRouter(goBackRoute, menuItems)
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
    return { path: routeLocation, component: {} }
  } else {
    return {
      ...routeLocation,
      path:
        "path" in routeLocation ? routeLocation.path : generateString("/path-"),
      component: {},
    }
  }
}
