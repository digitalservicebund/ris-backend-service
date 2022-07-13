<script lang="ts" setup>
import { computed } from "vue"
import { CoreData } from "../domain/docUnit"
import * as iconsAndLabels from "../iconsAndLabels.json"
import SimpleButton from "./SimpleButton.vue"

const props = defineProps<{ coreData: CoreData }>()
const emit = defineEmits<{
  (e: "updateValue", updatedValue: [keyof CoreData, string]): void
  (e: "updateDocUnit"): void
}>()

const data = computed(() =>
  iconsAndLabels.coreData.map((item) => {
    return {
      id: item.name as keyof CoreData,
      name: item.name,
      label: item.label,
      aria: item.label,
      icon: item.icon,
      value: props.coreData[item.name as keyof CoreData],
    }
  })
)

const updateValue = (event: Event, index: number) => {
  emit("updateValue", [
    data.value[index].id,
    (event.target as HTMLInputElement).value,
  ])
}
</script>

<template>
  <div v-if="!coreData">Loading...</div>
  <div v-else>
    <form novalidate class="ris-form" @submit="emit('updateDocUnit')">
      <v-row>
        <v-col><h2 id="coreData">Stammdaten</h2></v-col>
      </v-row>
      <v-row>
        <v-col cols="6">
          <!-- ^ removed md="6" because vue-tsc 0.36.0 throws an error TODO -->
          <template v-for="(item, index) in data">
            <div v-if="index <= 5" :key="item.id" class="ris-form__textfield">
              <v-icon class="icon_stammdaten">
                {{ item.icon }}
              </v-icon>
              <label :for="item.name" class="ris-form__label">
                {{ item.label }}
                <input
                  :id="item.id"
                  :value="item.value"
                  class="ris-form__input"
                  type="text"
                  :name="item.name"
                  :aria-label="item.aria"
                  @change="updateValue($event, index)"
                />
              </label>
            </div>
          </template>
        </v-col>
        <v-col cols="6">
          <template v-for="(item, index) in data">
            <div v-if="index > 5" :key="item.id" class="ris-form__textfield">
              <v-icon class="icon_stammdaten">
                {{ item.icon }}
              </v-icon>
              <label :for="item.name" class="ris-form__label">
                {{ item.label }}
                <input
                  :id="item.id"
                  :value="item.value"
                  class="ris-form__input"
                  type="text"
                  :name="item.name"
                  :aria-label="item.aria"
                  @change="updateValue($event, index)"
                />
              </label>
            </div>
          </template>
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <div class="ris-form__textfield">
            <SimpleButton @click="emit('updateDocUnit')" />
          </div>
        </v-col>
      </v-row>
    </form>
  </div>
</template>

<style lang="scss">
.ris-form {
  padding: rem(20px);

  &__textfield {
    padding: rem(20px);
  }

  &__input {
    width: 100%;
    padding: 17px 24px;
    margin-top: 5px;
    outline: 2px solid $text-tertiary;
    resize: vertical;

    &:hover,
    &:focus {
      // outline-width: 4px;
    }
  }

  &__label {
    padding: 12px 12px 20px 0;
  }
}
.icon_stammdaten {
  margin: 0 5px 5px 0;
}
</style>
