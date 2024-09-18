import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import DocumentUnitTextsNew from "@/components/texts/DocumentUnitTexts.vue"
import DocumentUnit from "@/domain/documentUnit"

describe("Texts", () => {
  test("renders all text fields with labels", async () => {
    render(DocumentUnitTextsNew, {
      global: {
        plugins: [
          [
            createTestingPinia({
              initialState: {
                session: { user: { roles: ["Internal"] } },
                docunitStore: {
                  documentUnit: new DocumentUnit("foo", {
                    documentNumber: "1234567891234",
                  }),
                },
              },
            }),
          ],
        ],
      },
    })
    screen.getByText("Entscheidungsname")
    screen.getByText("Titelzeile")
    screen.getByText("Leitsatz")
    screen.getByText("Orientierungssatz")
    screen.getByText("Sonstiger Orientierungssatz")
    screen.getByText("Tenor")
    screen.getByText("Gründe")
    screen.getByText("Tatbestand")
    screen.getByText("Entscheidungsgründe")
    screen.getByText("Abweichende Meinung")
    screen.getByText("Sonstiger Langtext")
    screen.getByText("Gliederung")
  })
})
