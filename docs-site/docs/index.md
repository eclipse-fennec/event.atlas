---
layout: home

hero:
  name: Fennec Event Atlas
  text: EMF models as SensiNact digital twins
  tagline: Describe how a device payload becomes a provider — in an XMI mapping, not in transformation code.
  image:
    src: /fennec-logo.png
    alt: Eclipse Fennec logo
  actions:
    - theme: brand
      text: User Manual
      link: /guides/sensinact-mapping
    - theme: alt
      text: View on GitHub
      link: https://github.com/eclipse-fennec/event.atlas

features:
  - icon: 🗺️
    title: Declarative mapping
    details: A mapping says "for objects of this EClass, build a provider with these services and resources, taking each value from this feature path". No hand-written transformation code.
    link: /guides/sensinact-mapping
    linkText: Read the guide
  - icon: 🧭
    title: Feature paths
    details: Values are addressed by an ordered list of EMF structural features, so nested payloads, collections and cross-references all resolve from the model itself — validated against the metamodel.
  - icon: 🧩
    title: Mapping profiles
    details: Several vendor models can present the same provider shape. A profile declares the target structure with required services, resources, types and units; conformance is checked on registration.
  - icon: ⏱️
    title: Persistence rules
    details: Reusable change and deletion rules — percentage, absolute delta, every-Nth, time throttle, retention — referenced per resource to control what the history provider keeps.
  - icon: 🔌
    title: OSGi whiteboard
    details: Register a ProviderMapping (or MappingProfile) as a service and the southbound builds the provider model in the twin. Hot-reloadable, no restart.
  - icon: ☕
    title: Plain Java core
    details: The mapping engine is ordinary Java — ValueMapper against a SensinactDigitalTwin — usable and testable without an OSGi framework.
---

## About Fennec Event Atlas

Fennec Event Atlas turns instances of a **domain-specific EMF model** into
[Eclipse SensiNact](https://projects.eclipse.org/projects/technology.sensinact)
digital-twin providers. Instead of writing a transformation per device type, you write a
mapping: which `EClass` it applies to, what the provider is called, and which feature path
feeds each resource. The SensiNact southbound registers the mapping as a service, and every
incoming instance is projected onto the twin automatically.

- **Input:** any EMF model instance (an `EObject`) plus a mapping XMI.
- **Output:** a SensiNact provider / service / resource structure, updated in the digital twin.
- **No code:** mappings are configuration — hot-reloadable, validated against the EMF
  metamodel, and reusable across device types.

Start with the **[SensiNact mapping guide](/guides/sensinact-mapping)** — it walks from a
minimal battery-sensor mapping through timestamps, collections, admin services, profiles and
persistence rules.
