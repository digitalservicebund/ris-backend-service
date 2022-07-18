import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import NavbarTop from "../../src/components/NavbarTop.vue"

const NAVBAR_TOP_CLASS_NAME = "topbar-main"
const LOGO_CLASS_NAME = "topbar-logo-circle"
const HEADER_TEXT_CLASS_NAME = "topbar-header"
const HEADER_TEXT = " Rechtsinformationssystem des Bundes [Platzhalter] "
const NAVBAR_TOP_LINK_CLASS_NAME = "topbar-link-box"
const NAVBAR_TOP_LINK_TEXT = "Rechtsprechung"
const NAVBAR_TOP_LINK_URL = "/jurisdiction"

describe("navbar top", () => {
  const vuetify = createVuetify({ components, directives })
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "/jurisdiction",
        name: "jurisdiction",
        component: {},
      },
    ],
  })

  test("navbar top should be rendered without error", async () => {
    const { container } = render(NavbarTop, {
      props: {},
      global: { plugins: [vuetify, router] },
    })

    expect(
      container.getElementsByClassName(NAVBAR_TOP_CLASS_NAME).length
    ).toEqual(1)

    expect(container.getElementsByClassName(LOGO_CLASS_NAME).length).toEqual(1)

    expect(
      container.getElementsByClassName(HEADER_TEXT_CLASS_NAME).length
    ).toBe(1)

    const header_text = container.getElementsByClassName(
      HEADER_TEXT_CLASS_NAME
    )[0]
    expect(header_text.textContent).toEqual(HEADER_TEXT)

    const headerText = container.getElementsByClassName(
      HEADER_TEXT_CLASS_NAME
    )[0]
    expect(headerText.textContent).toEqual(HEADER_TEXT)

    expect(
      container.getElementsByClassName(NAVBAR_TOP_LINK_CLASS_NAME).length
    ).toBe(1)

    const navLink = screen.getByText(NAVBAR_TOP_LINK_TEXT).closest("a")
    expect(navLink?.href).contains(NAVBAR_TOP_LINK_URL)
  })
})
