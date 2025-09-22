<script lang="ts" setup>
import Button from "primevue/button"
import InputText from "primevue/inputtext"
import { onMounted, ref, watch } from "vue"
import InputField from "@/components/input/InputField.vue"
import { useValidationStore } from "@/composables/useValidationStore"
import ParticipatingJudge from "@/domain/participatingJudge"

const props = defineProps<{
  modelValue?: ParticipatingJudge
  modelValueList?: ParticipatingJudge[]
}>()

const emit = defineEmits<{
  "update:modelValue": [value: ParticipatingJudge]
  addEntry: [void]
  cancelEdit: [void]
  removeEntry: [value?: boolean]
}>()

const lastSavedModelValue = ref(new ParticipatingJudge({ ...props.modelValue }))
const participatingJudge = ref(new ParticipatingJudge({ ...props.modelValue }))

const validationStore =
  useValidationStore<(typeof ParticipatingJudge.fields)[number]>()

function validateRequiredInput() {
  validationStore.reset()
  if (participatingJudge.value.missingRequiredFields?.length) {
    for (const missingField of participatingJudge.value.missingRequiredFields) {
      validationStore.add("Pflichtfeld nicht befüllt", missingField)
    }
  }
}

async function addParticipatingJudge() {
  await validateRequiredInput()
  emit("update:modelValue", participatingJudge.value as ParticipatingJudge)
  emit("addEntry")
}

watch(
  participatingJudge,
  () => {
    if (
      !participatingJudge.value.nameIsSet &&
      !participatingJudge.value.isEmpty
    ) {
      validationStore.add("Pflichtfeld nicht befüllt", "name")
    } else if (participatingJudge.value.nameIsSet) {
      validationStore.remove("name")
    }
  },
  { deep: true },
)

watch(
  () => props.modelValue,
  () => {
    participatingJudge.value = new ParticipatingJudge({ ...props.modelValue })
    lastSavedModelValue.value = new ParticipatingJudge({ ...props.modelValue })
    if (lastSavedModelValue.value.isEmpty) validationStore.reset()
  },
)

onMounted(() => {
  if (props.modelValue?.isEmpty !== undefined) {
    validateRequiredInput()
  }
  participatingJudge.value = new ParticipatingJudge({ ...props.modelValue })
})
</script>

<template>
  <div class="flex flex-col gap-24">
    <div class="flex justify-between gap-24">
      <InputField
        id="participatingJudgeNameInput"
        v-slot="slotProps"
        data-testid="participating-judge-name"
        label="Richter *"
        :validation-error="validationStore.getByField('name')"
      >
        <InputText
          id="participatingJudgeNameInputText"
          v-model="participatingJudge.name"
          aria-label="Name des Richters"
          data-testid="participating-judge-name-input"
          :invalid="slotProps.hasError"
          size="small"
          @focus="validationStore.remove('name')"
        ></InputText>
      </InputField>
      <InputField
        id="participatingJudgeReferencedOpinionsInput"
        data-testid="participating-judge-reference-opinions"
        label="Art der Mitwirkung"
      >
        <InputText
          id="participatingJudgeReferencedOpinionsTextInput"
          v-model="participatingJudge.referencedOpinions"
          aria-label="Art der Mitwirkung"
          data-testid="participating-judge-reference-opinions-input"
          size="small"
        ></InputText>
      </InputField>
    </div>
    <div class="flex w-full flex-row justify-between">
      <div>
        <div class="flex gap-16">
          <Button
            aria-label="Mitwirkenden Richter speichern"
            :disabled="
              !participatingJudge.nameIsSet || participatingJudge.isEmpty
            "
            label="Übernehmen"
            severity="secondary"
            size="small"
            @click.stop="addParticipatingJudge"
          ></Button>
          <Button
            v-if="!lastSavedModelValue.isEmpty"
            aria-label="Abbrechen"
            label="Abbrechen"
            size="small"
            text
            @click.stop="emit('cancelEdit')"
          ></Button>
        </div>
      </div>
      <Button
        v-if="!lastSavedModelValue.isEmpty"
        aria-label="Eintrag löschen"
        label="Eintrag löschen"
        severity="danger"
        size="small"
        @click.stop="emit('removeEntry', true)"
      ></Button>
    </div>
  </div>
</template>
