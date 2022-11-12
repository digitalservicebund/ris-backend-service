import { render } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import NavbarSide from "@/components/NavbarSide.vue"

function renderComponent(options?: {
  goBackLabel?: string
  goBackRoute?: { path: string }
  menuItems?: { name: string; route: { path: string }; isDisabled?: boolean }[]
}) {
  const props = {
    goBackLabel: options?.goBackLabel ?? "go back label",
    goBackRoute: options?.goBackRoute ?? { path: "go-back-route" },
    menuItems: options?.menuItems ?? [],
  }
  const router = createRouter({ routes: [], history: createWebHistory() })
  const global = { plugins: [router] }
  return render(NavbarSide, { props, global })
}

describe("navbarSide", () => {
  it("displays the go back label with related route", () => {
    const { queryByText } = renderComponent({
      goBackLabel: "return",
      goBackRoute: { path: "origin-route" },
    })

    const goBackItem = queryByText("return")
    const goBackLink = goBackItem?.closest("a")

    expect(goBackItem).toBeVisible()
    expect(goBackLink).toHaveAttribute("href")
    expect(goBackLink?.getAttribute("href")).toBe("/origin-route")
  })

  it("shows a router link for each configured menu item", () => {
    const menuItems = [
      { name: "first item", route: { path: "first-route" } },
      { name: "second item", route: { path: "second-route" } },
    ]

    const { queryByText } = renderComponent({ menuItems })

    const firstItem = queryByText("first item")
    const firstLink = firstItem?.closest("a")

    const secondItem = queryByText("second item")
    const secondLink = secondItem?.closest("a")

    expect(firstItem).toBeVisible()
    expect(firstLink).toHaveAttribute("href")
    expect(firstLink?.getAttribute("href")).toBe("/first-route")

    expect(secondItem).toBeVisible()
    expect(secondLink).toHaveAttribute("href")
    expect(secondLink?.getAttribute("href")).toBe("/second-route")
  })

  it("allows to render parent item with child entries", () => {
    const menuItems = [
      {
        name: "parent item",
        route: { path: "parent-route" },
        children: [
          { name: "first child", route: { path: "first-child" } },
          { name: "second child", route: { path: "second-child" } },
        ],
      },
    ]

    const { queryByText } = renderComponent({ menuItems })

    const firstChild = queryByText("first child")
    const firstLink = firstChild?.closest("a")

    const secondChild = queryByText("second child")
    const secondLink = secondChild?.closest("a")

    expect(firstChild).toBeVisible()
    expect(firstLink).toHaveAttribute("href")
    expect(firstLink?.getAttribute("href")).toBe("/first-child")

    expect(secondChild).toBeVisible()
    expect(secondLink).toHaveAttribute("href")
    expect(secondLink?.getAttribute("href")).toBe("/second-child")
  })

  it("allows to disable a menu item", () => {
    const menuItems = [
      { name: "disabled item", route: { path: "" }, isDisabled: true },
    ]

    const { queryByText } = renderComponent({ menuItems })

    const disabledItem = queryByText("disabled item")

    expect(disabledItem?.getAttribute("disabled")).toBeDefined()
  })
})
