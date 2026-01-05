<script setup lang="ts">
import Checkbox from "primevue/checkbox"
import { reactive } from "vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

const values = reactive({
  regular: false,
  checked: true,
  disabled: false,
  disabledChecked: true,
  invalid: false,
  invalidChecked: true,
  size: false,
})
</script>

<template>
  <KitchensinkPage name="Checkbox">
    <InputField
      id="unchecked"
      v-slot="{ id }"
      label="Unchecked"
      :label-position="LabelPosition.RIGHT"
    >
      <Checkbox v-model="values.regular" binary :input-id="id" />
    </InputField>
    <InputField
      id="checked"
      v-slot="{ id }"
      label="Checked"
      :label-position="LabelPosition.RIGHT"
    >
      <Checkbox v-model="values.checked" binary :input-id="id" />
    </InputField>

    <KitchensinkStory name="Disabled">
      <InputField
        id="disabledCheckbox"
        v-slot="{ id }"
        label="Disabled"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox v-model="values.disabled" binary disabled :input-id="id" />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Invalid">
      <InputField
        id="invalidCheckbox"
        v-slot="{ id }"
        label="Invalid"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox v-model="values.invalid" binary :input-id="id" invalid />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Sizes">
      <InputField
        id="small"
        v-slot="{ id }"
        label="Small"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox v-model="values.size" :input-id="id" value="Small" />
      </InputField>
      <InputField
        id="large"
        v-slot="{ id }"
        label="Large"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox
          v-model="values.size"
          :input-id="id"
          size="large"
          value="Large"
        />
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
