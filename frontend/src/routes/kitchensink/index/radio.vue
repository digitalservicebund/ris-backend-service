<script setup lang="ts">
import RadioButton from "primevue/radiobutton"
import { reactive } from "vue"
import InputField, { LabelPosition } from "@/components/input/InputField.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

const radioValues = reactive({
  group: "1",
  disabled: "1",
  sizes: "1",
})
</script>

<template>
  <KitchensinkPage name="Radio">
    <KitchensinkStory name="Group">
      <InputField id="1" label="Option 1" :label-position="LabelPosition.RIGHT">
        <RadioButton
          id="1"
          v-model="radioValues.group"
          name="regular"
          value="1"
        />
      </InputField>
      <InputField id="2" label="Option 2" :label-position="LabelPosition.RIGHT">
        <RadioButton
          id="2"
          v-model="radioValues.group"
          name="regular"
          value="2"
        />
      </InputField>
      <InputField id="3" label="Option 3" :label-position="LabelPosition.RIGHT">
        <RadioButton
          id="3"
          v-model="radioValues.group"
          name="regular"
          value="3"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Disabled">
      <InputField
        id="4"
        label="Disabled 1"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioButton
          id="4"
          v-model="radioValues.disabled"
          disabled
          name="regular"
          value="4"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Sizes">
      <InputField
        id="small"
        label="Small"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioButton
          id="small"
          v-model="radioValues.sizes"
          name="regular"
          size="small"
          value="small"
        />
      </InputField>
      <InputField
        id="large"
        label="Large"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioButton
          id="large"
          v-model="radioValues.sizes"
          name="large"
          value="large"
        />
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
