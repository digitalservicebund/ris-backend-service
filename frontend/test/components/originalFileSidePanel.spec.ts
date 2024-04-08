import { render, screen } from "@testing-library/vue"
import { Router, createRouter, createWebHistory } from "vue-router"
import OriginalFileSidePanel from "@/components/OriginalFileSidePanel.vue"

describe("originalFile SidePanel", () => {
  let router: Router
  beforeAll(() => {
    vi.mock("vue-router", async () => {
      const router = (await vi.importActual("vue-router")) as Record<
        string,
        unknown
      >
      return {
        ...router,
        useRoute: vi.fn().mockReturnValue({
          params: {
            documentNumber: "123",
          },
        }),
      }
    })

    router = createRouter({
      history: createWebHistory(),
      routes: [
        {
          path: "/",
          name: "home",
          component: {},
        },
        {
          path: "/caselaw/documentUnit/:documentNumber/files",
          name: "caselaw-documentUnit-documentNumber-files",
          component: {},
        },
      ],
    })
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
    expect(screen.queryByLabelText("Ladestatus")).not.toBeInTheDocument()
    expect(
      screen.queryByText("Es wurde noch kein Originaldokument hochgeladen."),
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
    screen.getByLabelText("Ladestatus")
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
