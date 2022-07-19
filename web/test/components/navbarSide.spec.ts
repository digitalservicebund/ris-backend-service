import { fireEvent } from "@testing-library/dom"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import NavbarSide from "../../src/components/NavbarSide.vue"

const OPEN_SIDEBAR_BTN_TEXT = "Menü"
const OPEN_SIDEBAR_TEXES = [
  "Rubriken",
  "Stammdaten",
  "Kurz- & Langtexte",
  "Rechtszug",
  "Dokumente",
  "Bearbeitungsstand",
  "docx --> html",
]
describe("navbar side", async () => {
  const vuetify = createVuetify({ components, directives })
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "jurisdiction-docUnit-:documentNumber-files",
        component: {},
      },
    ],
  })

  test("navbar side is closed", async () => {
    const { emitted } = render(NavbarSide, {
      props: { documentNumber: "KORE2022000003", visible: false },
      global: { plugins: [vuetify, router] },
    })
    const openSidebarBtn = screen.getByText(OPEN_SIDEBAR_BTN_TEXT)
    expect(openSidebarBtn).toBeTruthy()
    await fireEvent.click(openSidebarBtn)
    expect(emitted().toggleNavbar).toBeTruthy()
  })

  test("navbar side is opened", async () => {
    render(NavbarSide, {
      props: { documentNumber: "KORE2022000003", visible: false },
      global: { plugins: [vuetify, router] },
    })
    const openSidebarBtn = screen.getByText(OPEN_SIDEBAR_BTN_TEXT)
    await fireEvent.click(openSidebarBtn)
    OPEN_SIDEBAR_TEXES.forEach(async (textToMatch) => {
      const sidebarText = await screen.findByText(textToMatch, {
        exact: false,
      })
      expect(sidebarText).toBeTruthy()
    })
  })
})
