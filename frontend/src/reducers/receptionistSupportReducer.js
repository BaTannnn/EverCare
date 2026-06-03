import { defaultSupportSchedule } from "../pages/receptionist/receptionistSupportUtils";

export const receptionistSupportInitialState = {
  conversations: [],
  selectedId: null,
  messages: [],
  doctors: [],
  filters: { status: "", keyword: "" },
  loading: true,
  messagesLoading: false,
  aiLoading: false,
  aiSuggestion: null,
  aiError: "",
  submitting: false,
  error: "",
  notice: "",
  messageText: "",
  schedule: defaultSupportSchedule(),
  conversationPage: 1,
};

export const receptionistSupportActionTypes = {
  SET_FIELD: "SET_FIELD",
  PATCH: "PATCH",
};

export function receptionistSupportReducer(state, action) {
  switch (action.type) {
    case receptionistSupportActionTypes.SET_FIELD: {
      const currentValue = state[action.field];
      const nextValue = typeof action.value === "function" ? action.value(currentValue) : action.value;

      return {
        ...state,
        [action.field]: nextValue,
      };
    }
    case receptionistSupportActionTypes.PATCH:
      return {
        ...state,
        ...action.payload,
      };
    default:
      return state;
  }
}
