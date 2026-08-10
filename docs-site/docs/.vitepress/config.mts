import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitepress'
import { GUIDES, EXAMPLES } from '../../guides.mjs'

// Project coordinates are single-source in the workspace gradle.properties (read
// natively by Gradle and `-include`d by bnd) — read them here too, so a repository
// rename or a fork of this scaffolding never leaves a stale name in the docs.
const props = readFileSync(
  fileURLToPath(new URL('../../../gradle.properties', import.meta.url)),
  'utf8',
)
const prop = (key: string, fallback: string) =>
  props.match(new RegExp(`^${key}\\s*=\\s*(.+)$`, 'm'))?.[1].trim() ?? fallback

const org = prop('github_org', 'eclipse-fennec')
const repo = prop('github_repository', 'event.atlas')
const REPO_URL = `https://github.com/${org}/${repo}`

// Per-project docs are served under a versioned sub-path, matching the org
// convention (https://eclipse-fennec.github.io/<repo>/<version>/). The snapshot
// branch publishes to /<repo>/snapshot/; tagged releases / `latest` get added
// once the first release lands.
const version = process.env.DOCS_BRANCH || 'snapshot'
const base = `/${repo}/${version}/`

// Canonical published origin. Links that point OUTSIDE the current docs base
// (other doc versions) must be full URLs — VitePress auto-prepends `base` to any
// root-absolute (`/…`) link, which would otherwise double the path.
const SITE = `https://${org}.github.io/${repo}`

// Version selector. Only `snapshot` is deployed today; keep as data so adding
// `latest` and tagged versions later is a one-liner.
const versions = [{ text: 'snapshot', link: `${SITE}/snapshot/` }]

const guideItems = GUIDES.map((g) => ({ text: g.title, link: `/guides/${g.slug}` }))
const exampleItems = EXAMPLES.map((g) => ({ text: g.title, link: `/examples/${g.slug}` }))

export default defineConfig({
  title: 'Fennec Event Atlas',
  description:
    'Declarative mapping of EMF model instances onto Eclipse SensiNact digital-twin providers.',
  lang: 'en-US',
  base,
  cleanUrls: true,
  lastUpdated: true,
  ignoreDeadLinks: true,

  markdown: {
    // Shiki has no dedicated 'gradle' grammar; Gradle build files are Groovy.
    languageAlias: { gradle: 'groovy' },
  },

  head: [
    ['link', { rel: 'icon', type: 'image/png', href: `${base}fennec-logo.png` }],
    ['meta', { name: 'theme-color', content: '#c0631c' }],
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:title', content: 'Fennec Event Atlas' }],
    [
      'meta',
      {
        property: 'og:description',
        content:
          'Map EMF model instances onto Eclipse SensiNact digital twins — configuration, not code.',
      },
    ],
  ],

  themeConfig: {
    logo: '/fennec-logo.png',
    siteTitle: 'Fennec Event Atlas',

    nav: [
      { text: 'Home', link: '/' },
      { text: 'User Manual', items: guideItems },
      { text: 'Examples', items: exampleItems },
      { text: `version: ${version}`, items: versions },
    ],

    sidebar: {
      '/guides/': [{ text: 'User Manual', items: guideItems }],
      '/examples/': [{ text: 'Examples', items: exampleItems }],
    },

    socialLinks: [{ icon: 'github', link: REPO_URL }],

    search: { provider: 'local' },

    editLink: {
      pattern: `${REPO_URL}/edit/main/docs/:path`,
      text: 'Edit this page on GitHub',
    },

    footer: {
      message:
        'Released under the EPL-2.0 License. Eclipse Fennec is part of the Eclipse Foundation.',
      copyright: 'Copyright © Eclipse Foundation and contributors',
    },
  },
})
