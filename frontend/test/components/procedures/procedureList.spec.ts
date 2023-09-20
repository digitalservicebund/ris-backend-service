import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createPinia } from "pinia"
import { vi } from "vitest"
import { createRouter, createWebHistory } from "vue-router"
import ProcedureList from "@/components/procedures/ProcedureList.vue"
import { Procedure } from "@/domain/documentUnit"
import service from "@/services/procedureService"

vi.mock("@/services/procedureService")

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

async function renderComponent(options?: { procedures: Procedure[] }) {
  const mockedGetAll = vi.mocked(service.getAll).mockResolvedValue({
    status: 200,
    data: {
      content: options?.procedures || [
        {
          label: "testProcedure",
          documentUnitCount: 2,
          createdAt: "foo",
        },
      ],
      size: 1,
      totalElements: 10,
      totalPages: 10,
      number: 1,
      numberOfElements: 200,
      first: true,
      last: false,
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
        plugins: [router, createPinia()],
      },
    }),
    // eslint-disable-next-line testing-library/await-async-events
    user: userEvent.setup(),
    mockedGetAll,
    mockedGetDocumentUnits,
  }
}

describe("ProcedureList", () => {
  afterEach(() => {
    vi.resetAllMocks()
  })

  it("fetches docUnits once from BE if expanded", async () => {
    const { mockedGetAll, mockedGetDocumentUnits, user } =
      await renderComponent()
    expect(mockedGetAll).toHaveBeenCalledOnce()
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
        {
          label: "foo",
          documentUnitCount: 0,
          createdAt: "2023-09-18T19:57:01.826083Z",
        },
      ],
    })

    expect(mockedGetDocumentUnits).not.toHaveBeenCalled()

    await user.click(await screen.findByText(/foo/))
    await user.click(await screen.findByTestId("icons-open-close"))
    expect(mockedGetDocumentUnits).not.toHaveBeenCalledOnce()
  })
})
