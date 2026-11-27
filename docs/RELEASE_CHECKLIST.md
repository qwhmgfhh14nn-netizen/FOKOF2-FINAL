# FOKOF2 release checklist (dev candidate)

## Which APK is which
- Candidate = build of `dev` head (CI "review-output" commit.txt must equal the push commit). NOT yet published.
- Public `latest-debug` release currently points at main 9b8f1b1 (asset FOKOF2-debug.apk, updated 2026-11-26 11:42Z). This is the OLD build. Do not mistake it for the candidate.
- Release gate: call an APK delivered only after latest-debug target == main head, main build succeeded, asset timestamp fresh.

## Static evidence (checked, no device)
- Room DB version is 14 on both main and dev; no diff under data/local between them; migrations 11->14 present; destructive fallback only on downgrade. So installing dev over main does not change the schema.
- Catalog version 7 on dev: in-place edits re-sync on update and keep owner price/publish state.
- Signing: both builds use the repo debug.keystore, so install-over works. Open decision: release keystore.

## Fixture upgrade test (needs a phone or emulator; NOT run, no device available to the build agent)
Use only fixture data, never real user data. Install the candidate OVER the old build (never uninstall).
1. On the OLD build create disposable project "FIXTURE-A" (one text, one shape). Save. Close.
2. Install candidate APK over it. Open app. Expect: no crash, FIXTURE-A in Projects, opens with same elements.
3. In the candidate create "FIXTURE-2PAGE": add page 2, put different text on each page. Press Home (background), wait 1 min, reopen.
4. Expect: both pages present, switching pages works, text intact. Known limitation: autosave/saved project stores ONLY the current page, and undo resets on page switch. Record whether page 1 text survives after reopen.
5. Export dialog: "PDF for all pages" and PNG. Expect both pages in the PDF. Open the file.
6. Record result, device model, Android version, candidate commit.
Result: NOT RUN (pending a device). Known limitation recorded above.
