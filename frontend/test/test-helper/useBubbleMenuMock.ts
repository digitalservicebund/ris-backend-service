import { vi } from "vitest"

export const useBubbleMenuMock = () => {
  vi.mock("@tiptap/vue-3", async () => {
    const actual =
      await vi.importActual<typeof import("@tiptap/vue-3")>("@tiptap/vue-3")

    return {
      ...actual,
      BubbleMenu: {
        name: "BubbleMenu",
        template: "<div><slot /></div>",
        props: ["editor", "tippyOptions", "shouldShow"],
      },
    }
  })
}
