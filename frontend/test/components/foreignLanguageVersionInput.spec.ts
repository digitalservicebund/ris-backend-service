import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { beforeEach, vi } from "vitest"
import ForeignLanguageVersionInput from "@/components/ForeignLanguageVersionInput.vue"
import ForeignLanguageVersion, {
  LanguageCode,
} from "@/domain/foreignLanguageVersion"

const server = setupServer(
  http.get("/api/v1/caselaw/languagecodes", () => {
    const languageCode: LanguageCode = { id: "id", label: "Englisch" }
    return HttpResponse.json([languageCode])
  }),
)

function renderComponent(options?: { modelValue?: ForeignLanguageVersion }) {
  const user = userEvent.setup()
  const utils = render(ForeignLanguageVersionInput, {
    props: {
      modelValue: new ForeignLanguageVersion({ ...options?.modelValue }),
    },
  })
  return { user, ...utils }
}

describe("Foreign Langauge Version Input", () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })
  beforeAll(() => {
    server.listen()
  })
  afterAll(() => {
    server.close()
  })

  it("should disable 'Übernehmen' button with empty input", async () => {
    // Arrange + Act
    renderComponent()

    // Assert
    expect(
      screen.getByTestId("foreign-language-version-language"),
    ).toBeInTheDocument()
    expect(
      screen.getByTestId("foreign-language-version-link"),
    ).toBeInTheDocument()
    expect(
      screen.getByRole("button", { name: "Fremdsprachige Fassung speichern" }),
    ).toBeDisabled()
  })

  it("should disable 'Übernehmen' button with incomplete input", async () => {
    // Arrange + Act
    renderComponent({
      modelValue: {
        id: "id",
        link: "https://link-to-tranlsation.en",
      } as ForeignLanguageVersion,
    })
    const language = screen.getByRole("textbox", {
      name: /sprache/i,
    })
    const link = screen.getByRole("textbox", {
      name: /link/i,
    })

    // Assert
    expect(language).toHaveValue("")
    expect(link).toHaveValue("https://link-to-tranlsation.en")
    expect(
      screen.getByRole("button", { name: "Fremdsprachige Fassung speichern" }),
    ).toBeDisabled()
  })

  it("should enable 'Übernehmen' button with complete input", async () => {
    // Arrange + Act
    renderComponent({
      modelValue: {
        id: "id",
        languageCode: {
          id: "id",
          label: "Englisch",
        } as LanguageCode,
        link: "https://link-to-tranlsation.en",
      } as ForeignLanguageVersion,
    })
    const language = screen.getByRole("textbox", {
      name: /sprache/i,
    })
    const link = screen.getByRole("textbox", {
      name: /link/i,
    })

    // Assert
    expect(language).toHaveValue("Englisch")
    expect(link).toHaveValue("https://link-to-tranlsation.en")
    expect(
      screen.getByRole("button", { name: "Fremdsprachige Fassung speichern" }),
    ).toBeEnabled()
  })

  it("should emit addEntry event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "id",
        languageCode: {
          id: "id",
          label: "Englisch",
        } as LanguageCode,
        link: "https://link-to-tranlsation.en",
      } as ForeignLanguageVersion,
    })

    // Act
    await user.click(
      screen.getByRole("button", { name: "Fremdsprachige Fassung speichern" }),
    )

    // Assert
    expect(emitted("addEntry")).toBeTruthy()
  })

  it("should emit update:modelValue event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "id",
        languageCode: {
          id: "id",
          label: "Englisch",
        } as LanguageCode,
        link: "https://link-to-tranlsation.en",
      } as ForeignLanguageVersion,
    })

    // Act
    await user.click(
      screen.getByRole("button", { name: "Fremdsprachige Fassung speichern" }),
    )

    // Assert
    expect(emitted("update:modelValue")).toBeTruthy()
  })

  it("should emit cancelEdit event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "id",
        languageCode: {
          id: "id",
          label: "Englisch",
        } as LanguageCode,
        link: "https://link-to-tranlsation.en",
      } as ForeignLanguageVersion,
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
        id: "id",
        languageCode: {
          id: "id",
          label: "Englisch",
        } as LanguageCode,
        link: "https://link-to-tranlsation.en",
      } as ForeignLanguageVersion,
    })

    // Act
    await user.click(screen.getByRole("button", { name: "Eintrag löschen" }))

    // Assert
    expect(emitted("removeEntry")).toBeTruthy()
  })
})
