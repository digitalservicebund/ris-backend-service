<script setup lang="ts">
import { reactive } from "vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"
import InputField, {
  LabelPosition,
} from "@/shared/components/input/InputField.vue"
import RadioInput from "@/shared/components/input/RadioInput.vue"

const radioValues = reactive({
  regular: "r1",
  small: "s1",
  medium: "m1",
  disabled: "d1",
})
</script>

<template>
  <KitchensinkPage name="Radio">
    <KitchensinkStory name="Regular">
      <InputField
        id="r1"
        label="Regular 1"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput v-model="radioValues.regular" name="regular" value="r1" />
      </InputField>
      <InputField
        id="r2"
        label="Regular 2"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput v-model="radioValues.regular" name="regular" value="r2" />
      </InputField>
      <InputField
        id="r3"
        label="Regular 3"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput v-model="radioValues.regular" name="regular" value="r3" />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Medium">
      <InputField
        id="m1"
        label="Medium 1"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          v-model="radioValues.medium"
          name="medium"
          size="medium"
          value="m1"
        />
      </InputField>
      <InputField
        id="m2"
        label="Medium 2"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          v-model="radioValues.medium"
          name="medium"
          size="medium"
          value="m2"
        />
      </InputField>
      <InputField
        id="m3"
        label="Medium 3"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          v-model="radioValues.medium"
          name="medium"
          size="medium"
          value="m3"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Small">
      <InputField id="s1" label="Small 1" :label-position="LabelPosition.RIGHT">
        <RadioInput
          v-model="radioValues.small"
          name="small"
          size="small"
          value="s1"
        />
      </InputField>
      <InputField id="s2" label="Small 2" :label-position="LabelPosition.RIGHT">
        <RadioInput
          v-model="radioValues.small"
          name="small"
          size="small"
          value="s2"
        />
      </InputField>
      <InputField id="s3" label="Small 3" :label-position="LabelPosition.RIGHT">
        <RadioInput
          v-model="radioValues.small"
          name="small"
          size="small"
          value="s3"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Disabled">
      <InputField
        id="d1"
        label="Disabled 1"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          v-model="radioValues.disabled"
          disabled
          name="disabled"
          value="d1"
        />
      </InputField>
      <InputField
        id="d2"
        label="Disabled 2"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          v-model="radioValues.disabled"
          disabled
          name="disabled"
          value="d2"
        />
      </InputField>
      <InputField
        id="d3"
        label="Disabled 3"
        :label-position="LabelPosition.RIGHT"
      >
        <RadioInput
          v-model="radioValues.disabled"
          disabled
          name="disabled"
          value="d3"
        />
      </InputField>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
