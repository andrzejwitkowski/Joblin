import type { LucideIcon } from 'lucide-react'
import {
  ArrowRight,
  Award,
  BadgeCheck,
  Bot,
  Briefcase,
  Building2,
  CheckCircle2,
  Circle,
  ClipboardCheck,
  Clock,
  Coins,
  Copy,
  Database,
  ExternalLink,
  Globe2,
  GraduationCap,
  Heart,
  Languages,
  Laptop,
  Layers,
  Link,
  MapPin,
  Sparkles,
  Terminal,
  TrendingUp,
} from 'lucide-react'

/** Material-style ids from backend OfferIcons.ALL → Lucide. */
const OFFER_ICON_MAP: Record<string, LucideIcon> = {
  work: Briefcase,
  corporate_fare: Building2,
  verified: BadgeCheck,
  monetization_on: Coins,
  location_on: MapPin,
  schedule: Clock,
  travel_explore: Globe2,
  link: Link,
  launch: ExternalLink,
  open_in_new: ExternalLink,
  content_copy: Copy,
  layers: Layers,
  assignment_turned_in: ClipboardCheck,
  check_circle: CheckCircle2,
  terminal: Terminal,
  workspace_premium: Award,
  school: GraduationCap,
  laptop_mac: Laptop,
  favorite: Heart,
  trending_up: TrendingUp,
  translate: Languages,
  dataset: Database,
  robot_2: Bot,
  auto_awesome: Sparkles,
  arrow_right: ArrowRight,
}

export function OfferIcon({
  name,
  className,
  size = 16,
}: {
  name?: string | null
  className?: string
  size?: number
}) {
  const Icon = (name && OFFER_ICON_MAP[name]) || Circle
  return <Icon className={className} size={size} aria-hidden />
}
