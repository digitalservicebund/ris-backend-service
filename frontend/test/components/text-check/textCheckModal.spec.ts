import { userEvent } from "@testing-library/user-event"
import { render, screen } from "@testing-library/vue"
import type { ChainedCommands } from "@tiptap/core"
import { Editor } from "@tiptap/vue-3"
import { mount } from "@vue/test-utils"
import { EditorState } from "prosemirror-state"
import type { EditorView } from "prosemirror-view"
import TextCheckModal from "@/components/text-check/TextCheckModal.vue"
import { IgnoreOnceTagName } from "@/editor/ignoreOnceMark"
import { Match } from "@/types/textCheck"
import { useFeatureToggleServiceMock } from "~/test-helper/useFeatureToggleServiceMock"

useFeatureToggleServiceMock()

function createEditorMock(overrides: Partial<Editor> = {}): Editor {
  const docMock = {
    nodeAt: vi.fn().mockReturnValue({
      marks: [],
    }),
  }

  const stateMock = {
    selection: {
      from: baseMatch.offset,
      to: baseMatch.offset + baseMatch.length,
    },
    doc: docMock,
  } as unknown as EditorState

  const viewMock = {} as unknown as EditorView

  const mockEditor: Partial<Editor> = {
    view: viewMock,
    state: stateMock,
    commands: {} as never,
    chain: vi.fn(() => ({
      focus: vi.fn(),
    })) as unknown as () => ChainedCommands,
    isActive: vi.fn().mockReturnValue(false),
    on: vi.fn(),
    off: vi.fn(),
    getAttributes: vi.fn().mockReturnValue({}),
    // allow overrides for specific tests
    ...overrides,
  }

  return mockEditor as unknown as Editor
}

function renderComponent(match: Match, editor: Editor) {
  const user = userEvent.setup()

  const IgnoredWordHandlerStub = {
    props: ["match", "ignoredLocally"],
    emits: [
      "ignored-word:remove",
      "ignored-word:add",
      "globally-ignored-word:add",
      "globally-ignored-word:remove",
      "ignore-once:toggle",
    ],
    template: `
      <div data-testid="ignored-word-handler-stub">
        <button @click="$emit('ignored-word:remove')" data-testid="remove-btn">Remove</button>
        <button @click="$emit('ignored-word:add')" data-testid="add-btn">Add</button>
        <button @click="$emit('globally-ignored-word:add')" data-testid="global-add-btn">Global Add</button>
        <button @click="$emit('globally-ignored-word:remove')" data-testid="global-remove-btn">Global Remove</button>
        <button @click="$emit('ignore-once:toggle')" data-testid="ignore-once-btn">Global Remove</button>
      </div>
    `,
  }

  return {
    user,
    ...render(TextCheckModal, {
      props: {
        match: match,
        editor: editor,
      },
      global: {
        stubs: {
          IgnoredWordHandler: IgnoredWordHandlerStub,
        },
      },
    }),
  }
}

const baseMatch: Match = {
  word: "testword",
  offset: 0,
  length: 8,
  replacements: [{ value: "suggestion1" }, { value: "suggestion2" }],
  category: "",
  message: "This is a test message.",
  context: { text: "test context", length: 12, offset: 0 },
  sentence: "This is a test sentence.",
  shortMessage: "Test message.",
  type: { typeName: "UnknownWord" },
  rule: {
    id: "123",
    category: { id: "abc", name: "Test Category" },
    description: "Test rule description",
    issueType: "grammar",
  },
  ignoreForIncompleteSentence: true,
  id: 1,
  contextForSureMatch: 1,
  ignoredTextCheckWords: [],
}

describe("TextCheckModal", () => {
  test("renders the word", () => {
    // given
    const editor = createEditorMock({
      state: {
        selection: {
          from: 0,
          to: 8,
        },
        doc: {
          nodeAt: vi.fn().mockReturnValue({
            marks: [],
          }),
        },
      } as unknown as EditorState,
    })

    // when
    renderComponent(baseMatch, editor)

    // then
    expect(screen.getByTestId("text-check-modal")).toBeInTheDocument()
    expect(screen.getByTestId("text-check-modal-word")).toHaveTextContent(
      "testword",
    )
  })

  it("emits received from a subcomponent invoke the correct emmits to be emited by this component", async () => {
    const { emitted, user } = renderComponent(
      {
        ...baseMatch,
        ignoredTextCheckWords: [
          {
            type: "documentation_unit",
            word: "testword",
            id: "1",
          },
        ],
      },
      createEditorMock(),
    )
    await user.click(screen.getByTestId("remove-btn"))
    expect(emitted()["word:remove"]).toEqual([["testword"]])

    await user.click(screen.getByTestId("add-btn"))
    expect(emitted()["word:add"]).toEqual([["testword"]])

    await user.click(screen.getByTestId("global-add-btn"))
    expect(emitted()["globalWord:add"]).toEqual([["testword"]])

    await user.click(screen.getByTestId("global-remove-btn"))
    expect(emitted()["globalWord:remove"]).toEqual([["testword"]])

    await user.click(screen.getByTestId("ignore-once-btn"))
    expect(emitted()["ignore-once:toggle"]).toEqual([[0]])
  })

  it("passes true as ignoredLocally prop based on editor marks", () => {
    const editor = createEditorMock({
      state: {
        selection: { from: 0, to: 8 },
        doc: {
          nodeAt: vi.fn().mockReturnValue({
            marks: [{ type: { name: IgnoreOnceTagName } }],
          }),
        },
      } as unknown as EditorState,
    })

    const wrapper = mount(TextCheckModal, {
      props: { match: baseMatch, editor },
      global: {
        stubs: {
          IgnoredWordHandler: true,
        },
      },
    })

    const childComponent = wrapper.findComponent({ name: "IgnoredWordHandler" })
    expect(childComponent.props("ignoredLocally")).toBe(true)
  })

  it("passes false as ignoredLocally prop based on editor marks", () => {
    const editor = createEditorMock({
      state: {
        selection: { from: 0, to: 8 },
        doc: {
          nodeAt: vi.fn().mockReturnValue({
            marks: [],
          }),
        },
      } as unknown as EditorState,
    })

    const wrapper = mount(TextCheckModal, {
      props: { match: baseMatch, editor },
      global: {
        stubs: {
          IgnoredWordHandler: true,
        },
      },
    })

    const childComponent = wrapper.findComponent({ name: "IgnoredWordHandler" })
    expect(childComponent.props("ignoredLocally")).toBe(false)
  })
})
