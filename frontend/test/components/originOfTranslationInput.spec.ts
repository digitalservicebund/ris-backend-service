import { createTestingPinia } from "@pinia/testing"
import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { setActivePinia } from "pinia"
import { beforeEach } from "vitest"
import OriginOfTranslationInput from "@/components/OriginOfTranslationInput.vue"
import OriginOfTranslation, { LanguageCode } from "@/domain/originOfTranslation"

const server = setupServer(
  http.get("/api/v1/caselaw/languagecodes", () => {
    const languageCode: LanguageCode = { id: "id", label: "Englisch" }
    return HttpResponse.json([languageCode])
  }),
)

function renderComponent(options?: {
  modelValue?: OriginOfTranslation
  modelValueList?: OriginOfTranslation[]
}) {
  const user = userEvent.setup()
  const utils = render(OriginOfTranslationInput, {
    props: {
      modelValue: new OriginOfTranslation({ ...options?.modelValue }),
      modelValueList: options?.modelValueList,
    },
  })
  return { user, ...utils }
}

describe("OriginOfTranslationInput", () => {
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

  it("renders language combobox and chips inputs", () => {
    renderComponent()

    expect(
      screen.getByTestId("origin-of-translation-language"),
    ).toBeInTheDocument()
    expect(
      screen.getByTestId("origin-of-translation-translators"),
    ).toBeInTheDocument()
    expect(
      screen.getByTestId("origin-of-translation-border-numbers"),
    ).toBeInTheDocument()
    expect(
      screen.getByTestId("origin-of-translation-translation-type"),
    ).toBeInTheDocument()
    expect(screen.getByTestId("origin-of-translation-urls")).toBeInTheDocument()
  })

  it("disables 'Übernehmen' button with empty input", async () => {
    // Arrange + Act
    renderComponent()

    // Assert
    expect(
      screen.getByRole("button", {
        name: "Herkunft der Übersetzung speichern",
      }),
    ).toBeDisabled()
  })

  it("disables 'Übernehmen' button with incomplete input", async () => {
    // Arrange + Act
    renderComponent({
      modelValue: {
        id: "id",
      } as OriginOfTranslation,
    })
    const language = screen.getByRole("combobox", {
      name: /sprache/i,
    })

    // Assert
    expect(language).toHaveValue("")
    expect(
      screen.getByRole("button", {
        name: "Herkunft der Übersetzung speichern",
      }),
    ).toBeDisabled()
  })

  it("enables 'Übernehmen' button with complete input", async () => {
    // Arrange + Act
    renderComponent({
      modelValue: {
        id: "id",
        languageCode: {
          id: "id",
          label: "Englisch",
        } as LanguageCode,
      } as OriginOfTranslation,
    })
    const language = screen.getByRole("combobox", {
      name: /sprache/i,
    })

    // Assert
    expect(language).toHaveValue("Englisch")
    expect(
      screen.getByRole("button", {
        name: "Herkunft der Übersetzung speichern",
      }),
    ).toBeEnabled()
  })

  it("emits addEntry event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "id",
        languageCode: {
          id: "id",
          label: "Englisch",
        } as LanguageCode,
      } as OriginOfTranslation,
    })

    // Act
    await user.click(
      screen.getByRole("button", {
        name: "Herkunft der Übersetzung speichern",
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
        languageCode: {
          id: "id",
          label: "Englisch",
        } as LanguageCode,
      } as OriginOfTranslation,
    })

    // Act
    await user.click(
      screen.getByRole("button", {
        name: "Herkunft der Übersetzung speichern",
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
        languageCode: {
          id: "id",
          label: "Englisch",
        } as LanguageCode,
      } as OriginOfTranslation,
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
        languageCode: {
          id: "id",
          label: "Englisch",
        } as LanguageCode,
      } as OriginOfTranslation,
    })

    // Act
    await user.click(screen.getByRole("button", { name: "Eintrag löschen" }))

    // Assert
    expect(emitted("removeEntry")).toBeTruthy()
  })
})
