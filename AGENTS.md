# AGENTS — Agent Customization Notes

Purpose: Help AI coding agents get productive quickly with this repository.

Quick commands
- Build (Unix): `./mvnw clean package`
- Build (Windows): `mvnw.cmd clean package`
- Run (dev): `./mvnw spring-boot:run` (or `mvnw.cmd spring-boot:run`)
- Run (helper): Use [scripts/run-local.ps1](scripts/run-local.ps1) to set `DB_PASSWORD` and run the wrapper
- Tests: `./mvnw test`

Environment & prerequisites
- Java: 17 (project property in `pom.xml`)
- Use the included Maven wrapper (`mvnw` / `mvnw.cmd`) — do not assume a system Maven version
- Database password: `DB_PASSWORD` environment variable (see `scripts/run-local.ps1`)

Key files and locations
- Project README: [README.md](README.md)
- Build config: [pom.xml](pom.xml)
- Local run helper: [scripts/run-local.ps1](scripts/run-local.ps1)
- Database schema: [database/create-schema.sql](database/create-schema.sql)
- Tests and test config: `src/test/` and [test-classes/application-test.yml](test-classes/application-test.yml)
- Main source: `src/main/java/`

Testing and CI notes
- Unit and integration tests run via Maven `test`; surefire reports are in `target/surefire-reports/`.
- If you change dependencies or annotation processors (Lombok), re-run a full build: `./mvnw clean package`.

Agent guidance (concise)
- Prefer linking to existing docs instead of copying them. See links above.
- Before editing code, run `./mvnw test` and, for runtime changes, `./mvnw spring-boot:run`.
- Keep changes minimal and focused; run tests locally and point to failing surefire logs in `target/surefire-reports/`.
- If making DB-related changes, include schema migration or an updated `database/create-schema.sql` sample.

Where to go next (suggestions)
- Create a short CONTRIBUTING.md if there are repo-specific developer workflows not covered here.
- Add small automation skills for running the test suite and collecting surefire logs.
