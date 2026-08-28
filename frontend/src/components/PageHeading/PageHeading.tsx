import './PageHeading.css'

type PageHeadingProps = {
  eyebrow: string
  title: string
  description: string
}

export function PageHeading({ eyebrow, title, description }: PageHeadingProps) {
  return (
    <div className="page-heading">
      <p className="page-heading__eyebrow">{eyebrow}</p>
      <h1>{title}</h1>
      <p>{description}</p>
    </div>
  )
}
