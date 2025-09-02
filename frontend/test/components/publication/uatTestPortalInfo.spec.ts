import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import UatTestPortalInfo from "@/components/publication/UatTestPortalInfo.vue"

describe("UatTestPortalInfo", () => {
  it("shows InfoModal in UAT", async () => {
    setActivePinia(
      createTestingPinia({
        initialState: {
          session: {
            env: { environment: "uat", portalUrl: "https://uat.example" },
          },
        },
      }),
    )

    render(UatTestPortalInfo)

    expect(
      screen.getByText(
        "UAT veröffentlicht Dokeinheiten in ein Testportal, nicht das öffentliche Portal.",
      ),
    ).toBeInTheDocument()
    expect(screen.getByText("Dies ist zugänglich unter:")).toBeInTheDocument()
    const link = screen.getByRole("link", { name: "Link zum UAT-Portal" })
    expect(link).toBeInTheDocument()
    expect(link).toHaveAttribute("href", "https://uat.example")
    expect(link).toHaveAttribute("target", "_blank")
    expect(link).toHaveAttribute("rel", "noopener noreferrer")
  })

  it("hides InfoModal in other environment", async () => {
    setActivePinia(
      createTestingPinia({
        initialState: {
          session: {
            env: {
              environment: "production",
              portalUrl: "https://prod.example",
            },
          },
        },
      }),
    )
    render(UatTestPortalInfo)
    expect(
      screen.queryByText(
        "UAT veröffentlicht Dokeinheiten in ein Testportal, nicht das öffentliche Portal.",
      ),
    ).not.toBeInTheDocument()
    expect(screen.queryByRole("link")).not.toBeInTheDocument()
  })
})
