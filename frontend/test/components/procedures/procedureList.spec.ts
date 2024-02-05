import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import ProcedureList from "@/components/procedures/ProcedureList.vue"
import useQuery from "@/composables/useQueryFromRoute"
import { Procedure } from "@/domain/documentUnit"
import service from "@/services/procedureService"

vi.mock("@/services/procedureService")

const mocks = vi.hoisted(() => ({
  mockedPushQuery: vi.fn(),
}))

vi.mock("@/composables/useQueryFromRoute", async () => {
  const actual = (
    await vi.importActual<{ default: typeof useQuery }>(
      "@/composables/useQueryFromRoute",
    )
  ).default
  return {
    default: () => {
      return {
        ...actual(),
        pushQueryToRoute: mocks.mockedPushQuery,
      }
    },
  }
})

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes: [
    {
      path: "",
      name: "index",
      component: {},
    },
  ],
})

async function renderComponent(options?: { procedures: Procedure[][] }) {
  const mockedGetProcedures = vi
    .mocked(service.get)
    .mockResolvedValueOnce({
      status: 200,
      data: {
        content: options?.procedures[0] ?? [
          {
            label: "testProcedure",
            documentUnitCount: 2,
            createdAt: "foo",
          },
        ],
        size: 1,
        number: 1,
        numberOfElements: 200,
        first: true,
        last: false,
        empty: false,
      },
    })
    .mockResolvedValueOnce({
      status: 200,
      data: {
        content: options?.procedures[1] ?? [
          {
            label: "testProcedure",
            documentUnitCount: 2,
            createdAt: "foo",
          },
        ],
        size: 1,
        number: 1,
        numberOfElements: 200,
        first: true,
        last: false,
        empty: false,
      },
    })

  const mockedGetDocumentUnits = vi
    .mocked(service.getDocumentUnits)
    .mockResolvedValue({
      status: 200,
      data: [{ documentNumber: "testABC" }],
    })

  return {
    ...render(ProcedureList, {
      global: {
        stubs: { routerLink: { template: "<a><slot /></a>" } },
        plugins: [router],
      },
    }),
    // eslint-disable-next-line testing-library/await-async-events
    user: userEvent.setup(),
    mockedGetProcedures,
    mockedGetDocumentUnits,
  }
}

describe("ProcedureList", () => {
  afterEach(() => {
    vi.resetAllMocks()
  })

  it("fetches docUnits once from BE if expanded", async () => {
    const { mockedGetProcedures, mockedGetDocumentUnits, user } =
      await renderComponent()
    expect(mockedGetProcedures).toHaveBeenCalledOnce()
    expect(mockedGetDocumentUnits).not.toHaveBeenCalled()

    await user.click(await screen.findByTestId("icons-open-close"))
    expect(mockedGetDocumentUnits).toHaveBeenCalledOnce()

    // do not fetch again
    await user.click(await screen.findByTestId("icons-open-close"))
    expect(mockedGetDocumentUnits).toHaveBeenCalledOnce()
  })

  it("does not fetches docUnits if unecessary", async () => {
    const { mockedGetDocumentUnits, user } = await renderComponent({
      procedures: [
        [
          {
            label: "foo",
            documentUnitCount: 0,
            createdAt: "2023-09-18T19:57:01.826083Z",
          },
        ],
      ],
    })

    expect(mockedGetDocumentUnits).not.toHaveBeenCalled()

    await user.click(await screen.findByText(/foo/))
    await user.click(await screen.findByTestId("icons-open-close"))
    expect(mockedGetDocumentUnits).not.toHaveBeenCalledOnce()
  })

  it("searches on entry", async () => {
    const { user, mockedGetProcedures } = await renderComponent({
      procedures: [
        [
          {
            label: "foo",
            documentUnitCount: 0,
            createdAt: "2023-09-18T19:57:01.826083Z",
          },
          {
            label: "bar",
            documentUnitCount: 0,
            createdAt: "2023-09-18T19:57:01.826083Z",
          },
        ],
      ],
    })

    await user.type(await screen.findByLabelText("Nach Vorgängen suchen"), "b")
    expect(mockedGetProcedures).toHaveBeenCalledWith(10, 0, "b")

    expect(mocks.mockedPushQuery).not.toHaveBeenCalled()
  })

  it("debounces route updates on search", async () => {
    const { user } = await renderComponent()
    await user.type(await screen.findByLabelText("Nach Vorgängen suchen"), "b")

    expect(mocks.mockedPushQuery).not.toHaveBeenCalled()

    await new Promise((resolve) => {
      setTimeout(resolve, 500)
    })

    expect(mocks.mockedPushQuery).toHaveBeenCalledWith({ q: "b" })
  })

  it("keeps toggled procedure open on search (copies loaded documentUnits)", async () => {
    const { user } = await renderComponent({
      procedures: [
        [
          {
            label: "foo",
            documentUnitCount: 0,
            createdAt: "2023-09-18T19:57:01.826083Z",
          },
          {
            label: "bar",
            documentUnitCount: 0,
            createdAt: "2023-09-18T19:57:01.826083Z",
          },
        ],
        [
          {
            label: "bar",
            documentUnitCount: 0,
            createdAt: "2023-09-18T19:57:01.826083Z",
          },
        ],
      ],
    })

    await user.click(await screen.findByText("bar"))
    expect(await screen.findByText("erstellt am 18.09.2023")).toBeVisible()

    await user.type(await screen.findByLabelText("Nach Vorgängen suchen"), "b")
    expect(screen.queryByText("foo")).not.toBeInTheDocument()
    expect(await screen.findByText("erstellt am 18.09.2023")).toBeVisible()
  })
})
