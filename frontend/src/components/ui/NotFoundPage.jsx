import { Link } from 'react-router'

export default function NotFoundPage() {
  return (
    <div className="flex min-h-full flex-col items-center justify-center gap-4 py-20">
      <h1 className="text-4xl font-bold">404</h1>
      <p className="text-base-content/70">요청하신 페이지를 찾을 수 없습니다.</p>
      <Link to="/posts" className="btn btn-primary">
        게시판으로 이동
      </Link>
    </div>
  )
}
