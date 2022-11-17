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
      { name: "first item", route: "/first-route" },
      { name: "second item", route: "/second-route" },
    ]

    const { queryByText } = renderComponent({ menuItems })
    const firstItem = queryByText("first item")?.closest("a")
    const secondItem = queryByText("second item")?.closest("a")

    expect(firstItem).toBeVisible()
    expect(firstItem?.getAttribute("href")).toBe("/first-route")

    expect(secondItem).toBeVisible()
    expect(secondItem?.getAttribute("href")).toBe("/second-route")
  })

  it("allows to render parent item with child entries", () => {
    const menuItems = [
      {
        name: "parent item",
        route: "/parent",
        children: [
          { name: "first child", route: "/first-child" },
          { name: "second child", route: "/second-child" },
        ],
      },
    ]

    const { queryByText } = renderComponent({ menuItems })
    const firstChild = queryByText("first child")?.closest("a")
    const secondChild = queryByText("second child")?.closest("a")

    expect(firstChild).toBeVisible()
    expect(firstChild?.getAttribute("href")).toBe("/first-child")

    expect(secondChild).toBeVisible()
    expect(secondChild?.getAttribute("href")).toBe("/second-child")
  })

  it("allows to disable a menu item", () => {
    const menuItems = [
      { name: "disabled item", route: "/route", isDisabled: true },
    ]

    const { queryByText } = renderComponent({ menuItems })
    const disabledItem = queryByText("disabled item")

    expect(disabledItem?.getAttribute("disabled")).toBeDefined()
  })
})

interface MenuItem {
  name: string
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
  routes.push(generateRouterRoute({ name: "root", path: "/" }))
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
