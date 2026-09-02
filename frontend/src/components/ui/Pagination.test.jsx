import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'

import Pagination from './Pagination'

describe('Pagination', () => {
  it('페이지가 하나뿐이면 렌더링하지 않는다', () => {
    const { container } = render(
      <Pagination page={1} totalCount={5} pageSize={10} onChange={() => {}} />,
    )
    expect(container).toBeEmptyDOMElement()
  })

  it('총 개수와 페이지 크기로 마지막 페이지를 계산한다', () => {
    render(<Pagination page={1} totalCount={95} pageSize={10} onChange={() => {}} />)
    expect(screen.getByRole('button', { name: '10' })).toBeInTheDocument()
  })

  it('현재 페이지를 다시 눌러도 onChange 를 호출하지 않는다', async () => {
    const onChange = vi.fn()
    render(<Pagination page={3} totalCount={100} pageSize={10} onChange={onChange} />)

    await userEvent.click(screen.getByRole('button', { name: '3' }))
    expect(onChange).not.toHaveBeenCalled()

    await userEvent.click(screen.getByRole('button', { name: '4' }))
    expect(onChange).toHaveBeenCalledWith(4)
  })
})
