import { useStatusBadge } from "@/composables/useStatusBadge"
import { PublicationState } from "@/domain/documentUnit"

describe("useStatusBadge", () => {
  it("unpublished with undefined error", () => {
    const status = {
      publicationStatus: PublicationState.UNPUBLISHED,
      withError: undefined,
    }

    const statusBadge = useStatusBadge(status)

    expect(statusBadge.value.label).toBe("Unveröffentlicht")
  })

  it("unpublished without error", () => {
    const status = {
      publicationStatus: PublicationState.UNPUBLISHED,
      withError: false,
    }

    const statusBadge = useStatusBadge(status)

    expect(statusBadge.value.label).toBe("Unveröffentlicht")
  })

  it("unpublished with errors", () => {
    const status = {
      publicationStatus: PublicationState.UNPUBLISHED,
      withError: true,
    }

    const statusBadge = useStatusBadge(status)

    expect(statusBadge.value.label).toBe("Nicht veröffentlicht")
  })

  it("publishing with undefined error", () => {
    const status = {
      publicationStatus: PublicationState.PUBLISHING,
    }

    const statusBadge = useStatusBadge(status)

    expect(statusBadge.value.label).toBe("In Veröffentlichung")
  })

  it("publishing without errors", () => {
    const status = {
      publicationStatus: PublicationState.PUBLISHING,
      withError: false,
    }

    const statusBadge = useStatusBadge(status)

    expect(statusBadge.value.label).toBe("In Veröffentlichung")
  })

  it("publishing with errors", () => {
    const status = {
      publicationStatus: PublicationState.PUBLISHING,
      withError: true,
    }

    const statusBadge = useStatusBadge(status)

    expect(statusBadge.value.label).toBe("In Veröffentlichung")
  })

  it("published with undefined error", () => {
    const status = {
      publicationStatus: PublicationState.PUBLISHED,
      withError: undefined,
    }

    const statusBadge = useStatusBadge(status)

    expect(statusBadge.value.label).toBe("Veröffentlicht")
  })

  it("published without errors", () => {
    const status = {
      publicationStatus: PublicationState.PUBLISHED,
      withError: false,
    }

    const statusBadge = useStatusBadge(status)

    expect(statusBadge.value.label).toBe("Veröffentlicht")
  })

  it("published with errors", () => {
    const status = {
      publicationStatus: PublicationState.PUBLISHED,
      withError: true,
    }

    const statusBadge = useStatusBadge(status)

    expect(statusBadge.value.label).toBe("Veröffentlicht")
  })
})
