<script lang="ts" setup>
import InputText from "primevue/inputtext"
import Message from "primevue/message"
import { reactive } from "vue"
import InputField from "@/components/input/InputField.vue"
import NestedComponent from "@/components/NestedComponents.vue"
import KitchensinkPage from "@/kitchensink/components/KitchensinkPage.vue"
import KitchensinkStory from "@/kitchensink/components/KitchensinkStory.vue"

const values = reactive({
  regularTextInput: "",
  textInputWithPlaceholder: "",
  textInputWithValue: "Hello world",
  invalidTextInput: "Invalid value",
  readonlyTextInput: "Read-only",
  mediumTextInput: "Large text input",
  dateInput: "",
  yearInput: "",
  timeInput: "",
})
</script>

<template>
  <KitchensinkPage name="Input Text">
    <Message severity="info">
      <p class="ris-body1-bold">Code convention for usage of inputs</p>
      <p>
        In order to ensure a consistent layout for inputs, the actual input
        field must always be wrapped with an InputField containing the label and
        (if necessary) the error message.
      </p>
    </Message>
    <KitchensinkStory name="Regular">
      <InputField
        id="regularTextInput"
        v-slot="{ id }"
        label="Regular text input"
      >
        <InputText
          :id="id"
          v-model="values.regularTextInput"
          aria-label="regular text input"
          size="small"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="With placeholder">
      <InputField
        id="textInputWithPlaceholder"
        v-slot="{ id }"
        label="Regular text input"
      >
        <InputText
          :id="id"
          v-model="values.textInputWithPlaceholder"
          aria-label="text input with placeholder"
          placeholder="Placeholder"
          size="small"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="With value">
      <InputField
        id="textInputWithValue"
        v-slot="{ id }"
        label="Regular text input"
      >
        <InputText
          :id="id"
          v-model="values.textInputWithValue"
          aria-label="text input with value"
          size="small"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Invalid">
      <InputField
        id="invalidTextInput"
        v-slot="{ id }"
        label="Regular text input"
      >
        <InputText
          :id="id"
          v-model="values.invalidTextInput"
          aria-label="invalid text input"
          invalid
          size="small"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Read-only">
      <InputField
        id="readonlyTextInput"
        v-slot="{ id }"
        label="Regular text input"
      >
        <InputText
          :id="id"
          v-model="values.readonlyTextInput"
          aria-label="readonly text input"
          readonly
          size="small"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory name="Large">
      <InputField id="mediumTextInput" v-slot="{ id }" label="Large text input">
        <InputText
          :id="id"
          v-model="values.mediumTextInput"
          aria-label="large text input"
          size="large"
        />
      </InputField>
    </KitchensinkStory>

    <KitchensinkStory class="w-320" name="Nested">
      <NestedComponent aria-label="Nested Component" class="w-full">
        <InputField id="smallTextInput" v-slot="{ id }" label="Parent Input">
          <InputText
            :id="id"
            aria-label="small text input"
            fluid
            size="small"
          />
        </InputField>
        <!-- Child  -->
        <template #children>
          <InputField id="smallTextInput" v-slot="{ id }" label="Child Input">
            <InputText
              :id="id"
              aria-label="small text input"
              fluid
              size="small"
            />
          </InputField>
        </template>
      </NestedComponent>
    </KitchensinkStory>
  </KitchensinkPage>
</template>
