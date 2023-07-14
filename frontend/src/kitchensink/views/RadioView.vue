<script setup lang="ts">
import { reactive } from "vue"
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
  <h2 class="ds-heading-02-reg">Radio</h2>

  <h3 class="ds-heading-03-bold my-16">Regular</h3>
  <InputField id="r1" label="Regular 1" :label-position="LabelPosition.RIGHT">
    <RadioInput v-model="radioValues.regular" name="regular" value="r1" />
  </InputField>
  <InputField id="r2" label="Regular 2" :label-position="LabelPosition.RIGHT">
    <RadioInput v-model="radioValues.regular" name="regular" value="r2" />
  </InputField>
  <InputField id="r3" label="Regular 3" :label-position="LabelPosition.RIGHT">
    <RadioInput v-model="radioValues.regular" name="regular" value="r3" />
  </InputField>

  <h3 class="ds-heading-03-bold my-16">Medium</h3>
  <InputField id="m1" label="Medium 1" :label-position="LabelPosition.RIGHT">
    <RadioInput
      v-model="radioValues.medium"
      name="medium"
      size="medium"
      value="m1"
    />
  </InputField>
  <InputField id="m2" label="Medium 2" :label-position="LabelPosition.RIGHT">
    <RadioInput
      v-model="radioValues.medium"
      name="medium"
      size="medium"
      value="m2"
    />
  </InputField>
  <InputField id="m3" label="Medium 3" :label-position="LabelPosition.RIGHT">
    <RadioInput
      v-model="radioValues.medium"
      name="medium"
      size="medium"
      value="m3"
    />
  </InputField>

  <h3 class="ds-heading-03-bold my-16">Small</h3>
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

  <h3 class="ds-heading-03-bold my-16">Disabled</h3>
  <InputField id="d1" label="Disabled 1" :label-position="LabelPosition.RIGHT">
    <RadioInput
      v-model="radioValues.disabled"
      disabled
      name="disabled"
      value="d1"
    />
  </InputField>
  <InputField id="d2" label="Disabled 2" :label-position="LabelPosition.RIGHT">
    <RadioInput
      v-model="radioValues.disabled"
      disabled
      name="disabled"
      value="d2"
    />
  </InputField>
  <InputField id="d3" label="Disabled 3" :label-position="LabelPosition.RIGHT">
    <RadioInput
      v-model="radioValues.disabled"
      disabled
      name="disabled"
      value="d3"
    />
  </InputField>
</template>
