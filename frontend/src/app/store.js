import { configureStore } from '@reduxjs/toolkit'
import { setupListeners } from '@reduxjs/toolkit/query'

import { baseApi } from '@/api/baseApi'

/**
 * 로그인 상태를 따로 들고 있지 않는다. `useGetMeQuery()` 의 결과가 곧 로그인 상태다.
 * 스토어에 있는 것은 RTK Query 캐시뿐이다.
 */
export const store = configureStore({
  reducer: {
    [baseApi.reducerPath]: baseApi.reducer,
  },
  middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(baseApi.middleware),
})

setupListeners(store.dispatch)
