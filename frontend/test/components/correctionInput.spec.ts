import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { beforeEach, vi } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import CorrectionInput from "@/components/CorrectionInput.vue"
import Correction from "@/domain/correction"
import PendingProceeding from "@/domain/pendingProceeding"
import routes from "~/test-helper/routes"

function renderComponent(options?: { modelValue?: Correction }) {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  const utils = render(CorrectionInput, {
    props: {
      modelValue: new Correction({ ...options?.modelValue }),
      registerTextEditorRef: () => {},
    },
    global: {
      plugins: [
        [
          createTestingPinia({
            initialState: {
              session: { user: { internal: true } },
              docunitStore: {
                documentUnit: new PendingProceeding("foo", {
                  documentNumber: "1234567891234",
                }),
              },
            },
          }),
        ],
        [router],
      ],
    },
  })
  return {
    user,
    ...utils,
  }
}

describe("Correction Input", () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it("should disable 'Übernehmen' button with empty input", async () => {
    // Arrange + Act
    renderComponent()

    // Assert
    expect(
      screen.getByRole("button", { name: "Berichtigung speichern" }),
    ).toBeDisabled()
  })

  it("should disable 'Übernehmen' button with incomplete input", async () => {
    // Arrange + Act
    renderComponent({
      modelValue: {
        id: "d7880d24-67be-4e5a-8930-0e37312478e1",
        description: "Hauffen -> Haufen",
      } as Correction,
    })

    // Assert
    expect(
      screen.getByRole("button", { name: "Berichtigung speichern" }),
    ).toBeDisabled()
  })

  it("should enable 'Übernehmen' button with complete input", async () => {
    // Arrange + Act
    renderComponent({
      modelValue: {
        id: "920f16ce-61ef-4998-9fc1-5409434cc833",
        type: "Schreibfehlerberichtigung",
        description: "Hauffen -> Haufen",
      } as Correction,
    })

    // Assert
    expect(
      screen.getByRole("button", { name: "Berichtigung speichern" }),
    ).toBeEnabled()
  })

  it("should emit addEntry event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "b81e45f1-bfdc-4969-aac8-2a3498532fdd",
        type: "Schreibfehlerberichtigung",
        description: "Hauffen -> Haufen",
      } as Correction,
    })

    // Act
    await user.click(
      screen.getByRole("button", { name: "Berichtigung speichern" }),
    )

    // Assert
    expect(emitted("addEntry")).toBeTruthy()
  })

  it("should emit update:modelValue event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "b81e45f1-bfdc-4969-aac8-2a3498532fdd",
        type: "Schreibfehlerberichtigung",
        description: "Hauffen -> Haufen",
      } as Correction,
    })

    // Act
    await user.click(
      screen.getByRole("button", { name: "Berichtigung speichern" }),
    )

    // Assert
    expect(emitted("update:modelValue")).toBeTruthy()
  })

  it("should emit cancelEdit event", async () => {
    // Arrange
    const { user, emitted } = renderComponent({
      modelValue: {
        id: "b81e45f1-bfdc-4969-aac8-2a3498532fdd",
        type: "Schreibfehlerberichtigung",
      } as Correction,
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
        id: "b81e45f1-bfdc-4969-aac8-2a3498532fdd",
        type: "Schreibfehlerberichtigung",
      } as Correction,
    })

    // Act
    await user.click(screen.getByRole("button", { name: "Eintrag löschen" }))

    // Assert
    expect(emitted("removeEntry")).toBeTruthy()
  })
})
