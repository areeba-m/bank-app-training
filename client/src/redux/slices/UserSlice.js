import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { userApi } from "../../services/UserApi.js";

export const fetchCurrentUser = createAsyncThunk(
  "user/fetchCurrentUser",
  async (_, { getState, dispatch, rejectWithValue }) => {
    try {
      return await userApi.getCurrentUser({
        getState,
        dispatch,
      });
    } catch (err) {
      return rejectWithValue(
        err?.message || "Failed to fetch user information",
      );
    }
  },
);

const initialState = {
  user: null,
  status: "idle",
  error: null,
};

const userSlice = createSlice({
  name: "user",

  initialState,

  reducers: {
    clearUserError: (state) => {
      state.error = null;
    },

    clearUser: (state) => {
      state.user = null;
      state.status = "idle";
      state.error = null;
    },
  },

  extraReducers: (builder) => {
    builder
      .addCase(fetchCurrentUser.pending, (state) => {
        state.status = "loading";
        state.error = null;
      })

      .addCase(fetchCurrentUser.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.user = action.payload;
        state.error = null;
      })

      .addCase(fetchCurrentUser.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload || "Failed to fetch user information";
      });
  },
});

export const { clearUserError, clearUser } = userSlice.actions;

export default userSlice.reducer;
