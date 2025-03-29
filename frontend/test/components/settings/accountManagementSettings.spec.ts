import { render, screen } from "@testing-library/vue"
import AccountManagementSettings from "@/components/settings/AccountManagementSettings.vue"
import adminService from "@/services/adminService"

describe("account management settings", () => {
  test("displays account management", async () => {
    vi.spyOn(adminService, "getAccountManagementUrl").mockResolvedValue({
      status: 200,
      data: "url",
    })

    render(AccountManagementSettings)
    await screen.findByText("Kontoverwaltung")
    expect(screen.getByText("Bare.ID öffnen")).toBeInTheDocument()
  })
})
