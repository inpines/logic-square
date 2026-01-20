# Responsibility‑First System Analysis (Minimal Version)

> **This repository defines the minimum responsibility and necessity a system must justify before any design or implementation is allowed.**

This is **not** a delivery framework, architecture guide, or design methodology.  
It is a **pre‑design accountability gate**.

If a system cannot pass this repository, it should not be built.

---

## What this repository is for

This minimal repository exists to answer **one question only**:

> **Does this system have the right to exist?**

It does so by forcing four non‑negotiable clarifications:

1. **Responsibility** — What the system is accountable for (and explicitly not).
    
2. **Necessity** — Why not building it creates unacceptable value loss or risk.
    
3. **World Recognition** — What entities and facts must be respected if it exists.
    
4. **Accountable Commitment** — What the system explicitly promises, and how failure is judged.
    

No flows, no UI, no architecture, no technology.

---

## What is intentionally excluded

The following are **deliberately omitted** in this minimal version:

- ❌ Non‑Functional Requirements (performance, availability, scalability)
    
- ❌ Analysis techniques (flow, sequence, activity)
    
- ❌ Architecture or technical decisions
    
- ❌ Implementation guidance
    

These elements **only become meaningful after existence is justified**.

---

## Repository structure (Minimal)

```text
responsibility-first-system-analysis/
├─ 00-index.md
├─ 01-responsibility-order/
│  ├─ what.md
│  ├─ why.md
│  └─ README.md
├─ 02-domain/
│  ├─ things.md
│  ├─ events.md
│  └─ README.md
├─ 03-requirements/
│  ├─ functional-requirements.md
│  ├─ acceptance-criteria.md
│  └─ README.md
└─ README.md
```

---

## Mandatory reading order

**Do not skip. Do not reorder.**

1. `01-responsibility-order/what.md`
    
2. `01-responsibility-order/why.md`
    
3. `02-domain/`
    
4. `03-requirements/`
    

If any layer fails, **stop**.

---

## Success criteria for this repository

A system passes this repository only if:

- Its responsibility is unambiguous
    
- Its non‑existence is demonstrably unacceptable
    
- Its world entities and facts are clearly named
    
- Its commitments are testable by a third party
    

Passing this repository **does not mean the system is well designed**.  
It only means the system is **ethically and structurally allowed to exist**.

---

## When to move beyond this repository

Only after all sections are complete and internally consistent should you proceed to:

- Analysis techniques
    
- Non‑functional constraints
    
- Architecture and implementation
    

Those belong to a **separate repository or phase**.

---

## One final rule

> **If this repository feels uncomfortable or restrictive, it is working.**

---

# 01‑responsibility‑order/README.md

This folder defines the **order of responsibility and necessity**.

Nothing below this folder may introduce:

- flows
    
- design decisions
    
- technical assumptions
    

Failure here invalidates everything downstream.

---

# 01‑responsibility‑order/what.md

## Purpose

Define **what the system is responsible for — and explicitly what it is not**.

This is not a feature list.

---

## One‑sentence definition (required)

> **This system is responsible for _[problem type]_ on behalf of _[role]_, and explicitly does not take responsibility for _[judgement / decision / value]_.**

---

## Responsibility boundaries

### In Scope

- …
    

### Out of Scope (mandatory)

- …
    

---

## Responsibility checks

-  No decisions are made on behalf of humans
    
-  No values are silently enforced
    
-  No convenience overrides accountability
    

---

# 01‑responsibility‑order/why.md

## Purpose

Explain **why not building this system creates unacceptable consequences**.

---

## Client‑Driven necessity (value)

- What value cannot be realized without this system?
    
- Why is human effort insufficient?
    

---

## Risk‑Driven necessity (loss)

- What risk compounds without this system?
    
- Why is mitigation impossible without structural change?
    

---

## Necessity verdict (required)

-  Optimization only (optional system)
    
-  Risk containment only (defensive system)
    
-  Both value and risk (structurally necessary system)
    

---

# 02‑domain/README.md

This folder defines **what exists in the world if the system exists**.

No workflows. No UI. No databases.

---

# 02‑domain/things.md

## Thing definition template

> A Thing is an entity with identity and lifecycle that someone must be accountable for.

### Name

- …
    

### Why it must exist

- Value alignment:
    
- Risk alignment:
    

### Responsibility

- Who is accountable:
    

---

# 02‑domain/events.md

## Event definition template

> An Event records a fact that has already happened.

### Name (past tense)

- …
    

### Meaning

- What fact became irreversible:
    

---

# 03‑requirements/README.md

This folder converts justified responsibility into **verifiable commitments**.

---

# 03‑requirements/functional‑requirements.md

## Functional Requirement template

### FR‑001 —

**System commitment**

> The system must be able to …

**Responsible Thing**

- …
    

**Justification**

- Value:
    
- Risk:
    

---

# 03‑requirements/acceptance‑criteria.md

## Acceptance Criteria template

### AC‑001 — corresponds to FR‑001

- Given:
    
- When:
    
- Then:
    

> If this criterion fails, responsibility has failed.