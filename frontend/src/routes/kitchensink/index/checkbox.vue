<script setup lang="ts">
import { reactive } from "vue"
import CheckboxInput from "@/components/CheckboxInput.vue"
import InputField from "@/components/InputField.vue"
import { LabelPosition } from "@/components/utils/enumLabelPosition"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

const values = reactive({
  regular: false,
  regularChecked: true,
  disabled: false,
  disabledChecked: true,
  invalid: false,
  invalidChecked: true,
  medium: false,
  small: false,
})
</script>

<template>
  <KitchensinkPage name="Checkbox">
    <KitchensinkStory name="Regular">
      <InputField
        id="regularCheckbox"
        v-slot="{ id }"
        label="Regular"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput :id="id" v-model="values.regular" />
      </InputField>

      <InputField
        id="regularCheckbox"
        v-slot="{ id }"
        label="Regular"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput :id="id" v-model="values.regularChecked" />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Disabled">
      <InputField
        id="disabledCheckbox"
        v-slot="{ id }"
        label="Disabled"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput :id="id" v-model="values.disabled" disabled />
      </InputField>

      <InputField
        id="disabledCheckbox"
        v-slot="{ id }"
        label="Disabled"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput :id="id" v-model="values.disabledChecked" disabled />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Invalid">
      <InputField
        id="invalidCheckbox"
        v-slot="{ id }"
        label="Invalid"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput
          :id="id"
          v-model="values.invalid"
          :validation-error="{
            message: 'Error',
            instance: 'invalidCheckbox',
          }"
        />
      </InputField>

      <InputField
        id="invalidCheckedCheckbox"
        v-slot="{ id }"
        label="Invalid"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput
          :id="id"
          v-model="values.invalidChecked"
          :validation-error="{
            message: 'Error',
            instance: 'invalidCheckedCheckbox',
          }"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Small">
      <InputField
        id="smallCheckbox"
        v-slot="{ id }"
        label="Small"
        :label-position="LabelPosition.RIGHT"
      >
        <CheckboxInput :id="id" v-model="values.small" size="small" />
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
