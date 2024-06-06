import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import NavbarTop from "@/components/NavbarTop.vue"
import { User } from "@/domain/user"
import useSessionStore from "@/stores/sessionStore"

function renderComponent(options?: { env?: string; activeUser?: User }) {
  render(NavbarTop, {
    props: {},
    global: {
      plugins: [
        createTestingPinia({
          initialState: {
            session: {
              env: options?.env ?? "uat",
              user: options?.activeUser || {
                name: "user",
                documentationOffice: { abbreviation: "DS" },
              },
            },
          },
        }),
        createRouter({
          history: createWebHistory(),
          routes: [
            {
              path: "",
              name: "caselaw",
              component: {},
            },
            {
              path: "",
              name: "caselaw-procedures",
              component: {},
            },
            {
              path: "",
              name: "search",
              component: {},
            },
            {
              path: "",
              name: "settings",
              component: {},
            },
          ],
        }),
      ],
    },
  })
}
describe("navbar top", () => {
  test("navbar top should be rendered without error", async () => {
    renderComponent()

    expect(screen.getByText("Rechtsinformationen")).toBeInTheDocument()
    expect(screen.getByText("Vorgänge")).toBeInTheDocument()
    expect(screen.getByText("des Bundes")).toBeInTheDocument()
  })

  test("navbar top should be rendered with user and doc office badge", async () => {
    renderComponent({
      env: "staging",
      activeUser: {
        name: "fooUser",
        documentationOffice: { abbreviation: "fooDocumentationOffice" },
      },
    })

    const sessionStore = useSessionStore()
    await sessionStore.initSession()

    expect(sessionStore.env).toBe("staging")
    expect(screen.getByText("Rechtsinformationen")).toBeInTheDocument()
    expect(screen.getByText("Vorgänge")).toBeInTheDocument()
    expect(screen.getByText("des Bundes")).toBeInTheDocument()
    expect(screen.getByText("fooUser"))
    expect(screen.getByText("fooDocumentationOffice | Staging"))
  })
})
