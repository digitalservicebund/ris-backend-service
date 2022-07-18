import { render } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import NavbarTop from "../../src/components/NavbarTop.vue"

const NAVBAR_TOP_CLASS_NAME = "topbar-main"
const LOGO_CLASS_NAME = "topbar-logo-circle"
const HEADER_TEXT_CLASS_NAME = "topbar-header"
const HEADER_TEXT = "Rechtsinformationssystem\ndes Bundes [Platzhalter]"
const NAVBAR_TOP_LINK_CLASS_NAME = "topbar-link-box"
const NAVBAR_TOP_LINK_URL = "/jurisdiction"

describe("navbar top", () => {
  const vuetify = createVuetify({ components, directives })
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "/",
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

    test("navbar top should be be rendered", async () => {
      expect(
        container.getElementsByClassName(NAVBAR_TOP_CLASS_NAME).length
      ).toEqual(1)
    })

    test("logo should be be rendered", async () => {
      expect(container.getElementsByClassName(LOGO_CLASS_NAME).length).toEqual(
        1
      )
    })

    test("header text should be be rendered", async () => {
      expect(
        container.getElementsByClassName(HEADER_TEXT_CLASS_NAME).length
      ).toBe(1)
    })

    test("header text should be be rendered correctly", async () => {
      const header_text = container.getElementsByClassName(
        HEADER_TEXT_CLASS_NAME
      )[0]
      expect(header_text.textContent).toEqual(HEADER_TEXT)
    })

    test("header text should be be rendered correctly", async () => {
      const headerText = container.getElementsByClassName(
        HEADER_TEXT_CLASS_NAME
      )[0]
      expect(headerText.textContent).toEqual(HEADER_TEXT)
    })

    test("top bar link should be be rendered", async () => {
      expect(
        container.getElementsByClassName(NAVBAR_TOP_LINK_CLASS_NAME).length
      ).toBe(1)
    })

    test("top bar link should be be rendered correctly", async () => {
      const navLink = container.getElementsByClassName(
        NAVBAR_TOP_LINK_CLASS_NAME
      )[0]
      const navLinkUrl = navLink.firstChild
      expect(navLinkUrl).toHaveProperty("href", NAVBAR_TOP_LINK_URL)
    })
  })
})
