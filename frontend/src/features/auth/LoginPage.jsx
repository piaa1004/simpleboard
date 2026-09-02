import { useRef, useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router'

import { useGetMeQuery, useLoginMutation } from './authApi'
import { useDialog } from '@/hooks/useDialog'
import AuthShell from './AuthShell'
import Field from '@/components/ui/Field'
import { inputClass } from '@/lib/form'

export default function LoginPage() {
  const navigate = useNavigate()
  const { toast } = useDialog()
  const { data: me, isLoading: meLoading } = useGetMeQuery()
  const [login, { isLoading }] = useLoginMutation()

  const [form, setForm] = useState({ id: '', password: '' })
  const [errors, setErrors] = useState({})
  const idRef = useRef(null)
  const passwordRef = useRef(null)

  if (!meLoading && me) return <Navigate to="/posts" replace />

  const onChange = (event) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
    setErrors((prev) => (prev[name] ? { ...prev, [name]: undefined } : prev))
  }

  const onSubmit = async (event) => {
    event.preventDefault()

    const next = {}
    if (!form.id.trim()) next.id = '아이디를 입력해주세요.'
    if (!form.password) next.password = '비밀번호를 입력해주세요.'
    setErrors(next)

    if (next.id || next.password) {
      // 포커스 이동은 이벤트 핸들러 안에서만 한다.
      if (next.id) idRef.current?.focus()
      else passwordRef.current?.focus()
      return
    }

    try {
      const response = await login(form).unwrap()
      toast(response?.message ?? '로그인되었습니다.', 'success')
      navigate('/posts', { replace: true })
    } catch (error) {
      // 아이디·비밀번호 중 어느 쪽이 틀렸는지 서버가 구분해주지 않으므로 폼 하단에 함께 표시한다.
      setErrors({ form: error.message })
    }
  }

  return (
    <AuthShell title="로그인">
      <form className="space-y-3" onSubmit={onSubmit} noValidate>
        <Field label="아이디" error={errors.id}>
          <input
            ref={idRef}
            className={inputClass(errors.id)}
            name="id"
            placeholder="아이디"
            autoComplete="username"
            value={form.id}
            onChange={onChange}
          />
        </Field>

        <Field label="비밀번호" error={errors.password}>
          <input
            ref={passwordRef}
            className={inputClass(errors.password)}
            type="password"
            name="password"
            placeholder="비밀번호"
            autoComplete="current-password"
            value={form.password}
            onChange={onChange}
          />
        </Field>

        {errors.form && (
          <div className="alert alert-error py-2 text-sm">
            <span>{errors.form}</span>
          </div>
        )}

        <button type="submit" className="btn btn-primary w-full" disabled={isLoading}>
          {isLoading && <span className="loading loading-spinner loading-sm" />}
          로그인
        </button>
        <Link to="/posts" className="btn btn-ghost w-full">
          로그인 없이 보기
        </Link>
      </form>

      <p className="mt-6 text-center text-sm">
        계정이 없으신가요?{' '}
        <Link to="/register" className="link link-primary">
          회원가입
        </Link>
      </p>
    </AuthShell>
  )
}
