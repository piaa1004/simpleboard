import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'

/**
 * 모든 API 호출은 /api 로 나가고 Vite 프록시(vite.config.js)가 접두사를 떼어
 * 백엔드 루트(`/posts`, `/users` ...)로 넘긴다. 운영 리버스 프록시도 같은 규칙이어야 한다.
 */
const rawBaseQuery = fetchBaseQuery({
  baseUrl: '/api',
  credentials: 'include', // 인증이 HttpOnly 쿠키 기반이다.
})

const STATUS_MESSAGE = {
  400: '요청 값을 확인해주세요.',
  401: '로그인 후에 이용해주세요.',
  403: '권한이 없습니다.',
  404: '요청한 데이터를 찾을 수 없습니다.',
  500: '서버에서 오류가 발생했습니다.',
}

/** 액세스 토큰이 재발급됐다는 신호. 서버의 ResponseCode.CREATE_ACCESS_TOKEN 과 같은 값이다. */
const ACCESS_TOKEN_REISSUED = '4'

/**
 * - 액세스 토큰이 만료되면 서버가 새 토큰을 쿠키에 심고 code 4 를 돌려준다. 그때 한 번만 재요청한다.
 * - 에러는 { status, message } 로 정규화해서 화면이 응답 구조를 몰라도 되게 한다.
 */
const baseQuery = async (args, api, extraOptions) => {
  let result = await rawBaseQuery(args, api, extraOptions)

  if (result.data?.code === ACCESS_TOKEN_REISSUED) {
    result = await rawBaseQuery(args, api, extraOptions)
  }

  if (result.error) {
    const { status, data } = result.error
    return {
      ...result,
      error: {
        status,
        message: data?.message ?? STATUS_MESSAGE[status] ?? '요청을 처리하지 못했습니다.',
      },
    }
  }

  return result
}

/** 서버 공통 봉투 `{ code, message, data }` 에서 data 만 꺼낸다. */
export const unwrapEnvelope = (response) => response?.data ?? null

export const baseApi = createApi({
  reducerPath: 'api',
  baseQuery,
  tagTypes: ['Post', 'Comment', 'User'],
  endpoints: () => ({}),
})
