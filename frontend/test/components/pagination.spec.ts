import userEvent from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { defineComponent, ref } from "vue"
import { ServiceResponse } from "@/services/httpClient"
import Pagination, { Page } from "@/shared/components/Pagination.vue"

function returnServiceMock(options?: {
  totalElements?: number
  totalPages?: number
  last?: boolean
  first?: boolean
}): (page: number, size: number) => Promise<ServiceResponse<Page<number>>> {
  return async (page: number, size: number) =>
    Promise.resolve({
      status: 200,
      data: {
        content: [1, 2, 3, 4, 5],
        size: size,
        totalElements: options?.totalElements ?? 5,
        totalPages: options?.totalPages ?? 1,
        number: page,
        numberOfElements: options?.totalElements ?? 5,
        first: options?.first ?? true,
        last: options?.last ?? true,
      },
    })
}

function renderComponent(
  options?: {
    getInitialData?: boolean
    navigationPosition?: "top" | "bottom"
  },
  serviceOptions?: {
    totalElements?: number
    totalPages?: number
    last?: boolean
    first?: boolean
  }
) {
  return render(Pagination, {
    props: {
      itemService: returnServiceMock(serviceOptions),
      itemsPerPage: 10,
      ...(options?.getInitialData
        ? {
            getInitalData: options.getInitialData,
          }
        : {}),
      ...(options?.navigationPosition
        ? { navigationPosition: options.navigationPosition }
        : {}),
    },
    slots: { default: "<div>list slot</div>" },
  })
}

describe("Pagination", () => {
  test("does not render Pagination if no data", async () => {
    renderComponent()

    expect(screen.queryByText("zurück")).not.toBeInTheDocument()
    expect(screen.queryByText("1 von 1")).not.toBeInTheDocument()
    expect(screen.queryByText("vor")).not.toBeInTheDocument()
    expect(screen.queryByText("Total 5 Items")).not.toBeInTheDocument()
  })

  test("get's initial data", async () => {
    renderComponent({ getInitialData: true })

    await screen.findByText("zurück")
    await screen.findByText("1 von 1")
    await screen.findByText("vor")
    await screen.findByText("Total 5 Items")
  })

  test("pagination is at bottom position", async () => {
    renderComponent({ getInitialData: true, navigationPosition: "bottom" })

    const slot = screen.getByText("list slot")
    const navigation = await screen.findByText("vor")

    expect(slot.compareDocumentPosition(navigation)).toBe(4)
  })

  test("pagination is on top", async () => {
    renderComponent({ getInitialData: true, navigationPosition: "top" })

    const slot = screen.getByText("list slot")
    const navigation = await screen.findByText("vor")

    expect(slot.compareDocumentPosition(navigation)).toBe(2)
  })

  test("displays correct max Items", async () => {
    renderComponent({ getInitialData: true }, { totalElements: 1337 })

    await screen.findByText("Total 1337 Items")
  })

  test("displays correct max Pages", async () => {
    renderComponent({ getInitialData: true }, { totalPages: 200 })

    await screen.findByText("1 von 200")
  })

  test("next button disabled if on last page", async () => {
    renderComponent({ getInitialData: true }, { last: true })

    const nextButton = await screen.findByLabelText("nächste Ergebnisse")
    expect(nextButton).toBeDisabled()
  })

  test("next button enabled if not on last page", async () => {
    renderComponent({ getInitialData: true }, { last: false })

    const nextButton = await screen.findByLabelText("nächste Ergebnisse")
    expect(nextButton).toBeEnabled()
  })

  test("previous button disabled if on first page", async () => {
    renderComponent({ getInitialData: true }, { first: true })

    const previousButton = await screen.findByLabelText("vorherige Ergebnisse")
    expect(previousButton).toBeDisabled()
  })

  test("previous button enabled if not first page", async () => {
    renderComponent({ getInitialData: true }, { first: false })

    const previousButton = await screen.findByLabelText("vorherige Ergebnisse")
    expect(previousButton).toBeEnabled()
  })

  test("emits correct event at click on next Page", async () => {
    const { emitted } = renderComponent(
      { getInitialData: true },
      { last: false, first: false }
    )

    const user = userEvent.setup()
    await user.click(await screen.findByLabelText("nächste Ergebnisse"))
    expect(emitted()["updateItems"]).toHaveLength(2)

    await user.click(await screen.findByLabelText("vorherige Ergebnisse"))
    expect(emitted()["updateItems"]).toHaveLength(3)
  })

  test("expose update function to parent and call with correct arguments", async () => {
    const serviceMock = vi.fn(returnServiceMock())

    const wrapperComponent = defineComponent({
      components: { Pagination },
      setup() {
        const paginationComponentRef = ref()

        async function searchItems() {
          await paginationComponentRef.value?.updateItems(
            0,
            "test search string"
          )
        }

        return { serviceMock, paginationComponentRef, searchItems }
      },
      template: `
        <div>
          <button @click="searchItems">search items</button>
          <Pagination
            ref="paginationComponentRef"
            :items-per-page="5"
            :item-service="serviceMock"
          >
            <span>results</span>
          </Pagination>
        </div>`,
    })

    render(wrapperComponent)
    const user = userEvent.setup()

    await user.click(await screen.findByText("search items"))
    expect(serviceMock).toHaveBeenCalledWith(0, 5, "test search string")
  })
})
