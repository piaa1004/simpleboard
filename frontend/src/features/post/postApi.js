import { baseApi, unwrapEnvelope } from '@/api/baseApi'

const LIST_TAG = { type: 'Post', id: 'LIST' }

const listTags = (result) =>
  result ? [...result.map(({ numbers }) => ({ type: 'Post', id: numbers })), LIST_TAG] : [LIST_TAG]

/** 목록·검색·태그 조회는 type/data 파라미터만 다르다. */
const listParams = ({ mode, keyword }) =>
  mode === 'normal' ? {} : { type: mode, data: keyword }

export const postApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getPosts: builder.query({
      query: ({ mode, keyword, page, size }) => ({
        url: '/posts',
        params: { ...listParams({ mode, keyword }), page, size },
      }),
      transformResponse: (response) => unwrapEnvelope(response) ?? [],
      providesTags: listTags,
    }),

    /** 목록과 같은 태그를 물고 있어야 글 작성/삭제 한 번으로 둘 다 무효화된다. */
    getPostCount: builder.query({
      query: ({ mode, keyword }) => ({ url: '/posts/count', params: listParams({ mode, keyword }) }),
      transformResponse: (response) => unwrapEnvelope(response) ?? 0,
      providesTags: [LIST_TAG],
    }),

    getPost: builder.query({
      query: (postId) => `/posts/${postId}`,
      transformResponse: unwrapEnvelope,
      providesTags: (result, error, postId) => [{ type: 'Post', id: postId }],
    }),

    increaseViews: builder.mutation({
      query: (postId) => ({ url: `/posts/views/${postId}`, method: 'PATCH' }),
    }),

    likePost: builder.mutation({
      query: (postId) => ({ url: `/posts/likes/${postId}`, method: 'PATCH' }),
      invalidatesTags: (result, error, postId) => [{ type: 'Post', id: postId }],
    }),

    createPost: builder.mutation({
      query: (body) => ({ url: '/posts', method: 'POST', body }),
      invalidatesTags: [LIST_TAG],
    }),

    updatePost: builder.mutation({
      query: ({ postId, body }) => ({ url: `/posts/${postId}`, method: 'PUT', body }),
      invalidatesTags: (result, error, { postId }) => [{ type: 'Post', id: postId }, LIST_TAG],
    }),

    deletePost: builder.mutation({
      query: (postId) => ({ url: `/posts/${postId}`, method: 'DELETE' }),
      invalidatesTags: [LIST_TAG],
    }),

    getComments: builder.query({
      query: (postId) => `/comments/${postId}`,
      transformResponse: (response) => unwrapEnvelope(response) ?? [],
      providesTags: (result, error, postId) => [{ type: 'Comment', id: postId }],
    }),

    addComment: builder.mutation({
      query: ({ postId, content }) => ({
        url: '/comments',
        method: 'POST',
        body: { postId, content },
      }),
      // 목록의 댓글 수까지 같이 갱신되어야 한다.
      invalidatesTags: (result, error, { postId }) => [{ type: 'Comment', id: postId }, LIST_TAG],
    }),

    deleteComment: builder.mutation({
      query: ({ commentId }) => ({ url: `/comments/${commentId}`, method: 'DELETE' }),
      invalidatesTags: (result, error, { postId }) => [{ type: 'Comment', id: postId }, LIST_TAG],
    }),
  }),
})

export const {
  useGetPostsQuery,
  useGetPostCountQuery,
  useGetPostQuery,
  useIncreaseViewsMutation,
  useLikePostMutation,
  useCreatePostMutation,
  useUpdatePostMutation,
  useDeletePostMutation,
  useGetCommentsQuery,
  useAddCommentMutation,
  useDeleteCommentMutation,
} = postApi
