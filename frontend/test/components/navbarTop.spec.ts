import { createTestingPinia } from "@pinia/testing"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import NavbarTop from "@/components/NavbarTop.vue"
import { Env } from "@/domain/env"
import { User } from "@/domain/user"
import featureToggleService from "@/services/featureToggleService"
import useSessionStore from "@/stores/sessionStore"

function renderComponent(options?: { env?: Env; activeUser?: User }) {
  render(NavbarTop, {
    props: {},
    global: {
      plugins: [
        createTestingPinia({
          initialState: {
            session: {
              env: options?.env ?? { environment: "uat" },
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
              name: "caselaw-search",
              component: {},
            },
            {
              path: "",
              name: "caselaw-procedures",
              component: {},
            },
            {
              path: "",
              name: "caselaw-periodical-evaluation",
              component: {},
            },

            {
              path: "",
              name: "caselaw-inbox",
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
  beforeEach(() => {
    vi.spyOn(featureToggleService, "isEnabled").mockResolvedValue({
      status: 200,
      data: true,
    })
  })
  test("navbar top should be rendered without error", async () => {
    renderComponent()

    expect(screen.getByText("Rechtsinformationen")).toBeInTheDocument()
    expect(screen.getByText("Vorgänge")).toBeInTheDocument()
    expect(screen.getByText("des Bundes")).toBeInTheDocument()
  })

  test("navbar top should be rendered with user and doc office badge", async () => {
    renderComponent({
      env: { environment: "staging" },
      activeUser: {
        name: "Test User",
        documentationOffice: { abbreviation: "fooDocumentationOffice" },
        initials: "TU",
      },
    })

    const sessionStore = useSessionStore()
    await sessionStore.initSession()

    expect(sessionStore.env?.environment).toBe("staging")
    expect(screen.getByText("Rechtsinformationen")).toBeInTheDocument()
    expect(screen.getByText("Vorgänge")).toBeInTheDocument()
    expect(screen.getByText("des Bundes")).toBeInTheDocument()
    expect(screen.getByText("Test User")).toBeInTheDocument()
    expect(
      screen.getByText("fooDocumentationOffice | Staging"),
    ).toBeInTheDocument()
  })
  const badgeCases = [
    {
      env: { environment: "staging" } as Env,
      user: {
        name: "Test User",
        documentationOffice: { abbreviation: "DS" },
        initials: "TU",
      },
      expected: { label: "DS | Staging", color: "bg-red-300" },
    },
    {
      env: { environment: "uat" } as Env,
      user: {
        name: "Test User",
        documentationOffice: { abbreviation: "BGH" },
        initials: "TU",
      },
      expected: { label: "BGH | UAT", color: "bg-yellow-300" },
    },
    {
      env: { environment: "production" } as Env,
      user: {
        name: "Test User",
        documentationOffice: { abbreviation: "BFH" },
        initials: "TU",
      },
      expected: { label: "BFH", color: "bg-blue-300" },
    },
  ]
  badgeCases.forEach(({ env, user, expected }) => {
    it(`should display correct badge for env '${env.environment}' and docOffice '${user.documentationOffice?.abbreviation}'`, async () => {
      renderComponent({
        env: env,
        activeUser: user,
      })

      expect(screen.getByText(expected.label)).toBeInTheDocument()
      const badgeElem = screen.getByTestId("navbar-top-badge")
      expect(badgeElem).toHaveClass(expected.color)
    })
  })
})
