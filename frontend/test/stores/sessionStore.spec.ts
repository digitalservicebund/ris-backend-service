import { createPinia, setActivePinia } from "pinia"
import { User } from "@/domain/user"
import adminService from "@/services/adminService"
import authService from "@/services/authService"
import { ServiceResponse } from "@/services/httpClient"
import useSessionStore from "@/stores/sessionStore"
import { Env } from "@/types/env"

vi.mock("@/services/authService")
vi.mock("@/services/adminService")

describe("Session store", () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })
  afterEach(() => {
    vi.resetAllMocks()
  })

  it("calls the authService upon authentication check", async () => {
    const authServiceMock = vi
      .spyOn(authService, "getName")
      .mockResolvedValueOnce({
        status: 200,
        data: { name: "fooUser", initials: "fU" },
      })

    const { isAuthenticated } = useSessionStore()
    await isAuthenticated()

    expect(authServiceMock).toHaveBeenCalledOnce()
  })

  it.each([
    { name: "Test User", initials: "TU" },
    {
      name: "Test User",
      documentationOffice: { abbreviation: "DS" },
      initials: "TU",
    },
    { name: "Test User", email: "foo@mail.de", initials: "TU" },
  ])("set's and returns the correct user", async (user: User) => {
    vi.mocked(authService).getName.mockResolvedValue({
      status: 200,
      data: user,
    })

    const session = useSessionStore()
    await session.isAuthenticated()

    expect(session.user).toEqual(user)
  })

  it.each([
    { status: 400, error: { title: "could not get user" } },
    { status: 200, error: { title: "ignore my status" } },
  ])(
    "is not authenticated without user",
    async (serviceResponse: ServiceResponse<User>) => {
      vi.mocked(authService).getName.mockResolvedValue(serviceResponse)

      const { user, isAuthenticated } = useSessionStore()

      expect(await isAuthenticated()).toBeFalsy()
      expect(user).toBeUndefined()
    },
  )

  it.each([
    { environment: "staging" },
    { environment: "uat" },
    { environment: "production" },
  ] as Env[])("sets and returns the correct env", async (env: Env) => {
    vi.mocked(adminService).getEnv.mockResolvedValue({
      status: 200,
      data: env,
    })

    const session = useSessionStore()
    await session.initSession()

    expect(session.env?.environment).toEqual(env.environment)
  })
})
