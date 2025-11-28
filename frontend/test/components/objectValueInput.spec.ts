import { createTestingPinia } from "@pinia/testing"
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { setActivePinia } from "pinia"
import { beforeEach } from "vitest"
import ObjectValueInput from "@/components/ObjectValueInput.vue"
import ObjectValue, { CurrencyCode } from "@/domain/objectValue"

const server = setupServer(
  http.get("/api/v1/caselaw/currencycodes", () => {
    const currencyCode: CurrencyCode = { id: "id", label: "Euro (EUR)" }
    return HttpResponse.json([currencyCode])
  }),
)

function renderComponent(options?: {
  modelValue?: ObjectValue
  modelValueList?: ObjectValue[]
}) {
  const user = userEvent.setup()
  const utils = render(ObjectValueInput, {
    props: {
      modelValue: new ObjectValue({ ...options?.modelValue }),
      modelValueList: options?.modelValueList,
    },
  })
  return { user, ...utils }
}

describe("ObjectValueInput", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())

    vi.restoreAllMocks()
  })
  beforeAll(() => {
    server.listen()
  })
  afterAll(() => {
    server.close()
  })

  it("renders currency combobox and chips inputs", () => {
    renderComponent()

    expect(screen.getByTestId("object-value-amount")).toBeInTheDocument()
    expect(screen.getByTestId("object-value-currency")).toBeInTheDocument()
    expect(
      screen.getByTestId("object-value-proceeding-type"),
    ).toBeInTheDocument()
  })

  it("disables 'Übernehmen' button with empty input", async () => {
    // Arrange + Act
    renderComponent()

    // Assert
    expect(
      screen.getByRole("button", {
        name: "Gegenstandswert speichern",
      }),
    ).toBeDisabled()
  })

  it("disables 'Übernehmen' button with incomplete input", async () => {
    // Arrange + Act
    renderComponent({
      modelValue: {
        id: "id",
      } as ObjectValue,
    })

    const amount = screen.getByLabelText("Betrag", { exact: true })
    const currency = screen.getByLabelText("Währung", { exact: true })

    // Assert
    expect(amount).toHaveValue("")
    expect(currency).toHaveValue("")
    expect(
      screen.getByRole("button", {
        name: "Gegenstandswert speichern",
      }),
    ).toBeDisabled()
  })

  it("enables 'Übernehmen' button with complete input", async () => {
    // Arrange + Act
    renderComponent({
      modelValue: {
        id: "id",
        amount: 10000,
        currencyCode: {
          id: "id",
          label: "Euro (EUR)",
        } as CurrencyCode,
      } as ObjectValue,
    })

    const amount = screen.getByLabelText("Betrag", { exact: true })
    const currency = screen.getByLabelText("Währung", { exact: true })

    // Assert
    expect(amount).toHaveValue("10.000")
    expect(currency).toHaveValue("Euro (EUR)")
    expect(
      screen.getByRole("button", {
        name: "Gegenstandswert speichern",
      }),
    ).toBeEnabled()
  })

  it("emits addEntry event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "id",
        amount: 10000,
        currencyCode: {
          id: "id",
          label: "Euro (EUR)",
        } as CurrencyCode,
      } as ObjectValue,
    })

    // Act
    await user.click(
      screen.getByRole("button", {
        name: "Gegenstandswert speichern",
      }),
    )

    // Assert
    expect(emitted("addEntry")).toBeTruthy()
  })

  it("emits update:modelValue event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "id",
        amount: 10000,
        currencyCode: {
          id: "id",
          label: "Euro (EUR)",
        } as CurrencyCode,
      } as ObjectValue,
    })

    // Act
    await user.click(
      screen.getByRole("button", {
        name: "Gegenstandswert speichern",
      }),
    )

    // Assert
    expect(emitted("update:modelValue")).toBeTruthy()
  })

  it("emits cancelEdit event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "id",
        amount: 10000,
        currencyCode: {
          id: "id",
          label: "Euro (EUR)",
        } as CurrencyCode,
      } as ObjectValue,
    })

    // Act
    await user.click(screen.getByRole("button", { name: "Abbrechen" }))

    // Assert
    expect(emitted("cancelEdit")).toBeTruthy()
  })

  it("emits removeEntry event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "id",
        currencyCode: {
          id: "id",
          label: "Euro (EUR)",
        } as CurrencyCode,
      } as ObjectValue,
    })

    // Act
    await user.click(screen.getByRole("button", { name: "Eintrag löschen" }))

    // Assert
    expect(emitted("removeEntry")).toBeTruthy()
  })
})
