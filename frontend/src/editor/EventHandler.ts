import { Extension } from "@tiptap/core"
import { Plugin, PluginKey } from "@tiptap/pm/state"

export const EventHandler = Extension.create({
  name: "eventHandler",

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey("eventHandler"),
        props: {
          handlePaste: (view, event) => {
            const { state } = view
            const { selection } = state
            const { from } = selection
            const $from = state.doc.resolve(from)

            const parent = $from.node($from.depth - 1)
            const schema = state.schema
            const borderNumberContentNodeType = schema.nodes.borderNumberContent

            if (parent.type !== borderNumberContentNodeType) {
              return false
            }

            const clipboardData = event.clipboardData
            const pastedHTML = clipboardData?.getData("text/html")

            if (pastedHTML) {
              const parser = new DOMParser()
              const doc = parser.parseFromString(pastedHTML, "text/html")

              const borderNumberElements = doc.querySelectorAll("border-number")
              if (borderNumberElements.length > 0) {
                const borderNumberContentElements =
                  doc.querySelectorAll("content")

                if (borderNumberContentElements.length > 0) {
                  Array.from(borderNumberContentElements)
                    .reverse()
                    .forEach((contentNode) => {
                      this.editor.commands.insertContentAt(
                        from,
                        contentNode.outerHTML,
                      )
                    })

                  return true
                }

                return false
              }
              return false
            }
            return false
          },
          handleDrop: (view, event) => {
            const { state } = view
            const { selection } = state
            const { from } = selection
            const $from = state.doc.resolve(from)

            const parent = $from.node($from.depth - 1)
            const schema = state.schema
            const borderNumberContentNodeType = schema.nodes.borderNumberContent

            if (parent.type !== borderNumberContentNodeType) {
              return false
            }

            const dataTransfer = event.dataTransfer
            const draggedHTML = dataTransfer?.getData("text/html")

            if (draggedHTML) {
              const parser = new DOMParser()
              const doc = parser.parseFromString(draggedHTML, "text/html")

              const borderNumberElements = doc.querySelectorAll("border-number")
              if (borderNumberElements.length > 0) {
                const borderNumberContentElements =
                  doc.querySelectorAll("content")

                if (borderNumberContentElements.length > 0) {
                  Array.from(borderNumberContentElements)
                    .reverse()
                    .forEach((contentNode) => {
                      this.editor.commands.insertContentAt(
                        from,
                        contentNode.outerHTML,
                      )
                    })

                  return true
                }

                return false
              }
              return false
            }
            return false
          },
        },
      }),
    ]
  },
})
