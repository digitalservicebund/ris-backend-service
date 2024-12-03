<script lang="ts" setup>
import { Editor } from "@tiptap/vue-3"
import { BubbleMenuPlugin, BubbleMenuPluginProps } from "./bubble-menu-plugin"
import {
  defineComponent,
  h,
  onBeforeUnmount,
  onMounted,
  PropType,
  ref,
  useTemplateRef,
} from "vue"

const props = defineProps<{
  pluginKey?: any
  editor: Editor
  tippyOptions: any
  shouldShow: any
}>()

const root = useTemplateRef("root")

onMounted(() => {
  const { pluginKey, editor, tippyOptions, shouldShow } = props

  editor.registerPlugin(
    BubbleMenuPlugin({
      pluginKey,
      editor,
      element: root.value as HTMLElement,
      tippyOptions,
      shouldShow,
    }),
  )
})

onBeforeUnmount(() => {
  const { pluginKey, editor } = props

  editor.unregisterPlugin(pluginKey)
})
</script>

<template>
  <div ref="root">
    <slot></slot>
  </div>
</template>
<!-- export const BubbleMenu = defineComponent({
  name: 'BubbleMenu',

  props: {
    pluginKey: {
      // TODO: TypeScript breaks :(
      // type: [String, Object as PropType<Exclude<BubbleMenuPluginProps['pluginKey'], string>>],
      type: null,
      default: 'bubbleMenu',
    },

    editor: {
      type: Object as Editor,
      required: true,
    },

    tippyOptions: {
      type: Object as PropType<BubbleMenuPluginProps['tippyOptions']>,
      default: () => ({}),
    },

    shouldShow: {
      type: Function as PropType<Exclude<Required<BubbleMenuPluginProps>['shouldShow'], null>>,
      default: null,
    },
  },

  setup(props, { slots }) {
    const root = ref<HTMLElement | null>(null)

    onMounted(() => {
      const { pluginKey, editor, tippyOptions, shouldShow } = props

      editor.registerPlugin(
        BubbleMenuPlugin({
          pluginKey,
          editor,
          element: root.value as HTMLElement,
          tippyOptions,
          shouldShow,
        }),
      )
    })

    onBeforeUnmount(() => {
      const { pluginKey, editor } = props

      editor.unregisterPlugin(pluginKey)
    })

    return () => h('div', { ref: root }, slots.default?.())
  },
}) -->
