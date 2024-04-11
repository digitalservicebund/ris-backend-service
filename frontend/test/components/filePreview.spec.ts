import { render, screen } from "@testing-library/vue"
import { Router, createRouter, createWebHistory } from "vue-router"
import FilePreview from "@/components/AttachementView.vue"

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

  test("panel shows content", () => {
    render(FilePreview, {
      props: {
        content: "text",
      },
      global: { plugins: [router] },
    })
    expect(screen.getByTestId("text-editor")).toBeInTheDocument()
  })
})
