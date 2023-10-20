import { ref } from "vue"
import { Router } from "vue-router"
import useQuery from "@/composables/useQueryFromRoute"

describe("useQuery composable", () => {
  const mocks = vi.hoisted(() => ({
    mockedPush: vi.fn(),
  }))

  vi.mock("vue-router", async () => {
    const actual = await vi.importActual<Router>("vue-router")
    return {
      ...actual,
      useRouter: vi.fn().mockReturnValue({ push: mocks.mockedPush }),
      useRoute: vi.fn().mockReturnValue({ query: { foo: "initialFoo" } }),
    }
  })

  afterEach(() => {
    mocks.mockedPush.mockClear()
  })

  it("instantiates with correct keys", async () => {
    const { getQueryFromRoute } = useQuery<"foo" | "bar" | "baz">()
    const query = ref(getQueryFromRoute())

    expect(query.value).toEqual({ foo: "initialFoo" })
  })

  it("adds new query to route", async () => {
    const { pushQueryToRoute } = useQuery<"foo" | "bar" | "baz">()
    pushQueryToRoute({ bar: "barValue", baz: "bazValue" })

    expect(mocks.mockedPush).toHaveBeenCalledWith({
      query: { bar: "barValue", baz: "bazValue" },
    })
  })

  it("does not add empty query parameters", async () => {
    const { pushQueryToRoute } = useQuery<"foo" | "bar" | "baz">()
    pushQueryToRoute({ bar: "barValue", baz: "" })

    expect(mocks.mockedPush).toHaveBeenCalledWith({
      query: { bar: "barValue" },
    })
  })

  it("does not any query if empty", async () => {
    const { pushQueryToRoute } = useQuery<"foo" | "bar" | "baz">()
    pushQueryToRoute({ bar: "", baz: "" })

    expect(mocks.mockedPush).toHaveBeenCalledWith({
      query: {},
    })
  })
})
