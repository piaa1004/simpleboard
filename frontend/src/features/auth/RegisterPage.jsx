import { useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router'

import {
  useCheckMailCodeMutation,
  useLazyCheckUserAvailableQuery,
  useRegisterMutation,
  useSendMailCodeMutation,
} from './authApi'
import AuthShell from './AuthShell'
import Field, { CheckboxField } from '@/components/ui/Field'
import { inputClass } from '@/lib/form'
import { useCooldown } from '@/hooks/useCooldown'
import { useDialog } from '@/hooks/useDialog'

const EMPTY = { id: '', email: '', password: '', checkPassword: '' }
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
// 서버의 app.mail.resend-cooldown 과 맞춘다.
const RESEND_COOLDOWN = 30

export default function RegisterPage() {
  const navigate = useNavigate()
  const { toast } = useDialog()

  const idRef = useRef(null)
  const emailRef = useRef(null)
  const passwordRef = useRef(null)
  const checkPasswordRef = useRef(null)

  const [form, setForm] = useState(EMPTY)
  const [errors, setErrors] = useState({})
  const [showPassword, setShowPassword] = useState(false)

  const [code, setCode] = useState('')
  const [codeError, setCodeError] = useState('')
  const [codeSent, setCodeSent] = useState(false)
  const [resendLeft, startResendCooldown] = useCooldown(RESEND_COOLDOWN)

  const [checkUserAvailable] = useLazyCheckUserAvailableQuery()
  const [sendMailCode, { isLoading: sending }] = useSendMailCodeMutation()
  const [checkMailCode] = useCheckMailCodeMutation()
  const [register, { isLoading: registering }] = useRegisterMutation()

  const onChange = (event) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
    // 고치는 즉시 해당 칸의 오류를 지운다. 이미 해결한 메시지가 남아 있으면 혼란스럽다.
    setErrors((prev) => (prev[name] ? { ...prev, [name]: undefined } : prev))
  }

  /** 첫 오류에서 멈추지 않고 모든 오류를 모아서 돌려준다. */
  const validate = () => {
    const next = {}

    if (form.id.trim().length < 4) next.id = '아이디는 4자 이상 입력해주세요.'
    if (!EMAIL_PATTERN.test(form.email.trim())) next.email = '올바른 이메일 형식이 아닙니다.'
    if (form.password.length < 6) next.password = '비밀번호는 6자 이상 입력해주세요.'
    else if (form.password !== form.checkPassword) {
      next.checkPassword = '비밀번호가 일치하지 않습니다.'
    }

    return next
  }

  /** 화면에 보이는 순서대로 첫 오류 칸에 포커스를 준다. (이벤트 핸들러에서만 호출) */
  const focusFirstInvalid = (next) => {
    if (next.id) idRef.current?.focus()
    else if (next.email) emailRef.current?.focus()
    else if (next.password) passwordRef.current?.focus()
    else if (next.checkPassword) checkPasswordRef.current?.focus()
  }

  const requestCode = async () => {
    // 서버도 같은 검사를 하지만, 먼저 물어보면 어느 값이 문제인지 더 분명하게 알려줄 수 있다.
    await checkUserAvailable({ id: form.id.trim(), email: form.email.trim() }).unwrap()
    await sendMailCode({ email: form.email.trim() }).unwrap()
    startResendCooldown()
  }

  const onSendCode = async (event) => {
    event.preventDefault()

    const next = validate()
    setErrors(next)

    if (Object.values(next).some(Boolean)) {
      focusFirstInvalid(next)
      return
    }

    try {
      await requestCode()
      setCodeSent(true)
    } catch (error) {
      setErrors({ form: error.message })
    }
  }

  const onResend = async () => {
    try {
      await requestCode()
      toast('인증번호를 다시 보냈습니다.', 'success')
    } catch (error) {
      setCodeError(error.message)
    }
  }

  const onSubmitCode = async (event) => {
    event.preventDefault()

    if (!code.trim()) {
      setCodeError('메일로 받은 인증번호를 입력해주세요.')
      return
    }

    try {
      await checkMailCode({ email: form.email.trim(), code: code.trim() }).unwrap()

      const response = await register({
        id: form.id.trim(),
        email: form.email.trim(),
        password: form.password,
        checkPassword: form.checkPassword,
      }).unwrap()

      toast(response?.message ?? '회원가입이 완료되었습니다.', 'success')
      navigate('/login', { replace: true })
    } catch (error) {
      setCodeError(error.message)
    }
  }

  return (
    <AuthShell title="회원가입" backTo="/login">
      <ul className="steps mb-4 w-full text-xs">
        <li className="step step-primary">정보 입력</li>
        <li className={`step ${codeSent ? 'step-primary' : ''}`}>이메일 인증</li>
      </ul>

      {!codeSent ? (
        <form className="space-y-3" onSubmit={onSendCode} noValidate>
          <Field label="아이디" required error={errors.id} hint="4자 이상 입력해주세요.">
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

          <Field label="이메일" required error={errors.email} hint="인증번호를 받을 주소입니다.">
            <input
              ref={emailRef}
              className={inputClass(errors.email)}
              name="email"
              type="email"
              placeholder="you@example.com"
              autoComplete="email"
              value={form.email}
              onChange={onChange}
            />
          </Field>

          <Field label="비밀번호" required error={errors.password} hint="6자 이상 입력해주세요.">
            <input
              ref={passwordRef}
              className={inputClass(errors.password)}
              name="password"
              type={showPassword ? 'text' : 'password'}
              placeholder="비밀번호"
              autoComplete="new-password"
              value={form.password}
              onChange={onChange}
            />
          </Field>

          <Field label="비밀번호 확인" required error={errors.checkPassword}>
            <input
              ref={checkPasswordRef}
              className={inputClass(errors.checkPassword)}
              name="checkPassword"
              type={showPassword ? 'text' : 'password'}
              placeholder="비밀번호 확인"
              autoComplete="new-password"
              value={form.checkPassword}
              onChange={onChange}
            />
          </Field>

          <CheckboxField
            label="비밀번호 표시"
            checked={showPassword}
            onChange={(event) => setShowPassword(event.target.checked)}
          />

          {errors.form && (
            <div className="alert alert-error py-2 text-sm">
              <span>{errors.form}</span>
            </div>
          )}

          <button type="submit" className="btn btn-primary w-full" disabled={sending}>
            {sending && <span className="loading loading-spinner loading-sm" />}
            인증번호 전송
          </button>
        </form>
      ) : (
        <form className="space-y-3" onSubmit={onSubmitCode} noValidate>
          <p className="text-sm text-base-content/70">
            <span className="font-semibold">{form.email}</span> 로 인증번호를 보냈습니다. 메일이 오지
            않으면 스팸함도 확인해주세요.
          </p>

          <Field label="인증번호" required error={codeError} hint="6자리 숫자입니다.">
            <input
              className={inputClass(codeError)}
              autoFocus
              inputMode="numeric"
              maxLength={6}
              placeholder="메일로 받은 인증번호"
              value={code}
              onChange={(event) => {
                setCode(event.target.value)
                setCodeError('')
              }}
            />
          </Field>

          <button type="submit" className="btn btn-primary w-full" disabled={registering}>
            {registering && <span className="loading loading-spinner loading-sm" />}
            회원가입
          </button>

          <div className="flex gap-2">
            <button
              type="button"
              className="btn btn-ghost flex-1"
              onClick={() => setCodeSent(false)}
            >
              정보 수정
            </button>
            <button
              type="button"
              className="btn btn-ghost flex-1"
              disabled={resendLeft > 0 || sending}
              onClick={onResend}
            >
              {resendLeft > 0 ? `재전송 (${resendLeft}초)` : '인증번호 재전송'}
            </button>
          </div>
        </form>
      )}

      <p className="mt-6 text-center text-sm">
        이미 계정이 있으신가요?{' '}
        <Link to="/login" className="link link-primary">
          로그인
        </Link>
      </p>
    </AuthShell>
  )
}
