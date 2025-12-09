import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import DecisionSummary from "@/components/DecisionSummary.vue"
import { DisplayMode } from "@/components/enumDisplayMode"
import { PublicationState, PublicationStatus } from "@/domain/publicationStatus"
import routes from "~/test-helper/routes"

function renderComponent(options?: {
  summary?: string
  status?: PublicationStatus
  documentNumber?: string
  displayMode?: DisplayMode
  linkClickable?: boolean
  isPendingProceeding?: boolean
}) {
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  return render(DecisionSummary, {
    props: {
      summary: options?.summary ?? "test decision summary",
      status: options?.status ?? undefined,
      documentNumber: options?.documentNumber ?? undefined,
      displayMode: options?.displayMode ?? undefined,
      linkClickable: options?.linkClickable ?? true,
      isPendingProceeding: options?.isPendingProceeding ?? true,
    },
    global: {
      plugins: [[createTestingPinia()], [router]],
    },
  })
}

describe("Decision summary", () => {
  it("renders court correctly", async () => {
    renderComponent()
    expect(await screen.findByText(/test decision summary/)).toBeVisible()
  })

  it("renders with router link if documentNumber given", async () => {
    renderComponent({ documentNumber: "12345678" })
    expect(screen.getByRole("link")).toHaveAttribute(
      "href",
      expect.stringMatching(/12345678/),
    )
  })

  it("renders with button to open sidepanel if documentNumber given and display mode is sidepane", async () => {
    renderComponent({
      documentNumber: "12345678",
      displayMode: DisplayMode.SIDEPANEL,
    })
    expect(screen.getByRole("button")).toHaveTextContent("12345678")
  })

  it("renders with status badge if status given", async () => {
    renderComponent({
      status: {
        publicationStatus: PublicationState.PUBLISHED,
        withError: false,
      },
    })
    expect(await screen.findByText(/Veröffentlicht/)).toBeVisible()
  })

  it("docnumber is not clickable, if linkClickable false", async () => {
    renderComponent({
      linkClickable: false,
    })

    expect(screen.queryByRole("link")).not.toBeInTheDocument()
    expect(screen.queryByRole("button")).not.toBeInTheDocument()
  })

  it("docnumber links to decision preview", async () => {
    renderComponent({
      documentNumber: "12345678",
      isPendingProceeding: false,
    })

    expect(screen.getByRole("link")).toHaveAttribute(
      "href",
      expect.stringMatching(/documentUnit\/12345678\/preview/),
    )
  })

  it("docnumber links to pending proceeding preview", async () => {
    renderComponent({
      documentNumber: "12345678",
      isPendingProceeding: true,
    })

    expect(screen.getByRole("link")).toHaveAttribute(
      "href",
      expect.stringMatching(/pendingProceeding\/12345678\/preview/),
    )
  })
})
