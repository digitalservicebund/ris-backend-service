import { render, screen } from "@testing-library/vue"
import PortalPublicationStatusBadge from "@/components/publication/PortalPublicationStatusBadge.vue"
import { PortalPublicationStatus } from "@/domain/portalPublicationStatus"

describe("PortalPublicationStatusBadge", () => {
  it("should show unpublished status", async () => {
    await renderComponent(PortalPublicationStatus.UNPUBLISHED)
    expect(screen.getByText("Unveröffentlicht")).toBeInTheDocument()
  })
  it("should show published status", async () => {
    await renderComponent(PortalPublicationStatus.PUBLISHED)
    expect(screen.getByText("Veröffentlicht")).toBeInTheDocument()
  })
  it("should show withdrawn status", async () => {
    await renderComponent(PortalPublicationStatus.WITHDRAWN)
    expect(screen.getByText("Zurückgezogen")).toBeInTheDocument()
  })
  it("should not show anything on empty status", async () => {
    await renderComponent("Unknown" as unknown as PortalPublicationStatus)
    expect(screen.getByText("Unbekannter Status")).toBeInTheDocument()
  })
})

async function renderComponent(status: PortalPublicationStatus) {
  return render(PortalPublicationStatusBadge, { props: { status } })
}
