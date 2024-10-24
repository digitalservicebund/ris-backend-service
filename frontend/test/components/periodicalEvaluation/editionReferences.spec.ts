import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { fireEvent, render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import PeriodicalEditionReferences from "@/components/periodical-evaluation/references/PeriodicalEditionReferences.vue"
import LegalPeriodical from "@/domain/legalPeriodical"
import LegalPeriodicalEdition from "@/domain/legalPeriodicalEdition"
import { ServiceResponse } from "@/services/httpClient"
import service from "@/services/legalPeriodicalEditionService"
import testRoutes from "~/test-helper/routes"

const editionUUid = crypto.randomUUID()

async function renderComponent() {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: testRoutes,
  })

  // Mock the route with a specific uuid before rendering
  await router.push({
    name: "caselaw-periodical-evaluation-editionId-references",
    params: { editionId: editionUUid },
  })

  const legalPeriodical: LegalPeriodical = {
    uuid: "1",
    abbreviation: "BDZ",
    citationStyle: "2024, Heft 1",
  }
  const pinia = createTestingPinia({
    initialState: {
      editionStore: {
        edition: new LegalPeriodicalEdition({
          id: "1",
          legalPeriodical: legalPeriodical,
          name: "name",
          prefix: "präfix",
          suffix: "suffix",
          references: [],
        }),
      },
    },
    stubActions: false, // Ensure actions are not stubbed if you need to access them
  })

  return {
    user,
    ...render(PeriodicalEditionReferences, {
      global: {
        plugins: [router, pinia],
      },
    }),
  }
}

describe("Legal periodical edition evaluation", () => {
  beforeEach(async () => {
    const legalPeriodical: LegalPeriodical = {
      uuid: "1",
      abbreviation: "BDZ",
      citationStyle: "2024, Heft 1",
    }
    vi.spyOn(service, "get").mockImplementation(
      (): Promise<ServiceResponse<LegalPeriodicalEdition>> =>
        Promise.resolve({
          status: 200,
          data: new LegalPeriodicalEdition({
            id: editionUUid,
            legalPeriodical: legalPeriodical,
            name: "name",
            prefix: "präfix",
            suffix: "suffix",
            references: [],
          }),
        }),
    )
    vi.spyOn(service, "save").mockImplementation(
      (): Promise<ServiceResponse<LegalPeriodicalEdition>> =>
        Promise.resolve({
          status: 200,
          data: new LegalPeriodicalEdition({
            id: editionUUid,
            legalPeriodical: legalPeriodical,
            name: "name",
            prefix: "präfix",
            suffix: "suffix",
            references: [],
          }),
        }),
    )
  })

  it("reference supplement (Klammernzusatz) should display validation on blur and hide on focus", async () => {
    await renderComponent()
    const referenceSupplementInput = screen.getByLabelText("Klammernzusatz")
    expect(
      referenceSupplementInput,
      "should not have error while typing",
    ).not.toHaveClass("has-error")
    await fireEvent.blur(referenceSupplementInput)
    expect(
      referenceSupplementInput,
      "should have error if empty and unfocused",
    ).toHaveClass("has-error")
    await fireEvent.focus(referenceSupplementInput)
    expect(
      referenceSupplementInput,
      "should hide error while editing",
    ).not.toHaveClass("has-error")
    await fireEvent.blur(referenceSupplementInput)
    expect(
      referenceSupplementInput,
      "should have error if empty and unfocused",
    ).toHaveClass("has-error")
  })

  test("renders legal periodical reference input", async () => {
    await renderComponent()
    expect(
      screen.getByLabelText("Zitatstelle Präfix", { exact: true }),
    ).toHaveValue("präfix")
    expect(
      screen.getByLabelText("Zitatstelle Suffix", { exact: true }),
    ).toHaveValue("suffix")
    expect(screen.getByText("Zitierbeispiel: 2024, Heft 1")).toBeInTheDocument()
    expect(
      screen.getByLabelText("Gericht", { exact: true }),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Aktenzeichen", { exact: true }),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Entscheidungsdatum", { exact: true }),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Dokumenttyp", { exact: true }),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Zitatstelle *", { exact: true }),
    ).toBeInTheDocument()
    expect(
      screen.getByLabelText("Klammernzusatz", { exact: true }),
    ).toBeInTheDocument()
  })
})
