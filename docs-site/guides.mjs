// The published, user-facing pages (allowlist). Shared by the sync script and the
// VitePress config so the set and its order are defined exactly once.
//   file  — source markdown in ../docs (the single source of truth)
//   slug  — route name under the section
//   title — sidebar / nav label
//
// GUIDES   -> /guides/   (the user manual)
// EXAMPLES -> /examples/ (worked examples)
export const GUIDES = [
  { file: 'sensinact-mapping-user-guide.md', slug: 'sensinact-mapping', title: 'SensiNact mapping' },
];

export const EXAMPLES = [
];
