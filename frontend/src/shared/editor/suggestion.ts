import { Mention, MentionOptions } from "@tiptap/extension-mention"
import { PluginKey } from "@tiptap/pm/state"
import { SuggestionKeyDownProps, SuggestionProps } from "@tiptap/suggestion"
import { AnyExtension, VueRenderer } from "@tiptap/vue-3"
import tippy, { Instance as TippyInstance } from "tippy.js"
import SuggestionMenu from "@/shared/components/SuggestionMenu.vue"

class SuggestionMenuRenderer {
  private component?: VueRenderer = undefined
  private popup?: TippyInstance = undefined

  public onStart(props: SuggestionProps): void {
    this.component = new VueRenderer(SuggestionMenu, {
      props,
      editor: props.editor,
    })

    if (props.clientRect) {
      this.popup = tippy("body", {
        getReferenceClientRect: () => props.clientRect?.() ?? new DOMRect(),
        appendTo: () => document.body,
        content: this.component.element,
        showOnCreate: true,
        interactive: true,
        trigger: "manual",
        placement: "top-start",
      })[0]
    }
  }

  public onUpdate(props: SuggestionProps): void {
    this.component?.updateProps(props)

    if (props.clientRect) {
      this.popup?.setProps({
        getReferenceClientRect: () => props.clientRect?.() ?? new DOMRect(),
      })
    }
  }

  public onKeyDown(props: SuggestionKeyDownProps): boolean {
    if (props.event.key === "Escape") {
      this.popup?.hide()
      return true
    }

    return this.component?.ref?.onKeyDown(props)
  }

  public onExit(): void {
    this.popup?.destroy()
    this.component?.destroy()
  }
}

function createSuggestionExtensionOptions(
  options: SuggestionGroupOptions
): MentionOptions {
  return {
    HTMLAttributes: {
      class: options.elementClasses?.join(" "),
    },
    renderLabel({ node }) {
      return `${node.attrs.label}`
    },
    suggestion: {
      pluginKey: new PluginKey(`Suggestion_${options.segmentType}`),
      allowedPrefixes: options.triggerWithoutLeadingSpace ? null : undefined,
      char: options.trigger,
      items: ({ query }) => options.callback(query),
      render: () => new SuggestionMenuRenderer(),
    },
  }
}

/**
 * Configuration options for a specific suggestion group that should be
 * available in the editor. Each group gets identified by its own segment type.
 * Each group must use its own unique character sequence that the user types to
 * trigger the suggestion.
 */
export interface SuggestionGroupOptions {
  /**
   * Used as type for the segments in the editor content. Allows to identify
   * suggestions and differentiate them during data processing.
   */
  segmentType: string

  /**
   * Usually a single character (e.g. `@`) that triggers the suggestion when
   * typed by the user. In rare cases this can be a whole word. Each suggestion
   * group must use a different one to avoid conflicts.
   */
  trigger: string

  /**
   * Toggles if the trigger must always be preceded by a space or new line
   * character or can be used also within words.
   */
  triggerWithoutLeadingSpace?: boolean

  /**
   * Callback that is triggered for any keystroke by the user after the trigger.
   * The returned list are the available suggestion items available for the user.
   * Further typing by the user should usually shrink the list of items.
   * This can be as simple as a `filter` on a static array.
   * The `label` is what the user sees in the editor. The optional `id` can be
   * set for better data processing.
   */
  callback: (input: string) => { label: string; id?: string }[]

  /**
   * List of classes to set on the segment in the editor for visual appearance.
   * Can help to make a suggestion group stand-out in contrast to normal text or
   * other suggestion groups.
   */
  elementClasses?: string[]
}

/**
 * Creates a custom extension that provides automatic suggestion within the
 * TipTap editor. When the user types a specific sequence it triggers the
 * extension that then provides some suggestions to choose from in a pop-up
 * menu.
 *
 * This extension is based on a customized version of the Mention extension by
 * the TipTap, which itself is a thin layer on top of the suggestion
 * functionality by TipTap.
 */
export function createSuggestionExtension(
  options: SuggestionGroupOptions
): AnyExtension {
  const extension = Mention.extend({ name: options.segmentType })
  const extensionOptions = createSuggestionExtensionOptions(options)
  return extension.configure(extensionOptions)
}
