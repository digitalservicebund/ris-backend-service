import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import type { Router, RouteRecordRaw, RouteLocationRaw } from "vue-router"
import NavbarSide from "@/components/NavbarSide.vue"
import MenuItem from "@/domain/menuItem"
import Route from "@/domain/route"

const activeRoute: RouteLocationRaw = {
  name: "index",
}

describe("NavbarSide", () => {
  it("renders sidenav with multiple items and correct routes", async () => {
    const menuItems: MenuItem[] = [
      { label: "first item", route: { name: "first-route" } },
      { label: "second item", route: { name: "second-route" } },
    ]

    await renderComponent({ menuItems, activeRoute })
    expect(screen.getByText("first item")).toBeVisible()
    expect(screen.getByText("second item")).toBeVisible()

    expect(screen.getByTestId("first-route")).toHaveAttribute(
      "href",
      "/first-route",
    )

    expect(screen.getByTestId("second-route")).toHaveAttribute(
      "href",
      "/second-route",
    )
  })

  it("allows to render parent item with its children", async () => {
    const menuItems: MenuItem[] = [
      {
        label: "first parent item",
        route: {
          name: "parent-route",
        },
        children: [
          {
            label: "first child item",
            route: {
              name: "parent-route",
              hash: "#coreData",
            },
          },
          {
            label: "second child item",
            route: {
              name: "parent-route",
              hash: "#proceedingDecisions",
            },
          },
        ],
      },
    ]

    await renderComponent({ menuItems, activeRoute: { name: "parent-route" } })

    expect(screen.getByText("first parent item")).toBeVisible()
    expect(screen.getByText("first child item")).toBeVisible()
    expect(screen.getByText("second child item")).toBeVisible()
  })

  it("allows to disable a menu item", async () => {
    const menuItems = [
      { label: "disabled item", route: "/route", isDisabled: true },
    ]

    await renderComponent({ menuItems })
    const disabledItem = screen.queryByText("disabled item")

    expect(disabledItem).not.toBeInTheDocument()
  })

  describe("highlighting of the currently active menu item", () => {
    it("applies styling for parent active menu item", async () => {
      const menuItems: MenuItem[] = [
        { label: "active item", route: { name: "matching" } },
        { label: "passive item", route: { name: "not-matching" } },
      ]
      await renderComponent({
        menuItems,
        activeRoute: { path: "/matching" },
      })

      expect(screen.getByText("active item")).toBeVisible()
      expect(screen.getByText("passive item")).toBeVisible()

      expect(screen.getByTestId("active item")).toHaveClass("bg-blue-200")
      expect(screen.getByTestId("passive item")).not.toHaveClass("bg-blue-200")

      expect(screen.getByText("active item")).toHaveClass("underline")
      expect(screen.getByText("passive item")).not.toHaveClass("underline")
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

      expect(screen.getByLabelText("active item")).toHaveClass("bg-blue-200")
      expect(screen.getByLabelText("passive item")).not.toHaveClass(
        "bg-blue-200",
      )
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

      expect(screen.getByLabelText("active item")).toHaveClass("bg-blue-200")
    })

    it("don't highlight parent if its child is active", async () => {
      const menuItems: MenuItem[] = [
        {
          label: "parent node",
          route: {
            name: "parent",
          },
          children: [
            {
              label: "first child node",
              route: {
                name: "parent",
                hash: "#active",
              },
            },
            {
              label: "second child node",
              route: {
                name: "parent",
                hash: "#not-active",
              },
            },
          ],
        },
      ]

      await renderComponent({
        menuItems,
        activeRoute: "/parent#active",
      })

      expect(screen.getByTestId("parent node")).not.toHaveClass("bg-blue-200")
      expect(screen.getByTestId("first child node")).toHaveClass("bg-blue-200")
      expect(screen.getByTestId("second child node")).not.toHaveClass(
        "bg-blue-200",
      )
    })

    it("underlines the active level two menu item", async () => {
      const menuItems = [
        {
          label: "level one",
          route: "/route",
          children: [
            { label: "active level two", route: "/active-level-two" },
            { label: "passive level two", route: "/passive-level-two" },
          ],
        },
      ]

      await renderComponent({ menuItems, activeRoute: "/active-level-two" })

      expect(screen.getByLabelText("active level two")).toHaveClass("underline")
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

    it("underlines the expanded level one item with children", async () => {
      const menuItems = [
        {
          label: "underlined level one",
          route: "/route",
          children: [
            { label: "child one", route: "/child-one" },
            { label: "child two", route: "/child-two" },
          ],
        },
      ]

      await renderComponent({ menuItems, activeRoute: "/child-one" })

      expect(screen.getByLabelText("underlined level one")).toHaveClass(
        "underline",
      )
    })
  })
})

async function renderComponent(options: {
  menuItems: MenuItem[]
  activeRoute?: RouteLocationRaw
}) {
  const routes = []
  routes.push(options.activeRoute)
  routes.push(buildMenuItems(options.menuItems))

  const activeRoute = options?.activeRoute ?? "/index"

  const router = buildMenuItems(options.menuItems)
  await router.replace(activeRoute)
  await router.isReady()
  const global = { plugins: [router] }
  const props = {
    menuItems: options?.menuItems ?? [],
    activeRoute: options.activeRoute,
  }
  return render(NavbarSide, { props, global })
}

function buildMenuItems(menuItems: MenuItem[]): Router {
  const routes = []
  routes.push(generateRouterRoute({ name: "index" }))

  for (const item of menuItems) {
    routes.push(generateRouterRoute(item.route))

    for (const child of item.children ?? []) {
      routes.push(generateRouterRoute(child.route))
    }
  }

  return createRouter({ routes, history: createWebHistory() })
}

function generateRouterRoute(route?: Route): RouteRecordRaw {
  const path = "/" + route?.name
  return { ...route, path, component: {} }
}
