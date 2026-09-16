import { Bot, BriefcaseBusiness } from 'lucide-react'
import type { SourceBot } from './api'

export function SourceBotIcon({ bot, size = 16 }: { bot: SourceBot; size?: number }) {
  return bot === 'HERMES' ? <Bot size={size} aria-hidden /> : <BriefcaseBusiness size={size} aria-hidden />
}
