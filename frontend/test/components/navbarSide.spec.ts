import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import type { Router, RouteRecordRaw, RouteLocationRaw } from "vue-router"
import NavbarSide from "@/components/NavbarSide.vue"
import { generateString } from "~/test-helper/dataGenerators"

describe("NavbarSide", () => {
  it("renders sidenav with multiple items and correct routes", async () => {
    const menuItems = [
      { label: "first item", route: "/first-route" },
      { label: "second item", route: "/second-route" },
    ]

    await renderComponent({ menuItems })
    expect(screen.getByText(menuItems[0].label)).toBeVisible()
    expect(screen.getByTestId(menuItems[0].route)).toHaveAttribute(
      "href",
      menuItems[0].route,
    )

    expect(screen.getByText(menuItems[1].label)).toBeVisible()
    expect(screen.getByTestId(menuItems[1].route)).toHaveAttribute(
      "href",
      menuItems[1].route,
    )
  })

  it("does not show disable menu item", async () => {
    const menuItems = [
      { label: "disabled item", route: "/route", isDisabled: true },
    ]

    await renderComponent({ menuItems })
    const disabledItem = screen.queryByText("disabled item")
    expect(disabledItem).not.toBeInTheDocument()
  })

  describe("highlighting of the currently active menu item", () => {
    it("applies special class to menu item which matches current route", async () => {
      const menuItems = [
        { label: "active item", route: { name: "/matching" } },
        { label: "passive item", route: { name: "/not-matching" } },
      ]

      await renderComponent({
        menuItems,
        activeRoute: { name: "/matching" },
      })

      expect(screen.getByTestId(menuItems[0].label)).toHaveClass("bg-blue-200")
      expect(screen.getByTestId(menuItems[1].label)).not.toHaveClass(
        "bg-blue-200",
      )
    })

    it("routes match by name", async () => {
      const menuItems = [
        { label: "active item", route: { name: "active-route" } },
        { label: "passive item", route: { name: "passive-route" } },
      ]
      await renderComponent({
        menuItems,
        activeRoute: { name: "active-route" },
      })

      expect(screen.getByTestId("active item")).toHaveClass("bg-blue-200")
      expect(screen.getByTestId("passive item")).not.toHaveClass("bg-blue-200")
    })

    it("routes match includes hash if given", async () => {
      const menuItems = [
        { label: "active item", route: { name: "/foo", hash: "#matching" } },
        { label: "passive item", route: { name: "/foo", hash: "#no-match" } },
      ]
      await renderComponent({
        menuItems,
        activeRoute: { name: "/foo", hash: "#matching" },
      })

      expect(screen.getByTestId("active item")).toHaveClass("bg-blue-200")
      expect(screen.getByTestId("passive item")).not.toHaveClass("bg-blue-200")
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

      expect(screen.getByTestId("active item")).toHaveClass("bg-blue-200")
    })

    it("ignore level one item if any of its level two matches active route ", async () => {
      const menuItems = [
        {
          name: "parent",
          label: "parent item",
          route: "/foo",
          children: [
            {
              label: "first child",
              route: { name: "/foo", hash: "#matching" },
            },
            {
              label: "second child",
              route: { name: "/foo", hash: "#not-matching" },
            },
          ],
        },
      ]

      await renderComponent({
        menuItems,
        activeRoute: {
          name: "/foo",
          hash: "#matching",
        },
      })

      expect(screen.getByTestId("first child")).toHaveClass("bg-blue-200")
      expect(screen.getByTestId("second child")).not.toHaveClass("bg-blue-200")
      expect(screen.getByTestId("parent item")).not.toHaveClass("bg-blue-200")
    })

    it("underlines the active child menu item", async () => {
      const menuItems = [
        {
          label: "level one",
          route: "/route",
          children: [
            {
              label: "first child",
              route: { name: "/foo", hash: "#matching" },
            },
            {
              label: "second child",
              route: { name: "/foo", hash: "#not-matching" },
            },
          ],
        },
      ]

      await renderComponent({
        menuItems,
        activeRoute: {
          name: "/foo",
          hash: "#matching",
        },
      })

      expect(screen.getByTestId("first child")).toHaveClass("underline")
    })
  })

  describe("expansion of children menu items", () => {
    it("hides all subitems default", async () => {
      const menuItems = [
        {
          label: "first-parent-display",
          route: "/first-parent-route",
          children: [
            {
              label: "first-child-exclude",
              route: { name: "/foo", hash: "#matching" },
            },
            {
              label: "second-child-exclude",
              route: { name: "/foo", hash: "#not-matching" },
            },
          ],
        },
        {
          label: "second-parent-display",
          route: "/second-parent-route",
        },
      ]
      await renderComponent({ menuItems })

      expect(screen.getByText("first-parent-display")).toBeVisible()
      expect(
        screen.queryByTestId("first-child-exclude"),
      ).not.toBeInTheDocument()
      expect(
        screen.queryByTestId("second-child-exclude"),
      ).not.toBeInTheDocument()

      expect(screen.getByText("second-parent-display")).toBeVisible()
    })

    it("display children only if active parent", async () => {
      const menuItems = [
        {
          label: "first-parent-display",
          route: {
            name: "/foo",
            hash: "#matching",
          },
          children: [
            {
              label: "first-child-display",
              route: { name: "/foo" },
              hash: "#matchinng",
            },
            {
              label: "second-child-display",
              route: { name: "/foo" },
              hash: "#also-matching",
            },
          ],
        },
        {
          label: "second-parent",
          route: { name: "/not-matching" },
          children: [{ label: "third-child-exclude", route: "/not-matching" }],
        },
      ]

      await renderComponent({
        menuItems,
        activeRoute: {
          name: "/foo",
          hash: "#matching",
        },
      })

      expect(screen.getByTestId("first-child-display")).toBeVisible()
      expect(screen.getByTestId("second-child-display")).toBeVisible()

      expect(
        screen.queryByTestId("third-child-exclude"),
      ).not.toBeInTheDocument()
    })

    it("underlines parent if its child active", async () => {
      const menuItems = [
        {
          label: "first-parent-display",
          route: {
            name: "/foo",
          },
          children: [
            {
              label: "first-child-display",
              route: { name: "/foo" },
              hash: "#matching",
            },
          ],
        },
      ]

      await renderComponent({
        menuItems,
        activeRoute: {
          name: "/foo",
          hash: "#matching",
        },
      })

      expect(screen.getByText("first-parent-display")).toHaveClass("underline")
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
  menuItems?: MenuItem[]
  activeRoute?: RouteLocationRaw
}) {
  const menuItems = options?.menuItems ?? []
  const activeRoute = options?.activeRoute ?? "/"
  const router = buildRouter(menuItems)
  await router.replace(activeRoute)
  await router.isReady()
  const global = { plugins: [router] }
  const props = {
    menuItems: options?.menuItems ?? [],
  }
  return render(NavbarSide, { props, global })
}

function buildRouter(menuItems: MenuItem[]): Router {
  const routes = []
  routes.push(generateRouterRoute({ path: "/" }))

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
      "path" in routeLocation && routeLocation.path
        ? routeLocation.path
        : generateString({ prefix: "/path-" })
    return { ...routeLocation, path, component: {} }
  }
}
