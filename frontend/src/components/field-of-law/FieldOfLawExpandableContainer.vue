<script lang="ts" setup>
import Button from "primevue/button"
import RadioButton from "primevue/radiobutton"
import { computed, nextTick, ref, watch } from "vue"
import { DocumentUnitCategoriesEnum } from "@/components/enumDocumentUnitCategories"
import FieldOfLawSummary from "@/components/field-of-law/FieldOfLawSummary.vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import { FieldOfLaw } from "@/domain/fieldOfLaw"
import IconAdd from "~icons/ic/baseline-add"

const props = defineProps<{
  fieldsOfLaw: FieldOfLaw[]
  isResetButtonVisible: boolean
}>()

const emit = defineEmits<{
  "node:remove": [node: FieldOfLaw]
  "node:clicked": [node: FieldOfLaw]
  editingDone: [void]
  resetSearch: [void]
  inputMethodSelected: [method: InputMethod]
}>()

const titleRef = ref<HTMLElement | null>(null)
const isExpanded = ref(false)
const inputMethod = ref(InputMethod.DIRECT)

const expandButtonLabel = computed(() => {
  return props.fieldsOfLaw.length > 0 ? "Weitere Angabe" : "Sachgebiete"
})

function removeNode(node: FieldOfLaw) {
  emit("node:remove", node)
}

function nodeClicked(node: FieldOfLaw) {
  emit("node:clicked", node)
  enterEditMode()
}

function enterEditMode() {
  isExpanded.value = true
}

async function exitEditMode() {
  isExpanded.value = false
  await nextTick()
  titleRef.value?.scrollIntoView({ block: "nearest" })
  emit("editingDone")
}

watch(
  inputMethod,
  () => {
    emit("inputMethodSelected", inputMethod.value)
  },
  { deep: true },
)
</script>
<script lang="ts">
export enum InputMethod {
  DIRECT = "direct",
  SEARCH = "search",
}
</script>
<template>
  <div>
    <div class="flex w-full items-start justify-between bg-white">
      <div class="flex w-full flex-col">
        <div class="flex w-full flex-row items-center justify-between">
          <h2
            :id="DocumentUnitCategoriesEnum.FIELDS_OF_LAW"
            ref="titleRef"
            class="ris-label1-bold mb-16"
          >
            Sachgebiete
          </h2>
        </div>
        <FieldOfLawSummary
          data-testid="field-of-law-summary"
          :fields-of-law="fieldsOfLaw"
          @node:clicked="nodeClicked"
          @node:remove="removeNode"
        />
      </div>
    </div>

    <div v-if="isExpanded" class="flex flex-col items-start gap-24">
      <div class="flex w-full flex-row justify-between">
        <div class="flex flex-row gap-24">
          <InputField
            id="direct"
            label="Direkteingabe"
            :label-position="LabelPosition.RIGHT"
            @click="() => (inputMethod = InputMethod.DIRECT)"
          >
            <RadioButton
              id="direct"
              v-model="inputMethod"
              aria-label="Direkteingabe auswählen"
              name="input-method-group"
              size="small"
              value="direct"
            />
          </InputField>

          <InputField
            id="search"
            label="Suche"
            :label-position="LabelPosition.RIGHT"
            @click="inputMethod = InputMethod.SEARCH"
          >
            <RadioButton
              id="search"
              v-model="inputMethod"
              aria-label="Sachgebietsuche auswählen"
              name="input-method-group"
              size="small"
              value="search"
            />
          </InputField>
        </div>

        <div class="flex flex-row gap-8">
          <Button
            v-if="isResetButtonVisible && inputMethod === InputMethod.SEARCH"
            label="Suche zurücksetzen"
            severity="secondary"
            size="small"
            @click="emit('resetSearch')"
          ></Button>
          <Button label="Fertig" size="small" @click="exitEditMode"></Button>
        </div>
      </div>
      <slot />
    </div>

    <Button
      v-else
      :label="expandButtonLabel"
      severity="secondary"
      size="small"
      @click="enterEditMode"
      ><template #icon> <IconAdd /> </template
    ></Button>
  </div>
</template>
