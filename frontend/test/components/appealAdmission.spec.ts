import { createTestingPinia } from "@pinia/testing"
import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import AppealAdmissionComponent from "@/components/AppealAdmission.vue"
import { AppealAdmission } from "@/domain/appealAdmission"
import { AppealAdmitter } from "@/domain/appealAdmitter"
import { Decision } from "@/domain/decision"

import { onSearchShortcutDirective } from "@/utils/onSearchShortcutDirective"
import routes from "~/test-helper/routes"

function renderComponent(appealAdmission?: AppealAdmission) {
  const user = userEvent.setup()

  const router = createRouter({
    history: createWebHistory(),
    routes: routes,
  })
  return {
    user,
    ...render(AppealAdmissionComponent, {
      global: {
        directives: { "ctrl-enter": onSearchShortcutDirective },
        plugins: [
          [
            createTestingPinia({
              initialState: {
                docunitStore: {
                  documentUnit: new Decision("foo", {
                    documentNumber: "1234567891234",
                    contentRelatedIndexing: {
                      appealAdmission: appealAdmission,
                    },
                  }),
                },
              },
              stubActions: false,
            }),
          ],
          [router],
        ],
        stubs: {
          routerLink: {
            template: "<a><slot/></a>",
          },
        },
      },
    }),
  }
}

describe("appeal admission", () => {
  it("renders only 'Rechtsmittel zugelassen' when no appeal admission data exists", async () => {
    renderComponent()
    expect(screen.getByLabelText("Rechtsmittelzulassung")).toBeVisible()
    expect(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen" }),
    ).toBeInTheDocument()
    expect(
      screen.queryByRole("combobox", { name: "Rechtsmittel zugelassen durch" }),
    ).not.toBeInTheDocument()
  })

  it("renders only 'Rechtsmittel zugelassen' when appeal admitted is false", () => {
    renderComponent({
      admitted: false,
    })

    expect(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen" }),
    ).toHaveTextContent("Nein")
    expect(
      screen.queryByRole("combobox", { name: "Rechtsmittel zugelassen durch" }),
    ).not.toBeInTheDocument()
  })

  it("renders both 'Rechtsmittel zugelassen' and 'Rechtsmittel zugelassen durch' when appeal admitted is true", () => {
    renderComponent({
      admitted: true,
    })

    expect(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen" }),
    ).toHaveTextContent("Ja")
    expect(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen durch" }),
    ).toBeInTheDocument()
  })

  it("renders 'Rechtsmittel zugelassen durch' options", async () => {
    const { user } = renderComponent({
      admitted: true,
      by: AppealAdmitter.FG,
    })
    expect(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen durch" }),
    ).toHaveTextContent("FG")

    await user.click(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen durch" }),
    )

    expect(screen.getByRole("option", { name: "FG" })).toBeInTheDocument()
    expect(screen.getByRole("option", { name: "BFH" })).toBeInTheDocument()
  })

  it("can correctly update values", async () => {
    const { user } = renderComponent({
      admitted: false,
    })

    await user.click(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen" }),
    )
    await user.click(screen.getByRole("option", { name: "Ja" }))
    await user.click(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen durch" }),
    )
    await user.click(screen.getByRole("option", { name: "FG" }))

    expect(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen" }),
    ).toHaveTextContent("Ja")
    expect(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen durch" }),
    ).toHaveTextContent("FG")
  })

  it("removes 'Rechtsmittel zugelassen durch' when 'Rechtsmittel zugelassen' is set to 'Nein'", async () => {
    const { user } = renderComponent({
      admitted: true,
      by: AppealAdmitter.FG,
    })

    await user.click(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen" }),
    )
    await user.click(screen.getByRole("option", { name: "Nein" }))

    expect(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen" }),
    ).toHaveTextContent("Nein")
    expect(
      screen.queryByRole("combobox", { name: "Rechtsmittel zugelassen durch" }),
    ).not.toBeInTheDocument()

    await user.click(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen" }),
    )
    await user.click(screen.getByRole("option", { name: "Ja" }))

    expect(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen" }),
    ).toHaveTextContent("Ja")
    expect(
      screen.getByRole("combobox", { name: "Rechtsmittel zugelassen durch" }),
    ).toBeInTheDocument()
    expect(
      screen.queryByRole("combobox", { name: "Rechtsmittel zugelassen durch" }),
    ).not.toHaveValue()
  })
})
