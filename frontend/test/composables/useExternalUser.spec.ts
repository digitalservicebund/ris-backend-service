import { createTestingPinia } from "@pinia/testing"
import { setActivePinia } from "pinia"
import { beforeEach, describe, expect } from "vitest"
import { useExternalUser } from "@/composables/useExternalUser"
import useSessionStore from "@/stores/sessionStore"

function mockUser(role: string) {
  const mockedSessionStore = useSessionStore()
  mockedSessionStore.user = {
    documentationOffice: undefined,
    email: undefined,
    name: "",
    roles: [role],
  }
  return mockedSessionStore
}
describe("useExternalUser", () => {
  beforeEach(() => {
    setActivePinia(createTestingPinia())
  })

  it("should return true if user has role external", async () => {
    mockUser("External")
    const result = useExternalUser()

    expect(result).equals(true)
  })

  it("should return false if user has role internal", async () => {
    mockUser("Internal")
    const result = useExternalUser()

    expect(result).equals(false)
  })
})
