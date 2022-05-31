<script lang="ts" setup>
import { DocUnit } from "../../types/DocUnit"
import RisButton from "../ris-button/RisButton.vue"

interface Props {
  docUnit?: DocUnit
}

const props = defineProps<Props>()

const stammDatenList = [
  {
    id: "vorgang",
    name: "Vorgang",
    label: "Vorgang",
    aria: "Vorgang",
  },
  {
    id: "entscheidungsdatum",
    name: "Entscheidungsdatum",
    label: "Entscheidungsdatum",
    aria: "Entscheidungsdatum",
  },
  {
    id: "gerichtssitz",
    name: "Gerichtssitz",
    label: "Gerichtssitz",
    aria: "Gerichtssitz",
  },
  {
    id: "gericht",
    name: "Gericht",
    label: "Gericht",
    aria: "Gericht",
  },
  {
    id: "aktenzeichen",
    name: "Aktenzeichen",
    label: "Aktenzeichen",
    aria: "Aktenzeichen",
  },
  {
    id: "spruchkoerper",
    name: "Spruchkörper",
    label: "Spruchkörper",
    aria: "Spruchkörper",
  },
  {
    id: "eingangsart",
    name: "Eingangsart",
    label: "Eingangsart",
    aria: "Eingangsart",
  },
  {
    id: "dokumenttyp",
    name: "Dokumenttyp",
    label: "Dokumenttyp",
    aria: "Dokumenttyp",
  },
  {
    id: "eclinummer",
    name: "ECLI-Nummer",
    label: "ECLI-Nummer",
    aria: "ECLI-Nummer",
  },
]

const onSubmit = () => {
  alert("Daten gespeichert")
  console.log(props.docUnit)
}
</script>

<template>
  <!-- {{ props.docUnit.id }} -->
  <form novalidate class="ris-form" @submit.prevent="onSubmit">
    <v-row>
      <v-col cols="12" md="6">
        <template v-for="(item, index) in stammDatenList">
          <div v-if="index <= 4" :key="item.id" class="ris-form__textfield">
            <label :for="item.name" class="ris-form__label">
              {{ item.label }}
              <input
                :id="item.name"
                class="ris-form__input"
                type="text"
                :name="item.name"
                :aria-labelledby="item.aria"
              />
            </label>
          </div>
        </template>
      </v-col>
      <v-col cols="12" md="6">
        <template v-for="(item, index) in stammDatenList">
          <div v-if="index > 4" :key="item.id" class="ris-form__textfield">
            <label :for="item.name" class="ris-form__label">
              {{ item.label }}
              <input
                :id="item.name"
                class="ris-form__input"
                type="text"
                :name="item.name"
                :aria-labelledby="item.aria"
              />
            </label>
          </div>
        </template>
        <div class="ris-form__textfield">
          <RisButton type="submit" color="blue800" />
        </div>
      </v-col>
    </v-row>
  </form>
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
    outline: 2px solid $blue800;
    resize: vertical;

    &:hover,
    &:focus {
      outline-width: 4px;
    }
  }

  &__label {
    padding: 12px 12px 20px 0;
  }
}
</style>
