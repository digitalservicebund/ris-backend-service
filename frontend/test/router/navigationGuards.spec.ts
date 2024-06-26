import { createTestingPinia } from "@pinia/testing"
import { setActivePinia } from "pinia"
import { RouteLocationNormalizedGeneric } from "vue-router"
import router, { beforeEach as routerBeforeEach } from "@/router"
import useSessionStore from "@/stores/sessionStore"

function mockSessionStore(userAuthenticated = true) {
  const mockedSessionStore = useSessionStore()
  mockedSessionStore.isAuthenticated = vi.fn(() =>
    Promise.resolve(userAuthenticated),
  )

  return mockedSessionStore
}

describe("router's auth navigation guards", () => {
  beforeEach(() => {
    vi.mock("~pages", () => ({
      default: [
        {
          path: "/",
          component: { template: "<div>Home</div>" },
        },
        {
          path: "/caselaw",
          component: { template: "<div>Case Law</div>" },
        },
        {
          path: "/caselaw/documentunit/:id/categories",
          component: { template: "<div>Categories</div>" },
        },
        {
          path: "/norms",
          component: { template: "<div>Norms</div>" },
        },
      ],
    }))

    setActivePinia(createTestingPinia())

    afterEach(() => void vi.resetAllMocks())
  })

  const assignMock = vi.fn()
  window.location = { assign: assignMock as Location["assign"] } as Location
  window.location.href = "/"

  afterEach(() => {
    vi.resetAllMocks()
    assignMock.mockClear()
    document.cookie = ""
  })

  it("does not redirect, if not authenticated", async () => {
    mockSessionStore(false)

    const result = await routerBeforeEach(
      router.resolve("/caselaw") as RouteLocationNormalizedGeneric,
    )
    expect(result).toEqual(false)
  })

  it("does redirect, if authenticated", async () => {
    mockSessionStore(true)

    const result = await routerBeforeEach(router.currentRoute.value)
    expect(result).toEqual(true)
  })

  it("calls the store for authentication", async () => {
    const sessionStore = mockSessionStore()

    await routerBeforeEach(
      router.resolve("/caselaw") as RouteLocationNormalizedGeneric,
    )
    expect(sessionStore.isAuthenticated).toHaveBeenCalledOnce()
  })

  it("does safe location cookie if not authenticated", async () => {
    mockSessionStore(false)

    await routerBeforeEach(
      router.resolve(
        "/caselaw/documentunit/123456/categories",
      ) as RouteLocationNormalizedGeneric,
    )
    expect(document.cookie).toEqual(
      "location=/caselaw/documentunit/123456/categories",
    )
  })

  it("does follow location cookie if authenticated", async () => {
    mockSessionStore(true)
    document.cookie = "location=/norms; path=/;"

    await routerBeforeEach(
      router.resolve("/") as RouteLocationNormalizedGeneric,
    )
    expect(document.cookie).toEqual("")
    expect(window.location.href).toEqual("/norms")
  })
})
