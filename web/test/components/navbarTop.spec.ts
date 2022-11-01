import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import NavbarTop from "../../src/components/NavbarTop.vue"

const HEADER_TEXT_SUB_TEXT = "Rechtsinformationssystem"
const HEADER_TEXT = " Rechtsinformationssystem des Bundes [Platzhalter] "
const NAVBAR_TOP_LINK_TEXT = "Rechtsprechung"
const NAVBAR_TOP_LINK_URL = "/"

describe("navbar top", () => {
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "caselaw",
        component: {},
      },
    ],
  })

  test("navbar top should be rendered without error", async () => {
    render(NavbarTop, {
      props: {},
      global: { plugins: [router] },
    })

    const header_text = screen
      .getByText(HEADER_TEXT_SUB_TEXT, { exact: false })
      .closest("div")
    expect(header_text?.textContent).toEqual(HEADER_TEXT)

    const navLink = screen.getByText(NAVBAR_TOP_LINK_TEXT).closest("a")
    expect(navLink?.href).contains(NAVBAR_TOP_LINK_URL)
  })
})
