import { Outlet } from 'react-router'
import Header from './Header'

export default function AppLayout() {
  return (
    <div className="min-h-full bg-base-100">
      <Header />
      <main className="mx-auto w-full max-w-6xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
