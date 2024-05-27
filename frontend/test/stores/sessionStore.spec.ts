import { setActivePinia, createPinia } from "pinia"
import { User } from "@/domain/user"
import authService from "@/services/authService"
import { ServiceResponse } from "@/services/httpClient"
import useSessionStore from "@/stores/sessionStore"

vi.mock("@/services/authService")

describe("Session store", () => {
  beforeEach(() => void setActivePinia(createPinia()))
  afterEach(() => void vi.resetAllMocks())

  it("calls the authService upon initialization", async () => {
    const authServiceMock = vi
      .spyOn(authService, "getName")
      .mockResolvedValueOnce({ status: 200, data: { name: "fooUser" } })

    useSessionStore()

    expect(authServiceMock).toHaveBeenCalledOnce()
  })

  it.each([
    { name: "fooUser" },
    { name: "fooUser", documentationOffice: { abbreviation: "DS" } },
    { name: "fooUser", email: "foo@mail.de" },
  ])("set's the correct user", async (user: User) => {
    vi.mocked(authService).getName.mockResolvedValue({
      status: 200,
      data: user,
    })

    const session = useSessionStore()
    await session.fetchUser()

    expect(session.user).toEqual(user)
  })

  it.each([
    { status: 400, error: { title: "could not get user" } },
    { status: 200, error: { title: "ignore my status" } },
  ])(
    "logs errors if user cannot be loaded",
    async (serviceResponse: ServiceResponse<User>) => {
      vi.mocked(authService).getName.mockResolvedValue(serviceResponse)
      const consoleMock = vi.spyOn(console, "error").mockImplementation(vi.fn())

      const session = useSessionStore()
      await session.fetchUser()
      expect(consoleMock).toHaveBeenNthCalledWith(2, "failed to load user")
    },
  )
})
