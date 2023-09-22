import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import { createRouter, createWebHistory } from "vue-router"
import ArticleLink from "@/components/tableOfContents/ArticleLink.vue"

type ArticleLinkProps = InstanceType<typeof ArticleLink>["$props"]

function renderComponent(props?: Partial<ArticleLinkProps>) {
  const effectiveProps: ArticleLinkProps = {
    title: props?.title ?? "default",
    to: props?.to ?? "#",
    marginLevel: props?.marginLevel,
  }

  const utils = render(ArticleLink, {
    props: effectiveProps,
    global: {
      plugins: [
        createRouter({
          history: createWebHistory(),
          routes: [
            { path: "/", name: "root", component: {} },
            {
              path: "/mockPath",
              name: "mockPath",
              component: {},
            },
          ],
        }),
      ],
    },
  })

  // eslint-disable-next-line testing-library/await-async-events
  const user = userEvent.setup()
  return { ...utils, user }
}

describe("ArticleLink", () => {
  it("should render", () => {
    renderComponent()
    expect(screen.getByText("default")).toBeInTheDocument()
  })

  it("should render the title", () => {
    renderComponent({ title: "mockTitle" })
    expect(screen.getByRole("link", { name: "mockTitle" })).toBeInTheDocument()
  })

  it("should apply a margin level", () => {
    renderComponent({ marginLevel: 2 })
    expect(screen.getByRole("link")).toHaveStyle("padding-left: 52px")
  })

  it("should not apply a margin level if none is given", () => {
    renderComponent()
    expect(screen.getByRole("link")).not.toHaveAttribute("padding-left: 4px")
  })

  it("should render a link to the given path", () => {
    renderComponent({ to: "/mockPath" })
    expect(screen.getByRole("link")).toHaveAttribute("href", "/mockPath")
  })
})
