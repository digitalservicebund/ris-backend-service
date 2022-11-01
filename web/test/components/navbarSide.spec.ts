import { fireEvent, render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import NavbarSide from "../../src/components/NavbarSide.vue"

const OPEN_SIDEBAR_BTN_TEXT = "Menü"

describe("navbar side", async () => {
  const DOCUMENT_NR = "KORE2022000003"
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "/caselaw",
        name: "caselaw",
        component: {},
      },
      {
        path: `/caselaw/documentunit/${DOCUMENT_NR}/files`,
        name: "caselaw-documentUnit-:documentNumber-files",
        component: {},
      },
      {
        path: `/caselaw/documentunit/${DOCUMENT_NR}/categories`,
        name: "caselaw-documentUnit-:documentNumber-categories",
        component: {},
      },
      {
        path: `/caselaw/documentunit/${DOCUMENT_NR}/publication`,
        name: "caselaw-documentUnit-:documentNumber-publication",
        component: {},
      },
    ],
  })
  test("navbar side is closed", async () => {
    const { emitted } = render(NavbarSide, {
      props: { documentNumber: DOCUMENT_NR, visible: false },
      global: { plugins: [router] },
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
        plugins: [router],
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
    ).toEqual(`/caselaw/documentunit/${DOCUMENT_NR}/categories`)
    expect(
      screen.getByText("Stammdaten").closest("a")?.getAttribute("href")
    ).toEqual(`/caselaw/documentunit/${DOCUMENT_NR}/categories#coreData`)
    expect(
      screen.getByText("Kurz- & Langtexte").closest("a")?.getAttribute("href")
    ).toEqual(`/caselaw/documentunit/${DOCUMENT_NR}/categories#texts`)
    expect(
      screen.getByText("Dokumente").closest("a")?.getAttribute("href")
    ).toEqual(`/caselaw/documentunit/${DOCUMENT_NR}/files`)
    expect(
      screen.getByText("Veröffentlichen").closest("a")?.getAttribute("href")
    ).toEqual(`/caselaw/documentunit/${DOCUMENT_NR}/publication`)
  })
})
