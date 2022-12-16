import { render, screen } from "@testing-library/vue"
import { describe, test } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import OriginalFileSidePanel from "@/components/OriginalFileSidePanel.vue"

describe("originalFile SidePanel", () => {
  global.ResizeObserver = require("resize-observer-polyfill")
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "",
        name: "caselaw-documentUnit-:documentNumber-files",
        component: {},
      },
    ],
  })

  test("panel not visible if closed", () => {
    render(OriginalFileSidePanel, {
      props: {
        open: false,
        hasFile: true,
        file: "<p>Foo</p>",
      },
      global: { plugins: [router] },
    })
    screen.getByText("Originaldokument")
    expect(screen.queryByText("Dokument wird geladen")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Es wurde noch kein Originaldokument hochgeladen.")
    ).not.toBeInTheDocument()
  })

  test("renders loading if no file provided", async () => {
    render(OriginalFileSidePanel, {
      props: {
        open: true,
        hasFile: true,
      },
      global: { plugins: [router] },
    })
    screen.getByText("Dokument wird geladen")
  })

  test("links to file upload if documentUnit has no file", async () => {
    render(OriginalFileSidePanel, {
      props: {
        open: true,
        hasFile: false,
      },
      global: { plugins: [router] },
    })
    screen.getByText("Es wurde noch kein Originaldokument hochgeladen.")
    screen.getByText("Zum Upload")
  })
})
