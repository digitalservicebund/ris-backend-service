import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import EditionHandover from "@/components/EditionHandover.vue"
import { HandoverMail, HandoverReport, Preview } from "@/domain/eventRecord"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import Reference from "@/domain/reference"
import handoverEditionService from "@/services/handoverEditionService"
import legalPeriodicalEditionService from "@/services/legalPeriodicalEditionService"
import routes from "~/test-helper/routes"

function renderComponent() {
  const user = userEvent.setup()
  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })

  return {
    user,
    ...render(EditionHandover, {
      global: {
        plugins: [
          createTestingPinia({
            initialState: {
              editionStore: {
                edition: new LegalPeriodicalEdition({
                  id: "123",
                  legalPeriodical: {
                    uuid: "456",
                    title: "foo",
                  },
                  references: [new Reference({ id: "789", citation: "AB, 3" })],
                }),
              },
            },
          }),
          [router],
        ],
      },
    }),
  }
}
describe("Edition Handover", () => {
  vi.spyOn(handoverEditionService, "getEventLog").mockImplementation(() =>
    Promise.resolve({
      status: 200,
      data: [new HandoverReport({})],
    }),
  )
  test("renders successfully", async () => {
    renderComponent()

    expect(
      screen.getByRole("heading", { name: "Überprüfung der Daten ..." }),
    ).toBeInTheDocument()
    expect(
      await screen.findByRole("heading", { name: "Übergabe an jDV" }),
    ).toBeInTheDocument()
    expect(
      await screen.findByRole("heading", { name: "Letzte Ereignisse" }),
    ).toBeInTheDocument()
  })

  test("renders error", async () => {
    vi.spyOn(handoverEditionService, "getEventLog").mockImplementation(() =>
      Promise.resolve({
        status: 300,
        error: {
          title: "Leider ist ein Fehler aufgetreten",
          description: "",
        },
      }),
    )
    renderComponent()

    expect(
      screen.getByRole("heading", { name: "Überprüfung der Daten ..." }),
    ).toBeInTheDocument()
    expect(
      await screen.findByRole("heading", { name: "Übergabe an jDV" }),
    ).toBeInTheDocument()
    expect(
      await screen.findByText("Leider ist ein Fehler aufgetreten"),
    ).toBeInTheDocument()
  })

  test.skip("renders handover result", async () => {
    vi.spyOn(handoverEditionService, "getEventLog").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: [new HandoverReport({})],
      }),
    )
    vi.spyOn(handoverEditionService, "handoverEdition").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: new HandoverMail({ success: true }),
      }),
    )
    vi.spyOn(handoverEditionService, "getPreview").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: [
          new Preview({
            xml: "<xml>all good</xml>",
            success: true,
          }),
        ],
      }),
    )
    vi.spyOn(legalPeriodicalEditionService, "get").mockImplementation(() =>
      Promise.resolve({
        status: 200,
        data: new LegalPeriodicalEdition({}),
      }),
    )
    const { user } = renderComponent()

    expect(
      screen.getByRole("heading", { name: "Überprüfung der Daten ..." }),
    ).toBeInTheDocument()
    expect(
      await screen.findByLabelText("Fundstellen der Ausgabe an jDV übergeben"),
    ).toBeInTheDocument()

    await user.click(
      screen.getByLabelText("Fundstellen der Ausgabe an jDV übergeben"),
    )

    // TODO
    // expect(
    //   await screen.findByLabelText("Erfolg der jDV Übergabe"),
    // ).toBeInTheDocument()
  })
})
