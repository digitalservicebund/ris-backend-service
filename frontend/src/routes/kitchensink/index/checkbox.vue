<script setup lang="ts">
import Checkbox from "primevue/checkbox"
import { reactive } from "vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

const values = reactive({
  regular: false,
  disabled: false,
  disabledChecked: true,
  invalid: false,
  invalidChecked: true,
  size: false,
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
        <Checkbox :id="id" v-model="values.regular" binary />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Disabled">
      <InputField
        id="disabledCheckbox"
        v-slot="{ id }"
        label="Disabled"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox :id="id" v-model="values.disabled" binary disabled />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Invalid">
      <InputField
        id="invalidCheckbox"
        v-slot="{ id }"
        label="Invalid"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox :id="id" v-model="values.invalid" binary invalid />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Sizes">
      <InputField
        id="mini"
        v-slot="{ id }"
        label="Normal"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox :id="id" v-model="values.size" value="Normal" />
      </InputField>
      <InputField
        id="small"
        v-slot="{ id }"
        label="Large"
        :label-position="LabelPosition.RIGHT"
      >
        <Checkbox :id="id" v-model="values.size" value="Large" />
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
