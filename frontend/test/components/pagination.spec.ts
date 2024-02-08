import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import Pagination from "@/components/Pagination.vue"

function renderComponent(options?: {
  currentPage?: number
  getInitialData?: boolean
  navigationPosition?: "top" | "bottom"
  last?: boolean
  first?: boolean
  empty?: boolean
  numberOfElements?: number
}) {
  return render(Pagination, {
    props: {
      page: {
        content: [1, 2, 3, 4, 5],
        size: 100,
        numberOfElements: options?.numberOfElements ?? 100,
        number: options?.currentPage ?? 0,
        first: options?.first ?? true,
        last: options?.last ?? false,
        empty: options?.empty ?? false,
      },
      ...(options?.navigationPosition
        ? { navigationPosition: options.navigationPosition }
        : {}),
    },
    slots: { default: "<div>list slot</div>" },
  })
}

describe("Pagination", () => {
  test("display navigation", async () => {
    renderComponent()

    for (const element of ["Zurück", "Weiter"]) {
      await screen.findByText(element)
    }
  })

  test("pagination is at bottom position", async () => {
    renderComponent({ navigationPosition: "bottom" })

    const slot = screen.getByText("list slot")
    const navigation = await screen.findByText("Weiter")

    expect(slot.compareDocumentPosition(navigation)).toBe(4)
  })

  test("pagination is on top", async () => {
    renderComponent({ navigationPosition: "top" })

    const slot = screen.getByText("list slot")
    const navigation = await screen.findByText("Zurück")

    expect(slot.compareDocumentPosition(navigation)).toBe(2)
  })

  test("next button disabled if on last page", async () => {
    renderComponent({ first: false, last: true })

    const nextButton = await screen.findByLabelText("nächste Ergebnisse")
    expect(nextButton).toBeDisabled()
  })

  test("next button enabled if not on last page", async () => {
    renderComponent({ last: false })

    const nextButton = await screen.findByLabelText("nächste Ergebnisse")
    expect(nextButton).toBeEnabled()
  })

  test("number of results and no buttons shown for total one page", async () => {
    renderComponent({ first: true, last: true })

    const resultText = await screen.findByText("100 Ergebnisse gefunden")
    expect(resultText).toBeVisible()

    const nextButton = screen.queryByLabelText("nächste Ergebnisse")
    expect(nextButton).not.toBeInTheDocument()
    const previousButton = screen.queryByLabelText("vorherige Ergebnisse")
    expect(previousButton).not.toBeInTheDocument()
  })

  test("number of results shown for first of multiple pages", async () => {
    renderComponent({ first: true, last: false })

    const pageText = await screen.findByText("Seite 1: ", { trim: false })
    expect(pageText).toBeVisible()
    const resultText = await screen.findByText("100 Ergebnisse angezeigt")
    expect(resultText).toBeVisible()
  })

  test("exactly one result shown in single case", async () => {
    renderComponent({ first: true, last: true, numberOfElements: 1 })

    const resultText = await screen.findByText("1 Ergebnis gefunden")
    expect(resultText).toBeVisible()
  })

  test("previous button disabled if on first page", async () => {
    renderComponent({ first: true })

    const previousButton = await screen.findByLabelText("vorherige Ergebnisse")
    expect(previousButton).toBeDisabled()
  })

  test("previous button enabled if not first page", async () => {
    renderComponent({ first: false })

    const previousButton = await screen.findByLabelText("vorherige Ergebnisse")
    expect(previousButton).toBeEnabled()
  })

  test("emits correct event at click on next Page", async () => {
    const { emitted } = renderComponent({
      last: false,
      first: false,
      currentPage: 3,
    })

    const user = userEvent.setup()
    await user.click(await screen.findByLabelText("nächste Ergebnisse"))
    expect(emitted()["updatePage"]).toHaveLength(1)
    expect(emitted()["updatePage"][0]).toEqual([4])

    await user.click(await screen.findByLabelText("vorherige Ergebnisse"))
    expect(emitted()["updatePage"]).toHaveLength(2)
    expect(emitted()["updatePage"][1]).toEqual([2])
  })
})
