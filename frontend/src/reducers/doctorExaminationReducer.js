import {
  emptyRecordForm,
  emptyServiceForm,
} from "../pages/doctor/examinationWorkspaceUtils";

export const doctorExaminationInitialState = {
  workspace: {
    appointment: null,
    recordForm: emptyRecordForm,
    services: [],
    prescription: null,
  },
  serviceSearch: {
    form: emptyServiceForm,
    keyword: "",
    options: [],
    searching: false,
    dropdownOpen: false,
    selectedIds: [],
  },
  medicineSearch: {
    keyword: "",
    results: [],
    dropdownOpen: false,
    loading: false,
    error: "",
    activePresetField: null,
  },
  ui: {
    loading: true,
    savingRecord: false,
    completingRecord: false,
    savingService: false,
    savingPrescription: false,
    openingResultFileId: null,
    resultFileViewer: { show: false, url: "", title: "" },
    error: "",
    notice: "",
    completeModal: false,
    completeError: "",
  },
};

export const doctorExaminationActionTypes = {
  PATCH_SLICE: "PATCH_SLICE",
};

export function doctorExaminationReducer(state, action) {
  switch (action.type) {
    case doctorExaminationActionTypes.PATCH_SLICE: {
      const currentSlice = state[action.slice];
      const nextPatch = Object.fromEntries(
        Object.entries(action.payload).map(([key, value]) => [
          key,
          typeof value === "function" ? value(currentSlice[key]) : value,
        ])
      );

      return {
        ...state,
        [action.slice]: {
          ...currentSlice,
          ...nextPatch,
        },
      };
    }
    default:
      return state;
  }
}
