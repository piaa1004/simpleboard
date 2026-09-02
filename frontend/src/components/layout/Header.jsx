import { useState } from 'react'
import { NavLink, useNavigate } from 'react-router'

import { baseApi } from '@/api/baseApi'
import { useGetMeQuery, useLogoutMutation } from '@/features/auth/authApi'
import { useDispatch } from 'react-redux'

export default function Header() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)

  const { data: me } = useGetMeQuery()
  const [logout] = useLogoutMutation()

  const onLogout = async () => {
    try {
      await logout().unwrap()
    } catch {
      /* 서버 응답과 무관하게 클라이언트 캐시는 비운다. */
    }

    setMenuOpen(false)
    dispatch(baseApi.util.resetApiState())
    navigate('/login', { replace: true })
  }

  return (
    <header className="sticky top-0 z-50 border-b border-base-300 bg-base-100">
      <div className="navbar mx-auto w-full max-w-6xl px-4">
        <div className="flex-1 gap-1">
          <NavLink to="/posts" className="btn btn-ghost text-lg font-bold">
            SimpleBoard
          </NavLink>
        </div>

        <div className="flex-none">
          {me ? (
            <div className="relative">
              <button
                type="button"
                className="btn btn-ghost gap-2"
                onClick={() => setMenuOpen((open) => !open)}
              >
                <span>{me.id} 님</span>
              </button>

              {menuOpen && (
                <ul className="menu absolute right-0 z-50 mt-2 w-48 rounded-box border border-base-300 bg-base-100 p-2 shadow-lg">
                  <li className="menu-title text-xs">{me.email}</li>
                  <li>
                    <button type="button" className="text-error" onClick={onLogout}>
                      로그아웃
                    </button>
                  </li>
                </ul>
              )}
            </div>
          ) : (
            <div className="flex gap-2">
              <NavLink to="/login" className="btn btn-sm btn-primary">
                로그인
              </NavLink>
              <NavLink to="/register" className="btn btn-sm btn-ghost">
                회원가입
              </NavLink>
            </div>
          )}
        </div>
      </div>
    </header>
  )
}
