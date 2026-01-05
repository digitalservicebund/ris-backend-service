import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { beforeEach, vi } from "vitest"
import CountryOfOriginInput from "@/components/CountryOfOriginInput.vue"
import CountryOfOrigin from "@/domain/countryOfOrigin"
import { FieldOfLaw } from "@/domain/fieldOfLaw"

const server = setupServer(
  http.get(
    "/api/v1/caselaw/fieldsoflaw/search-by-identifier",
    ({ request }) => {
      const country: FieldOfLaw = {
        children: [],
        hasChildren: false,
        norms: [],
        identifier: "RE-07-DEU",
        text: "Deutschland",
      }

      const fieldOfLaw: FieldOfLaw = {
        children: [],
        hasChildren: false,
        norms: [],
        identifier: "AR-01-01-01",
        text: "Verschulden bei Vertragsschluss (culpa in contrahendo)",
      }

      if (new URL(request.url).searchParams.get("q") === "RE-07-") {
        return HttpResponse.json([country])
      }

      return HttpResponse.json([country, fieldOfLaw])
    },
  ),
)

function renderComponent(options?: { modelValue?: CountryOfOrigin }) {
  const user = userEvent.setup()
  const utils = render(CountryOfOriginInput, {
    props: {
      modelValue: new CountryOfOrigin({ ...options?.modelValue }),
    },
  })
  return { user, ...utils }
}

describe("Country of Origin Input", () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })
  beforeAll(() => {
    server.listen()
  })
  afterAll(() => {
    server.close()
  })

  it("should not show countries for Rechtlicher Rahmen", async () => {
    // Arrange + Act
    const { user } = renderComponent()

    // Assert
    await user.type(
      screen.getByRole("combobox", { name: "Rechtlicher Rahmen" }),
      "RE-07-DEU",
    )
    expect(screen.queryByText("RE-07-DEU")).not.toBeInTheDocument()

    await user.type(
      screen.getByRole("combobox", { name: "Rechtlicher Rahmen" }),
      "AR",
    )
    expect(screen.queryByText("AR-01-01-01")).toBeVisible()
  })

  it("should show only countries for Landbezeichnung", async () => {
    // Arrange + Act
    const { user } = renderComponent()

    // Assert
    await user.type(
      screen.getByRole("combobox", { name: "Landbezeichnung" }),
      "D",
    )
    expect(screen.queryByText("RE-07-DEU")).toBeVisible()

    await user.type(
      screen.getByRole("combobox", { name: "Landbezeichnung" }),
      "AR",
    )
    expect(screen.queryByText("AR-01-01-01")).not.toBeInTheDocument()
  })

  it("should remove legacy value once country is selected", async () => {
    // Arrange + Act
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "347622c8-3881-45a5-87a8-260ebbc3ce34",
        legacyValue: "legacy value",
      } as CountryOfOrigin,
    })

    // Assert
    await user.type(
      screen.getByRole("combobox", { name: "Landbezeichnung" }),
      "D",
    )
    await user.click(screen.getByText("RE-07-DEU"))

    await user.click(
      screen.getByRole("button", { name: "Herkunftsland speichern" }),
    )

    // Assert
    const savedValue = emitted("update:modelValue")[0] as CountryOfOrigin[]
    expect(savedValue).toHaveLength(1)
    expect(savedValue[0].legacyValue).toBeUndefined()
    expect(savedValue[0].country?.identifier).toEqual("RE-07-DEU")
  })

  it("should disable 'Übernehmen' button with empty input", async () => {
    // Arrange + Act
    renderComponent()

    // Assert
    expect(
      screen.getByRole("combobox", { name: "Landbezeichnung" }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole("combobox", { name: "Rechtlicher Rahmen" }),
    ).toBeInTheDocument()
    expect(
      screen.getByRole("button", { name: "Herkunftsland speichern" }),
    ).toBeDisabled()
  })

  it("should disable 'Übernehmen' button with incomplete input", async () => {
    // Arrange + Act
    renderComponent({
      modelValue: {
        id: "6d665869-1c28-4ca6-8167-6934b5bff7e8",
        fieldOfLaw: {
          identifier: "AR-01-01-01",
          text: "Verschulden bei Vertragsschluss (culpa in contrahendo)",
        },
      } as CountryOfOrigin,
    })

    // Assert
    expect(
      screen.getByRole("combobox", { name: "Landbezeichnung" }),
    ).toHaveValue("")
    expect(
      screen.getByRole("combobox", { name: "Rechtlicher Rahmen" }),
    ).toHaveValue("AR-01-01-01")
    expect(
      screen.getByRole("button", { name: "Herkunftsland speichern" }),
    ).toBeDisabled()
  })

  it("should enable 'Übernehmen' button with complete input", async () => {
    // Arrange + Act
    renderComponent({
      modelValue: {
        id: "ebd262c0-343d-48fe-bdbc-e3d2da391aa1",
        fieldOfLaw: {
          identifier: "AR-01-01-01",
          text: "Verschulden bei Vertragsschluss (culpa in contrahendo)",
        },
        country: {
          identifier: "RE-07-DEU",
          text: "Deutschland",
        },
      } as CountryOfOrigin,
    })

    // Assert
    expect(
      screen.getByRole("combobox", { name: "Landbezeichnung" }),
    ).toHaveValue("RE-07-DEU")
    expect(
      screen.getByRole("combobox", { name: "Rechtlicher Rahmen" }),
    ).toHaveValue("AR-01-01-01")
    expect(
      screen.getByRole("button", { name: "Herkunftsland speichern" }),
    ).toBeEnabled()
  })

  it("should emit addEntry and update:modelValue events", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "ebd262c0-343d-48fe-bdbc-e3d2da391aa1",
        fieldOfLaw: {
          identifier: "AR-01-01-01",
          text: "Verschulden bei Vertragsschluss (culpa in contrahendo)",
        },
        country: {
          identifier: "RE-07-DEU",
          text: "Deutschland",
        },
      } as CountryOfOrigin,
    })

    // Act
    await user.click(
      screen.getByRole("button", { name: "Herkunftsland speichern" }),
    )

    // Assert
    expect(emitted("addEntry")).toBeTruthy()
    expect(emitted("update:modelValue")).toBeTruthy()
  })

  it("should emit cancelEdit event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "ebd262c0-343d-48fe-bdbc-e3d2da391aa1",
        fieldOfLaw: {
          identifier: "AR-01-01-01",
          text: "Verschulden bei Vertragsschluss (culpa in contrahendo)",
        },
        country: {
          identifier: "RE-07-DEU",
          text: "Deutschland",
        },
      } as CountryOfOrigin,
    })

    // Act
    await user.click(screen.getByRole("button", { name: "Abbrechen" }))

    // Assert
    expect(emitted("cancelEdit")).toBeTruthy()
  })

  it("should emit removeEntry event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "ebd262c0-343d-48fe-bdbc-e3d2da391aa1",
        fieldOfLaw: {
          identifier: "AR-01-01-01",
          text: "Verschulden bei Vertragsschluss (culpa in contrahendo)",
        },
        country: {
          identifier: "RE-07-DEU",
          text: "Deutschland",
        },
      } as CountryOfOrigin,
    })

    // Act
    await user.click(screen.getByRole("button", { name: "Eintrag löschen" }))

    // Assert
    expect(emitted("removeEntry")).toBeTruthy()
  })
})
