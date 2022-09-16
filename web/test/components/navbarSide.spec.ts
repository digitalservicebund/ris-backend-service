import { fireEvent, render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import { createVuetify } from "vuetify"
import * as components from "vuetify/components"
import * as directives from "vuetify/directives"
import NavbarSide from "../../src/components/NavbarSide.vue"

const OPEN_SIDEBAR_BTN_TEXT = "Menü"

describe("navbar side", async () => {
  const DOCUMENT_NR = "KORE2022000003"
  const vuetify = createVuetify({ components, directives })
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "/jurisdiction",
        name: "jurisdiction",
        component: {},
      },
      {
        path: `/jurisdiction/documentunit/${DOCUMENT_NR}/files`,
        name: "jurisdiction-documentUnit-:documentNumber-files",
        component: {},
      },
      {
        path: `/jurisdiction/documentunit/${DOCUMENT_NR}/categories`,
        name: "jurisdiction-documentUnit-:documentNumber-categories",
        component: {},
      },
      {
        path: `/jurisdiction/documentunit/${DOCUMENT_NR}/publication`,
        name: "jurisdiction-documentUnit-:documentNumber-publication",
        component: {},
      },
    ],
  })
  test("navbar side is closed", async () => {
    const { emitted } = render(NavbarSide, {
      props: { documentNumber: DOCUMENT_NR, visible: false },
      global: { plugins: [vuetify, router] },
    })
    const openSidebarBtn = screen.getByText(OPEN_SIDEBAR_BTN_TEXT)
    expect(openSidebarBtn).toBeInTheDocument()
    await fireEvent.click(openSidebarBtn)
    expect(emitted().toggleNavbar).toBeTruthy()
  })

  test("navbar side is opened", async () => {
    const { getByText } = render(NavbarSide, {
      props: { documentNumber: DOCUMENT_NR, visible: true },
      global: {
        plugins: [vuetify, router],
      },
    })
    getByText("Rubriken", { exact: false })
    getByText("Stammdaten", { exact: false })
    getByText("Kurz- & Langtexte", { exact: false })
    getByText("Rechtszug", { exact: false })
    getByText("Dokumente", { exact: false })
    getByText("Bearbeitungsstand", { exact: false })
    getByText("Veröffentlichen", { exact: false })
    expect(
      screen.getByText("Rubriken").closest("a")?.getAttribute("href")
    ).toEqual(`/jurisdiction/documentunit/${DOCUMENT_NR}/categories`)
    expect(
      screen.getByText("Stammdaten").closest("a")?.getAttribute("href")
    ).toEqual(`/jurisdiction/documentunit/${DOCUMENT_NR}/categories#coreData`)
    expect(
      screen.getByText("Kurz- & Langtexte").closest("a")?.getAttribute("href")
    ).toEqual(`/jurisdiction/documentunit/${DOCUMENT_NR}/categories#texts`)
    expect(
      screen.getByText("Dokumente").closest("a")?.getAttribute("href")
    ).toEqual(`/jurisdiction/documentunit/${DOCUMENT_NR}/files`)
    expect(
      screen.getByText("Veröffentlichen").closest("a")?.getAttribute("href")
    ).toEqual(`/jurisdiction/documentunit/${DOCUMENT_NR}/publication`)
  })
})
