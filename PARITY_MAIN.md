# 1.21.1 to 26.1 parity audit

Audit date: 2026-09-26. Branch heads compared: `origin/1.21.1` at `bfdf12c0`, `origin/main` at `d02dd030`. Rechecked on 2026-09-27 against `origin/1.21.1` at `7cb7df99`: `8e6288ab`, `58321be5`, `ad0eedfa`, `2aa5a7c9`, `c7ca1506` and `6c52545c` need nothing on main (already there, or version bump only).

This file tracks remaining tasks only. Remove each task after its fix is verified and committed. Commit hashes refer to the 1.21.1 source changes unless stated otherwise.

## Port from 1.21.1

- [ ] **Thaumometer under Iris shaders** (the Iris part of `085881bc`). Ported on 2026-09-27, not yet checked with shaders: `mixin/iris/pathways/HandRendererMixin` makes Iris treat the thaumometer as translucent, and `ThaumometerHandRenderer` skips Iris's solid hand pass, so the thaumometer draws once, in the translucent pass. The mixin only applies when Iris is loaded (`TCMixinPlugin`), and its target was checked against the Iris 1.11.3 jar. To verify, run with Sodium, Iris and a shader pack, hold the thaumometer and check it draws once and nodes show through the lens. Remove this entry once that is done.
