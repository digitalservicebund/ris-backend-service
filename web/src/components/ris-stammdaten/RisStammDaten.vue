<script lang="ts" setup>
import { onMounted, ref } from "vue"
import { updateDocUnit } from "../../api"
import { useDocUnitsStore } from "../../store"
import { DocUnit } from "../../types/DocUnit"
import RisButton from "../ris-button/RisButton.vue"

interface Props {
  docUnitId?: number
}

const props = defineProps<Props>()
const docUnitsStore = useDocUnitsStore()
const docUnit = ref<DocUnit>()

onMounted(() => {
  if (!props.docUnitId) return
  docUnitsStore.getAndSetSelected(props.docUnitId).then((du) => {
    docUnit.value = du
  })
})

const stammDatenList = [
  {
    id: "aktenzeichen",
    name: "Aktenzeichen",
    label: "Aktenzeichen",
    aria: "Aktenzeichen",
    icon: "grid_3x3",
  },
  {
    id: "gerichtstyp",
    name: "Gerichtstyp",
    label: "Gerichtstyp",
    aria: "Gerichtstyp",
    icon: "home",
  },
  {
    id: "dokumenttyp",
    name: "Dokumenttyp",
    label: "Dokumenttyp",
    aria: "Dokumenttyp",
    icon: "category",
  },
  {
    id: "vorgang",
    name: "Vorgang",
    label: "Vorgang",
    aria: "Vorgang",
    icon: "inventory_2",
  },
  {
    id: "ecli",
    name: "ECLI",
    label: "ECLI",
    aria: "ECLI",
    icon: "translate",
  },
  {
    id: "spruchkoerper",
    name: "Spruchkörper",
    label: "Spruchkörper",
    aria: "Spruchkörper",
    icon: "people_alt",
  },
  {
    id: "entscheidungsdatum",
    name: "Entscheidungsdatum",
    label: "Entscheidungsdatum",
    aria: "Entscheidungsdatum",
    icon: "calendar_today",
  },
  {
    id: "gerichtssitz",
    name: "Gerichtssitz",
    label: "Gerichtssitz",
    aria: "Gerichtssitz",
    icon: "location_on",
  },
  {
    id: "rechtskraft",
    name: "Rechtskraft",
    label: "Rechtskraft",
    aria: "Rechtskraft",
    icon: "gavel",
  },
  {
    id: "eingangsart",
    name: "Eingangsart",
    label: "Eingangsart",
    aria: "Eingangsart",
    icon: "markunread_mailbox",
  },
  {
    id: "dokumentationsstelle",
    name: "Dokumentationsstelle",
    label: "Dokumentationsstelle",
    aria: "Dokumentationsstelle",
    icon: "school",
  },
  {
    id: "region",
    name: "Region",
    label: "Region",
    aria: "Region",
    icon: "map",
  },
]

const onSubmit = () => {
  if (!docUnit.value) return
  updateDocUnit(docUnit.value).then((updatedDocUnit) => {
    docUnit.value = updatedDocUnit
    docUnitsStore.update(updatedDocUnit)
  })
  alert("Daten gespeichert")
}
</script>

<template>
  <div v-if="!docUnit">Loading...</div>
  <div v-else>
    <form novalidate class="ris-form" @submit.prevent="onSubmit">
      <v-row>
        <v-col cols="12" md="6">
          <template v-for="(item, index) in stammDatenList">
            <div v-if="index <= 5" :key="item.id" class="ris-form__textfield">
              <v-icon class="icon_stammdaten">
                {{ item.icon }}
              </v-icon>
              <label :for="item.name" class="ris-form__label">
                {{ item.label }}
                <input
                  :id="item.name"
                  v-model="docUnit[item.id]"
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
            <div v-if="index > 5" :key="item.id" class="ris-form__textfield">
              <v-icon class="icon_stammdaten">
                {{ item.icon }}
              </v-icon>
              <label :for="item.name" class="ris-form__label">
                {{ item.label }}
                <input
                  :id="item.name"
                  v-model="docUnit[item.id]"
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
.icon_stammdaten {
  margin: 0 5px 5px 0;
}
</style>
