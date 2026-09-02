import { Link } from 'react-router'
import { ArrowLeftIcon } from '@/components/ui/icons'

/** 헤더 없는 인증 화면(로그인·회원가입)의 공통 껍데기. */
export default function AuthShell({ title, backTo = '/posts', children }) {
  return (
    <div className="flex min-h-full items-center justify-center bg-base-100 px-4 py-10">
      <div className="card w-full max-w-md border border-base-300 bg-base-100">
        <div className="card-body">
          <div className="mb-2 flex items-center gap-2">
            <Link to={backTo} className="btn btn-ghost btn-sm btn-circle" aria-label="뒤로">
              <ArrowLeftIcon />
            </Link>
            <h1 className="card-title">{title}</h1>
          </div>
          {children}
        </div>
      </div>
    </div>
  )
}
