import MockAdapter from "axios-mock-adapter"
import httpClient, { axiosInstance } from "@/services/httpClient"

describe("httpClient", () => {
  let server: MockAdapter

  beforeEach(() => {
    server = new MockAdapter(axiosInstance)
  })

  afterEach(() => {
    server.restore()
  })

  it("put the API base prefix in front of the given URL", async () => {
    server.onAny().reply(200)

    await httpClient.get("test")

    expect(server.history.get).toBeDefined()
    expect(server.history.get[0].url).toBe("/api/v1/test")
  })

  it("returns response status and body when the server has responded", async () => {
    server.onAny().reply(200, "test body")

    const response = await httpClient.get("")

    expect(response.error).toBeUndefined()
    expect(response.status).toBe(200)
    expect(response.data).toBe("test body")
  })

  it("returns with server response also if status code is above 400", async () => {
    server.onAny().reply(400, "test body")

    const response = await httpClient.get("")

    expect(response.error).toBeUndefined()
    expect(response.status).toBe(400)
    expect(response.data).toBe("test body")
  })

  it("forwards the error if the network connection failed", async () => {
    server.onAny().networkError()

    const response = await httpClient.get("")

    expect(response.error).toBeDefined()
    expect(response.status).toBe(500)
    expect(response.data).toBeUndefined()
  })
})
