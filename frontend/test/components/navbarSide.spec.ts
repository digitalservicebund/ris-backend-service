import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import type { Router, RouteRecordRaw, RouteLocationRaw } from "vue-router"
import NavbarSide from "@/components/NavbarSide.vue"
import MenuItem from "@/domain/menuItem"
import Route from "@/domain/route"

describe("NavbarSide", () => {
  it("renders sidenav with multiple items and correct routes", async () => {
    const menuItems: MenuItem[] = [
      { label: "first item", route: { name: "first-route" } },
      { label: "second item", route: { name: "second-route" } },
    ]

    await renderComponent({ menuItems })
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
        label: "Rubriken",
        route: {
          name: "parent-route",
        },
        children: [
          {
            label: "Stammdaten",
            route: {
              name: "caselaw-documentUnit-documentNumber-categories",
              hash: "#coreData",
            },
          },
          {
            label: "Rechtszug",
            route: {
              name: "caselaw-documentUnit-documentNumber-categories",
              hash: "#proceedingDecisions",
            },
          },
        ],
      },
    ]

    await renderComponent({ menuItems })

    const firstSubItem = screen.getByText("first level two") as HTMLElement
    const secondSubItem = screen.getByLabelText(
      "second level two",
    ) as HTMLElement

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

    expect(disabledItem).not.toBeInTheDocument()
  })

  describe("highlighting of the currently active menu item", () => {
    it("applies special class to menu item which matches current route", async () => {
      const menuItems: MenuItem[] = [
        { label: "active item", route: { path: "/matching" } },
        { label: "passive item", route: { path: "/not-matching" } },
      ]
      await renderComponent({
        menuItems,
        activeRoute: { path: "/matching" },
      })

      expect(screen.getByText("active item")).toBeVisible()
      expect(screen.getByText("passive item")).toBeVisible()

      //.toHaveClass("bg-blue-200")
      expect(screen.getByLabelText("passive item")).not.toHaveClass(
        "bg-blue-200",
      )
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

      expect(screen.getByLabelText("active item")).toHaveClass("bg-blue-200")
      expect(screen.getByLabelText("passive item")).not.toHaveClass(
        "bg-blue-200",
      )
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

    it("can also match with URL encoded hashes", async () => {
      const menuItems = [
        { label: "active item", route: "/foo#matching" },
        { label: "passive item", route: "/foo#not-matching" },
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

      expect(screen.getByLabelText("level one")).not.toHaveClass("bg-blue-200")
      expect(screen.getByLabelText("first level two")).toHaveClass(
        "bg-blue-200",
      )
      expect(screen.getByLabelText("second level two")).not.toHaveClass(
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

async function renderComponent(options?: {
  menuItems?: MenuItem[]
  activeRoute?: RouteLocationRaw
}) {
  const menuItems = options?.menuItems ?? []
  const activeRoute = options?.activeRoute ?? "/index"
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
  routes.push(generateRouterRoute({ name: "/index" }))

  for (const item of menuItems) {
    routes.push(generateRouterRoute(item.route))

    for (const child of item.children ?? []) {
      routes.push(generateRouterRoute(child.route))
    }
  }

  return createRouter({ routes, history: createWebHistory() })
}

function generateRouterRoute(route?: Route): RouteRecordRaw {
  let path = "/" + route?.name
  if (route?.hash) {
    path = path + "/" + route?.hash
  }

  return { ...route, path, component: {} }
}
