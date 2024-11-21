import { createTestingPinia } from "@pinia/testing"
import { fireEvent, render, screen } from "@testing-library/vue"
import { setActivePinia } from "pinia"
import ScheduledPublishingDateTime from "@/components/ScheduledPublishingDateTime.vue"
import DocumentUnit from "@/domain/documentUnit"
import { useDocumentUnitStore } from "@/stores/documentUnitStore"

function mockDocUnitStore(scheduledPublicationDateTime?: string) {
  const mockedSessionStore = useDocumentUnitStore()
  mockedSessionStore.documentUnit = new DocumentUnit("q834", {
    coreData: { scheduledPublicationDateTime },
  })

  return mockedSessionStore
}

describe("ScheduledPublishingDateTime", () => {
  beforeEach(() => setActivePinia(createTestingPinia()))

  it("should show empty default state if no date is set and doc unit is publishable", async () => {
    mockDocUnitStore()
    render(ScheduledPublishingDateTime, {
      props: { isPublishable: true },
    })

    const dateField = screen.getByLabelText("Terminiertes Datum")
    expect(dateField).toHaveValue("")
    expect(dateField).not.toHaveAttribute("readonly")

    const timeField = screen.getByLabelText("Terminierte Uhrzeit")
    expect(timeField).toHaveValue("05:00")
    expect(timeField).not.toHaveAttribute("readonly")

    expect(screen.getByLabelText("Termin setzen")).toBeDisabled()

    expect(
      screen.queryByTestId("scheduledPublishingDate_errors"),
    ).not.toBeInTheDocument()
  })

  it("should be readonly if docunit is not publishable and no scheduled date is set", async () => {
    mockDocUnitStore()
    render(ScheduledPublishingDateTime, {
      props: { isPublishable: false },
    })

    const dateField = screen.getByLabelText("Terminiertes Datum")
    expect(dateField).toHaveValue("")
    expect(dateField).toHaveAttribute("readonly")

    const timeField = screen.getByLabelText("Terminierte Uhrzeit")
    expect(timeField).toHaveValue("05:00")
    expect(dateField).toHaveAttribute("readonly")

    expect(screen.getByLabelText("Termin setzen")).toBeDisabled()

    expect(
      screen.queryByTestId("scheduledPublishingDate_errors"),
    ).not.toBeInTheDocument()
  })

  it("should render previously set publishing date", () => {
    mockDocUnitStore("2080-10-10T23:00:00.000Z")
    render(ScheduledPublishingDateTime, {
      props: { isPublishable: true },
    })

    const dateField = screen.getByLabelText("Terminiertes Datum")
    expect(dateField).toHaveValue("11.10.2080")
    expect(dateField).toHaveAttribute("readonly")

    const timeField = screen.getByLabelText("Terminierte Uhrzeit")
    expect(timeField).toHaveValue("01:00")
    expect(timeField).toHaveAttribute("readonly")

    expect(screen.queryByLabelText("Termin setzen")).not.toBeInTheDocument()

    expect(screen.queryByLabelText("Termin löschen")).toBeEnabled()

    expect(
      screen.queryByTestId("scheduledPublishingDate_errors"),
    ).not.toBeInTheDocument()
  })

  it("should set scheduling date", async () => {
    const store = mockDocUnitStore()
    render(ScheduledPublishingDateTime, {
      props: { isPublishable: true },
    })

    const dateField = screen.getByLabelText("Terminiertes Datum")
    await fireEvent.update(dateField, "01.01.2050")

    await fireEvent.click(screen.getByLabelText("Termin setzen"))

    expect(store.documentUnit?.coreData.scheduledPublicationDateTime).toEqual(
      "2050-01-01T04:00:00.000Z",
    )

    expect(dateField).toHaveValue("01.01.2050")
    expect(screen.getByLabelText("Terminierte Uhrzeit")).toHaveValue("05:00")
    expect(screen.getByLabelText("Termin löschen")).toBeEnabled()

    expect(
      screen.queryByTestId("scheduledPublishingDate_errors"),
    ).not.toBeInTheDocument()
  })

  it("should reset state after deleting scheduling", async () => {
    const store = mockDocUnitStore("2080-10-11T02:00:00.000Z")
    render(ScheduledPublishingDateTime, {
      props: { isPublishable: true },
    })

    await fireEvent.click(screen.getByLabelText("Termin löschen"))

    expect(
      store.documentUnit?.coreData.scheduledPublicationDateTime,
    ).toBeUndefined()

    const dateField = screen.getByLabelText("Terminiertes Datum")
    expect(dateField).toHaveValue("")
    expect(dateField).not.toHaveAttribute("readonly")

    const timeField = screen.getByLabelText("Terminierte Uhrzeit")
    expect(timeField).toHaveValue("05:00")
    expect(timeField).not.toHaveAttribute("readonly")

    expect(screen.getByLabelText("Termin setzen")).toBeDisabled()

    expect(
      screen.queryByTestId("scheduledPublishingDate_errors"),
    ).not.toBeInTheDocument()
  })

  it("should show error on invalid date", async () => {
    mockDocUnitStore()
    render(ScheduledPublishingDateTime, {
      props: { isPublishable: true },
    })

    const dateField = screen.getByLabelText("Terminiertes Datum")
    await fireEvent.update(dateField, "40.01.2050")

    expect(dateField).toHaveValue("40.01.2050")
    expect(screen.getByLabelText("Termin setzen")).toBeDisabled()

    expect(
      screen.getByTestId("scheduledPublishingDate_errors"),
    ).toBeInTheDocument()
    expect(screen.getByText("Kein valides Datum.")).toBeVisible()
  })

  it("should show error on incomplete date", async () => {
    mockDocUnitStore()
    render(ScheduledPublishingDateTime, {
      props: { isPublishable: true },
    })

    const dateField = screen.getByLabelText("Terminiertes Datum")
    await fireEvent.update(dateField, "40.01.205")
    await fireEvent.blur(dateField)

    expect(dateField).toHaveValue("40.01.205")
    expect(screen.getByLabelText("Termin setzen")).toBeDisabled()

    expect(
      screen.getByTestId("scheduledPublishingDate_errors"),
    ).toBeInTheDocument()
    expect(screen.getByText("Unvollständiges Datum.")).toBeVisible()
  })

  it("should show error on invalid time", async () => {
    mockDocUnitStore()
    render(ScheduledPublishingDateTime, {
      props: { isPublishable: true },
    })

    const timeField = screen.getByLabelText("Terminierte Uhrzeit")
    await fireEvent.update(timeField, "invalid")
    await fireEvent.blur(timeField)

    expect(timeField).toHaveValue("")
    expect(screen.getByLabelText("Termin setzen")).toBeDisabled()

    expect(
      screen.getByTestId("scheduledPublishingDate_errors"),
    ).toBeInTheDocument()
    expect(screen.getByText("Unvollständige Uhrzeit.")).toBeVisible()
  })

  it("should show error when scheduling date_time is not in the future", async () => {
    mockDocUnitStore()
    render(ScheduledPublishingDateTime, {
      props: { isPublishable: true },
    })

    const dateField = screen.getByLabelText("Terminiertes Datum")
    await fireEvent.update(dateField, "01.01.2024")

    expect(dateField).toHaveValue("01.01.2024")

    expect(screen.getByLabelText("Termin setzen")).toBeDisabled()

    expect(
      screen.getByTestId("scheduledPublishingDate_errors"),
    ).toBeInTheDocument()
    expect(
      screen.getByText(
        "Der Terminierungszeitpunkt muss in der Zukunft liegen.",
      ),
    ).toBeVisible()
  })

  it("should show error if scheduled date is set and doc unit is not publishable", () => {
    mockDocUnitStore("2080-10-11T03:00:00.000Z")
    render(ScheduledPublishingDateTime, {
      props: { isPublishable: false },
    })

    expect(screen.getByLabelText("Termin löschen")).toBeEnabled()

    expect(
      screen.getByTestId("scheduledPublishingDate_errors"),
    ).toBeInTheDocument()
    expect(
      screen.getByText(
        "Die terminierte Abgabe kann aufgrund von Fehlern in der Plausibilitätsprüfung nicht durchgeführt werden.",
      ),
    ).toBeVisible()
  })
})
