import { baseApi, unwrapEnvelope } from '@/api/baseApi'

export const authApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    /** 이 쿼리의 결과가 곧 로그인 상태다. 401 이면 비로그인. */
    getMe: builder.query({
      query: () => '/users',
      transformResponse: unwrapEnvelope,
      providesTags: ['User'],
    }),

    login: builder.mutation({
      query: (body) => ({ url: '/logins', method: 'POST', body }),
      invalidatesTags: ['User'],
    }),

    logout: builder.mutation({
      query: () => ({ url: '/user/logout', method: 'POST' }),
      invalidatesTags: ['User'],
    }),

    register: builder.mutation({
      query: (body) => ({ url: '/registers', method: 'POST', body }),
    }),

    /** 회원가입 전 아이디/이메일 중복 확인. 성공(2xx)이면 사용 가능. */
    checkUserAvailable: builder.query({
      query: ({ id, email }) => ({ url: '/users/exists', params: { id, email } }),
    }),

    /** 회원가입용 이메일 인증번호 발송. */
    sendMailCode: builder.mutation({
      query: (body) => ({ url: '/mail/send', method: 'POST', body }),
    }),

    /** 인증번호 확인. 서버가 이 이메일을 인증됨으로 표시하고, 그래야 회원가입이 통과한다. */
    checkMailCode: builder.mutation({
      query: (body) => ({ url: '/mail/check', method: 'POST', body }),
    }),
  }),
})

export const {
  useGetMeQuery,
  useLoginMutation,
  useLogoutMutation,
  useRegisterMutation,
  useLazyCheckUserAvailableQuery,
  useSendMailCodeMutation,
  useCheckMailCodeMutation,
} = authApi
