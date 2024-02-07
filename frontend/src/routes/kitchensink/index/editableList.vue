<script lang="ts" setup>
import { defineComponent, h, ref } from "vue"
import EditableList from "@/components/EditableListCaselaw.vue"
import EditableListItem from "@/domain/editableListItem"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import { withSummarizer } from "@/shared/components/DataSetSummary.vue"
import TextInput from "@/shared/components/input/TextInput.vue"

class ExtendedEditableListItem implements EditableListItem {
  public text?: string

  constructor(data: Partial<ExtendedEditableListItem> = {}) {
    Object.assign(this, data)
  }

  get renderDecision(): string {
    return this.text ? this.text : "default text"
  }
}

const listWithEntries = ref<ExtendedEditableListItem[]>([
  new ExtendedEditableListItem({ text: "foo" }),
  new ExtendedEditableListItem({ text: "bar" }),
])

function summerizer(dataEntry: EditableListItem) {
  return h("div", { class: ["ds-label-01-reg"] }, dataEntry.renderDecision)
}

const SummaryComponent = withSummarizer(summerizer)
const EditComponent = defineComponent({
  //Todo: how to define props and emits in defineComponent
  // eslint-disable-next-line vue/require-prop-types
  props: ["modelValue"],
  emits: ["update:modelValue"],
  setup(props, { emit }) {
    return () =>
      h(TextInput, {
        id: "testId",
        ariaLabel: "testAriaLabel",
        modelValue: props.modelValue.text,
        class: ["my-24 ds-input-medium"],
        "onUpdate:modelValue": (value) => emit("update:modelValue", value),
      })
  },
})

const defaultValue = new ExtendedEditableListItem()
const emptyList = ref([])
</script>

<template>
  <KitchensinkPage name="Editable list">
    <KitchensinkStory name="With entries">
      <EditableList
        v-model="listWithEntries"
        :default-value="defaultValue"
        :edit-component="EditComponent"
        :summary-component="SummaryComponent"
      />
    </KitchensinkStory>
    <KitchensinkStory name="With no entries">
      <EditableList
        v-model="emptyList"
        :default-value="defaultValue"
        :edit-component="EditComponent"
        :summary-component="SummaryComponent"
      />
    </KitchensinkStory>
  </KitchensinkPage>
</template>
