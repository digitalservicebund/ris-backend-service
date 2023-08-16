import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import Pagination from "@/shared/components/Pagination.vue"

function renderComponent(options?: {
  currentPage?: number
  getInitialData?: boolean
  navigationPosition?: "top" | "bottom"
  totalElements?: number
  totalPages?: number
  last?: boolean
  first?: boolean
}) {
  return render(Pagination, {
    props: {
      page: {
        content: [1, 2, 3, 4, 5],
        size: 100,
        totalElements: options?.totalElements ?? 5,
        totalPages: options?.totalPages ?? 1,
        number: options?.currentPage ?? 0,
        numberOfElements: options?.totalElements ?? 5,
        first: options?.first ?? true,
        last: options?.last ?? true,
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

    for (const element of ["zurück", "1 von 1", "vor", "Total 5 Items"]) {
      await screen.findByText(element)
    }
  })

  test("pagination is at bottom position", async () => {
    renderComponent({ navigationPosition: "bottom" })

    const slot = screen.getByText("list slot")
    const navigation = await screen.findByText("vor")

    expect(slot.compareDocumentPosition(navigation)).toBe(4)
  })

  test("pagination is on top", async () => {
    renderComponent({ navigationPosition: "top" })

    const slot = screen.getByText("list slot")
    const navigation = await screen.findByText("vor")

    expect(slot.compareDocumentPosition(navigation)).toBe(2)
  })

  test("displays correct max Items", async () => {
    renderComponent({ getInitialData: true, totalElements: 1337 })

    await screen.findByText("Total 1337 Items")
  })

  test("displays correct max Pages", async () => {
    renderComponent({ totalPages: 200 })

    await screen.findByText("1 von 200")
  })

  test("next button disabled if on last page", async () => {
    renderComponent({ last: true })

    const nextButton = await screen.findByLabelText("nächste Ergebnisse")
    expect(nextButton).toBeDisabled()
  })

  test("next button enabled if not on last page", async () => {
    renderComponent({ last: false })

    const nextButton = await screen.findByLabelText("nächste Ergebnisse")
    expect(nextButton).toBeEnabled()
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
