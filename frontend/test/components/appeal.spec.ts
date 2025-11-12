import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { http, HttpResponse } from "msw"
import { setupServer } from "msw/node"
import { setActivePinia } from "pinia"
import { vi } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import AppealComponent from "@/components/Appeal.vue"
import { Appeal, AppealWithdrawal, PkhPlaintiff } from "@/domain/appeal"
import { Decision } from "@/domain/decision"

import routes from "~/test-helper/routes"

const server = setupServer(
  http.get("/api/v1/caselaw/appeal/appellants", () => {
    const appellants = [
      { id: "1", value: "Kläger" },
      { id: "2", value: "Beklagter" },
      { id: "3", value: "Sonstiger" },
      { id: "4", value: "Keine Angabe" },
    ]
    return HttpResponse.json(appellants)
  }),
  http.get("/api/v1/caselaw/appeal/statuses", () => {
    const statuses = [
      { id: "1", value: "unbegründet" },
      { id: "2", value: "unzulässig" },
      { id: "3", value: "sonstiges" },
      { id: "4", value: "keine Angabe" },
    ]
    return HttpResponse.json(statuses)
  }),
)

function renderComponent(appeal?: Appeal) {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(AppealComponent, {
      props: { label: "Rechtsmittel" },
      global: {
        plugins: [
          [
            createTestingPinia({
              initialState: {
                docunitStore: {
                  documentUnit: new Decision("foo", {
                    documentNumber: "1234567891234",
                    contentRelatedIndexing: {
                      appeal: appeal,
                    },
                  }),
                },
              },
              stubActions: false,
            }),
          ],
          [router],
        ],
      },
    }),
  }
}

describe("appeal", () => {
  beforeAll(() => server.listen())
  afterAll(() => server.close())
  beforeEach(() => {
    vi.resetModules()
    vi.resetAllMocks()
    setActivePinia(createTestingPinia())
  })
  it("display empty appeal", async () => {
    renderComponent()

    expect(screen.queryByTestId("appellants")).toHaveTextContent(
      "Bitte auswählen",
    )
    expect(screen.queryByTestId("revision-defendant")).toHaveTextContent(
      "Bitte auswählen",
    )
    expect(screen.queryByTestId("revision-plaintiff")).toHaveTextContent(
      "Bitte auswählen",
    )
    expect(screen.queryByTestId("joint-revision-defendant")).toHaveTextContent(
      "Bitte auswählen",
    )
    expect(screen.queryByTestId("joint-revision-plaintiff")).toHaveTextContent(
      "Bitte auswählen",
    )
    expect(screen.queryByTestId("nzb-defendant")).toHaveTextContent(
      "Bitte auswählen",
    )
    expect(screen.queryByTestId("nzb-plaintiff")).toHaveTextContent(
      "Bitte auswählen",
    )
    expect(screen.queryByTestId("appeal-withdrawal")).toHaveTextContent(
      "Bitte auswählen",
    )
    expect(screen.queryByTestId("pkh-plaintiff")).toHaveTextContent(
      "Bitte auswählen",
    )
  })

  it("add appellants via dropdown", async () => {
    const { user } = renderComponent()

    expect(screen.queryByTestId("appellants")).toHaveTextContent(
      "Bitte auswählen",
    )

    await user.click(screen.getByTestId("appellants"))
    expect(screen.getByLabelText("Kläger")).toBeInTheDocument()
    expect(screen.getByLabelText("Beklagter")).toBeInTheDocument()
    expect(screen.getByLabelText("Sonstiger")).toBeInTheDocument()
    expect(screen.getByLabelText("Keine Angabe")).toBeInTheDocument()

    await user.click(screen.getByLabelText("Kläger"))
    await user.click(screen.getByLabelText("Sonstiger"))
    await user.click(screen.getByTestId("appellants")) //close dropdown

    expect(screen.queryByTestId("appellants")).toHaveTextContent("Kläger")
    expect(screen.queryByTestId("appellants")).toHaveTextContent("Sonstiger")
    expect(screen.queryByLabelText("Beklagter")).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Keine Angabe")).not.toBeInTheDocument()
  })

  it.each([
    "revision-defendant",
    "revision-plaintiff",
    "joint-revision-defendant",
    "joint-revision-plaintiff",
    "nzb-defendant",
    "nzb-plaintiff",
  ])("adds appeal status for %s via dropdown", async (testId) => {
    const { user } = renderComponent()

    expect(screen.queryByTestId(testId)).toHaveTextContent("Bitte auswählen")

    await user.click(screen.getByTestId(testId))
    expect(screen.getByLabelText("unbegründet")).toBeInTheDocument()
    expect(screen.getByLabelText("unzulässig")).toBeInTheDocument()
    expect(screen.getByLabelText("sonstiges")).toBeInTheDocument()
    expect(screen.getByLabelText("keine Angabe")).toBeInTheDocument()

    await user.click(screen.getByLabelText("unbegründet"))
    await user.click(screen.getByLabelText("sonstiges"))
    await user.click(screen.getByTestId(testId)) //close dropdown

    expect(screen.queryByTestId(testId)).toHaveTextContent("unbegründet")
    expect(screen.queryByTestId(testId)).toHaveTextContent("sonstiges")
    expect(screen.queryByLabelText("unzulässig")).not.toBeInTheDocument()
    expect(screen.queryByLabelText("keine Angabe")).not.toBeInTheDocument()
  })

  it("set appeal withdrawal", async () => {
    const { user } = renderComponent()

    await user.click(screen.getByTestId("appeal-withdrawal"))
    expect(screen.getByLabelText("Ja")).toBeInTheDocument()
    expect(screen.getByLabelText("Nein")).toBeInTheDocument()
    expect(screen.getByLabelText("Keine Angabe")).toBeInTheDocument()

    await user.click(screen.getByLabelText("Ja"))
    expect(screen.queryByTestId("appeal-withdrawal")).toHaveTextContent("Ja")
    expect(screen.queryByLabelText("Nein")).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Keine Angabe")).not.toBeInTheDocument()
  })

  it("set pkh plaintiff", async () => {
    const { user } = renderComponent()

    await user.click(screen.getByTestId("pkh-plaintiff"))
    expect(screen.getByLabelText("Ja")).toBeInTheDocument()
    expect(screen.getByLabelText("Nein")).toBeInTheDocument()
    expect(screen.getByLabelText("Keine Angabe")).toBeInTheDocument()

    await user.click(screen.getByLabelText("Ja"))
    expect(screen.queryByTestId("pkh-plaintiff")).toHaveTextContent("Ja")
    expect(screen.queryByLabelText("Nein")).not.toBeInTheDocument()
    expect(screen.queryByLabelText("Keine Angabe")).not.toBeInTheDocument()
  })

  it("display non-empty appeal", async () => {
    renderComponent({
      appellants: [{ id: "1", value: "Kläger" }],
      revisionDefendantStatuses: [{ id: "1", value: "unbegründet" }],
      revisionPlaintiffStatuses: [{ id: "2", value: "unzulässig" }],
      jointRevisionDefendantStatuses: [
        { id: "1", value: "unbegründet" },
        { id: "2", value: "unzulässig" },
      ],
      jointRevisionPlaintiffStatuses: [{ id: "1", value: "unbegründet" }],
      nzbDefendantStatuses: [{ id: "2", value: "unzulässig" }],
      nzbPlaintiffStatuses: [
        { id: "1", value: "unbegründet" },
        { id: "2", value: "unzulässig" },
        { id: "3", value: "sonstiges" },
      ],
      appealWithdrawal: AppealWithdrawal.JA,
      pkhPlaintiff: PkhPlaintiff.NEIN,
    })

    // Need to wait for Wertetabelle to be loaded in OnMounted
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(screen.queryByTestId("appellants")).toHaveTextContent("Kläger")
    expect(screen.queryByTestId("revision-defendant")).toHaveTextContent(
      "unbegründet",
    )
    expect(screen.queryByTestId("revision-plaintiff")).toHaveTextContent(
      "unzulässig",
    )
    expect(screen.queryByTestId("joint-revision-defendant")).toHaveTextContent(
      "unbegründet",
    )
    expect(screen.queryByTestId("joint-revision-defendant")).toHaveTextContent(
      "unzulässig",
    )
    expect(screen.queryByTestId("joint-revision-plaintiff")).toHaveTextContent(
      "unbegründet",
    )
    expect(screen.queryByTestId("nzb-defendant")).toHaveTextContent(
      "unzulässig",
    )
    expect(screen.queryByTestId("nzb-plaintiff")).toHaveTextContent(
      "unbegründet",
    )
    expect(screen.queryByTestId("nzb-plaintiff")).toHaveTextContent(
      "unzulässig",
    )
    expect(screen.queryByTestId("nzb-plaintiff")).toHaveTextContent("sonstiges")
    expect(screen.queryByTestId("appeal-withdrawal")).toHaveTextContent("Ja")
    expect(screen.queryByTestId("pkh-plaintiff")).toHaveTextContent("Nein")
  })
})
