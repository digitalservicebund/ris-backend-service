import { createTestingPinia, TestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import NavbarTop from "@/components/NavbarTop.vue"
import useSessionStore from "@/stores/sessionStore"

function renderComponent(pinia: TestingPinia = createTestingPinia()) {
  render(NavbarTop, {
    props: {},
    global: {
      plugins: [
        pinia,
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
    renderComponent(
      createTestingPinia({
        initialState: {
          session: {
            env: "staging",
            // user: { name: "user" },
            // user: {},
            // user: undefined,
          },
        },
      }),
    )

    const sessionStore = useSessionStore()
    await sessionStore.initSession()
    // sessionStore.user = {
    //   name: "user",
    //   documentationOffice: { abbreviation: "DS"}
    // }

    expect(sessionStore.env).toBe("staging")
    expect(screen.getByText("Rechtsinformationen")).toBeInTheDocument()
    expect(screen.getByText("Vorgänge")).toBeInTheDocument()
    expect(screen.getByText("des Bundes")).toBeInTheDocument()
    expect(screen.getByText("user"))
    expect(screen.getByText("Staging"))
  })
})
