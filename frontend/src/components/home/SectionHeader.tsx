import { Link } from 'react-router'

type Props = {
  title: string
  description?: string
  moreTo?: string
}

export function SectionHeader({ title, description, moreTo }: Props) {
  return (
    <div className="mb-4 flex items-end justify-between gap-4">
      <div>
        <h2 className="text-xl font-extrabold md:text-2xl">{title}</h2>
        {description && <p className="mt-1 text-sm text-gray-500 md:text-base">{description}</p>}
      </div>
      {moreTo && (
        <Link to={moreTo} className="shrink-0 text-sm font-semibold text-primary hover:underline">
          전체보기 ›
        </Link>
      )}
    </div>
  )
}
