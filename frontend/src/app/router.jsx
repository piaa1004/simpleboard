import { createBrowserRouter, Navigate } from 'react-router'

import AppLayout from '@/components/layout/AppLayout'
import NotFoundPage from '@/components/ui/NotFoundPage'

import LoginPage from '@/features/auth/LoginPage'
import RegisterPage from '@/features/auth/RegisterPage'

import PostListPage from '@/features/post/PostListPage'
import PostViewPage from '@/features/post/PostViewPage'

/**
 * CKEditor 를 쓰는 글쓰기 화면만 route-level lazy 로 분리한다.
 * 첫 화면(게시글 목록)이 그 무거운 의존성을 같이 내려받지 않게 하기 위함이다.
 */
const lazyRoute = (load) => () => load().then((module) => ({ Component: module.default }))

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <Navigate to="/posts" replace /> },

      { path: 'posts', element: <PostListPage /> },
      { path: 'posts/new', lazy: lazyRoute(() => import('@/features/post/PostEditorPage')) },
      { path: 'posts/:postId', element: <PostViewPage /> },
      { path: 'posts/:postId/edit', lazy: lazyRoute(() => import('@/features/post/PostEditorPage')) },
    ],
  },

  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },

  { path: '*', element: <NotFoundPage /> },
])
