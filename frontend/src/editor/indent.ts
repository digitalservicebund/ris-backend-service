// Sources:
// https://github.com/ueberdosis/tiptap/issues/1036#issuecomment-1000983233
// https://github.com/ueberdosis/tiptap/issues/1036#issuecomment-981094752
// https://github.com/django-tiptap/django_tiptap/blob/main/django_tiptap/templates/forms/tiptap_textarea.html#L453-L602

import { CommandProps, Extension, KeyboardShortcutCommand } from "@tiptap/core"
import { TextSelection, Transaction } from "prosemirror-state"

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    indent: {
      indent: () => ReturnType
      outdent: () => ReturnType
    }
  }
}

type IndentOptions = {
  names: string[]
  indentRange: number
  minIndentLevel: number
  maxIndentLevel: number
  defaultIndentLevel: number
  HTMLAttributes: Record<string, unknown>
}

type IndentType = "indent" | "outdent"

const clamp = (val: number, min: number, max: number): number =>
  Math.min(Math.max(val, min), max)

const updateIndentLevel = (
  transaction: Transaction,
  options: IndentOptions,
  type: IndentType,
): Transaction => {
  const { doc, selection } = transaction
  if (!doc || !selection || !(selection instanceof TextSelection))
    return transaction

  const { from, to } = selection
  doc.nodesBetween(from, to, (node, pos) => {
    if (options.names.includes(node.type.name)) {
      transaction = setNodeIndentMarkup(
        transaction,
        pos,
        options.indentRange * (type === "indent" ? 1 : -1),
        options,
      )
      return false
    }
    return true
  })
  return transaction
}

const setNodeIndentMarkup = (
  transaction: Transaction,
  pos: number,
  delta: number,
  options: IndentOptions,
): Transaction => {
  const node = transaction.doc?.nodeAt(pos)
  if (!node) return transaction

  const indent = clamp(
    (node.attrs.indent || 0) + delta,
    options.minIndentLevel * options.indentRange,
    options.maxIndentLevel * options.indentRange,
  )
  if (indent === node.attrs.indent) return transaction

  return transaction.setNodeMarkup(
    pos,
    node.type,
    { ...node.attrs, indent },
    node.marks,
  )
}

const getOutdent =
  (): KeyboardShortcutCommand =>
  ({ editor }) => {
    // If the cursor is not at the start of the node, don't unindent
    if (editor.state.selection.$head.parentOffset > 0) return false

    return editor.chain().focus().outdent().run()
  }

export const Indent = Extension.create<IndentOptions, never>({
  name: "indent",

  addOptions() {
    return {
      names: ["heading", "paragraph"],
      indentRange: 40,
      minIndentLevel: 0,
      maxIndentLevel: 10,
      defaultIndentLevel: 0,
      HTMLAttributes: {},
    }
  },

  addGlobalAttributes() {
    return [
      {
        types: this.options.names,
        attributes: {
          indent: {
            default: this.options.defaultIndentLevel,
            renderHTML: (attributes) =>
              attributes.indent > 0
                ? {
                    style: `margin-left: ${attributes.indent}px!important;`,
                  }
                : null,
            parseHTML: (element) =>
              parseInt(element.style.marginLeft, 10) ||
              this.options.defaultIndentLevel,
          },
        },
      },
    ]
  },

  addCommands() {
    return {
      indent:
        () =>
        ({ tr, state, dispatch }: CommandProps) => {
          tr = updateIndentLevel(
            tr.setSelection(state.selection),
            this.options,
            "indent",
          )
          if (tr.docChanged && dispatch) {
            dispatch(tr)
            return true
          }
          return false
        },
      outdent:
        () =>
        ({ tr, state, dispatch }: CommandProps) => {
          tr = updateIndentLevel(
            tr.setSelection(state.selection),
            this.options,
            "outdent",
          )
          if (tr.docChanged && dispatch) {
            dispatch(tr)
            return true
          }
          return false
        },
    }
  },

  addKeyboardShortcuts() {
    return {
      Backspace: getOutdent(),
    }
  },

  onUpdate() {
    const { editor } = this
    if (editor.isActive("listItem")) {
      const node = editor.state.selection.$head.node()
      if (node.attrs.indent) {
        editor.commands.updateAttributes(node.type.name, { indent: 0 })
      }
    }
  },
})
